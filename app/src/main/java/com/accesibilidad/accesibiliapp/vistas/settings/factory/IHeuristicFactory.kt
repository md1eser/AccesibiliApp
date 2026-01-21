package com.accesibilidad.accesibiliapp.vistas.settings.factory

import androidx.compose.runtime.Composable
import com.accesibilidad.accesibiliapp.data.heuristicas.Heuristic

interface IHeuristicFactory {
    fun getDefaults(): Map<String, String>

    @Composable
    fun FormBody(
        inputValues: Map<String, String>,
        onValueChange: (String, String) -> Unit
    )

    fun create(values: Map<String, String>): Heuristic
}

class HeuristicFactory(labels: List<String>) {
    private val factories: Map<String, IHeuristicFactory> = mapOf(
        "ExistenceHeuristic" to ExistenceHeuristicFactory(labels)
        // Aquí irían otras fábricas si existieran
    )

    fun getHeuristicTypes(): List<String> = factories.keys.toList()

    fun getDefaultsFor(type: String): Map<String, String> {
        return factories[type]?.getDefaults() ?: emptyMap()
    }

    @Composable
    fun HeuristicFormBody(
        type: String,
        inputValues: Map<String, String>,
        onValueChange: (String, String) -> Unit
    ) {
        factories[type]?.FormBody(inputValues, onValueChange)
    }

    fun create(type: String, values: Map<String, String>): Heuristic {
        return factories[type]?.create(values)
            ?: throw IllegalArgumentException("Fábrica no encontrada para el tipo: $type")
    }
}