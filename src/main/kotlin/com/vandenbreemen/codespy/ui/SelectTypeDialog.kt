package com.vandenbreemen.codespy.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.AlertDialog
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.SelectTypeDialogViewModel

@Composable
fun SelectTypeDialog(
    viewModel: SelectTypeDialogViewModel,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filteredTypes by viewModel.visibleTypes

    LaunchedEffect(query) {
        viewModel.onUserInputChange(query)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Type to View") },
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
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        .height(250.dp)
                        .scrollable(
                            state = verticalScrollState,
                            orientation = Orientation.Vertical
                        )
                ) {
                    items(filteredTypes.size) { idx ->
                        val type = filteredTypes[idx]
                        val displayText = "${type.pkg}.${type.name}"
                        Text(
                            displayText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* Handle type selection here */ }
                                .padding(8.dp)
                        )
                    }
                }
            }
        },
        buttons = {}
    )
}
