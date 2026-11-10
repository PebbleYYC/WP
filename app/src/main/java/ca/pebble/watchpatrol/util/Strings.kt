package ca.pebble.watchpatrol.util

/** "rolex-submariner" -> "Rolex Submariner". */
fun prettifySlug(slug: String): String =
    slug.split('-', '_')
        .filter { it.isNotBlank() }
        .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
        .ifBlank { slug }
