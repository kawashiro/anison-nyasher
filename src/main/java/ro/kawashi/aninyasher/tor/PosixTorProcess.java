package ro.kawashi.aninyasher.tor;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.Thread;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A {@link TorProcess} implementation for POSIX systems.
 */
public class PosixTorProcess implements TorProcess {

    private static final Logger logger = LogManager.getLogger(PosixTorProcess.class);

    private final ProcessBuilder torProcessBuilder;
    private Process torProcess;
    private BufferedReader torStdout;

    /**
     * Creates a new {@link PosixTorProcess} instance providing a string with the path to the tor binary.
     *
     * @param torBinary Path to the tor binary.
     */
    public PosixTorProcess(String torBinary) {
        this(new ProcessBuilder(torBinary).redirectInput(ProcessBuilder.Redirect.PIPE));
    }

    /**
     * Creates a new {@link PosixTorProcess} instance providing a {@link ProcessBuilder} for the tor binary.
     *
     * @param torProcessBuilder Process builder for the tor binary.
     */
    public PosixTorProcess(ProcessBuilder torProcessBuilder) {
        this.torProcessBuilder = torProcessBuilder;
    }

    /**
     * @inheritDoc
     */
    @Override
    public TorProcess start() throws TorException {
        try {
            torProcess = torProcessBuilder.start();
            torStdout = new BufferedReader(new InputStreamReader(torProcess.getInputStream()));
            awaitUntilMessage("Bootstrapped 100%");

        } catch (IOException e) {
            throw new TorException("Unable to start tor process: " + e.getMessage(), e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        return this;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void stop() {
        if (torProcess != null) {
            torProcess.destroy();
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public TorProcess changeExitNode() throws TorException {
        try {
            Runtime.getRuntime().exec("kill -HUP " + torProcess.pid());
            awaitUntilMessage("Received reload signal");
            awaitUntilMessage("Configuration file");

        } catch (IOException e) {
            throw new TorException(e.getMessage(), e);
        }

        return this;
    }

    /**
     * @inheritDoc
     */
    @Override
    public TorServiceStatus getStatus() {
        if (torProcess == null) {
            return TorServiceStatus.NOT_STARTED;
        }

        if (torProcess.isAlive()) {
            return TorServiceStatus.ALIVE;
        }

        return torProcess.exitValue() == 0 ? TorServiceStatus.EXITED : TorServiceStatus.DEAD;
    }

    private void awaitUntilMessage(String message) {
        try {
            String line;
            do {
                line = torStdout.readLine();
                if (line == null) {
                    throw new TorException("Tor process exited unexpectedly");
                }
                logger.debug("TOR :: " + line);
            } while (!line.contains(message));

        } catch (IOException e) {
            throw new TorException(e.getMessage(), e);
        }
    }
}
