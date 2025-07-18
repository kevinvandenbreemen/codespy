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
import com.vandenbreemen.grucd.model.Model

@Composable
fun SelectTypeDialog(
    model: Model,
    onDismiss: () -> Unit
) {
    // Try to extract types from model. Adjust this if your model structure is different.
    val allTypes: List<String> = remember(model) {
        // Example: If model has a 'types' property that's a list of objects with 'name' and 'packageName'
        val typesList = try {
            val prop = model::class.members.find { it.name == "types" }
            prop?.call(model) as? List<Any> ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        typesList.map {
            val name = it::class.members.find { m -> m.name == "name" }?.call(it) as? String ?: "Unknown"
            val pkg = it::class.members.find { m -> m.name == "packageName" }?.call(it) as? String ?: ""
            if (pkg.isNotEmpty()) "$pkg.$name" else name
        }
    }
    var query by remember { mutableStateOf("") }
    val filteredTypes = remember(query, allTypes) {
        allTypes.filter { typeName -> typeName.contains(query, ignoreCase = true) }
    }

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
