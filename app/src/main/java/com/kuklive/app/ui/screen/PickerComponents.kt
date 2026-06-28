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
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kuklive.app.data.Catalog
import com.kuklive.app.ui.theme.Brand

/**
 * A labelled multi-select dropdown. An empty selection means "all" and is
 * represented by the [emptyLabel] item at the top, which clears the set.
 */
@Composable
fun MultiSelectDropdown(
    label: String,
    emptyLabel: String,
    summary: String,
    options: List<Pair<String, String>>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onClear: () -> Unit,
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
                text = summary,
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
            modifier = Modifier.heightIn(max = 460.dp),
        ) {
            DropdownMenuItem(
                text = { Text(emptyLabel) },
                onClick = { onClear() },
                leadingIcon = {
                    if (selected.isEmpty()) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Brand)
                    }
                },
            )
            options.forEach { (value, display) ->
                val checked = value in selected
                DropdownMenuItem(
                    text = { Text(display) },
                    onClick = { onToggle(value) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (checked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                            contentDescription = null,
                            tint = if (checked) Brand else Color.Gray,
                        )
                    },
                )
            }
        }
    }
}

@Composable
fun MultiCountryPicker(selected: Set<String>, onToggle: (String) -> Unit, onClear: () -> Unit) {
    MultiSelectDropdown(
        label = "Countries",
        emptyLabel = "All countries",
        summary = Catalog.countrySummary(selected),
        options = Catalog.countries.map { it.code to it.label },
        selected = selected,
        onToggle = onToggle,
        onClear = onClear,
    )
}

@Composable
fun MultiLanguagePicker(selected: Set<String>, onToggle: (String) -> Unit, onClear: () -> Unit) {
    MultiSelectDropdown(
        label = "Languages",
        emptyLabel = "Any language",
        summary = Catalog.languageSummary(selected),
        options = Catalog.languages.filter { it.code.isNotEmpty() }.map { it.code to it.name },
        selected = selected,
        onToggle = onToggle,
        onClear = onClear,
    )
}
