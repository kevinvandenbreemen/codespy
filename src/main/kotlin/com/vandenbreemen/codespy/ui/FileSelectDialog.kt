package com.vandenbreemen.codespy.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun FileSelectDialog(
    directory: File,
    onFileSelected: (File) -> Unit,
    onDismiss: () -> Unit
) {
    val files = directory.listFiles()?.filter { it.isFile } ?: emptyList()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select a file") },
        text = {
            Column {
                files.forEach { file ->
                    Text(
                        file.name,
                        modifier = Modifier
                            .clickable { onFileSelected(file) }
                            .padding(4.dp)
                    )
                }
            }
        },
        buttons = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

