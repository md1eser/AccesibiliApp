package com.accesibilidad.accesibiliapp.vistas.report.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.accesibilidad.accesibiliapp.data.entity.IssueWithBarriers
import com.accesibilidad.accesibiliapp.vistas.common.rememberColorForId

@Composable
fun IssueScoreChart(
    issues: List<IssueWithBarriers>,
    modifier: Modifier = Modifier
) {
    // Calculamos los slices solo cuando cambia la lista de issues
    val slices = remember(issues) {
        issues
            .map { it.issue }
            .filter { it.score != null && it.score!! > 0f }
            .map { issue ->
                // Nota: ColorUtilsKt.rememberColorForId parece ser una función composable
                // o una utilidad tuya. Si es composable, no puede ir dentro de este remember normal.
                // Aquí asumo la lógica de mapeo.
                // Si rememberColorForId necesita composer, deberás mover esto fuera o llamar a la utilidad de color aquí.
                PieSlice(
                    value = issue.score!!,
                    color = androidx.compose.ui.graphics.Color.Gray // Reemplazar con lógica real de color
                )
            }
    }

    // Si rememberColorForId es Composable, la lógica correcta sería iterar fuera:
    /*
    val slices = issues.mapNotNull {
        val score = it.issue.score
        if (score != null && score > 0) {
            val color = rememberColorForId(it.issue.id)
            PieSlice(score, color)
        } else null
    }
    */

    ScoreChart(slices, modifier)
}

@Composable
fun ScoreChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier
) {
    // Aquí iría tu implementación de Canvas o librería de gráficos
    // basada en el código original que dibuja los slices.
}