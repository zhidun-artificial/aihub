package ai.zhidun.app.hub.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Assistant 不是单例bean，每次对话都会创建一个新的Assistant实例。
 */
public interface AssistantApi {

    /**
     * @param memoryId 这里的MemoryId表示对话，机器人会记住对话的上下文。
     *                 根据 <a href="https://docs.langchain4j.dev/tutorials/chat-memory#memory-vs-history">memory-vs-history</a>，这里的记忆和历史是不同的。
     *                 记忆存储于一个本地的memdb中，而历史存储在mysql中，两者存储的字段也不一样。
     * @param query 用户的输入, query可以作为模板中的占位符，例如：`你好，我是{{query}}`
     * @return TokenStream
     */
        TokenStream chat(@MemoryId String memoryId, @UserMessage @V("query") String query);
}