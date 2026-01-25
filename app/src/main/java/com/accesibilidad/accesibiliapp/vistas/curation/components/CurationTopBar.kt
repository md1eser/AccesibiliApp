package com.accesibilidad.accesibiliapp.vistas.curation.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CurationTopBar(
    onContinueClick: () -> Unit
) {
    TopAppBar(
        title = { Text("") }, // Empty as per design
        actions = {
            TextButton(onClick = onContinueClick) {
                Text("Continuar")
            }
        }
    )
}