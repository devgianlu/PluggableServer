package com.gianlu.pluggableserver.api;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Gianlu
 */
public abstract class BaseComponent implements HttpHandler {
    private final String appId;
    private volatile boolean started = false;

    public BaseComponent(@NotNull Map<String, String> config, @NotNull String appId) {
        this.appId = appId;
    }

    /**
     * May be called multiple times.
     *
     * @return a valid and unique HttpHandler
     */
    @NotNull
    public abstract HttpHandler getHandler();

    @NotNull
    public abstract String id();

    /**
     * Start this component.
     */
    public final synchronized void start() {
        startImpl();
        started = true;
    }

    /**
     * Stop this component, {@link #start()} may be called again.
     */
    public final synchronized void stop() {
        stopImpl();
        started = false;
    }

    protected abstract void startImpl();

    protected abstract void stopImpl();

    public final synchronized boolean isStarted() {
        return started;
    }

    @Override
    public void handleRequest(@NotNull HttpServerExchange exchange) throws Exception {
        getHandler().handleRequest(exchange);
    }

    @NotNull
    public final String attachedToAppId() {
        return appId;
    }
}
