package com.gianlu.pluggableserver.core.handlers;

import com.gianlu.pluggableserver.core.CoreUtils;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gianlu
 */
public abstract class AuthenticatedHandlerWithAppAndComponentId extends AuthenticatedHandlerWithAppId {

    public abstract void handleAuthenticated(@NotNull HttpServerExchange exchange, @NotNull String appId, @NotNull String componentId) throws Exception;

    @Override
    public final void handleAuthenticated(@NotNull HttpServerExchange exchange, @NotNull String appId) throws Exception {
        String componentId = CoreUtils.getParam(exchange, "componentId");
        if (componentId == null) {
            exchange.setStatusCode(StatusCodes.BAD_REQUEST);
            exchange.getResponseSender().send("MISSING_COMPONENT_ID");
            return;
        }

        handleAuthenticated(exchange, appId, componentId);
    }
}
