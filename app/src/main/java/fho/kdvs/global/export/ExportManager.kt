package fho.kdvs.global.export

/**
 * Interface for exporting music through third-party APIs.
 */
interface ExportManager {
    suspend fun getExportPlaylistUri(): String?
}