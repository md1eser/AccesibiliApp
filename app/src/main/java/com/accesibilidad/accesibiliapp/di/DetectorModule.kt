package com.accesibilidad.accesibiliapp.di


import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.accesibilidad.accesibiliapp.Constants
import com.accesibilidad.accesibiliapp.data.deteccion.Detector
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Module
@InstallIn(SingletonComponent::class) // Hilt necesita saber dónde instalar el módulo
object DetectorModule {

    @Provides
    @Singleton
    fun provideDetector(@ApplicationContext context: Context): Detector {
        val detector = Detector(
            context,
            Constants.MODEL_PATH,
            Constants.LABELS_PATH
        )

        // Se lanza la inicialización en un hilo de IO para no bloquear el inicio de la app
        CoroutineScope(Dispatchers.IO).launch {
            detector.initialize()
        }

        return detector
    }
}