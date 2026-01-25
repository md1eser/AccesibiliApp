package com.accesibilidad.accesibiliapp.vistas.report.details.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.accesibilidad.accesibiliapp.data.entity.Issue
import com.accesibilidad.accesibiliapp.data.entity.IssueWithBarriers
import com.accesibilidad.accesibiliapp.vistas.common.rememberColorForId

@Composable
fun IssueList(
    issues: List<IssueWithBarriers>,
    onIssueClick: (Issue) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(issues, key = { it.issue.id }) { issueWithBarriers ->
            IssueItem(
                issueWithBarriers = issueWithBarriers,
                onClick = { onIssueClick(issueWithBarriers.issue) }
            )
        }
    }
}

@Composable
private fun IssueItem(
    issueWithBarriers: IssueWithBarriers,
    onClick: () -> Unit
) {
    val color = rememberColorForId(issueWithBarriers.issue.id)
    val issue = issueWithBarriers.issue
    val barrierCount = issueWithBarriers.barriers.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
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
            // Indicador de Color
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(color, CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Información
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = issue.type ?: "Tipo desconocido",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (!issue.desc.isNullOrBlank()) {
                    Text(
                        text = issue.desc,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "$barrierCount barreras",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}