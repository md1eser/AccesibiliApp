package com.accesibilidad.accesibiliapp.di


import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.accesibilidad.accesibiliapp.data.dao.ReportDao
import com.accesibilidad.accesibiliapp.data.repository.ReportRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReportModule {

    @Provides
    @Singleton
    fun provideReportRepository(reportDao: ReportDao): ReportRepository {
        return ReportRepository(reportDao)
    }
}