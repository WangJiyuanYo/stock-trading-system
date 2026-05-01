package icu.iseenu.exception;

import lombok.Getter;

/**
 * 资源未找到异常
 * 用于处理资源不存在的情况
 */
@Getter
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(404, message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(404, message, cause);
    }
}
