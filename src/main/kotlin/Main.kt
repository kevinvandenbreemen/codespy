package com.vandenbreemen

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.DrawerValue
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.ModalDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.io.File
import kotlinx.coroutines.launch
import com.vandenbreemen.codespy.ui.FileSelectDialog
import com.vandenbreemen.com.vandenbreemen.codespy.di.Dependencies


@Composable
@Preview
fun App() {

    //  Set up the view model here
    val viewModel = Dependencies.main.codeSpyViewModel()

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val showFileDialog = remember { mutableStateOf(false) }
    val selectedFile = remember { mutableStateOf<File?>(null) }

    MaterialTheme {
        ModalDrawer(
            drawerState = drawerState,
            drawerContent = {
                Column {
                    Text("Menu Item 1")
                    Text("Menu Item 2")
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("My App Title") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                androidx.compose.material.Icon(Icons.Filled.Menu, contentDescription = "Menu")
                            }
                        }
                    )
                }
            ) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Column {
                        Button(onClick = { showFileDialog.value = true }) {
                            Text("Select File")
                        }
                        selectedFile.value?.let {
                            Text("Selected: ${it.name}")
                        }
                        if (showFileDialog.value) {
                            FileSelectDialog(
                                initialDirectory = File(System.getProperty("user.home")),
                                onDirectorySelected = {
                                    selectedFile.value = it
                                    showFileDialog.value = false
                                },
                                onDismiss = { showFileDialog.value = false },
                                showOnlyDirectories = true
                            )
                        }
                    }
                }
            }
        }

    }

}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "CodeSpy",
        state = rememberWindowState(width = 1000.dp, height = 800.dp)
    ) {
        App()
    }
}