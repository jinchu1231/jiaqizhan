package jnpf.base.model.Template7;


import jnpf.model.visualJson.FieLdsModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ChildrenModel {

    //子表的属性
    private List<FieLdsModel> childrenList;
    //子表名称
    private String className;
    //json原始名称
    private String tableModel;
    //子表系统控件
    private List<KeyModel> systemList;
}
