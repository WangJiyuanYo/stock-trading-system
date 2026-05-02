package icu.iseenu.common.exception;

import lombok.Getter;

/**
 * 参数校验异常
 * 用于处理参数验证失败的情况
 */
@Getter
public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(400, message);
    }

    public ValidationException(String message, Throwable cause) {
        super(400, message, cause);
    }
}
