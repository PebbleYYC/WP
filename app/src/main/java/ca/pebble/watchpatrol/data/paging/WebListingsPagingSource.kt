package ca.pebble.watchpatrol.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import ca.pebble.watchpatrol.domain.model.Listing

/**
 * Pages the scraped public feed by page number. Dedupes across pages (the
 * public feed uses "Load More", and ?page= may be ignored and re-serve page 1),
 * stopping cleanly when a page yields no new listings.
 */
class WebListingsPagingSource(
    private val fetch: suspend (page: Int) -> List<Listing>,
) : PagingSource<Int, Listing>() {

    private val seen = mutableSetOf<Long>()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Listing> = try {
        val page = params.key ?: 1
        val items = fetch(page)
        val fresh = items.filter { seen.add(it.id) }
        LoadResult.Page(
            data = fresh,
            prevKey = null,
            nextKey = if (fresh.isEmpty()) null else page + 1,
        )
    } catch (e: Exception) {
        LoadResult.Error(e)
    }

    override fun getRefreshKey(state: PagingState<Int, Listing>): Int? = null
}
