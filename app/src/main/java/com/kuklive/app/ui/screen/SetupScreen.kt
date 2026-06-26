package com.kuklive.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kuklive.app.data.SettingsStore
import com.kuklive.app.ui.theme.Brand

@Composable
fun SetupScreen(
    initialCountry: String = SettingsStore.DEFAULT_COUNTRY,
    initialLanguage: String = "",
    onContinue: (country: String, language: String) -> Unit,
) {
    var country by remember { mutableStateOf(initialCountry) }
    var language by remember { mutableStateOf(initialLanguage) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Default.LiveTv, contentDescription = null, tint = Brand, modifier = Modifier.height(56.dp))
            Spacer(Modifier.height(12.dp))
            Text("Welcome to Kuklive", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(6.dp))
            Text(
                "Pick your country and language — we'll load only those channels so it stays fast.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(28.dp))

            Column(
                modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CountryDropdown(selected = country, onSelected = { country = it })
                Spacer(Modifier.height(8.dp))
                LanguageDropdown(selected = language, onSelected = { language = it })
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = { onContinue(country, language) },
                colors = ButtonDefaults.buttonColors(containerColor = Brand),
                modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp).height(52.dp),
            ) {
                Text("Show channels", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
