package com.gianlu.pluggableserver.core.handlers;

import com.gianlu.pluggableserver.core.Components;
import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gianlu
 */
public class DeleteComponentHandler extends AuthenticatedHandlerWithDomain {
    private final Components components;

    public DeleteComponentHandler(Components components) {
        this.components = components;
    }

    @Override
    public void handleAuthenticated(@NotNull HttpServerExchange exchange, @NotNull String domain) {
        components.delete(domain);
    }
}
