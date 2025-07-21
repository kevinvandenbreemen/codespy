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
import com.vandenbreemen.codespy.ui.SelectTypeDialog
import com.vandenbreemen.codespy.ui.SelectTypeWithLevelsDialog
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
    val showTypeAndImmediateTypesDialog = remember { mutableStateOf(false) }
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
                            "Show Type",
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .clickable {
                                    showTypeDialog.value = true
                                    scope.launch { drawerState.close() }
                                }
                        )

                        //  Show dialog for selecting a type along with its immediate types
                        Text(
                            "Select Type Local System to View",
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .clickable {
                                    showTypeAndImmediateTypesDialog.value = true
                                    scope.launch { drawerState.close() }
                                }
                        )

                        // Show "Back to Parent Model" button only if there's a parent model available
                        if (viewModel.hasParentModel()) {
                            Text(
                                "Back to Parent Model",
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .clickable {
                                        viewModel.navigateToParentModel()
                                        scope.launch { drawerState.close() }
                                    }
                            )
                        }
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
                                    initialDirectory = viewModel.getLastParentDirectory()
                                        ?: File(System.getProperty("user.home")),
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
                            viewModel.renderingViewModelState.value?.let {
                                ModelRendering(modelRenderingViewModel = it)
                            } ?: run {

                                //  Check view model for spinner state.  If present show spinner
                                if (viewModel.isLoadingState.value) {
                                    CircularProgressIndicator()
                                } else {
                                    Text("No model rendering available. Please select a directory with a valid model.")
                                }
                            }

                            val model = viewModel.modelState.value
                            if (showTypeDialog.value && model != null) {
                                SelectTypeDialog(
                                    viewModel = object : SelectTypeDialogViewModel(model) {
                                        override fun onTypeSelected(type: Type) {

                                            viewModel.onUserSelectedType(type)

                                            showTypeDialog.value = false
                                        }
                                    },
                                    onDismiss = { showTypeDialog.value = false }
                                )
                            }

                            if (showTypeAndImmediateTypesDialog.value && model != null) {
                                SelectTypeWithLevelsDialog(
                                    viewModel = object : SelectTypeDialogViewModel(model) {
                                        override fun onTypeSelected(type: Type) {
                                            // This won't be used in the new dialog, but we need to implement it
                                        }
                                    },
                                    onTypeAndLevelsSelected = { type, levels ->
                                        viewModel.onUserSelectedTypeWithLevels(type, levels)
                                        showTypeAndImmediateTypesDialog.value = false
                                    },
                                    onDismiss = { showTypeAndImmediateTypesDialog.value = false }
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