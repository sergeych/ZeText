@file:OptIn(ExperimentalComposeUiApi::class)

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sun.jna.Platform
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.sergeych.common.Centered
import net.sergeych.compose.FileDialog
import net.sergeych.zetext.ZeText
import java.awt.Desktop
import java.io.File


@Composable
fun rememberAppState(): AppState = remember { AppState() }


@Preview
@Composable
fun App2() {
    val show by remember { mutableStateOf(true) }
//    if (show) NewPasswordDialog(rememberAppState(), { res ->
//        show = false
//        println("Result: $res")
//    })
    if (show) PasswordDialog{
        println("->> $it")
        delay(1000)
        println("<<- releasing")
        "some error"
    }
}

val menuKey = if (Platform.isMac()) "⌘"
else if (Platform.isWindows()) "Alt"
else if (Platform.isLinux()) "Ctrl"
else "??"

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FrameWindowScope.App(window: ComposeWindow, file: File?, decryptMode: Boolean) {
    var startupFileName by remember { mutableStateOf(file) }
    val state = rememberAppState()
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    var loadFileName by remember { mutableStateOf<String?>(null) }
    var showOpenPasswordDialog by remember { mutableStateOf(false) }
//    var password by remember { mutableStateOf("") }

    Desktop.getDesktop()?.let {
        it.setOpenFileHandler({
            it.files.firstOrNull()?.let {
                if( state.canOpen )
                    state.openFile(it)
            }
        })
    }

    WindowMenuBar(state)
    MaterialTheme {
        Scaffold(
            scaffoldState = scaffoldState,
        ) {
            if (state.isEmpty) {
                window.title = "ZeText"
                Centered(Modifier.padding(it)) {
                    Text(
                        """
                            New: $menuKey + N
                            
                            Open: $menuKey + O
                            
                            Close: ESC ESC
                            
                            see application menu for more
                        """.trimIndent(),
                        color = MaterialTheme.colors.onSurface.copy(0.5f)
                    )
                }
            } else
                ZeEditor(window, state)
        }
    }
    if (state.requestOpen) {
        if (state.path != null) {
            loadFileName = state.path
            showOpenPasswordDialog = true
        } else {
            FileDialog("Open ztext:", true, ".ztext") { path ->
                state.requestOpen = false
                if (path != null) {
                    loadFileName = path.toString()
                    showOpenPasswordDialog = true
                }
            }
        }
    }
    if (state.busyMessage != null) {
        AlertDialog(
            {},
            modifier = Modifier.fillMaxWidth(0.34f),
            text = {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    H1(state.busyMessage ?: "")
                    CircularProgressIndicator(Modifier.paddingFromBaseline(18.dp, 2.dp))
                }
            },
            buttons = {}
        )
    }
    if (state.errorMessage != null) {
        AlertDialog(
            {},
            modifier = Modifier.fillMaxWidth(0.75f),
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.Warning, "", Modifier.size(66.dp).padding(8.dp), Color.Red)
                    Column(Modifier.fillMaxWidth()) {
                        H1("Error:")
                        Text(state.errorMessage ?: "", modifier = Modifier.paddingFromBaseline(18.dp))
                        Spacer(Modifier.height(12.dp))
                        Button({ state.errorMessage = null }) { Text("OK") }
                    }
                }
            },
            buttons = {
            }
        )
    }
    if (showOpenPasswordDialog) {
        PasswordDialog { psw ->
            if (psw == null) {
                showOpenPasswordDialog = false
                state.requestOpen = false
                if (decryptMode)
                    System.exit(100)
                null
            } else {
                val result = state.loadFile(loadFileName!!, psw)
                when (result) {
                    ZeText.Result.BadPassword -> {
                        println("reporing error")
                        "Invalid password"
                    }
                    else -> {
                        showOpenPasswordDialog = false
                        state.requestOpen = false
                        if (decryptMode) {
                            if (result is ZeText.Result.Success) {
                                println(state.text)
                                System.exit(0)
                            } else {
                                System.exit(101)
                            }
                        }
                        null
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        if (startupFileName != null) {
            state.openFile(startupFileName!!)
            startupFileName = null
        }
//    DisposableEffect(Unit) {
        scope.launch {
            state.events.collect {
                scaffoldState.snackbarHostState.showSnackbar(it)
            }
        }
//        onDispose { }
    }
}

fun startUIApp(file: File? = null, decryptMode: Boolean = false) = application {
    Window(onCloseRequest = ::exitApplication) {
        App(window, file, decryptMode)
//        App2()
//        LayoutDialogErrorDemo()
    }
}

fun main(args: Array<String>) {
//    val s = Argv0.source(ZtCli::class.java)
//    println("Program: $s")
//    ZtCli().main(arrayOf("-d", "/Users/sergeych/dev/first_1.ztext"))
    ZtCli().main(args)
}
