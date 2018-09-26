package com.gianlu.pluggableserver.core.handlers;

import com.gianlu.pluggableserver.core.Components;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gianlu
 */
public class StartComponentHandler extends AuthenticatedHandlerWithDomain {
    private final Components components;

    public StartComponentHandler(Components components) {
        this.components = components;
    }

    @Override
    public void handleAuthenticated(@NotNull HttpServerExchange exchange, @NotNull String domain) {
        if (components.startComponent(domain)) {
            exchange.getResponseSender().send("OK");
        } else {
            exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
            exchange.getResponseSender().send("FAILED");
        }
    }
}
