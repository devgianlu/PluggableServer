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
        String domain = null;

        while (true) {
            out.print("> ");
            String line = scanner.nextLine();
            if (line.isEmpty()) continue;

            out.println(line);

            if (line.equals("quit")) {
                if (domain != null) domain = null;
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
            } else if (line.startsWith("domain")) {
                if (client == null) throw new IllegalStateException("Not connected!");

                domain = line.split("\\s")[1].trim();
            } else if (line.equals("destroyState")) {
                if (client == null) throw new IllegalStateException("Not connected!");

                out.println(client.destroyState());
            } else if (line.equals("start")) {
                if (client == null) throw new IllegalStateException("Not connected!");
                if (domain == null) throw new IllegalStateException("Domain not selected!");

                out.println(client.start(domain));
            } else if (line.equals("stop")) {
                if (client == null) throw new IllegalStateException("Not connected!");
                if (domain == null) throw new IllegalStateException("Domain not selected!");

                out.println(client.stop(domain));
            } else if (line.startsWith("set")) {
                if (client == null) throw new IllegalStateException("Not connected!");
                if (domain == null) throw new IllegalStateException("Domain not selected!");

                String[] split = line.split("\\s");
                out.println(client.setConfig(domain, split[1], split[2]));
            } else if (line.equals("get")) {
                if (client == null) throw new IllegalStateException("Not connected!");
                if (domain == null) throw new IllegalStateException("Domain not selected!");

                out.println(client.getConfig(domain));
            } else if (line.startsWith("jar")) {
                if (client == null) throw new IllegalStateException("Not connected!");
                if (domain == null) throw new IllegalStateException("Domain not selected!");

                out.println(client.uploadComponent(domain, line.substring(4)));
            } else if (line.startsWith("data")) {
                if (client == null) throw new IllegalStateException("Not connected!");
                if (domain == null) throw new IllegalStateException("Domain not selected!");

                out.println(client.uploadData(domain, line.substring(5)));
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
