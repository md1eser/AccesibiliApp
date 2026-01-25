package com.accesibilidad.accesibiliapp.vistas.settings.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.accesibilidad.accesibiliapp.data.heuristicas.Heuristic
import com.accesibilidad.accesibiliapp.data.repository.json
import com.accesibilidad.accesibiliapp.vistas.common.refactor.DialogScaffold
import com.accesibilidad.accesibiliapp.vistas.settings.factory.HeuristicFactory
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHeuristicDialog(
    heuristicToEdit: Heuristic?,
    labels: List<String>,
    onDismiss: () -> Unit,
    onSave: (Heuristic) -> Unit
) {
    // Instanciamos la fábrica una sola vez cuando cambian las etiquetas
    val factory = remember(labels) { HeuristicFactory(labels) }
    val isEditing = heuristicToEdit != null

    // Estado del Tipo de Heurística
    var selectedType by remember {
        mutableStateOf(
            if (isEditing && heuristicToEdit != null) heuristicToEdit::class.simpleName!!
            else factory.getHeuristicTypes().firstOrNull() ?: ""
        )
    }

    // Estado de los Valores del Formulario
    var inputValues by remember { mutableStateOf(mapOf<String, String>()) }

    // Efecto para cargar datos iniciales (Crear vs Editar)
    LaunchedEffect(heuristicToEdit, selectedType) {
        inputValues = if (isEditing && heuristicToEdit != null && heuristicToEdit::class.simpleName == selectedType) {
            heuristicToMap(heuristicToEdit) // Lógica extraída abajo
        } else {
            factory.getDefaultsFor(selectedType)
        }
    }

    DialogScaffold(
        title = if (isEditing) "Editar Heurística" else "Agregar Heurística",
        onDismiss = onDismiss,
        confirmButtonText = if (isEditing) "Guardar" else "Agregar",
        showCancelButton = true,
        onConfirm = {
            try {
                val newHeuristic = factory.create(selectedType, inputValues)
                onSave(newHeuristic)
                onDismiss()
            } catch (e: Exception) {
                e.printStackTrace() // Manejo de error básico
            }
        }
    ) {
        // Selector de Tipo (Solo visible si creamos una nueva, o podrías permitir cambiarlo)
        if (!isEditing) {
            HeuristicTypeSelector(
                types = factory.getHeuristicTypes(),
                selectedType = selectedType,
                onTypeSelected = { newType ->
                    selectedType = newType
                    inputValues = factory.getDefaultsFor(newType)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Renderizado del Formulario Dinámico
        factory.HeuristicFormBody(
            type = selectedType,
            inputValues = inputValues,
            onValueChange = { key, value ->
                inputValues = inputValues.toMutableMap().apply { put(key, value) }
            }
        )
    }
}

@Composable
fun DeleteHeuristicDialog(
    heuristicName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar Heurística") },
        text = { Text("¿Estás seguro de que deseas eliminar \"$heuristicName\"?") },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Eliminar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// --- Componentes Privados Auxiliares ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeuristicTypeSelector(
    types: List<String>,
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedType,
            onValueChange = {},
            readOnly = true,
            label = { Text("Tipo de heurística") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            types.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type) },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Lógica pura: Convierte el objeto Heuristic a Map<String, String> para la UI
private fun heuristicToMap(heuristic: Heuristic): Map<String, String> {
    return try {
        val jsonString = json.encodeToString(PolymorphicSerializer(Heuristic::class), heuristic)
        val jsonElement = json.parseToJsonElement(jsonString).jsonObject

        jsonElement.mapValues { entry ->
            val value = entry.value
            when {
                value is JsonPrimitive -> value.content
                value is JsonArray -> value.joinToString(",") { it.jsonPrimitive.content }
                else -> value.toString()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyMap()
    }
}