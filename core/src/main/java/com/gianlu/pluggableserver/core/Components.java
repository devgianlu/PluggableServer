package com.gianlu.pluggableserver.core;

import com.gianlu.pluggableserver.api.BaseComponent;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
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
    private final Handler handler;
    private final Map<String, HttpHandler> handlers;
    private final Map<String, InternalComponent> components;
    private final File componentsDir;

    Components() {
        this.componentsDir = new File("./components");
        if (!componentsDir.exists() && !componentsDir.mkdir())
            throw new IllegalStateException("Cannot create components directory!");

        this.handler = new Handler();
        this.handlers = new HashMap<>();
        this.components = new HashMap<>();
    }

    @NotNull
    public HttpHandler handler() {
        return handler;
    }

    public void addHandler(@NotNull String domain, @NotNull HttpHandler handler) {
        handlers.put(domain, handler);
    }

    public void loadComponent(@NotNull String domain, @NotNull Path path) throws IOException {
        File file = new File(componentsDir, domain);
        Files.copy(path, new FileOutputStream(file));
        components.put(domain, new InternalComponent(domain, file));
        LOGGER.info("Loaded " + domain);
    }

    public boolean hasComponent(@NotNull String domain) {
        return components.containsKey(domain);
    }

    public boolean startComponent(@NotNull String domain) {
        InternalComponent component = components.get(domain);
        if (component == null) return false;
        return component.start();
    }

    public void stopComponent(@NotNull String domain) {
        InternalComponent component = components.get(domain);
        if (component != null) component.stop();
    }

    public boolean setConfig(@NotNull String domain, @NotNull String key, @NotNull String value) {
        InternalComponent component = components.get(domain);
        if (component == null) return false;

        component.putConfig(key, value);
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

    private class InternalComponent {
        private final ClassLoader classLoader;
        private final Map<String, String> config;
        private final String domain;
        private final File jarFile;
        private boolean started = false;
        private BaseComponent component;

        private InternalComponent(@NotNull String domain, @NotNull File jarFile) throws MalformedURLException {
            this.jarFile = jarFile;
            this.domain = domain;
            this.config = new HashMap<>();
            this.classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, Components.class.getClassLoader());
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
                component = clazz.newInstance();

                LOGGER.info("Initialized " + domain);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | ClassCastException ex) {
                LOGGER.fatal(String.format("Failed initializing %s!", domain), ex);
            }
        }

        private boolean start() {
            if (component == null) return false;

            handlers.put(domain, component.createHandler(config));

            component.start();
            started = true;
            LOGGER.info("Started " + domain);
            return true;
        }

        private void stop() {
            handlers.remove(domain);
            if (component != null) {
                component.stop();
                component = null;
            }

            started = false;
            LOGGER.info("Stopped " + domain);

            components.remove(domain);
            if (!jarFile.delete())
                LOGGER.warn("Couldn't delete file for " + domain);
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
