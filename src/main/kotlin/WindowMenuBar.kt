import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import com.sun.jna.Platform
import java.io.File

fun HotKey(code: Key) =
    KeyShortcut(
        key = code, ctrl = Platform.isLinux(), alt=Platform.isWindows(), meta = Platform.isMac()
    )


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FrameWindowScope.WindowMenuBar(state: AppState) = MenuBar {
    Menu("File") {
        Item(
            "New", onClick = { state.newFile() }, enabled = state.canNew, shortcut = HotKey(Key.N)
            )
        Separator()
        Item(
            "Open...", onClick = { state.requestOpen = true }, enabled = state.canOpen, shortcut = HotKey(Key.O)
            )
        Item(
            "Save", onClick = { state.requestSave = true }, enabled = state.canSave, shortcut = HotKey(Key.S)
            )
        Item(
            "Close", onClick = { state.requestClose = true }, enabled = state.canClose
            )
        Separator()
        Item("Exit", onClick = { System.exit(0) }, enabled = !state.canClose, shortcut = HotKey(Key.Q))
    }
    Menu("Tools") {
        Item(
            "Install zetext shell script",
            onClick = {
                state.busyMessage = "Installing shell script"
                try {
                    val optCl = ProcessHandle.current().info().commandLine()
                    if (optCl.isEmpty) {
                        state.errorMessage = "Failed to get process handle, can't install script"
                    } else {
                        val cl = optCl.get()
                        var success = false
                        for (x in arrayOf("/usr/local/bin", "${System.getenv()["HOME"]}/bin")) {
                            println("trying to create in $x")
                            val f = File(x)
                            if (f.exists() && f.isDirectory) {
                                println("Found directory $f")
                                try {
                                    if (f.canWrite()) {
                                        println("even can write to it")
                                        val script = File(f, "zetext")
                                        script.writeText(
                                            """
                                        #!/bin/bash
                                        
                                        ${cl} $*
                                        
                                    """.trimIndent()
                                        )
                                        script.setExecutable(true)
                                        state.showSnackSync("created script: $script")
                                        success = true
                                        break
                                    }
                                } catch (x: Exception) {
                                    println("Failed to create script: $x")
                                }
                            } else println("can't write to $x, skipping")
                        }
                        if (!success)
                            state.errorMessage = "Failed to create zetext script"
                    }
                } finally {
                    state.busyMessage = null
                }
            }
        )
    }
}