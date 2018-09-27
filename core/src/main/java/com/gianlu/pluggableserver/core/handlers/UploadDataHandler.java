package com.gianlu.pluggableserver.core.handlers;

import com.gianlu.pluggableserver.core.Components;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.StatusCodes;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gianlu
 */
public class UploadDataHandler extends AuthenticatedHandlerWithDomain {
    private final Components components;

    public UploadDataHandler(@NotNull Components components) {
        this.components = components;
    }

    @Override
    public void handleAuthenticated(@NotNull HttpServerExchange exchange, @NotNull String domain) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            exchange.startBlocking();
            return;
        }

        try (FormDataParser parser = FormParserFactory.builder().build().createParser(exchange)) {
            FormData data = parser.parseBlocking();
            FormData.FormValue file = data.getFirst("file");
            if (file == null) {
                exchange.setStatusCode(StatusCodes.BAD_REQUEST);
                exchange.getResponseSender().send("MISSING_FILE");
                return;
            }

            if (file.isFile()) {
                String path = components.uploadData(domain, file.getPath(), file.getFileName(), Boolean.parseBoolean(data.getFirst("zipped").getValue()));
                if (path != null) {
                    exchange.setStatusCode(StatusCodes.OK);
                    exchange.getResponseSender().send(path);
                } else {
                    exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
                    exchange.getResponseSender().send("FAILED");
                }
            } else {
                exchange.setStatusCode(StatusCodes.BAD_REQUEST);
                exchange.getResponseSender().send("NOT_A_FILE");
            }
        }
    }
}
