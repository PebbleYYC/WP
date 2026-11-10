package ca.pebble.watchpatrol.util

import android.content.Context
import android.content.Intent
import android.net.Uri

/** Opens a URL in the user's browser. No-ops on blank/invalid input. */
fun openUrl(context: Context, url: String?) {
    if (url.isNullOrBlank()) return
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
