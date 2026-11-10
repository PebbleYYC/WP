package ca.pebble.watchpatrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ca.pebble.watchpatrol.ui.navigation.WatchPatrolRoot
import ca.pebble.watchpatrol.ui.theme.WatchPatrolTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WatchPatrolTheme {
                WatchPatrolRoot()
            }
        }
    }
}
