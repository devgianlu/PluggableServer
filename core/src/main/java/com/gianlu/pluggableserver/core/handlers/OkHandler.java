package com.gianlu.pluggableserver.core.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * @author Gianlu
 */
public class OkHandler implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) {
        exchange.getResponseSender().send("OK");
    }
}
