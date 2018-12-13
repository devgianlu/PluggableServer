package com.gianlu.pluggableserver.client.commands;

import com.gianlu.pluggableserver.client.Client;
import com.gianlu.pluggableserver.client.Main;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author Gianlu
 */
public class AddRedirectCommand extends AbsCommandWithClient {
    AddRedirectCommand() {
        super("addRedirect");
    }

    @NotNull
    @Override
    protected String handleImpl(@NotNull Main main, @NotNull String[] args, @NotNull Client client) throws IOException {
        return client.addRedirect(args[0], args[1], args[2]);
    }
}
