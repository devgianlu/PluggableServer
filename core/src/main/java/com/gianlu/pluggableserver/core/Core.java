package com.gianlu.pluggableserver.core;

import com.gianlu.pluggableserver.api.ApiUtils;
import com.gianlu.pluggableserver.core.handlers.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.MalformedURLException;

/**
 * @author Gianlu
 */
public class Core implements StateListener {
    private static final Logger LOGGER = Logger.getLogger(Core.class);
    private final static JsonParser PARSER = new JsonParser();
    private final Undertow undertow;
    private final Components components;
    private final int port;
    private final String apiUrl;
    private final File stateFile;
    private final CloudStorageApi storageApi;

    public Core(@Nullable String apiUrl, @Nullable String stateFile, @Nullable String firebaseProjectId, @Nullable String firebaseCredentialsJson) throws IOException {
        this.apiUrl = CoreUtils.getEnv("API_URL", apiUrl);
        if (this.apiUrl == null)
            throw new IllegalArgumentException("Missing API URL!");

        this.stateFile = new File(CoreUtils.getEnv("STATE_FILE", stateFile));
        if (!this.stateFile.exists() && !this.stateFile.createNewFile())
            throw new IOException("Cannot create state file!");

        firebaseProjectId = CoreUtils.getEnv("FIREBASE_PROJECT_ID", firebaseProjectId);
        firebaseCredentialsJson = CoreUtils.getEnv("FIREBASE_CREDENTIALS_JSON", firebaseCredentialsJson);

        this.storageApi = new CloudStorageApi(firebaseProjectId, firebaseCredentialsJson);

        this.port = ApiUtils.getEnvPort(80);
        this.components = new Components(this);
        this.undertow = Undertow.builder()
                .addHttpListener(port, "0.0.0.0")
                .setHandler(components.handler())
                .build();

        addBaseApiHandlers();

        resumeFromState();
    }

    private void resumeFromState() {
        storageApi.getState(stateFile);
        storageApi.getComponents(components.componentsDir);

        JsonArray array = readStateJson();
        if (array == null) return;

        try {
            for (JsonElement elm : array)
                components.loadFromState(elm.getAsJsonObject());
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void saveState() {
        try (OutputStream out = new FileOutputStream(stateFile)) {
            out.write(components.stateJson().toString().getBytes());
            LOGGER.info("State saved successfully!");
        } catch (IOException ex) {
            LOGGER.fatal("Failed saving state to file!", ex);
        }
    }

    @Override
    public void destroyState() {
        if (!stateFile.delete()) {
            try (OutputStream out = new FileOutputStream(stateFile)) {
                out.write('\0');
                LOGGER.info("State file emptied successfully!");
            } catch (IOException ex) {
                LOGGER.warn("Failed emptying state file!", ex);
            }
        } else {
            LOGGER.info("State file removed successfully!");
        }

        storageApi.destroyState();
        storageApi.destroyComponents();
    }

    @Override
    public JsonArray readStateJson() {
        try (InputStream in = new FileInputStream(stateFile)) {
            JsonElement element = PARSER.parse(new InputStreamReader(in));
            if (element == null) {
                LOGGER.info("State file is empty!");
                return null;
            }

            if (!element.isJsonArray()) {
                LOGGER.info("Corrupted state file: " + element);
                return null;
            }

            return element.getAsJsonArray();
        } catch (IOException ex) {
            LOGGER.fatal("Failed resuming state from file!", ex);
            return null;
        }
    }

    @Override
    public boolean uploadToCloud() {
        try {
            storageApi.uploadState(stateFile);
            storageApi.uploadComponents(components.componentsDir);
            return true;
        } catch (IOException ex) {
            LOGGER.fatal("Failed resuming state from file!", ex);
            return false;
        }
    }

    private void addBaseApiHandlers() {
        RoutingHandler router = new RoutingHandler();
        router.get("/", new OkHandler())
                .get("/GenerateToken", new GenerateTokenHandler())
                .get("/GetState", new GetStateHandler(this))
                .get("/UploadToCloud", new UploadToCloudHandler(this))
                .delete("/DestroyState", new DestroyStateHandler(this))
                .get("/ListComponents", new ListComponentsHandler(components))
                .get("/{domain}/SetConfig", new SetConfigHandler(components))
                .get("/{domain}/GetConfig", new GetConfigHandler(components))
                .get("/{domain}/StartComponent", new StartComponentHandler(components))
                .get("/{domain}/StopComponent", new StopComponentHandler(components))
                .delete("/{domain}/DeleteComponent", new DeleteComponentHandler(components))
                .put("/{domain}/UploadData", new UploadDataHandler(components))
                .put("/{domain}/UploadComponent", new UploadComponentHandler(components));

        components.addHandler(apiUrl, router);
        LOGGER.info(String.format("Loaded control API at %s.", apiUrl));
    }

    public void start() {
        LOGGER.info(String.format("Starting server on port %d!", port));
        undertow.start();
    }
}
