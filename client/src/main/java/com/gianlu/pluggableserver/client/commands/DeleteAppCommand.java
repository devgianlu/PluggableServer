package com.gianlu.pluggableserver.client.commands;

import com.gianlu.pluggableserver.client.Client;
import com.gianlu.pluggableserver.client.Main;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author Gianlu
 */
public class DeleteAppCommand extends AbsCommandWithClientAndApp {
    DeleteAppCommand() {
        super("deleteApp");
    }

    @NotNull
    @Override
    protected String handleImplWithApp(@NotNull Main main, @NotNull String[] args, @NotNull Client client) throws IOException {
        return client.deleteApp();
    }
}
