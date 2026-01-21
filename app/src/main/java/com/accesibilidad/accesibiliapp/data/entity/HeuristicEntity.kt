// HeuristicEntity.kt
package com.accesibilidad.accesibiliapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "heuristics")
data class HeuristicEntity(
    @PrimaryKey val id: String,
    //@ColumnInfo(name = "categoryId")
    //val categoryId: Int,
    val heuristicJson: String
)