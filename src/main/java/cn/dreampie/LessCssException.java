package cn.dreampie;

public class LessCssException extends RuntimeException {
  public LessCssException(String message) {
    super(message);
  }

  public LessCssException(String message, Throwable cause) {
    super(message, cause);
  }
}
