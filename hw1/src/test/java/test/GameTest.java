package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.google.gson.Gson;
import controllers.PlayGame;
import java.io.File;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import models.GameBoard;
import models.Message;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;


@TestMethodOrder(OrderAnnotation.class)
public class GameTest {
  private static final Gson gson = new Gson();
  
  private static Message testMove(int player, int x, int y) {
    HttpResponse<String> response = Unirest
        .post("http://localhost:8080/move/" + player)
        .body("x=" + x +  "&y=" + y)
        .asString();
    
    return gson.fromJson(response.getBody(), Message.class);
  }
  
  private static GameBoard testStartGame(char type) {
    HttpResponse<String> response = Unirest.post("http://localhost:8080/startgame").body("type=" + type).asString();
    assertEquals(response.getStatus(), 200);
    return gson.fromJson(response.getBody(), GameBoard.class);
  }


  private static void testJoinGame() {
    HttpResponse<String> response = Unirest.get("http://localhost:8080/joingame").asString();
    assertEquals(response.getStatus(), 200);
  }

  /**
   * Start the server before any test gets executed.
   */
  @BeforeAll
  public static void init() throws Exception {
    PlayGame.main(new String[] {});
  }

  /**
   * This method starts a new game before every test run. It will run every time
   * before a test.
   */
  @BeforeEach
  public void startNewGame() {
    HttpResponse<String> response = Unirest.get("http://localhost:8080/newgame").asString();
    assertEquals(response.getStatus(), 200);
  }
  
  @Test
  public void testEcho() {
    HttpResponse<String> response = Unirest.post("http://localhost:8080/echo").body("Hello").asString();
    assertEquals("Hello", response.getBody());
  }

  /**
   * Test the startgame endpoint with character X.
   */
  @Test
  public void startGameXTest() {
    GameBoard gameBoard = testStartGame('X');
    assertEquals(false, gameBoard.isGameStarted());
    assertEquals('X', gameBoard.getPlayer1().getType());
  }

  /**
   * Test the startgame endpoint with character X.
   */
  @Test
  public void startGameOTest() {
    GameBoard gameBoard = testStartGame('O');
    assertEquals(false, gameBoard.isGameStarted());
    assertEquals('O', gameBoard.getPlayer1().getType());
  }

  /**
   * Test the startgame endpoint with character X.
   */
  @Test
  public void startGameWithInvalidCharacterTest() {
    HttpResponse<String> response = Unirest.post("http://localhost:8080/startgame").body("type=A").asString();
    assertNotEquals(response.getStatus(), 200);
  }
  
  @Test
  public void testPlayer2Join() {
    testStartGame('O');
    HttpResponse<String> response = Unirest.get("http://localhost:8080/joingame").asString();
    assertEquals(response.getStatus(), 200);
  }
  
  /**
   * Test that a player cannot make a move until both player have joined the game.
   */
  @Test
  public void playerCannotMoveUntilBothJoined() {
    testStartGame('X');
    assertEquals(false, testMove(1, 0, 0).isValid());
  }

  @Test
  public void player2CannotJoinUntilPlayer1Joined() {
    HttpResponse<String> response = Unirest.get("http://localhost:8080/joingame").asString();
    assertNotEquals(response.getStatus(), 200);
  }

  @Test
  public void playerOneAlwaysMoveFirst() {
    testStartGame('X');
    testJoinGame();
    
    assertEquals(false, testMove(2, 0, 0).isValid());
  }
  
  @Test
  public void playerCannotMakeTwoMovesInOneTurn() {
    testStartGame('X');
    testJoinGame();
    
    assertEquals(true, testMove(1, 0, 0).isValid());
    assertEquals(false, testMove(1, 0, 1).isValid());
  }

  @Test
  public void cannotMoveToInvalidPosition() {
    testStartGame('X');
    testJoinGame();
    
    assertEquals(false, testMove(1, -1, 0).isValid());
    assertEquals(false, testMove(1, 0, -1).isValid());
    assertEquals(false, testMove(1, 4, 0).isValid());
    assertEquals(false, testMove(1, 0, 4).isValid());
  }

  @Test
  public void cannotMoveToOccpupiedPosition() {
    testStartGame('X');
    testJoinGame();
    
    assertEquals(true, testMove(1, 0, 0).isValid());
    assertEquals(false, testMove(2, 0, 0).isValid());
  }

  @Test
  public void cannotMoveWithInvalidId() {
    testStartGame('X');
    testJoinGame();
    

    HttpResponse<String> response = Unirest
        .post("http://localhost:8080/move/3")
        .body("x=0&y=0")
        .asString();

    assertNotEquals(200, response.getStatus());
  }
  
