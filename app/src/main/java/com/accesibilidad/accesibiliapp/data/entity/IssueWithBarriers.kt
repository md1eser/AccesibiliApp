// IssueWithBarriers.kt
package com.accesibilidad.accesibiliapp.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class IssueWithBarriers(
    @Embedded val issue: Issue,
    @Relation(
        parentColumn = "id",
        entityColumn = "issueId"
    )
    val barriers: List<Barrier>
)