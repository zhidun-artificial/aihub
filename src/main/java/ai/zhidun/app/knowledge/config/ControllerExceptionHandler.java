package ai.zhidun.app.knowledge.config;

import ai.zhidun.app.knowledge.common.BizError;
import ai.zhidun.app.knowledge.common.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ControllerExceptionHandler {

    @ExceptionHandler(value = {BizException.class})
    public ResponseEntity<BizError> bizException(BizException ex) {
        log.warn("业务异常", ex);
        return new ResponseEntity<>(ex.getError(), ex.getStatus());
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<BizError> common(Exception ex) {
        BizError message = BizError.error(ex.getMessage());
        log.warn("未知异常", ex);
        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}