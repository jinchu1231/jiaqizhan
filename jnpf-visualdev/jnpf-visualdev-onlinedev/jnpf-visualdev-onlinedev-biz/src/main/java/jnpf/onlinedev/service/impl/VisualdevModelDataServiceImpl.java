package jnpf.onlinedev.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.base.ActionResult;
import jnpf.base.UserInfo;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.entity.VisualdevReleaseEntity;
import jnpf.base.model.ColumnDataModel;
import jnpf.base.model.FormDataField;
import jnpf.base.model.VisualDevJsonModel;
import jnpf.base.service.DbLinkService;
import jnpf.base.service.SuperServiceImpl;
import jnpf.base.service.VisualdevReleaseService;
import jnpf.base.service.VisualdevService;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.MsgCode;
import jnpf.database.model.entity.DbLinkEntity;
import jnpf.database.util.TenantDataSourceUtil;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.service.FlowTaskService;
import jnpf.exception.DataException;
import jnpf.exception.WorkFlowException;
import jnpf.model.flow.DataModel;
import jnpf.model.visualJson.FieLdsModel;
import jnpf.model.visualJson.FormCloumnUtil;
import jnpf.model.visualJson.FormDataModel;
import jnpf.model.visualJson.TableModel;
import jnpf.model.visualJson.analysis.FormAllModel;
import jnpf.model.visualJson.analysis.FormModel;
import jnpf.model.visualJson.analysis.RecursionForm;
import jnpf.onlinedev.entity.VisualdevModelDataEntity;
import jnpf.onlinedev.mapper.VisualdevModelDataMapper;
import jnpf.onlinedev.model.OnlineDevData;
import jnpf.onlinedev.model.OnlineDevListModel.VisualColumnSearchVO;
import jnpf.onlinedev.model.PaginationModel;
import jnpf.onlinedev.model.PaginationModelExport;
import jnpf.onlinedev.model.VisualdevModelDataCrForm;
import jnpf.onlinedev.model.VisualdevModelDataInfoVO;
import jnpf.onlinedev.service.VisualDevListService;
import jnpf.onlinedev.service.VisualdevModelDataService;
import jnpf.onlinedev.util.OnlineDevDbUtil;
import jnpf.onlinedev.util.onlineDevUtil.OnlineDevInfoUtils;
import jnpf.onlinedev.util.onlineDevUtil.OnlineProductSqlUtils;
import jnpf.onlinedev.util.onlineDevUtil.OnlinePublicUtils;
import jnpf.onlinedev.util.onlineDevUtil.OnlineSwapDataUtils;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.FlowFormDataUtil;
import jnpf.util.FormCheckUtils;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.TableFeildsEnum;
import jnpf.util.UserProvider;
import jnpf.util.context.RequestContext;
import jnpf.util.visiual.JnpfKeyConsts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * 0代码功能数据表
 * 版本： V3.0.0
 * 版权： 引迈信息技术有限公司
 * 作者： 管理员/admin
 * 日期： 2020-07-24 11:59
 */
