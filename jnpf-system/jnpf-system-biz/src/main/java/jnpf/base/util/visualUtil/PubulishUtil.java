package jnpf.base.util.visualUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.base.UserInfo;
import jnpf.base.entity.ModuleButtonEntity;
import jnpf.base.entity.ModuleColumnEntity;
import jnpf.base.entity.ModuleDataAuthorizeEntity;
import jnpf.base.entity.ModuleDataAuthorizeSchemeEntity;
import jnpf.base.entity.ModuleEntity;
import jnpf.base.entity.ModuleFormEntity;
import jnpf.base.model.module.PropertyJsonModel;
import jnpf.base.model.online.AuthFlieds;
import jnpf.base.model.online.PerColModels;
import jnpf.base.model.online.VisualMenuModel;
import jnpf.base.service.ModuleButtonService;
import jnpf.base.service.ModuleColumnService;
import jnpf.base.service.ModuleDataAuthorizeSchemeService;
import jnpf.base.service.ModuleDataAuthorizeService;
import jnpf.base.service.ModuleFormService;
import jnpf.base.service.ModuleService;
import jnpf.emnus.SearchMethodEnum;
import jnpf.permission.model.authorize.ConditionModel;
import jnpf.util.DateUtil;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 功能发布
 *
 * @author JNPF开发平台组
 * @version V3.4
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2022/4/7
 */
@Component
public class PubulishUtil {
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private ModuleService moduleService;
    @Autowired
    private ModuleButtonService moduleButtonService;
    @Autowired
    private ModuleColumnService moduleColumnService;
    @Autowired
    private ModuleFormService moduleFormService;
    @Autowired
    private ModuleDataAuthorizeService moduleDataAuthorizeService;
    @Autowired
    private ModuleDataAuthorizeSchemeService moduleDataAuthorizeSchemeService;

    /**
     * 功能类型
     */
    private final static Integer Type = 3;

    /**
     * pc父级菜单 默认
     */
    private static final String pcCate = "功能示例";


    /**
     * app父级菜单 默认
     */
    private static final String appCate = "移动应用";

    /**
     * pc端分类
     */
    private static final String pcCategory = "Web";

    /**
     * app端分类
     */
    private static final String appCategory = "App";

    /**
     * pc父级菜单id 默认
     */
    private String parentId;

    /**
     * app父级菜单id
     */
    private String appParentId;

    /**
     * 图标
     */
    private final static String icon = "icon-ym icon-ym-webForm";

    private final static List<String> symbol = new ArrayList() {{
        add("@userId");
        add("@organizeId");
    }};

