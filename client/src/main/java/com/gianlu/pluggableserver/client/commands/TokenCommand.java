package com.gianlu.pluggableserver.client.commands;

import com.gianlu.pluggableserver.client.Client;
import com.gianlu.pluggableserver.client.Main;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author Gianlu
 */
public class TokenCommand extends AbsCommandWithClient {
    TokenCommand() {
        super("token");
    }

    @NotNull
    @Override
    protected String handleImpl(@NotNull Main main, @NotNull String[] args, @NotNull Client client) throws IOException {
        main.println(client.requestToken());
        main.print("Enter token: ");
        String token = main.waitForToken();
        client.setToken(token);
        return token;
    }
}
