// ReportMetadata.kt
package com.accesibilidad.accesibiliapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class ReportMetadata(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val image: ByteArray,
    val categoryId: Long? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReportMetadata

        if (id != other.id) return false
        if (name != other.name) return false
        if (!image.contentEquals(other.image)) return false
        if (categoryId != other.categoryId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + image.contentHashCode()
        result = 31 * result + (categoryId?.hashCode() ?: 0)
        return result
    }
}