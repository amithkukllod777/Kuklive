package com.kuklive.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kuklive.app.ui.MainViewModel
import com.kuklive.app.ui.screen.ChannelsScreen
import com.kuklive.app.ui.screen.PlayerScreen
import com.kuklive.app.ui.screen.SettingsScreen
import com.kuklive.app.ui.screen.SetupScreen
import com.kuklive.app.ui.theme.Brand
import com.kuklive.app.ui.theme.KukliveTheme

object Routes {
    const val CHANNELS = "channels"
    const val PLAYER = "player"
    const val SETTINGS = "settings"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KukliveTheme {
                KukliveApp()
            }
        }
    }
}

@Composable
private fun KukliveApp() {
    val app = LocalAppContext()
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel(
        factory = MainViewModel.Factory(app.playlistRepository, app.settingsStore)
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Gate: splash while settings load, then onboarding, then the main app.
    when {
        !state.settingsLoaded -> {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Brand)
                }
            }
            return
        }
        state.setupNeeded -> {
            SetupScreen(
                initialCountries = state.countryCodes.ifEmpty { setOf(com.kuklive.app.data.SettingsStore.DEFAULT_COUNTRY) },
                initialLanguages = state.languageCodes,
                onContinue = { countries, languages -> viewModel.applySetup(countries, languages) },
            )
            return
        }
    }

    NavHost(navController = navController, startDestination = Routes.CHANNELS) {
        composable(Routes.CHANNELS) {
            ChannelsScreen(
                viewModel = viewModel,
                onChannelClick = { channel ->
                    viewModel.selectForPlayback(channel)
                    navController.navigate(Routes.PLAYER)
                },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.PLAYER) {
            PlayerScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

@Composable
private fun LocalAppContext(): KukliveApplication {
    return LocalContext.current.applicationContext as KukliveApplication
}
