package com.accesibilidad.accesibiliapp.data.heuristicas

import com.accesibilidad.accesibiliapp.data.deteccion.BoundingBox
import com.accesibilidad.accesibiliapp.data.entity.Barrier
import com.accesibilidad.accesibiliapp.data.entity.Issue
import com.accesibilidad.accesibiliapp.data.entity.IssueWithBarriers
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("existencia")
data class ExistenceHeuristic(
    override val id: String,
    override val name: String,
    override val desc: String,
    override val targets: List<String>,
    val scorePresent: Float,
    val scoreMissing: Float
) : Heuristic {

    override fun calcular(barriers: List<BoundingBox>): List<IssueWithBarriers> {
        val issues = mutableListOf<IssueWithBarriers>()

        // Buscamos si alguno de los objetos detectados está en nuestra lista de targets
        val detectedTargets = barriers.filter { it.clsName in targets }

        if (detectedTargets.isNotEmpty()) {
            // Si existe, creamos un Issue por cada hallazgo
            detectedTargets.forEach { box ->
                val issue = Issue(
                    id = 0,
                    reportId = 0,
                    type = "existencia",
                    desc = desc,
                    score = scorePresent
                )
                val barrier = Barrier(boundingBox = box)
                issues.add(IssueWithBarriers(issue, listOf(barrier)))
            }
        } else {
            // Lógica opcional: Si no existe y debería, podrías reportar el scoreMissing aquí
        }

        return issues
    }
}