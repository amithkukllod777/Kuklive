package com.kuklive.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kuklive.app.ui.MainViewModel
import com.kuklive.app.ui.screen.ChannelsScreen
import com.kuklive.app.ui.screen.PlayerScreen
import com.kuklive.app.ui.screen.SettingsScreen
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
