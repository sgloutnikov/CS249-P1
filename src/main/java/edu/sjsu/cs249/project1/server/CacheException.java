package edu.sjsu.cs249.project1.server;

/**
 * This class is used to specify a custom exception thrown by ClientCacheManager.java.
 *
 * @author David Fisher
 */
public class CacheException extends Exception {
    private static final long serialVersionUID = -41889224753690504L;

    public CacheException(final String message) {
        super(message);
    }

    public CacheException(final String message, final Throwable cause) {
        super(message, cause);
    }
}