package com.gianlu.pluggableserver.client;

import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * @author Gianlu
 */
public class Main {

    private static void handle(@NotNull InputStream in, @NotNull InputStream tokenIn, @NotNull PrintStream out) throws IOException {
        Scanner tokenScanner = new Scanner(tokenIn);

        Scanner scanner = new Scanner(in);
        Client client = null;
        String appId = null;
        String componentId = null;

        while (true) {
            out.print("> ");

            String line;
            if (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (line.isEmpty()) continue;
            } else {
                break;
            }

            out.println(line);

            if (line.equals("quit")) {
                if (componentId != null) componentId = null;
                else if (appId != null) appId = null;
                else break;
            } else if (line.startsWith("connect")) {
                if (client != null) throw new IllegalStateException("Already connected!");

                client = new Client(line.substring(8));
                out.println(client.connect());
            } else if (line.equals("token")) {
                if (client == null) throw new IllegalStateException("Not connected!");

                out.println(client.requestToken());
                out.print("Enter the token: ");
                client.setToken(tokenScanner.nextLine().trim());
            } else if (line.equals("list")) {
                if (client == null) throw new IllegalStateException("Not connected!");

                out.println(client.listComponents());
            } else if (line.equals("getState")) {
                if (client == null) throw new IllegalStateException("Not connected!");

                out.println(client.getState());
            } else if (line.equals("uploadToCloud")) {
                if (client == null) throw new IllegalStateException("Not connected!");

                out.println(client.uploadToCloud());
            } else if (line.startsWith("appId")) {
                if (client == null) throw new IllegalStateException("Not connected!");

                appId = line.split("\\s")[1].trim();
            } else if (line.startsWith("componentId")) {
                if (client == null) throw new IllegalStateException("Not connected!");
                if (appId == null) throw new IllegalStateException("App not selected!");

                componentId = line.split("\\s")[1].trim();
            } else if (line.equals("destroyState")) {
                if (client == null) throw new IllegalStateException("Not connected!");

                out.println(client.destroyState());
            } else if (line.equals("delete")) {
                if (client == null) throw new IllegalStateException("Not connected!");
                if (appId == null) throw new IllegalStateException("App not selected!");

                out.println(client.deleteApp(appId));
            } else if (line.startsWith("add")) {
                if (client == null) throw new IllegalStateException("Not connected!");
                if (appId == null) throw new IllegalStateException("App not selected!");

                out.println(client.addComponent(appId, line.substring(4)));
            } else if (line.startsWith("listenTo")) {
                if (client == null) throw new IllegalStateException("Not connected!");
                if (appId == null) throw new IllegalStateException("App not selected!");
                if (componentId == null) throw new IllegalStateException("Component not selected!");

                out.println(client.listenTo(appId, componentId, line.substring(9)));
            } else if (line.startsWith("stopListening")) {
                if (client == null) throw new IllegalStateException("Not connected!");

                out.println(client.stopListening(line.substring(14)));
            } else if (line.equals("start")) {
                if (client == null) throw new IllegalStateException("Not connected!");
                if (appId == null) throw new IllegalStateException("App not selected!");
                if (componentId == null) throw new IllegalStateException("Component not selected!");

                out.println(client.startComponent(appId, componentId));
            } else if (line.equals("stop")) {
                if (client == null) throw new IllegalStateException("Not connected!");
                if (appId == null) throw new IllegalStateException("App not selected!");
                if (componentId == null) throw new IllegalStateException("Component not selected!");

                out.println(client.stopComponent(appId, componentId));
            } else if (line.startsWith("set")) {
                if (client == null) throw new IllegalStateException("Not connected!");
                if (appId == null) throw new IllegalStateException("App not selected!");

                String[] split = line.split("\\s");
                out.println(client.setConfig(appId, split[1], split[2]));
            } else if (line.equals("get")) {
                if (client == null) throw new IllegalStateException("Not connected!");
                if (appId == null) throw new IllegalStateException("App not selected!");

                out.println(client.getConfig(appId));
            } else if (line.startsWith("jar")) {
                if (client == null) throw new IllegalStateException("Not connected!");
                if (appId == null) throw new IllegalStateException("App not selected!");

                out.println(client.uploadApp(appId, line.substring(4)));
            } else if (line.startsWith("data")) {
                if (client == null) throw new IllegalStateException("Not connected!");
                if (appId == null) throw new IllegalStateException("App not selected!");

                out.println(client.uploadData(appId, line.substring(5)));
            } else {
                out.println("Unknown command: " + line);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            handle(System.in, System.in, System.out);
        } else {
            handle(new FileInputStream(args[0]), System.in, System.out);
        }
    }
}
