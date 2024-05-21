
package gas.service.impl;

import gas.entity.*;
import gas.mapper.GasBaseServiceMapper;
import gas.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gas.model.gasbaseservice.*;
import org.springframework.stereotype.Service;
import jnpf.base.service.SuperServiceImpl;
import java.math.BigDecimal;
import cn.hutool.core.util.ObjectUtil;
import jnpf.util.GeneraterSwapUtil;
import java.lang.reflect.Field;
import com.baomidou.mybatisplus.annotation.TableField;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import jnpf.base.model.ColumnDataModel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import jnpf.model.QueryAllModel;
import java.text.SimpleDateFormat;
import jnpf.util.*;
import java.util.*;
import jnpf.base.UserInfo;
import jnpf.permission.entity.UserEntity;
/**
 *
 * 服务区设置
 * 版本： V3.6
 * 版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * 作者： JNPF开发平台组
 * 日期： 2024-05-21
 */
@Service
public class GasBaseServiceServiceImpl extends SuperServiceImpl<GasBaseServiceMapper, GasBaseServiceEntity> implements GasBaseServiceService{
    @Autowired
    private GeneraterSwapUtil generaterSwapUtil;

    @Autowired
    private UserProvider userProvider;
    @Override
    public List<GasBaseServiceEntity> getList(GasBaseServicePagination gasBaseServicePagination){
        return getTypeList(gasBaseServicePagination,gasBaseServicePagination.getDataType());
    }
    /** 列表查询 */
    @Override
    public List<GasBaseServiceEntity> getTypeList(GasBaseServicePagination gasBaseServicePagination,String dataType){
        String userId=userProvider.get().getUserId();
        Map<String,Class> tableClassMap=new HashMap<>();
        tableClassMap.put("gas_base_service",GasBaseServiceEntity.class);

        MPJLambdaWrapper<GasBaseServiceEntity> wrapper = JoinWrappers
                .lambda("gas_base_service",GasBaseServiceEntity.class)
                .selectAll(GasBaseServiceEntity.class);
        MPJLambdaWrapper<GasBaseServiceEntity> wrapper2 = JoinWrappers
                .lambda("gas_base_service",GasBaseServiceEntity.class)
                .distinct().selectAll(GasBaseServiceEntity.class);

        QueryAllModel queryAllModel = new QueryAllModel();
        queryAllModel.setWrapper(wrapper);
        queryAllModel.setClassMap(tableClassMap);
        //数据过滤
        boolean isPc = ServletUtil.getHeader("jnpf-origin").equals("pc");
        String columnData = !isPc ? GasBaseServiceConstant.getAppColumnData() : GasBaseServiceConstant.getColumnData();
        ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(columnData, ColumnDataModel.class);
        String ruleJson = !isPc ? JsonUtil.getObjectToString(columnDataModel.getRuleListApp()) : JsonUtil.getObjectToString(columnDataModel.getRuleList());
        queryAllModel.setRuleJson(ruleJson);
        //高级查询
        boolean hasSuperQuery =  true;
        if (hasSuperQuery) {
            queryAllModel.setSuperJson(gasBaseServicePagination.getSuperQueryJson());
        }
        //数据权限
        boolean pcPermission = false;
        boolean appPermission = false;
        if (isPc && pcPermission && !userProvider.get().getIsAdministrator()) {
            queryAllModel.setModuleId(gasBaseServicePagination.getMenuId());
        }
        if (!isPc && appPermission && !userProvider.get().getIsAdministrator()) {
            queryAllModel.setModuleId(gasBaseServicePagination.getMenuId());
        }
        //拼接复杂条件
        wrapper = generaterSwapUtil.getConditionAllTable(queryAllModel);
        queryAllModel.setWrapper(wrapper2);
        wrapper2 = generaterSwapUtil.getConditionAllTable(queryAllModel);
        //其他条件拼接
        otherConditions(gasBaseServicePagination, wrapper, isPc);
        otherConditions(gasBaseServicePagination, wrapper2, isPc);

        if("0".equals(dataType)){
            Page<GasBaseServiceEntity> page=new Page<>(gasBaseServicePagination.getCurrentPage(), gasBaseServicePagination.getPageSize());
            IPage<GasBaseServiceEntity> userIPage=this.selectJoinListPage(page, GasBaseServiceEntity.class, wrapper2);
            List<Object> collect = userIPage.getRecords().stream().map(t -> t.getId()).collect(Collectors.toList());
            if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(collect)){
                wrapper.in(GasBaseServiceEntity::getId,collect);
            }
            List<GasBaseServiceEntity> result = this.selectJoinList(GasBaseServiceEntity.class, wrapper);
            return gasBaseServicePagination.setData(result,userIPage.getTotal());
        }else{
            List<GasBaseServiceEntity> list = this.selectJoinList(GasBaseServiceEntity.class, wrapper);
            if("2".equals(dataType)){
                List<String> selectIds = Arrays.asList(gasBaseServicePagination.getSelectIds());
                return list.stream().filter(t -> selectIds.contains(t.getId())).collect(Collectors.toList());
            }else{
                return list;
            }
        }

    }

    /**
    * 其他条件拼接
    */
    private void otherConditions(GasBaseServicePagination gasBaseServicePagination, MPJLambdaWrapper<GasBaseServiceEntity> wrapper, boolean isPc) {
        //关键词
        if(ObjectUtil.isNotEmpty(gasBaseServicePagination.getJnpfKeyword())){
        }
        //普通查询
        if(isPc){
            if(ObjectUtil.isNotEmpty(gasBaseServicePagination.getName())){
                String value = gasBaseServicePagination.getName() instanceof List ?
                    JsonUtil.getObjectToString(gasBaseServicePagination.getName()) :
                    String.valueOf(gasBaseServicePagination.getName());
                wrapper.like(GasBaseServiceEntity::getName,value);
            }

            if(ObjectUtil.isNotEmpty(gasBaseServicePagination.getManagementunit())){
                List<String> idList = new ArrayList<>();
                try {
                    String[][] managementunit = JsonUtil.getJsonToBean(gasBaseServicePagination.getManagementunit(),String[][].class);
                    for(int i=0;i<managementunit.length;i++){
                        if(managementunit[i].length>0){
                            idList.add(JsonUtil.getObjectToString(Arrays.asList(managementunit[i])));
                        }
                    }
                }catch (Exception e1){
                    try {
                        List<String> managementunit = JsonUtil.getJsonToList(gasBaseServicePagination.getManagementunit(),String.class);
                        if(managementunit.size()>0){
                            idList.addAll(managementunit);
                        }
                    }catch (Exception e2){
                        idList.add(String.valueOf(gasBaseServicePagination.getManagementunit()));
                    }
                }
                wrapper.and(t->{
                    idList.forEach(tt->{
                        t.like(GasBaseServiceEntity::getManagementunit, tt).or();
                    });
                });
            }

            if(ObjectUtil.isNotEmpty(gasBaseServicePagination.getBusinessunit())){
                List<String> idList = new ArrayList<>();
                try {
                    String[][] businessunit = JsonUtil.getJsonToBean(gasBaseServicePagination.getBusinessunit(),String[][].class);
                    for(int i=0;i<businessunit.length;i++){
                        if(businessunit[i].length>0){
                            idList.add(JsonUtil.getObjectToString(Arrays.asList(businessunit[i])));
                        }
                    }
                }catch (Exception e1){
                    try {
                        List<String> businessunit = JsonUtil.getJsonToList(gasBaseServicePagination.getBusinessunit(),String.class);
                        if(businessunit.size()>0){
                            idList.addAll(businessunit);
                        }
                    }catch (Exception e2){
                        idList.add(String.valueOf(gasBaseServicePagination.getBusinessunit()));
                    }
                }
                wrapper.and(t->{
                    idList.forEach(tt->{
                        t.like(GasBaseServiceEntity::getBusinessunit, tt).or();
                    });
                });
            }

            if(ObjectUtil.isNotEmpty(gasBaseServicePagination.getCity())){
                List<String> idList = new ArrayList<>();
                try {
                    String[][] city = JsonUtil.getJsonToBean(gasBaseServicePagination.getCity(),String[][].class);
                    for(int i=0;i<city.length;i++){
                        if(city[i].length>0){
                            idList.add(JsonUtil.getObjectToString(Arrays.asList(city[i])));
                        }
                    }
                }catch (Exception e1){
                    try {
                        List<String> city = JsonUtil.getJsonToList(gasBaseServicePagination.getCity(),String.class);
                        if(city.size()>0){
                            idList.add(JsonUtil.getObjectToString(city));
                        }
                    }catch (Exception e2){
                        idList.add(String.valueOf(gasBaseServicePagination.getCity()));
                    }
                }
                wrapper.and(t->{
                    idList.forEach(tt->{
                        t.like(GasBaseServiceEntity::getCity, tt).or();
                    });
                });
            }

            if(ObjectUtil.isNotEmpty(gasBaseServicePagination.getHighwayType())){
                List<String> idList = new ArrayList<>();
                try {
                    String[][] highwayType = JsonUtil.getJsonToBean(gasBaseServicePagination.getHighwayType(),String[][].class);
                    for(int i=0;i<highwayType.length;i++){
                        if(highwayType[i].length>0){
                            idList.add(JsonUtil.getObjectToString(Arrays.asList(highwayType[i])));
                        }
                    }
                }catch (Exception e1){
                    try {
                        List<String> highwayType = JsonUtil.getJsonToList(gasBaseServicePagination.getHighwayType(),String.class);
                        if(highwayType.size()>0){
                            idList.addAll(highwayType);
                        }
                    }catch (Exception e2){
                        idList.add(String.valueOf(gasBaseServicePagination.getHighwayType()));
                    }
                }
                wrapper.and(t->{
                    idList.forEach(tt->{
                        t.like(GasBaseServiceEntity::getHighwayType, tt).or();
                    });
                });
            }

            if(ObjectUtil.isNotEmpty(gasBaseServicePagination.getType())){
                List<String> idList = new ArrayList<>();
                try {
                    String[][] type = JsonUtil.getJsonToBean(gasBaseServicePagination.getType(),String[][].class);
                    for(int i=0;i<type.length;i++){
                        if(type[i].length>0){
                            idList.add(JsonUtil.getObjectToString(Arrays.asList(type[i])));
                        }
                    }
                }catch (Exception e1){
                    try {
                        List<String> type = JsonUtil.getJsonToList(gasBaseServicePagination.getType(),String.class);
                        if(type.size()>0){
                            idList.addAll(type);
                        }
                    }catch (Exception e2){
                        idList.add(String.valueOf(gasBaseServicePagination.getType()));
                    }
                }
                wrapper.and(t->{
                    idList.forEach(tt->{
                        t.like(GasBaseServiceEntity::getType, tt).or();
                    });
                });
            }

            if(ObjectUtil.isNotEmpty(gasBaseServicePagination.getDirection())){
                List<String> idList = new ArrayList<>();
                try {
                    String[][] direction = JsonUtil.getJsonToBean(gasBaseServicePagination.getDirection(),String[][].class);
                    for(int i=0;i<direction.length;i++){
                        if(direction[i].length>0){
                            idList.add(JsonUtil.getObjectToString(Arrays.asList(direction[i])));
                        }
                    }
                }catch (Exception e1){
                    try {
                        List<String> direction = JsonUtil.getJsonToList(gasBaseServicePagination.getDirection(),String.class);
                        if(direction.size()>0){
                            idList.addAll(direction);
                        }
                    }catch (Exception e2){
                        idList.add(String.valueOf(gasBaseServicePagination.getDirection()));
                    }
                }
                wrapper.and(t->{
                    idList.forEach(tt->{
                        t.like(GasBaseServiceEntity::getDirection, tt).or();
                    });
                });
            }

            if(ObjectUtil.isNotEmpty(gasBaseServicePagination.getDirectiondriving())){
                String value = gasBaseServicePagination.getDirectiondriving() instanceof List ?
                    JsonUtil.getObjectToString(gasBaseServicePagination.getDirectiondriving()) :
                    String.valueOf(gasBaseServicePagination.getDirectiondriving());
                wrapper.like(GasBaseServiceEntity::getDirectiondriving,value);
            }

            if(ObjectUtil.isNotEmpty(gasBaseServicePagination.getEntrance())){
                List<String> idList = new ArrayList<>();
                try {
                    String[][] entrance = JsonUtil.getJsonToBean(gasBaseServicePagination.getEntrance(),String[][].class);
                    for(int i=0;i<entrance.length;i++){
                        if(entrance[i].length>0){
                            idList.add(JsonUtil.getObjectToString(Arrays.asList(entrance[i])));
                        }
                    }
                }catch (Exception e1){
                    try {
                        List<String> entrance = JsonUtil.getJsonToList(gasBaseServicePagination.getEntrance(),String.class);
                        if(entrance.size()>0){
                            idList.addAll(entrance);
                        }
                    }catch (Exception e2){
                        idList.add(String.valueOf(gasBaseServicePagination.getEntrance()));
                    }
                }
                wrapper.and(t->{
                    idList.forEach(tt->{
                        t.like(GasBaseServiceEntity::getEntrance, tt).or();
                    });
                });
            }

            if(ObjectUtil.isNotEmpty(gasBaseServicePagination.getExport())){
                List<String> idList = new ArrayList<>();
                try {
                    String[][] export = JsonUtil.getJsonToBean(gasBaseServicePagination.getExport(),String[][].class);
                    for(int i=0;i<export.length;i++){
                        if(export[i].length>0){
                            idList.add(JsonUtil.getObjectToString(Arrays.asList(export[i])));
                        }
                    }
                }catch (Exception e1){
                    try {
                        List<String> export = JsonUtil.getJsonToList(gasBaseServicePagination.getExport(),String.class);
                        if(export.size()>0){
                            idList.addAll(export);
                        }
                    }catch (Exception e2){
                        idList.add(String.valueOf(gasBaseServicePagination.getExport()));
                    }
                }
                wrapper.and(t->{
                    idList.forEach(tt->{
                        t.like(GasBaseServiceEntity::getExport, tt).or();
                    });
                });
            }

        }
        //排序
        if(StringUtil.isEmpty(gasBaseServicePagination.getSidx())){
                wrapper.orderByDesc(GasBaseServiceEntity::getId);
        }else{
            try {
                String[] split = gasBaseServicePagination.getSidx().split(",");
                for(String sidx:split){
                GasBaseServiceEntity gasBaseServiceEntity = new GasBaseServiceEntity();
                    if (sidx.startsWith("-")) {
                        Field declaredField = gasBaseServiceEntity.getClass().getDeclaredField(sidx.substring(1));
                        declaredField.setAccessible(true);
                        wrapper.orderByDesc(declaredField.getAnnotation(TableField.class).value());
                    } else {
                        Field declaredField = gasBaseServiceEntity.getClass().getDeclaredField(sidx);
                        declaredField.setAccessible(true);
                        wrapper.orderByAsc(declaredField.getAnnotation(TableField.class).value());
                    }
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public GasBaseServiceEntity getInfo(String id){
        MPJLambdaWrapper<GasBaseServiceEntity> wrapper = JoinWrappers
                .lambda("gas_base_service",GasBaseServiceEntity.class)
                .selectAll(GasBaseServiceEntity.class);
        wrapper.and(
            t->t.eq(GasBaseServiceEntity::getId,id)
        );
        return this.selectJoinOne(GasBaseServiceEntity.class,wrapper);
    }
    @Override
    public void create(GasBaseServiceEntity entity){
        this.save(entity);
    }
    @Override
    public boolean update(String id, GasBaseServiceEntity entity){
        return this.updateById(entity);
    }
    @Override
    public void delete(GasBaseServiceEntity entity){
        if(entity!=null){
            this.removeById(entity.getId());
        }
    }
    /** 验证表单唯一字段，正则，非空 i-0新增-1修改*/
    @Override
    public String checkForm(GasBaseServiceForm form,int i) {
        boolean isUp =StringUtil.isNotEmpty(form.getId()) && !form.getId().equals("0");
        String id="";
        String countRecover = "";
        if (isUp){
            id = form.getId();
        }
        //主表字段验证
                if(StringUtil.isEmpty(form.getName())){
                    return "服务区名不能为空";
                }
                if(StringUtil.isEmpty(form.getDistrictCode())){
                    return "行政区划不能为空";
                }
                if(StringUtil.isEmpty(form.getPileNumber())){
                    return "桩号不能为空";
                }
                if(StringUtil.isEmpty(form.getDirectiondriving())){
                    return "行车方向不能为空";
                }
                if(StringUtil.isEmpty(form.getLongitude())){
                    return "经度不能为空";
                }
                if(StringUtil.isEmpty(form.getLatitude())){
                    return "纬度不能为空";
                }
        return countRecover;
    }
    /**
    * 新增修改数据(事务回滚)
    * @param id
    * @param gasBaseServiceForm
    * @return
    */
    @Override
    @Transactional
    public void saveOrUpdate(GasBaseServiceForm gasBaseServiceForm,String id, boolean isSave) throws Exception{
        UserInfo userInfo=userProvider.get();
        UserEntity userEntity = generaterSwapUtil.getUser(userInfo.getUserId());
        gasBaseServiceForm = JsonUtil.getJsonToBean(
        generaterSwapUtil.swapDatetime(GasBaseServiceConstant.getFormData(),gasBaseServiceForm,GasBaseServiceConstant.TABLERENAMES),GasBaseServiceForm.class);
        GasBaseServiceEntity entity = JsonUtil.getJsonToBean(gasBaseServiceForm, GasBaseServiceEntity.class);
        if(isSave){
            entity.setCode(generaterSwapUtil.getBillNumber("serviceCode", false));
            entity.setId(RandomUtil.uuId());
        }
        else{
            if(StringUtil.isEmpty(entity.getCode())){
            entity.setCode(generaterSwapUtil.getBillNumber("serviceCode", false));
            }
        }
        this.saveOrUpdate(entity);

    }
}
