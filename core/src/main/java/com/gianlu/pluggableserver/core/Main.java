package com.gianlu.pluggableserver.core;

import java.io.IOException;

/**
 * @author Gianlu
 */
public class Main {
    public static void main(String[] args) throws IOException {
        new Core(args.length > 0 ? args[0] : null, args.length > 1 ? args[1] : null).start();
    }
}
