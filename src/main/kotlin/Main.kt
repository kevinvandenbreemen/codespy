package com.vandenbreemen

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.vandenbreemen.codespy.ui.FileSelectDialog
import com.vandenbreemen.codespy.ui.ModelRendering
import com.vandenbreemen.com.vandenbreemen.codespy.di.Dependencies
import com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.SelectTypeDialogViewModel
import com.vandenbreemen.com.vandenbreemen.codespy.viewmodel.CodeSpyViewModel
import com.vandenbreemen.grucd.model.Type
import kotlinx.coroutines.launch
import java.io.File


@Composable
@Preview
fun App() {

    //  Set up the view model here
    val viewModel: CodeSpyViewModel = Dependencies.main.codeSpyViewModel()

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val showFileDialog = remember { mutableStateOf(false) }
    val selectedFile = remember { mutableStateOf<File?>(null) }
    val showTypeDialog = remember { mutableStateOf(false) }
    val showUiTester = remember { mutableStateOf(false) }

    MaterialTheme {
        ModalDrawer(
            drawerState = drawerState,
            drawerContent = {
                Column {
                    Text(
                        "UI tester",
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .clickable {
                                showUiTester.value = true
                                scope.launch { drawerState.close() }
                            }
                    )
                    val model = viewModel.modelState.value
                    if (model != null) {
                        Text(
                            "Select type to view",
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .clickable {
                                    showTypeDialog.value = true
                                    scope.launch { drawerState.close() }
                                }
                        )
                    }
                }
            }
        ) {
            if (showUiTester.value) {
                com.vandenbreemen.codespy.ui.UiTesterScreen(onBack = { showUiTester.value = false })
            } else {
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
                            LaunchedEffect(selectedFile.value) {
                                selectedFile.value?.let {
                                    viewModel.selectNewDirectory(it)
                            }
                            }
                            // Show the message from the view model
                            val message = viewModel.directoryMessage.value
                            if (message.isNotEmpty()) {
                                Text(message)
                            }
                            // Render the model if available
                            val model = viewModel.modelState.value
                            ModelRendering(model)
                            if (showTypeDialog.value && model != null) {
                                com.vandenbreemen.codespy.ui.SelectTypeDialog(
                                    viewModel = object : SelectTypeDialogViewModel(model) {
                                        override fun onTypeSelected(type: Type) {
                                            // XXX  Handle type selection here
                                            showTypeDialog.value = false
                                        }
                                    },
                                    onDismiss = { showTypeDialog.value = false }
                                )
                            }
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