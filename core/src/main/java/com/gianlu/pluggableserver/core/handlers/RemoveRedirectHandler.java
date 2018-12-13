package com.gianlu.pluggableserver.core.handlers;

import com.gianlu.pluggableserver.core.Applications;
import com.gianlu.pluggableserver.core.CoreUtils;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gianlu
 */
public class RemoveRedirectHandler extends AuthenticatedHandler {
    private final Applications applications;

    public RemoveRedirectHandler(@NotNull Applications applications) {
        this.applications = applications;
    }

    @Override
    public void handleAuthenticated(@NotNull HttpServerExchange exchange) {
        String redirectId = CoreUtils.getParam(exchange, "id");
        if (redirectId == null) {
            exchange.setStatusCode(StatusCodes.BAD_REQUEST);
            exchange.getResponseSender().send("MISSING_ID");
            return;
        }

        applications.removeRedirect(redirectId);
        exchange.getResponseSender().send("OK");
    }
}
