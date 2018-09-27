package com.gianlu.pluggableserver.core;

import com.gianlu.pluggableserver.api.ApiUtils;
import com.gianlu.pluggableserver.core.handlers.*;
import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

/**
 * @author Gianlu
 */
public class Core {
    private static final Logger LOGGER = Logger.getLogger(Core.class);
    private final Undertow undertow;
    private final Components components;
    private final int port;
    private final String apiUrl;

    public Core(@Nullable String apiUrl) {
        this.apiUrl = CoreUtils.getEnv("API_URL", apiUrl);
        if (this.apiUrl == null) throw new IllegalArgumentException("Missing API URL!");

        this.port = ApiUtils.getEnvPort(80);
        this.components = new Components();
        this.undertow = Undertow.builder()
                .addHttpListener(port, "0.0.0.0")
                .setHandler(components.handler())
                .build();

        addBaseApiHandlers();
    }

    private void addBaseApiHandlers() {
        RoutingHandler router = new RoutingHandler();
        router.get("/", new OkHandler())
                .get("/GenerateToken", new GenerateTokenHandler())
                .get("/ListComponents", new ListComponentsHandler(components))
                .get("/{domain}/SetConfig", new SetConfigHandler(components))
                .get("/{domain}/GetConfig", new GetConfigHandler(components))
                .get("/{domain}/StartComponent", new StartComponentHandler(components))
                .get("/{domain}/StopComponent", new StopComponentHandler(components))
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
