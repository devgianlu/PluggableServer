package com.gianlu.pluggableserver.core;

import com.gianlu.pluggableserver.api.ApiUtils;
import com.gianlu.pluggableserver.core.handlers.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
    private final Applications applications;
    private final int port;
    private final String apiUrl;
    private final File stateFile;
    private final @Nullable CloudStorageApi storageApi;

    public Core(@Nullable String apiUrl, @Nullable String stateFile, @Nullable String firebaseProjectId, @Nullable String firebaseCredentialsJson) throws IOException {
        this.apiUrl = CoreUtils.getEnv("API_URL", apiUrl);
        if (this.apiUrl == null)
            throw new IllegalArgumentException("Missing API URL!");

        this.stateFile = new File(CoreUtils.getEnv("STATE_FILE", stateFile));
        if (!this.stateFile.exists() && !this.stateFile.createNewFile())
            throw new IOException("Cannot create state file!");

        firebaseProjectId = CoreUtils.getEnv("FIREBASE_PROJECT_ID", firebaseProjectId);
        firebaseCredentialsJson = CoreUtils.getEnv("FIREBASE_CREDENTIALS_JSON", firebaseCredentialsJson);

        if (firebaseProjectId != null && firebaseCredentialsJson != null) {
            this.storageApi = new CloudStorageApi(firebaseProjectId, firebaseCredentialsJson);
        } else {
            this.storageApi = null;
        }

        this.port = ApiUtils.getEnvPort(80);
        this.applications = new Applications(this);
        resumeFromState();
        addBaseApiHandlers();

        this.undertow = Undertow.builder()
                .addHttpListener(port, "0.0.0.0")
                .setHandler(applications.handler())
                .build();
    }

    private void resumeFromState() {
        if (storageApi != null) {
            storageApi.getState(stateFile);
            storageApi.getComponents(applications.componentsDir);
        }

        JsonObject obj = readStateJson();
        if (obj == null) return;

        try {
            JsonArray apps = obj.getAsJsonArray("apps");
            for (JsonElement elm : apps)
                applications.loadAppFromState(elm.getAsJsonObject());

            JsonArray handlers = obj.getAsJsonArray("handlers");
            for (JsonElement elm : handlers)
                applications.loadHandlerFromState(elm.getAsJsonObject());

            JsonArray redirects = obj.getAsJsonArray("redirects");
            for (JsonElement elm : redirects)
                applications.loadRedirectFromState(elm.getAsJsonObject());

            JsonArray maintenance = obj.getAsJsonArray("maintenance");
            if (maintenance != null) {
                for (JsonElement elm : maintenance)
                    applications.maintenanceOn(elm.getAsString());
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void saveState() {
        try (OutputStream out = new FileOutputStream(stateFile)) {
            out.write(applications.stateJson().toString().getBytes());
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

        if (storageApi != null) {
            storageApi.destroyState();
            storageApi.destroyComponents();
        }
    }

    @Override
    public JsonObject readStateJson() {
        try (InputStream in = new FileInputStream(stateFile)) {
            JsonElement element = PARSER.parse(new InputStreamReader(in));
            if (element == null) {
                LOGGER.info("State file is empty!");
                return null;
            }

            if (!element.isJsonObject()) {
                LOGGER.info("Corrupted state file: " + element);
                return null;
            }

            return element.getAsJsonObject();
        } catch (IOException ex) {
            LOGGER.fatal("Failed resuming state from file!", ex);
            return null;
        }
    }

    @Override
    public boolean uploadToCloud() {
        if (storageApi == null) {
            LOGGER.warn("Cloud hasn't been setup!");
            return false;
        }

        try {
            storageApi.uploadState(stateFile);
            storageApi.uploadComponents(applications.componentsDir);
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
                .get("/DestroyState", new DestroyStateHandler(this));

        router.get("/ListComponents", new ListComponentsHandler(applications))
                .get("/{appId}/SetConfig", new SetConfigHandler(applications))
                .get("/{appId}/GetConfig", new GetConfigHandler(applications))
                .get("/{appId}/{componentId}/StartComponent", new StartComponentHandler(applications))
                .get("/{appId}/{componentId}/StopComponent", new StopComponentHandler(applications))
                .get("/{appId}/{className}/AddComponent", new AddComponentHandler(applications))
                .get("/{appId}/{componentId}/ListenTo/{domain}", new ListenToComponentHandler(applications))
                .get("/StopListeningTo/{domain}", new StopListeningHandler(applications))
                .get("/MaintenanceOn/{domain}", new MaintenanceOnHandler(applications))
                .get("/{appId}/DeleteApp", new DeleteAppHandler(applications))
                .put("/{appId}/UploadData", new UploadDataHandler(applications))
                .put("/{appId}/UploadApp", new UploadAppHandler(applications));

        router.put("/AddRedirect", new AddRedirectHandler(applications))
                .get("/RemoveRedirect/{id}", new RemoveRedirectHandler(applications));

        applications.addHandler(apiUrl, router);
        LOGGER.info(String.format("Loaded control API at %s.", apiUrl));
    }

    public void start() {
        LOGGER.info(String.format("Starting server on port %d!", port));
        undertow.start();
    }
}
