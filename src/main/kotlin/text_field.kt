import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun InputText(
    text: MutableState<String>, label: String = "", placeholder: String? = null, isPassword: Boolean = false,
    modifier: Modifier = Modifier.fillMaxWidth(), autoFocus: Boolean = false, enabled: Boolean = true,
    validate: ((String) -> Boolean)? = null
) {
    var showPassword by remember { mutableStateOf(isPassword) }
    val placeholder: (@Composable () -> Unit)? = placeholder?.let { { Text(it) } }
    val eyeIcon: (@Composable () -> Unit)? =
        if (isPassword) {
            {
                IconButton({ showPassword = !showPassword }, Modifier.focusProperties { canFocus = false }) {
                    Icon(painter = painterResource("eye.svg"), "")
                }
            }
        } else null
    val fr = FocusRequester()
    OutlinedTextField(
        text.value, text::value::set,
        singleLine = true,
        label = { Text(label) },
        trailingIcon = eyeIcon,
        placeholder = placeholder,
        isError = validate?.invoke(text.value)?.let{!it} ?: false,
        visualTransformation = if (showPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = modifier.composed { focusRequester(fr) },
        enabled = enabled
    )
    if (autoFocus)
        SideEffect { fr.requestFocus() }
}