package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.marketjournal.data.local.MarketJournalDatabase
import com.example.marketjournal.data.provider.MarketDataProviderManager
import com.example.marketjournal.data.repository.MarketJournalRepository
import com.example.marketjournal.ui.ConfigScreen
import com.example.marketjournal.ui.DailyReportsScreen
import com.example.marketjournal.ui.JournalScreen
import com.example.marketjournal.ui.MainViewModel
import com.example.marketjournal.ui.MainViewModelFactory
import com.example.marketjournal.ui.MarketJournalHeader
import com.example.marketjournal.ui.NavigationTab
import com.example.marketjournal.ui.ScannersScreen
import com.example.marketjournal.ui.SchedulerAuditScreen
import com.example.ui.theme.AccentSky
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        val database = MarketJournalDatabase.getDatabase(applicationContext)
        val providerManager = MarketDataProviderManager()
        val repository = MarketJournalRepository(database.marketJournalDao(), providerManager)
        MainViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MarketJournalApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MarketJournalApp(viewModel: MainViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val scannersState by viewModel.scannersState.collectAsStateWithLifecycle()
    val trackedPositions by viewModel.trackedPositions.collectAsStateWithLifecycle()
    val reportsList by viewModel.reportsList.collectAsStateWithLifecycle()
    val selectedReport by viewModel.selectedReport.collectAsStateWithLifecycle()
    val isGeneratingReport by viewModel.isGeneratingReport.collectAsStateWithLifecycle()
    val schedulerState by viewModel.schedulerState.collectAsStateWithLifecycle()
    val jobLogsList by viewModel.jobLogsList.collectAsStateWithLifecycle()
    val configState by viewModel.configState.collectAsStateWithLifecycle()
    val marketBreadth by viewModel.marketBreadth.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            MarketJournalHeader(
                calendarStatus = schedulerState.calendarStatus,
                providerType = configState.providerType,
                marketBreadth = marketBreadth,
                onRefresh = {
                    viewModel.refreshMarketData()
                    viewModel.refreshCalendarStatus()
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Slate800,
                contentColor = ProfitGreen
            ) {
                val navItems = listOf(
                    Triple(NavigationTab.SCANNERS, "Scanners", Icons.Default.Assessment),
                    Triple(NavigationTab.JOURNAL, "Journal", Icons.Default.Book),
                    Triple(NavigationTab.REPORTS, "EOD Reports", Icons.Default.Description),
                    Triple(NavigationTab.SCHEDULER, "Scheduler", Icons.Default.Schedule),
                    Triple(NavigationTab.CONFIG, "Config", Icons.Default.Settings)
                )

                navItems.forEach { (tab, label, icon) ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(tab) },
                        icon = { Icon(icon, contentDescription = label) },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Slate900,
                            selectedTextColor = ProfitGreen,
                            indicatorColor = ProfitGreen,
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400
                        ),
                        modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate900)
        ) {
            when (selectedTab) {
                NavigationTab.SCANNERS -> {
                    ScannersScreen(
                        state = scannersState,
                        onStrategySelected = { viewModel.setScannerStrategy(it) },
                        onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                        onMinScoreChanged = { viewModel.setMinScoreFilter(it) },
                        onTrackStock = { scored ->
                            viewModel.trackScoredStock(scored)
                        }
                    )
                }

                NavigationTab.JOURNAL -> {
                    JournalScreen(
                        positions = trackedPositions,
                        onUpdateStatus = { id, status, exitPrice ->
                            viewModel.updatePositionStatus(id, status, exitPrice)
                        },
                        onDeletePosition = { id ->
                            viewModel.deletePosition(id)
                        },
                        onAddCustomPosition = { symbol, name, strat, entry, qty, sl, t1, t2, notes ->
                            viewModel.addCustomPosition(symbol, name, strat, entry, qty, sl, t1, t2, notes)
                        }
                    )
                }

                NavigationTab.REPORTS -> {
                    DailyReportsScreen(
                        reports = reportsList,
                        selectedReport = selectedReport,
                        isGenerating = isGeneratingReport,
                        onSelectReport = { report ->
                            viewModel.selectReport(report)
                        },
                        onGenerateReport = { date, force ->
                            viewModel.triggerEodReportJob(date, force)
                        }
                    )
                }

                NavigationTab.SCHEDULER -> {
                    SchedulerAuditScreen(
                        state = schedulerState,
                        jobLogs = jobLogsList,
                        onTriggerJob = { date, force ->
                            viewModel.triggerEodReportJob(date, force)
                        },
                        onRefreshCalendar = {
                            viewModel.refreshCalendarStatus()
                        }
                    )
                }

                NavigationTab.CONFIG -> {
                    ConfigScreen(
                        state = configState,
                        onSwitchProvider = { type ->
                            viewModel.switchProvider(type)
                        },
                        onSaveBrokerConfig = { brokerConfig ->
                            viewModel.saveLiveBrokerConfig(brokerConfig)
                        }
                    )
                }
            }
        }
    }
}
