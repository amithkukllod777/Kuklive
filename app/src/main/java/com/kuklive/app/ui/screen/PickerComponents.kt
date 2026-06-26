package com.kuklive.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kuklive.app.data.Catalog
import com.kuklive.app.ui.theme.Brand

/** A labelled dropdown that picks a value from [options] (value -> display label). */
@Composable
fun LabeledDropdown(
    label: String,
    selectedLabel: String,
    options: List<Pair<String, String>>,
    selectedValue: String,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground)

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Text(
                text = selectedLabel,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 440.dp),
        ) {
            options.forEach { (value, display) ->
                DropdownMenuItem(
                    text = { Text(display) },
                    onClick = { onSelected(value); expanded = false },
                    trailingIcon = {
                        if (value == selectedValue) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Brand)
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun CountryDropdown(selected: String, onSelected: (String) -> Unit) {
    val options = Catalog.countries.map { it.code to it.label }
    val selectedLabel = Catalog.countries.firstOrNull { it.code == selected }?.label
        ?: Catalog.countryLabel(selected) ?: "Select country"
    LabeledDropdown("Country", selectedLabel, options, selected, onSelected)
}

@Composable
fun LanguageDropdown(selected: String, onSelected: (String) -> Unit) {
    val options = Catalog.languages.map { it.code to it.name }
    val selectedLabel = Catalog.languages.firstOrNull { it.code == selected }?.name ?: "Any language"
    LabeledDropdown("Language", selectedLabel, options, selected, onSelected)
}
