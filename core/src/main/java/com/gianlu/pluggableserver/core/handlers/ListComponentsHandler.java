package com.gianlu.pluggableserver.core.handlers;

import com.gianlu.pluggableserver.core.Applications;
import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gianlu
 */
public class ListComponentsHandler extends AuthenticatedHandler {
    private final Applications applications;

    public ListComponentsHandler(@NotNull Applications applications) {
        this.applications = applications;
    }

    @Override
    public void handleAuthenticated(@NotNull HttpServerExchange exchange) {
        StringBuilder builder = new StringBuilder();
        applications.toString(builder);
        exchange.getResponseSender().send(builder.toString());
    }
}
