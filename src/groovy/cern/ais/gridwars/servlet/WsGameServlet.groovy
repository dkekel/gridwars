/*
 * Copyright (C) CERN 2013 - European Laboratory for Particle Physics
 * All Rights Reserved.
 *
 * Authors:
 *   Dmitry Kekelidze (dmitry.kekelidze@cern.ch)
 *   Gerardo Lastra (gerardo.lastra@cern.ch)
 */

package cern.ais.gridwars.servlet

import cern.ais.gridwars.DataSendService
import cern.ais.gridwars.Game
import org.apache.catalina.websocket.MessageInbound
import org.apache.catalina.websocket.StreamInbound
import org.apache.catalina.websocket.WebSocketServlet
import org.apache.catalina.websocket.WsOutbound
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.support.WebApplicationContextUtils

import javax.servlet.ServletConfig
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import java.nio.ByteBuffer
import java.nio.CharBuffer

public class WsGameServlet extends WebSocketServlet
{
  DataSendService dataSendService;

  @Override
  void init(ServletConfig config) throws ServletException
  {
    super.init(config)
    WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext())
    GrailsApplication app = (GrailsApplication) ctx.getBean("grailsApplication")
    dataSendService = app.mainContext.dataSendService
  }

  @Override
  protected StreamInbound createWebSocketInbound(String subProtocol, HttpServletRequest request)
  {
    return new MyMessageInbound();
  }

  private class MyMessageInbound extends MessageInbound
  {
    WsOutbound wsOutbound;
    Game game;

    @Override
    public void onOpen(WsOutbound outbound)
    {
      System.out.println("Open Client.");
      wsOutbound = outbound;
    }

    @Override
    public void onClose(int status)
    {
      System.out.println("Close Client.");
    }

    @Override
    public void onTextMessage(CharBuffer cb) throws IOException
    {
      // Request for a game
      Long id = Long.valueOf(cb.toString())

      if (id > 0)
      {
        dataSendService.sendGameToClient(id, wsOutbound)
        println("Sending data to client")
      }
      else
        println("Send game failed. id < 0")

      wsOutbound.close(0, null);
    }

    @Override
    public void onBinaryMessage(ByteBuffer bb) throws IOException
    {
    }
  }
}
