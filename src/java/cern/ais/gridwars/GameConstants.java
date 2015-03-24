/*
 * Copyright (C) CERN 2013 - European Laboratory for Particle Physics
 * All Rights Reserved.
 *
 * Authors:
 *   Dmitry Kekelidze (dmitry.kekelidze@cern.ch)
 *   Gerardo Lastra (gerardo.lastra@cern.ch)
 */

package cern.ais.gridwars;

public class GameConstants
{
  public static final int    UNIVERSE_SIZE                     = 50;
  public static final Double GROWTH_RATE                       = 1.10;
  public static final Long   MAXIMUM_POPULATION                = 100L;
  public static final Long   STARTING_POPULATION               = 100L;
  public static final int    TIMEOUT_DURATION_MS               = 50;
  public static final int    INSTANTIATION_TIMEOUT_DURATION_MS = 5000;
  public static final int    CLASS_LOAD_TIMEOUT_DURATION_MS    = 10000;
  public static final int    TURN_LIMIT                        = 200;
  public static final int    MAXIMUM_GAMES_PER_OPPONENT        = 9;

  public static final byte[][] PLAYER_COLORS = {
          {(byte) 255, 0, 0}, // Red
          {0, 0, (byte) 255}, // Blue
          {0, (byte) 255, 0}, // Green
          {(byte) 170, (byte) 86, 0}, // Orange
          {(byte) 102, (byte) 102, (byte) 51}, // Brown
          {(byte) 128, 0, (byte) 128}, // Purple
          {0, (byte) 128, (byte) 128}, // Cyan
          {0, 0, 0} // Black
  };

  public static String getRGB(int playerNumber)
  {
    return "rgb(" + ((int) PLAYER_COLORS[playerNumber][0] & 0xff) + "," + ((int) PLAYER_COLORS[playerNumber][1] & 0xff) + "," + ((int) PLAYER_COLORS[playerNumber][2] & 0xff) + ")";
  }
}
