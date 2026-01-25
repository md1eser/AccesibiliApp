package com.accesibilidad.accesibiliapp.vistas.report.details.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.accesibilidad.accesibiliapp.data.entity.IssueWithBarriers
import com.accesibilidad.accesibiliapp.vistas.common.rememberColorForId

import com.github.tehras.charts.piechart.PieChart
import com.github.tehras.charts.piechart.PieChartData
import com.github.tehras.charts.piechart.animation.simpleChartAnimation
import com.github.tehras.charts.piechart.renderer.SimpleSliceDrawer


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight

@Composable
fun IssueScoreChart(
    issues: List<IssueWithBarriers>,
    modifier: Modifier = Modifier
) {
    // 1. Transformamos los datos.
    val slices = issues.mapNotNull { item ->
        val score = item.issue.score
        if (score != null && score > 0f) {
            val color = rememberColorForId(item.issue.id)
            PieChartData.Slice(
                value = score,
                color = color
            )
        } else {
            null
        }
    }

    // --- NUEVO: Calculamos el total ---
    // Usamos remember para no recalcular si la lista no cambia
    val totalScore = remember(slices) {
        slices.sumOf { it.value.toDouble() }.toFloat()
    }

    // 2. Preparamos la data del gráfico.
    val pieChartData = remember(slices) {
        PieChartData(
            slices = slices,
            // padAngle = 2f,
        )
    }

    // 3. Renderizamos el Gráfico
    Box(modifier = modifier) {
        if (slices.isNotEmpty()) {
            // Capa 1: El Gráfico (Fondo)
            PieChart(
                pieChartData = pieChartData,
                modifier = Modifier.fillMaxSize(),
                animation = simpleChartAnimation(),
                sliceDrawer = SimpleSliceDrawer(
                    sliceThickness = 30f // Necesario para dejar el hueco en el centro
                )
            )

            // Capa 2: El Texto con la Suma (Frente/Centro)
            // Usamos una Column por si quisieras agregar una etiqueta debajo del número (ej: "Total")
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    // Formateamos para quitar decimales innecesarios (o usa .toString() simple)
                    text = String.format("%.0f", totalScore),
                    style = MaterialTheme.typography.headlineMedium, // Estilo grande de Material3
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface // Color que contraste
                )

                // Opcional: Texto pequeño debajo
                // Text("Puntos", style = MaterialTheme.typography.bodySmall)
            }

        } else {
            Text("No data", modifier = Modifier.align(Alignment.Center))
        }
    }
}