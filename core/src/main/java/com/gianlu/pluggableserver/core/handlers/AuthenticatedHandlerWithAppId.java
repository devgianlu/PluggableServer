package com.gianlu.pluggableserver.core.handlers;

import com.gianlu.pluggableserver.core.CoreUtils;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gianlu
 */
public abstract class AuthenticatedHandlerWithAppId extends AuthenticatedHandler {

    public abstract void handleAuthenticated(@NotNull HttpServerExchange exchange, @NotNull String appId) throws Exception;

    @Override
    public final void handleAuthenticated(@NotNull HttpServerExchange exchange) throws Exception {
        String appId = CoreUtils.getParam(exchange, "appId");
        if (appId == null) {
            exchange.setStatusCode(StatusCodes.BAD_REQUEST);
            exchange.getResponseSender().send("MISSING_APP_ID");
            return;
        }

        handleAuthenticated(exchange, appId);
    }
}
