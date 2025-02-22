package ai.zhidun.app.hub.common;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public final class CustomIdentifierGenerator implements IdentifierGenerator {


    @Override
    public Number nextId(Object entity) {
        return IdWorker.getId();
    }

    @Override
    public String nextUUID(Object entity) {
        return CustomIdentifierGenerator.uuidV7();
    }

    public static String uuidV7() {
        UUID uuid = UuidCreator.getTimeOrderedEpoch();
        return uuid.toString().replace(StringPool.DASH, StringPool.EMPTY);
    }
}
