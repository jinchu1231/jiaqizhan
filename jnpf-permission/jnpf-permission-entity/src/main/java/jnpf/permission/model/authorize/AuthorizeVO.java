package jnpf.permission.model.authorize;


import jnpf.base.model.base.SystemBaeModel;
import jnpf.base.model.button.ButtonModel;
import jnpf.base.model.column.ColumnModel;
import jnpf.base.model.form.ModuleFormModel;
import jnpf.base.model.module.ModuleModel;
import jnpf.base.model.resource.ResourceModel;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 权限集合模型
 */
@Data
public class AuthorizeVO implements Serializable {
    // 菜单
//    private List<MenuModel> menuList;
    // 功能
    private List<ModuleModel> moduleList = new ArrayList<>();
    // 按钮
    private List<ButtonModel> buttonList = new ArrayList<>();
    // 视图
    private List<ColumnModel> columnList = new ArrayList<>();
    // 资源
    private List<ResourceModel> resourceList = new ArrayList<>();
    //表单
    private List<ModuleFormModel> formsList = new ArrayList<>();

    /**
     * 系统
     */
    private List<SystemBaeModel> systemList = new ArrayList<>();

    public AuthorizeVO() {
    }

    public AuthorizeVO(List<ModuleModel> moduleList, List<ButtonModel> buttonList, List<ColumnModel> columnList, List<ResourceModel> resourceList, List<ModuleFormModel> formsList, List<SystemBaeModel> systemList) {
        this.moduleList = moduleList;
        this.buttonList = buttonList;
        this.columnList = columnList;
        this.resourceList = resourceList;
        this.formsList = formsList;
        this.systemList = systemList;
    }
}
