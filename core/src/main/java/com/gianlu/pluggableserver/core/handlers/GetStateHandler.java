package com.gianlu.pluggableserver.core.handlers;

import com.gianlu.pluggableserver.core.StateListener;
import com.google.gson.JsonArray;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gianlu
 */
public class GetStateHandler extends AuthenticatedHandler {
    private final StateListener listener;

    public GetStateHandler(StateListener listener) {
        this.listener = listener;
    }

    @Override
    public void handleAuthenticated(@NotNull HttpServerExchange exchange) {
        JsonArray state = listener.readStateJson();
        if (state == null) {
            exchange.setStatusCode(StatusCodes.NOT_FOUND);
            exchange.getResponseSender().send("MISSING_STATE");
        } else {
            exchange.setStatusCode(StatusCodes.OK);
            exchange.getResponseSender().send(state.toString());
        }
    }
}
