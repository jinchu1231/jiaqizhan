
package gas.service.impl;

import gas.entity.*;
import gas.mapper.GasBaseRouteMapper;
import gas.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gas.model.gasbaseroute.*;
import jnpf.base.model.ColumnDataModel;
import jnpf.model.QueryAllModel;
import org.springframework.stereotype.Service;
import jnpf.base.service.SuperServiceImpl;
import java.math.BigDecimal;
import cn.hutool.core.util.ObjectUtil;

import java.lang.reflect.Field;
import com.baomidou.mybatisplus.annotation.TableField;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.MPJLambdaWrapper;

import jnpf.util.*;
import java.util.*;
import jnpf.base.UserInfo;
import jnpf.permission.entity.UserEntity;
/**
 *
 * 路线管理
 * 版本： V3.6
 * 版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * 作者： JNPF开发平台组
 * 日期： 2024-05-21
 */
@Service
public class GasBaseRouteServiceImpl extends SuperServiceImpl<GasBaseRouteMapper, GasBaseRouteEntity> implements GasBaseRouteService{

    @Autowired
    private GeneraterSwapUtil generaterSwapUtil;

    @Autowired
    private UserProvider userProvider;
    @Override
    public List<GasBaseRouteEntity> getList(GasBaseRoutePagination gasBaseRoutePagination){
        return getTypeList(gasBaseRoutePagination,gasBaseRoutePagination.getDataType());
    }
    /** 列表查询 */
    @Override
    public List<GasBaseRouteEntity> getTypeList(GasBaseRoutePagination gasBaseRoutePagination,String dataType){
        String userId=userProvider.get().getUserId();
        Map<String,Class> tableClassMap=new HashMap<>();
        tableClassMap.put("gas_base_route",GasBaseRouteEntity.class);

        MPJLambdaWrapper<GasBaseRouteEntity> wrapper = JoinWrappers
                .lambda("gas_base_route",GasBaseRouteEntity.class)
                .selectAll(GasBaseRouteEntity.class);
        MPJLambdaWrapper<GasBaseRouteEntity> wrapper2 = JoinWrappers
                .lambda("gas_base_route",GasBaseRouteEntity.class)
                .distinct().selectAll(GasBaseRouteEntity.class);

        QueryAllModel queryAllModel = new QueryAllModel();
        queryAllModel.setWrapper(wrapper);
        queryAllModel.setClassMap(tableClassMap);
        //数据过滤
        boolean isPc = ServletUtil.getHeader("jnpf-origin").equals("pc");
        String columnData = !isPc ? GasBaseRouteConstant.getAppColumnData() : GasBaseRouteConstant.getColumnData();
        ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(columnData, ColumnDataModel.class);
        String ruleJson = !isPc ? JsonUtil.getObjectToString(columnDataModel.getRuleListApp()) : JsonUtil.getObjectToString(columnDataModel.getRuleList());
        queryAllModel.setRuleJson(ruleJson);
        //高级查询
        boolean hasSuperQuery =  true;
        if (hasSuperQuery) {
            queryAllModel.setSuperJson(gasBaseRoutePagination.getSuperQueryJson());
        }
        //数据权限
        boolean pcPermission = false;
        boolean appPermission = false;
        if (isPc && pcPermission && !userProvider.get().getIsAdministrator()) {
            queryAllModel.setModuleId(gasBaseRoutePagination.getMenuId());
        }
        if (!isPc && appPermission && !userProvider.get().getIsAdministrator()) {
            queryAllModel.setModuleId(gasBaseRoutePagination.getMenuId());
        }
        //拼接复杂条件
        wrapper = generaterSwapUtil.getConditionAllTable(queryAllModel);
        queryAllModel.setWrapper(wrapper2);
        wrapper2 = generaterSwapUtil.getConditionAllTable(queryAllModel);
        //其他条件拼接
        otherConditions(gasBaseRoutePagination, wrapper, isPc);
        otherConditions(gasBaseRoutePagination, wrapper2, isPc);

        if("0".equals(dataType)){
            Page<GasBaseRouteEntity> page=new Page<>(gasBaseRoutePagination.getCurrentPage(), gasBaseRoutePagination.getPageSize());
            IPage<GasBaseRouteEntity> userIPage=this.selectJoinListPage(page, GasBaseRouteEntity.class, wrapper2);
            List<Object> collect = userIPage.getRecords().stream().map(t -> t.getId()).collect(Collectors.toList());
            if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(collect)){
                wrapper.in(GasBaseRouteEntity::getId,collect);
            }
            List<GasBaseRouteEntity> result = this.selectJoinList(GasBaseRouteEntity.class, wrapper);
            return gasBaseRoutePagination.setData(result,userIPage.getTotal());
        }else{
            List<GasBaseRouteEntity> list = this.selectJoinList(GasBaseRouteEntity.class, wrapper);
            if("2".equals(dataType)){
                List<String> selectIds = Arrays.asList(gasBaseRoutePagination.getSelectIds());
                return list.stream().filter(t -> selectIds.contains(t.getId())).collect(Collectors.toList());
            }else{
                return list;
            }
        }

    }

    /**
    * 其他条件拼接
    */
    private void otherConditions(GasBaseRoutePagination gasBaseRoutePagination, MPJLambdaWrapper<GasBaseRouteEntity> wrapper, boolean isPc) {
        //假删除标志
        wrapper.isNull(GasBaseRouteEntity::getDeleteMark);

        //关键词
        if(ObjectUtil.isNotEmpty(gasBaseRoutePagination.getJnpfKeyword())){
        }
        //普通查询
        if(isPc){
            if(ObjectUtil.isNotEmpty(gasBaseRoutePagination.getName())){
                String value = gasBaseRoutePagination.getName() instanceof List ?
                    JsonUtil.getObjectToString(gasBaseRoutePagination.getName()) :
                    String.valueOf(gasBaseRoutePagination.getName());
                wrapper.like(GasBaseRouteEntity::getName,value);
            }

        }
        //排序
        if(StringUtil.isEmpty(gasBaseRoutePagination.getSidx())){
                wrapper.orderByDesc(GasBaseRouteEntity::getId);
        }else{
            try {
                String[] split = gasBaseRoutePagination.getSidx().split(",");
                for(String sidx:split){
                GasBaseRouteEntity gasBaseRouteEntity = new GasBaseRouteEntity();
                    if (sidx.startsWith("-")) {
                        Field declaredField = gasBaseRouteEntity.getClass().getDeclaredField(sidx.substring(1));
                        declaredField.setAccessible(true);
                        wrapper.orderByDesc(declaredField.getAnnotation(TableField.class).value());
                    } else {
                        Field declaredField = gasBaseRouteEntity.getClass().getDeclaredField(sidx);
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
    public GasBaseRouteEntity getInfo(String id){
        MPJLambdaWrapper<GasBaseRouteEntity> wrapper = JoinWrappers
                .lambda("gas_base_route",GasBaseRouteEntity.class)
                .selectAll(GasBaseRouteEntity.class);
        wrapper.and(
            t->t.eq(GasBaseRouteEntity::getId,id)
        );
        return this.selectJoinOne(GasBaseRouteEntity.class,wrapper);
    }
    @Override
    public void create(GasBaseRouteEntity entity){
        this.save(entity);
    }
    @Override
    public boolean update(String id, GasBaseRouteEntity entity){
        return this.updateById(entity);
    }
    @Override
    public void delete(GasBaseRouteEntity entity){
        if(entity!=null){
            this.removeById(entity.getId());
        }
    }
    /** 验证表单唯一字段，正则，非空 i-0新增-1修改*/
    @Override
    public String checkForm(GasBaseRouteForm form,int i) {
        boolean isUp =StringUtil.isNotEmpty(form.getId()) && !form.getId().equals("0");
        String id="";
        String countRecover = "";
        if (isUp){
            id = form.getId();
        }
        //主表字段验证
                if(StringUtil.isEmpty(form.getName())){
                    return "路线名称不能为空";
                }
        return countRecover;
    }
    /**
    * 新增修改数据(事务回滚)
    * @param id
    * @param gasBaseRouteForm
    * @return
    */
    @Override
    @Transactional
    public void saveOrUpdate(GasBaseRouteForm gasBaseRouteForm,String id, boolean isSave) throws Exception{
        UserInfo userInfo=userProvider.get();
        UserEntity userEntity = generaterSwapUtil.getUser(userInfo.getUserId());
        gasBaseRouteForm = JsonUtil.getJsonToBean(
        generaterSwapUtil.swapDatetime(GasBaseRouteConstant.getFormData(),gasBaseRouteForm,GasBaseRouteConstant.TABLERENAMES),GasBaseRouteForm.class);
        GasBaseRouteEntity entity = JsonUtil.getJsonToBean(gasBaseRouteForm, GasBaseRouteEntity.class);
        if(isSave){
            entity.setCode(generaterSwapUtil.getBillNumber("routeCode", false));
            entity.setId(RandomUtil.uuId());
        }
        else{
            if(StringUtil.isEmpty(entity.getCode())){
            entity.setCode(generaterSwapUtil.getBillNumber("routeCode", false));
            }
        }
        this.saveOrUpdate(entity);

    }
}
