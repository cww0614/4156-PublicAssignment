package models;

public class Message {

  private boolean moveValidity;

  @SuppressWarnings("unused") // The field will be used in JSON serialization
  private int code;

  @SuppressWarnings("unused") // The field will be used in JSON serialization
  private String message;
  
  /**
   * Create a new Message object with the specified parameters.
   */
  public Message(boolean moveValidity, int code, String message) {
    this.moveValidity = moveValidity;
    this.code  = code;
    this.message = message;
  }
  
  public boolean isValid() {
    return moveValidity;
  }

}
