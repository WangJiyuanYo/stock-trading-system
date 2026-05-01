package icu.iseenu.exception;

import lombok.Getter;

/**
 * 系统内部异常
 * 用于处理服务器内部错误
 */
@Getter
public class SystemException extends BusinessException {

    public SystemException(String message) {
        super(500, message);
    }

    public SystemException(String message, Throwable cause) {
        super(500, message, cause);
    }
}
