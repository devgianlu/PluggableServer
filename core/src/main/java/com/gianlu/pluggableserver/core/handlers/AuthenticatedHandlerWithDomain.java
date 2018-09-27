package com.gianlu.pluggableserver.core.handlers;

import com.gianlu.pluggableserver.core.CoreUtils;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gianlu
 */
public abstract class AuthenticatedHandlerWithDomain extends AuthenticatedHandler {

    public abstract void handleAuthenticated(@NotNull HttpServerExchange exchange, @NotNull String domain) throws Exception;

    @Override
    public final void handleAuthenticated(@NotNull HttpServerExchange exchange) throws Exception {
        String domain = CoreUtils.getParam(exchange, "domain");
        if (domain == null) {
            exchange.setStatusCode(StatusCodes.BAD_REQUEST);
            exchange.getResponseSender().send("MISSING_DOMAIN");
            return;
        }

        handleAuthenticated(exchange, domain);
    }
}
