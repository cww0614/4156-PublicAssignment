package models;

public class Player {

  private char type;

  private int id;

  /**
   * Create a new Player with the specified type and id.
   */
  public Player(char type, int id) {
    this.type = type;
    this.id = id;
  }
  
  /**
   * Return the character used by this player.
   */
  public char getType() {
    return this.type;
  }

  /**
   * Return the id of this player.
   */
  public int getId() {
    return this.id;
  }
}
