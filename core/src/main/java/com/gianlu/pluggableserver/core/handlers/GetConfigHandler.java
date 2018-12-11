package com.gianlu.pluggableserver.core.handlers;

import com.gianlu.pluggableserver.core.Applications;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Gianlu
 */
public class GetConfigHandler extends AuthenticatedHandlerWithAppId {
    private final Applications applications;

    public GetConfigHandler(@NotNull Applications applications) {
        this.applications = applications;
    }

    @Override
    public void handleAuthenticated(@NotNull HttpServerExchange exchange, @NotNull String appId) {
        Map<String, String> config = applications.getConfig(appId);
        if (config == null) {
            exchange.setStatusCode(StatusCodes.NOT_FOUND);
            exchange.getResponseSender().send("DOMAIN_NOT_FOUND");
            return;
        }

        exchange.getResponseSender().send(config.toString());
    }
}
