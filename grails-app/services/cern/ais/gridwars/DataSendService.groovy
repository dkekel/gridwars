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

  private static void sendTurnToClient(byte[] turn, WsOutbound wsOutbound)
  {
    wsOutbound.writeBinaryMessage(ByteBuffer.wrap(turn));
  }
}
