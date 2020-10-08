package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import models.GameBoard;
import models.Player;

public class GameBoardDao {
  /**
   * Connect this instance to a database.
   */
  public void connect() throws SQLException {
    close();
    conn = DriverManager.getConnection("jdbc:sqlite:data.db");
  }
  
  /**
   * Close the underlying connection in this instance.
   */
  public void close() throws SQLException {
    if (conn != null) {
      conn.close();
      conn = null;
    }
  }
  
  /**
   * Reset saved state to initial state.
   */
  public void reset() throws SQLException {
    String[] sqls = new String[] {
        "DROP TABLE IF EXISTS game_board;",
        "DROP TABLE IF EXISTS player;",

        "CREATE TABLE player (" 
            + "id INTEGER PRIMARY KEY NOT NULL,"
            + "character CHARACTEER(1)"
            + ");",

        "CREATE TABLE game_board (" 
            + "id INTEGER PRIMARY KEY NOT NULL,"
            + "p1 INTEGER,"
            + "p2 INTEGER,"
            + "game_started INTEGER,"
            + "turn INTEGER,"
            + "board_state CHARACTER(9),"
            + "winner INTEGER,"
            + "is_draw INTEGER,"
            + "FOREIGN KEY (p1) REFERENCES player (id),"
            + "FOREIGN KEY (p2) REFERENCES player (id)"
            + ");"
            
    };

    try (Statement stmt = conn.createStatement()) {
      for (String sql : sqls) {
        stmt.execute(sql);
      }
    }
  }
  
  /**
   * Get the game board stored in the database.
   * @return the game board object, or null if nothing was stored before
   */
  public GameBoard getGameBoard() throws SQLException {
    try (Statement stmt = conn.createStatement()) {
      try (ResultSet rs = stmt.executeQuery("SELECT count(1) FROM sqlite_master WHERE "
          + "type = 'table' "
          + "AND name = 'game_board';")) {
        rs.next();
        if (rs.getInt(1) == 0) {
          return null;
        }
      }
      
      try (ResultSet rs = stmt.executeQuery("SELECT p1.id, p1.character, p2.id, p2.character, "
          + "game_started, turn, board_state, winner, is_draw "
          + "FROM game_board "
          + "LEFT JOIN player AS p1 ON p1.id = game_board.p1 "
          + "LEFT JOIN player AS p2 ON p2.id = game_board.p2;")) {
        if (!rs.next()) {
          return null;
        }
        
        Player p1 = null;
        if (rs.getString(2) != null) {
          p1 = new Player(rs.getString(2).charAt(0), rs.getInt(1));
        }

        Player p2 = null;
        if (rs.getString(4) != null) {
          p2 = new Player(rs.getString(4).charAt(0), rs.getInt(3));
        }

        boolean started = rs.getBoolean(5);
        int turn = rs.getInt(6);
        String rawBoardState = rs.getString(7);
        int winner = rs.getInt(8);
        boolean isDraw = rs.getBoolean(9);
        
        char[][] boardState = new char[3][3];
        for (int i = 0; i < 3; ++i) {
          for (int j = 0; j < 3; ++j) {
            boardState[i][j] = rawBoardState.charAt(i * 3 + j);
          }
        }
        
        return new GameBoard(p1, p2, started, turn, boardState, winner, isDraw);
      }
    }
  }
  
  /**
   * save the game board to the database.
   */
  public void saveGameBoard(GameBoard board) throws SQLException {
    String savePlayerSql = "REPLACE INTO player (id, character) VALUES (?, ?);";
    String saveBoardSql = "REPLACE INTO game_board "
        + "(id, p1, p2, game_started, turn, board_state, winner, is_draw)"
        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

    Player p1 = board.getPlayer1();
    Player p2 = board.getPlayer2();

    if (p1 != null) {
      try (PreparedStatement stmt = conn.prepareStatement(savePlayerSql)) {
        stmt.setInt(1, p1.getId());
        stmt.setInt(2, p1.getType());
        stmt.execute();
      }
    }

    if (p2 != null) {
      try (PreparedStatement stmt = conn.prepareStatement(savePlayerSql)) {
        stmt.setInt(1, p2.getId());
        stmt.setInt(2, p2.getType());
        stmt.execute();
      }
    }

    try (PreparedStatement stmt = conn.prepareStatement(saveBoardSql)) {
      stmt.setInt(1, 1);
      stmt.setInt(2, p1.getId());

      if (p2 != null) {
        stmt.setInt(3, p2.getId());
      } else {
        stmt.setNull(3, java.sql.Types.NULL);
      }

      stmt.setBoolean(4, board.isGameStarted());
      stmt.setInt(5, board.getTurn());
      
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < 3; ++i) {
        for (int j = 0; j < 3; ++j) {
          sb.append(board.getBoardState()[i][j]);
        }
      }
      stmt.setString(6, sb.toString());
      
      stmt.setInt(7, board.getWinner());
      stmt.setBoolean(8, board.isDraw());
      stmt.execute();
    }
  }
  
  private Connection conn;
}
