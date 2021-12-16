import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ButtonBar(block: @Composable () -> Unit) {
    Box(Modifier.background(MaterialTheme.colors.primary.copy(0.2f))) {
        Box(Modifier.padding(4.dp)) {
            Row(
                Modifier.height(30.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                block()
            }
        }
    }
}