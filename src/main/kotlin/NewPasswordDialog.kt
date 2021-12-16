@file:OptIn(ExperimentalMaterialApi::class)

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NewPasswordDialog(state: AppState, onClose: (Boolean) -> Unit) {
    if( state.needsPassword ) {
        var password1 = mutableStateOf(state.password ?: "")
        var password2 = mutableStateOf(state.password ?: "")

        fun checkPassword1(): Boolean {
            val value = password1.value
            return value.trim() == value && value.length >= 5
        }

        fun checkPassword2(): Boolean {
            return password1.value == password2.value
        }

        AlertDialog(
            {},
            modifier = Modifier.fillMaxWidth(0.75f).onKeyEvent {
                when (it.key) {
                    Key.Escape -> {
                        onClose(false)
                        true
                    }
                    Key.Enter -> {
                        if (checkPassword1() && checkPassword2()) {
                            state.password = password1.value
                            onClose(true)
                        }
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
                            state.password = password1.value
                            onClose(true)
                        },
                        enabled = checkPassword1() && checkPassword2()
                    ) {
                        Text("Encrypt")
                    }
                    Button(
                        onClick = { onClose(false) },
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary)
                    ) {
                        Text("Cancel")
                    }
//                Spacer(Modifier.weight(1f))
                }
            }, text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    H1("Encrypting ZeText:")
//                    Spacer(Modifier.padding(2.dp))
                    InputText(
                        password1,
                        "enter new password",
                        isPassword = true,
                        autoFocus = true,
                        validate = { checkPassword1() }
                    )
                    InputText(password2, "check password", isPassword = true,
                        validate = { checkPassword2() }
                    )
                }
            }
        )
    }
    else {
        println("password already set, re-using it")
        onClose(true)
    }
}