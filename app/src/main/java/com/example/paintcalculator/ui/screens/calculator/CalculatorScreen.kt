package com.example.paintcalculator.ui.screens.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paintcalculator.model.MeasurementUnit
import com.example.paintcalculator.model.OpeningType
import com.example.paintcalculator.ui.viewmodel.PaintCalculatorUiState
import com.example.paintcalculator.ui.viewmodel.PaintCalculatorViewModel
import com.example.paintcalculator.util.PaintCalculatorEngine
import com.example.paintcalculator.util.UnitConverter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CalculatorScreen(
    viewModel: PaintCalculatorViewModel,
    uiState: PaintCalculatorUiState
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var dialogOpeningType by remember { mutableStateOf<OpeningType?>(null) }
    var unitDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.userNotification) {
        uiState.userNotification?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissNotification()
        }
    }

    if (dialogOpeningType != null) {
        OpeningDialog(
            type = dialogOpeningType!!,
            initialUnit = uiState.measurementUnit,
            onDismiss = { dialogOpeningType = null },
            onConfirm = { opening ->
                viewModel.addOpening(opening)
                dialogOpeningType = null
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val isWideScreen = maxWidth >= 760.dp

            if (isWideScreen) {
                // Tablet / Landscape 2-column layout
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Left Column: Inputs
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        HeaderSection()
                        WallDimensionsSection(
                            uiState = uiState,
                            unitDropdownExpanded = unitDropdownExpanded,
                            onUnitDropdownExpandChange = { unitDropdownExpanded = it },
                            viewModel = viewModel
                        )
                        OpeningsSection(
                            uiState = uiState,
                            onAddOpening = { dialogOpeningType = it },
                            onRemoveOpening = { viewModel.removeOpening(it) }
                        )
                        CoatsSection(uiState = uiState, viewModel = viewModel)
                        CoverageSection(uiState = uiState, viewModel = viewModel)
                        WasteSection(uiState = uiState, viewModel = viewModel)
                        PurchaseSizeSection(uiState = uiState, viewModel = viewModel)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Right Column: Results & Actions
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ResultSection(uiState = uiState)
                        ActionButtonsSection(uiState = uiState, viewModel = viewModel)
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            } else {
                // Phone portrait single-column scrolling layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HeaderSection()
                    WallDimensionsSection(
                        uiState = uiState,
                        unitDropdownExpanded = unitDropdownExpanded,
                        onUnitDropdownExpandChange = { unitDropdownExpanded = it },
                        viewModel = viewModel
                    )
                    OpeningsSection(
                        uiState = uiState,
                        onAddOpening = { dialogOpeningType = it },
                        onRemoveOpening = { viewModel.removeOpening(it) }
                    )
                    CoatsSection(uiState = uiState, viewModel = viewModel)
                    CoverageSection(uiState = uiState, viewModel = viewModel)
                    WasteSection(uiState = uiState, viewModel = viewModel)
                    PurchaseSizeSection(uiState = uiState, viewModel = viewModel)
                    ResultSection(uiState = uiState)
                    ActionButtonsSection(uiState = uiState, viewModel = viewModel)
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "BRUSHBUDGET",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Estimate paint quantity from wall area and coverage.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WallDimensionsSection(
    uiState: PaintCalculatorUiState,
    unitDropdownExpanded: Boolean,
    onUnitDropdownExpandChange: (Boolean) -> Unit,
    viewModel: PaintCalculatorViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "WALL DIMENSIONS",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Common unit selector
            ExposedDropdownMenuBox(
                expanded = unitDropdownExpanded,
                onExpandedChange = onUnitDropdownExpandChange,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = "${uiState.measurementUnit.displayName} (${uiState.measurementUnit.abbreviation})",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Measurement Unit") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = unitDropdownExpanded,
                    onDismissRequest = { onUnitDropdownExpandChange(false) }
                ) {
                    MeasurementUnit.entries.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text("${unit.displayName} (${unit.abbreviation})") },
                            onClick = {
                                viewModel.onUnitChanged(unit)
                                onUnitDropdownExpandChange(false)
                            }
                        )
                    }
                }
            }

            // Length and Height in a Row (or stacked on very narrow screens)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = uiState.wallLengthInput,
                    onValueChange = { viewModel.onWallLengthChanged(it) },
                    label = { Text("Length") },
                    suffix = { Text(uiState.measurementUnit.abbreviation) },
                    isError = uiState.wallLengthError != null,
                    supportingText = uiState.wallLengthError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = "Wall length" }
                )

                OutlinedTextField(
                    value = uiState.wallHeightInput,
                    onValueChange = { viewModel.onWallHeightChanged(it) },
                    label = { Text("Height") },
                    suffix = { Text(uiState.measurementUnit.abbreviation) },
                    isError = uiState.wallHeightError != null,
                    supportingText = uiState.wallHeightError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = "Wall height" }
                )
            }

            // Gross Wall Area summary
            if (uiState.grossWallAreaM2 > 0) {
                val sqMeters = UnitConverter.formatDecimal(uiState.grossWallAreaM2)
                val sqFeet = UnitConverter.formatDecimal(
                    UnitConverter.squareMetersToSquareFeet(uiState.grossWallAreaM2)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GROSS WALL AREA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$sqMeters m²",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (uiState.appSettings.showSquareFeet) {
                            Text(
                                text = "($sqFeet ft²)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OpeningsSection(
    uiState: PaintCalculatorUiState,
    onAddOpening: (OpeningType) -> Unit,
    onRemoveOpening: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DOORS & WINDOWS",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${uiState.openings.size}/${PaintCalculatorEngine.MAX_OPENINGS}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (uiState.isOpeningLimitReached) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Warning if max limit reached
            if (uiState.isOpeningLimitReached) {
                Text(
                    text = "Maximum 10 openings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }

            // Openings list or empty placeholder
            if (uiState.openings.isEmpty()) {
                Text(
                    text = "No deductions added.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.openings.forEach { opening ->
                        val areaM2 = PaintCalculatorEngine.calculateOpeningArea(opening)
                        val formattedArea = UnitConverter.formatDecimal(areaM2)

                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (opening.type == OpeningType.DOOR) Icons.Default.DoorFront else Icons.Default.Window,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = opening.type.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${UnitConverter.formatDecimal(opening.width)} × ${UnitConverter.formatDecimal(opening.height)} ${opening.unit.abbreviation} • Qty ${opening.quantity}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$formattedArea m²",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    IconButton(
                                        onClick = { onRemoveOpening(opening.id) },
                                        modifier = Modifier.semantics { contentDescription = "Delete opening" }
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Delete opening",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Add Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onAddOpening(OpeningType.DOOR) },
                    enabled = !uiState.isOpeningLimitReached,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = "Add door" }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ADD DOOR", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = { onAddOpening(OpeningType.WINDOW) },
                    enabled = !uiState.isOpeningLimitReached,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = "Add window" }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ADD WINDOW", style = MaterialTheme.typography.labelMedium)
                }
            }

            // Summary row if openings exist
            if (uiState.openings.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "TOTAL OPENINGS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${UnitConverter.formatDecimal(uiState.openingAreaM2)} m²",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "NET PAINTABLE AREA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${UnitConverter.formatDecimal(uiState.netPaintableAreaM2)} m²",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Inline error if opening area exceeds gross wall area
            AnimatedVisibility(visible = uiState.areaExceedsError != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = uiState.areaExceedsError ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun CoatsSection(
    uiState: PaintCalculatorUiState,
    viewModel: PaintCalculatorViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "NUMBER OF COATS",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Number of coats" },
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (1..5).forEach { coatNumber ->
                    val isSelected = uiState.coats == coatNumber
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onCoatsChanged(coatNumber) },
                        label = {
                            Text(
                                text = "$coatNumber",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CoverageSection(
    uiState: PaintCalculatorUiState,
    viewModel: PaintCalculatorViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "PAINT COVERAGE",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Enter the coverage stated by your paint manufacturer.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = uiState.coverageInput,
                onValueChange = { viewModel.onCoverageChanged(it) },
                label = { Text("Coverage per liter") },
                suffix = { Text("m²/L") },
                isError = uiState.coverageError != null,
                supportingText = {
                    Text(
                        uiState.coverageError ?: "Use the coverage listed by the paint manufacturer."
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Paint coverage" }
            )

            // Quick preset chips: 8, 10, 12, 14, Custom
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(8.0, 10.0, 12.0, 14.0).forEach { preset ->
                    val isSelected = uiState.coverageInput == preset.toString()
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onCoveragePresetSelected(preset) },
                        label = { Text("${preset.toInt()} m²/L") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WasteSection(
    uiState: PaintCalculatorUiState,
    viewModel: PaintCalculatorViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "WASTE ALLOWANCE",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.semantics { contentDescription = "Waste allowance" }
            ) {
                listOf(5.0, 10.0, 15.0, 20.0).forEach { percent ->
                    val isSelected = !uiState.isCustomWaste && uiState.wastePercent == percent
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onWastePresetSelected(percent) },
                        label = { Text("${percent.toInt()}%") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                FilterChip(
                    selected = uiState.isCustomWaste,
                    onClick = { viewModel.onCustomWasteToggled() },
                    label = { Text("Custom") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            AnimatedVisibility(visible = uiState.isCustomWaste) {
                OutlinedTextField(
                    value = uiState.customWasteInput,
                    onValueChange = { viewModel.onCustomWasteChanged(it) },
                    label = { Text("Custom Waste") },
                    suffix = { Text("%") },
                    isError = uiState.wasteError != null,
                    supportingText = uiState.wasteError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text(
                text = "Allows for application waste, touch-ups, and surface variation.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PurchaseSizeSection(
    uiState: PaintCalculatorUiState,
    viewModel: PaintCalculatorViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PURCHASE SIZE (OPTIONAL)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = uiState.canSizeLiters == null && !uiState.isCustomCanSize,
                    onClick = { viewModel.onCanSizeSelected(null) },
                    label = { Text("None") }
                )

                listOf(1.0, 2.5, 5.0, 10.0, 15.0, 20.0).forEach { size ->
                    val isSelected = !uiState.isCustomCanSize && uiState.canSizeLiters == size
                    val label = if (size == 2.5) "2.5 L" else "${size.toInt()} L"
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onCanSizeSelected(size) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                FilterChip(
                    selected = uiState.isCustomCanSize,
                    onClick = { viewModel.onCustomCanSizeToggled() },
                    label = { Text("Custom") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            AnimatedVisibility(visible = uiState.isCustomCanSize) {
                OutlinedTextField(
                    value = uiState.customCanSizeInput,
                    onValueChange = { viewModel.onCustomCanSizeChanged(it) },
                    label = { Text("Custom Can Size") },
                    suffix = { Text("Liters") },
                    isError = uiState.canSizeError != null,
                    supportingText = uiState.canSizeError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ResultSection(
    uiState: PaintCalculatorUiState
) {
    val result = uiState.calculationResult
    val isReady = uiState.isInputValid && result != null && result.requiredPaintLiters > 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isReady) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header / Priority 1: Large Estimated Paint
            Text(
                text = "ESTIMATED PAINT",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (isReady) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val formattedPaint = if (isReady) UnitConverter.formatDecimal(result.requiredPaintLiters) else "0.00"
                Text(
                    text = formattedPaint,
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = if (isReady) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.semantics {
                        contentDescription = "Estimated paint $formattedPaint liters"
                    }
                )
                Text(
                    text = "Liters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isReady) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            if (isReady) {
                Text(
                    text = "Recommended purchase: ${result.recommendedPurchaseLiters} L",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            HorizontalDivider(
                color = if (isReady) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outlineVariant
            )

            // Priority 2 & 3: Breakdown Grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Net paintable area
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "NET PAINTABLE AREA",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val netArea = if (result != null) UnitConverter.formatDecimal(result.netPaintableAreaM2) else "0.00"
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$netArea m²",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.appSettings.showSquareFeet && result != null && result.netPaintableAreaM2 > 0) {
                            Text(
                                text = "(${UnitConverter.formatDecimal(UnitConverter.squareMetersToSquareFeet(result.netPaintableAreaM2))} ft²)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Coats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "COATS",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${uiState.coats}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Coverage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "COVERAGE",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${uiState.coverageInput} m²/L",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Base Paint
                if (isReady) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "BASE PAINT",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${UnitConverter.formatDecimal(result.basePaintLiters)} L",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Waste
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "WASTE",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${UnitConverter.formatDecimal(uiState.wastePercent, if (uiState.wastePercent % 1.0 == 0.0) 0 else 1)}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Priority 4: Optional Can Calculation Breakdown
            if (isReady && result.requiredCans != null && result.purchasedPaintLiters != null && result.extraPaintLiters != null && uiState.canSizeLiters != null) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )

                val canSizeLabel = if (uiState.canSizeLiters % 1.0 == 0.0) "${uiState.canSizeLiters.toInt()} L" else "${uiState.canSizeLiters} L"
                val canUnitText = if (result.requiredCans == 1) "CAN" else "CANS"

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "PURCHASE ESTIMATION ($canSizeLabel CANS)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "CANS REQUIRED", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "${result.requiredCans} $canUnitText",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "TOTAL PURCHASED", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "${UnitConverter.formatDecimal(result.purchasedPaintLiters)} L",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "EXTRA PAINT", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "${UnitConverter.formatDecimal(result.extraPaintLiters)} L",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButtonsSection(
    uiState: PaintCalculatorUiState,
    viewModel: PaintCalculatorViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // CALCULATE Primary Button
        Button(
            onClick = { viewModel.calculateExplicit() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .semantics { contentDescription = "Calculate paint" },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Calculate, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CALCULATE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // SAVE CALCULATION Button
        Button(
            onClick = { viewModel.saveCurrentCalculation() },
            enabled = uiState.isInputValid && (uiState.calculationResult?.requiredPaintLiters ?: 0.0) > 0,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .semantics { contentDescription = "Save calculation" },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(Icons.Default.BookmarkBorder, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SAVE CALCULATION",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // RESET Button
        OutlinedButton(
            onClick = { viewModel.resetToDefaults() },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .semantics { contentDescription = "Reset" }
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("RESET")
        }
    }
}
