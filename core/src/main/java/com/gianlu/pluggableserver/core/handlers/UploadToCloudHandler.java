package com.gianlu.pluggableserver.core.handlers;

import com.gianlu.pluggableserver.core.StateListener;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gianlu
 */
public class UploadToCloudHandler extends AuthenticatedHandler {
    private final StateListener state;

    public UploadToCloudHandler(@NotNull StateListener state) {
        this.state = state;
    }

    @Override
    public void handleAuthenticated(@NotNull HttpServerExchange exchange) {
        if (state.uploadToCloud()) {
            exchange.getResponseSender().send("OK");
        } else {
            exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
            exchange.getResponseSender().send("FAILED");
        }
    }
}
