package com.gianlu.pluggableserver.core.handlers;

import com.gianlu.pluggableserver.core.Applications;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.StatusCodes;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author Gianlu
 */
public class AddRedirectHandler extends AuthenticatedHandler {
    private final Applications applications;

    public AddRedirectHandler(Applications applications) {
        this.applications = applications;
    }

    @Override
    public void handleAuthenticated(@NotNull HttpServerExchange exchange) throws IOException {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            exchange.startBlocking();
            return;
        }

        try (FormDataParser parser = FormParserFactory.builder().build().createParser(exchange)) {
            FormData data = parser.parseBlocking();
            FormData.FormValue regex = data.getFirst("regex");
            if (regex == null) {
                exchange.setStatusCode(StatusCodes.BAD_REQUEST);
                exchange.getResponseSender().send("MISSING_REGEX");
                return;
            }

            FormData.FormValue statusCode = data.getFirst("statusCode");
            if (statusCode == null) {
                exchange.setStatusCode(StatusCodes.BAD_REQUEST);
                exchange.getResponseSender().send("MISSING_STATUS_CODE");
                return;
            }

            int statusCodeInt;
            try {
                statusCodeInt = Integer.parseInt(statusCode.getValue());
            } catch (NumberFormatException ex) {
                exchange.setStatusCode(StatusCodes.BAD_REQUEST);
                exchange.getResponseSender().send("BAD_STATUS_CODE");
                return;
            }

            if (statusCodeInt != StatusCodes.PERMANENT_REDIRECT && statusCodeInt != StatusCodes.TEMPORARY_REDIRECT) {
                exchange.setStatusCode(StatusCodes.BAD_REQUEST);
                exchange.getResponseSender().send("BAD_STATUS_CODE");
                return;
            }

            FormData.FormValue location = data.getFirst("location");
            if (location == null) {
                exchange.setStatusCode(StatusCodes.BAD_REQUEST);
                exchange.getResponseSender().send("MISSING_LOCATION");
                return;
            }

            exchange.getResponseSender().send(applications.addRedirect(regex.getValue(), statusCodeInt, location.getValue()));
        }
    }
}
