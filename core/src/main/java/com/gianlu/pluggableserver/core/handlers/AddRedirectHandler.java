package com.gianlu.pluggableserver.core.handlers;

import com.gianlu.pluggableserver.core.Applications;
import com.gianlu.pluggableserver.core.CoreUtils;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gianlu
 */
public class AddRedirectHandler extends AuthenticatedHandler {
    private final Applications applications;

    public AddRedirectHandler(Applications applications) {
        this.applications = applications;
    }

    @Override
    public void handleAuthenticated(@NotNull HttpServerExchange exchange) {
        String regex = CoreUtils.getParam(exchange, "regex");
        if (regex == null) {
            exchange.setStatusCode(StatusCodes.BAD_REQUEST);
            exchange.getResponseSender().send("MISSING_REGEX");
            return;
        }

        String statusCode = CoreUtils.getParam(exchange, "statusCode");
        if (statusCode == null) {
            exchange.setStatusCode(StatusCodes.BAD_REQUEST);
            exchange.getResponseSender().send("MISSING_STATUS_CODE");
            return;
        }

        int statusCodeInt;
        try {
            statusCodeInt = Integer.parseInt(statusCode);
        } catch (NumberFormatException ex) {
            exchange.setStatusCode(StatusCodes.BAD_REQUEST);
            exchange.getResponseSender().send("BAD_STATUS_CODE");
            return;
        }


        String location = CoreUtils.getParam(exchange, "location");
        if (location == null) {
            exchange.setStatusCode(StatusCodes.BAD_REQUEST);
            exchange.getResponseSender().send("MISSING_LOCATION");
            return;
        }

        applications.addRedirect(regex, statusCodeInt, location);
    }
}
