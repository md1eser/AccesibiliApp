package com.accesibilidad.accesibiliapp.vistas.review.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReviewTopBar(
    issueCount: Int,
    onContinueClick: () -> Unit
) {
    TopAppBar(
        title = { Text("Problemáticas ($issueCount)") },
        actions = {
            TextButton(onClick = onContinueClick) {
                Text("Continuar")
            }
        }
    )
}