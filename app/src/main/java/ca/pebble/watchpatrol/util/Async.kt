package ca.pebble.watchpatrol.util

/** Minimal one-shot async state for screens that aren't paged. */
sealed interface Async<out T> {
    data object Loading : Async<Nothing>
    data class Success<T>(val data: T) : Async<T>
    data class Error(val message: String) : Async<Nothing>
}

inline fun <T> runAsync(block: () -> T): Async<T> = try {
    Async.Success(block())
} catch (e: Exception) {
    Async.Error(e.message ?: "Something went wrong")
}
