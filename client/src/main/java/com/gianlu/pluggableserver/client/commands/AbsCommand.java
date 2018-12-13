package com.gianlu.pluggableserver.client.commands;

import com.gianlu.pluggableserver.client.Main;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Gianlu
 */
public abstract class AbsCommand {
    private static final Pattern SPLIT_PATTERN = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
    final String cmd;

    AbsCommand(@NotNull String cmd) {
        this.cmd = cmd;
    }

    final void handle(@NotNull Main main, @NotNull String cmdLine) throws IOException {
        List<String> matches = new ArrayList<>();
        Matcher matcher = SPLIT_PATTERN.matcher(cmdLine);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                matches.add(matcher.group(1));
            } else if (matcher.group(2) != null) {
                matches.add(matcher.group(2));
            } else {
                matches.add(matcher.group());
            }
        }

        handleImpl(main, Arrays.copyOfRange(matches.toArray(new String[0]), 1, matches.size()));
    }

    protected abstract void handleImpl(@NotNull Main main, @NotNull String[] args) throws IOException;
}