  @Test
  public void player1ShouldBeAbleToWin() throws Exception {
    testStartGame('O');
    testJoinGame();

    BoardReceiver receiver = new BoardReceiver("ws://localhost:8080/gameboard", 5);
    try {
      assertEquals(true, testMove(1, 0, 0).isValid());
      assertEquals(true, testMove(2, 2, 2).isValid());
      assertEquals(true, testMove(1, 0, 1).isValid());
      assertEquals(true, testMove(2, 1, 1).isValid());
      assertEquals(true, testMove(1, 0, 2).isValid());
      receiver.await();
      assertEquals(1, receiver.getBoard().getWinner());
    } finally {
      receiver.stop();
    }
  }

  @Test
  public void player2ShouldBeAbleToWin() throws Exception {
    testStartGame('O');
    testJoinGame();

    BoardReceiver receiver = new BoardReceiver("ws://localhost:8080/gameboard", 6);
    try {
      assertEquals(true, testMove(1, 0, 0).isValid());
      assertEquals(true, testMove(2, 0, 1).isValid());
      assertEquals(true, testMove(1, 0, 2).isValid());
      assertEquals(true, testMove(2, 1, 1).isValid());
      assertEquals(true, testMove(1, 1, 0).isValid());
      assertEquals(true, testMove(2, 2, 1).isValid());
      receiver.await();
      assertEquals(2, receiver.getBoard().getWinner());
    } finally {
      receiver.stop();
    }
  }

  @Test
  public void playerShouldBeAbleToWinDiag() throws Exception {
    testStartGame('O');
    testJoinGame();

    BoardReceiver receiver = new BoardReceiver("ws://localhost:8080/gameboard", 5);
    try {
      assertEquals(true, testMove(1, 0, 0).isValid());
      assertEquals(true, testMove(2, 0, 1).isValid());
      assertEquals(true, testMove(1, 1, 1).isValid());
      assertEquals(true, testMove(2, 0, 2).isValid());
      assertEquals(true, testMove(1, 2, 2).isValid());
      receiver.await();
      assertEquals(1, receiver.getBoard().getWinner());
    } finally {
      receiver.stop();
    }
  }

  @Test
  public void playerShouldBeAbleToWinDiag2() throws Exception {
    testStartGame('O');
    testJoinGame();

    BoardReceiver receiver = new BoardReceiver("ws://localhost:8080/gameboard", 5);
    try {
      assertEquals(true, testMove(1, 0, 2).isValid());
      assertEquals(true, testMove(2, 0, 0).isValid());
      assertEquals(true, testMove(1, 1, 1).isValid());
      assertEquals(true, testMove(2, 0, 1).isValid());
      assertEquals(true, testMove(1, 2, 0).isValid());
      receiver.await();
      assertEquals(1, receiver.getBoard().getWinner());
    } finally {
      receiver.stop();
    }
  }

  @Test
  public void gameCouldDraw() throws Exception {
    testStartGame('X');
    testJoinGame();

    BoardReceiver receiver = new BoardReceiver("ws://localhost:8080/gameboard", 9);
    try {
      assertEquals(true, testMove(1, 0, 0).isValid());
      assertEquals(true, testMove(2, 0, 2).isValid());
      assertEquals(true, testMove(1, 0, 1).isValid());
      assertEquals(true, testMove(2, 1, 0).isValid());
      assertEquals(true, testMove(1, 1, 2).isValid());
      assertEquals(true, testMove(2, 1, 1).isValid());
      assertEquals(true, testMove(1, 2, 0).isValid());
      assertEquals(true, testMove(2, 2, 1).isValid());
      assertEquals(true, testMove(1, 2, 2).isValid());
      receiver.await();
      assertEquals(true, receiver.getBoard().isDraw());
    } finally {
      receiver.stop();
    }
  }

  private static void restart() throws Exception {
    restart(false);
  }
  
  private static void restart(boolean clean) throws Exception {
    close();
    
    if (clean) {
      new File("data.db").delete();
    }
    
    init();
    
    // Wait until the server is up, unirest seems to retry by itself
    Unirest.get("http://localhost:8080/").asString();
  }
  
  @Test
  public void crashTest() throws Exception {
    // Delete the database to emulate a fresh start
    restart(true);
    // Need to manually start a new game here because we just deleted the database
    startNewGame();
    restart();
    testStartGame('O');
    restart();
    testJoinGame();

    restart();
    assertEquals(true, testMove(1, 0, 0).isValid());
    restart();
    assertEquals(true, testMove(2, 2, 2).isValid());
    restart();
    assertEquals(true, testMove(1, 0, 1).isValid());
    restart();
    assertEquals(true, testMove(2, 1, 1).isValid());
    restart();

    BoardReceiver receiver = new BoardReceiver("ws://localhost:8080/gameboard", 1);
    try {
      assertEquals(true, testMove(1, 0, 2).isValid());
      receiver.await();
      assertEquals(1, receiver.getBoard().getWinner());
    } finally {
      receiver.stop();
    }
  }

  /**
   * Stop the REST server after all tests finished.
   */
  @AfterAll
  public static void close() throws Exception {
    PlayGame.stop();
  }
}