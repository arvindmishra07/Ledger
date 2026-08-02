package com.ledger.app.data.repository


import com.ledger.app.data.database.dao.CategoryDao
import com.ledger.app.data.database.entity.CategoryEntity
import com.ledger.app.domain.model.Category
import com.ledger.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAll(): Flow<List<Category>> {
        return categoryDao.getAll().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getById(id: Long): Category? {
        return categoryDao.getById(id)?.toDomain()
    }

    override suspend fun add(
        name: String,
        iconKey: String,
        colorHex: String,
        isIncome: Boolean,
        sortOrder: Int
    ): Long {
        return categoryDao.insert(
            CategoryEntity(
                name = name,
                iconKey = iconKey,
                colorHex = colorHex,
                isIncome = isIncome,
                sortOrder = sortOrder,
                isDefault = false
            )
        )
    }

    override suspend fun update(category: Category) {
        categoryDao.update(category.toEntity())
    }

    override suspend fun delete(category: Category) {
        categoryDao.delete(category.toEntity())
    }

    private fun CategoryEntity.toDomain(): Category = Category(
        id = id,
        name = name,
        iconKey = iconKey,
        colorHex = colorHex,
        isIncome = isIncome,
        sortOrder = sortOrder,
        isDefault = isDefault
    )

    private fun Category.toEntity(): CategoryEntity = CategoryEntity(
        id = id,
        name = name,
        iconKey = iconKey,
        colorHex = colorHex,
        isIncome = isIncome,
        sortOrder = sortOrder,
        isDefault = isDefault
    )
}