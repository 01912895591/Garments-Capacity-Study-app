package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.CapacityStudy
import com.example.data.database.StudyProcess
import com.example.data.repository.CapacityStudyRepository
import com.example.utils.PdfReportExporter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StopwatchState(
    val processId: Int = 0,
    val operatorIndex: Int = 1, // 1 for OP-1, 2 for OP-2, 3 for OP-3
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedMs: Long = 0L,
    val laps: List<Double> = emptyList()
)

class CapacityStudyViewModel(private val repository: CapacityStudyRepository) : ViewModel() {

    // List of all studies
    val allStudies: StateFlow<List<CapacityStudy>> = repository.allStudies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Study ID state
    private val _activeStudyId = MutableStateFlow<Int>(-1)
    val activeStudyId: StateFlow<Int> = _activeStudyId.asStateFlow()

    // Active Capacity Study loaded reactively
    val activeStudy: StateFlow<CapacityStudy?> = _activeStudyId
        .flatMapLatest { id ->
            if (id > 0) repository.getStudyByIdFlow(id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Active study processes loaded reactively
    val activeProcesses: StateFlow<List<StudyProcess>> = _activeStudyId
        .flatMapLatest { id ->
            if (id > 0) repository.getProcessesForStudyFlow(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stopwatch logic variables
    private val _stopwatchState = MutableStateFlow(StopwatchState())
    val stopwatchState: StateFlow<StopwatchState> = _stopwatchState.asStateFlow()

    private var stopwatchJob: Job? = null
    private var stopwatchBaseTime = 0L
    private var stopwatchAccumulatedTime = 0L

    init {
        // Automatically precheck/initialize default study if none exists
        viewModelScope.launch {
            val studies = repository.getAllStudiesList()
            if (studies.isEmpty()) {
                val id = repository.createNewStudyWithDefaultTemplate()
                _activeStudyId.value = id.toInt()
            } else {
                _activeStudyId.value = studies.first().id
            }
        }
    }

    fun selectStudy(studyId: Int) {
        _activeStudyId.value = studyId
    }

    fun createNewBlankStudy(
        lineNo: String,
        buyer: String,
        styleNo: String,
        studiedBy: String,
        date: String,
        targetPerHour: Int = 150,
        daysRun: String = "",
        graphInfo: String = "",
        smv: String = ""
    ) {
        viewModelScope.launch {
            val id = repository.createNewStudy(
                lineNo = lineNo,
                buyer = buyer,
                styleNo = styleNo,
                studiedBy = studiedBy,
                date = date,
                targetPerHour = targetPerHour,
                daysRun = daysRun,
                graphInfo = graphInfo,
                smv = smv
            )
            _activeStudyId.value = id.toInt()
        }
    }

    fun createStudyFromDefaultTemplate() {
        viewModelScope.launch {
            val id = repository.createNewStudyWithDefaultTemplate()
            _activeStudyId.value = id.toInt()
        }
    }

    fun updateActiveStudyDetails(
        lineNo: String,
        buyer: String,
        styleNo: String,
        studiedBy: String,
        date: String,
        targetPerHour: Int,
        currentPcsPerHour: Int,
        daysRun: String,
        graphInfo: String,
        smv: String
    ) {
        val current = activeStudy.value ?: return
        viewModelScope.launch {
            repository.updateStudy(
                current.copy(
                    lineNo = lineNo,
                    buyer = buyer,
                    styleNo = styleNo,
                    studiedBy = studiedBy,
                    date = date,
                    targetPerHour = targetPerHour,
                    currentPcsPerHour = currentPcsPerHour,
                    daysRun = daysRun,
                    graphInfo = graphInfo,
                    smv = smv
                )
            )
        }
    }

    fun deleteActiveStudy() {
        val current = activeStudy.value ?: return
        viewModelScope.launch {
            repository.deleteStudy(current)
            val leftOver = repository.getAllStudiesList()
            if (leftOver.isNotEmpty()) {
                _activeStudyId.value = leftOver.first().id
            } else {
                // Generate a fresh workspace if all deleted
                val id = repository.createNewStudyWithDefaultTemplate()
                _activeStudyId.value = id.toInt()
            }
        }
    }

    fun deleteStudy(study: CapacityStudy) {
        viewModelScope.launch {
            repository.deleteStudy(study)
            if (_activeStudyId.value == study.id) {
                val leftOver = repository.getAllStudiesList()
                if (leftOver.isNotEmpty()) {
                    _activeStudyId.value = leftOver.first().id
                } else {
                    // Generate a fresh workspace if all deleted
                    val id = repository.createNewStudyWithDefaultTemplate()
                    _activeStudyId.value = id.toInt()
                }
            }
        }
    }

    fun saveProcessDetail(process: StudyProcess) {
        viewModelScope.launch {
            if (process.id == 0) {
                repository.insertProcess(process)
            } else {
                repository.updateProcess(process)
            }
        }
    }

    fun deleteProcess(process: StudyProcess) {
        viewModelScope.launch {
            repository.deleteProcess(process)
        }
    }

    fun resetAllObservationsInActiveStudy() {
        val proList = activeProcesses.value
        viewModelScope.launch {
            proList.forEach { p ->
                repository.updateProcess(
                    p.copy(
                        op1C1 = 0.0, op1C2 = 0.0, op1C3 = 0.0, op1C4 = 0.0, op1C5 = 0.0,
                        op2C1 = 0.0, op2C2 = 0.0, op2C3 = 0.0, op2C4 = 0.0, op2C5 = 0.0,
                        op3C1 = 0.0, op3C2 = 0.0, op3C3 = 0.0, op3C4 = 0.0, op3C5 = 0.0
                    )
                )
            }
        }
    }

    // --- TIMING / STOPWATCH SYSTEM ---

    fun startStopwatch(processId: Int, operatorIndex: Int) {
        stopwatchJob?.cancel()
        
        stopwatchBaseTime = System.currentTimeMillis()
        stopwatchAccumulatedTime = 0L
        _stopwatchState.value = StopwatchState(
            processId = processId,
            operatorIndex = operatorIndex,
            isRunning = true,
            isPaused = false,
            elapsedMs = 0L,
            laps = emptyList()
        )

        stopwatchJob = viewModelScope.launch {
            while (true) {
                delay(40)
                val curState = _stopwatchState.value
                if (curState.isRunning) {
                     _stopwatchState.value = curState.copy(
                         elapsedMs = stopwatchAccumulatedTime + (System.currentTimeMillis() - stopwatchBaseTime)
                     )
                }
            }
        }
    }

    fun pauseStopwatch() {
        val curState = _stopwatchState.value
        if (curState.isRunning) {
            stopwatchAccumulatedTime += System.currentTimeMillis() - stopwatchBaseTime
            _stopwatchState.value = curState.copy(
                isRunning = false,
                isPaused = true,
                elapsedMs = stopwatchAccumulatedTime
            )
        }
    }

    fun resumeStopwatch() {
        val curState = _stopwatchState.value
        if (curState.isPaused && curState.processId > 0) {
            stopwatchBaseTime = System.currentTimeMillis()
            _stopwatchState.value = curState.copy(
                isRunning = true,
                isPaused = false
            )
        }
    }

    fun resetStopwatch() {
        val curState = _stopwatchState.value
        if (curState.processId > 0) {
            if (curState.laps.isNotEmpty()) {
                val updatedLaps = curState.laps.dropLast(1)
                val newAccumulatedSeconds = updatedLaps.sum()
                stopwatchAccumulatedTime = (newAccumulatedSeconds * 1000).toLong()
                stopwatchBaseTime = System.currentTimeMillis()
                _stopwatchState.value = curState.copy(
                    isRunning = true,
                    isPaused = false,
                    elapsedMs = stopwatchAccumulatedTime,
                    laps = updatedLaps
                )
            } else {
                stopwatchAccumulatedTime = 0L
                stopwatchBaseTime = System.currentTimeMillis()
                _stopwatchState.value = curState.copy(
                    isRunning = true,
                    isPaused = false,
                    elapsedMs = 0L,
                    laps = emptyList()
                )
            }
        }
    }

    fun captureLapObservation() {
        val state = _stopwatchState.value
        if (!state.isRunning && !state.isPaused) return

        val totalSeconds = state.elapsedMs / 1000.0
        val sumPreviousLaps = state.laps.sum()
        val currentLapDiff = (totalSeconds - sumPreviousLaps).coerceAtLeast(0.0)

        val updatedLaps = state.laps + currentLapDiff
        
        if (updatedLaps.size >= 5) {
            // Stop and Automatically Save Observation once 5 cycles complete!
            stopwatchJob?.cancel()
            _stopwatchState.value = state.copy(
                isRunning = false,
                isPaused = false,
                laps = updatedLaps
            )
            saveCapturedLapsToProcess(state.processId, state.operatorIndex, updatedLaps)
            // auto-dismiss stopwatch state
            viewModelScope.launch {
                delay(1500) // Keep state briefly so user can see 5th lap summary
                _stopwatchState.value = StopwatchState() // Reset
            }
        } else {
            _stopwatchState.value = state.copy(laps = updatedLaps)
        }
    }

    fun stopAndCancelStopwatch() {
        stopwatchJob?.cancel()
        _stopwatchState.value = StopwatchState()
    }

    private fun saveCapturedLapsToProcess(processId: Int, opIndex: Int, laps: List<Double>) {
        val process = activeProcesses.value.find { it.id == processId } ?: return
        val l = laps.toMutableList()
        while (l.size < 5) l.add(0.0)

        val updated = when (opIndex) {
            1 -> process.copy(op1C1 = l[0], op1C2 = l[1], op1C3 = l[2], op1C4 = l[3], op1C5 = l[4])
            2 -> process.copy(op2C1 = l[0], op2C2 = l[1], op2C3 = l[2], op2C4 = l[3], op2C5 = l[4])
            3 -> process.copy(op3C1 = l[0], op3C2 = l[1], op3C3 = l[2], op3C4 = l[3], op3C5 = l[4])
            else -> process
        }

        viewModelScope.launch {
            repository.updateProcess(updated)
        }
    }

    // Export report functions
    fun exportPdfReport(context: Context, share: Boolean = true, print: Boolean = false) {
        val study = activeStudy.value ?: return
        val proList = activeProcesses.value
        PdfReportExporter.generateAndSharePdf(context, study, proList, share, print)
    }
}

class CapacityStudyViewModelFactory(private val repository: CapacityStudyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CapacityStudyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CapacityStudyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
