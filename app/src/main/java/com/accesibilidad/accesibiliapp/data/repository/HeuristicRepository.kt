package com.accesibilidad.accesibiliapp.data.repository

import android.util.Log
import com.accesibilidad.accesibiliapp.data.dao.HeuristicDao
import com.accesibilidad.accesibiliapp.data.entity.HeuristicEntity
import com.accesibilidad.accesibiliapp.data.heuristicas.Heuristic
import com.accesibilidad.accesibiliapp.data.heuristicas.ExistenceHeuristic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.serializer
import javax.inject.Inject
import javax.inject.Singleton

// Módulo de serialización para heurísticas polimórficas
private val heuristicModule = SerializersModule {
    polymorphic(Heuristic::class) {
        subclass(ExistenceHeuristic::class, ExistenceHeuristic.serializer())
        // Agrega aquí otras subclases de Heuristic cuando las tengas
    }
}

// Instancia de Json configurada para heurísticas
val json = Json {
    serializersModule = heuristicModule
    classDiscriminator = "heuristicType"
    prettyPrint = true
    ignoreUnknownKeys = true
}

@Singleton
class HeuristicRepository @Inject constructor(
    private val dao: HeuristicDao
) {

    /**
     * Obtiene todas las heurísticas de la base de datos
     */
    fun getAll(): Flow<List<Heuristic>> {
        return dao.getAll().map { entities ->
            entities.mapNotNull { entity ->
                try {
                    json.decodeFromString<Heuristic>(entity.heuristicJson)
                } catch (e: Exception) {
                    Log.e("HeuristicRepository", "Error deserializing heuristic ${entity.id}", e)
                    null
                }
            }
        }
    }

    /**
     * Obtiene una heurística por ID desde la base de datos
     */
    fun getHeuristic(id: String): Flow<Heuristic?> {
        return dao.getById(id).map { entity ->
            entity?.let {
                try {
                    json.decodeFromString<Heuristic>(it.heuristicJson)
                } catch (e: Exception) {
                    Log.e("HeuristicRepository", "Error deserializing heuristic $id", e)
                    null
                }
            }
        }
    }

    /**
     * Agrega una nueva heurística a la base de datos
     */
    suspend fun add(heuristic: Heuristic) {
        Log.d("HeuristicRepository", "Adding heuristic: $heuristic")
        try {
            val jsonString = json.encodeToString(serializer(), heuristic)
            val entity = HeuristicEntity(
                id = heuristic.id,
                heuristicJson = jsonString
            )
            dao.insert(entity)
        } catch (e: Exception) {
            Log.e("HeuristicRepository", "Error adding heuristic ${heuristic.id}", e)
            throw e
        }
    }

    /**
     * Actualiza una heurística existente
     */
    suspend fun update(heuristic: Heuristic) {
        try {
            val jsonString = json.encodeToString(serializer(), heuristic)
            val entity = HeuristicEntity(
                id = heuristic.id,
                heuristicJson = jsonString
            )
            dao.insert(entity) // insert con REPLACE funciona como update
        } catch (e: Exception) {
            Log.e("HeuristicRepository", "Error updating heuristic ${heuristic.id}", e)
            throw e
        }
    }

    /**
     * Elimina una heurística por objeto
     */
    suspend fun delete(heuristic: Heuristic) {
        dao.deleteById(heuristic.id)
    }

    /**
     * Elimina una heurística por ID
     */
    suspend fun deleteById(id: String) {
        Log.d("HeuristicRepository", "Deleting heuristic with id: $id")
        dao.deleteById(id)
    }

    /**
     * Elimina todas las heurísticas de la base de datos
     */
    suspend fun deleteAll() {
        dao.deleteAll()
    }
}