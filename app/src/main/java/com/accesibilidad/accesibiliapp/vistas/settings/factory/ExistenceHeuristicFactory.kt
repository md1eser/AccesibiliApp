package com.accesibilidad.accesibiliapp.vistas.settings.factory

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.accesibilidad.accesibiliapp.data.heuristicas.ExistenceHeuristic
import com.accesibilidad.accesibiliapp.data.heuristicas.Heuristic
import java.util.UUID

class ExistenceHeuristicFactory(private val targetOptions: List<String>) : IHeuristicFactory {

    override fun getDefaults(): Map<String, String> = mapOf(
        "id" to "",
        "name" to "", // "name" mapea a HintConstants.AUTOFILL_HINT_NAME en el original
        "desc" to "",
        "targets" to "",
        "scorePresent" to "1.0",
        "scoreMissing" to "0.0"
    )

    @Composable
    override fun FormBody(
        inputValues: Map<String, String>,
        onValueChange: (String, String) -> Unit
    ) {
        HeuristicFormBodyExistence(inputValues, onValueChange, targetOptions)
    }

    override fun create(values: Map<String, String>): Heuristic {
        val id = values["id"]?.takeIf { it.isNotEmpty() } ?: UUID.randomUUID().toString()
        val name = values["name"] ?: ""
        val desc = values["desc"] ?: ""

        // Convertir string CSV a lista, limpiando vacíos
        val targets = values["targets"]
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        val scorePresent = values["scorePresent"]?.toFloatOrNull() ?: 1.0f
        val scoreMissing = values["scoreMissing"]?.toFloatOrNull() ?: 0.0f

        return ExistenceHeuristic(
            scorePresent = scorePresent,
            scoreMissing = scoreMissing,
            id = id,
            name = name,
            targets = targets,
            desc = desc
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeuristicFormBodyExistence(
    inputValues: Map<String, String>,
    onValueChange: (String, String) -> Unit,
    targetOptions: List<String>
) {
    Column {
        // Nombre
        TextField(
            value = inputValues["name"] ?: "",
            onValueChange = { onValueChange("name", it) },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )

        // Descripción
        TextField(
            value = inputValues["desc"] ?: "",
            onValueChange = { onValueChange("desc", it) },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )

        // Targets (Multi-select Dropdown)
        val currentTargetsString = inputValues["targets"] ?: ""
        val selectedTargets = remember(currentTargetsString) {
            currentTargetsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        }
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            TextField(
                value = selectedTargets.joinToString(", "),
                onValueChange = {},
                readOnly = true,
                label = { Text("Objetos") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                targetOptions.forEach { option ->
                    val isSelected = selectedTargets.contains(option)
                    DropdownMenuItem(
                        text = {
                            // Checkbox + Texto
                            androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Checkbox(checked = isSelected, onCheckedChange = null)
                                Text(text = option, modifier = Modifier.padding(start = 8.dp))
                            }
                        },
                        onClick = {
                            val newSet = if (isSelected) {
                                selectedTargets - option
                            } else {
                                selectedTargets + option
                            }
                            onValueChange("targets", newSet.joinToString(","))
                        }
                    )
                }
            }
        }

        // Score Present
        TextField(
            value = inputValues["scorePresent"] ?: "",
            onValueChange = { onValueChange("scorePresent", it) },
            label = { Text("Puntaje si presente") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )

        // Score Missing
        TextField(
            value = inputValues["scoreMissing"] ?: "",
            onValueChange = { onValueChange("scoreMissing", it) },
            label = { Text("Puntaje si ausente") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )
    }
}