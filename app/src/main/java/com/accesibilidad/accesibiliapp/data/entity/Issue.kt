// Issue.kt
package com.accesibilidad.accesibiliapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Issue")
data class Issue(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reportId: Long = 0, // Clave foránea manual
    val type: String?,
    val desc: String?, // Mapeado a columna "desc"
    val score: Float?
)