/*
 * Copyright (C) CERN 2013 - European Laboratory for Particle Physics
 * All Rights Reserved.
 *
 * Authors:
 *   Dmitry Kekelidze (dmitry.kekelidze@cern.ch)
 *   Gerardo Lastra (gerardo.lastra@cern.ch)
 */
package cern.ais.gridwars;

import cern.ais.gridwars.api.bot.PlayerBot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;


final class Player {

    private final int id;
    private final PlayerBot playerBot;
    private final BotPrintWriter botOutputPrintWriter;
    private final int colorIndex;

    Player(int id, PlayerBot playerBot, File outputFile, int colorIndex) throws FileNotFoundException {
        this.id = id;
        this.playerBot = Objects.requireNonNull(playerBot);
        this.botOutputPrintWriter = createBotPrintWriter(outputFile);
        this.colorIndex = colorIndex;
    }

    private BotPrintWriter createBotPrintWriter(File outputFile) throws FileNotFoundException {
        return (outputFile != null)
            ? new BotPrintWriter(outputFile, GameConstants.BOT_PRINT_OUTPUT_BYTE_LIMIT, true)
            : null;
    }

    int getId() {
        return id;
    }

    PlayerBot getPlayerBot() {
        return playerBot;
    }

    BotPrintWriter getBotOutputPrintWriter() {
        return botOutputPrintWriter;
    }

    boolean hasBotOutputPrintWriter() {
        return botOutputPrintWriter != null;
    }

    int getColorIndex() {
        return colorIndex;
    }

    void dispose() {
        if (hasBotOutputPrintWriter()) {
            botOutputPrintWriter.close();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        return id == player.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Player[" + id + "]";
    }
}
