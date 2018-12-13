package com.gianlu.pluggableserver.client.commands;

import com.gianlu.pluggableserver.client.Client;
import com.gianlu.pluggableserver.client.Main;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gianlu
 */
public class SetComponentIdCommand extends AbsCommandWithClientAndApp {
    SetComponentIdCommand() {
        super("setComponentId");
    }

    @NotNull
    @Override
    protected String handleImplWithApp(@NotNull Main main, @NotNull String[] args, @NotNull Client client) {
        client.setComponentId(args[0]);
        return String.format("APP ID: %s, COMPONENT ID: %s", client.getAppId(), client.getComponentId());
    }
}
