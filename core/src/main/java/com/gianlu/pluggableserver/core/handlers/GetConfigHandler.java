package com.gianlu.pluggableserver.core.handlers;

import com.gianlu.pluggableserver.core.Components;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Gianlu
 */
public class GetConfigHandler extends AuthenticatedHandlerWithDomain {
    private final Components components;

    public GetConfigHandler(@NotNull Components components) {
        this.components = components;
    }

    @Override
    public void handleAuthenticated(@NotNull HttpServerExchange exchange, @NotNull String domain) {
        Map<String, String> config = components.getConfig(domain);
        if (config == null) {
            exchange.setStatusCode(StatusCodes.NOT_FOUND);
            exchange.getResponseSender().send("DOMAIN_NOT_FOUND");
            return;
        }

        exchange.getResponseSender().send(config.toString());
    }
}
