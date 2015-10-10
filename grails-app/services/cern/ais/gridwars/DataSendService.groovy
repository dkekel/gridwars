package cern.ais.gridwars

import org.apache.catalina.websocket.WsOutbound

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream

class DataSendService
{
  def gameSerializationService
  public void sendGameToClient(Long gameId, WsOutbound wsOutbound)
  {
	  def bytes = gameSerializationService.load(gameId)
    log.info("Sending Game: $gameId. Size ${ bytes.size() / 1024 } KB.");

    wsOutbound.writeBinaryMessage(ByteBuffer.wrap(bytes));
  }
}