    public Integer publishMenu(VisualMenuModel visualMenuModel) {
        UserInfo userInfo = userProvider.get();

        List<ModuleEntity> moduleList = moduleService.getModuleList(visualMenuModel.getId());

        ModuleEntity moduleEntity = new ModuleEntity();
        String uuid = RandomUtil.uuId();
        String appUuid = RandomUtil.uuId();

        PerColModels pcPerCols = visualMenuModel.getPcPerCols() != null ? visualMenuModel.getPcPerCols() : new PerColModels();
        PerColModels appPerCols = visualMenuModel.getAppPerCols() != null ? visualMenuModel.getAppPerCols() : new PerColModels();

        moduleEntity.setCategory(pcCategory);

        moduleEntity.setFullName(visualMenuModel.getFullName());
        moduleEntity.setEnCode(visualMenuModel.getEncode());
        moduleEntity.setIcon(icon);
        moduleEntity.setType(visualMenuModel.getType());
        moduleEntity.setModuleId(visualMenuModel.getId());
        PropertyJsonModel jsonModel = new PropertyJsonModel();
        jsonModel.setModuleId(visualMenuModel.getId());
        jsonModel.setIconBackgroundColor("");
        jsonModel.setIsTree(0);
        moduleEntity.setPropertyJson(JsonUtil.getObjectToString(jsonModel));
        moduleEntity.setSortCode((999L));
        moduleEntity.setEnabledMark(1);
        moduleEntity.setIsButtonAuthorize(1);
        moduleEntity.setIsColumnAuthorize(1);
        moduleEntity.setIsDataAuthorize(1);
        moduleEntity.setIsFormAuthorize(1);
        moduleEntity.setCreatorTime(DateUtil.getNowDate());
        moduleEntity.setCreatorUserId(userInfo.getUserId());
        moduleEntity.setId(uuid);
        String address = Objects.equals(visualMenuModel.getType(), 8) ? "portal" : "model";
        moduleEntity.setUrlAddress(address + "/" + visualMenuModel.getEncode());

        boolean menu = false;

        if (1 == visualMenuModel.getPc()) {
            List<ModuleEntity> pcModuleList = moduleList.stream().filter(module -> pcCategory.equals(module.getCategory())).collect(Collectors.toList());
            //是否生成过菜单
            if (pcModuleList.size() > 0) {
                for (ModuleEntity entity : pcModuleList) {
                    String menuId = entity.getId();
                    //变更权限
                    alterPer(entity, pcPerCols);
                    moduleEntity.setParentId(entity.getParentId());
                    moduleEntity.setSystemId(entity.getSystemId());
                    moduleEntity.setId(menuId);
                    moduleEntity.setSortCode(entity.getSortCode());
                    moduleEntity.setIcon(entity.getIcon());

                    //更新菜单
                    menu = moduleService.update(entity.getId(), moduleEntity);
                }
            } else {
                //创建菜单
                moduleEntity.setParentId(visualMenuModel.getPcModuleParentId());
                moduleEntity.setSystemId(visualMenuModel.getPcSystemId());
                if (StringUtil.isEmpty(moduleEntity.getParentId())) {
                    return 3;
                }
                menu = this.createMenu(moduleEntity);

                batchCreatePermissions(pcPerCols, uuid);
            }
            if (!menu) {
                return 2;
            }
        }
        moduleEntity.setCategory(appCategory);
        moduleEntity.setId(appUuid);
        String portalAddress = Objects.equals(visualMenuModel.getType(), 8) ? "visualPortal" : "dynamicModel";
        moduleEntity.setUrlAddress("/pages/apply/" + portalAddress + "/index?id=" + visualMenuModel.getId());
        moduleEntity.setEnCode(visualMenuModel.getEncode());
        if (1 == visualMenuModel.getApp()) {
            List<ModuleEntity> appModuleList = moduleList.stream().filter(module -> appCategory.equals(module.getCategory())).collect(Collectors.toList());
            if (appModuleList.size() > 0) {
                for (ModuleEntity entity : appModuleList) {
                    String menuId = entity.getId();
                    //变更权限
                    alterPer(entity, appPerCols);
                    moduleEntity.setParentId(entity.getParentId());
                    moduleEntity.setSystemId(entity.getSystemId());
                    moduleEntity.setId(menuId);
                    moduleEntity.setSortCode(entity.getSortCode());
                    moduleEntity.setIcon(entity.getIcon());
                    //更新菜单
                    menu = moduleService.update(entity.getId(), moduleEntity);
                }
            } else {
                moduleEntity.setParentId(visualMenuModel.getAppModuleParentId());
                moduleEntity.setSystemId(visualMenuModel.getAppSystemId());
                if (StringUtil.isEmpty(moduleEntity.getParentId())) {
                    return 3;
                }
                menu = this.createMenu(moduleEntity);
                batchCreatePermissions(appPerCols, appUuid);
            }

            if (!menu) {
                //创建失败，编码或名称是否重复
                return 2;
            }
        }
        return 1;//同步成功
    }

    /**
     * 创建菜单验证
     *
     * @param moduleEntity
     * @return
     */
    private boolean createMenu(ModuleEntity moduleEntity) {
        if (moduleService.isExistByFullName(moduleEntity, moduleEntity.getCategory(), moduleEntity.getSystemId())) {
            return false;
        }
        if (moduleService.isExistByEnCode(moduleEntity, moduleEntity.getCategory(), moduleEntity.getSystemId())) {
            return false;
        }
        moduleService.save(moduleEntity);
        return true;
    }

