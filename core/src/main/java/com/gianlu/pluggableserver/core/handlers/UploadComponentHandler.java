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
public class UploadComponentHandler extends AuthenticatedHandlerWithDomain {
    private final Components components;

    public UploadComponentHandler(Components components) {
        this.components = components;
    }

    @Override
    public void handleAuthenticated(@NotNull HttpServerExchange exchange, @NotNull String domain) throws Exception {
        if (components.hasComponent(domain)) {
            exchange.setStatusCode(StatusCodes.ACCEPTED);
            exchange.getResponseSender().send("COMPONENT_ALREADY_LOADED");
            return;
        }

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
                components.loadComponent(domain, file.getPath());
                exchange.setStatusCode(StatusCodes.OK);
                exchange.getResponseSender().send("COMPONENT_LOADED");
            } else {
                exchange.setStatusCode(StatusCodes.BAD_REQUEST);
                exchange.getResponseSender().send("NOT_A_FILE");
            }
        }
    }
}
