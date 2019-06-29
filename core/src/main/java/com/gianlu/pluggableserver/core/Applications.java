package com.gianlu.pluggableserver.core;

import com.gianlu.pluggableserver.api.BaseComponent;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.FileUtils;
import io.undertow.util.Headers;
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
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Gianlu
 */
public class Applications {
    private final static Logger LOGGER = Logger.getLogger(Applications.class);
    private static final MaintenanceHandler MAINTENANCE_HANDLER = new MaintenanceHandler();
    final File componentsDir;
    private final Handler handler;
    private final Map<String, HttpHandler> handlers;
    private final Map<String, InternalApplication> applications;
    private final StateListener state;
    private final Redirects redirects;

    Applications(@NotNull StateListener state) {
        this.state = state;

        this.componentsDir = new File("./applications");
        if (!componentsDir.exists() && !componentsDir.mkdir())
            throw new IllegalStateException("Cannot create applications directory!");

        this.handler = new Handler();
        this.handlers = Collections.synchronizedMap(new HashMap<>());
        this.applications = new HashMap<>();
        this.redirects = new Redirects();
    }

    void loadAppFromState(@NotNull JsonObject obj) throws MalformedURLException {
        String appId = obj.get("id").getAsString();
        applications.put(appId, new InternalApplication(obj));
        LOGGER.info("Loaded " + appId + " from state");
    }

    void loadHandlerFromState(@NotNull JsonObject obj) {
        componentListenTo(obj.get("appId").getAsString(), obj.get("componentId").getAsString(), obj.get("domain").getAsString());
    }

    void loadRedirectFromState(@NotNull JsonObject obj) {
        redirects.entries.add(new RedirectEntry(obj));
    }

    @NotNull
    public HttpHandler handler() {
        return handler;
    }

    public void loadApp(@NotNull String id, @NotNull Path path) throws IOException {
        File file = new File(componentsDir, id + ".jar");
        Files.copy(path, new FileOutputStream(file));
        applications.put(id, new InternalApplication(id, file));
        LOGGER.info("Loaded " + id);
        state.saveState();
    }

    public boolean setConfig(@NotNull String appId, @NotNull String key, @NotNull String value) {
        InternalApplication app = applications.get(appId);
        if (app == null) return false;

        app.putConfig(key, value);
        state.saveState();
        return true;
    }

    public void toString(@NotNull StringBuilder builder) {
        builder.append("APPS\n");
        for (InternalApplication app : applications.values()) {
            builder.append("    - ID: ").append(app.id)
                    .append("\n      CONFIG: ").append(app.config)
                    .append("\n      COMPONENTS: ");

            app.componentsToString(builder);
            builder.append('\n');
        }

        builder.append("HANDLERS\n");
        for (Map.Entry<String, HttpHandler> entry : handlers.entrySet()) {
            if (entry.getValue() instanceof BaseComponent) {
                builder.append("    - ").append(entry.getKey())
                        .append(" -> ").append(((BaseComponent) entry.getValue()).id());
            } else if (entry.getValue() instanceof MaintenanceHandler) {
                builder.append("    - ").append(entry.getKey())
                        .append(" -> Maintenance");
            } else {
                builder.append("    - ").append(entry.getKey())
                        .append(" -> ").append(entry.getValue().toString());
            }

            builder.append('\n');
        }

        builder.append("REDIRECTS\n");
        for (RedirectEntry entry : redirects.entries) {
            builder.append("    - ").append(entry.id)
                    .append("\n      CODE: ").append(entry.statusCode)
                    .append("\n      REDIRECT: ").append(entry.pattern.toString())
                    .append(" -> ").append(entry.location);

            builder.append('\n');
        }
    }

    @Nullable
    public Map<String, String> getConfig(@NotNull String appId) {
        InternalApplication app = applications.get(appId);
        if (app == null) return null;
        else return app.config;
    }

