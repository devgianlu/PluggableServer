package com.gianlu.pluggableserver.client.commands;

import com.gianlu.pluggableserver.client.Client;
import com.gianlu.pluggableserver.client.Main;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author Gianlu
 */
public class ConnectCommand extends AbsCommand {

    ConnectCommand() {
        super("connect");
    }

    @Override
    public void handleImpl(@NotNull Main main, @NotNull String[] args) throws IOException {
        Client client = new Client(args[0]);
        main.println(client.connect());
        main.setClient(client);
    }
}
