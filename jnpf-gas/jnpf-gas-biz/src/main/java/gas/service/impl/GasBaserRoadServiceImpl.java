
package gas.service.impl;

import gas.entity.*;
import gas.mapper.GasBaserRoadMapper;
import gas.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gas.model.gasbaserroad.*;
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
 * 路段设置
 * 版本： V3.6
 * 版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * 作者： JNPF开发平台组
 * 日期： 2024-05-21
 */
@Service
public class GasBaserRoadServiceImpl extends SuperServiceImpl<GasBaserRoadMapper, GasBaserRoadEntity> implements GasBaserRoadService{
    @Autowired
    private GeneraterSwapUtil generaterSwapUtil;

    @Autowired
    private UserProvider userProvider;
    @Override
    public List<GasBaserRoadEntity> getList(GasBaserRoadPagination gasBaserRoadPagination){
        return getTypeList(gasBaserRoadPagination,gasBaserRoadPagination.getDataType());
    }
    /** 列表查询 */
    @Override
    public List<GasBaserRoadEntity> getTypeList(GasBaserRoadPagination gasBaserRoadPagination,String dataType){
        String userId=userProvider.get().getUserId();
        Map<String,Class> tableClassMap=new HashMap<>();
        tableClassMap.put("gas_base_road",GasBaserRoadEntity.class);

        MPJLambdaWrapper<GasBaserRoadEntity> wrapper = JoinWrappers
                .lambda("gas_base_road",GasBaserRoadEntity.class)
                .selectAll(GasBaserRoadEntity.class);
        MPJLambdaWrapper<GasBaserRoadEntity> wrapper2 = JoinWrappers
                .lambda("gas_base_road",GasBaserRoadEntity.class)
                .distinct().selectAll(GasBaserRoadEntity.class);

        QueryAllModel queryAllModel = new QueryAllModel();
        queryAllModel.setWrapper(wrapper);
        queryAllModel.setClassMap(tableClassMap);
        //数据过滤
        boolean isPc = ServletUtil.getHeader("jnpf-origin").equals("pc");
        String columnData = !isPc ? GasBaserRoadConstant.getAppColumnData() : GasBaserRoadConstant.getColumnData();
        ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(columnData, ColumnDataModel.class);
        String ruleJson = !isPc ? JsonUtil.getObjectToString(columnDataModel.getRuleListApp()) : JsonUtil.getObjectToString(columnDataModel.getRuleList());
        queryAllModel.setRuleJson(ruleJson);
        //高级查询
        boolean hasSuperQuery =  true;
        if (hasSuperQuery) {
            queryAllModel.setSuperJson(gasBaserRoadPagination.getSuperQueryJson());
        }
        //数据权限
        boolean pcPermission = false;
        boolean appPermission = false;
        if (isPc && pcPermission && !userProvider.get().getIsAdministrator()) {
            queryAllModel.setModuleId(gasBaserRoadPagination.getMenuId());
        }
        if (!isPc && appPermission && !userProvider.get().getIsAdministrator()) {
            queryAllModel.setModuleId(gasBaserRoadPagination.getMenuId());
        }
        //拼接复杂条件
        wrapper = generaterSwapUtil.getConditionAllTable(queryAllModel);
        queryAllModel.setWrapper(wrapper2);
        wrapper2 = generaterSwapUtil.getConditionAllTable(queryAllModel);
        //其他条件拼接
        otherConditions(gasBaserRoadPagination, wrapper, isPc);
        otherConditions(gasBaserRoadPagination, wrapper2, isPc);

        if("0".equals(dataType)){
            Page<GasBaserRoadEntity> page=new Page<>(gasBaserRoadPagination.getCurrentPage(), gasBaserRoadPagination.getPageSize());
            IPage<GasBaserRoadEntity> userIPage=this.selectJoinListPage(page, GasBaserRoadEntity.class, wrapper2);
            List<Object> collect = userIPage.getRecords().stream().map(t -> t.getId()).collect(Collectors.toList());
            if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(collect)){
                wrapper.in(GasBaserRoadEntity::getId,collect);
            }
            List<GasBaserRoadEntity> result = this.selectJoinList(GasBaserRoadEntity.class, wrapper);
            return gasBaserRoadPagination.setData(result,userIPage.getTotal());
        }else{
            List<GasBaserRoadEntity> list = this.selectJoinList(GasBaserRoadEntity.class, wrapper);
            if("2".equals(dataType)){
                List<String> selectIds = Arrays.asList(gasBaserRoadPagination.getSelectIds());
                return list.stream().filter(t -> selectIds.contains(t.getId())).collect(Collectors.toList());
            }else{
                return list;
            }
        }

    }

    /**
    * 其他条件拼接
    */
    private void otherConditions(GasBaserRoadPagination gasBaserRoadPagination, MPJLambdaWrapper<GasBaserRoadEntity> wrapper, boolean isPc) {
        //假删除标志
        wrapper.isNull(GasBaserRoadEntity::getDeleteMark);

        //关键词
        if(ObjectUtil.isNotEmpty(gasBaserRoadPagination.getJnpfKeyword())){
        }
        //普通查询
        if(isPc){
            if(ObjectUtil.isNotEmpty(gasBaserRoadPagination.getName())){
                String value = gasBaserRoadPagination.getName() instanceof List ?
                    JsonUtil.getObjectToString(gasBaserRoadPagination.getName()) :
                    String.valueOf(gasBaserRoadPagination.getName());
                wrapper.like(GasBaserRoadEntity::getName,value);
            }

        }
        //排序
        if(StringUtil.isEmpty(gasBaserRoadPagination.getSidx())){
                wrapper.orderByDesc(GasBaserRoadEntity::getId);
        }else{
            try {
                String[] split = gasBaserRoadPagination.getSidx().split(",");
                for(String sidx:split){
                GasBaserRoadEntity gasBaserRoadEntity = new GasBaserRoadEntity();
                    if (sidx.startsWith("-")) {
                        Field declaredField = gasBaserRoadEntity.getClass().getDeclaredField(sidx.substring(1));
                        declaredField.setAccessible(true);
                        wrapper.orderByDesc(declaredField.getAnnotation(TableField.class).value());
                    } else {
                        Field declaredField = gasBaserRoadEntity.getClass().getDeclaredField(sidx);
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
    public GasBaserRoadEntity getInfo(String id){
        MPJLambdaWrapper<GasBaserRoadEntity> wrapper = JoinWrappers
                .lambda("gas_base_road",GasBaserRoadEntity.class)
                .selectAll(GasBaserRoadEntity.class);
        wrapper.and(
            t->t.eq(GasBaserRoadEntity::getId,id)
        );
        return this.selectJoinOne(GasBaserRoadEntity.class,wrapper);
    }
    @Override
    public void create(GasBaserRoadEntity entity){
        this.save(entity);
    }
    @Override
    public boolean update(String id, GasBaserRoadEntity entity){
        return this.updateById(entity);
    }
    @Override
    public void delete(GasBaserRoadEntity entity){
        if(entity!=null){
            this.removeById(entity.getId());
        }
    }
    /** 验证表单唯一字段，正则，非空 i-0新增-1修改*/
    @Override
    public String checkForm(GasBaserRoadForm form,int i) {
        boolean isUp =StringUtil.isNotEmpty(form.getId()) && !form.getId().equals("0");
        String id="";
        String countRecover = "";
        if (isUp){
            id = form.getId();
        }
        //主表字段验证
                if(StringUtil.isEmpty(form.getName())){
                    return "路段名称不能为空";
                }
        return countRecover;
    }
    /**
    * 新增修改数据(事务回滚)
    * @param id
    * @param gasBaserRoadForm
    * @return
    */
    @Override
    @Transactional
    public void saveOrUpdate(GasBaserRoadForm gasBaserRoadForm,String id, boolean isSave) throws Exception{
        UserInfo userInfo=userProvider.get();
        UserEntity userEntity = generaterSwapUtil.getUser(userInfo.getUserId());
        gasBaserRoadForm = JsonUtil.getJsonToBean(
        generaterSwapUtil.swapDatetime(GasBaserRoadConstant.getFormData(),gasBaserRoadForm,GasBaserRoadConstant.TABLERENAMES),GasBaserRoadForm.class);
        GasBaserRoadEntity entity = JsonUtil.getJsonToBean(gasBaserRoadForm, GasBaserRoadEntity.class);
        if(isSave){
            entity.setCode(generaterSwapUtil.getBillNumber("road", false));
            entity.setId(RandomUtil.uuId());
            entity.setVersion(0);
        }
        else{
            if(StringUtil.isEmpty(entity.getCode())){
            entity.setCode(generaterSwapUtil.getBillNumber("road", false));
            }
        }
        this.saveOrUpdate(entity);

    }
}
