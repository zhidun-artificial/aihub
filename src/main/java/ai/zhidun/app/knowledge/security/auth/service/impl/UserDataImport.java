package ai.zhidun.app.knowledge.security.auth.service.impl;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class UserDataImport implements ReadListener<UserData> {

    private final UserServiceImpl service;

    private static final int BATCH_COUNT = 100;

    private final List<UserData> cache = new ArrayList<>(BATCH_COUNT);

    private int count = 0;

    public UserDataImport(UserServiceImpl service) {
        this.service = service;
    }

    @Override
    public void invoke(UserData data, AnalysisContext analysisContext) {
        log.info("解析到一条数据:{}", data);
        cache.add(data);
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (cache.size() >= BATCH_COUNT) {
            try {
                count += service.save(cache);
            } finally {
                cache.clear();
            }
        }

    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!cache.isEmpty()) {
            count += service.save(cache);
        }
    }

    public int count() {
        return this.count;
    }
}
