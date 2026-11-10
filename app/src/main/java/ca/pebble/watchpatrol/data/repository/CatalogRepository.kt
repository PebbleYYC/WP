package ca.pebble.watchpatrol.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ca.pebble.watchpatrol.data.paging.CursorPagingSource
import ca.pebble.watchpatrol.data.remote.WatchPatrolApi
import ca.pebble.watchpatrol.domain.model.Brand
import ca.pebble.watchpatrol.domain.model.WatchModel
import ca.pebble.watchpatrol.domain.model.toDomain
import kotlinx.coroutines.flow.Flow

class CatalogRepository(private val api: WatchPatrolApi) {

    fun brands(query: String? = null): Flow<PagingData<Brand>> =
        Pager(PagingConfig(pageSize = 30, enablePlaceholders = false)) {
            CursorPagingSource(
                firstPage = { api.getBrands(q = query) },
                nextPage = { url -> api.getBrandsByUrl(url) },
                map = { it.toDomain() },
            )
        }.flow

    fun models(brandSlug: String? = null, query: String? = null): Flow<PagingData<WatchModel>> =
        Pager(PagingConfig(pageSize = 30, enablePlaceholders = false)) {
            CursorPagingSource(
                firstPage = { api.getModels(brand = brandSlug, q = query) },
                nextPage = { url -> api.getModelsByUrl(url) },
                map = { it.toDomain() },
            )
        }.flow

    suspend fun brand(slug: String): Brand = api.getBrand(slug).toDomain()

    suspend fun model(slug: String): WatchModel = api.getModel(slug).toDomain()
}
