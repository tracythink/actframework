package act.xio;

import act.Destroyable;

/**
 * Encapsulate operations provided by underline network service, e.g. netty/undertow etc
 */
public interface NetworkService extends Destroyable {
    void register(int port, NetworkClient client);

    void start();

    void shutdown();
}
