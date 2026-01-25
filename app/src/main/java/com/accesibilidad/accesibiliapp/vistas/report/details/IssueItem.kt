package com.accesibilidad.accesibiliapp.vistas.report.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.accesibilidad.accesibiliapp.data.entity.IssueWithBarriers
import com.accesibilidad.accesibiliapp.vistas.common.rememberColorForId

@Composable
fun IssueItem(
    issueWithBarriers: IssueWithBarriers,
    onClick: () -> Unit
) {
    val color = rememberColorForId(issueWithBarriers.issue.id)
    val issue = issueWithBarriers.issue
    val barrierCount = issueWithBarriers.barriers.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Indicador Visual de Color
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(color, CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 2. Información del Issue
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = issue.type ?: "Tipo desconocido",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (!issue.desc.isNullOrBlank()) {
                    Text(
                        text = issue.desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 3. Badge o Contador de Barreras
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "$barrierCount barreras detectadas",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }


        }
    }
}