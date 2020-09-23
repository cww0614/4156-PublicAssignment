package models;

import models.error.AlreadyOccupiedExcpetion;
import models.error.InvalidMoveException;
import models.error.InvalidPositionException;
import models.error.NotYourTurnException;

public class GameBoard {

  private Player p1;

  private Player p2;

  private boolean gameStarted;

  private int turn;

  private char[][] boardState;

  private int winner;

  private boolean isDraw;

  /**
   * Create a new board with reasonable initial state.
   */
  public GameBoard() {
    newGame();
  }
  
  /**
   *  Reset the board to initial state.
   */
  public void newGame() {
    this.p1 = this.p2 = null;
    this.gameStarted = false;;
    this.turn = 1;

    this.boardState = new char[3][3];
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 3; ++j) {
        this.boardState[i][j] = 0;
      }
    }
    
    this.winner = 0;
    this.isDraw = false;
  }

  /**
   * Set the first player of this game to specified player.
   */
  public void setPlayer1(Player player) {
    this.p1 = player;
  }

  /**
   * Set the second player of this game to specified player.
   */
  public void setPlayer2(Player player) {
    this.p2 = player;
  }
  
  /**
   * Get the first player.
   */
  public Player getPlayer1() {
    return this.p1;
  }

  /**
   * Get the second player.
   */
  public Player getPlayer2() {
    return this.p2;
  }
  
  /**
   * Start the game.
   */
  public void startGame() {
    this.gameStarted = true;
  }
  
  /**
   * Let the player p place a move at position specified by x and y.
   */
  public void move(Move move) throws InvalidMoveException {
    Player player = move.getPlayer();
    int x = move.getX();
    int y = move.getY();

    if (x < 0 || x >= 3 || y < 0 || y >= 3) {
      throw new InvalidPositionException();
    }

    if (turn != player.getId()) {
      throw new NotYourTurnException();
    }

    if (boardState[x][y] != 0) {
      throw new AlreadyOccupiedExcpetion();
    }
          
    boardState[x][y] = player.getType();
    turn = player.getId() == p1.getId() ? p2.getId() : p1.getId();
    
    checkWinner();
  }
  
  private static boolean threeEqual(char a, char b, char c) {
    return a == b && b == c && a != 0;
  }
  
  private void checkWinner() {
    // Horizontal && Vertical
    for (int i = 0; i < 3; ++i) {
      if (threeEqual(boardState[i][0], boardState[i][1], boardState[i][2])) {
        setWinner(boardState[i][0]);
        return;
      }

      if (threeEqual(boardState[0][i], boardState[1][i], boardState[2][i])) {
        setWinner(boardState[0][i]);
        return;
      }
    }
    
    // Diagonals
    if (threeEqual(boardState[0][0], boardState[1][1], boardState[2][2])) {
      setWinner(boardState[1][1]);
      return;
    }

    if (threeEqual(boardState[2][0], boardState[1][1], boardState[0][2])) {
      setWinner(boardState[1][1]);
      return;
    }
    
    // Check if is draw
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 3; ++j) {
        if (boardState[i][j] == 0) {
          return;
        }
      }
    }
    
    isDraw = true;
  }
  
  private void setWinner(char c) {
    if (c == p1.getType()) {
      winner = p1.getId();
    } else {
      winner = p2.getId();
    }
  }
}
