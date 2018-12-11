package com.gianlu.pluggableserver.core.handlers;

import com.gianlu.pluggableserver.core.Applications;
import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gianlu
 */
public class DeleteAppHandler extends AuthenticatedHandlerWithAppId {
    private final Applications applications;

    public DeleteAppHandler(Applications applications) {
        this.applications = applications;
    }

    @Override
    public void handleAuthenticated(@NotNull HttpServerExchange exchange, @NotNull String appId) {
        applications.deleteApp(appId);
        exchange.getResponseSender().send("OK");
    }
}
