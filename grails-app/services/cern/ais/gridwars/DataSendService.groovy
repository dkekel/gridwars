package cern.ais.gridwars

import org.apache.catalina.websocket.WsOutbound

import java.nio.ByteBuffer
import java.nio.CharBuffer

class DataSendService
{
  def gameSerializationService
  public void sendGameToClient(Long gameId, WsOutbound wsOutbound)
  {
    for (turn in gameSerializationService.load(gameId))
      sendTurnToClient(turn, wsOutbound)
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

  private static void sendTurnToClient(byte[] turn, WsOutbound wsOutbound)
  {
    wsOutbound.writeBinaryMessage(ByteBuffer.wrap(turn));
  }
}
