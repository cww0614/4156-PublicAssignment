package models.error;

public class AlreadyOccupiedExcpetion extends InvalidMoveException {
  private static final long serialVersionUID = -8371452135194678898L;

  @Override
  public int code() {
    return 104;
  }

  @Override
  public String cause() {
    return "The slot is already occupied";
  }
}