    private void batchCreatePermissions(PerColModels perColModels, String moduleId) {

        List<AuthFlieds> buttonPermission = Objects.nonNull(perColModels.getButtonPermission()) ? perColModels.getButtonPermission() : new ArrayList<>();
        List<AuthFlieds> formPermission = Objects.nonNull(perColModels.getFormPermission()) ? perColModels.getFormPermission() : new ArrayList<>();
        List<AuthFlieds> listPermission = Objects.nonNull(perColModels.getListPermission()) ? perColModels.getListPermission() : new ArrayList<>();
        List<AuthFlieds> dataPermission = Objects.nonNull(perColModels.getDataPermission()) ? perColModels.getDataPermission() : new ArrayList<>();
        List<ModuleDataAuthorizeSchemeEntity> dataPermissionScheme = Objects.nonNull(perColModels.getDataPermissionScheme()) ? perColModels.getDataPermissionScheme() : new ArrayList<>();

        //按钮
        List<ModuleButtonEntity> buttonEntities = buttonPermission.stream().map(button -> {
            ModuleButtonEntity buttonEntity = new ModuleButtonEntity();
            buttonEntity.setEnabledMark(button.getStatus() ? 1 : 0);
            buttonEntity.setEnCode(button.getEncode());
            buttonEntity.setFullName(button.getFullName());
            buttonEntity.setParentId("-1");
            buttonEntity.setModuleId(moduleId);
            buttonEntity.setSortCode(0L);
            return buttonEntity;
        }).collect(Collectors.toList());

        //表单权限
        List<ModuleFormEntity> moduleFormEntities = formPermission.stream().map(form -> {
            ModuleFormEntity formEntity = new ModuleFormEntity();
            formEntity.setBindTable(form.getBindTableName());
            formEntity.setEnabledMark(form.getStatus() ? 1 : 0);
            formEntity.setEnCode(form.getEncode());
            formEntity.setFullName(form.getFullName());
            formEntity.setParentId("-1");
            formEntity.setModuleId(moduleId);
            formEntity.setFieldRule(form.getRule());
            formEntity.setChildTableKey(form.getChildTableKey());
            formEntity.setSortCode(0L);
            return formEntity;
        }).collect(Collectors.toList());

        //列表
        List<ModuleColumnEntity> moduleColumnEntities = listPermission.stream().map(list -> {
            ModuleColumnEntity moduleColumnEntity = new ModuleColumnEntity();
            moduleColumnEntity.setBindTable(list.getBindTableName());
            moduleColumnEntity.setEnabledMark(list.getStatus() ? 1 : 0);
            moduleColumnEntity.setEnCode(list.getEncode());
            moduleColumnEntity.setFullName(list.getFullName());
            moduleColumnEntity.setParentId("-1");
            moduleColumnEntity.setModuleId(moduleId);
            moduleColumnEntity.setSortCode(0L);
            moduleColumnEntity.setChildTableKey(list.getChildTableKey());
            moduleColumnEntity.setFieldRule(list.getRule());
            return moduleColumnEntity;
        }).collect(Collectors.toList());

        List<ModuleDataAuthorizeEntity> moduleDataEntities = new ArrayList<>();


        //数据权限
        for (AuthFlieds authFlieds : dataPermission) {
            ModuleDataAuthorizeEntity authorizeEntity = new ModuleDataAuthorizeEntity();
            authorizeEntity.setBindTable(authFlieds.getBindTableName());
            authorizeEntity.setConditionSymbol(symbol.contains(authFlieds.getAuthCondition()) ? SearchMethodEnum.Equal.getSymbol() : SearchMethodEnum.Included.getSymbol());
            authorizeEntity.setId(authFlieds.getId());
            authorizeEntity.setDescription("同步菜单自动生成");
            authorizeEntity.setEnCode(authFlieds.getEncode());
            authorizeEntity.setFieldRule(authFlieds.getRule());
            authorizeEntity.setFullName(authFlieds.getFullName());
            authorizeEntity.setModuleId(moduleId);
            authorizeEntity.setType("varchar");
            authorizeEntity.setConditionText(authFlieds.getAuthCondition());
            authorizeEntity.setEnabledMark(1);
            authorizeEntity.setSortCode(-9527l);
            moduleDataEntities.add(authorizeEntity);
        }

        for (ModuleButtonEntity btn : buttonEntities) {
            moduleButtonService.create(btn);
        }
        for (ModuleFormEntity formEntity : moduleFormEntities) {
            moduleFormService.create(formEntity);
        }
        for (ModuleColumnEntity moduleColumnEntity : moduleColumnEntities) {
            moduleColumnService.create(moduleColumnEntity);
        }
        for (ModuleDataAuthorizeEntity authorizeEntity : moduleDataEntities) {
            moduleDataAuthorizeService.save(authorizeEntity);
        }

        //方案
        for (ModuleDataAuthorizeSchemeEntity moduleDataAuthorizeEntity : dataPermissionScheme) {
            moduleDataAuthorizeEntity.setModuleId(moduleId);

            //字段是数据库现有字段，那生成的方案里面的id必须是字段id
            List<ModuleDataAuthorizeEntity> dataAuthorizeEntityList = moduleDataAuthorizeService.getList(moduleId);
            for (ModuleDataAuthorizeEntity auth : dataAuthorizeEntityList) {
                if (moduleDataAuthorizeEntity.getDescription().equals(auth.getFieldRule() + "_" + auth.getEnCode() + "_" + auth.getConditionSymbol())
                        && Objects.equals(-9527l, moduleDataAuthorizeEntity.getSortCode())) {
                    String conditionJson = moduleDataAuthorizeEntity.getConditionJson();
                    List<ConditionModel> listc = JsonUtil.getJsonToList(conditionJson, ConditionModel.class);
                    ConditionModel.ConditionItemModel conditionItemModel = listc.get(0).getGroups().get(0);
                    conditionItemModel.setId(auth.getId());
                    moduleDataAuthorizeEntity.setConditionJson(JsonUtil.getObjectToString(listc));
                }
            }
            moduleDataAuthorizeSchemeService.save(moduleDataAuthorizeEntity);
        }

        //创建全部数据方案
        if (Objects.nonNull(perColModels.getDataPermission())) {
            Boolean exist = moduleDataAuthorizeSchemeService.isExistAllData(moduleId);
            if (!exist) {
                ModuleDataAuthorizeSchemeEntity moduleDataAuthorizeSchemeEntity = new ModuleDataAuthorizeSchemeEntity();
                moduleDataAuthorizeSchemeEntity.setFullName("全部数据");
                moduleDataAuthorizeSchemeEntity.setEnCode("jnpf_alldata");
                moduleDataAuthorizeSchemeEntity.setAllData(1);
                moduleDataAuthorizeSchemeEntity.setModuleId(moduleId);
                moduleDataAuthorizeSchemeService.create(moduleDataAuthorizeSchemeEntity);
            }
        }
    }

