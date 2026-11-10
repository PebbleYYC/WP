package ca.pebble.watchpatrol.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun WatchPatrolTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Material You wallpaper colors on Android 12+. Off by default so the
    // brand identity stays consistent; flip to true to opt in.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> WpDarkColors
        else -> WpLightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = WpTypography,
        content = content,
    )
}
