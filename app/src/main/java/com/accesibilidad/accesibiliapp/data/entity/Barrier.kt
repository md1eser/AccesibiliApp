package com.accesibilidad.accesibiliapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Ignore
import com.accesibilidad.accesibiliapp.data.deteccion.BoundingBox

@Entity(tableName = "barriers")
data class Barrier(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val issueId: Long = 0,

    // Campos del BoundingBox aplanados
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val cx: Float,
    val cy: Float,
    val w: Float,
    val h: Float,
    val cnf: Float,
    val cls: Int,
    val clsName: String
) {
    // Constructor que acepta BoundingBox directamente
    @Ignore
    constructor(
        id: Long = 0,
        issueId: Long = 0,
        boundingBox: BoundingBox
    ) : this(
        id = id,
        issueId = issueId,
        x1 = boundingBox.x1,
        y1 = boundingBox.y1,
        x2 = boundingBox.x2,
        y2 = boundingBox.y2,
        cx = boundingBox.cx,
        cy = boundingBox.cy,
        w = boundingBox.w,
        h = boundingBox.h,
        cnf = boundingBox.cnf,
        cls = boundingBox.cls,
        clsName = boundingBox.clsName
    )

    // Helper para convertir a BoundingBox
    fun toBoundingBox() = BoundingBox(
        x1 = x1,
        y1 = y1,
        x2 = x2,
        y2 = y2,
        cx = cx,
        cy = cy,
        w = w,
        h = h,
        cnf = cnf,
        cls = cls,
        clsName = clsName
    )

    companion object {
        // Helper para crear Barrier desde BoundingBox (por si lo usas en otros lugares)
        fun fromBoundingBox(issueId: Long, box: BoundingBox, id: Long = 0) =
            Barrier(id = id, issueId = issueId, boundingBox = box)
    }
}