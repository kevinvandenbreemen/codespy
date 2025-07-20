package com.vandenbreemen.codespy.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.SelectTypeDialogViewModel
import com.vandenbreemen.grucd.model.Type

@Composable
fun SelectTypeWithLevelsDialog(
    viewModel: SelectTypeDialogViewModel,
    onTypeAndLevelsSelected: (Type, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<Type?>(null) }
    var levels by remember { mutableStateOf("3") }
    val filteredTypes by viewModel.visibleTypes

    LaunchedEffect(query) {
        viewModel.onUserInputChange(query)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Type and Levels") },
        text = {
            Column {
                val verticalScrollState = rememberScrollState()

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Type name") },
                    modifier = Modifier.fillMaxWidth()
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(200.dp)
                        .scrollable(
                            state = verticalScrollState,
                            orientation = Orientation.Vertical
                        )
                ) {
                    items(filteredTypes.size) { idx ->
                        val type = filteredTypes[idx]
                        val isSelected = selectedType == type
                        val displayText = buildAnnotatedString {
                            append("${type.pkg}.")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(type.name)
                            }
                        }
                        Text(
                            displayText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedType = type
                                }
                                .padding(8.dp),
                            color = if (isSelected) androidx.compose.material.MaterialTheme.colors.primary
                            else androidx.compose.material.MaterialTheme.colors.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = levels,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            levels = newValue
                        }
                    },
                    label = { Text("Number of levels") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedType?.let { type ->
                        val numLevels = levels.toIntOrNull() ?: 3
                        onTypeAndLevelsSelected(type, numLevels)
                    }
                },
                enabled = selectedType != null && levels.isNotEmpty()
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
