package ai.zhidun.app.hub.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Response<T>(int code, String msg, T data) {

    public static <T> Response<T> ok(T data) {
        return new Response<>(0, "ok", data);
    }

    public static <T> Response<List<T>> list(List<T> data) {
        return new Response<>(0, "ok", data);
    }

    public static Response<Empty> ok() {
        return ok(new Empty());
    }

    public static <T> Response<PageVo<T>> page(IPage<T> page) {
        return ok(new PageVo<>(page));
    }

    public record PageVo<T>(
            @JsonIgnore
            IPage<T> page
    ) {

        @JsonProperty
        public List<T> records() {
            return this.page.getRecords();
        }

        @JsonProperty
        public long total() {
            return this.page.getTotal();
        }

        @JsonProperty
        public long pageNo() {
            return this.page.getCurrent();
        }

        @JsonProperty
        public long pageSize() {
            return this.page.getSize();
        }
    }

    public record Empty() {
    }
}
