package com.gianlu.pluggableserver.client.commands;

import com.gianlu.pluggableserver.client.Client;
import com.gianlu.pluggableserver.client.Main;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author Gianlu
 */
public class ComponentListenToCommand extends AbsCommandWithClientAndAppAndComponent {
    ComponentListenToCommand() {
        super("listenTo");
    }

    @NotNull
    @Override
    protected String handleImplWithAppAndComponent(@NotNull Main main, @NotNull String[] args, @NotNull Client client) throws IOException {
        return client.listenTo(args[0]);
    }
}
