package com.accesibilidad.accesibiliapp.data.repository


import com.accesibilidad.accesibiliapp.data.dao.ReportDao
import com.accesibilidad.accesibiliapp.data.entity.ReportEntity
import com.accesibilidad.accesibiliapp.data.entity.ReportMetadata
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject



class ReportRepository @Inject constructor(
    private val reportDao: ReportDao
) {

    suspend fun insertReport(report: ReportEntity): Long {
        val issues = report.issues.map { it.issue }
        val barriers = report.issues.map { it.barriers }

        return reportDao.insertReport(report.metadata, issues, barriers)
    }

    fun getReport(id: Long): Flow<ReportEntity?> {
        return reportDao.getReport(id)
    }

    fun getAllReports(): Flow<List<ReportEntity?>> {
        return reportDao.getAllReports()
    }

    suspend fun updateReport(report: ReportEntity) {
        // El decompilado muestra que solo actualiza la metadata
        reportDao.updateReport(report.metadata)
    }

    suspend fun deleteReport(report: ReportEntity) {
        // El decompilado muestra que solo usa la metadata para borrar
        reportDao.deleteReport(report.metadata)
    }

    fun getScore(report: ReportEntity): Flow<Float> {
        return reportDao.getCalculatedFinalScoreForReport(report.metadata.id)
    }

    fun getAverageScoreForCategoryTree(categoryId: Long): Flow<Float> {
        return reportDao.getAverageScoreForCategoryTree(categoryId)
    }
}