package act.xio.undertow;

import act.app.ActionContext;
import act.app.App;
import act.conf.AppConfig;
import act.xio.NetworkClient;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.osgl.http.H;
import org.osgl.util.E;

/**
 * Dispatch undertow request to Act application
 */
public class ActHttpHandler implements HttpHandler {

    private final NetworkClient client;

    public ActHttpHandler(NetworkClient client) {
        E.NPE(client);
        this.client = client;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
        } else {
            ActionContext ctx = createAppContext(exchange);
            client.handle(ctx);
        }
    }

    private ActionContext createAppContext(HttpServerExchange exchange) {
        App app = client.app();
        AppConfig config = app.config();
        ActionContext ctx = ActionContext.create(app, req(exchange, config), resp(exchange, config));
        exchange.putAttachment(ActBlockingExchange.KEY_APP_CTX, ctx);
        exchange.startBlocking(new ActBlockingExchange(exchange));
        //ctx.saveLocal();
        return ctx;
    }

    private H.Request req(HttpServerExchange exchange, AppConfig config) {
        return new UndertowRequest(exchange, config);
    }

    private H.Response resp(HttpServerExchange exchange, AppConfig config) {
        return new UndertowResponse(exchange, config);
    }
}
