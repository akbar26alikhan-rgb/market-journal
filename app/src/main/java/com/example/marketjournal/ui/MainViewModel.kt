package com.example.marketjournal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.marketjournal.calendar.IndianTradingCalendar
import com.example.marketjournal.calendar.MarketStatusInfo
import com.example.marketjournal.data.local.DailyReportEntity
import com.example.marketjournal.data.local.JobExecutionLogEntity
import com.example.marketjournal.data.repository.MarketJournalRepository
import com.example.marketjournal.model.JobExecutionLog
import com.example.marketjournal.model.JournalPerformanceSnapshot
import com.example.marketjournal.model.MarketBreadthSummary
import com.example.marketjournal.model.MarketDataProviderType
import com.example.marketjournal.model.PositionStatus
import com.example.marketjournal.model.ProviderStatus
import com.example.marketjournal.model.ScannerStrategy
import com.example.marketjournal.model.ScoredStock
import com.example.marketjournal.model.TrackedPosition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class NavigationTab(val label: String) {
    SCANNERS("Scanners"),
    JOURNAL("Journal"),
    REPORTS("EOD Reports"),
    SCHEDULER("Scheduler & Audit"),
    CONFIG("Config & APIs")
}

data class ScannersUiState(
    val selectedStrategy: ScannerStrategy = ScannerStrategy.INTRADAY,
    val scoredStocks: List<ScoredStock> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val minScoreFilter: Int = 50,
    val errorMessage: String? = null
)

data class JournalUiState(
    val positions: List<TrackedPosition> = emptyList(),
    val showClosedOnly: Boolean = false,
    val snapshot: JournalPerformanceSnapshot = JournalPerformanceSnapshot(
        0, 0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
    )
)

data class ReportsUiState(
    val reports: List<DailyReportEntity> = emptyList(),
    val selectedReport: DailyReportEntity? = null,
    val isGenerating: Boolean = false,
    val lastGeneratedMessage: String? = null
)

data class SchedulerUiState(
    val jobLogs: List<JobExecutionLogEntity> = emptyList(),
    val calendarStatus: MarketStatusInfo = IndianTradingCalendar.getMarketStatus(),
    val isRunningJob: Boolean = false,
    val lastRunResult: JobExecutionLog? = null
)

data class ConfigUiState(
    val providerType: MarketDataProviderType = MarketDataProviderType.DEMO_DATA,
    val providerStatus: ProviderStatus = ProviderStatus.ACTIVE_DEMO,
    val liveBaseUrl: String = "https://api.kite.trade",
    val liveApiKey: String = "",
    val liveAccessToken: String = "",
    val saveMessage: String? = null
)

