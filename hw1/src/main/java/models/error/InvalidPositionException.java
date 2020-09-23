package models.error;

public class InvalidPositionException extends InvalidMoveException {
  private static final long serialVersionUID = 1743493112114560847L;

  @Override
  public int code() {
    return 102;
  }

  @Override
  public String cause() {
    return "The position supplied is invalid";
  }
}
