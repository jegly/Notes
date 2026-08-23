package com.kin.easynotes.presentation.screens.settings.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Battery1Bar
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DynamicFeed
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.HdrAuto
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RoundedCorner
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.kin.easynotes.R
import com.kin.easynotes.presentation.screens.settings.SettingsScaffold
import com.kin.easynotes.presentation.screens.settings.model.SettingsViewModel
import com.kin.easynotes.presentation.screens.settings.widgets.ActionType
import com.kin.easynotes.presentation.screens.settings.widgets.SettingsBox
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextOverflow
import com.kin.easynotes.presentation.theme.AppFont
import com.kin.easynotes.presentation.theme.CatppuccinMocha
import com.kin.easynotes.presentation.theme.DraculaColors
import com.kin.easynotes.presentation.theme.PALETTE_COLORS
import com.kin.easynotes.presentation.theme.PtyxisPalette
import com.kin.easynotes.presentation.theme.PtyxisPaletteExtended
import com.kin.easynotes.presentation.theme.appFontFromKey
import androidx.compose.ui.graphics.Color

fun shapeManager(isBoth: Boolean = false,isLast: Boolean = false,isFirst: Boolean = false,radius: Int): RoundedCornerShape {
    val smallerRadius: Dp = (radius/5).dp
    val defaultRadius: Dp = radius.dp

    return when {
        isBoth -> RoundedCornerShape(defaultRadius)
        isLast -> RoundedCornerShape(smallerRadius,smallerRadius,defaultRadius,defaultRadius)
        isFirst -> RoundedCornerShape(defaultRadius,defaultRadius,smallerRadius,smallerRadius)
        else -> RoundedCornerShape(smallerRadius)
    }
}

