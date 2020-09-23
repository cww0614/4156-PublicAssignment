package models.error;

public class InvalidMoveException extends Exception {
  private static final long serialVersionUID = 8953601149038132258L;

  public int code() {
    return 101;
  }

  public String cause() {
    return "Invalid Move";
  }
}
