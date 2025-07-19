package com.vandenbreemen.codespy.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
    // Try to extract types from model. Adjust this if your model structure is different.


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Type to View") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Type name") },
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    items(filteredTypes.size) { idx ->
                        val typeName = filteredTypes[idx]
                        Text(
                            typeName,
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
