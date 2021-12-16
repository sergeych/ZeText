@file:OptIn(ExperimentalMaterialApi::class)

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.awt.FileDialog


@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
fun ZeEditor(window: ComposeWindow, state: AppState) {
    val fr = FocusRequester()
    val ss = rememberScrollState(0)
    val coroutineScope = rememberCoroutineScope()
    var lastEsc by remember { mutableStateOf(false) }

    if (state.requestClose) {
        if (state.textChanged) {
            AlertDialog(
                {},
                modifier = Modifier.width(300.dp),
                title = { Text("Please confirm:") },
                text = {
                    Text("You are goin to drop any changes. Are you sure?")
                },
                buttons = {
                    Row(
                        modifier = Modifier.padding(all = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = {
                                state.requestClose = false
                                state.clear()
                            }
                        ) {
                            Text("Drop changes")
                        }
                        SecondaryButton(
                            onClick = {
                                state.requestClose = false
                            },
                        ) {
                            Text("Cancel")
                        }
                        Spacer(Modifier.weight(1f))
                    }
                }
            )
        } else
            state.clear()
    } else {
        Column(
            modifier = Modifier.fillMaxWidth().fillMaxHeight().background(Color.White).onKeyEvent { ke ->
                if( ke.type == KeyEventType.KeyDown ) {
                    when(ke.key) {
                        Key.Escape -> {
                            if (lastEsc) {
                                state.requestClose = true
                                lastEsc = false
                            } else {
                                lastEsc = true
                            }
                            true
                        }
                        else -> {
                            lastEsc = false
                            false
                        }
                    }
                }
                else false
            },
        ) {
            Box(Modifier.weight(1f).padding(4.dp).fillMaxWidth()) {
                BasicTextField(
                    state.text, state::text::set,
                    Modifier.fillMaxSize()
//                        .verticalScroll(ss)
                        .padding(0.dp)
                        .focusRequester(fr),
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 1.em)
                )
                VerticalScrollbar(
                    rememberScrollbarAdapter(ss), Modifier.align(Alignment.CenterEnd),
                    style = defaultScrollbarStyle().copy(hoverDurationMillis = 700)
                )
            }
            DisposableEffect(Unit) {
                fr.requestFocus()
                onDispose {  }
            }
        }
    }
    if (state.requestSave) {
        NewPasswordDialog(state) { ok ->
            state.requestSave = false
            if (ok) {
                if (state.path == null) {
                    val d = java.awt.FileDialog(window, "Select file save")
                    d.mode = FileDialog.SAVE
                    d.isVisible = true
                    state.path = "${d.directory}${d.file}"
                    println("${state.path}")
                }
                coroutineScope.launch(Dispatchers.IO) {
                    state.save()
                }
            }
        }
    }
}