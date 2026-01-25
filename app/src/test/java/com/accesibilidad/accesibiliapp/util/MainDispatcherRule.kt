package com.accesibilidad.accesibiliapp.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Regla reutilizable para tests de JUnit 4 que intercambia el dispatcher Main
 * por un TestDispatcher (generalmente UnconfinedTestDispatcher).
 *
 * Esto permite testear ViewModels que usan viewModelScope.launch { } sin errores.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        // Antes del test: Seteamos el dispatcher Main falso
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        // Después del test: Limpiamos para no afectar otros tests
        Dispatchers.resetMain()
    }
}