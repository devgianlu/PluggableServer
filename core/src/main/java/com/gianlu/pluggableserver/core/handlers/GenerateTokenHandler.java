package com.gianlu.pluggableserver.core.handlers;

import com.gianlu.pluggableserver.core.TokenHolder;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * @author Gianlu
 */
public class GenerateTokenHandler implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) {
        TokenHolder.get().generate();
        exchange.getResponseSender().send("OK");
    }
}
