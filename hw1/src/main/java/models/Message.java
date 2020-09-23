package models;

public class Message {

  private boolean moveValidity;

  private int code;

  private String message;
  
  /**
   * Create a new Message object with the specified parameters.
   */
  public Message(boolean moveValidity, int code, String message) {
    this.moveValidity = moveValidity;
    this.code  = code;
    this.message = message;
  }

}
