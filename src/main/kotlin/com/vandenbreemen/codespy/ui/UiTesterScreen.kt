package com.vandenbreemen.codespy.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.SelectTypeDialogViewModel
import com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.TypeLayoutLogicViewModel
import com.vandenbreemen.grucd.model.Model
import com.vandenbreemen.grucd.model.RelationType
import com.vandenbreemen.grucd.model.Type
import com.vandenbreemen.grucd.model.TypeRelation

@Composable
fun UiTesterScreen(onBack: () -> Unit) {
    // Create a fake model with some types
    val fakeModel = remember {
        val fakeTypes = listOf(
            Type("User", "com.example"),
            Type("Order", "com.example"),
            Type("Product", "com.shop"),
            Type("Inventory", "com.shop"),
            Type("Customer", "com.example"),
            Type("Supplier", "com.shop"),
            Type("Category", "com.shop"),
            Type("Review", "com.example"),
            Type("Shipment", "com.shop"),
            Type("Payment", "com.example")
        )
        Model(fakeTypes).apply {
            this.addRelation(
                TypeRelation(
                    fakeTypes[0], fakeTypes[1], RelationType.subclass
                )
            )
            this.addRelation(
                TypeRelation(
                    fakeTypes[1], fakeTypes[2], RelationType.encapsulates
                )
            )
        }
    }

    var showTypeDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = onBack, modifier = Modifier.padding(bottom = 16.dp)) {
            Text("Back")
        }
        Text("ModelRendering showcase:", modifier = Modifier.padding(bottom = 8.dp))
        ModelRendering(TypeLayoutLogicViewModel(fakeModel))
        Button(onClick = { showTypeDialog = true }, modifier = Modifier.padding(vertical = 16.dp)) {
            Text("Show SelectTypeDialog")
        }
        if (showTypeDialog) {
            SelectTypeDialog(
                viewModel = object : SelectTypeDialogViewModel(fakeModel) {
                    override fun onTypeSelected(type: Type) {
                        // Handle type selection
                        println("Selected type: ${type.name} from package ${type.pkg}")
                        showTypeDialog = false
                    }
                },
                onDismiss = { showTypeDialog = false }
            )
        }
    }
}
