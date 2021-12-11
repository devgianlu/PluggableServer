package com.gianlu.pluggableserver.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Gianlu
 */
public class TokenHolder {
    private static final Logger LOGGER = LogManager.getLogger(TokenHolder.class);
    private static TokenHolder instance;
    private final AtomicReference<String> token = new AtomicReference<>();
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    private TokenHolder() {
    }

    @NotNull
    public static TokenHolder get() {
        if (instance == null) instance = new TokenHolder();
        return instance;
    }

    public synchronized void generate() {
        if (token.get() == null) {
            token.set(UUID.randomUUID().toString());
            executorService.schedule(() -> {
                synchronized (TokenHolder.this) {
                    token.set(null);
                    LOGGER.info("Token destroyed.");
                }
            }, 30, TimeUnit.MINUTES);
        }

        LOGGER.info("Token is: " + token.get());
    }

    public synchronized boolean checkToken(@Nullable String given) {
        if (given == null) return false;
        else return Objects.equals(token.get(), given);
    }
}
