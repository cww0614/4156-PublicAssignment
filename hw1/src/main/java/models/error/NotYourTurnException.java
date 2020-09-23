package models.error;

public class NotYourTurnException extends InvalidMoveException {
  private static final long serialVersionUID = 949332918148263362L;

  @Override
  public int code() {
    return 103;
  }

  @Override
  public String cause() {
    return "Not you turn";
  }
}
