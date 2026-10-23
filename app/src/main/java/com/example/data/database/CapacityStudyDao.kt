package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CapacityStudyDao {
    @Query("SELECT * FROM capacity_studies ORDER BY timestamp DESC")
    fun getAllStudies(): Flow<List<CapacityStudy>>

    @Query("SELECT * FROM capacity_studies ORDER BY timestamp DESC")
    suspend fun getAllStudiesList(): List<CapacityStudy>

    @Query("SELECT * FROM capacity_studies WHERE id = :studyId LIMIT 1")
    fun getStudyByIdFlow(studyId: Int): Flow<CapacityStudy?>

    @Query("SELECT * FROM capacity_studies WHERE id = :studyId LIMIT 1")
    suspend fun getStudyById(studyId: Int): CapacityStudy?

    @Query("SELECT * FROM study_processes WHERE studyId = :studyId ORDER BY sl ASC")
    fun getProcessesForStudyFlow(studyId: Int): Flow<List<StudyProcess>>

    @Query("SELECT * FROM study_processes WHERE studyId = :studyId ORDER BY sl ASC")
    suspend fun getProcessesForStudy(studyId: Int): List<StudyProcess>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudy(study: CapacityStudy): Long

    @Update
    suspend fun updateStudy(study: CapacityStudy)

    @Delete
    suspend fun deleteStudy(study: CapacityStudy)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProcess(process: StudyProcess): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProcesses(processes: List<StudyProcess>)

    @Update
    suspend fun updateProcess(process: StudyProcess)

    @Delete
    suspend fun deleteProcess(process: StudyProcess)

    @Query("DELETE FROM study_processes WHERE studyId = :studyId")
    suspend fun clearProcessesForStudy(studyId: Int)
}
