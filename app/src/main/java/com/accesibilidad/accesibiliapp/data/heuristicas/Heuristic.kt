package com.accesibilidad.accesibiliapp.data.heuristicas

import com.accesibilidad.accesibiliapp.data.deteccion.BoundingBox
import com.accesibilidad.accesibiliapp.data.entity.IssueWithBarriers
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Polymorphic
@Serializable
interface Heuristic {
    val id: String
    val name: String
    val desc: String
    val targets: List<String>

    fun calcular(barriers: List<BoundingBox>): List<IssueWithBarriers>
}