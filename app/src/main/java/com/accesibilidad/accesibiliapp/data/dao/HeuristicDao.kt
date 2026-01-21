package com.accesibilidad.accesibiliapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.accesibilidad.accesibiliapp.data.entity.HeuristicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HeuristicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(heuristic: HeuristicEntity)

    @Query("SELECT * FROM heuristics")
    fun getAll(): Flow<List<HeuristicEntity>>

    @Query("SELECT * FROM heuristics WHERE id = :id")
    fun getById(id: String): Flow<HeuristicEntity?>

    //@Query("SELECT * FROM heuristics WHERE categoryId = :categoryId")
    //fun getByCategoryId(categoryId: Int): Flow<List<HeuristicEntity>>

    @Query("DELETE FROM heuristics WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM heuristics")
    suspend fun deleteAll()
}