package ai.zhidun.app.hub.common;

public record BizError(int code, String msg) {

    public static BizError error(String msg) {
        return error(500, msg);
    }

    public static BizError error(int code, String msg) {
        return new BizError(code, msg);
    }

}
