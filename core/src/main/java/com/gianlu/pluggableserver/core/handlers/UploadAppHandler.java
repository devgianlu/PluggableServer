package com.gianlu.pluggableserver.core.handlers;

import com.gianlu.pluggableserver.core.Applications;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.StatusCodes;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gianlu
 */
public class UploadAppHandler extends AuthenticatedHandlerWithAppId {
    private final Applications applications;

    public UploadAppHandler(Applications applications) {
        this.applications = applications;
    }

    @Override
    public void handleAuthenticated(@NotNull HttpServerExchange exchange, @NotNull String appId) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            exchange.startBlocking();
            return;
        }

        try (FormDataParser parser = FormParserFactory.builder().build().createParser(exchange)) {
            FormData data = parser.parseBlocking();
            FormData.FormValue file = data.getFirst("jar");
            if (file == null) {
                exchange.setStatusCode(StatusCodes.BAD_REQUEST);
                exchange.getResponseSender().send("MISSING_JAR");
                return;
            }

            if (file.isFile()) {
                applications.loadApp(appId, file.getPath());
                exchange.setStatusCode(StatusCodes.OK);
                exchange.getResponseSender().send("OK");
            } else {
                exchange.setStatusCode(StatusCodes.BAD_REQUEST);
                exchange.getResponseSender().send("NOT_A_FILE");
            }
        }
    }
}
