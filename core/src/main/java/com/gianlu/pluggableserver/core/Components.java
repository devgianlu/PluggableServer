package com.gianlu.pluggableserver.core;

import com.gianlu.pluggableserver.api.BaseComponent;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.FileUtils;
import io.undertow.util.StatusCodes;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gianlu
 */
public class Components {
    private final static Logger LOGGER = Logger.getLogger(Components.class);
    private static final String COMPONENT_ENTRY_CLASS_KEY = "pluggable_entryClass";
    public final File componentsDir;
    private final Handler handler;
    private final Map<String, HttpHandler> handlers;
    private final Map<String, InternalComponent> components;
    private final StateListener state;

    Components(@NotNull StateListener state) {
        this.state = state;

        this.componentsDir = new File("./components");
        if (!componentsDir.exists() && !componentsDir.mkdir())
            throw new IllegalStateException("Cannot create components directory!");

        this.handler = new Handler();
        this.handlers = new HashMap<>();
        this.components = new HashMap<>();
    }

    void loadFromState(@NotNull JsonObject obj) throws MalformedURLException {
        String domain = obj.get("domain").getAsString();
        components.put(domain, new InternalComponent(obj));
        LOGGER.info("Loaded " + domain + " from state");
    }

    @NotNull
    public HttpHandler handler() {
        return handler;
    }

    public void addHandler(@NotNull String domain, @NotNull HttpHandler handler) {
        handlers.put(domain, handler);
    }

    public void loadComponent(@NotNull String domain, @NotNull Path path) throws IOException {
        File file = new File(componentsDir, domain + ".jar");
        Files.copy(path, new FileOutputStream(file));
        components.put(domain, new InternalComponent(domain, file));
        LOGGER.info("Loaded " + domain);
        state.saveState();
    }

    public boolean hasComponent(@NotNull String domain) {
        return components.containsKey(domain);
    }

    public boolean startComponent(@NotNull String domain) {
        InternalComponent component = components.get(domain);
        if (component == null) return false;

        boolean a = component.start();
        state.saveState();
        return a;
    }

    public void stopComponent(@NotNull String domain) {
        InternalComponent component = components.get(domain);
        if (component != null) component.stop();
        state.saveState();
    }

    public boolean setConfig(@NotNull String domain, @NotNull String key, @NotNull String value) {
        InternalComponent component = components.get(domain);
        if (component == null) return false;

        component.putConfig(key, value);
        state.saveState();
        return true;
    }

    @NotNull
    public List<String> components() {
        List<String> list = new ArrayList<>();
        for (InternalComponent component : components.values())
            list.add(component.domain + "    " + component.started + "    " + component.config);
        return list;
    }

    @Nullable
    public Map<String, String> getConfig(@NotNull String domain) {
        InternalComponent component = components.get(domain);
        if (component == null) return null;
        else return component.config;
    }

    @Nullable
    public String uploadData(@NotNull String domain, @NotNull Path path, @NotNull String filename, boolean zipped) {
        InternalComponent component = components.get(domain);
        if (component == null) return null;

        try {
            if (zipped) {
                CoreUtils.unzip(path.toFile(), component.dataDir, LOGGER);
                return component.dataDir.getPath();
            } else {
                if (!component.dataDir.exists())
                    if (!component.dataDir.mkdir())
                        LOGGER.warn("Failed creating data directory for " + component.domain);

                File file = new File(component.dataDir, filename);
                Files.copy(path, new FileOutputStream(file));
                return file.getPath();
            }
        } catch (IOException ex) {
            LOGGER.fatal("Failed uploading data for " + domain, ex);
            return null;
        }
    }

    public boolean canLoad(@NotNull String domain) {
        if (hasComponent(domain)) {
            InternalComponent component = components.get(domain);
            return !component.started;
        } else {
            return true;
        }
    }

    @NotNull
    public JsonArray stateJson() {
        JsonArray array = new JsonArray(components.size());
        for (InternalComponent component : components.values()) array.add(component.stateJson());
        return array;
    }

    public void delete(String domain) {
        InternalComponent component = components.remove(domain);
        if (component == null) return;

        component.delete();
        state.saveState();
    }

    private class InternalComponent {
        private final URLClassLoader classLoader;
        private final Map<String, String> config;
        private final String domain;
        private final File dataDir;
        private final File jarFile;
        private boolean started = false;
        private BaseComponent component;

        private InternalComponent(@NotNull JsonObject obj) throws MalformedURLException {
            this.domain = obj.get("domain").getAsString();
            this.config = CoreUtils.toMap(obj.getAsJsonObject("config"));
            this.jarFile = new File(obj.get("jarPath").getAsString());
            this.classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, Components.class.getClassLoader());
            this.dataDir = new File(componentsDir, domain);

            init();

            if (obj.get("started").getAsBoolean()) start();
        }

        private InternalComponent(@NotNull String domain, @NotNull File jarFile) throws MalformedURLException {
            this.domain = domain;
            this.jarFile = jarFile;
            this.config = new HashMap<>();
            this.classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, Components.class.getClassLoader());
            this.dataDir = new File(componentsDir, domain);
        }

        private void putConfig(@NotNull String key, @NotNull String value) {
            config.put(key, value);
            LOGGER.info(String.format("Set %s=%s for %s", key, value, domain));

            if (key.equals(COMPONENT_ENTRY_CLASS_KEY)) init();
        }

        private void init() {
            try {
                // noinspection unchecked
                Class<BaseComponent> clazz = (Class<BaseComponent>) classLoader.loadClass(config.get(COMPONENT_ENTRY_CLASS_KEY));
                component = clazz.getConstructor(Map.class).newInstance(config);

                LOGGER.info("Initialized " + domain);
            } catch (ReflectiveOperationException ex) {
                LOGGER.fatal(String.format("Failed initializing %s!", domain), ex);
            }
        }

        private boolean start() {
            if (component == null) return false;

            handlers.put(domain, component.getHandler());

            component.start();
            started = true;
            LOGGER.info("Started " + domain);
            return true;
        }

        private void stop() {
            if (component == null) return;

            handlers.remove(domain);
            component.stop();

            started = false;
            LOGGER.info("Stopped " + domain);
        }

        private void delete() {
            stop();

            try {
                if (dataDir.exists()) FileUtils.deleteRecursive(dataDir.toPath());

                classLoader.close();
                if (jarFile.exists() && !jarFile.delete())
                    throw new IOException("Failed deleting JAR!"); // This won't usually work
                LOGGER.info("Deleted " + component);
            } catch (IOException ex) {
                LOGGER.warn("Failed deleting " + component, ex);
            }
        }

        @NotNull
        JsonObject stateJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("domain", domain);
            obj.addProperty("jarPath", jarFile.getAbsolutePath());
            obj.add("config", CoreUtils.toJson(config));
            obj.addProperty("started", started);
            return obj;
        }
    }

    private class Handler implements HttpHandler {

        @Override
        public void handleRequest(HttpServerExchange exchange) throws Exception {
            String host = exchange.getHostName();
            if (host == null) {
                LOGGER.trace("Missing Host header.");
                exchange.setStatusCode(StatusCodes.BAD_REQUEST);
                return;
            }

            HttpHandler handler = handlers.get(host);
            if (handler == null) {
                LOGGER.info("Handler not found for host: " + host);
                exchange.setStatusCode(StatusCodes.NOT_FOUND);
                return;
            }

            handler.handleRequest(exchange);
        }
    }
}
