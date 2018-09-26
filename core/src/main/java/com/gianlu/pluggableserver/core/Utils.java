package com.gianlu.pluggableserver.core;

import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Deque;

/**
 * @author Gianlu
 */
public final class Utils {

    private Utils() {
    }

    @Nullable
    public static String getParam(@NotNull HttpServerExchange exchange, @NotNull String name) {
        Deque<String> param = exchange.getQueryParameters().get(name);
        if (param == null) return null;
        else return param.getFirst();
    }
}
