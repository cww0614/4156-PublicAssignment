package test;

import com.google.gson.Gson;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import models.GameBoard;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

@WebSocket
public class BoardReceiver {
  private static final Gson gson = new Gson();

  private GameBoard board;
  private WebSocketClient client;
  private CountDownLatch connectLatch;
  private CountDownLatch messageLatch;

  /**
   * Construct a websocket client that receives the latest gameboard.
   * @param dest the websocket endpoint
   * @param moveCount how many moves are expected, this client will wait until such number 
   *     of gameboard update is received.
   */
  public BoardReceiver(String dest, int moveCount) throws Exception {
    connectLatch = new CountDownLatch(1);
    messageLatch = new CountDownLatch(moveCount);
    client = new WebSocketClient();

    client.start();
    client.connect(this, new URI(dest), new ClientUpgradeRequest());
    connectLatch.await();
  }

  public GameBoard getBoard() {
    return board;
  }
  
  public void await() throws InterruptedException {
    messageLatch.await();
  }

  public void stop() throws Exception {
    client.stop();
  }

  @OnWebSocketConnect
  public void onConnect(Session session) {
    connectLatch.countDown();
  }

  @OnWebSocketMessage
  public void onMessage(String msg) {
    this.board = gson.fromJson(msg, GameBoard.class);
    messageLatch.countDown();
  }
}
