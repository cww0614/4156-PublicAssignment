package models.error;

public abstract class InvalidMoveException extends Exception {
  private static final long serialVersionUID = 8953601149038132258L;

  public abstract int code();

  public abstract String cause();
}
