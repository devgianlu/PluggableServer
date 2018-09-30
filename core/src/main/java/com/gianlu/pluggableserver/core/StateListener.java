package com.gianlu.pluggableserver.core;

import com.google.gson.JsonArray;
import org.jetbrains.annotations.Nullable;

/**
 * @author Gianlu
 */
public interface StateListener {
    void saveState();

    void destroyState();

    @Nullable
    JsonArray readStateJson();

    boolean uploadToCloud();
}
