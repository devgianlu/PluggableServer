package com.gianlu.pluggableserver.client.commands;

import com.gianlu.pluggableserver.client.Client;
import com.gianlu.pluggableserver.client.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * @author Gianlu
 */
public class SystemCommand extends AbsCommandWithClient {

    public SystemCommand() {
        super("system");
    }

    @Override
    protected @Nullable String handleImpl(@NotNull Main main, @NotNull String[] args, @NotNull Client client) throws IOException {
        return client.system();
    }
}
