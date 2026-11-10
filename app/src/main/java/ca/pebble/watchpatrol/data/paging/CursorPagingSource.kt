package ca.pebble.watchpatrol.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import ca.pebble.watchpatrol.data.remote.dto.Paginated

/**
 * Generic cursor pager. The page key is the absolute `next` URL returned by
 * the API, so it works for any DRF-style cursor pagination regardless of the
 * underlying query parameters.
 */
class CursorPagingSource<Dto : Any, Model : Any>(
    private val firstPage: suspend () -> Paginated<Dto>,
    private val nextPage: suspend (url: String) -> Paginated<Dto>,
    private val map: (Dto) -> Model,
) : PagingSource<String, Model>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Model> = try {
        val response = params.key?.let { nextPage(it) } ?: firstPage()
        LoadResult.Page(
            data = response.results.map(map),
            prevKey = null,
            nextKey = response.next,
        )
    } catch (e: Exception) {
        LoadResult.Error(e)
    }

    override fun getRefreshKey(state: PagingState<String, Model>): String? = null
}
