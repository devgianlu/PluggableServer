package com.gianlu.pluggableserver.core.handlers;

import com.gianlu.pluggableserver.core.Components;
import com.gianlu.pluggableserver.core.CoreUtils;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gianlu
 */
public class SetConfigHandler extends AuthenticatedHandlerWithDomain {
    private final Components components;

    public SetConfigHandler(@NotNull Components components) {
        this.components = components;
    }

    @Override
    public void handleAuthenticated(@NotNull HttpServerExchange exchange, @NotNull String domain) {
        String key = CoreUtils.getParam(exchange, "key");
        if (key == null) {
            exchange.setStatusCode(StatusCodes.BAD_REQUEST);
            exchange.getResponseSender().send("MISSING_KEY");
            return;
        }

        String value = CoreUtils.getParam(exchange, "value");
        if (value == null) {
            exchange.setStatusCode(StatusCodes.BAD_REQUEST);
            exchange.getResponseSender().send("MISSING_VALUE");
            return;
        }

        if (components.setConfig(domain, key, value)) {
            exchange.getResponseSender().send("OK");
        } else {
            exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
            exchange.getResponseSender().send("FAILED");
        }
    }
}
