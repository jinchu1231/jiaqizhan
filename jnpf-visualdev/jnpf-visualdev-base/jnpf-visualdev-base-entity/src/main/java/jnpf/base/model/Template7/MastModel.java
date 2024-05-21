package jnpf.base.model.Template7;


import jnpf.model.visualJson.FieLdsModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class MastModel {

    //主表的属性
    private List<FieLdsModel> mastList;
    //系统自带的赋值
    private List<KeyModel> keyMastList;
}