@Composable
fun ColorStylesScreen(navController: NavController, settingsViewModel: SettingsViewModel) {
    val themeMode = settingsViewModel.settings.value.themeMode
    val isPresetActive = themeMode == "catppuccin" || themeMode == "dracula"

    SettingsScaffold(
        settingsViewModel = settingsViewModel,
        title = stringResource(id = R.string.color_styles),
        onBackNavClicked = { navController.navigateUp() }
    ) {
        LazyColumn {
            item {
                ThemeModeSection(settingsViewModel)
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.minimalistic_mode),
                    description = stringResource(id = R.string.minimalistic_mode_description),
                    icon = Icons.Rounded.DynamicFeed,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isBoth = true),
                    actionType = ActionType.SWITCH,
                    variable = settingsViewModel.settings.value.minimalisticMode,
                    switchEnabled = { settingsViewModel.update(settingsViewModel.settings.value.copy(minimalisticMode = it))}
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.system_theme),
                    description = stringResource(id = R.string.system_theme_description),
                    icon = Icons.Rounded.HdrAuto,
                    actionType = ActionType.SWITCH,
                    radius = shapeManager(isFirst = true ,isBoth = (!isSystemInDarkTheme() && settingsViewModel.settings.value.automaticTheme), radius = settingsViewModel.settings.value.cornerRadius),
                    variable = settingsViewModel.settings.value.automaticTheme,
                    isEnabled = !isPresetActive,
                    switchEnabled = { settingsViewModel.update(settingsViewModel.settings.value.copy(automaticTheme = it))}
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.dark_theme),
                    description = stringResource(id = R.string.dark_theme_description),
                    isEnabled = !isPresetActive && !settingsViewModel.settings.value.automaticTheme,
                    icon = Icons.Rounded.Palette,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius),
                    actionType = ActionType.SWITCH,
                    variable = settingsViewModel.settings.value.darkTheme,
                    switchEnabled = { settingsViewModel.update(settingsViewModel.settings.value.copy(automaticTheme = false, darkTheme = it))}
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.dynamic_colors),
                    description = stringResource(id = R.string.dynamic_colors_description),
                    icon = Icons.Rounded.Colorize,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius),
                    isEnabled = !isPresetActive && !settingsViewModel.settings.value.automaticTheme,
                    actionType = ActionType.SWITCH,
                    variable = settingsViewModel.settings.value.dynamicTheme,
                    switchEnabled = { settingsViewModel.update(settingsViewModel.settings.value.copy(automaticTheme = false, dynamicTheme = it))}
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.accent_color),
                    description = stringResource(id = R.string.accent_color_description),
                    icon = Icons.Rounded.Palette,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isLast = !(settingsViewModel.settings.value.darkTheme)),
                    actionType = ActionType.CUSTOM,
                    isEnabled = !isPresetActive,
                    customAction = { onExit ->
                        ColorSelectionDialog(
                            settingsViewModel = settingsViewModel,
                            onDismissRequest = onExit
                        )
                    },
                )
            }
            item {
                val value = settingsViewModel.settings.value.amoledTheme
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.amoled_colors),
                    description = stringResource(id = R.string.amoled_colors_description),
                    icon = Icons.Rounded.DarkMode,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isLast = !value),
                    actionType = ActionType.SWITCH,
                    isEnabled = !isPresetActive && settingsViewModel.settings.value.darkTheme,
                    variable = value,
                    switchEnabled = { settingsViewModel.update(settingsViewModel.settings.value.copy(amoledTheme = it))}
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.extreme_amoled_mode),
                    icon = Icons.Rounded.Battery1Bar,
                    description = stringResource(id = R.string.extreme_amoled_mode_description),
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isLast = true),
                    actionType = ActionType.SWITCH,
                    variable = settingsViewModel.settings.value.extremeAmoledMode,
                    isEnabled = !isPresetActive && settingsViewModel.settings.value.amoledTheme && settingsViewModel.settings.value.darkTheme,
                    switchEnabled = { settingsViewModel.update(settingsViewModel.settings.value.copy(extremeAmoledMode = it))}
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.radius),
                    description = stringResource(id = R.string.radius_description),
                    icon = Icons.Rounded.RoundedCorner,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isFirst = true),
                    actionType = ActionType.CUSTOM,
                    customAction = {
                        onExit -> OnRadiusClicked(settingsViewModel) {
                            settingsViewModel.update(settingsViewModel.settings.value.copy(cornerRadius = it))
                            onExit()
                        }
                    }
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = if (settingsViewModel.settings.value.viewMode) stringResource(id = R.string.grid_view) else stringResource(id = R.string.column_view),
                    icon = if (settingsViewModel.settings.value.viewMode) Icons.Rounded.GridView else Icons.Rounded.ViewAgenda,
                    description = stringResource(id = R.string.view_style_description),
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius),
                    actionType = ActionType.SWITCH,
                    variable = settingsViewModel.settings.value.viewMode,
                    switchEnabled = { settingsViewModel.update(settingsViewModel.settings.value.copy(viewMode = it))}
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = if (settingsViewModel.settings.value.sortDescending) stringResource(id = R.string.sort_descending) else stringResource(id = R.string.sort_ascending),
                    description = stringResource(id = R.string.sort_description),
                    icon = Icons.AutoMirrored.Rounded.Sort,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isLast = true),
                    actionType = ActionType.SWITCH,
                    variable = settingsViewModel.settings.value.sortDescending,
                    switchEnabled = { settingsViewModel.update(settingsViewModel.settings.value.copy(sortDescending = it)) }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.make_search_bar_longer),
                    description = stringResource(id = R.string.make_search_bar_longer_description),
                    icon = Icons.Rounded.Search,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isFirst = true),
                    actionType = ActionType.SWITCH,
                    variable = settingsViewModel.settings.value.makeSearchBarLonger,
                    switchEnabled = { settingsViewModel.update(settingsViewModel.settings.value.copy(makeSearchBarLonger = it)) }
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.select_font),
                    description = appFontFromKey(settingsViewModel.settings.value.appFont).displayName,
                    icon = Icons.Rounded.FormatSize,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius),
                    actionType = ActionType.CUSTOM,
                    customAction = { onExit ->
                        OnFontFamilyClicked(settingsViewModel) {
                            settingsViewModel.update(settingsViewModel.settings.value.copy(appFont = it))
                            onExit()
                        }
                    }
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.font_size),
                    description = stringResource(id = R.string.font_size_description),
                    icon = Icons.Rounded.FormatSize,
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius, isLast = true),
                    actionType = ActionType.CUSTOM,
                    customAction = {
                        onExit -> OnFontSizeClicked(settingsViewModel) {
                            settingsViewModel.update(settingsViewModel.settings.value.copy(fontSize = it))
                            onExit()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun OnFontFamilyClicked(settingsViewModel: SettingsViewModel, onExit: (String) -> Unit) {
    val currentKey = settingsViewModel.settings.value.appFont

    Dialog(onDismissRequest = { onExit(currentKey) }) {
        Column(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(settingsViewModel.settings.value.cornerRadius / 3)
                )
                .fillMaxWidth()
                .fillMaxSize(0.7f)
        ) {
            Text(
                text = stringResource(id = R.string.select_font),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp)
            )
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(AppFont.entries) { font ->
                    val isSelected = font.key == currentKey
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onExit(font.key) }
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = font.displayName,
                            fontFamily = font.family,
                            fontSize = 16.sp,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnRadiusClicked( settingsViewModel: SettingsViewModel,onExit: (Int) -> Unit) {
    val minimalRadius = 5
    val settingsRadius = settingsViewModel.settings.value.cornerRadius
    var sliderPosition by remember { mutableFloatStateOf(((settingsRadius - minimalRadius).toFloat()/30)) }
    val realRadius : Int  = (((sliderPosition*100).toInt())/3) + minimalRadius

    @Composable
    fun example(shape: RoundedCornerShape) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp, 3.dp, 32.dp, 1.dp)
                .background(
                    shape = shape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                )
                .height(62.dp),
        )
    }
    Dialog(onDismissRequest = { onExit(realRadius) }) {
        Column(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(realRadius / 3)
                )
                .fillMaxWidth()
                .fillMaxSize(0.38f)
        ) {
            Text(
                text = stringResource(id = R.string.select_radius),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp)
            )
            example(shapeManager(radius = realRadius, isFirst = true))
            example(shapeManager(radius = realRadius))
            example(shapeManager(radius =  realRadius, isLast = true))
            Slider(
                value = sliderPosition,
                modifier = Modifier.padding(32.dp,16.dp,32.dp,16.dp),
                colors = SliderDefaults.colors(inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                onValueChange = { newValue -> sliderPosition = newValue}
            )
        }
    }
}

