package com.gianlu.pluggableserver.client.commands;

import com.gianlu.pluggableserver.client.Main;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Gianlu
 */
public final class Commands {
    private static final Set<AbsCommand> COMMANDS = new HashSet<>();

    static {
        COMMANDS.add(new AddComponentCommand());
        COMMANDS.add(new AddRedirectCommand());
        COMMANDS.add(new ComponentListenToCommand());
        COMMANDS.add(new ComponentStopListeningCommand());
        COMMANDS.add(new ConnectCommand());
        COMMANDS.add(new DeleteAppCommand());
        COMMANDS.add(new DestroyStateCommand());
        COMMANDS.add(new GetAppConfigCommand());
        COMMANDS.add(new GetStateCommand());
        COMMANDS.add(new ListCommand());
        COMMANDS.add(new MaintenanceOnCommand());
        COMMANDS.add(new QuitCommand());
        COMMANDS.add(new RemoveRedirectCommand());
        COMMANDS.add(new SetAppConfigCommand());
        COMMANDS.add(new SetAppIdCommand());
        COMMANDS.add(new SetComponentIdCommand());
        COMMANDS.add(new StartComponentCommand());
        COMMANDS.add(new StopComponentCommand());
        COMMANDS.add(new TokenCommand());
        COMMANDS.add(new UploadAppCommand());
        COMMANDS.add(new UploadDataCommand());
        COMMANDS.add(new UploadToCloudCommand());
        COMMANDS.add(new SystemCommand());
    }

    private Commands() {
    }

    public static boolean isAllowedNoToken(@NotNull AbsCommand cmd) {
        return cmd.cmd.equals("token");
    }

    public static void handle(@NotNull Main main, @NotNull String line) throws IOException {
        String[] split = line.split("\\s");
        String cmd = split[0];

        for (AbsCommand abs : COMMANDS) {
            if (abs.cmd.equals(cmd)) {
                abs.handle(main, line);
                return;
            }
        }

        System.out.println("Unknown command: " + line);
    }
}
