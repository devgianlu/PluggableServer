package com.gianlu.pluggableserver.core;

import com.google.gson.JsonObject;
import io.undertow.server.HttpServerExchange;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Gianlu
 */
public final class CoreUtils {

    private CoreUtils() {
    }

    public static void clear(File file, Logger logger) {
        if (file.isFile()) {
            if (!file.delete())
                logger.warn("Failed deleting " + file.getAbsolutePath());
        } else if (file.isDirectory()) {
            for (File sub : file.listFiles())
                clear(sub, logger);

            if (!file.delete())
                logger.warn("Failed deleting " + file.getAbsolutePath());
        }
    }

    public static void unzip(@NotNull File zipFile, @NotNull File dest, @NotNull Logger logger) throws IOException {
        byte[] buffer = new byte[2048];
        try (ZipInputStream in = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                if (entry.isDirectory())
                    continue;

                File file = new File(dest, entry.getName());
                logger.info("Extracting " + file.getAbsolutePath());
                if (!file.getParentFile().exists() && !file.getParentFile().mkdirs())
                    logger.warn("Failed creating directories: " + file.getAbsolutePath());

                try (FileOutputStream out = new FileOutputStream(file)) {
                    int read;
                    while ((read = in.read(buffer)) != -1)
                        out.write(buffer, 0, read);
                }
            }
        }
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
