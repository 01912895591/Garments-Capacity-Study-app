package com.example.data.repository

import com.example.data.database.CapacityStudy
import com.example.data.database.CapacityStudyDao
import com.example.data.database.StudyProcess
import kotlinx.coroutines.flow.Flow

class CapacityStudyRepository(private val dao: CapacityStudyDao) {

    val allStudies: Flow<List<CapacityStudy>> = dao.getAllStudies()

    suspend fun getAllStudiesList(): List<CapacityStudy> = dao.getAllStudiesList()

    fun getStudyByIdFlow(studyId: Int): Flow<CapacityStudy?> = dao.getStudyByIdFlow(studyId)

    suspend fun getStudyById(studyId: Int): CapacityStudy? = dao.getStudyById(studyId)

    fun getProcessesForStudyFlow(studyId: Int): Flow<List<StudyProcess>> = dao.getProcessesForStudyFlow(studyId)

    suspend fun insertStudy(study: CapacityStudy): Long = dao.insertStudy(study)

    suspend fun updateStudy(study: CapacityStudy) = dao.updateStudy(study)

    suspend fun deleteStudy(study: CapacityStudy) = dao.deleteStudy(study)

    suspend fun updateProcess(process: StudyProcess) = dao.updateProcess(process)

    suspend fun insertProcess(process: StudyProcess) = dao.insertProcess(process)

    suspend fun deleteProcess(process: StudyProcess) = dao.deleteProcess(process)

