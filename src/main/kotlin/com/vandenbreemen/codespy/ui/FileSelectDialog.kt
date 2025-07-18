package com.vandenbreemen.codespy.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox

@Composable
fun FileSelectDialog(
    directory: File,
    onFileSelected: (File) -> Unit,
    onDismiss: () -> Unit,
    showOnlyDirectories: Boolean = false
) {
    val files = directory.listFiles()?.toList()?.filter {
        if (showOnlyDirectories) it.isDirectory else true
    } ?: emptyList()
    val columns = 4 // Number of columns per row
    val rows = if (files.isNotEmpty()) (files.size + columns - 1) / columns else 0
    // Prepare grid data: each column is a list of files
    val gridColumns = List(columns) { col ->
        List(rows) { row ->
            val index = row * columns + col
            if (index < files.size) files[index] else null
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select a file") },
        text = {
            LazyRow {
                items(gridColumns) { columnFiles ->
                    Column {
                        columnFiles.forEach { file ->
                            if (file != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (file.isDirectory) {
                                        Icon(Icons.Filled.AccountBox, contentDescription = "Folder", modifier = Modifier.size(20.dp))
                                    }
                                    Text(
                                        file.name,
                                        modifier = Modifier
                                            .clickable { onFileSelected(file) }
                                            .padding(4.dp)
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.padding(4.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }
        },
        buttons = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