@Composable
fun OnFontSizeClicked(settingsViewModel: SettingsViewModel, onExit: (Int) -> Unit) {
    val minFontSize = 12
    val maxFontSize = 18
    val currentFontSize = settingsViewModel.settings.value.fontSize
    var sliderPosition by remember { mutableFloatStateOf(((currentFontSize - minFontSize).toFloat()) / (maxFontSize - minFontSize)) }
    val selectedFontSize = minFontSize + ((maxFontSize - minFontSize) * sliderPosition).toInt()

    Dialog(onDismissRequest = { onExit(selectedFontSize) }) {
        Column(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(settingsViewModel.settings.value.cornerRadius / 3)
                )
                .fillMaxWidth()
                .fillMaxSize(0.5f)
        ) {
            Text(
                text = stringResource(id = R.string.font_size),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp)
            )
            
            Text(
                text = stringResource(id = R.string.font_size_value, selectedFontSize),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(settingsViewModel.settings.value.cornerRadius / 2)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.font_size_preview_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = stringResource(id = R.string.font_size_preview_text),
                    fontSize = selectedFontSize.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            Slider(
                value = sliderPosition,
                modifier = Modifier.padding(32.dp, 16.dp, 32.dp, 16.dp),
                colors = SliderDefaults.colors(inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                onValueChange = { newValue -> 
                    sliderPosition = newValue 
                    val newFontSize = minFontSize + ((maxFontSize - minFontSize) * newValue).toInt()
                }
            )
            
            // Add a button to confirm the selection
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(settingsViewModel.settings.value.cornerRadius / 2)
                        )
                        .clickable { onExit(selectedFontSize) }
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.agree),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeModeSection(settingsViewModel: SettingsViewModel) {
    val themeMode = settingsViewModel.settings.value.themeMode
    val cornerRadius = settingsViewModel.settings.value.cornerRadius

    Card(
        shape = shapeManager(isBoth = true, radius = cornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                text = stringResource(id = R.string.theme_style),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(id = R.string.theme_style_description),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            val options = listOf(
                "system"     to stringResource(id = R.string.theme_mode_system),
                "catppuccin" to stringResource(id = R.string.theme_mode_catppuccin),
                "dracula"    to stringResource(id = R.string.theme_mode_dracula),
                "ptyxis"     to stringResource(id = R.string.theme_mode_ptyxis),
                "ptyxis_ext" to stringResource(id = R.string.theme_mode_ptyxis_ext),
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                options.forEachIndexed { i, (key, label) ->
                    SegmentedButton(
                        selected = themeMode == key,
                        onClick = {
                            if (themeMode != key) {
                                settingsViewModel.update(settingsViewModel.settings.value.copy(themeMode = key))
                            }
                        },
                        shape = SegmentedButtonDefaults.itemShape(i, options.size)
                    ) {
                        Text(label, fontSize = 12.sp)
                    }
                }
            }

            if (themeMode == "catppuccin" || themeMode == "dracula") {
                val accentEntries: List<Pair<String, Color>>
                val currentKey: String
                val onPick: (String) -> Unit
                if (themeMode == "catppuccin") {
                    accentEntries = CatppuccinMocha.accents.map { (k, v) -> k to v.second }
                    currentKey    = settingsViewModel.settings.value.catppuccinAccent
                    onPick        = {
                        settingsViewModel.update(settingsViewModel.settings.value.copy(catppuccinAccent = it))
                    }
                } else {
                    accentEntries = DraculaColors.accents.map { (k, v) -> k to v.second }
                    currentKey    = settingsViewModel.settings.value.draculaAccent
                    onPick        = {
                        settingsViewModel.update(settingsViewModel.settings.value.copy(draculaAccent = it))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = stringResource(id = R.string.theme_accent_picker_description),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    accentEntries.forEach { (key, color) ->
                        val isSelected = key == currentKey
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (isSelected)
                                        Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                )
                                .clickable { onPick(key) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color.Black.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            } else if (themeMode == "ptyxis") {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = stringResource(id = R.string.theme_palette_picker_description),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val currentKey = settingsViewModel.settings.value.ptyxisPalette
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(PtyxisPalette.entries) { palette ->
                        PtyxisSwatch(
                            background = Color(palette.background),
                            accent = Color(palette.primaryC),
                            label = palette.displayName,
                            isSelected = palette.key == currentKey,
                            onClick = {
                                settingsViewModel.update(settingsViewModel.settings.value.copy(ptyxisPalette = palette.key))
                            }
                        )
                    }
                }
            } else if (themeMode == "ptyxis_ext") {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = stringResource(id = R.string.theme_palette_picker_description),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val currentKey = settingsViewModel.settings.value.ptyxisExtendedPalette
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(PtyxisPaletteExtended.entries) { palette ->
                        PtyxisSwatch(
                            background = Color(palette.background),
                            accent = Color(palette.ansi[4]),
                            label = palette.displayName,
                            isSelected = palette.key == currentKey,
                            onClick = {
                                settingsViewModel.update(settingsViewModel.settings.value.copy(ptyxisExtendedPalette = palette.key))
                            }
                        )
                    }
                }
            }
        }
    }
}

/** A single palette pick: background-filled circle with an accent-colored ring, name below. */
@Composable
private fun PtyxisSwatch(
    background: Color,
    accent: Color,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(56.dp).clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(background)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else accent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(accent))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ColorSelectionDialog(
    settingsViewModel: SettingsViewModel,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = shapeManager(isBoth = true, radius = settingsViewModel.settings.value.cornerRadius),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(id = R.string.select_accent_color),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 16.dp)
                )

                // System Colors Button
                val isUsingSystemColors = settingsViewModel.settings.value.customColor == -7896468
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isUsingSystemColors)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                        .clickable {
                            settingsViewModel.update(settingsViewModel.settings.value.copy(customColor = -7896468))
                            onDismissRequest()
                        }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.use_system_colors),
                        fontWeight = if (isUsingSystemColors) FontWeight.Bold else FontWeight.Normal,
                        color = if (isUsingSystemColors)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }

                val colorChunks = PALETTE_COLORS.chunked(3)
                colorChunks.forEach { rowColors ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowColors.forEach { color ->
                            val isSelected = settingsViewModel.settings.value.customColor == color.toArgb()
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable {
                                        settingsViewModel.update(settingsViewModel.settings.value.copy(customColor = color.toArgb()))
                                        onDismissRequest()
                                    }
                                    .then(
                                        if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                        else Modifier
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}