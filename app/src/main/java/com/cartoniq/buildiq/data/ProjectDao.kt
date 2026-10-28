package com.cartoniq.buildiq.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ProjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun observeProjects(): LiveData<List<ProjectEntity>>

    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    suspend fun getAllProjects(): List<ProjectEntity>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): ProjectEntity?

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    @Query("DELETE FROM projects")
    suspend fun clearProjects()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPrice(price: PriceEntity)

    @Query("DELETE FROM prices WHERE `key` = :key")
    suspend fun deletePrice(key: String)

    @Query("SELECT * FROM prices")
    suspend fun getPrices(): List<PriceEntity>

    @Query("DELETE FROM prices")
    suspend fun clearPrices()
}
