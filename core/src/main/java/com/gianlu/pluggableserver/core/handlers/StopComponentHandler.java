package com.gianlu.pluggableserver.core.handlers;

import com.gianlu.pluggableserver.core.Components;
import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gianlu
 */
public class StopComponentHandler extends AuthenticatedHandlerWithDomain {
    private final Components components;

    public StopComponentHandler(@NotNull Components components) {
        this.components = components;
    }

    @Override
    public void handleAuthenticated(@NotNull HttpServerExchange exchange, @NotNull String domain) {
        components.stopComponent(domain);
        exchange.getResponseSender().send("OK");
    }
}
