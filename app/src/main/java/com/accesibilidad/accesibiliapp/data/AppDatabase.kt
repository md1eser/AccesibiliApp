package com.accesibilidad.accesibiliapp.data


import androidx.room.Database
import androidx.room.RoomDatabase
import com.accesibilidad.accesibiliapp.data.dao.CategoryDao
import com.accesibilidad.accesibiliapp.data.dao.HeuristicDao
import com.accesibilidad.accesibiliapp.data.dao.ReportDao
import com.accesibilidad.accesibiliapp.data.entity.Barrier
import com.accesibilidad.accesibiliapp.data.entity.CategoryEntity
import com.accesibilidad.accesibiliapp.data.entity.HeuristicEntity
import com.accesibilidad.accesibiliapp.data.entity.Issue
import com.accesibilidad.accesibiliapp.data.entity.ReportMetadata

@Database(
    entities = [
        CategoryEntity::class,
        HeuristicEntity::class,
        ReportMetadata::class,
        Issue::class,
        Barrier::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun heuristicDao(): HeuristicDao
    abstract fun reportDao(): ReportDao
}