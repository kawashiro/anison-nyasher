package ro.kawashi.aninyasher.tor;

public interface TorProcess {

    void start() throws TorException;

    void stop();

    void changeExitNode() throws TorException;

    TorServiceStatus getStatus();
}
