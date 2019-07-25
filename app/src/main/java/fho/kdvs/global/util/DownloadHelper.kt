package fho.kdvs.global.util

import java.io.File

object DownloadHelper {
    const val broadcastExtension = ".mp3"
    const val temporaryExtension = ".tmp"

    fun renameFileAfterCompletion(file: File) {
        val dest = File("${file.parent}${file.name}$broadcastExtension")
        file.renameTo(dest)
    }
}