    @Nullable
    public String uploadData(@NotNull String appId, @NotNull Path path, @NotNull String filename, boolean zipped) {
        InternalApplication app = applications.get(appId);
        if (app == null) {
            LOGGER.fatal(String.format("App %s not found!", appId));
            return null;
        }

        try {
            if (zipped) {
                CoreUtils.unzip(path.toFile(), app.dataDir, LOGGER);
                return app.dataDir.getPath();
            } else {
                if (!app.dataDir.exists())
                    if (!app.dataDir.mkdir())
                        LOGGER.warn("Failed creating data directory for " + appId);

                File file = new File(app.dataDir, filename);
                Files.copy(path, new FileOutputStream(file));
                return file.getPath();
            }
        } catch (IOException ex) {
            LOGGER.fatal("Failed uploading data for " + appId, ex);
            return null;
        }
    }

    @NotNull
    public JsonObject stateJson() {
        JsonObject obj = new JsonObject();

        JsonArray appsArray = new JsonArray(applications.size());
        for (InternalApplication app : applications.values()) appsArray.add(app.stateJson());
        obj.add("apps", appsArray);

        JsonArray maintenanceArray = new JsonArray();
        JsonArray handlersArray = new JsonArray(handlers.size());
        for (Map.Entry<String, HttpHandler> entry : handlers.entrySet()) {
            if (entry.getValue() instanceof BaseComponent) {
                BaseComponent component = (BaseComponent) entry.getValue();

                JsonObject handlerObj = new JsonObject();
                handlerObj.addProperty("appId", component.attachedToAppId());
                handlerObj.addProperty("componentId", component.id());
                handlerObj.addProperty("domain", entry.getKey());
                handlersArray.add(handlerObj);
            } else if (entry.getValue() instanceof MaintenanceHandler) {
                maintenanceArray.add(entry.getKey());
            }
        }
        obj.add("handlers", handlersArray);
        obj.add("maintenance", maintenanceArray);

        JsonArray redirectsArray = new JsonArray(redirects.entries.size());
        for (RedirectEntry entry : redirects.entries) {
            JsonObject redirectObj = new JsonObject();
            redirectObj.addProperty("id", entry.id);
            redirectObj.addProperty("code", entry.statusCode);
            redirectObj.addProperty("regex", entry.pattern.toString());
            redirectObj.addProperty("location", entry.location);
            redirectsArray.add(redirectObj);
        }
        obj.add("redirects", redirectsArray);

        return obj;
    }

    public void deleteApp(@NotNull String appId) {
        InternalApplication app = applications.remove(appId);
        if (app == null) return;

        app.delete();
        state.saveState();
    }

    public boolean startComponent(@NotNull String appId, @NotNull String componentId) {
        InternalApplication app = applications.get(appId);
        if (app == null) {
            LOGGER.fatal(String.format("App %s not found!", appId));
            return false;
        }

        boolean a = app.startComponent(componentId);
        state.saveState();
        return a;
    }

    public void maintenanceOn(@NotNull String domain) {
        addHandler(domain, MAINTENANCE_HANDLER);
    }

    public void stopComponent(@NotNull String appId, @NotNull String componentId) {
        InternalApplication app = applications.get(appId);
        if (app != null) app.stopComponent(componentId);
        state.saveState();
    }

    public void addHandler(@NotNull String domain, @NotNull HttpHandler handler) {
        handlers.put(domain, handler);
        state.saveState();
    }

    public boolean componentListenTo(@NotNull String appId, @NotNull String componentId, @NotNull String domain) {
        InternalApplication app = applications.get(appId);
        if (app == null) {
            LOGGER.fatal(String.format("App %s not found!", appId));
            return false;
        }

        boolean a = app.componentListenTo(componentId, domain);
        state.saveState();
        return a;
    }

    public void stopListening(@NotNull String domain) {
        handlers.remove(domain);
        state.saveState();
        LOGGER.info(String.format("Unbounded %s.", domain));
    }

