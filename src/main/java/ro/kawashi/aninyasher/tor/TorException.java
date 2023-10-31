package ro.kawashi.aninyasher.tor;

/**
 * Exception thrown when an error occurs while starting or communicating with the tor process.
 */
public class TorException extends RuntimeException {

    /**
     * @inheritDoc
     */
    public TorException(String message) {
        super(message);
    }

    /**
     * @inheritDoc
     */
    public TorException(String message, Throwable cause) {
        super(message, cause);
    }
}
