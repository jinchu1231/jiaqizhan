package jnpf.base.model.Template7;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KeyModel {
    //系统自带属性
    private String jnpfKey;
    //字段名称
    private String model;
    //规则数据
    private String rule;
}
