package com.accesibilidad.accesibiliapp.vistas.report.list

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.accesibilidad.accesibiliapp.vistas.common.refactor.StyledListItem

@Composable
fun AddCategoryItem(
    onClick: () -> Unit
) {
    StyledListItem(
        onClick = onClick,
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir Categoria"
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Añadir Categoria")
            }
        }
    )
}