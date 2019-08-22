package fho.kdvs.track

/**
 * In order to provide specific functionality for track source in [TrackDetailsFragment],
 * we must have run-time knowledge of calling fragment.
 */
enum class TrackDetailsType {
    BROADCAST_DETAILS, FAVORITE
}