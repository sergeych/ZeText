import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Composable
fun H1(text: String) {
    Text(text, fontSize = 1.4.em, modifier = Modifier.paddingFromBaseline(6.dp,4.dp),
        color = MaterialTheme.colors.onSurface)
}