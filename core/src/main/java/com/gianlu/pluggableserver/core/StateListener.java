package com.gianlu.pluggableserver.core;

/**
 * @author Gianlu
 */
public interface StateListener {
    void saveState();

    void destroyState();
}