    private void getModuleParentId() {
        QueryWrapper<ModuleEntity> moduleWrapper = new QueryWrapper<>();
        moduleWrapper.lambda().eq(ModuleEntity::getFullName, pcCate).eq(ModuleEntity::getCategory, pcCategory);
        ModuleEntity pcModule = moduleService.getOne(moduleWrapper);
        if (pcModule != null) {
            this.setParentId(pcModule.getId());
        }
        QueryWrapper<ModuleEntity> appWrapper = new QueryWrapper<>();
        appWrapper.lambda().eq(ModuleEntity::getFullName, appCate).eq(ModuleEntity::getCategory, appCategory);
        ModuleEntity appModule = moduleService.getOne(appWrapper);
        if (appModule != null) {
            this.setAppParentId(appModule.getId());
        }
    }

    /**
     * 自动变更权限
     *
     * @param entity
     * @param perColModel
     * @return
     */
    private void alterPer(ModuleEntity entity, PerColModels perColModel) {
        String moduleMainId = entity.getId();

        PerColModels perColModels = new PerColModels();
        //列表
        if (perColModel.getListPermission() != null) {
            Map<String, String> colMap = new HashMap<>();
            List<ModuleColumnEntity> columnEntities = moduleColumnService.getList(moduleMainId);
            columnEntities.stream().forEach(col -> colMap.put(col.getEnCode(), col.getId()));
            List<AuthFlieds> listPermission = perColModel.getListPermission() != null ? perColModel.getListPermission() : new ArrayList<>();

            //只变更状态
            List<AuthFlieds> authColList = intersectList1(listPermission, colMap);
            Map<String, Boolean> stateMap = new HashMap<>();
            authColList.stream().forEach(auth -> stateMap.put(auth.getEncode(), auth.getStatus()));
            for (ModuleColumnEntity columnEntity : columnEntities) {
                if (Objects.nonNull(stateMap.get(columnEntity.getEnCode()))) {
                    columnEntity.setEnabledMark(stateMap.get(columnEntity.getEnCode()) ? 1 : 0);
                    moduleColumnService.update(columnEntity.getId(), columnEntity);
                }
            }
            //新增
            List<AuthFlieds> authColCreList = intersectList2(listPermission, authColList);
            perColModels.setListPermission(authColCreList);
//            //删除
//            Map<String, String> colDataMap = intersectList3(colMap, authColList);
//            List<ModuleColumnEntity> colEnties = columnEntities.stream().filter(col -> colDataMap.get(col.getEnCode()) != null).collect(Collectors.toList());
//            for (ModuleColumnEntity moduleColumnEntity : colEnties) {
//                moduleColumnService.delete(moduleColumnEntity);
//            }
        }

        //表单
        if (perColModel.getFormPermission() != null) {
            Map<String, String> formMap = new HashMap<>();
            List<ModuleFormEntity> formEntities = moduleFormService.getList(moduleMainId);
            formEntities.stream().forEach(form -> formMap.put(form.getEnCode(), form.getId()));
            List<AuthFlieds> formPermission = perColModel.getFormPermission() != null ? perColModel.getFormPermission() : new ArrayList<>();
            List<AuthFlieds> authFormList = intersectList1(formPermission, formMap);

            Map<String, Boolean> stateFMap = new HashMap<>();
            authFormList.stream().forEach(auth -> stateFMap.put(auth.getEncode(), auth.getStatus()));
            for (ModuleFormEntity formEntity : formEntities) {
                if (Objects.nonNull(stateFMap.get(formEntity.getEnCode()))) {
                    formEntity.setEnabledMark(stateFMap.get(formEntity.getEnCode()) ? 1 : 0);
                    moduleFormService.update(formEntity.getId(), formEntity);
                }
            }

            List<AuthFlieds> authFormCreList = intersectList2(formPermission, authFormList);
            perColModels.setFormPermission(authFormCreList);
//            Map<String, String> formDataMap = intersectList3(formMap, authFormList);
//            List<ModuleFormEntity> formEnties = formEntities.stream().filter(form -> formDataMap.get(form.getEnCode()) != null).collect(Collectors.toList());
//            for (ModuleFormEntity formEntity : formEnties) {
//                moduleFormService.delete(formEntity);
//            }
        }
        //按钮权限
        if (perColModel.getButtonPermission() != null) {
            Map<String, String> btnMap = new HashMap<>();
            List<ModuleButtonEntity> buttonEntities = moduleButtonService.getListByModuleIds(moduleMainId);
            buttonEntities.stream().forEach(btn -> btnMap.put(btn.getEnCode(), btn.getId()));
            List<AuthFlieds> buttonPermission = perColModel.getButtonPermission() != null ? perColModel.getButtonPermission() : new ArrayList<>();
            List<AuthFlieds> authBtnList = intersectList1(buttonPermission, btnMap);
            Map<String, Boolean> stateBMap = new HashMap<>();

            authBtnList.stream().forEach(auth -> stateBMap.put(auth.getEncode(), auth.getStatus()));
            for (ModuleButtonEntity btnEntity : buttonEntities) {
                if (Objects.nonNull(stateBMap.get(btnEntity.getEnCode()))) {
                    btnEntity.setEnabledMark(stateBMap.get(btnEntity.getEnCode()) ? 1 : 0);
                    moduleButtonService.update(btnEntity.getId(), btnEntity);
                }
            }

            List<AuthFlieds> authBtnCreList = intersectList2(buttonPermission, authBtnList);
//            Map<String, String> btnDataMap = intersectList3(btnMap, authBtnList);
//            List<ModuleButtonEntity> btnEnties = buttonEntities.stream().filter(btn -> btnDataMap.get(btn.getEnCode()) != null).collect(Collectors.toList());
//            for (ModuleButtonEntity buttonEntity : btnEnties) {
//                moduleButtonService.delete(buttonEntity);
//            }

            perColModels.setButtonPermission(authBtnCreList);
        }

        //表单权限字段
        if (perColModel.getDataPermission() != null) {
            List<ModuleDataAuthorizeEntity> dataAuthorizeEntityList = moduleDataAuthorizeService.getList(moduleMainId);
            List<AuthFlieds> dataPermission = perColModel.getDataPermission() != null ? perColModel.getDataPermission() : new ArrayList<>();
            List<AuthFlieds> dataPermissionList = new ArrayList<>(dataPermission);
            //交集
            List<AuthFlieds> authDataList = new ArrayList<>();
            for (AuthFlieds authFlieds : dataPermission) {
                for (ModuleDataAuthorizeEntity authorizeEntity : dataAuthorizeEntityList) {
                    String conditions = symbol.contains(authFlieds.getAuthCondition()) ? SearchMethodEnum.Equal.getSymbol() : SearchMethodEnum.Included.getSymbol();
                    if (authorizeEntity.getConditionText().equalsIgnoreCase(authFlieds.getAuthCondition())
                            && authorizeEntity.getConditionSymbol().equalsIgnoreCase(SearchMethodEnum.Equal.getMessage())
                            && authorizeEntity.getEnCode().equalsIgnoreCase(authFlieds.getEncode())) {
                        authDataList.add(authFlieds);
                    }
                }
            }
            List<AuthFlieds> authDataCreList = intersectList2(dataPermission, authDataList);
            //需要删除
            List<String> collect = dataPermissionList.stream().map(AuthFlieds::getEncode).collect(Collectors.toList());
            List<ModuleDataAuthorizeEntity> dataEntities = new ArrayList<>();
            if (dataAuthorizeEntityList.size() > 0) {
                for (ModuleDataAuthorizeEntity item : dataAuthorizeEntityList) {
                    if (!collect.contains(item.getEnCode()) && Objects.equals(-9527l, item.getSortCode())) {
                        if (item.getConditionSymbol().equalsIgnoreCase(SearchMethodEnum.Equal.getMessage())) {
                            //需要移除的权限
                            dataEntities.add(item);
                        }
                    }
                }
            }
            for (ModuleDataAuthorizeEntity dataEntity : dataEntities) {
                moduleDataAuthorizeService.delete(dataEntity);
            }
            perColModels.setDataPermission(authDataCreList);
        }
        //表单权限方案
        if (perColModel.getDataPermissionScheme() != null) {
            //交集
            List<ModuleDataAuthorizeSchemeEntity> togetherList = new ArrayList<>();
            List<ModuleDataAuthorizeSchemeEntity> dataAuthorizeSchemeList = moduleDataAuthorizeSchemeService.getList(moduleMainId);
            List<ModuleDataAuthorizeSchemeEntity> dataPermissionScheme = perColModel.getDataPermissionScheme();
            List<ModuleDataAuthorizeSchemeEntity> dataPermissionSchemeClone = new ArrayList<>(dataPermissionScheme);

            for (ModuleDataAuthorizeSchemeEntity authFlieds : dataPermissionScheme) {
                for (ModuleDataAuthorizeSchemeEntity schemeEntity : dataAuthorizeSchemeList) {
                    if (schemeEntity.getConditionJson() == null) {
                        continue;
                    }
                    if (Objects.equals(-9527l, schemeEntity.getSortCode()) && schemeEntity.getConditionText().contains(authFlieds.getConditionText())) {
                        togetherList.add(authFlieds);
                    }
                }
            }

            //需要新增 dataPermissionScheme
            dataPermissionScheme.removeAll(togetherList);

            //需要删除
            List<String> collect = dataPermissionSchemeClone.stream().map(ModuleDataAuthorizeSchemeEntity::getConditionText).collect(Collectors.toList());
            List<ModuleDataAuthorizeSchemeEntity> deleteSchemeList = new ArrayList<>();
            List<String> strings = Arrays.asList("【{所属组织} {等于} {@organizeId}】", "【{所属组织} {等于} {@organizationAndSuborganization}】", "【{所属组织} {等于} {@branchManageOrganize}】",
                    "【{所属组织} {等于} {@branchManageOrganizeAndSub}】", "【{创建人员} {等于} {@userAraSubordinates}】", "【{创建人员} {等于} {@userId}】");
            if (dataAuthorizeSchemeList.size() > 0) {
                for (ModuleDataAuthorizeSchemeEntity item : dataAuthorizeSchemeList) {
                    if (strings.contains(item.getConditionText()) && !collect.contains(item.getConditionText()) && Objects.equals(-9527l, item.getSortCode())) {
                        //需要移除的权限方案
                        deleteSchemeList.add(item);
                    }
                }
            }

            for (ModuleDataAuthorizeSchemeEntity scheme : deleteSchemeList) {
                moduleDataAuthorizeSchemeService.delete(scheme);
            }
            ;
            perColModels.setDataPermissionScheme(new ArrayList<>(dataPermissionScheme));
        }

        //新增的权限
        batchCreatePermissions(perColModels, moduleMainId);

    }


