package com.example.paintcalculator.ui.screens.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.paintcalculator.model.MeasurementUnit
import com.example.paintcalculator.model.OpeningType
import com.example.paintcalculator.model.PaintOpening

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpeningDialog(
    type: OpeningType,
    initialUnit: MeasurementUnit,
    onDismiss: () -> Unit,
    onConfirm: (PaintOpening) -> Unit
) {
    // Default reasonable dimensions based on type
    var widthText by remember {
        mutableStateOf(if (type == OpeningType.DOOR) "0.90" else "1.50")
    }
    var heightText by remember {
        mutableStateOf(if (type == OpeningType.DOOR) "2.10" else "1.20")
    }
    var quantity by remember { mutableIntStateOf(1) }
    var selectedUnit by remember { mutableStateOf(initialUnit) }
    var unitDropdownExpanded by remember { mutableStateOf(false) }

    var widthError by remember { mutableStateOf<String?>(null) }
    var heightError by remember { mutableStateOf<String?>(null) }

    fun validateAndSubmit() {
        val width = widthText.toDoubleOrNull()
        val height = heightText.toDoubleOrNull()

        var hasError = false
        if (width == null || width <= 0) {
            widthError = "Width must be > 0"
            hasError = true
        } else {
            widthError = null
        }

        if (height == null || height <= 0) {
            heightError = "Height must be > 0"
            hasError = true
        } else {
            heightError = null
        }

        if (!hasError && width != null && height != null) {
            onConfirm(
                PaintOpening(
                    id = System.currentTimeMillis(),
                    type = type,
                    width = width,
                    height = height,
                    quantity = quantity,
                    unit = selectedUnit
                )
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add ${type.displayName}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Width input
                OutlinedTextField(
                    value = widthText,
                    onValueChange = {
                        widthText = it
                        widthError = null
                    },
                    label = { Text("Width") },
                    suffix = { Text(selectedUnit.abbreviation) },
                    isError = widthError != null,
                    supportingText = widthError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                // Height input
                OutlinedTextField(
                    value = heightText,
                    onValueChange = {
                        heightText = it
                        heightError = null
                    },
                    label = { Text("Height") },
                    suffix = { Text(selectedUnit.abbreviation) },
                    isError = heightError != null,
                    supportingText = heightError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                // Unit Selector
                ExposedDropdownMenuBox(
                    expanded = unitDropdownExpanded,
                    onExpandedChange = { unitDropdownExpanded = !unitDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = "${selectedUnit.displayName} (${selectedUnit.abbreviation})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = unitDropdownExpanded,
                        onDismissRequest = { unitDropdownExpanded = false }
                    ) {
                        MeasurementUnit.entries.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text("${unit.displayName} (${unit.abbreviation})") },
                                onClick = {
                                    selectedUnit = unit
                                    unitDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Quantity selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quantity",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            enabled = quantity > 1
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease quantity")
                        }
                        Text(
                            text = quantity.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { if (quantity < 10) quantity++ },
                            enabled = quantity < 10
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase quantity")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { validateAndSubmit() }) {
                Text("ADD", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}
