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

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Objects;


final class Player {

    private final int id;
    private final PlayerBot playerBot;
    private final PrintStream botOutputPrintStream;

    Player(int id, PlayerBot playerBot, OutputStream botOutputStream) {
        this.id = id;
        this.playerBot = Objects.requireNonNull(playerBot);
        this.botOutputPrintStream = createBotPrintStream(botOutputStream);
    }

    private PrintStream createBotPrintStream(OutputStream botOutputStream) {
        return (botOutputStream != null)
            ? new PrintStream(botOutputStream)
            : null;
    }

    int getId() {
        return id;
    }

    PlayerBot getPlayerBot() {
        return playerBot;
    }

    PrintStream getBotOutputPrintStream() {
        return botOutputPrintStream;
    }

    boolean hasBotOutputPrintWriter() {
        return botOutputPrintStream != null;
    }

    void dispose() {
        if (hasBotOutputPrintWriter()) {
            botOutputPrintStream.close();
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
