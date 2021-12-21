import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import net.sergeych.zetext.ZeText
import java.io.File
import java.nio.file.Path

class AppState {
    private var zeText: ZeText? = null

    fun newFile() {
        text = ""
        lastText = ""
        isEmpty = false
        zeText = null
        password = null
    }

    suspend fun loadFile(filePath: String, password: String): ZeText.Result =
        ZeText.loadFrom(filePath, password).also { r ->
            when (r) {
                is ZeText.Result.Success -> {
                    zeText = r.value
                    text = zeText!!.text
                    lastText = text
                    path = filePath
                    isEmpty = false
                }
                is ZeText.Result.InvalidData -> errorMessage = "Invalid file: ${r.reason}"
            }
        }

    val needsPassword: Boolean
        get() = zeText == null && password == null

    fun clear() {
        text = ""
        lastText = ""
        password = null
        zeText = null
        path = null
        isEmpty = true
        requestClose = false
    }

    private val _notifications = MutableSharedFlow<String>() // private mutable shared flow
    val events = _notifications.asSharedFlow() // publicly exposed as read-only shared flow

    var busyMessage by mutableStateOf<String?>(null)
    var errorMessage by mutableStateOf<String?>(null)

    @Suppress("unused")
    suspend fun showSnack(text: String) {
        _notifications.emit(text)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun showSnackSync(text: String) {
        GlobalScope.launch { _notifications.emit(text) }
    }

    suspend fun save() {
        try {
            busyMessage = "Saving..."
            path?.let { name ->
                val fullName = if (name.lowercase().endsWith(".ztext")) name else "$name.ztext"
                if (zeText == null) {
                    zeText = ZeText.create(text, password ?: throw Exception("password is not set"))
                } else {
                    zeText!!.text = text
                }
                zeText!!.saveToFile(fullName)
                path = fullName
                lastText = text
                _notifications.emit("$path is saved")
            }
                ?: throw Exception("path must be set for save()")
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = "Failed to save file: $e"
        } finally {
            busyMessage = null
        }
    }

    fun openFile(file: File) {
        if( !canSave ) {
            zeText = null
            path = file.absolutePath
            requestOpen = true
        }
        else {
            // Error: openFile should not be called when there is modified opened file")
        }
    }

    var password: String? = null

    val textChanged: Boolean
        get() {
            return text != lastText
        }

    var requestSave by mutableStateOf(false)
    var requestOpen by mutableStateOf(false)
    var requestClose by mutableStateOf(false)

    val canSave: Boolean
        get() = !isEmpty && textChanged && !requestSave

    val canOpen: Boolean
        get() = !requestOpen && !canSave

    val canNew: Boolean
        get() = !canSave

    val canClose: Boolean get() = !isEmpty

    var path: String? = null
    val fileName: String?
        get() = path?.let { Path.of(it).fileName.toString() }
    var lastText: String = ""
    var text by mutableStateOf("")
    var isEmpty: Boolean by mutableStateOf(true)

}