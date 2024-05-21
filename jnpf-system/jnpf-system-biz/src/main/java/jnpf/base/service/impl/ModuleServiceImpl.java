package jnpf.base.service.impl;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import jnpf.base.ActionResult;
import jnpf.base.entity.ModuleButtonEntity;
import jnpf.base.entity.ModuleColumnEntity;
import jnpf.base.entity.ModuleDataAuthorizeEntity;
import jnpf.base.entity.ModuleDataAuthorizeSchemeEntity;
import jnpf.base.entity.ModuleEntity;
import jnpf.base.entity.ModuleFormEntity;
import jnpf.base.entity.SystemEntity;
import jnpf.base.mapper.ModuleMapper;
import jnpf.base.model.module.ModuleExportModel;
import jnpf.base.model.module.ModuleModel;
import jnpf.base.service.DbLinkService;
import jnpf.base.service.ModuleButtonService;
import jnpf.base.service.ModuleColumnService;
import jnpf.base.service.ModuleDataAuthorizeSchemeService;
import jnpf.base.service.ModuleDataAuthorizeService;
import jnpf.base.service.ModuleFormService;
import jnpf.base.service.ModuleService;
import jnpf.base.service.SuperServiceImpl;
import jnpf.base.service.SystemService;
import jnpf.base.vo.DownloadVO;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.JnpfConst;
import jnpf.constant.MsgCode;
import jnpf.database.util.DbTypeUtil;
import jnpf.emnus.ModuleTypeEnum;
import jnpf.exception.DataException;
import jnpf.permission.model.authorize.AuthorizeVO;
import jnpf.permission.service.AuthorizeService;
import jnpf.util.DateUtil;
import jnpf.util.FileExport;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.ReflectionUtil;
import jnpf.util.StringUtil;
import jnpf.util.context.SpringContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 系统功能
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Service
public class ModuleServiceImpl extends SuperServiceImpl<ModuleMapper, ModuleEntity> implements ModuleService {

    @Autowired
    private ModuleButtonService moduleButtonService;
    @Autowired
    private ModuleColumnService moduleColumnService;
    @Autowired
    private ModuleDataAuthorizeService moduleDataAuthorizeService;
    @Autowired
    private ModuleButtonService buttonService;
    @Autowired
    private ModuleColumnService columnService;
    @Autowired
    private ModuleFormService formService;
    @Autowired
    private ModuleDataAuthorizeSchemeService schemeService;
    @Autowired
    private SystemService systemService;
    @Autowired
    private AuthorizeService authorizeService;
    @Autowired
    private FileExport fileExport;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private DbLinkService dbLinkService;


    @Override
    public List<ModuleEntity> getList(boolean filterFlowWork, List<String> moduleAuthorize, List<String> moduleUrlAddressAuthorize) {
        QueryWrapper<ModuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByAsc(ModuleEntity::getSortCode)
                .orderByDesc(ModuleEntity::getCreatorTime);
        // 移除工作流程菜单
        if (filterFlowWork) {
            List<String> moduleCode = JnpfConst.MODULE_CODE;
            queryWrapper.lambda().notIn(ModuleEntity::getEnCode, moduleCode);
        }
        if (moduleAuthorize.size() > 0) {
            queryWrapper.lambda().notIn(ModuleEntity::getId, moduleAuthorize);
        }
        if (moduleUrlAddressAuthorize.size() > 0) {
            queryWrapper.lambda().and(t-> t.notIn(ModuleEntity::getUrlAddress, moduleUrlAddressAuthorize).or().isNull(ModuleEntity::getUrlAddress));
        }
        return this.list(queryWrapper);
    }

