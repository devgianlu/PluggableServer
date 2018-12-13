package com.gianlu.pluggableserver.client.commands;

import com.gianlu.pluggableserver.client.Client;
import com.gianlu.pluggableserver.client.Main;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gianlu
 */
public class SetAppIdCommand extends AbsCommandWithClient {
    SetAppIdCommand() {
        super("setAppId");
    }

    @NotNull
    @Override
    protected String handleImpl(@NotNull Main main, @NotNull String[] args, @NotNull Client client) {
        client.setAppId(args[0]);
        return String.format("APP ID: %s, COMPONENT ID: %s", client.getAppId(), client.getComponentId());
    }
}
