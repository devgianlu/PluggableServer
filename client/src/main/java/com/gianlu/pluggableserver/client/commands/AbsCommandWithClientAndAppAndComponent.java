package com.gianlu.pluggableserver.client.commands;

import com.gianlu.pluggableserver.client.Client;
import com.gianlu.pluggableserver.client.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * @author Gianlu
 */
public abstract class AbsCommandWithClientAndAppAndComponent extends AbsCommandWithClientAndApp {
    AbsCommandWithClientAndAppAndComponent(@NotNull String cmd) {
        super(cmd);
    }

    @Nullable
    @Override
    protected final String handleImplWithApp(@NotNull Main main, @NotNull String[] args, @NotNull Client client) throws IOException {
        if (client.hasComponentId()) {
            return handleImplWithAppAndComponent(main, args, client);
        } else {
            main.error("Missing component ID!");
            return null;
        }
    }

    @NotNull
    protected abstract String handleImplWithAppAndComponent(@NotNull Main main, @NotNull String[] args, @NotNull Client client) throws IOException;
}
