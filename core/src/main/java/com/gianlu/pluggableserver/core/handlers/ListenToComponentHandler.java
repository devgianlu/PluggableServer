package com.gianlu.pluggableserver.core.handlers;

import com.gianlu.pluggableserver.core.Applications;
import com.gianlu.pluggableserver.core.CoreUtils;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gianlu
 */
public class ListenToComponentHandler extends AuthenticatedHandlerWithAppAndComponentId {
    private final Applications applications;

    public ListenToComponentHandler(@NotNull Applications applications) {
        this.applications = applications;
    }

    @Override
    public void handleAuthenticated(@NotNull HttpServerExchange exchange, @NotNull String appId, @NotNull String componentId) {
        String domain = CoreUtils.getParam(exchange, "domain");
        if (domain == null) {
            exchange.setStatusCode(StatusCodes.BAD_REQUEST);
            exchange.getResponseSender().send("MISSING_DOMAIN");
            return;
        }

        if (applications.componentListenTo(appId, componentId, domain)) {
            exchange.getResponseSender().send("OK");
        } else {
            exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
            exchange.getResponseSender().send("FAILED");
        }
    }
}
