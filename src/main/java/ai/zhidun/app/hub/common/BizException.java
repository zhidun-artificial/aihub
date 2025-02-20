package ai.zhidun.app.hub.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public final class BizException extends RuntimeException {
    private final HttpStatus status;

    private final BizError error;

    public BizException(HttpStatus status, BizError error) {
        this.status = status;
        this.error = error;
    }

    public BizException(HttpStatus status, BizError error, Throwable e) {
        super(e);
        this.status = status;
        this.error = error;
    }
}
