package com.gianlu.pluggableserver.core;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

/**
 * @author Gianlu
 */
public interface StateListener {
    void saveState();

    void destroyState();

    @Nullable
    JsonObject readStateJson();

    boolean uploadToCloud();
}
