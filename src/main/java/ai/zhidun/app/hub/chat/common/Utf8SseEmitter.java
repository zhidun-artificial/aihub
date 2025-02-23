package ai.zhidun.app.hub.chat.common;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.nio.charset.StandardCharsets;

public class Utf8SseEmitter extends SseEmitter {

    @Override
    protected void extendResponse(@NonNull ServerHttpResponse outputMessage) {
        super.extendResponse(outputMessage);

        HttpHeaders headers = outputMessage.getHeaders();
        if (headers.getContentType() == null) {
            headers.setContentType(new MediaType(MediaType.TEXT_EVENT_STREAM, StandardCharsets.UTF_8));
        }
    }
}
