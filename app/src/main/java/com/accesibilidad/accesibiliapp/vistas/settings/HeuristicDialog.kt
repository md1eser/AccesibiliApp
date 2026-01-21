package com.accesibilidad.accesibiliapp.vistas.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
fun AddHeuristicDialog(
    heuristicToEdit: Heuristic? = null,
    onSave: (Heuristic) -> Unit,
    onDismiss: () -> Unit,
    labels: List<String>
) {
    val factory = remember(labels) { HeuristicFactory(labels) }
    val isEditing = heuristicToEdit != null

    var selectedType by remember {
        mutableStateOf(
            if (isEditing) heuristicToEdit!!::class.simpleName!!
            else factory.getHeuristicTypes().first()
        )
    }

    var inputValues by remember { mutableStateOf(mapOf<String, String>()) }

    LaunchedEffect(heuristicToEdit, selectedType) {
        if (isEditing && heuristicToEdit != null) {
            try {
                // Serialización polimórfica para convertir la interfaz Heuristic a JSON
                val jsonString = json.encodeToString(PolymorphicSerializer(Heuristic::class), heuristicToEdit)
                val jsonElement = json.parseToJsonElement(jsonString).jsonObject

                val map = jsonElement.mapValues { entry ->
                    val value = entry.value
                    when {
                        value is JsonPrimitive -> value.content
                        value is JsonArray -> value.joinToString(",") { it.jsonPrimitive.content }
                        else -> value.toString()
                    }
                }
                inputValues = map
            } catch (e: Exception) {
                e.printStackTrace()
                inputValues = factory.getDefaultsFor(selectedType)
            }
        } else {
            inputValues = factory.getDefaultsFor(selectedType)
        }
    }

    DialogScaffold(
        title = if (isEditing) "Editar Heurística" else "Agregar Heurística",
        onDismiss = onDismiss,
        confirmButtonText = if (isEditing) "Guardar" else "Agregar",
        onConfirm = {
            try {
                val heuristic = factory.create(selectedType, inputValues)
                onSave(heuristic)
                onDismiss()
            } catch (e: Exception) {
                println("Error al guardar la heurística: ${e.message}")
            }
        },
        showCancelButton = true, // Activamos el botón de cancelar
        cancelButtonText = "Cancelar"
    ) {
        // NOTA: Ya estamos dentro de un ColumnScope provisto por DialogScaffold.
        // No hace falta abrir otro Column {} aquí, podemos poner los elementos directamente.

        if (!isEditing) {
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
                    factory.getHeuristicTypes().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                selectedType = type
                                expanded = false
                                inputValues = factory.getDefaultsFor(type)
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        factory.HeuristicFormBody(
            type = selectedType,
            inputValues = inputValues,
            onValueChange = { key, value ->
                inputValues = inputValues.toMutableMap().apply { put(key, value) }
            }
        )
    }
}