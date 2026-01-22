package com.accesibilidad.accesibiliapp.vistas.report.details

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.accesibilidad.accesibiliapp.data.entity.Issue
import com.accesibilidad.accesibiliapp.data.entity.ReportEntity

@Composable
fun ReportDetailsContent(
    report: ReportEntity,
    onIssueClick: (Issue) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Gráfico de puntuación (peso 0.4 -> 40% de la pantalla)
        IssueScoreChart(
            issues = report.issues,
            modifier = Modifier
                .weight(0.4f)
                .fillMaxWidth()
                .padding(16.dp)
        )

        Text(
            text = "Problemáticas Encontradas",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        // Lista de problemas (peso 0.6 -> 60% de la pantalla)
        IssueList(
            issues = report.issues,
            onIssueClick = onIssueClick,
            modifier = Modifier.weight(0.6f)
        )
    }
}