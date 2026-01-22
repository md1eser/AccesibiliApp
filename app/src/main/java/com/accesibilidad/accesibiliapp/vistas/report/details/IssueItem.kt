package com.accesibilidad.accesibiliapp.vistas.report.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.accesibilidad.accesibiliapp.data.entity.IssueWithBarriers
import com.accesibilidad.accesibiliapp.vistas.common.rememberColorForId

@Composable
fun IssueItem(
    issueWithBarriers: IssueWithBarriers,
    onClick: () -> Unit
) {
    // Recupera el color asociado al ID del issue (visible en el código descompilado)
    val color = rememberColorForId(issueWithBarriers.issue.id)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp) // El padding de 16.dp se aplica antes del clickable en el binario
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            // El color de fondo es SurfaceContainerHigh según el código descompilado
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // --- ZONA NO DESCOMPILADA ---
        // El contenido interno (IssueItem$lambda$0) lanzó una excepción en el archivo Java.
        // Aquí deberías restaurar tu diseño original. Un ejemplo típico sería:

        /*
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de color
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = issueWithBarriers.issue.metadata?.name ?: "Problema sin nombre",
                    style = MaterialTheme.typography.titleMedium
                )
                // Otros detalles...
            }
        }
        */
    }
}