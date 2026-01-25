package com.accesibilidad.accesibiliapp.data.dao

import androidx.room.*
import com.accesibilidad.accesibiliapp.data.entity.*
import kotlinx.coroutines.flow.Flow


@Dao
interface ReportDao {

    // --- Inserciones básicas ---
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertReportMetadata(reportMetadata: ReportMetadata): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertIssue(issue: Issue): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBarriers(barriers: List<Barrier>)

    // --- Operaciones CRUD básicas ---
    @Update
    suspend fun updateReport(reportMetadata: ReportMetadata)

    @Delete
    suspend fun deleteReport(reportMetadata: ReportMetadata)


    @Transaction
    suspend fun insertReport(
        reportMetadata: ReportMetadata,
        issues: List<Issue>,
        barriers: List<List<Barrier>>
    ): Long {
        val reportId = insertReportMetadata(reportMetadata)

        issues.forEachIndexed { index, issue ->
            // Asignamos el ID del reporte padre al issue
            val issueWithId = issue.copy(reportId = reportId)
            val issueId = insertIssue(issueWithId)

            // Asignamos el ID del issue padre a sus barreras
            val issueBarriers = barriers[index].map { barrier ->
                barrier.copy(issueId = issueId)
            }
            insertBarriers(issueBarriers)
        }
        return reportId
    }


    @Transaction // Necesario porque Report contiene relaciones
    @Query("SELECT * FROM reports WHERE id = :id")
    fun getReport(id: Long): Flow<ReportEntity?>

    @Transaction
    @Query("SELECT * FROM reports")
    fun getAllReports(): Flow<List<ReportEntity?>>

    // Query extraída de lambda$getCalculatedFinalScoreForReport$8
    @Query("SELECT IFNULL(SUM(score), 0.0) FROM Issue WHERE reportId = :reportId")
    fun getCalculatedFinalScoreForReport(reportId: Long): Flow<Float>

    // Query recursiva compleja extraída de lambda$getAverageScoreForCategoryTree$9
    @Query("""
        WITH RECURSIVE CategoryDescendants(id) AS (
            SELECT id FROM categories WHERE id = :categoryId
            UNION ALL
            SELECT c.id FROM categories c, CategoryDescendants cd WHERE c.parentId = cd.id
        )
        SELECT IFNULL(AVG(all_reports.suma), 0.0)
        FROM (
            SELECT IFNULL(SUM(Issue.score), 0.0) as suma
            FROM reports LEFT JOIN issue ON reports.id = issue.reportId
            WHERE categoryId IN (SELECT id FROM CategoryDescendants)
            GROUP BY Issue.reportId
        ) as all_reports
    """)
    fun getAverageScoreForCategoryTree(categoryId: Long): Flow<Float>
}