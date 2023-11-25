package ro.kawashi.aninyasher.remoteservice.anison

/**
 * Exception on any Anison service error.
 *
 * @param message String
 */
class AnisonException(message: String) extends RuntimeException(message)

/**
 * Exception if song was already aired.
 *
 * @param message String
 */
class SongNotVotableException(message: String) extends AnisonException(message)