@Service
public class VisualdevModelDataServiceImpl extends SuperServiceImpl<VisualdevModelDataMapper, VisualdevModelDataEntity> implements VisualdevModelDataService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private DbLinkService dblinkService;
    @Autowired
    private VisualdevReleaseService visualdevReleaseService;
    @Autowired
    private VisualDevListService visualDevListService;
    @Autowired
    private OnlineSwapDataUtils onlineSwapDataUtils;
    @Autowired
    private OnlineDevInfoUtils onlineDevInfoUtils;
    @Autowired
    private OnlineDevDbUtil onlineDevDbUtil;
    @Autowired
    private FlowFormDataUtil flowFormDataUtil;
    @Autowired
    private FormCheckUtils formCheckUtils;
    @Autowired
    private UserService userService;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private VisualdevService visualdevService;
    @Autowired
    private VisualdevModelDataService visualdevModelDataService;
    @Autowired
    private FlowTaskService flowTaskService;



    @Override
    public List<VisualdevModelDataEntity> getList(String modelId) {
        QueryWrapper<VisualdevModelDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(VisualdevModelDataEntity::getVisualDevId, modelId);
        return this.list(queryWrapper);
    }
    /**
     * 表单字段
     * @param id
     * @param filterType  过滤类型，0或者不传为默认过滤子表和关联表单，1-弹窗配置需要过滤掉的类型
     * @return
     */
    @Override
    public List<FormDataField> fieldList(String id, Integer filterType) {
        VisualdevReleaseEntity entity = visualdevReleaseService.getById(id);
        FormDataModel formData = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);

        List<FieLdsModel> fieLdsModelList = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
        List<FieLdsModel> mainFieldModelList = new ArrayList<>();

        OnlinePublicUtils.recursionFields(mainFieldModelList,fieLdsModelList);
        //过滤掉无法传递的组件
        List<String> notInList=new ArrayList<>();
        notInList.add(JnpfKeyConsts.RELATIONFORM);
        notInList.add(JnpfKeyConsts.CHILD_TABLE);
        if (Objects.equals(filterType,1)) {
            notInList.add("link");
            notInList.add("button");
            notInList.add("JNPFText");
            notInList.add("alert");
            notInList.add(JnpfKeyConsts.POPUPSELECT);
            notInList.add(JnpfKeyConsts.QR_CODE);
            notInList.add(JnpfKeyConsts.BARCODE);
            notInList.add(JnpfKeyConsts.BILLRULE);
            notInList.add(JnpfKeyConsts.CREATEUSER);
            notInList.add(JnpfKeyConsts.CREATETIME);
            notInList.add(JnpfKeyConsts.UPLOADIMG);
            notInList.add(JnpfKeyConsts.UPLOADFZ);
            notInList.add(JnpfKeyConsts.MODIFYUSER);
            notInList.add(JnpfKeyConsts.MODIFYTIME);

            notInList.add(JnpfKeyConsts.CURRORGANIZE);
            notInList.add(JnpfKeyConsts.CURRPOSITION);
            notInList.add(JnpfKeyConsts.IFRAME);
            notInList.add(JnpfKeyConsts.RELATIONFORM_ATTR);
            notInList.add(JnpfKeyConsts.POPUPSELECT_ATTR);
        }

        List<FormDataField> formDataFieldList = mainFieldModelList.stream().filter(fieLdsModel ->
                !"".equals(fieLdsModel.getVModel())
                        && StringUtil.isNotEmpty(fieLdsModel.getVModel())
                        && !notInList.contains(fieLdsModel.getConfig().getJnpfKey())
        ).map(fieLdsModel -> {
            FormDataField formDataField = new FormDataField();
            formDataField.setLabel(fieLdsModel.getConfig().getLabel());
            formDataField.setVModel(fieLdsModel.getVModel());
            return formDataField;
        }).collect(Collectors.toList());

        return formDataFieldList;
    }

    @Override
    public List<Map<String, Object>> getPageList(VisualdevEntity entity, PaginationModel paginationModel) {
//        String json = null;
//        if (StringUtil.isNotEmpty(paginationModel.getKeyword())) {
//            Map<String, Object> map = new HashMap<>();
//            map.put(paginationModel.getRelationField(), paginationModel.getKeyword());
//            json = JsonUtil.getObjectToString(map);
//        }
//        paginationModel.setQueryJson(json);
        VisualDevJsonModel visualJsonModel = OnlinePublicUtils.getVisualJsonModel(entity);

        //判断请求客户端来源
        if (!RequestContext.isOrignPc()){
            visualJsonModel.setColumnData(visualJsonModel.getAppColumnData());
        }
        List<Map<String, Object>> dataList = visualDevListService.getRelationFormList(visualJsonModel, paginationModel);
        return dataList;
    }

    @Override
    public List<Map<String, Object>> exportData(String[] keys, PaginationModelExport paginationModelExport, VisualDevJsonModel visualDevJsonModel) {
        PaginationModel paginationModel =new PaginationModel();
        BeanUtil.copyProperties(paginationModelExport,paginationModel);
        List<String> keyList = Arrays.asList(keys);
        List<Map<String,Object>> noSwapDataList;
        ColumnDataModel columnDataModel = visualDevJsonModel.getColumnData();
        List<VisualColumnSearchVO> searchVOList = new ArrayList<>();
        List<TableModel> visualTables = visualDevJsonModel.getVisualTables();
        TableModel mainTable = visualTables.stream().filter(vi -> vi.getTypeId().equals("1")).findFirst().orElse(null);
        //解析控件
        FormDataModel formDataModel = visualDevJsonModel.getFormData();
        List<FieLdsModel> fieLdsModels = JsonUtil.getJsonToList(formDataModel.getFields(), FieLdsModel.class);
        RecursionForm recursionForm = new RecursionForm(fieLdsModels, visualTables);
        List<FormAllModel> formAllModel = new ArrayList<>();
        FormCloumnUtil.recursionForm(recursionForm, formAllModel);
        //封装查询条件
        if (StringUtil.isNotEmpty(paginationModel.getQueryJson())){
            Map<String, Object> keyJsonMap = JsonUtil.stringToMap(paginationModel.getQueryJson());
            searchVOList= JsonUtil.getJsonToList(columnDataModel.getSearchList(),VisualColumnSearchVO.class);
            searchVOList =	searchVOList.stream().map(searchVO->{
                searchVO.setValue(keyJsonMap.get(searchVO.getId()));
                return searchVO;
            }).filter(vo->vo.getValue()!=null && StringUtil.isNotEmpty(String.valueOf(vo.getValue()))).collect(Collectors.toList());
            //左侧树查询
            boolean b =false;
            if (columnDataModel.getTreeRelation()!=null){
                b = keyJsonMap.keySet().stream().anyMatch(t -> t.equalsIgnoreCase(String.valueOf(columnDataModel.getTreeRelation())));
            }
            if (b && keyJsonMap.size()>searchVOList.size()){
                String relation =String.valueOf(columnDataModel.getTreeRelation());
                VisualColumnSearchVO vo =new VisualColumnSearchVO();
                vo.setSearchType("1");
                vo.setVModel(relation);
                vo.setValue(keyJsonMap.get(relation));
                searchVOList.add(vo);
            }
        }
        //判断有无表
        List<VisualColumnSearchVO> searchVOS = new ArrayList<>();
        if (visualDevJsonModel.getVisualTables().size()>0){
            //当前用户信息
            UserInfo userInfo = userProvider.get();
            //菜单id
            String moduleId = paginationModel.getMenuId();
            //封装搜索数据
            OnlineProductSqlUtils.queryList(formAllModel,visualDevJsonModel,paginationModel);
            noSwapDataList =visualDevListService.getListWithTable(visualDevJsonModel,paginationModel,userInfo,moduleId,keyList);
        }else{
            noSwapDataList =visualDevListService.getWithoutTableData(visualDevJsonModel.getId());
            noSwapDataList = visualDevListService.getList(noSwapDataList, searchVOList, paginationModel);
        }

        //数据转换
        List<FieLdsModel> fields = new ArrayList<>();
        OnlinePublicUtils.recursionFields(fields, fieLdsModels);
        noSwapDataList = onlineSwapDataUtils.getSwapList(noSwapDataList, fields,visualDevJsonModel.getId(),false,new ArrayList<>());

        return noSwapDataList;
    }


    @Override
    public VisualdevModelDataEntity getInfo(String id) {
        QueryWrapper<VisualdevModelDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(VisualdevModelDataEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public VisualdevModelDataInfoVO infoDataChange(String id, VisualdevEntity visualdevEntity) throws IOException, ParseException, DataException, SQLException {
        FormDataModel formDataModel = JsonUtil.getJsonToBean(visualdevEntity.getFormData(), FormDataModel.class);
        List<FieLdsModel> modelList = JsonUtil.getJsonToList(formDataModel.getFields(), FieLdsModel.class);

        VisualdevModelDataEntity visualdevModelDataEntity = this.getInfo(id);

        List<FieLdsModel> childFieldModelList = new ArrayList<>();
        List<FieLdsModel> mainFieldModelList = new ArrayList<>();
        //二维码 条形码
        List<FormModel> models = new ArrayList<>();
        OnlinePublicUtils.recurseFiled(modelList, mainFieldModelList,childFieldModelList,models);

        if (visualdevModelDataEntity != null) {
            Map<String, Object>  DataMap = JsonUtil.stringToMap(visualdevModelDataEntity.getData());
            Map<String, Object> childTableMap = DataMap.entrySet().stream().filter(m -> m.getKey().contains("tableField"))
                .collect(Collectors.toMap((e) -> (String) e.getKey(),
                (e) -> ObjectUtil.isNotEmpty(e.getValue()) ? e.getValue() : ""));
            Map<String, Object> mainTableMap = DataMap.entrySet().stream().filter(m -> !m.getKey().contains("tableField"))
                .collect(Collectors.toMap((e) -> (String) e.getKey(),
                    (e) -> ObjectUtil.isNotEmpty(e.getValue()) ? e.getValue() : ""));
            mainTableMap = onlineDevInfoUtils.swapChildTableDataInfo(mainFieldModelList, mainTableMap,models);

            for (Map.Entry<String,Object> entry : childTableMap.entrySet()){
                List<Map<String, Object>> listMap = JsonUtil.getJsonToListMap(String.valueOf(entry.getValue()));
                FieLdsModel fieLdsModel = childFieldModelList.stream().filter(child -> child.getVModel().equalsIgnoreCase(entry.getKey())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(fieLdsModel)){
                    List<Map<String,Object>> tableValueList = new ArrayList<>();
                    if (Objects.nonNull(listMap)){
                        for (Map<String, Object> map : listMap){
                            Map<String,Object> childFieldMap  = onlineDevInfoUtils.swapChildTableDataInfo(fieLdsModel.getConfig().getChildren(),map,models);
                            tableValueList.add(childFieldMap);
                        }
                    }
                    Map<String,Object> childFieldsMap = new HashMap<>();
                    childFieldsMap.put(entry.getKey(),tableValueList);
                    mainTableMap.putAll(childFieldsMap);
                }
            }
            String objectToString = JsonUtilEx.getObjectToString(mainTableMap);
            VisualdevModelDataInfoVO vo = new VisualdevModelDataInfoVO();
            vo.setData(objectToString);
            vo.setId(id);
            return vo;
        }
        return null;
    }

    @Override
    public DataModel visualCreate(VisualdevEntity visualdevEntity,Map<String, Object> map,boolean isLink,boolean isUpload) throws Exception {
        FormDataModel formData = JsonUtil.getJsonToBean(visualdevEntity.getFormData(), FormDataModel.class);
        List<FieLdsModel> list = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
        List<TableModel> tableModels = JsonUtil.getJsonToList(visualdevEntity.getVisualTables(), TableModel.class);
        DbLinkEntity linkEntity = StringUtil.isNotEmpty(visualdevEntity.getDbLinkId()) ? dblinkService.getInfo(visualdevEntity.getDbLinkId()) : null;
        //是否开启并发锁
        Boolean concurrency = false;
        Integer primaryKeyPolicy = formData.getPrimaryKeyPolicy();
        if (formData.getConcurrencyLock()) {
            //初始化version值
            map.put(TableFeildsEnum.VERSION.getField() , 0);
            concurrency = true;
        }
        //单行唯一校验
        if(!isUpload) {
            String b = formCheckUtils.checkForm(list, map, linkEntity, tableModels, primaryKeyPolicy, formData.getLogicalDelete(), null);
            if (StringUtil.isNotEmpty(b)) {
                throw new WorkFlowException(b + "不能重复" );
            }
        }

        OnlineSwapDataUtils.swapDatetime(list,map);
        String mainId = RandomUtil.uuId();
        UserInfo userInfo = userProvider.get();
        UserEntity info = userService.getInfo(userInfo.getUserId());
        DataModel dataModel = DataModel.builder().dataNewMap(map).fieLdsModelList(list).tableModelList(tableModels)
                .mainId(mainId).link(linkEntity).userEntity(info).concurrencyLock(concurrency)
                .primaryKeyPolicy(primaryKeyPolicy).flowEnable(Objects.equals(visualdevEntity.getEnableFlow(),1))
                .linkOpen(isLink).build();
//        if (!StringUtil.isEmpty(visualdevEntity.getVisualTables()) && !OnlineDevData.TABLE_CONST.equals(visualdevEntity.getVisualTables())) {
            flowFormDataUtil.create(dataModel);
//        } else {
//            VisualdevModelDataEntity entity = new VisualdevModelDataEntity();
//            entity.setData(JsonUtilEx.getObjectToString(dataModel.getDataNewMap()));
//            entity.setVisualDevId(visualdevEntity.getId());
//            entity.setId(dataModel.getMainId());
//            entity.setSortcode(RandomUtil.parses());
//            entity.setCreatortime(new Date());
//            entity.setCreatoruserid(userProvider.get().getUserId());
//            entity.setEnabledmark(1);
//            this.save(entity);
//        }
        return dataModel;
    }

    @Override
    public DataModel visualUpdate(VisualdevEntity visualdevEntity, Map<String, Object> map,String id,boolean isUpload) throws WorkFlowException {
        ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(visualdevEntity.getColumnData(), ColumnDataModel.class);
        FormDataModel formData = JsonUtil.getJsonToBean(visualdevEntity.getFormData(), FormDataModel.class);
        List<FieLdsModel> list = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
        boolean inlineEdit = columnDataModel.getType() != null && columnDataModel.getType() == 4;
        if (inlineEdit) {
            list = JsonUtil.getJsonToList(columnDataModel.getColumnList(), FieLdsModel.class);
            list = list.stream().filter(f -> !f.getId().toLowerCase().contains(JnpfKeyConsts.CHILD_TABLE_PREFIX)).collect(Collectors.toList());
        }
        List<TableModel> tableModels = JsonUtil.getJsonToList(visualdevEntity.getVisualTables(), TableModel.class);
        TableModel mainT = tableModels.stream().filter(t -> t.getTypeId().equals("1" )).findFirst().orElse(null);
        DbLinkEntity linkEntity = StringUtil.isNotEmpty(visualdevEntity.getDbLinkId()) ? dblinkService.getInfo(visualdevEntity.getDbLinkId()) : null;
        //是否开启并发锁
        Boolean isConcurrencyLock = false;
        Integer primaryKeyPolicy = formData.getPrimaryKeyPolicy();
        if (formData.getConcurrencyLock()) {
            if (map.get(TableFeildsEnum.VERSION.getField() ) == null) {
                map.put(TableFeildsEnum.VERSION.getField() , 0);
            } else {
                boolean version = true;
                try {
                    version = onlineDevDbUtil.getVersion(mainT.getTable(), linkEntity, map, id, primaryKeyPolicy);
                }catch (Exception e){
                    throw new WorkFlowException(e.getMessage());
                }
                if (!version) {
                    throw new WorkFlowException(MsgCode.VS405.get());
                } else {
                    Integer vs = Integer.valueOf(String.valueOf(map.get(TableFeildsEnum.VERSION.getField() )));
                    map.put(TableFeildsEnum.VERSION.getField() , vs + 1);
                }
            }
            isConcurrencyLock = true;
        }
        //单行唯一校验
        if(!isUpload) {
            String b = formCheckUtils.checkForm(list, map, linkEntity, tableModels, primaryKeyPolicy, formData.getLogicalDelete(), id);
            if (StringUtil.isNotEmpty(b)) {
                throw new WorkFlowException(b + "不能重复" );
            }
        }
        OnlineSwapDataUtils.swapDatetime(list,map);
        UserInfo userInfo = userProvider.get();
        UserEntity info = userService.getInfo(userInfo.getUserId());
        DataModel dataModel = DataModel.builder().dataNewMap(map).fieLdsModelList(list).tableModelList(tableModels)
                .mainId(id) .link(linkEntity).userEntity(info).concurrencyLock(isConcurrencyLock)
                .primaryKeyPolicy(primaryKeyPolicy) .flowEnable(Objects.equals(visualdevEntity.getEnableFlow(),1)).build();
//        if (StringUtil.isEmpty(visualdevEntity.getVisualTables()) || OnlineDevData.TABLE_CONST.equals(visualdevEntity.getVisualTables())) {
//            VisualdevModelDataEntity entity = new VisualdevModelDataEntity();
//            entity.setData(JsonUtilEx.getObjectToString(dataModel.getDataNewMap()));
//            entity.setVisualDevId(visualdevEntity.getId());
//            entity.setId(dataModel.getMainId());
//            entity.setLastmodifytime(new Date());
//            entity.setLastmodifyuserid(userProvider.get().getUserId());
//            this.updateById(entity);
//        } else {
            flowFormDataUtil.update(dataModel);
//        }
        return dataModel;
    }

    @Override
    public DataModel visualCreate(VisualdevEntity visualdevEntity, Map<String, Object> map, boolean isLink) throws Exception {
        return visualCreate(visualdevEntity,map,isLink,false);
    }

    @Override
    public DataModel visualCreate(VisualdevEntity visualdevEntity, Map<String, Object> map) throws Exception {
        return visualCreate(visualdevEntity,map,false);
    }

    @Override
    public DataModel visualUpdate(VisualdevEntity visualdevEntity, Map<String, Object> map, String id) throws Exception {
        return visualUpdate(visualdevEntity,map,id,false);
    }

    @Override
    public void visualDelete(VisualdevEntity visualdevEntity,List<String> idsVoList) throws Exception{
        VisualDevJsonModel visualJsonModel = OnlinePublicUtils.getVisualJsonModel(visualdevEntity);
        //判断请求客户端来源
        if (!RequestContext.isOrignPc()) {
            visualJsonModel.setColumnData(visualJsonModel.getAppColumnData());
        }
        List<String> idsList = new ArrayList<>();
        StringJoiner  errMess=new StringJoiner(",");
        if (visualdevEntity.getEnableFlow() == 1) {
            for (String id : idsVoList) {
                FlowTaskEntity taskEntity = flowTaskService.getInfoSubmit(id);
                if (taskEntity != null) {
                    if (taskEntity.getStatus().equals(0) || taskEntity.getStatus().equals(4)) {
                        flowTaskService.delete(taskEntity);
                        idsList.add(id);
                    }
                } else {
                    idsList.add(id);
                }
            }
        } else {
            idsList = idsVoList;
        }
        if (idsList.size() == 0) {
            throw new WorkFlowException(errMess.toString());
        }
        if (!StringUtil.isEmpty(visualdevEntity.getVisualTables()) && !OnlineDevData.TABLE_CONST.equals(visualdevEntity.getVisualTables())) {
            for(String id:idsList){
                try {
                    tableDelete(id, visualJsonModel);
                }catch (Exception e){
                    throw new WorkFlowException(e.getMessage());
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(VisualdevModelDataEntity entity) {
        if (entity != null) {
            this.removeById(entity.getId());
        }
    }

    @Override
    public boolean tableDelete(String id,VisualDevJsonModel visualDevJsonModel) throws Exception {
        DbLinkEntity linkEntity = dblinkService.getInfo(visualDevJsonModel.getDbLinkId());
        VisualDevJsonModel model = BeanUtil.copyProperties(visualDevJsonModel, VisualDevJsonModel.class);
        return onlineDevDbUtil.deleteTable(id, model, linkEntity);
    }

    @Override
    public ActionResult tableDeleteMore(List<String> ids, VisualDevJsonModel visualDevJsonModel) throws Exception {
        List<String> dataInfoVOList = new ArrayList<>();
        for (String id : ids) {
            boolean isDel = tableDelete(id, visualDevJsonModel);
            if(isDel){
                dataInfoVOList.add(id);
            }
        }
        visualDevJsonModel.setDataIdList(dataInfoVOList);
        return ActionResult.success(MsgCode.SU003.get());
    }

    @Override
    public ActionResult createData(String modelId, String tenantId, VisualdevModelDataCrForm visualdevModelDataCrForm) throws WorkFlowException {
        try {
            if (configValueUtil.isMultiTenancy()) {
                if (StringUtil.isNotEmpty(tenantId)) {
                    //切换成租户库
                    TenantDataSourceUtil.switchTenant(tenantId);
                } else {
                    return ActionResult.fail("缺少租户信息" );
                }
            }
            VisualdevEntity visualdevEntity = visualdevService.getReleaseInfo(modelId);
            Map<String, Object> map = JsonUtil.stringToMap(visualdevModelDataCrForm.getData());
            visualdevModelDataService.visualCreate(visualdevEntity, map, true);
        }catch (Exception e){
            throw new WorkFlowException(e.getMessage());
        }
        return ActionResult.success(MsgCode.SU001.get());
    }

}