    @Override
    public List<ModuleEntity> getList() {
        QueryWrapper<ModuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().isNotNull(ModuleEntity::getUrlAddress);
        try {
            if (!DbTypeUtil.checkOracle(dbLinkService.getResource("0"))) {
                queryWrapper.lambda().ne(ModuleEntity::getUrlAddress, "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        queryWrapper.lambda().orderByAsc(ModuleEntity::getSortCode)
                .orderByDesc(ModuleEntity::getCreatorTime);
        return this.list(queryWrapper);
    }

    @Override
    public List<ModuleEntity> getList(String systemId, String category, String keyword, Integer type, Integer enabledMark, String parentId, boolean release) {
        // 定义变量判断是否需要使用修改时间倒序
        boolean flag = false;
        QueryWrapper<ModuleEntity> queryWrapper = new QueryWrapper<>();
        QueryWrapper<SystemEntity> query = new QueryWrapper<>();
        query.lambda().eq(SystemEntity::getEnCode, JnpfConst.MAIN_SYSTEM_CODE);
        query.lambda().select(SystemEntity::getId);
        SystemEntity systemEntity = systemService.getOne(query);
        if ("0".equals(systemId) && release) {
            queryWrapper.lambda().eq(ModuleEntity::getCategory, "Web");
            queryWrapper.lambda().eq(ModuleEntity::getSystemId, systemEntity.getId());
            queryWrapper.lambda().select(ModuleEntity::getId);
            List<String> workModuleIds = this.list(queryWrapper).stream().map(ModuleEntity::getId).collect(Collectors.toList());
            // 重新定义一个查询对象
            queryWrapper = new QueryWrapper<>();
            if (!workModuleIds.isEmpty()) {
                queryWrapper.lambda().notIn(ModuleEntity::getId, workModuleIds);
            }
        }
        // 如果是主系统且不是管理员菜单需要去分级里面获取
        AuthorizeVO authorize = authorizeService.getAuthorizeByUser(true);
        List<String> collect = new ArrayList<>();
        // 根据系统id获取功能
        if (!"0".equals(systemId)) {
            collect = authorize.getModuleList().stream().filter(t -> t.getSystemId().equals(systemId)).map(ModuleModel::getId).collect(Collectors.toList());
        } else {
            collect = authorize.getModuleList().stream().map(ModuleModel::getId).distinct().collect(Collectors.toList());
        }
        collect.add("");
        List<List<String>> lists = Lists.partition(collect, 1000);
        queryWrapper.lambda().and(t-> {
            for (List<String> list : lists) {
                t.in(ModuleEntity::getId, list).or();
            }
        });
        if (!StringUtil.isEmpty(category)) {
            queryWrapper.lambda().eq(ModuleEntity::getCategory, category);
        }
        if (!StringUtil.isEmpty(keyword)) {
            flag = true;
            queryWrapper.lambda().and(t -> t.like(ModuleEntity::getFullName, keyword)
                    .or().like(ModuleEntity::getEnCode, keyword)
                    .or().like(ModuleEntity::getUrlAddress, keyword)
            );
        }
        if (type != null) {
            flag = true;
            queryWrapper.lambda().eq(ModuleEntity::getType, type);
        }
        if (enabledMark != null) {
            flag = true;
            queryWrapper.lambda().eq(ModuleEntity::getEnabledMark, enabledMark);
        }
        if (StringUtil.isNotEmpty(parentId)) {
            queryWrapper.lambda().eq(ModuleEntity::getParentId, parentId);
        }
        queryWrapper.lambda().orderByAsc(ModuleEntity::getSortCode)
                .orderByDesc(ModuleEntity::getCreatorTime);
        if (flag) {
            queryWrapper.lambda().orderByDesc(ModuleEntity::getLastModifyTime);
        }
        // 移除工作流程菜单
        List<String> moduleCode = JnpfConst.MODULE_CODE;
        queryWrapper.lambda().notIn(ModuleEntity::getEnCode, moduleCode);
        return this.list(queryWrapper);
    }

    @Override
    public List<ModuleEntity> getList(String id) {
        QueryWrapper<ModuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ModuleEntity::getParentId, id);
        queryWrapper.lambda().orderByAsc(ModuleEntity::getSortCode)
                .orderByDesc(ModuleEntity::getCreatorTime);
        return this.list(queryWrapper);
    }

    @Override
    public ModuleEntity getInfo(String id) {
        QueryWrapper<ModuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ModuleEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public ModuleEntity getInfo(String id, String systemId) {
        QueryWrapper<ModuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ModuleEntity::getId, id);
        queryWrapper.lambda().eq(ModuleEntity::getSystemId, systemId);
        return this.getOne(queryWrapper);
    }

    @Override
    public List<ModuleEntity> getInfoByFullName(String fullName, String systemId) {
        QueryWrapper<ModuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ModuleEntity::getFullName, fullName);
        queryWrapper.lambda().eq(ModuleEntity::getSystemId, systemId);
        return this.list(queryWrapper);
    }

    @Override
    public ModuleEntity getInfo(String id, String systemId, String parentId) {
        QueryWrapper<ModuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ModuleEntity::getId, id);
        queryWrapper.lambda().eq(ModuleEntity::getSystemId, systemId);
        queryWrapper.lambda().eq(ModuleEntity::getParentId, parentId);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean isExistByFullName(ModuleEntity entity, String category, String systemId) {
        QueryWrapper<ModuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ModuleEntity::getFullName, entity.getFullName()).eq(ModuleEntity::getCategory, category);
        if (!StringUtil.isEmpty(entity.getId())) {
            queryWrapper.lambda().ne(ModuleEntity::getId, entity.getId());
        }
        queryWrapper.lambda().eq(ModuleEntity::getParentId, entity.getParentId());
        // 通过系统id查询
        queryWrapper.lambda().eq(ModuleEntity::getSystemId, systemId);

        List<ModuleEntity> entityList = this.list(queryWrapper);
        if (entityList.size() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isExistByEnCode(ModuleEntity entity, String category, String systemId) {
        QueryWrapper<ModuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ModuleEntity::getEnCode, entity.getEnCode()).eq(ModuleEntity::getCategory, category);
        if (!StringUtil.isEmpty(entity.getId())) {
            queryWrapper.lambda().ne(ModuleEntity::getId, entity.getId());
        }

        List<ModuleEntity> entityList = this.list(queryWrapper);
        if (entityList.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    @DSTransactional
    @Override
    public void delete(ModuleEntity entity) {
        this.removeById(entity.getId());
        QueryWrapper<ModuleButtonEntity> buttonWrapper = new QueryWrapper<>();
        buttonWrapper.lambda().eq(ModuleButtonEntity::getModuleId, entity.getId());
        moduleButtonService.remove(buttonWrapper);
        QueryWrapper<ModuleColumnEntity> columnWrapper = new QueryWrapper<>();
        columnWrapper.lambda().eq(ModuleColumnEntity::getModuleId, entity.getId());
        moduleColumnService.remove(columnWrapper);
        QueryWrapper<ModuleDataAuthorizeEntity> dataWrapper = new QueryWrapper<>();
        dataWrapper.lambda().eq(ModuleDataAuthorizeEntity::getModuleId, entity.getId());
        moduleDataAuthorizeService.remove(dataWrapper);
        Object service = SpringContext.getBean("appDataServiceImpl");
        Class[] types = new Class[]{String.class};
        Object[] datas = new Object[]{entity.getId()};
        ReflectionUtil.invokeMethod(service, "delete", types, datas);
    }

    @Override
    public void deleteBySystemId(String systemId) {
        QueryWrapper<ModuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ModuleEntity::getSystemId, systemId);
        this.remove(queryWrapper);
    }

    @DSTransactional
    @Override
    public void deleteModule(ModuleEntity entity) {
        QueryWrapper<ModuleButtonEntity> buttonWrapper = new QueryWrapper<>();
        buttonWrapper.lambda().eq(ModuleButtonEntity::getModuleId, entity.getId());
        moduleButtonService.remove(buttonWrapper);
        QueryWrapper<ModuleColumnEntity> columnWrapper = new QueryWrapper<>();
        columnWrapper.lambda().eq(ModuleColumnEntity::getModuleId, entity.getId());
        moduleColumnService.remove(columnWrapper);
        QueryWrapper<ModuleFormEntity> formWrapper = new QueryWrapper<>();
        formWrapper.lambda().eq(ModuleFormEntity::getModuleId, entity.getId());
        formService.remove(formWrapper);
    }

    @DSTransactional
    @Override
    public void create(ModuleEntity entity) {
//        boolean flag = false;
//        if (entity.getId() == null) {
        entity.setId(RandomUtil.uuId());
//            flag = true;
//        }
        this.save(entity);
        //添加默认按钮
        if ("3".equals(String.valueOf(entity.getType()))) {
//            Map<String,Object> propJsonMap= JSONUtil.StringToMap(entity.getPropertyJson());
//            if(propJsonMap!=null){
//                VisualdevEntity visualdevEntity =visualdevService.getInfo(propJsonMap.get("moduleId").toString());
//                //去除模板中的F_
//                visualdevEntity= VisualUtil.delFKey(visualdevEntity);
//                if(visualdevEntity!=null){
//                    List<BtnData> btnData =new ArrayList<>();
//                    Map<String,Object> column=JSONUtil.StringToMap(visualdevEntity.getColumnData());
//                    if(column.get("columnBtnsList")!=null){
//                        btnData.addAll(JSONUtil.getJsonToList(JSONUtil.getJsonToListMap(column.get("columnBtnsList").toString()),BtnData.class));
//                    }
//                    if(column.get("btnsList")!=null){
//                        btnData.addAll(JSONUtil.getJsonToList(JSONUtil.getJsonToListMap(column.get("btnsList").toString()),BtnData.class));
//                    }
//                    if(btnData.size()>0){
//                        for(BtnData btn:btnData){
//                            ModuleButtonEntity moduleButtonEntity=new ModuleButtonEntity();
//                            moduleButtonEntity.setId(RandomUtil.uuId());
//                            moduleButtonEntity.setParentId("-1");
//                            moduleButtonEntity.setFullName(btn.getLabel());
//                            moduleButtonEntity.setEnCode("btn_"+btn.getValue());
//                            moduleButtonEntity.setSortCode(0L);
//                            moduleButtonEntity.setModuleId(entity.getId());
//                            moduleButtonEntity.setEnabledMark(1);
//                            moduleButtonEntity.setIcon(btn.getIcon());
//                            moduleButtonService.save(moduleButtonEntity);
//                        }
//                    }
//                    List<IndexGridField6Model> indexGridField6Models =new ArrayList<>();
//                    if(column.get("columnList")!=null){
//                        indexGridField6Models.addAll(JSONUtil.getJsonToList(JSONUtil.getJsonToListMap(column.get("columnList").toString()),IndexGridField6Model.class));
//                       if(indexGridField6Models.size()>0){
//                           for(IndexGridField6Model field6Model:indexGridField6Models){
//                               ModuleColumnEntity moduleColumnEntity=new ModuleColumnEntity();
//                               moduleColumnEntity.setId(RandomUtil.uuId());
//                               moduleColumnEntity.setParentId("-1");
//                               moduleColumnEntity.setFullName(field6Model.getLabel());
//                               moduleColumnEntity.setEnCode(field6Model.getProp());
//                               moduleColumnEntity.setSortCode(0L);
//                               moduleColumnEntity.setModuleId(entity.getId());
//                               moduleColumnEntity.setEnabledMark(1);
//                               moduleColumnService.save(moduleColumnEntity);
//                           }
//                       }
//                    }
//                }
//            }
        } else if ("4".equals(String.valueOf(entity.getType()))) {
            for (int i = 0; i < 3; i++) {
                String fullName = "新增";
                String value = "add";
                String icon = "el-icon-plus";
                if (i == 1) {
                    fullName = "编辑";
                    value = "edit";
                    icon = "el-icon-edit";
                }
                if (i == 2) {
                    fullName = "删除";
                    value = "remove";
                    icon = "el-icon-delete";
                }
                ModuleButtonEntity moduleButtonEntity = new ModuleButtonEntity();
                moduleButtonEntity.setId(RandomUtil.uuId());
                moduleButtonEntity.setParentId("-1");
                moduleButtonEntity.setFullName(fullName);
                moduleButtonEntity.setEnCode("btn_" + value);
                moduleButtonEntity.setSortCode(0L);
                moduleButtonEntity.setModuleId(entity.getId());
                moduleButtonEntity.setEnabledMark(1);
                moduleButtonEntity.setIcon(icon);
                moduleButtonService.save(moduleButtonEntity);
            }

        }
    }

    @Override
    public boolean update(String id, ModuleEntity entity) {
        entity.setId(id);
        entity.setLastModifyTime(DateUtil.getNowDate());
        return this.updateById(entity);
    }

    @Override
    public DownloadVO exportData(String id) {
        //获取信息转model
        ModuleEntity moduleEntity = getInfo(id);
        List<ModuleButtonEntity> buttonServiceList = buttonService.getListByModuleIds(id);
        List<ModuleColumnEntity> columnServiceList = columnService.getList(id);
        List<ModuleDataAuthorizeSchemeEntity> schemeServiceList = schemeService.getList(id);
        List<ModuleDataAuthorizeEntity> authorizeServiceList = moduleDataAuthorizeService.getList(id);
        List<ModuleFormEntity> formList = formService.getList(id);
        ModuleExportModel exportModel = new ModuleExportModel();
        exportModel.setModuleEntity(moduleEntity);
        exportModel.setButtonEntityList(buttonServiceList);
        exportModel.setColumnEntityList(columnServiceList);
        exportModel.setFormEntityList(formList);
        exportModel.setSchemeEntityList(schemeServiceList);
        exportModel.setAuthorizeEntityList(authorizeServiceList);
        //导出文件
        DownloadVO downloadVO = fileExport.exportFile(exportModel, configValueUtil.getTemporaryFilePath(), moduleEntity.getFullName(), ModuleTypeEnum.SYSTEM_MODULE.getTableName());
        return downloadVO;
    }

    @Override
    @DSTransactional
    public ActionResult importData(ModuleExportModel exportModel, Integer type) throws DataException {
        try {
            StringBuilder message = new StringBuilder();
            ModuleEntity moduleEntity = exportModel.getModuleEntity();
            StringJoiner stringJoiner = new StringJoiner("、");
            if (getInfo(moduleEntity.getId()) != null) {
                stringJoiner.add("ID");
            }
            String id = moduleEntity.getId();
            moduleEntity.setId(null);
            if (isExistByEnCode(moduleEntity, moduleEntity.getCategory(), moduleEntity.getSystemId())) {
                stringJoiner.add("编码");
            }
            if (isExistByFullName(moduleEntity, moduleEntity.getCategory(), moduleEntity.getSystemId())) {
                stringJoiner.add("名称");
            }
            moduleEntity.setId(id);
            if (stringJoiner.length() > 0) {
                if (ObjectUtil.equal(type, 1)) {
                    String copyNum = UUID.randomUUID().toString().substring(0, 5);
                    moduleEntity.setFullName(moduleEntity.getFullName() + ".副本" + copyNum);
                    moduleEntity.setEnCode(moduleEntity.getEnCode() + copyNum);
                    moduleEntity.setId(RandomUtil.uuId());
                    this.setIgnoreLogicDelete().removeById(moduleEntity);
                    this.setIgnoreLogicDelete().saveOrUpdate(moduleEntity);
                }
            } else {
                this.setIgnoreLogicDelete().removeById(moduleEntity);
                this.setIgnoreLogicDelete().saveOrUpdate(moduleEntity);
            }
            if (stringJoiner.length() > 0) {
                message.append(stringJoiner.toString()).append("重复；");
            }
            StringJoiner exceptionMessage = new StringJoiner("、");
            StringJoiner IDMessage = new StringJoiner("、");
            StringJoiner fullNameMessage = new StringJoiner("、");
            StringJoiner enCodeMessage = new StringJoiner("、");
            //按钮
            List<ModuleButtonEntity> buttonEntityList = JsonUtil.getJsonToList(exportModel.getButtonEntityList(), ModuleButtonEntity.class);
            for (ModuleButtonEntity buttonEntity : buttonEntityList) {
                if (buttonService.getInfo(buttonEntity.getId()) != null) {
                    IDMessage.add(buttonEntity.getId());
                }
                if (buttonService.isExistByFullName(moduleEntity.getId(), buttonEntity.getFullName(), null)) {
                    fullNameMessage.add(buttonEntity.getFullName());
                }
                if (buttonService.isExistByEnCode(moduleEntity.getId(), buttonEntity.getEnCode(), null)) {
                    enCodeMessage.add(buttonEntity.getEnCode());
                }
                if (ObjectUtil.equal(type, 1)) {
                    buttonEntity.setId(RandomUtil.uuId());
                    buttonEntity.setModuleId(moduleEntity.getId());
                    if (fullNameMessage.length() > 0 || enCodeMessage.length() > 0) {
                        String copyNum = UUID.randomUUID().toString().substring(0, 5);
                        buttonEntity.setFullName(buttonEntity.getFullName() + ".副本" + copyNum);
                        buttonEntity.setEnCode(buttonEntity.getEnCode() + copyNum);
                    }
                    buttonService.setIgnoreLogicDelete().saveOrUpdate(buttonEntity);
                } else if (IDMessage.length() == 0 && fullNameMessage.length() == 0 && enCodeMessage.length() == 0) {
                    buttonService.setIgnoreLogicDelete().removeById(buttonEntity);
                    buttonEntity.setModuleId(moduleEntity.getId());
                    buttonService.setIgnoreLogicDelete().saveOrUpdate(buttonEntity);
                }
            }
            tmpMessage("buttonEntityList：" , message, exceptionMessage, IDMessage, fullNameMessage, enCodeMessage);
            //列表
            List<ModuleColumnEntity> columnEntityList = JsonUtil.getJsonToList(exportModel.getColumnEntityList(), ModuleColumnEntity.class);
            for (ModuleColumnEntity columnEntity : columnEntityList) {
                if (columnService.getInfo(columnEntity.getId()) != null) {
                    IDMessage.add(columnEntity.getId());
                }
                if (columnService.isExistByFullName(moduleEntity.getId(), columnEntity.getFullName(), null)) {
                    fullNameMessage.add(columnEntity.getFullName());
                }
                if (columnService.isExistByEnCode(moduleEntity.getId(), columnEntity.getEnCode(), null)) {
                    enCodeMessage.add(columnEntity.getEnCode());
                }
                if (ObjectUtil.equal(type, 1)) {
                    columnEntity.setId(RandomUtil.uuId());
                    columnEntity.setModuleId(moduleEntity.getId());
                    if (fullNameMessage.length() > 0 || enCodeMessage.length() > 0) {
                        String copyNum = UUID.randomUUID().toString().substring(0, 5);
                        columnEntity.setFullName(columnEntity.getFullName() + ".副本" + copyNum);
                        columnEntity.setEnCode(columnEntity.getEnCode() + copyNum);
                    }
                    columnService.setIgnoreLogicDelete().saveOrUpdate(columnEntity);
                } else if (IDMessage.length() == 0 && fullNameMessage.length() == 0 && enCodeMessage.length() == 0) {
                    columnService.setIgnoreLogicDelete().removeById(columnEntity);
                    columnEntity.setModuleId(moduleEntity.getId());
                    columnService.setIgnoreLogicDelete().saveOrUpdate(columnEntity);
                }
            }
            tmpMessage("columnEntityList：" , message, exceptionMessage, IDMessage, fullNameMessage, enCodeMessage);
            //表单
            List<ModuleFormEntity> formEntityList = JsonUtil.getJsonToList(exportModel.getFormEntityList(), ModuleFormEntity.class);
            for (ModuleFormEntity formEntity : formEntityList) {
                if (formService.getInfo(formEntity.getId()) != null) {
                    IDMessage.add(formEntity.getId());
                }
                if (formService.isExistByFullName(moduleEntity.getId(), formEntity.getFullName(), null)) {
                    fullNameMessage.add(formEntity.getFullName());
                }
                if (formService.isExistByEnCode(moduleEntity.getId(), formEntity.getEnCode(), null)) {
                    enCodeMessage.add(formEntity.getEnCode());
                }
                if (ObjectUtil.equal(type, 1)) {
                    formEntity.setId(RandomUtil.uuId());
                    formEntity.setModuleId(moduleEntity.getId());
                    if (fullNameMessage.length() > 0 || enCodeMessage.length() > 0) {
                        String copyNum = UUID.randomUUID().toString().substring(0, 5);
                        formEntity.setFullName(formEntity.getFullName() + ".副本" + copyNum);
                        formEntity.setEnCode(formEntity.getEnCode() + copyNum);
                    }
                    formService.setIgnoreLogicDelete().saveOrUpdate(formEntity);
                } else if (IDMessage.length() == 0 && fullNameMessage.length() == 0 && enCodeMessage.length() == 0) {
                    formService.setIgnoreLogicDelete().removeById(formEntity);
                    formEntity.setModuleId(moduleEntity.getId());
                    formService.setIgnoreLogicDelete().saveOrUpdate(formEntity);
                }
            }
            tmpMessage("formEntityList：" , message, exceptionMessage, IDMessage, fullNameMessage, enCodeMessage);
            //数据权限
            Map<String, String> authorizeId = new HashMap<>(16);
            List<ModuleDataAuthorizeEntity> authorizeEntityList = JsonUtil.getJsonToList(exportModel.getAuthorizeEntityList(), ModuleDataAuthorizeEntity.class);
            for (ModuleDataAuthorizeEntity authorizeEntity : authorizeEntityList) {
                if (moduleDataAuthorizeService.getInfo(authorizeEntity.getId()) != null) {
                    IDMessage.add(authorizeEntity.getId());
                }
                if (moduleDataAuthorizeService.isExistByFullName(moduleEntity.getId(), authorizeEntity.getFullName(), null)) {
                    fullNameMessage.add(authorizeEntity.getFullName());
                }
                if (moduleDataAuthorizeService.isExistByEnCode(moduleEntity.getId(), authorizeEntity.getEnCode(), null)) {
                    enCodeMessage.add(authorizeEntity.getEnCode());
                }
                if (ObjectUtil.equal(type, 1)) {
                    authorizeEntity.setId(RandomUtil.uuId());
                    authorizeEntity.setModuleId(moduleEntity.getId());
                    if (fullNameMessage.length() > 0 || enCodeMessage.length() > 0) {
                        String copyNum = UUID.randomUUID().toString().substring(0, 5);
                        authorizeEntity.setFullName(authorizeEntity.getFullName() + ".副本" + copyNum);
                        authorizeEntity.setEnCode(authorizeEntity.getEnCode() + copyNum);
                    }
                    moduleDataAuthorizeService.setIgnoreLogicDelete().saveOrUpdate(authorizeEntity);
                    authorizeId.put(authorizeEntity.getId(), authorizeEntity.getId());
                } else if (IDMessage.length() == 0 && fullNameMessage.length() == 0 && enCodeMessage.length() == 0) {
                    moduleDataAuthorizeService.setIgnoreLogicDelete().removeById(authorizeEntity);
                    authorizeEntity.setModuleId(moduleEntity.getId());
                    moduleDataAuthorizeService.setIgnoreLogicDelete().saveOrUpdate(authorizeEntity);
                }
            }
            tmpMessage("authorizeEntityList：" , message, exceptionMessage, IDMessage, fullNameMessage, enCodeMessage);
            //数据权限方案
            List<ModuleDataAuthorizeSchemeEntity> schemeEntityList = JsonUtil.getJsonToList(exportModel.getSchemeEntityList(), ModuleDataAuthorizeSchemeEntity.class);
            for (ModuleDataAuthorizeSchemeEntity schemeEntity : schemeEntityList) {
                if (schemeService.getInfo(schemeEntity.getId()) != null) {
                    IDMessage.add(schemeEntity.getId());
                }
                if (schemeService.isExistByFullName(moduleEntity.getId(), schemeEntity.getFullName(), null)) {
                    fullNameMessage.add(schemeEntity.getFullName());
                }
                if (schemeService.isExistByEnCode(moduleEntity.getId(), schemeEntity.getEnCode(), null)) {
                    enCodeMessage.add(schemeEntity.getEnCode());
                }
                if (ObjectUtil.equal(type, 1)) {
                    schemeEntity.setId(RandomUtil.uuId());
                    schemeEntity.setModuleId(moduleEntity.getId());
                    String conditionJson = schemeEntity.getConditionJson();
                    if (StringUtil.isNotEmpty(conditionJson)) {
                        for (String oldId : authorizeId.keySet()) {
                            conditionJson = conditionJson.replaceAll(oldId, authorizeId.get(oldId));
                        }
                    }
                    if (fullNameMessage.length() > 0 || enCodeMessage.length() > 0) {
                        String copyNum = UUID.randomUUID().toString().substring(0, 5);
                        schemeEntity.setFullName(schemeEntity.getFullName() + ".副本" + copyNum);
                        schemeEntity.setEnCode(schemeEntity.getEnCode() + copyNum);
                    }
                    schemeService.setIgnoreLogicDelete().saveOrUpdate(schemeEntity);
                } else if (IDMessage.length() == 0 && fullNameMessage.length() == 0 && enCodeMessage.length() == 0) {
                    schemeService.setIgnoreLogicDelete().removeById(schemeEntity);
                    schemeEntity.setModuleId(moduleEntity.getId());
                    schemeService.setIgnoreLogicDelete().saveOrUpdate(schemeEntity);
                }
            }
            tmpMessage("schemeEntityList：" , message, exceptionMessage, IDMessage, fullNameMessage, enCodeMessage);
            if (ObjectUtil.equal(type, 0) && message.length() > 0) {
                return ActionResult.fail(message.toString().substring(0, message.lastIndexOf("；")));
            }
            return ActionResult.success(MsgCode.IMP001.get());
        } catch (Exception e) {
            e.printStackTrace();
            //手动回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new DataException(e.getMessage());
        } finally {
            this.clearIgnoreLogicDelete();
            buttonService.clearIgnoreLogicDelete();
            columnService.clearIgnoreLogicDelete();
            formService.clearIgnoreLogicDelete();
            moduleDataAuthorizeService.clearIgnoreLogicDelete();
            schemeService.clearIgnoreLogicDelete();
        }
    }

    private void tmpMessage(String moduleType, StringBuilder message, StringJoiner exceptionMessage, StringJoiner IDMessage, StringJoiner fullNameMessage, StringJoiner enCodeMessage) {
        if (IDMessage.length() > 0) {
            exceptionMessage.add("ID（" + IDMessage.toString() + "）重复");
        }
        if (enCodeMessage.length() > 0) {
            exceptionMessage.add("编码（" + enCodeMessage.toString() + "）重复");
        }
        if (fullNameMessage.length() > 0) {
            exceptionMessage.add("名称（" + fullNameMessage.toString() + "）重复");
        }
        if (exceptionMessage.length() > 0) {
            message.append(moduleType + exceptionMessage.toString()).append("；");
            exceptionMessage = new StringJoiner("、");
            IDMessage = new StringJoiner("、");
            fullNameMessage = new StringJoiner("、");
            enCodeMessage = new StringJoiner("、");
        }
    }

    @Override
    @DSTransactional
    public List<ModuleEntity> getModuleList(String visualId) {
        QueryWrapper<ModuleEntity> moduleWrapper = new QueryWrapper<>();
        moduleWrapper.lambda().eq(ModuleEntity::getModuleId, visualId);
        return this.list(moduleWrapper);
    }

    @Override
    public List<ModuleEntity> getModuleBySystemIds(List<String> ids, List<String> moduleAuthorize, List<String> moduleUrlAddressAuthorize) {
        if (ids.size() == 0) {
            return new ArrayList<>();
        }
        QueryWrapper<ModuleEntity> queryWrapper = new QueryWrapper<>();
        if (moduleAuthorize != null && moduleAuthorize.size() > 0) {
            queryWrapper.lambda().notIn(ModuleEntity::getId, ids);
        }
        if (moduleUrlAddressAuthorize != null && moduleUrlAddressAuthorize.size() > 0) {
            queryWrapper.lambda().and(t-> t.notIn(ModuleEntity::getUrlAddress, moduleUrlAddressAuthorize).or().isNull(ModuleEntity::getUrlAddress));
        }
        queryWrapper.lambda().in(ModuleEntity::getSystemId, ids);
        queryWrapper.lambda().notIn(ModuleEntity::getEnCode, JnpfConst.MODULE_CODE);
        queryWrapper.lambda().eq(ModuleEntity::getEnabledMark, 1);
        return this.list(queryWrapper);
    }

    @Override
    public List<ModuleEntity> getModuleByPortal(List<String> portalIds) {
        if (portalIds.size() == 0) {
            return new ArrayList<>();
        }
        QueryWrapper<ModuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(ModuleEntity::getModuleId, portalIds);
        return this.list(queryWrapper);
    }

    @Override
    public List<ModuleEntity> getMainModule(List<String> moduleAuthorize, List<String> moduleUrlAddressAuthorize, boolean singletonOrg) {
        SystemEntity mainSystem = systemService.getInfoByEnCode(JnpfConst.MAIN_SYSTEM_CODE);
        QueryWrapper<ModuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ModuleEntity::getSystemId, mainSystem.getId());
        if (!singletonOrg) {
            queryWrapper.lambda().eq(ModuleEntity::getEnabledMark, 1);
        }
        // 移除工作流程菜单
        List<String> moduleCode = JnpfConst.MODULE_CODE;
        if (moduleAuthorize != null && moduleAuthorize.size() > 0) {
            queryWrapper.lambda().notIn(ModuleEntity::getId, moduleAuthorize);
        }
        if (moduleUrlAddressAuthorize != null && moduleUrlAddressAuthorize.size() > 0) {
            queryWrapper.lambda().and(t-> t.notIn(ModuleEntity::getUrlAddress, moduleUrlAddressAuthorize).or().isNull(ModuleEntity::getUrlAddress));
        }
        queryWrapper.lambda().notIn(ModuleEntity::getEnCode, moduleCode);
        queryWrapper.lambda().orderByAsc(ModuleEntity::getSortCode).orderByDesc(ModuleEntity::getCreatorTime);
        return this.list(queryWrapper);
    }

    @Override
    public List<ModuleEntity> getModuleByIds(List<String> ids, List<String> moduleAuthorize, List<String> moduleUrlAddressAuthorize, boolean singletonOrg) {
        if (ids.size() == 0) {
            return new ArrayList<>();
        }
        QueryWrapper<ModuleEntity> queryWrapper = new QueryWrapper<>();
        List<List<String>> lists = Lists.partition(ids, 1000);
        queryWrapper.lambda().and(t-> {
            for (List<String> list : lists) {
                t.in(ModuleEntity::getId, list).or();
            }
        });
        if (moduleAuthorize != null && moduleAuthorize.size() > 0) {
            queryWrapper.lambda().notIn(ModuleEntity::getId, moduleAuthorize);
        }
        if (moduleUrlAddressAuthorize != null && moduleUrlAddressAuthorize.size() > 0) {
            queryWrapper.lambda().and(t-> t.notIn(ModuleEntity::getUrlAddress, moduleUrlAddressAuthorize).or().isNull(ModuleEntity::getUrlAddress));
        }
        // 移除工作流程菜单
        List<String> moduleCode = JnpfConst.MODULE_CODE;
        queryWrapper.lambda().notIn(ModuleEntity::getEnCode, moduleCode);
        if (!singletonOrg) {
            queryWrapper.lambda().eq(ModuleEntity::getEnabledMark, 1);
        }
        queryWrapper.lambda().orderByAsc(ModuleEntity::getSortCode).orderByDesc(ModuleEntity::getCreatorTime);
        return this.list(queryWrapper);
    }
    @Override
    public List<ModuleEntity> getModuleByIds(List<String> ids) {
        if (ids.size() == 0) {
            return new ArrayList<>();
        }
        QueryWrapper<ModuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(ModuleEntity::getId, ids);
        // 移除工作流程菜单
        List<String> moduleCode = JnpfConst.MODULE_CODE;
        queryWrapper.lambda().notIn(ModuleEntity::getEnCode, moduleCode);
        queryWrapper.lambda().eq(ModuleEntity::getEnabledMark, 1);
        return this.list(queryWrapper);
    }
    @Override
    public List<ModuleEntity> getListByEnCode(List<String> enCodeList) {
        if (enCodeList.size() == 0) {
            return new ArrayList<>();
        }
        QueryWrapper<ModuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(ModuleEntity::getEnCode, enCodeList);
        queryWrapper.lambda().eq(ModuleEntity::getEnabledMark, 1);
        return this.list(queryWrapper);
    }

    @Override
    public List<ModuleEntity> findModuleAdmin(int mark, String id, List<String> moduleAuthorize, List<String> moduleUrlAddressAuthorize) {
        QueryWrapper<ModuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().notIn(ModuleEntity::getEnCode, JnpfConst.MODULE_CODE);
        if (StringUtil.isNotEmpty(id)) {
            queryWrapper.lambda().ne(ModuleEntity::getId, id);
        }
        if (mark == 1) {
            queryWrapper.lambda().eq(ModuleEntity::getEnabledMark, mark);
        }
        if (moduleAuthorize != null && moduleAuthorize.size() > 0) {
            queryWrapper.lambda().notIn(ModuleEntity::getId, moduleAuthorize);
        }
        if (moduleUrlAddressAuthorize != null && moduleUrlAddressAuthorize.size() > 0) {
            queryWrapper.lambda().and(t-> t.notIn(ModuleEntity::getUrlAddress, moduleUrlAddressAuthorize).or().isNull(ModuleEntity::getUrlAddress));
        }
        queryWrapper.lambda().orderByAsc(ModuleEntity::getSortCode).orderByDesc(ModuleEntity::getCreatorTime);
        return this.list(queryWrapper);
    }

    @Override
    public void getParentModule(List<ModuleEntity> data, Map<String, ModuleEntity> moduleEntityMap) {
        data.forEach(t -> {
            ModuleEntity moduleEntity = t;
            while (moduleEntity != null) {
                if (!moduleEntityMap.containsKey(moduleEntity.getId())) {
                    moduleEntityMap.put(moduleEntity.getId(), moduleEntity);
                }
                moduleEntity = this.getInfo(moduleEntity.getParentId());
            }
        });
    }

    @Override
    public List<ModuleEntity> getListByUrlAddress(List<String> ids, List<String> urlAddressList) {
        urlAddressList = urlAddressList.stream().filter(StringUtil::isNotEmpty).collect(Collectors.toList());
        if (ids.size() == 0) {
            return new ArrayList<>();
        }
        QueryWrapper<ModuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(ModuleEntity::getId, ids);
        if (urlAddressList.size() > 0) {
            queryWrapper.lambda().or().in(ModuleEntity::getUrlAddress, urlAddressList);
        }
        List<String> moduleCode = JnpfConst.MODULE_CODE;
        queryWrapper.lambda().notIn(ModuleEntity::getEnCode, moduleCode);
        return this.list(queryWrapper);
    }
}
