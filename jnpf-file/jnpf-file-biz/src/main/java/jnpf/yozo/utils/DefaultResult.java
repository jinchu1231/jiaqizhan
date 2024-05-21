package jnpf.yozo.utils;

public class DefaultResult<T> implements IResult<T> {
    private static final DefaultResult.SuccessResult SUCCESS = new DefaultResult.SuccessResult();
    private static final DefaultResult.FailResult FAIL = new DefaultResult.FailResult();
    private boolean success;
    private String message;
    private T data;

    public DefaultResult() {
    }

    public DefaultResult(boolean success, String message) {
        this(success, message, (T) null);
    }

    public DefaultResult(boolean success, T data) {
        this(success, (String)null, data);
    }

    public DefaultResult(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> DefaultResult<T> successResult() {
        return (DefaultResult<T>) SUCCESS;
    }

    public static <T> DefaultResult<T> successResult(T data) {
        return new DefaultResult(true, (String)null, data);
    }

    public static <T> DefaultResult<T> successResult(String message, T data) {
        return new DefaultResult(true, message, data);
    }

    public static <T> DefaultResult<T> failResult() {
        return (DefaultResult<T>) FAIL;
    }

    public static <T> DefaultResult<T> failResult(String message) {
        return new DefaultResult(false, message, (Object)null);
    }

    public static <T> DefaultResult<T> failResult(String message, T data) {
        return new DefaultResult(false, message, data);
    }

    public static <T> DefaultResult<T> failResult(T data) {
        return new DefaultResult(false, (String)null, data);
    }

    public static <T> DefaultResult<T> result(boolean success, String message, T data) {
        return new DefaultResult(success, message, data);
    }

    public boolean isSuccess() {
        return this.success;
    }

    public String getMessage() {
        return this.message;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T obj) {
        this.data = obj;
    }

    private static class FailResult extends DefaultResult<Object> {
        public FailResult() {
            super(false, (String)null, (Object)null);
        }
    }

    private static class SuccessResult extends DefaultResult<Object> {
        public SuccessResult() {
            super(true, (String)null, (Object)null);
        }
    }
}
