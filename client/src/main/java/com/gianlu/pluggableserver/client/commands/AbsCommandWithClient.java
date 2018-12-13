package com.gianlu.pluggableserver.client.commands;

import com.gianlu.pluggableserver.client.Client;
import com.gianlu.pluggableserver.client.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * @author Gianlu
 */
public abstract class AbsCommandWithClient extends AbsCommand {

    public AbsCommandWithClient(@NotNull String cmd) {
        super(cmd);
    }

    @Override
    protected final void handleImpl(@NotNull Main main, @NotNull String[] args) throws IOException {
        Client client = main.getClient();
        if (client == null) {
            main.error("Not connected!");
            return;
        }

        if (client.hasToken() || Commands.isAllowedNoToken(this)) {
            String result = handleImpl(main, args, client);
            if (result != null) main.println(result);
        } else {
            main.error("Missing token!");
        }
    }

    @Nullable
    protected abstract String handleImpl(@NotNull Main main, @NotNull String[] args, @NotNull Client client) throws IOException;
}
