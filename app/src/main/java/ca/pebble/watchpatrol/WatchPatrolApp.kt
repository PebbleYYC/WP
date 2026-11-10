package ca.pebble.watchpatrol

import android.app.Application
import ca.pebble.watchpatrol.di.AppContainer

class WatchPatrolApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
