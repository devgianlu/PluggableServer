package com.gianlu.pluggableserver.core.handlers;

import com.gianlu.pluggableserver.core.Applications;
import com.gianlu.pluggableserver.core.CoreUtils;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gianlu
 */
public class StopListeningHandler extends AuthenticatedHandler {
    private final Applications applications;

    public StopListeningHandler(@NotNull Applications applications) {
        this.applications = applications;
    }

    @Override
    public void handleAuthenticated(@NotNull HttpServerExchange exchange) {
        String domain = CoreUtils.getParam(exchange, "domain");
        if (domain == null) {
            exchange.setStatusCode(StatusCodes.BAD_REQUEST);
            exchange.getResponseSender().send("MISSING_DOMAIN");
            return;
        }

        applications.stopListening(domain);
        exchange.getResponseSender().send("OK");
    }
}
