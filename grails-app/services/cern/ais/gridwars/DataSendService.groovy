package cern.ais.gridwars

import org.apache.catalina.websocket.WsOutbound

import java.nio.ByteBuffer
import java.nio.CharBuffer

class DataSendService
{

  public void sendGameToClient(Long gameId, WsOutbound wsOutbound)
  {
    def match = Match.get(gameId)
    for (Turn turn : match.players.turns.flatten().sort { it.number })
    {
      sendTurnToClient(turn, wsOutbound)
    }
  }

  private static void sendTurnToClient(Turn turn, WsOutbound wsOutbound)
  {
//    sendListOfCommandsToClient(turn.status, wsOutbound)
    sendBinaryGameStatusToClient(turn.status, wsOutbound)
  }

  private static void sendListOfCommandsToClient(GameStatus gameStatus, WsOutbound wsOutbound)
  {
    wsOutbound.writeTextMessage(CharBuffer.wrap(gameStatus.content.toCharArray()))
  }

  private static void sendBinaryGameStatusToClient(GameStatus gameStatus, WsOutbound wsOutbound)
  {
    ByteBuffer byteBuffer = ByteBuffer.allocate(GameConstants.UNIVERSE_SIZE * GameConstants.UNIVERSE_SIZE * 4);
    byteBuffer.put(gameStatus.imageData)
    wsOutbound.writeBinaryMessage(byteBuffer);
  }

}
