package ro.kawashi.aninyasher.tor;

/**
 * Interface for a tor process manipulation.
 */
public interface TorProcess {

    /**
     * Starts the tor process and waits until it is fully bootstrapped.
     *
     * @return TorProcess
     * @throws TorException If the tor process cannot be started.
     */
    TorProcess start() throws TorException;

    /**
     * Stops the tor process.
     */
    void stop();

    /**
     * Change the exit node to change a public IP address.
     *
     * @return TorProcess
     * @throws TorException If tor process unexpectedly exits.
     */
    TorProcess changeExitNode() throws TorException;

    /**
     * Returns the current status of the tor process.
     *
     * @return TorServiceStatus
     */
    TorServiceStatus getStatus();
}
