/*
 * Copyright (C) CERN 2013 - European Laboratory for Particle Physics
 * All Rights Reserved.
 *
 * Authors:
 *   Dmitry Kekelidze (dmitry.kekelidze@cern.ch)
 *   Gerardo Lastra (gerardo.lastra@cern.ch)
 */
package cern.ais.gridwars;

import cern.ais.gridwars.bot.PlayerBot;
import cern.ais.gridwars.util.BotPrintWriter;

import java.io.File;
import java.io.FileNotFoundException;


public class Player {

    private final int id;
    private final PlayerBot playerBot;
    private final BotPrintWriter botOutputPrintWriter;
    private final int colorIndex;

    public Player(int id, PlayerBot playerBot, File outputFile, int colorIndex) throws FileNotFoundException {
        this.id = id;
        this.playerBot = playerBot;
        this.botOutputPrintWriter = createBotPrintWriter(outputFile);
        this.colorIndex = colorIndex;
    }

    private BotPrintWriter createBotPrintWriter(File outputFile) throws FileNotFoundException {
        return (outputFile != null)
            ? new BotPrintWriter(outputFile, GameConstants.BOT_PRINT_OUPUT_BYTE_LIMIT, true)
            : null;
    }

    public int getId() {
        return id;
    }

    public PlayerBot getPlayerBot() {
        return playerBot;
    }

    public BotPrintWriter getBotOutputPrintWriter() {
        return botOutputPrintWriter;
    }

    public boolean hasBotOutputPrintWriter() {
        return botOutputPrintWriter != null;
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public void dispose() {
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
        return String.valueOf(id);
    }
}
