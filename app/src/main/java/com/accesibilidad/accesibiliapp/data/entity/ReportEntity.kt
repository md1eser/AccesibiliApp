// ReportEntity.kt
package com.accesibilidad.accesibiliapp.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ReportEntity(
    @Embedded val metadata: ReportMetadata,
    @Relation(
        parentColumn = "id",
        entityColumn = "reportId",
        entity = Issue::class
    )
    val issues: List<IssueWithBarriers>
) {
    // Cálculo del puntaje promedio basado en los issues
    val score: Float
        get() = if (issues.isEmpty()) 0f else issues.map { it.issue.score ?: 0f }.average().toFloat()
}