    @Nullable
    public String addComponent(@NotNull String appId, @NotNull String className) {
        InternalApplication app = applications.get(appId);
        if (app == null) {
            LOGGER.fatal(String.format("App %s not found!", appId));
            return null;
        }

        BaseComponent component = app.addComponent(className);
        state.saveState();
        return component == null ? null : component.id();
    }

    @NotNull
    public String addRedirect(@NotNull String regex, int code, @NotNull String location) {
        RedirectEntry entry = new RedirectEntry(Pattern.compile(regex), code, location);
        redirects.entries.add(entry);
        LOGGER.info(String.format("Added redirect from %s to %s.", regex, location));
        state.saveState();
        return entry.id;
    }

    public void removeRedirect(@NotNull String redirectId) {
        redirects.remove(redirectId);
        state.saveState();
    }

    public void stop() {
        for (String key : handlers.keySet())
            handlers.put(key, MAINTENANCE_HANDLER);

        for (InternalApplication app : applications.values())
            app.stop();

    }

    public static class RedirectEntry {
        final String location;
        final int statusCode;
        private final Pattern pattern;
        private final String id;

        RedirectEntry(@NotNull Pattern pattern, int statusCode, @NotNull String location) {
            this.pattern = pattern;
            this.statusCode = statusCode;
            this.location = location;
            this.id = UUID.randomUUID().toString();

            if (statusCode != StatusCodes.PERMANENT_REDIRECT && statusCode != StatusCodes.TEMPORARY_REDIRECT)
                throw new IllegalArgumentException("Invalid redirect status code: " + statusCode);
        }

        RedirectEntry(@NotNull JsonObject obj) {
            this.pattern = Pattern.compile(obj.get("regex").getAsString());
            this.id = obj.get("id").getAsString();
            this.statusCode = obj.get("code").getAsInt();
            this.location = obj.get("location").getAsString();
        }

        boolean matches(@NotNull String url) {
            return pattern.matcher(url).matches();
        }
    }

    private static class MaintenanceHandler implements HttpHandler {

        @Override
        public void handleRequest(HttpServerExchange exchange) {
            exchange.setStatusCode(503);
            exchange.setReasonPhrase("Maintenance");
        }
    }

    private class InternalApplication {
        private final URLClassLoader classLoader;
        private final Map<String, String> config;
        private final File dataDir;
        private final String id;
        private final File jarFile;
        private final Map<String, BaseComponent> components = new HashMap<>();

        private InternalApplication(@NotNull JsonObject obj) throws MalformedURLException {
            this.id = obj.get("id").getAsString();
            this.config = CoreUtils.toMap(obj.getAsJsonObject("config"));
            this.jarFile = new File(obj.get("jarPath").getAsString());
            this.classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, Applications.class.getClassLoader());
            this.dataDir = new File(componentsDir, id);

            JsonArray compArray = obj.getAsJsonArray("components");
            for (JsonElement elm : compArray) {
                JsonObject compObj = elm.getAsJsonObject();
                BaseComponent component = addComponent(compObj.get("class").getAsString());
                if (component != null && compObj.get("started").getAsBoolean()) component.start();
            }
        }

