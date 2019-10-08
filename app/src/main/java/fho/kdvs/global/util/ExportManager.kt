package fho.kdvs.global.util

/**
 * Interface for exporting music through third-party APIs.
 */
interface ExportManager {
    suspend fun getExportPlaylistUri(): String?
}