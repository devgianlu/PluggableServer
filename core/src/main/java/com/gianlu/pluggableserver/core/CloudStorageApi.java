package com.gianlu.pluggableserver.core;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.xnio.streams.Streams;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Gianlu
 */
public class CloudStorageApi {
    private static final Logger LOGGER = LogManager.getLogger(CloudStorageApi.class);
    private final String bucket;
    private final Storage storage;

    public CloudStorageApi(@NotNull String projectId, @NotNull String credentialsJson) throws IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(credentialsJson.getBytes()));
        this.storage = StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(credentials)
                .build().getService();

        this.bucket = projectId + ".appspot.com";
    }

    private static void listFiles(File node, List<String> paths) {
        if (node.isFile()) {
            paths.add(node.getAbsolutePath());
        } else if (node.isDirectory()) {
            for (File sub : node.listFiles())
                listFiles(sub, paths);
        }
    }

    public void getState(@NotNull File stateFile) {
        Blob blob = storage.get(bucket, "state.json");
        if (blob != null && blob.exists()) {
            blob.downloadTo(stateFile.toPath());
            LOGGER.info("Downloaded state from cloud!");
        } else {
            LOGGER.info("State not present on cloud!");
        }
    }

    public void getComponents(@NotNull File componentsDir) {
        Blob blob = storage.get(bucket, "components.zip");
        if (blob != null && blob.exists()) {
            try {
                CoreUtils.clear(componentsDir, LOGGER);
                unpackComponents(blob, componentsDir);
                LOGGER.info("Downloaded components from cloud!");
            } catch (IOException ex) {
                LOGGER.fatal("Failed unpacking components!", ex);
            }
        } else {
            LOGGER.info("Components not present on cloud!");
        }
    }

    private void unpackComponents(@NotNull Blob blob, @NotNull File componentsDir) throws IOException {
        File tmp = File.createTempFile("pluggable_components_", ".zip");
        blob.downloadTo(tmp.toPath());
        CoreUtils.unzip(tmp, componentsDir, LOGGER);
    }

    public void uploadComponents(@NotNull File componentsDir) throws IOException {
        int subAt = componentsDir.getAbsolutePath().length() + 1;

        List<String> files = new ArrayList<>();
        listFiles(componentsDir, files);

        try (WriteChannel writer = storage.writer(BlobInfo.newBuilder(bucket, "components.zip").build())) {
            try (ZipOutputStream out = new ZipOutputStream(new WriterOutputStream(writer))) {
                for (String file : files) {
                    out.putNextEntry(new ZipEntry(file.substring(subAt)));
                    try (FileInputStream in = new FileInputStream(file)) {
                        Streams.copyStream(in, out, false);
                    }
                }
            }
        }

        LOGGER.info("Uploaded components to cloud!");
    }

    public void uploadState(@NotNull File stateFile) throws IOException {
        storage.create(BlobInfo.newBuilder(bucket, "state.json").build(), Files.readAllBytes(stateFile.toPath()));
        LOGGER.info("Uploaded state to cloud!");
    }

    public void destroyState() {
        storage.delete(BlobId.of(bucket, "state.json"));
        LOGGER.info("Deleted state from cloud!");
    }

    public void destroyComponents() {
        storage.delete(BlobId.of(bucket, "components.zip"));
        LOGGER.info("Deleted components from cloud!");
    }

    private static class WriterOutputStream extends OutputStream {
        private final WriteChannel channel;

        WriterOutputStream(@NotNull WriteChannel channel) {
            this.channel = channel;
        }

        @Override
        public void write(int b) throws IOException {
            write(new byte[]{(byte) b}, 0, 1);
        }

        @Override
        public void write(@NotNull byte[] b, int off, int len) throws IOException {
            channel.write(ByteBuffer.wrap(b, off, len));
        }
    }
}
