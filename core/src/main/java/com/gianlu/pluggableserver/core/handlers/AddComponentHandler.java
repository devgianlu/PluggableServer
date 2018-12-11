package com.gianlu.pluggableserver.core.handlers;

import com.gianlu.pluggableserver.core.Applications;
import com.gianlu.pluggableserver.core.CoreUtils;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gianlu
 */
public class AddComponentHandler extends AuthenticatedHandlerWithAppId {
    private final Applications applications;

    public AddComponentHandler(@NotNull Applications applications) {
        this.applications = applications;
    }

    @Override
    public void handleAuthenticated(@NotNull HttpServerExchange exchange, @NotNull String appId) {
        String className = CoreUtils.getParam(exchange, "className");
        if (className == null) {
            exchange.setStatusCode(StatusCodes.BAD_REQUEST);
            exchange.getResponseSender().send("MISSING_CLASS_NAME");
            return;
        }

        String componentId = applications.addComponent(appId, className);
        if (componentId != null) {
            exchange.getResponseSender().send("OK: " + componentId);
        } else {
            exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
            exchange.getResponseSender().send("FAILED");
        }
    }
}
