package com.gianlu.pluggableserver.client;

import com.gianlu.pluggableserver.client.commands.Commands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * @author Gianlu
 */
public class Main {
    private final InputStream in;
    private final Scanner tokenIn;
    private final PrintStream out;
    private final PrintStream err;
    private Client client;
    private volatile boolean running = true;

    public Main(@NotNull InputStream in, @NotNull InputStream tokenIn, @NotNull PrintStream out, @NotNull PrintStream err) {
        this.in = in;
        this.tokenIn = new Scanner(tokenIn);
        this.out = out;
        this.err = err;
    }

    public static void main(String[] args) throws IOException {
        Main main;
        if (args.length == 0) main = new Main(System.in, System.in, System.out, System.err);
        else main = new Main(new FileInputStream(args[0]), System.in, System.out, System.err);

        Scanner scanner = new Scanner(main.in);
        PrintStream out = main.out;

        while (main.running) {
            out.print("> ");

            String line;
            if (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (line.isEmpty()) continue;
            } else {
                break;
            }

            out.println(line);
            if (!Commands.handle(main, line))
                out.println("UNKNOWN COMMAND: " + line);
        }

        out.println("EXITING.");
    }

    public void println(@NotNull String str) {
        out.println(str);
    }

    public void print(@NotNull String str) {
        out.print(str);
    }

    @Nullable
    public Client getClient() {
        return client;
    }

    public void setClient(@NotNull Client client) {
        this.client = client;
    }

    public void error(@NotNull String str) {
        err.println(str);
    }

    @NotNull
    public String waitForToken() {
        if (tokenIn.hasNextLine()) return tokenIn.nextLine();
        else throw new IllegalStateException("Missing token!");
    }

    public void exit() {
        running = false;
    }
}
