package ai.zhidun.app.knowledge.common;

@SuppressWarnings("unused")
public record BizError(int code, String msg) {

    public static BizError error(String msg) {
        return new BizError(500, msg);
    }

    public static BizError error(int code, String msg) {
        return new BizError(code, msg);
    }

}
