package ca.pebble.watchpatrol.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import ca.pebble.watchpatrol.domain.model.Listing

/** Pages Reddit listings by the API's `after` fullname cursor. */
class RedditPagingSource(
    private val fetch: suspend (after: String?) -> Pair<List<Listing>, String?>,
) : PagingSource<String, Listing>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Listing> = try {
        val (items, after) = fetch(params.key)
        LoadResult.Page(
            data = items,
            prevKey = null,
            nextKey = after,
        )
    } catch (e: Exception) {
        LoadResult.Error(e)
    }

    override fun getRefreshKey(state: PagingState<String, Listing>): String? = null
}
