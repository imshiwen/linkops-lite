package dev.linkops.server.exception;

/** 资源不存在,接口返回 404。 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
