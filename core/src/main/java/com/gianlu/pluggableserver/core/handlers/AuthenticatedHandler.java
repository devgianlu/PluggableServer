package com.gianlu.pluggableserver.core.handlers;

import com.gianlu.pluggableserver.core.TokenHolder;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Deque;

/**
 * @author Gianlu
 */
public abstract class AuthenticatedHandler implements HttpHandler {
    @Nullable
    private static String findToken(@NotNull HttpServerExchange exchange) {
        String token;

        token = exchange.getRequestHeaders().getFirst("Pluggable-Token");
        if (token != null) return token;

        Deque<String> d = exchange.getQueryParameters().get("token");
        token = d != null ? d.getFirst() : null;
        if (token != null) return token;

        return null;
    }

    @Override
    public final void handleRequest(HttpServerExchange exchange) throws Exception {
        String token = findToken(exchange);
        if (token == null) {
            exchange.setStatusCode(StatusCodes.UNAUTHORIZED);
            exchange.getResponseSender().send("MISSING_TOKEN");
            return;
        }

        if (TokenHolder.get().checkToken(token)) {
            handleAuthenticated(exchange);
        } else {
            exchange.setStatusCode(StatusCodes.UNAUTHORIZED);
            exchange.getResponseSender().send("BAD_TOKEN");
        }
    }

    public abstract void handleAuthenticated(@NotNull HttpServerExchange exchange) throws Exception;
}
