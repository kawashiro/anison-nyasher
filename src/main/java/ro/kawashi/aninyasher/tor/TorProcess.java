package ro.kawashi.aninyasher.tor;

public interface TorProcess {

    TorProcess start() throws TorException;

    void stop();

    TorProcess changeExitNode() throws TorException;

    TorServiceStatus getStatus();
}
