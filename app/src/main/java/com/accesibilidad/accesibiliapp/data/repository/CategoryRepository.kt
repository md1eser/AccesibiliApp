package com.accesibilidad.accesibiliapp.data.repository


import com.accesibilidad.accesibiliapp.data.dao.CategoryDao
import com.accesibilidad.accesibiliapp.data.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {

    suspend fun insert(category: CategoryEntity): Long {
        return categoryDao.insert(category)
    }

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.update(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.delete(category)
    }

    fun getAllCategories(): Flow<List<CategoryEntity>> {
        return categoryDao.getAllCategories()
    }

    fun getRootCategories(): Flow<List<CategoryEntity>> {
        return categoryDao.getRootCategories()
    }

    fun getSubcategories(parentId: Long): Flow<List<CategoryEntity>> {
        return categoryDao.getSubcategories(parentId)
    }
}