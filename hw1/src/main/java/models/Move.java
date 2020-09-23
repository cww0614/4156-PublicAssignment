package models;

public class Move {

  private final Player player;

  private final int moveX;

  private final int moveY;

  public final Player getPlayer() {
    return player;
  }

  public int getX() {
    return moveX;
  }

  public int getY() {
    return moveY;
  }

  /** 
   * Create a new Move object using the specified parameters.
   */
  public Move(Player player, int moveX, int moveY) {
    this.player = player;
    this.moveX = moveX;
    this.moveY = moveY;
  }

}
