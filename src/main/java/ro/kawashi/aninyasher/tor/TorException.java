package ro.kawashi.aninyasher.tor;

public class TorException extends RuntimeException {

    public TorException(String message) {
        super(message);
    }

    public TorException(String message, Throwable cause) {
        super(message, cause);
    }
}
