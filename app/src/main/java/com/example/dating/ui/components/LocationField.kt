package com.example.dating.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.text.Normalizer

data class CountryData(val name: String, val cities: List<String>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationField(
    location: String,
    isEditMode: Boolean,
    onLocationChange: (String) -> Unit,
    countries: List<CountryData>
) {
    // Keep the actual selections here
    var selectedCountry by remember { mutableStateOf<CountryData?>(null) }
    var selectedCity by remember { mutableStateOf<String?>(null) }

    // Show previously saved raw value if parsing failed
    val parsed = remember(location) { location.split(",").map { it.trim() } }
    val countryText = selectedCountry?.name ?: parsed.getOrNull(1).orEmpty()
    val cityText    = selectedCity ?: parsed.getOrNull(0).orEmpty()

    // 🔁 Re-sync selection when data arrives, location changes, or you enter edit mode
    LaunchedEffect(countries, location, isEditMode) {
        if (selectedCountry == null || selectedCity == null) {
            parseLocation(location, countries)?.let { (c, city) ->
                selectedCountry = c
                selectedCity = city
            }
        }
    }

    var countryExpanded by remember { mutableStateOf(false) }
    var cityExpanded by remember { mutableStateOf(false) }

    var countryQuery by rememberSaveable { mutableStateOf("") }
    var filteredCountries by remember { mutableStateOf(countries) }
    LaunchedEffect(countryQuery, countries) {
        delay(250)
        filteredCountries =
            if (countryQuery.isBlank()) countries
            else countries.filter {
                it.name.stripAccents().contains(countryQuery.stripAccents(), ignoreCase = true)
            }
    }

    val citySource = selectedCountry?.cities.orEmpty()
    var cityQuery by rememberSaveable(selectedCountry?.name) { mutableStateOf("") }
    var filteredCities by remember { mutableStateOf(citySource) }
    LaunchedEffect(cityQuery, selectedCountry) {
        delay(250)
        filteredCities =
            if (cityQuery.isBlank()) citySource
            else citySource.filter {
                it.stripAccents().contains(cityQuery.stripAccents(), ignoreCase = true)
            }
    }

    // Reflect to parent when both are valid
    LaunchedEffect(selectedCountry, selectedCity) {
        val c = selectedCountry?.name
        val t = selectedCity
        if (!c.isNullOrBlank() && !t.isNullOrBlank()) {
            onLocationChange("$t, $c")
        }
    }

    Column(Modifier.fillMaxWidth()) {

        // ===== COUNTRY =====
        ExposedDropdownMenuBox(
            expanded = countryExpanded && isEditMode,
            onExpandedChange = { if (isEditMode) countryExpanded = !countryExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = countryText,
                onValueChange = { /* read-only display; search in menu */ },
                label = { Text("Country") },
                singleLine = true,
                readOnly = true,
                enabled = isEditMode,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    // 👇 Tap the field itself to open the menu
                    .clickable(enabled = isEditMode) { countryExpanded = true },

            )

            ExposedDropdownMenu(
                expanded = countryExpanded && isEditMode,
                onDismissRequest = { countryExpanded = false },
                modifier = Modifier.heightIn(max = 360.dp)
            ) {
                DropdownSearchField(
                    query = countryQuery,
                    onQueryChange = { countryQuery = it },
                    placeholder = "Search country…",
                    ime = ImeAction.Next
                )

                if (filteredCountries.isEmpty()) {
                    DisabledMenuText("No countries match")
                } else {
                    Column {
                        filteredCountries.take(150).forEach { country ->
                            DropdownMenuItem(
                                text = { Text(country.name) },
                                onClick = {
                                    // ✅ Changing country always resets city
                                    selectedCountry = country
                                    selectedCity = null
                                    cityQuery = ""
                                    countryExpanded = false
                                    // Optionally: immediately open the City picker to continue flow
                                    cityExpanded = true
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // ===== CITY =====
        ExposedDropdownMenuBox(
            expanded = cityExpanded && isEditMode && selectedCountry != null,
            onExpandedChange = {
                if (isEditMode && selectedCountry != null) {
                    cityExpanded = !cityExpanded
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = cityText,
                onValueChange = { /* read-only display; search in menu */ },
                label = { Text("City") },
                singleLine = true,
                readOnly = true,
                enabled = isEditMode && selectedCountry != null,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    // 👇 Tap the field anytime (after selection) to re-open the city menu
                    .clickable(enabled = isEditMode && selectedCountry != null) { cityExpanded = true },
                supportingText = {
                    val msg = when {
                        !isEditMode -> "Locked (Edit profile to change)"
                        selectedCountry == null -> "Choose a country first"
                        else -> ""
                    }
                    Text(msg, Modifier.alpha(if (msg.isEmpty()) 0f else 1f))
                }
            )

            ExposedDropdownMenu(
                expanded = cityExpanded && isEditMode && selectedCountry != null,
                onDismissRequest = { cityExpanded = false },
                modifier = Modifier.heightIn(max = 360.dp)
            ) {
                DropdownSearchField(
                    query = cityQuery,
                    onQueryChange = { cityQuery = it },
                    placeholder = "Search city…",
                    ime = ImeAction.Done
                )

                if (filteredCities.isEmpty()) {
                    DisabledMenuText("No cities match")
                } else {
                    Column {
                        filteredCities.take(300).forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city) },
                                onClick = {
                                    // ✅ Always from the selected country’s list,
                                    // so it cannot become invalid.
                                    selectedCity = city
                                    cityExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        if (isEditMode && (selectedCountry == null || selectedCity == null)) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Select a country and city from the lists",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DropdownSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    ime: ImeAction
) {
    Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ime),
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            }
        )
    }
}

@Composable
private fun DisabledMenuText(text: String) {
    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun parseLocation(
    location: String,
    countries: List<CountryData>
): Pair<CountryData, String>? {
    val parts = location.split(",").map { it.trim() }
    if (parts.size < 2) return null
    val city = parts[0]
    val countryName = parts[1]
    val country = countries.firstOrNull {
        it.name.stripAccents().equals(countryName.stripAccents(), ignoreCase = true)
    } ?: return null
    val validCity = country.cities.firstOrNull {
        it.stripAccents().equals(city.stripAccents(), ignoreCase = true)
    } ?: return null
    return country to validCity
}

fun String.stripAccents(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
