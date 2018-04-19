/*
 * Copyright (C) CERN 2013 - European Laboratory for Particle Physics
 * All Rights Reserved.
 *
 * Authors:
 *   Dmitry Kekelidze (dmitry.kekelidze@cern.ch)
 *   Gerardo Lastra (gerardo.lastra@cern.ch)
 */
package cern.ais.gridwars;

import java.util.concurrent.TimeUnit;


final class GameConstants {

    static final long MATCH_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(60);
    static final int UNIVERSE_SIZE = 50;
    static final double GROWTH_RATE = 1.10;
    static final int MAXIMUM_POPULATION = 100;
    static final int STARTING_POPULATION = 100;
    static final int TURN_TIMEOUT_MS = 50;
    static final long BOT_INSTANTIATION_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(5);
    static final int TURN_LIMIT = 2000;

    static final int BOT_PRINT_OUTPUT_BYTE_LIMIT = 1024 * 1024 * 5; // 5 MB

    static final byte[][] PLAYER_COLORS = {
        {(byte) 255, 0, 0}, // Red
        {0, 0, (byte) 255}, // Blue
        {0, (byte) 255, 0}, // Green
        {(byte) 170, (byte) 86, 0}, // Orange
        {(byte) 102, (byte) 102, (byte) 51}, // Brown
        {(byte) 128, 0, (byte) 128}, // Purple
        {0, (byte) 128, (byte) 128}, // Cyan
        {0, 0, 0} // Black
    };

    private GameConstants() {
    }
}
