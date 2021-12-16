@file:OptIn(ExperimentalMaterialApi::class)

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PasswordDialog(password: String, check: suspend (String?) -> String?) {
    var text = mutableStateOf(password)
    var errorString: String? by remember { mutableStateOf(null) }
    var scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }

    fun done(value: String?) {
        scope.launch {
            busy = true
            errorString = check(value)
            busy = false
        }
    }

    AlertDialog(
        {},
//            { onClose(false) },
//            title = {
//                H1("Encrypting ZeText:")
//            },
        modifier = Modifier.fillMaxWidth(0.75f).onKeyEvent {
            when (it.key) {
                Key.Escape -> {
                    done(null)
                    true
                }
                Key.Enter -> {
                    done(text.value)
                    true
                }
                else -> false
            }
        },
        buttons = {
            Row(
                modifier = Modifier.padding(all = 8.dp).padding(end = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        done(text.value)
                    },
                    enabled = !busy && text.value != ""
                ) {
                    if( busy ) CircularProgressIndicator(Modifier.size(20.dp))
                    else Text("OK")
                }
                SecondaryButton(
                    onClick = { done(null) },
                    enabled = !busy
                ) {
                    Text("Cancel")
                }
            }
        }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                H1("Decrypting ZeText:")
//                    Spacer(Modifier.padding(2.dp))
                InputText(
                    text,
                    "enter password",
                    isPassword = true,
                    autoFocus = true,
                    validate = { errorString == null},
                    enabled = !busy
                )
                if( errorString != null )
                    Text(errorString!!, style = MaterialTheme.typography.subtitle2, color=Color.Red)
            }
        }
    )
}