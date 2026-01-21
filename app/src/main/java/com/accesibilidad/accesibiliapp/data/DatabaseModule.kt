package com.accesibilidad.accesibiliapp.data


import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.accesibilidad.accesibiliapp.data.dao.CategoryDao
import com.accesibilidad.accesibiliapp.data.dao.HeuristicDao
import com.accesibilidad.accesibiliapp.data.dao.ReportDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideHeuristicDao(db: AppDatabase): HeuristicDao = db.heuristicDao()

    @Provides
    fun provideReportDao(db: AppDatabase): ReportDao = db.reportDao()
}