package ai.zhidun.app.hub.documents.service;

import ai.zhidun.app.hub.documents.model.BlockedWordVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BlockChecker {
    private final BlockedWordService service;

    public BlockChecker(BlockedWordService service) {
        this.service = service;
    }


    public sealed interface CheckResult permits Passed, Blocked {

    }

    public record Passed() implements CheckResult {

    }

    public record Blocked(String reason) implements CheckResult {

    }

    public CheckResult check(String content) {
        for (BlockedWordVo vo : service.all()) {
            String value = vo.value();
            if (content.contains(value)) {
                log.info("is blocked by {}", value);
                return new Blocked("包含违禁词:" + value);
            }
        }
        return new Passed();
    }
}
