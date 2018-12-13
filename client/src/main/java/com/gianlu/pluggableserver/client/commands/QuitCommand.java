package com.gianlu.pluggableserver.client.commands;

import com.gianlu.pluggableserver.client.Client;
import com.gianlu.pluggableserver.client.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Gianlu
 */
public class QuitCommand extends AbsCommandWithClient {
    QuitCommand() {
        super("quit");
    }

    @Nullable
    @Override
    protected String handleImpl(@NotNull Main main, @NotNull String[] args, @NotNull Client client) {
        if (client.hasComponentId()) {
            client.setComponentId(null);
            return String.format("APP ID: %s, COMPONENT ID: %s", client.getAppId(), client.getComponentId());
        } else if (client.hasAppId()) {
            client.setAppId(null);
            return String.format("APP ID: %s, COMPONENT ID: %s", client.getAppId(), client.getComponentId());
        } else {
            main.exit();
            return null;
        }
    }
}
