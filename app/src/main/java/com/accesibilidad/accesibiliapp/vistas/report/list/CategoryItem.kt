package com.accesibilidad.accesibiliapp.vistas.report.list


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.accesibilidad.accesibiliapp.data.entity.CategoryEntity
import com.accesibilidad.accesibiliapp.vistas.common.refactor.StyledListItem

@Composable
fun CategoryItem(
    category: CategoryEntity,
    score: Float?,
    onClick: () -> Unit,
    onRename: (CategoryEntity) -> Unit,
    onDelete: (CategoryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    StyledListItem(
        modifier = modifier,
        onClick = onClick,
        content = {
            // El código decompilado muestra un Row con un botón y un menú
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Menú de opciones (3 puntos o carpeta según icono en decompilado)
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Filled.Folder, // Icono usado en el decompilado
                            contentDescription = "Opciones de carpeta"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Renombrar") },
                            onClick = {
                                onRename(category)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar") },
                            onClick = {
                                onDelete(category)
                                showMenu = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Nombre de la categoría y puntaje
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = category.name)

                    if (score != null) {
                        Text(text = String.format("%.2f", score))
                    }
                }
            }
        }
    )
}