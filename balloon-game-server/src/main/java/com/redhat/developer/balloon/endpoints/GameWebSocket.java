package com.redhat.developer.balloon.endpoints;

import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import com.redhat.developer.balloon.services.GameService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ServerEndpoint("/game") // for the mobile game
@ApplicationScoped
public class GameWebSocket {
  private static final Logger LOG =
      Logger.getLogger(GameWebSocket.class.getName());

  GameService gameService;

  // @Inject
  // @Channel("popstream")
  // Emitter<String> popstream;

  @ConfigProperty(name = "kafkaprops", defaultValue = "false")
  boolean kafkaSend;

  public GameWebSocket(GameService gameService) {
    this.gameService = gameService;
  }

  @OnOpen
  public void onOpen(Session session) {
    LOG.info("onOpen");
    LOG.info("id: " + session.getId());
    gameService.addSession(session.getId(), session);
  }

  @OnClose
  public void onClose(Session session) {
    LOG.info("onClose");
    gameService.removeSession(session.getId());
  }

  @OnError
  public void onError(Session session, Throwable throwable) {
    LOG.log(Level.SEVERE, "onError", throwable);
  }

  @OnMessage // from client to server
  public void onMessage(String message, Session session) {
    if (message == null)
      return;

    JsonReader jsonReader = Json.createReader(new StringReader(message));
    JsonObject jsonMessage = jsonReader.readObject();
    // LOG.info("jsonMessage: " + jsonMessage);
    String msgtype = jsonMessage.getString("type");
    // LOG.info("msgtype: " + msgtype);

    if (msgtype != null && !msgtype.equals("")) {
      if (msgtype.equals("register")) {
        gameService.registerClient(jsonMessage, session);
      } else if (msgtype.equals("score")) {
        var scoreMessage = gameService.score(jsonMessage);
        if (kafkaSend) {
          // popstream.send(scoreMessage);
        }
      }
    }

  } // OnMessage


}
