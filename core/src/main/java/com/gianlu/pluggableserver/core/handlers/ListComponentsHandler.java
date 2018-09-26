package com.gianlu.pluggableserver.core.handlers;

import com.gianlu.pluggableserver.core.Components;
import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gianlu
 */
public class ListComponentsHandler extends AuthenticatedHandler {
    private final Components components;

    public ListComponentsHandler(@NotNull Components components) {
        this.components = components;
    }

    @Override
    public void handleAuthenticated(@NotNull HttpServerExchange exchange) {
        StringBuilder builder = new StringBuilder();
        builder.append("NAME    STARTED    CONFIG\n");

        for (String str : components.components())
            builder.append(str).append('\n');

        exchange.getResponseSender().send(builder.toString());
    }
}
