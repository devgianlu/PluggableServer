package com.gianlu.pluggableserver.core.handlers;

import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gianlu
 */
public class SystemHandler extends AuthenticatedHandler {
    @Override
    public void handleAuthenticated(@NotNull HttpServerExchange exchange) {
        String builder = " - Total memory: " + Runtime.getRuntime().totalMemory() +
                "\n - Max memory: " + Runtime.getRuntime().maxMemory() +
                "\n - Free memory: " + Runtime.getRuntime().freeMemory() +
                "\n - Processors: " + Runtime.getRuntime().availableProcessors();
        exchange.getResponseSender().send(builder);
    }
}
