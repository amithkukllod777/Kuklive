package com.kuklive.app

import android.app.Application
import com.kuklive.app.data.PlaylistRepository
import com.kuklive.app.data.SettingsStore

/**
 * Application-scoped singletons. A lightweight service-locator keeps the app
 * dependency-injection-free while still sharing a single repository / settings
 * store across ViewModels.
 */
class KukliveApplication : Application() {

    val settingsStore: SettingsStore by lazy { SettingsStore(this) }
    val playlistRepository: PlaylistRepository by lazy { PlaylistRepository() }
}
