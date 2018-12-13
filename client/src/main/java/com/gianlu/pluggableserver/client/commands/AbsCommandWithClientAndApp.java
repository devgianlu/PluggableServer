package com.gianlu.pluggableserver.client.commands;

import com.gianlu.pluggableserver.client.Client;
import com.gianlu.pluggableserver.client.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * @author Gianlu
 */
public abstract class AbsCommandWithClientAndApp extends AbsCommandWithClient {
    AbsCommandWithClientAndApp(@NotNull String cmd) {
        super(cmd);
    }

    @Nullable
    protected abstract String handleImplWithApp(@NotNull Main main, @NotNull String[] args, @NotNull Client client) throws IOException;

    @Nullable
    @Override
    protected final String handleImpl(@NotNull Main main, @NotNull String[] args, @NotNull Client client) throws IOException {
        if (client.hasAppId()) {
            return handleImplWithApp(main, args, client);
        } else {
            main.error("Missing app ID!");
            return null;
        }
    }
}