class MainViewModel(
    private val repository: MarketJournalRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(NavigationTab.SCANNERS)
    val selectedTab: StateFlow<NavigationTab> = _selectedTab.asStateFlow()

    private val _scannersState = MutableStateFlow(ScannersUiState())
    val scannersState: StateFlow<ScannersUiState> = _scannersState.asStateFlow()

    val trackedPositions: StateFlow<List<TrackedPosition>> = repository.allPositions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reportsList: StateFlow<List<DailyReportEntity>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val jobLogsList: StateFlow<List<JobExecutionLogEntity>> = repository.allJobLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedReport = MutableStateFlow<DailyReportEntity?>(null)
    val selectedReport: StateFlow<DailyReportEntity?> = _selectedReport.asStateFlow()

    private val _isGeneratingReport = MutableStateFlow(false)
    val isGeneratingReport: StateFlow<Boolean> = _isGeneratingReport.asStateFlow()

    private val _schedulerState = MutableStateFlow(SchedulerUiState())
    val schedulerState: StateFlow<SchedulerUiState> = _schedulerState.asStateFlow()

    private val _configState = MutableStateFlow(ConfigUiState())
    val configState: StateFlow<ConfigUiState> = _configState.asStateFlow()

    private val _marketBreadth = MutableStateFlow<MarketBreadthSummary?>(null)
    val marketBreadth: StateFlow<MarketBreadthSummary?> = _marketBreadth.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialJournalIfEmpty()
            refreshMarketData()
            refreshCalendarStatus()
        }
    }

    fun selectTab(tab: NavigationTab) {
        _selectedTab.value = tab
    }

    fun setScannerStrategy(strategy: ScannerStrategy) {
        _scannersState.value = _scannersState.value.copy(selectedStrategy = strategy)
        runScan(strategy)
    }

    fun setSearchQuery(query: String) {
        _scannersState.value = _scannersState.value.copy(searchQuery = query)
    }

    fun setMinScoreFilter(score: Int) {
        _scannersState.value = _scannersState.value.copy(minScoreFilter = score)
    }

    fun refreshMarketData() {
        viewModelScope.launch {
            _scannersState.value = _scannersState.value.copy(isLoading = true, errorMessage = null)
            val breadthResult = repository.getMarketBreadth()
            _marketBreadth.value = breadthResult.getOrNull()

            runScan(_scannersState.value.selectedStrategy)
            repository.refreshPositionsWithLatestPrices()
        }
    }

    private fun runScan(strategy: ScannerStrategy) {
        viewModelScope.launch {
            _scannersState.value = _scannersState.value.copy(isLoading = true, errorMessage = null)
            val scanResult = repository.scanStocks(strategy)
            scanResult.onSuccess { scored ->
                _scannersState.value = _scannersState.value.copy(
                    scoredStocks = scored,
                    isLoading = false
                )
            }.onFailure { err ->
                _scannersState.value = _scannersState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Failed to scan stocks"
                )
            }
        }
    }

    fun trackScoredStock(scored: ScoredStock, quantity: Int = 10, notes: String = "") {
        viewModelScope.launch {
            repository.addPositionFromScored(scored, quantity, notes)
        }
    }

    fun addCustomPosition(
        symbol: String,
        stockName: String,
        strategy: ScannerStrategy,
        entryPrice: Double,
        quantity: Int,
        stopLoss: Double,
        target1: Double,
        target2: Double,
        notes: String
    ) {
        viewModelScope.launch {
            repository.addCustomPosition(
                symbol = symbol,
                stockName = stockName,
                strategy = strategy,
                entryPrice = entryPrice,
                quantity = quantity,
                stopLoss = stopLoss,
                target1 = target1,
                target2 = target2,
                notes = notes
            )
        }
    }

    fun updatePositionStatus(id: Long, status: PositionStatus, exitPrice: Double? = null) {
        viewModelScope.launch {
            repository.updatePositionStatus(id, status, exitPrice)
        }
    }

    fun deletePosition(id: Long) {
        viewModelScope.launch {
            repository.deletePosition(id)
        }
    }

    fun selectReport(report: DailyReportEntity?) {
        _selectedReport.value = report
    }

    fun triggerEodReportJob(date: LocalDate = IndianTradingCalendar.currentIstDate(), force: Boolean = false) {
        viewModelScope.launch {
            _isGeneratingReport.value = true
            _schedulerState.value = _schedulerState.value.copy(isRunningJob = true)

            val log = repository.runScheduledEodReport(date, force)

            _schedulerState.value = _schedulerState.value.copy(
                isRunningJob = false,
                lastRunResult = log,
                calendarStatus = IndianTradingCalendar.getMarketStatus()
            )
            _isGeneratingReport.value = false

            // If success, select generated report
            val generated = repository.getReportByDate(log.executionDate)
            if (generated != null) {
                _selectedReport.value = generated
            }
        }
    }

    fun refreshCalendarStatus() {
        _schedulerState.value = _schedulerState.value.copy(
            calendarStatus = IndianTradingCalendar.getMarketStatus()
        )
    }

    fun switchProvider(type: MarketDataProviderType) {
        repository.providerManager.setProviderType(type)
        val status = repository.providerManager.getActiveProvider().getConfigurationStatus()
        _configState.value = _configState.value.copy(
            providerType = type,
            providerStatus = status,
            saveMessage = "Switched to ${type.name} mode."
        )
        refreshMarketData()
    }

    fun saveLiveConfig(baseUrl: String, apiKey: String, accessToken: String) {
        repository.providerManager.getLiveProvider().updateConfig(baseUrl, apiKey, accessToken)
        val status = repository.providerManager.getLiveProvider().getConfigurationStatus()
        _configState.value = _configState.value.copy(
            liveBaseUrl = baseUrl,
            liveApiKey = apiKey,
            liveAccessToken = accessToken,
            providerStatus = status,
            saveMessage = if (status.isOperational) "Credentials validated!" else "Saved. Missing valid live connection."
        )
    }
}

class MainViewModelFactory(
    private val repository: MarketJournalRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository) as T
    }
}