    suspend fun createNewStudy(
        lineNo: String,
        buyer: String,
        styleNo: String,
        studiedBy: String,
        date: String,
        targetPerHour: Int = 150,
        currentPcsPerHour: Int = 100,
        daysRun: String = "",
        graphInfo: String = "",
        smv: String = ""
    ): Long {
        val study = CapacityStudy(
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
        val studyId = dao.insertStudy(study).toInt()
        
        // Manual study starts with 0 operations as requested (no demo/default layout)
        
        return studyId.toLong()
    }

    suspend fun createNewStudyWithDefaultTemplate(): Long {
        val study = CapacityStudy(
            lineNo = "C",
            buyer = "ROYAL FASHIONS",
            styleNo = "4102/B",
            studiedBy = "Basher",
            date = "1-Jul-24",
            targetPerHour = 150,
            currentPcsPerHour = 100
        )
        val studyId = dao.insertStudy(study).toInt()
        
        // Populate processes with pre-filled capacity data from the uploaded Excel sheet
        val populatedProcesses = getDefaultProcessesWithData(studyId)
        dao.insertProcesses(populatedProcesses)
        
        return studyId.toLong()
    }

    private fun getDefaultProcessesConfig(): List<StudyProcess> {
        return listOf(
            StudyProcess(sl = 1, processName = "BOX PLAKT", mc = "KANCHA", studyId = 0),
            StudyProcess(sl = 2, processName = "BTN PLAKT", mc = "SNL", studyId = 0),
            StudyProcess(sl = 3, processName = "CARE LBL JOIN", mc = "SNL", studyId = 0),
            StudyProcess(sl = 4, processName = "PKT ROLLING", mc = "SNL", studyId = 0),
            StudyProcess(sl = 5, processName = "PKT JOIN", mc = "SNL", studyId = 0),
            StudyProcess(sl = 6, processName = "BOX PLAKT HOLE", mc = "BH", studyId = 0),
            StudyProcess(sl = 7, processName = "BAK-BOX TUCK", mc = "SNL", studyId = 0),
            StudyProcess(sl = 8, processName = "MAIN LBL JOIN", mc = "SNL", studyId = 0),
            StudyProcess(sl = 9, processName = "LBL JOIN PATCH", mc = "SNL", studyId = 0),
            StudyProcess(sl = 10, processName = "BAK JOIN", mc = "SNL", studyId = 0),
            StudyProcess(sl = 11, processName = "SHOULDER TOP STC", mc = "SNL", studyId = 0),
            StudyProcess(sl = 12, processName = "BAK TOP STC", mc = "SNL", studyId = 0),
            StudyProcess(sl = 13, processName = "SHOULDER JOIN", mc = "SNL", studyId = 0),
            StudyProcess(sl = 14, processName = "GAMBLE", mc = "SNL", studyId = 0),
            StudyProcess(sl = 15, processName = "SLV PLAKT", mc = "SNL", relation = "Sequence", studyId = 0),
            StudyProcess(sl = 16, processName = "SLV JOIN", mc = "OL", relation = "Parallel", studyId = 0),
            StudyProcess(sl = 17, processName = "ARMHOLE TOP STC", mc = "SNL", relation = "Parallel", studyId = 0),
            StudyProcess(sl = 18, processName = "COLLAR JOIN", mc = "SNL", studyId = 0),
            StudyProcess(sl = 19, processName = "COLLAR TOP STC", mc = "SNL", relation = "Parallel", studyId = 0),
            StudyProcess(sl = 20, processName = "SIDE SEAM", mc = "FOA", studyId = 0),
            StudyProcess(sl = 21, processName = "CUFF JOIN", mc = "SNL", studyId = 0)
        )
    }

    private fun getDefaultProcessesWithData(studyId: Int): List<StudyProcess> {
        return listOf(
            StudyProcess(
                studyId = studyId, sl = 1, processName = "BOX PLAKT", mc = "KANCHA",
                op1Name = "IMRAN", op1C1 = 18.0, op1C2 = 18.0, op1C3 = 24.0, op1C4 = 20.0, op1C5 = 18.0
            ),
            StudyProcess(
                studyId = studyId, sl = 2, processName = "BTN PLAKT", mc = "SNL",
                op1Name = "SHOVA", op1C1 = 16.0, op1C2 = 14.0, op1C3 = 15.0, op1C4 = 16.0, op1C5 = 16.0
            ),
            StudyProcess(
                studyId = studyId, sl = 3, processName = "CARE LBL JOIN", mc = "SNL",
                op1Name = "AMENA", op1C1 = 12.0, op1C2 = 14.0, op1C3 = 16.0, op1C4 = 16.0, op1C5 = 12.0
            ),
            StudyProcess(
                studyId = studyId, sl = 4, processName = "PKT ROLLING", mc = "SNL",
                op1Name = "SONIA", op1C1 = 20.0, op1C2 = 26.0, op1C3 = 19.0, op1C4 = 21.0, op1C5 = 19.0
            ),
            StudyProcess(
                studyId = studyId, sl = 5, processName = "PKT JOIN", mc = "SNL",
                op1Name = "AL-AMIN", op1C1 = 36.0, op1C2 = 35.0, op1C3 = 35.0, op1C4 = 29.0, op1C5 = 32.0
            ),
            StudyProcess(
                studyId = studyId, sl = 6, processName = "BOX PLAKT HOLE", mc = "BH",
                op1Name = "SORIF", op1C1 = 23.0, op1C2 = 22.0, op1C3 = 26.0, op1C4 = 24.0, op1C5 = 28.0
            ),
            StudyProcess(
                studyId = studyId, sl = 7, processName = "BAK-BOX TUCK", mc = "SNL",
                op1Name = "RUBI", op1C1 = 27.0, op1C2 = 26.0, op1C3 = 24.0, op1C4 = 30.0, op1C5 = 36.0
            ),
            StudyProcess(
                studyId = studyId, sl = 8, processName = "MAIN LBL JOIN", mc = "SNL",
                op1Name = "NIPA", op1C1 = 26.0, op1C2 = 22.0, op1C3 = 24.0, op1C4 = 23.0, op1C5 = 22.0
            ),
            StudyProcess(
                studyId = studyId, sl = 9, processName = "LBL JOIN PATCH", mc = "SNL",
                op1Name = "JARIN", op1C1 = 18.0, op1C2 = 21.0, op1C3 = 20.0, op1C4 = 20.0, op1C5 = 18.0
            ),
            StudyProcess(
                studyId = studyId, sl = 10, processName = "BAK JOIN", mc = "SNL",
                op1Name = "AMENA", op1C1 = 36.0, op1C2 = 34.0, op1C3 = 37.0, op1C4 = 32.0, op1C5 = 34.0
            ),
            StudyProcess(
                studyId = studyId, sl = 11, processName = "SHOULDER TOP STC", mc = "SNL",
                op1Name = "TASLIMA", op1C1 = 11.0, op1C2 = 9.0, op1C3 = 11.0, op1C4 = 10.0, op1C5 = 12.0
            ),
            StudyProcess(
                studyId = studyId, sl = 12, processName = "BAK TOP STC", mc = "SNL",
                op1Name = "TASLIMA", op1C1 = 17.0, op1C2 = 15.0, op1C3 = 11.0, op1C4 = 11.0, op1C5 = 14.0
            ),
            StudyProcess(
                studyId = studyId, sl = 13, processName = "SHOULDER JOIN", mc = "SNL",
                op1Name = "SATHI", op1C1 = 24.0, op1C2 = 25.0, op1C3 = 26.0, op1C4 = 30.0, op1C5 = 27.0
            ),
            StudyProcess(
                studyId = studyId, sl = 14, processName = "GAMBLE", mc = "SNL",
                op1Name = "MARJIA", op1C1 = 18.0, op1C2 = 18.0, op1C3 = 20.0, op1C4 = 18.0, op1C5 = 18.0
            ),
            StudyProcess(
                studyId = studyId, sl = 15, processName = "SLV PLAKT", mc = "SNL", relation = "Sequence",
                op1Name = "FARJANA", op1C1 = 31.0, op1C2 = 28.0, op1C3 = 28.0, op1C4 = 28.0, op1C5 = 30.0,
                op2Name = "NURUNNAHAR", op2C1 = 27.0, op2C2 = 28.0, op2C3 = 31.0, op2C4 = 32.0, op2C5 = 26.0
            ),
            StudyProcess(
                studyId = studyId, sl = 16, processName = "SLV JOIN", mc = "OL", relation = "Parallel",
                op1Name = "ROZI", op1C1 = 49.0, op1C2 = 57.0, op1C3 = 55.0, op1C4 = 54.0, op1C5 = 53.0,
                op2Name = "SHOPNA", op2C1 = 46.0, op2C2 = 53.0, op2C3 = 52.0, op2C4 = 59.0, op2C5 = 51.0
            ),
            StudyProcess(
                studyId = studyId, sl = 17, processName = "ARMHOLE TOP STC", mc = "SNL", relation = "Parallel",
                op1Name = "LIPI", op1C1 = 39.0, op1C2 = 40.0, op1C3 = 39.0, op1C4 = 40.0, op1C5 = 40.0,
                op2Name = "SULTANA", op2C1 = 42.0, op2C2 = 43.0, op2C3 = 42.0, op2C4 = 43.0, op2C5 = 43.0
            ),
            StudyProcess(
                studyId = studyId, sl = 18, processName = "COLLAR JOIN", mc = "SNL",
                op1Name = "SORNA", op1C1 = 18.0, op1C2 = 19.0, op1C3 = 20.0, op1C4 = 23.0, op1C5 = 20.0
            ),
            StudyProcess(
                studyId = studyId, sl = 19, processName = "COLLAR TOP STC", mc = "SNL", relation = "Parallel",
                op1Name = "HENA", op1C1 = 32.0, op1C2 = 35.0, op1C3 = 33.0, op1C4 = 37.0, op1C5 = 31.0,
                op2Name = "RANU", op2C1 = 30.0, op2C2 = 32.0, op2C3 = 35.0, op2C4 = 36.0, op2C5 = 38.0
            ),
            StudyProcess(
                studyId = studyId, sl = 20, processName = "SIDE SEAM", mc = "FOA",
                op1Name = "RUMI", op1C1 = 37.0, op1C2 = 38.0, op1C3 = 37.0, op1C4 = 37.0, op1C5 = 37.0
            ),
            StudyProcess(
                studyId = studyId, sl = 21, processName = "CUFF JOIN", mc = "SNL",
                op1Name = "POLY", op1C1 = 44.0, op1C2 = 45.0, op1C3 = 44.0, op1C4 = 44.0, op1C5 = 45.0
            )
        )
    }
}
