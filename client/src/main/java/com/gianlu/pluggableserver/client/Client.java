package com.gianlu.pluggableserver.client;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * @author Gianlu
 */
public class Client {
    private final HttpClient client;
    private final String url;
    private String token;
    private String currentComponentId;
    private String currentAppId;

    public Client(@NotNull String url) {
        this.url = url;
        this.client = HttpClients.createDefault();
    }

    @NotNull
    private String requestSync(@NotNull HttpUriRequest request) throws IOException {
        if (token != null) request.addHeader("Pluggable-Token", token);

        HttpResponse resp = client.execute(request);
        return EntityUtils.toString(resp.getEntity());
    }

    @NotNull
    public String connect() throws IOException {
        return requestSync(new HttpGet(url));
    }

    public void setToken(@NotNull String token) {
        if (token.isEmpty()) throw new IllegalArgumentException("Token mustn't be empty!");
        this.token = token;
    }

    @NotNull
    public String requestToken() throws IOException {
        return requestSync(new HttpGet(url + "/GenerateToken"));
    }

    @NotNull
    public String getState() throws IOException {
        return requestSync(new HttpGet(url + "/GetState"));
    }

    @NotNull
    public String uploadToCloud() throws IOException {
        return requestSync(new HttpGet(url + "/UploadToCloud"));
    }

    @NotNull
    public String listComponents() throws IOException {
        return requestSync(new HttpGet(url + "/ListComponents"));
    }

    @NotNull
    public String destroyState() throws IOException {
        return requestSync(new HttpGet(url + "/DestroyState"));
    }

    private void checkAppId() {
        if (currentAppId == null) throw new IllegalStateException("Missing app ID!");
    }

    private void checkComponentId() {
        if (currentComponentId == null) throw new IllegalStateException("Missing app ID!");
    }

    @Nullable
    public String getComponentId() {
        return currentComponentId;
    }

    public void setComponentId(@Nullable String componentId) {
        this.currentComponentId = componentId;
    }

    @Nullable
    public String getAppId() {
        return currentAppId;
    }

    public void setAppId(@Nullable String appId) {
        this.currentAppId = appId;
        this.currentComponentId = null;
    }

    @NotNull
    public String addRedirect(@NotNull @RegExp String regex, @NotNull String statusCode, @NotNull String location) throws IOException {
        return requestSync(new HttpGet(url + "/AddRedirect/" + regex + "/" + statusCode + "/" + location + "/"));
    }

    @NotNull
    public String startComponent() throws IOException {
        checkAppId();
        checkComponentId();
        return requestSync(new HttpGet(url + "/" + currentAppId + "/" + currentComponentId + "/StartComponent"));
    }

    @NotNull
    public String stopComponent() throws IOException {
        checkAppId();
        checkComponentId();
        return requestSync(new HttpGet(url + "/" + currentAppId + "/" + currentComponentId + "/StopComponent"));
    }

    @NotNull
    public String addComponent(@NotNull String className) throws IOException {
        checkAppId();
        return requestSync(new HttpGet(url + "/" + currentAppId + "/" + className + "/AddComponent"));
    }

    @NotNull
    public String getConfig() throws IOException {
        checkAppId();
        return requestSync(new HttpGet(url + "/" + currentAppId + "/GetConfig"));
    }

    @NotNull
    public String setConfig(@NotNull String key, @NotNull String value) throws IOException {
        checkAppId();
        return requestSync(new HttpGet(url + "/" + currentAppId + "/SetConfig?key=" + key + "&value=" + value));
    }

    @NotNull
    public String deleteApp() throws IOException {
        checkAppId();
        return requestSync(new HttpGet(url + "/" + currentAppId + "/DeleteApp"));
    }

    @NotNull
    public String uploadApp(@NotNull String jarPath) throws IOException {
        checkAppId();

        HttpPut put = new HttpPut(url + "/" + currentAppId + "/UploadApp");
        put.setEntity(MultipartEntityBuilder.create()
                .addPart("jar", new FileBody(new File(jarPath)))
                .build());

        return requestSync(put);
    }

    @NotNull
    public String uploadData(@NotNull String dataPath) throws IOException {
        checkAppId();

        boolean zipped = dataPath.endsWith(".zip");

        HttpPut put = new HttpPut(url + "/" + currentAppId + "/UploadData");
        put.setEntity(MultipartEntityBuilder.create()
                .addTextBody("zipped", String.valueOf(zipped))
                .addPart("file", new FileBody(new File(dataPath)))
                .build());

        return requestSync(put);
    }

    @NotNull
    public String listenTo(@NotNull String domain) throws IOException {
        checkAppId();
        checkComponentId();
        return requestSync(new HttpGet(url + "/" + currentAppId + "/" + currentComponentId + "/ListenTo/" + domain));
    }

    @NotNull
    public String stopListening(@NotNull String domain) throws IOException {
        return requestSync(new HttpGet(url + "/StopListeningTo/" + domain));
    }

    public boolean hasToken() {
        return token != null;
    }

    public boolean hasAppId() {
        return currentAppId != null;
    }

    public boolean hasComponentId() {
        return currentComponentId != null;
    }
}
