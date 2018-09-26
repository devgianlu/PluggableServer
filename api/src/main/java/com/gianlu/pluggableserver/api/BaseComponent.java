package com.gianlu.pluggableserver.api;

import io.undertow.server.HttpHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Gianlu
 */
public abstract class BaseComponent {
    public BaseComponent() {
    }

    @NotNull
    public abstract HttpHandler createHandler(@NotNull Map<String, String> config);

    /**
     * Start this component.
     */
    public abstract void start();

    /**
     * Stop this component, {@link #start()} won't be called again.
     */
    public abstract void stop();
}
