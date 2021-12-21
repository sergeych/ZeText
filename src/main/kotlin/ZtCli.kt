import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.mm.coloredconsole.colored
import java.io.File

class ZtCli : CliktCommand(name="zetext") {

    val decryptFile: Boolean by option(
        "-d",
        help = "decrypt .ztext to stdout (will ask for password in GUI). Source file is required!"
    ).flag()
    val source: File? by argument().file(mustExist = true, canBeDir = false).optional()

    override fun run() {
        colored {
            if (decryptFile && source == null) error("Missing source file for -d key")
            startUIApp(source, decryptFile)
        }
    }
}
