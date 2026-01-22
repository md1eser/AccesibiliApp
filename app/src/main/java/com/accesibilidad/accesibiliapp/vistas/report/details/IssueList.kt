package com.accesibilidad.accesibiliapp.vistas.report.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.accesibilidad.accesibiliapp.data.entity.Issue
import com.accesibilidad.accesibiliapp.data.entity.IssueWithBarriers

@Composable
fun IssueList(
    issues: List<IssueWithBarriers>,
    onIssueClick: (Issue) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(issues) { issueWithBarriers ->
            IssueItem(
                issueWithBarriers = issueWithBarriers,
                onClick = { onIssueClick(issueWithBarriers.issue) }
            )
        }
    }
}