package controllers;

import com.google.gson.Gson;
import data.GameBoardDao;
import io.javalin.Javalin;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Queue;
import models.GameBoard;
import models.Message;
import models.Move;
import models.Player;
import models.error.InvalidMoveException;
import org.eclipse.jetty.websocket.api.Session;

public class PlayGame {

  private static final int PORT_NUMBER = 8080;

  private static Javalin app;

  private static GameBoardDao gameBoardDao = new GameBoardDao();

  private PlayGame() {
  }

  /** Main method of the application.
   * @param args Command line arguments
   */
  public static void main(final String[] args) throws Exception {
    gameBoardDao.connect();

    final Gson gson = new Gson();

    GameBoard maybeBoard = gameBoardDao.getGameBoard();
    final GameBoard board = maybeBoard == null ? new GameBoard() : maybeBoard;

    app = Javalin.create(config -> {
      config.addStaticFiles("/public");
    }).start(PORT_NUMBER);

    // Test Echo Server
    app.post("/echo", ctx -> {
      ctx.result(ctx.body());
    });

    app.get("/newgame", ctx -> {
      ctx.redirect("/tictactoe.html");
    });

    app.post("/startgame", ctx -> {
      String type = ctx.formParam("type");
      char c;
      if (type.equals("X")) {
        c = 'X';
      } else if (type.equals("O")) {
        c = 'O';
      } else {
        ctx.status(400);
        return;
      }

      board.newGame();
      gameBoardDao.reset();

      board.setPlayer1(new Player(c, 1));
      ctx.result(gson.toJson(board));
      gameBoardDao.saveGameBoard(board);
    });

    app.get("/joingame", ctx -> {
      Player player1 = board.getPlayer1();
      if (player1 == null) {
        ctx.status(412);
        ctx.result("Player 1 not joined yet");
        return;
      }

      char c;
      if (player1.getType() == 'X') {
        c = 'O';
      } else {
        c = 'X';
      }

      board.setPlayer2(new Player(c, 2));
      board.startGame();

      ctx.redirect("/tictactoe.html?p=2");

      sendGameBoardToAllPlayers(gson.toJson(board));
      gameBoardDao.saveGameBoard(board);
    });

    app.post("/move/:playerId", ctx -> {
      int playerId = Integer.parseInt(ctx.pathParam("playerId"));
      int x = Integer.parseInt(ctx.formParam("x"));
      int y = Integer.parseInt(ctx.formParam("y"));

      Player player = null;
      if (playerId == board.getPlayer1().getId()) {
        player = board.getPlayer1();
      } else if (playerId == board.getPlayer2().getId()) {
        player = board.getPlayer2();
      } else {
        ctx.status(400);
        ctx.result("Invalid player id");
        return;
      }

      try {
        board.move(new Move(player, x, y));
        ctx.result(gson.toJson(new Message(true, 100, "")));
      } catch (InvalidMoveException e) {
        ctx.result(gson.toJson(new Message(false, e.code(), e.cause())));
      }

      sendGameBoardToAllPlayers(gson.toJson(board));
      gameBoardDao.saveGameBoard(board);
    });

    // Web sockets - DO NOT DELETE or CHANGE
    app.ws("/gameboard", new UiWebSocket());
  }

  /** Send message to all players.
   * @param gameBoardJson Gameboard JSON
   * @throws IOException Websocket message send IO Exception
   */
  private static void sendGameBoardToAllPlayers(final String gameBoardJson) throws IOException {
    Queue<Session> sessions = UiWebSocket.getSessions();
    for (Session sessionPlayer : sessions) {
      sessionPlayer.getRemote().sendString(gameBoardJson);
    }
  }

  /**
   * Close the application.
   */
  public static void stop() throws SQLException {
    app.stop();
    gameBoardDao.close();
  }
}
