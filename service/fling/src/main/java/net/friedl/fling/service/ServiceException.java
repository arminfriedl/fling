package net.friedl.fling.service;

public class ServiceException extends Exception {
  private static final long serialVersionUID = 2159182914434903969L;

  /**
   * {@inheritDoc}
   */
  public ServiceException() {
    super();
  }

  /**
   * {@inheritDoc}
   */
  public ServiceException(String message) {
    super(message);
  }

  /**
   * {@inheritDoc}
   */
  public ServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * {@inheritDoc}
   */
  public ServiceException(Throwable cause) {
    super(cause);
  }

  /**
   * {@inheritDoc}
   */
  protected ServiceException(String message, Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
