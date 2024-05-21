package jnpf.yozo.utils;

public interface IResult<T> {
    boolean isSuccess();

    String getMessage();

    T getData();

    void setData(T var1);
}
