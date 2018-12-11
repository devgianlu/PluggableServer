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
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * @author Gianlu
 */
public class Client {
    private final HttpClient client;
    private final String url;
    private String token;

    public Client(@NotNull String url) {
        this.url = url;
        this.client = HttpClients.createDefault();
    }

    @NotNull
    private String requestSync(HttpUriRequest request) throws IOException {
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

    @NotNull
    public String startComponent(@NotNull String appId, @NotNull String componentId) throws IOException {
        return requestSync(new HttpGet(url + "/" + appId + "/" + componentId + "/StartComponent"));
    }

    @NotNull
    public String stopComponent(@NotNull String appId, @NotNull String componentId) throws IOException {
        return requestSync(new HttpGet(url + "/" + appId + "/" + componentId + "/StopComponent"));
    }

    @NotNull
    public String addComponent(@NotNull String appId, @NotNull String className) throws IOException {
        return requestSync(new HttpGet(url + "/" + appId + "/" + className + "/AddComponent"));
    }

    @NotNull
    public String getConfig(@NotNull String domain) throws IOException {
        return requestSync(new HttpGet(url + "/" + domain + "/GetConfig"));
    }

    @NotNull
    public String setConfig(@NotNull String domain, @NotNull String key, @NotNull String value) throws IOException {
        return requestSync(new HttpGet(url + "/" + domain + "/SetConfig?key=" + key + "&value=" + value));
    }

    @NotNull
    public String deleteApp(@NotNull String domain) throws IOException {
        return requestSync(new HttpGet(url + "/" + domain + "/DeleteApp"));
    }

    @NotNull
    public String uploadApp(@NotNull String appId, @NotNull String jarPath) throws IOException {
        HttpPut put = new HttpPut(url + "/" + appId + "/UploadApp");
        put.setEntity(MultipartEntityBuilder.create()
                .addPart("jar", new FileBody(new File(jarPath)))
                .build());

        return requestSync(put);
    }

    @NotNull
    public String uploadData(@NotNull String appId, @NotNull String dataPath) throws IOException {
        boolean zipped = dataPath.endsWith(".zip");

        HttpPut put = new HttpPut(url + "/" + appId + "/UploadData");
        put.setEntity(MultipartEntityBuilder.create()
                .addTextBody("zipped", String.valueOf(zipped))
                .addPart("file", new FileBody(new File(dataPath)))
                .build());

        return requestSync(put);
    }

    @NotNull
    public String listenTo(@NotNull String appId, @NotNull String componentId, @NotNull String domain) throws IOException {
        return requestSync(new HttpGet(url + "/" + appId + "/" + componentId + "/ListenTo/" + domain));
    }

    @NotNull
    public String stopListening(@NotNull String domain) throws IOException {
        return requestSync(new HttpGet(url + "/StopListeningTo/" + domain));
    }
}
