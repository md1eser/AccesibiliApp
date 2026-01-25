package com.accesibilidad.accesibiliapp.data.repository

import android.net.Uri
import com.accesibilidad.accesibiliapp.data.deteccion.DetectionResult
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CaptureRepositoryTest {

    private lateinit var repository: CaptureRepository

    @Before
    fun setUp() {
        repository = CaptureRepository()
    }

    @After
    fun tearDown() {
        // Buena práctica en MockK para limpiar estado entre tests
        clearAllMocks()
    }

    @Test
    fun testSetCategoryIdUpdatesState() {
        val testId = 999L
        repository.setCategoryId(testId)

        assertEquals(testId, repository.categoryId.value)
    }

    @Test
    fun testSetImageUriUpdatesState() {
        // En MockK, mockear clases de Android es muy directo
        val mockUri = mockk<Uri>()

        repository.setImageUri(mockUri)

        assertEquals(mockUri, repository.imageUri.value)
    }

    @Test
    fun testSetDetectionResultLogic() {
        // --- PREPARACIÓN (GIVEN) ---
        // 1. Creamos mocks para el input y para lo que devolverá deepCopy()
        val inputResult = mockk<DetectionResult>()
        val copyOfResult = mockk<DetectionResult>()

        // 2. Definimos comportamiento: cuando se llame a deepCopy, devuelve el mock 'copyOfResult'
        every { inputResult.deepCopy() } returns copyOfResult

        // 3. También necesitamos que la copia se pueda copiar (para el resetToOriginal)
        val restoredResult = mockk<DetectionResult>()
        every { copyOfResult.deepCopy() } returns restoredResult

        // --- ACCIÓN (WHEN) ---
        repository.setDetectionResult(inputResult)

        // --- VERIFICACIÓN (THEN) ---
        // El valor actual debe ser la copia, no el input original (inmutabilidad)
        assertEquals(copyOfResult, repository.detectionResult.value)

        // Verificamos que efectivamente se llamó a deepCopy
        verify(exactly = 2) { inputResult.deepCopy() }
        // ¿Por qué 2 veces?
        // Una para _detectionResult y otra para _originalDetectionResult (porque es null al inicio)
    }

    @Test
    fun testOriginalResultIsNotOverwritten() {
        // --- GIVEN ---
        // Primer resultado (el original)
        val result1 = mockk<DetectionResult>()
        val copy1 = mockk<DetectionResult>()
        every { result1.deepCopy() } returns copy1
        // Necesario para el reset posterior
        every { copy1.deepCopy() } returns copy1

        // Segundo resultado (una edición posterior)
        val result2 = mockk<DetectionResult>()
        val copy2 = mockk<DetectionResult>()
        every { result2.deepCopy() } returns copy2

        // --- WHEN ---
        repository.setDetectionResult(result1) // Se guarda copy1 como original
        repository.setDetectionResult(result2) // Se guarda copy2 como actual, original sigue siendo copy1

        // --- THEN ---
        assertEquals("El valor actual debe ser el segundo", copy2, repository.detectionResult.value)

        // Resetear al original
        repository.resetToOriginal()

        assertEquals("Al resetear debe volver a la copia del primero", copy1, repository.detectionResult.value)
    }

    @Test
    fun testClearResetsEverything() {
        // Llenamos con datos dummy
        repository.setCategoryId(10L)
        repository.setImageUri(mockk()) // mockk simple

        val result = mockk<DetectionResult>()
        every { result.deepCopy() } returns mockk()
        repository.setDetectionResult(result)

        // Acción
        repository.clear()

        // Assert
        assertNull(repository.categoryId.value)
        assertNull(repository.imageUri.value)
        assertNull(repository.detectionResult.value)
    }
}