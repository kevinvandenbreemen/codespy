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
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox

@Composable
fun FileSelectDialog(
    initialDirectory: File,
    onDirectorySelected: (File) -> Unit,
    onDismiss: () -> Unit,
    showOnlyDirectories: Boolean = false
) {
    val currentDirectory = remember { mutableStateOf(initialDirectory) }
    val files = currentDirectory.value.listFiles()?.toList() ?: emptyList()
    val filteredFiles = files.filter { if (showOnlyDirectories) it.isDirectory else true }
    val columns = 4
    val rows = if (filteredFiles.isNotEmpty()) (filteredFiles.size + columns - 1) / columns else 0
    val gridColumns = List(columns) { col ->
        List(rows) { row ->
            val index = row * columns + col
            if (index < filteredFiles.size) filteredFiles[index] else null
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select a directory") },
        text = {
            Box(modifier = Modifier.size(width = 500.dp, height = 300.dp)) {
                Column {
                    if (currentDirectory.value.parentFile != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AccountBox, contentDescription = "Folder", modifier = Modifier.size(20.dp))
                            Text("..", modifier = Modifier.clickable {
                                currentDirectory.value = currentDirectory.value.parentFile!!
                            }.padding(4.dp))
                        }
                    }
                    LazyRow {
                        items(gridColumns) { columnFiles ->
                            Column {
                                columnFiles.forEach { file ->
                                    if (file != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (file.isDirectory) {
                                                Icon(Icons.Filled.AccountBox, contentDescription = "Folder", modifier = Modifier.size(20.dp))
                                                Text(
                                                    file.name,
                                                    modifier = Modifier.clickable {
                                                        currentDirectory.value = file
                                                    }.padding(4.dp)
                                                )
                                            } else {
                                                Text(
                                                    file.name,
                                                    modifier = Modifier.padding(4.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.padding(4.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }
                }
            }
        },
        buttons = {
            Row {
                Button(onClick = onDismiss) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onDirectorySelected(currentDirectory.value) }) {
                    Text("Select this directory")
                }
            }
        }
    )
}
