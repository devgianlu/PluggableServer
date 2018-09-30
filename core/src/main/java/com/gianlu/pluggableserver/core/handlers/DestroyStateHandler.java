package com.gianlu.pluggableserver.core.handlers;

import com.gianlu.pluggableserver.core.StateListener;
import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gianlu
 */
public class DestroyStateHandler extends AuthenticatedHandler {
    private final StateListener state;

    public DestroyStateHandler(StateListener state) {
        this.state = state;
    }

    @Override
    public void handleAuthenticated(@NotNull HttpServerExchange exchange) {
        state.destroyState();
        exchange.getResponseSender().send("OK");
    }
}
