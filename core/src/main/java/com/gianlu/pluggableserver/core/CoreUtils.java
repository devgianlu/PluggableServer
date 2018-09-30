package com.gianlu.pluggableserver.core;

import com.google.gson.JsonObject;
import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Gianlu
 */
public final class CoreUtils {

    private CoreUtils() {
    }

    @Nullable
    public static String getParam(@NotNull HttpServerExchange exchange, @NotNull String name) {
        Deque<String> param = exchange.getQueryParameters().get(name);
        if (param == null) return null;
        else return param.getFirst();
    }

    public static String getEnv(@NotNull String name, String fallback) {
        String env = System.getenv(name);
        if (env == null || env.isEmpty()) return fallback;
        return env;
    }

    @NotNull
    public static JsonObject toJson(Map<String, String> map) {
        JsonObject obj = new JsonObject();
        for (String key : map.keySet()) obj.addProperty(key, map.get(key));
        return obj;
    }

    @NotNull
    public static Map<String, String> toMap(JsonObject obj) {
        Map<String, String> map = new HashMap<>();
        for (String key : obj.keySet()) map.put(key, obj.get(key).getAsString());
        return map;
    }
}
