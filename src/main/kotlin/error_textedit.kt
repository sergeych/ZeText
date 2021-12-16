import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LayoutDialogErrorDemo() {
    var t1: String by remember { mutableStateOf("") }
    var t2: String by remember { mutableStateOf("") }
    AlertDialog(
        {},
        title = { Text("The Heading",fontSize = 1.4.em, modifier = Modifier.paddingFromBaseline(6.dp,4.dp)) },
        buttons = { Text("No buttons") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(t1, { t1 = it }, label = { Text("Field1") })
                OutlinedTextField(t2, { t2 = it }, label = { Text("Field2") })
            }
        }
    )
}
