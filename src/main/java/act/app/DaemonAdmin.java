package act.app;

import act.cli.Command;
import act.cli.Required;
import act.data.JodaDateTimeCodec;
import act.di.Context;
import act.util.PropertySpec;
import org.joda.time.DateTime;
import org.osgl.util.E;

import java.util.Map;

/**
 * Access application daemon status
 */
@SuppressWarnings("unused")
public class DaemonAdmin {

    @Command(name = "act.daemon.list", help = "List app daemons")
    @PropertySpec("id,state")
    public Iterable<Daemon> list() {
        return App.instance().registeredDaemons();
    }

    @Command(name = "act.daemon.start", help = "Start app daemon")
    public void start(
            @Required("specify daemon id") String id,
            @Context CliContext context
    ) {
        Daemon daemon = get(id, context);
        daemon.start();
        report(daemon, context);
    }

    @Command(name = "act.daemon.stop", help = "Stop app daemon")
    public void stop(
            @Required("specify daemon id") String id,
            @Context CliContext context
    ) {
        Daemon daemon = get(id, context);
        daemon.stop();
        report(daemon, context);
    }

    @Command(name = "act.daemon.restart", help = "Re-Start app daemon")
    public void restart(
            @Required("specify daemon id") String id,
            @Context CliContext context
    ) {
        Daemon daemon = get(id, context);
        daemon.restart();
        report(daemon, context);
    }

    @Command(name = "act.daemon.status", help = "Report app daemon status")
    public void status(
            @Required("specify daemon id") String id,
            @Context CliContext context,
            @Context JodaDateTimeCodec fmt
    ) {
        Daemon daemon = get(id, context);
        Daemon.State state = daemon.state();
        DateTime ts = daemon.timestamp();
        Exception lastError = daemon.lastError();
        context.println("Daemon[%s]: %s at %s", id, state, fmt.toString(ts));
        if (null != lastError) {
            DateTime errTs = daemon.errorTimestamp();
            if (null != errTs) {
                context.println("Last error: %s at %s", E.stackTrace(lastError), fmt.toString(errTs));
            } else {
                context.println("Last error: %s", E.stackTrace(lastError));
            }
        }
        Map<String, Object> attributes = daemon.getAttributes();
        if (!attributes.isEmpty()) {
            context.println("Attributes: %s", attributes);
        }
    }

    private static Daemon get(String id, CliContext context) {
        Daemon daemon = App.instance().registeredDaemon(id);
        if (null == daemon) {
            context.println("Unknown daemon: %s", id);
            return null;
        }
        return daemon;
    }

    private static void report(Daemon daemon, CliContext context) {
        context.println("Daemon[%s]: %s", daemon.id(), daemon.state());
    }
}
