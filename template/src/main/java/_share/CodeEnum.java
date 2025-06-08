package ${basePackage}._share;

/**
 * Api 状态码
 * @author cap4j-gen-arch
 */
public enum CodeEnum {

    /**
     * 成功
     */
    SUCCESS(0, "成功"),
    /**
     * 失败
     */
    FAIL(-1, "失败"),
    /**
     * 参数错误
     */
    PARAM_INVALIDATE(-2, "参数错误"),
    /**
     * 约束未通过
     */
    SPECIFICATION_UNSATISFIED(-3, "约束不满足"),
    /**
     * 系统异常
     */
    ERROR(-9, "系统异常"),
    /**
     * 404
     */
    NOT_FOUND(404, "没找到请求"),
    /**
     * 不支持当前请求方法
     */
    METHOD_NOT_SUPPORTED(405, "不支持当前请求方法"),
    /**
     * 消息不能读取
     */
    MESSAGE_NOT_READABLE(407, "消息不能读取");

    private final Integer code;
    private final String name;

    CodeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