    /**
     * 取交集 （不需要变动的数据）
     *
     * @param authFlieds  新提交过来的
     * @param databaseMap 数据库存在数据
     * @return
     */
    private List<AuthFlieds> intersectList1(List<AuthFlieds> authFlieds, Map<String, String> databaseMap) {
        List<AuthFlieds> lastList = new LinkedList<>();
        for (AuthFlieds authFlied : authFlieds) {
            if (databaseMap.containsKey(authFlied.getEncode())) {
                lastList.add(authFlied);
            }
        }
        return lastList;
    }

    /**
     * 求差集 （需要新增的数据） create
     *
     * @param auth1 新提交过来的
     * @param auth2 与数据库的交集
     * @return
     */
    private List<AuthFlieds> intersectList2(List<AuthFlieds> auth1, List<AuthFlieds> auth2) {
        auth1.removeAll(auth2);
        return auth1;
    }

    /**
     * 求差集 （需要变更的数据） delete
     *
     * @param databaseMap 数据库已存在的数据 <encode,id>
     * @param auth2       与数据库的交集
     * @return
     */
    private Map<String, String> intersectList3(Map<String, String> databaseMap, List<AuthFlieds> auth2) {
        Map<String, String> changeMap = new HashMap<>();
        changeMap.putAll(databaseMap);
        for (AuthFlieds authFlieds : auth2) {
            if (databaseMap.get(authFlieds.getEncode()) != null) {
                changeMap.remove(authFlieds.getEncode());
            }
        }
        return changeMap;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setAppParentId(String appParentId) {
        this.appParentId = appParentId;
    }


    public void publishPortalMenu(VisualMenuModel visualMenuModel) {

    }
}
