package ai.zhidun.app.knowledge.documents.service;

import ai.zhidun.app.knowledge.documents.model.BlockRuleVo;
import ai.zhidun.app.knowledge.store.utils.FileParser.TextExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BlockChecker {
    private final BlockService service;

    public BlockChecker(BlockService service) {
        this.service = service;
    }


    public sealed interface CheckResult permits Passed, Blocked {

    }

    public record Passed() implements CheckResult {

    }

    public record Blocked(String reason) implements CheckResult {

    }

    public CheckResult check(TextExtractor extractor) {
        String content = extractor.content();
        String fileName = extractor.fileName();
        for (BlockRuleVo blockRuleVo : service.all()) {
            String blockedWord = blockRuleVo.blockedWord();
            if (content.contains(blockedWord)) {
                log.info("{} is blocked by {}", fileName, blockedWord);
                return new Blocked("包含违禁词:" + blockedWord);
            }
        }
        return new Passed();
    }
}