        private InternalApplication(@NotNull String id, @NotNull File jarFile) throws MalformedURLException {
            this.id = id;
            this.jarFile = jarFile;
            this.config = new HashMap<>();
            this.classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, Applications.class.getClassLoader());
            this.dataDir = new File(componentsDir, id);
        }

        private void putConfig(@NotNull String key, @NotNull String value) {
            config.put(key, value);
            LOGGER.info(String.format("Set %s=%s for %s", key, value, id));
        }

        @Nullable
        private BaseComponent addComponent(@NotNull String className) {
            try {
                // noinspection unchecked
                Class<BaseComponent> clazz = (Class<BaseComponent>) classLoader.loadClass(className);
                BaseComponent component = clazz.getConstructor(Map.class, String.class).newInstance(config, id);
                components.put(component.id(), component);

                LOGGER.info("Initialized " + component.id());
                return component;
            } catch (ReflectiveOperationException ex) {
                LOGGER.fatal(String.format("Failed initializing %s!", className), ex);
                return null;
            }
        }

        private boolean componentListenTo(@NotNull String componentId, @NotNull String domain) {
            BaseComponent component = components.get(componentId);
            if (component == null) {
                LOGGER.fatal(String.format("Component %s not found!", componentId));
                return false;
            }

            if (!component.isStarted()) {
                LOGGER.fatal(String.format("Component %s isn't started!", componentId));
                return false;
            }

            handlers.put(domain, component);
            LOGGER.info(String.format("%s is now bound to %s", domain, componentId));
            return true;
        }

        private boolean startComponent(@NotNull String componentId) {
            BaseComponent component = components.get(componentId);
            if (component == null) {
                LOGGER.fatal(String.format("Component %s not found!", componentId));
                return false;
            }

            component.start();
            LOGGER.info(String.format("Started %s successfully!", componentId));
            return true;
        }

        private void stopComponent(@NotNull String componentId) {
            BaseComponent component = components.get(componentId);
            if (component == null) {
                LOGGER.fatal(String.format("Component %s not found!", componentId));
                return;
            }

            synchronized (handlers) {
                handlers.values().removeIf(c -> c == component);
            }

            component.stop();
            LOGGER.info(String.format("Stopped %s successfully!", componentId));
        }

        @NotNull
        JsonObject stateJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", id);
            obj.addProperty("jarPath", jarFile.getAbsolutePath());
            obj.add("config", CoreUtils.toJson(config));

            JsonArray compArray = new JsonArray(components.size());
            for (BaseComponent component : components.values()) {
                JsonObject compObj = new JsonObject();
                compObj.addProperty("class", component.getClass().getCanonicalName());
                compObj.addProperty("started", component.isStarted());
                compArray.add(compObj);
            }

            obj.add("components", compArray);
            return obj;
        }

        private void componentsToString(@NotNull StringBuilder builder) {
            for (Map.Entry<String, BaseComponent> entry : components.entrySet()) {
                builder.append("\n        - CMP ID: ").append(entry.getKey())
                        .append("\n            STARTED: ").append(entry.getValue().isStarted());
            }
        }

        private void delete() {
            List<String> toStop = new ArrayList<>(components.keySet());
            for (String componentId : toStop)
                stopComponent(componentId);

            try {
                if (dataDir.exists()) FileUtils.deleteRecursive(dataDir.toPath());

                classLoader.close();
                if (jarFile.exists() && !jarFile.delete())
                    throw new IOException("Failed deleting JAR!"); // This won't usually work

                LOGGER.info(String.format("Deleting %s!", id));
            } catch (IOException ex) {
                LOGGER.warn(String.format("Failed deleting %s!", id), ex);
            }
        }

        void stop() {
            for (BaseComponent component : components.values())
                component.stop();
        }
    }

    private class Redirects {
        private final List<RedirectEntry> entries = new ArrayList<>();

        boolean handle(@NotNull HttpServerExchange exchange) {
            String url = exchange.getRequestURL();

            for (RedirectEntry entry : entries) {
                if (entry.matches(url)) {
                    exchange.setStatusCode(entry.statusCode);
                    exchange.getResponseHeaders().add(Headers.LOCATION, entry.location);
                    LOGGER.trace("Redirecting to " + entry.location);
                    return true;
                }
            }

            return false;
        }

        void remove(@NotNull String redirectId) {
            Iterator<RedirectEntry> iterator = entries.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().id.equals(redirectId)) {
                    iterator.remove();
                    LOGGER.info(String.format("Removed redirect %s!", redirectId));
                    return;
                }
            }
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

            if (redirects.handle(exchange)) {
                exchange.endExchange();
                return;
            }

            HttpHandler handler = handlers.get(host);
            if (handler == null) {
                LOGGER.info("Component not found for host: " + host);
                exchange.setStatusCode(StatusCodes.NOT_FOUND);
                return;
            }

            handler.handleRequest(exchange);
        }
    }
}
