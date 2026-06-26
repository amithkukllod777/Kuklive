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
    initialCountries: Set<String> = setOf(SettingsStore.DEFAULT_COUNTRY),
    initialLanguages: Set<String> = emptySet(),
    onContinue: (countries: Set<String>, languages: Set<String>) -> Unit,
) {
    var countries by remember { mutableStateOf(initialCountries) }
    var languages by remember { mutableStateOf(initialLanguages) }

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
                "Pick countries and languages — choose as many as you like, or leave them open for all. We load only what you pick so it stays fast.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(28.dp))

            Column(
                modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MultiCountryPicker(
                    selected = countries,
                    onToggle = { code -> countries = countries.toggle(code) },
                    onClear = { countries = emptySet() },
                )
                Spacer(Modifier.height(8.dp))
                MultiLanguagePicker(
                    selected = languages,
                    onToggle = { code -> languages = languages.toggle(code) },
                    onClear = { languages = emptySet() },
                )
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = { onContinue(countries, languages) },
                colors = ButtonDefaults.buttonColors(containerColor = Brand),
                modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp).height(52.dp),
            ) {
                Text("Show channels", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

internal fun Set<String>.toggle(value: String): Set<String> =
    if (value in this) this - value else this + value
