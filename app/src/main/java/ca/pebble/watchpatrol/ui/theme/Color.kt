package ca.pebble.watchpatrol.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Brand palette — deep navy + a cool steel-blue accent, with a warm "price" gold.
internal val Ink = Color(0xFF0E1420)
internal val InkElevated = Color(0xFF151D2C)
internal val InkCard = Color(0xFF1A2335)
internal val Steel = Color(0xFF7FB2FF)
internal val SteelDark = Color(0xFF2E4D7A)
internal val Mist = Color(0xFFE8ECF4)
internal val MutedText = Color(0xFF9AA7BD)
internal val Gold = Color(0xFFE7C067)
internal val Positive = Color(0xFF5BD6A0)
internal val Negative = Color(0xFFF2808A)

val WpDarkColors = darkColorScheme(
    primary = Steel,
    onPrimary = Ink,
    primaryContainer = SteelDark,
    onPrimaryContainer = Mist,
    secondary = Gold,
    onSecondary = Ink,
    background = Ink,
    onBackground = Mist,
    surface = InkElevated,
    onSurface = Mist,
    surfaceVariant = InkCard,
    onSurfaceVariant = MutedText,
    outline = Color(0xFF33405A),
    outlineVariant = Color(0xFF26324A),
    error = Negative,
    onError = Ink,
    surfaceContainer = InkCard,
    surfaceContainerHigh = Color(0xFF202B41),
    surfaceContainerHighest = Color(0xFF26324A),
)

val WpLightColors = lightColorScheme(
    primary = Color(0xFF2E5AA8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7E3FB),
    onPrimaryContainer = Color(0xFF0B1B35),
    secondary = Color(0xFF8A6D1F),
    onSecondary = Color.White,
    background = Color(0xFFF7F9FD),
    onBackground = Color(0xFF14181F),
    surface = Color.White,
    onSurface = Color(0xFF14181F),
    surfaceVariant = Color(0xFFEDF1F8),
    onSurfaceVariant = Color(0xFF53607A),
    outline = Color(0xFFC3CDDE),
    error = Color(0xFFB3261E),
    onError = Color.White,
)
