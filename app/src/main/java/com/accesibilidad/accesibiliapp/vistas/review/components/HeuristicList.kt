package com.accesibilidad.accesibiliapp.vistas.review.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.accesibilidad.accesibiliapp.data.entity.IssueWithBarriers

@Composable
fun HeuristicList(
    allIssues: Map<String, List<IssueWithBarriers>>,
    issuesByHeuristic: Map<String, List<IssueWithBarriers>>, // Las filtradas
    dismissedHeuristics: Set<String>,
    onHeuristicClick: (String) -> Unit,
    onToggleHeuristic: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Obtenemos todas las heurísticas posibles (del mapa completo) para mostrarlas aunque estén vacías/descartadas
    val heuristicsToShow = allIssues.keys.toList()

    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(heuristicsToShow) { heuristicName ->
            val issues = issuesByHeuristic[heuristicName] ?: emptyList()
            val isEnabled = !dismissedHeuristics.contains(heuristicName)
            val count = issues.size

            HeuristicCard(
                heuristicName = heuristicName,
                issueCount = count,
                isEnabled = isEnabled,
                onCardClick = { onHeuristicClick(heuristicName) },
                onToggle = { isChecked -> onToggleHeuristic(heuristicName, isChecked) }
            )
        }
    }
}

@Composable
fun HeuristicCard(
    heuristicName: String,
    issueCount: Int,
    isEnabled: Boolean,
    onCardClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Usabas un componente propio StyledCard, aquí uso uno genérico Card
    Card(
        onClick = onCardClick,
        modifier = modifier.fillMaxWidth(),
        // colors = CardDefaults.cardColors(...) // Ajusta si está deshabilitada
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = heuristicName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$issueCount problemas detectados",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}