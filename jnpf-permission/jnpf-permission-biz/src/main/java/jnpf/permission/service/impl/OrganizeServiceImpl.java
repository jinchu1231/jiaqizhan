package jnpf.permission.service.impl;

import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.google.common.collect.ImmutableMap;
import jnpf.base.ActionResult;
import jnpf.base.UserInfo;
import jnpf.base.service.SuperServiceImpl;
import jnpf.constant.MsgCode;
import jnpf.constant.PermissionConst;
import jnpf.exception.DataException;
import jnpf.permission.entity.OrganizeAdministratorEntity;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.mapper.OrganizeMapper;
import jnpf.permission.model.organize.OrganizeConditionModel;
import jnpf.permission.model.organize.OrganizeModel;
import jnpf.permission.service.OrganizeAdministratorService;
import jnpf.permission.service.OrganizeRelationService;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.PositionService;
import jnpf.permission.service.UserRelationService;
import jnpf.permission.service.UserService;
import jnpf.util.CacheKeyUtil;
import jnpf.util.DateUtil;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.RedisUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * 组织机构
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Service
public class OrganizeServiceImpl extends SuperServiceImpl<OrganizeMapper, OrganizeEntity> implements OrganizeService {

    @Autowired
    private PositionService positionService;
    @Autowired
    private CacheKeyUtil cacheKeyUtil;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private UserRelationService userRelationService;
    @Autowired
    private OrganizeRelationService organizeRelationService;
    @Autowired
    private OrganizeAdministratorService organizeAdministratorService;

    @Override
    public List<OrganizeEntity> getListAll(List<String> idAll, String keyWord) {
        // 定义变量判断是否需要使用修改时间倒序
        boolean flag = false;
        List<OrganizeEntity> list = new ArrayList<>();
        QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtil.isNotEmpty(keyWord)) {
            flag = true;
            queryWrapper.lambda().and(
                    t -> t.like(OrganizeEntity::getFullName, keyWord)
                            .or().like(OrganizeEntity::getEnCode, keyWord)
            );
        }
        // 排序
        queryWrapper.lambda().orderByAsc(OrganizeEntity::getSortCode)
                .orderByDesc(OrganizeEntity::getCreatorTime);
        if (flag) {
            queryWrapper.lambda().orderByDesc(OrganizeEntity::getLastModifyTime);
        }
        if (idAll.size() > 0) {
            queryWrapper.lambda().in(OrganizeEntity::getId, idAll);
            list = this.list(queryWrapper);
        }
        return list;
    }


    @Override
    public List<OrganizeEntity> getParentIdList(String id) {
        QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OrganizeEntity::getParentId, id);
        queryWrapper.lambda().eq(OrganizeEntity::getCategory, PermissionConst.DEPARTMENT);
        queryWrapper.lambda().orderByAsc(OrganizeEntity::getSortCode)
                .orderByDesc(OrganizeEntity::getCreatorTime);
        List<OrganizeEntity> list = this.list(queryWrapper);
        return list;
    }

    @Override
    public List<OrganizeEntity> getList(boolean filterEnabledMark) {
        return new LinkedList<>(getOrgMaps(null, filterEnabledMark, null).values());
    }

    public List<OrganizeEntity> getListByEnabledMark(Boolean enable) {
        QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
        if (enable) {
            queryWrapper.lambda().eq(OrganizeEntity::getEnabledMark, 1);
        }
        Map<String, OrganizeEntity> orgMaps = getBaseOrgMaps(queryWrapper, ImmutableMap.of(
                OrganizeEntity::getSortCode, true,
                OrganizeEntity::getCreatorTime, false)
                , null);

//        Map<String, OrganizeEntity> entityList = new LinkedHashMap<>();
//        if (StringUtil.isNotEmpty(keyword)) {
//            getParentOrganize(orgMaps, orgMaps, entityList);
//            orgMaps.clear();
//            orgMaps = entityList;
//        }
        return new LinkedList<>(orgMaps.values());
    }

    @Override
    public OrganizeEntity getInfoByFullName(String fullName) {
        if (StringUtil.isEmpty(fullName)) {
            return null;
        }
        QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OrganizeEntity::getFullName, fullName);
        List<OrganizeEntity> list = this.list(queryWrapper);
        return list.size() > 0 ? list.get(0) : null;
    }

    @Override
    public List<OrganizeEntity> getList(String keyword) {
        return new LinkedList<>(getOrgMaps(keyword, false, null).values());
    }


    /**
     * 获取组织信息
     * @return OrgId, OrgEntity
     */
    @Override
    public Map<String, OrganizeEntity> getOrgMapsAll(SFunction<OrganizeEntity, ?>... columns) {
        return getOrgMaps(null, false, null, columns);
    }

    /**
     * 获取组织信息
     * @param keyword
     * @param filterEnabledMark
     * @param type
     * @return OrgId, OrgEntity
     */
    @Override
    public Map<String, OrganizeEntity> getOrgMaps(String keyword, boolean filterEnabledMark, String type, SFunction<OrganizeEntity, ?>... columns) {
        QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtil.isNotEmpty(keyword)) {
            queryWrapper.lambda().and(
                    t -> t.like(OrganizeEntity::getFullName, keyword)
                            .or().like(OrganizeEntity::getEnCode, keyword.toLowerCase())
            );
        }
        if (filterEnabledMark) {
            queryWrapper.lambda().eq(OrganizeEntity::getEnabledMark, 1);
        }
        if (StringUtil.isNotEmpty(type)) {
            queryWrapper.lambda().eq(OrganizeEntity::getCategory, type);
        }
        Map<String, OrganizeEntity> orgMaps = getBaseOrgMaps(queryWrapper, ImmutableMap.of(
                OrganizeEntity::getSortCode, true,
                OrganizeEntity::getCreatorTime, false)
                , null);

        Map<String, OrganizeEntity> entityList = new LinkedHashMap<>();
        if (StringUtil.isNotEmpty(keyword)) {
            getParentOrganize(orgMaps, orgMaps, entityList);
            orgMaps.clear();
            orgMaps = entityList;
        }
        return orgMaps;
    }

    /**
     * 组织基础过滤
     * @param queryWrapper
     * @param orderBy Map<Column, isAsc>
     * @param groupBy Column
     * @param columns query
     * @return
     */
    public Map<String, OrganizeEntity> getBaseOrgMaps(QueryWrapper<OrganizeEntity> queryWrapper, Map<SFunction<OrganizeEntity, ?>, Boolean> orderBy, List<SFunction<OrganizeEntity, ?>> groupBy, SFunction<OrganizeEntity, ?>... columns) {
        if(queryWrapper == null){
            queryWrapper = new QueryWrapper<>();
        }
        LambdaQueryWrapper<OrganizeEntity> lambdaQueryWrapper = queryWrapper.lambda();

        List<SFunction<OrganizeEntity, ?>> columnList;
        List<SFunction<OrganizeEntity, ?>> bigColumnList = null;
        //没有指定查询字段就返回全部字段
        if(columns == null || columns.length == 0){
            columnList = Arrays.asList(OrganizeEntity::getId
                    ,OrganizeEntity::getParentId
                    ,OrganizeEntity::getCategory
                    ,OrganizeEntity::getEnCode
                    ,OrganizeEntity::getFullName
                    ,OrganizeEntity::getManagerId
                    ,OrganizeEntity::getSortCode
                    ,OrganizeEntity::getEnabledMark
                    ,OrganizeEntity::getCreatorTime
                    ,OrganizeEntity::getCreatorUserId
                    ,OrganizeEntity::getLastModifyTime
                    ,OrganizeEntity::getLastModifyUserId
                    ,OrganizeEntity::getDeleteMark
                    ,OrganizeEntity::getDeleteTime
                    ,OrganizeEntity::getDeleteUserId
                    ,OrganizeEntity::getTenantId);
            //把长文本字段分开查询, 默认带有排序， 数据量大的情况长文本字段参与排序速度非常慢
            bigColumnList = Arrays.asList(OrganizeEntity::getDescription
                    ,OrganizeEntity::getPropertyJson
                    ,OrganizeEntity::getOrganizeIdTree);
        }else{
            columnList = new ArrayList<>(Arrays.asList(columns));
            //指定字段中没有ID， 强制添加ID字段
            if(!columnList.contains((SFunction<OrganizeEntity, ?>)OrganizeEntity::getId)){
                columnList.add(OrganizeEntity::getId);
            }
        }
        lambdaQueryWrapper.select(columnList);
        QueryWrapper<OrganizeEntity> bigColumnQuery = null;
        if(bigColumnList != null){
            //获取大字段不参与排序
            bigColumnQuery = queryWrapper.clone();
        }
        //排序
        if(orderBy != null && !orderBy.isEmpty()){
            orderBy.forEach((k,v)->{
                lambdaQueryWrapper.orderBy(true, v, k);
            });
        }
        //分组
        if(groupBy != null && !groupBy.isEmpty()){
            lambdaQueryWrapper.groupBy(groupBy);
        }
        List<OrganizeEntity> list = this.list(queryWrapper);

        Map<String, OrganizeEntity> orgMaps = new LinkedHashMap<>(list.size(), 1);
        list.forEach(t->orgMaps.put(t.getId(), t));

        if(bigColumnList != null) {
            //获取大字段数据
            bigColumnQuery.lambda().select(OrganizeEntity::getId, OrganizeEntity::getOrganizeIdTree);
            List<OrganizeEntity> listBigFields = this.list(bigColumnQuery);
            listBigFields.forEach(t -> {
                OrganizeEntity organizeEntity = orgMaps.get(t.getId());
                if (organizeEntity != null) {
                    organizeEntity.setOrganizeIdTree(t.getOrganizeIdTree());
                }
            });
        }
        return orgMaps;
    }

    /**
     * 获取父级集合
     *
     * @param list       需要遍历的集合
     * @param entityList 结果集
     */
    private void getParentOrganize(Map<String, OrganizeEntity> list, Map<String, OrganizeEntity> searchList, Map<String, OrganizeEntity> entityList) {
        Map<String, OrganizeEntity> list1 = new LinkedHashMap<>();
        searchList.forEach((id, entity) -> {
            entityList.put(id, entity);
            OrganizeEntity info = list.get(id);
            if(info == null){
                info = getInfo(id);
            }
            if (Objects.nonNull(info)) {
                if (StringUtil.isNotEmpty(info.getParentId()) && !"-1".equals(info.getParentId())) {
                    OrganizeEntity organizeEntity = list.get(info.getParentId());
                    if(organizeEntity == null){
                        organizeEntity = getInfo(info.getParentId());
                    }
                    if (organizeEntity != null) {
                        list1.put(organizeEntity.getId(), organizeEntity);
                        getParentOrganize(list, list1, entityList);
                    }
                } else if (StringUtil.isNotEmpty(info.getParentId()) && "-1".equals(info.getParentId())) {
                    entityList.put(id, info);
                }
            }
        });
    }

    @Override
    public List<OrganizeEntity> getOrgEntityList(List<String> idList, Boolean enable) {
        if (idList.size() > 0) {
            QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(OrganizeEntity::getId, idList);
            if (enable) {
                queryWrapper.lambda().eq(OrganizeEntity::getEnabledMark, 1);
            }
//            queryWrapper.lambda().select(OrganizeEntity::getId, OrganizeEntity::getFullName);
            return this.list(queryWrapper);
        }
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getOrgMap(String type, String category) {
        if (type.equals("id-fullName")){
            return getOrgMap();
        } else if (type.equals("fullName-id")){
            return getOrgNameAndId(category);
        } else {
            return getOrgEncodeAndName(category);
        }
    }
    @Override
    public List<OrganizeEntity> getOrgEntityList(Set<String> idList) {
        if (idList.size() > 0) {
            QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().select(OrganizeEntity::getId, OrganizeEntity::getFullName).in(OrganizeEntity::getId, idList);
            List<OrganizeEntity> list = this.list(queryWrapper);
            return list;
        }
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getOrgMap() {
        QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(OrganizeEntity::getId, OrganizeEntity::getFullName);
        List<OrganizeEntity> list = this.list(queryWrapper);
        return list.stream().collect(Collectors.toMap(OrganizeEntity::getId, OrganizeEntity::getFullName));
    }

    @Override
    public Map<String, Object> getOrgEncodeAndName(String type) {
        QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(OrganizeEntity::getId, OrganizeEntity::getFullName ,OrganizeEntity::getEnCode);
        queryWrapper.lambda().eq(OrganizeEntity::getCategory, type);
        List<OrganizeEntity> list = this.list(queryWrapper);
        return list.stream().collect(Collectors.toMap(o->o.getFullName()+ "/"+o.getEnCode(), OrganizeEntity::getId, (v1,v2)->v2));
    }

    @Override
    public Map<String, Object> getOrgNameAndId(String type) {
        QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(OrganizeEntity::getId, OrganizeEntity::getFullName);
        if (StringUtil.isNotEmpty(type)){
            queryWrapper.lambda().eq(OrganizeEntity::getCategory, type);
        }
        List<OrganizeEntity> list = this.list(queryWrapper);
        Map<String,Object> allOrgMap = new HashMap<>();
        for (OrganizeEntity entity : list){
            allOrgMap.put(entity.getFullName(),entity.getId());
        }
        return allOrgMap;
    }

    @Override
    public List<OrganizeEntity> getOrgRedisList() {
        if (redisUtil.exists(cacheKeyUtil.getOrganizeList())) {
            return JsonUtil.getJsonToList(redisUtil.getString(cacheKeyUtil.getOrganizeList()).toString(), OrganizeEntity.class);
        }
        QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OrganizeEntity::getEnabledMark, 1);

        List<OrganizeEntity> list = this.list(queryWrapper);
        if (list.size() > 0) {
            redisUtil.insert(cacheKeyUtil.getOrganizeList(), JsonUtil.getObjectToString(list), 300);
        }
        return list;
    }

    @Override
    public OrganizeEntity getInfo(String id) {
        return this.getById(id);
    }

    @Override
    public OrganizeEntity getByFullName(String fullName) {
        OrganizeEntity organizeEntity = new OrganizeEntity();
        QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OrganizeEntity::getFullName, fullName);
        queryWrapper.lambda().select(OrganizeEntity::getId);
        List<OrganizeEntity> list = this.list(queryWrapper);
        if (list.size() > 0) {
            organizeEntity = list.get(0);
        }
        return organizeEntity;
    }

    @Override
    public boolean isExistByFullName(OrganizeEntity entity, boolean isCheck, boolean isFilter) {
        QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OrganizeEntity::getFullName, entity.getFullName());
        if (!isCheck) {
            if (isFilter) {
                queryWrapper.lambda().ne(OrganizeEntity::getId, entity.getId());
            }
            List<OrganizeEntity> entityList = this.list(queryWrapper);
            if (entityList.size() > 0) {
                for (OrganizeEntity organizeEntity : entityList) {
                    if (organizeEntity != null && organizeEntity.getParentId().equals(entity.getParentId()) && organizeEntity.getCategory().equals(entity.getCategory())) {
                        return true;
                    }
                }
            }
            return false;
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public void getOrganizeIdTree(String organizeId, List<String> organizeParentIdList) {
        OrganizeEntity entity = getInfo(organizeId);
        if (entity != null) {
            organizeParentIdList.add(entity.getId());
            if (StringUtil.isNotEmpty(entity.getParentId())) {
                getOrganizeIdTree(entity.getParentId(), organizeParentIdList);
            }
        }
    }

    @Override
    public void getOrganizeId(String organizeId, List<OrganizeEntity> organizeList) {
        OrganizeEntity entity = getInfo(organizeId);
        if (entity != null) {
            organizeList.add(entity);
            if (StringUtil.isNotEmpty(entity.getParentId())) {
                getOrganizeId(entity.getParentId(), organizeList);
            }
        }
    }


    @Override
    public List<OrganizeEntity> getOrganizeId(String organizeId){
        List<OrganizeEntity> organizeList = new ArrayList<>();
        this.getOrganizeId(organizeId, organizeList);
        return organizeList;
    }

    @Override
    public boolean isExistByEnCode(String enCode, String id) {
        QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OrganizeEntity::getEnCode, enCode);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(OrganizeEntity::getId, id);
        }
        return this.count(queryWrapper) > 0;
    }

    @Override
    public void create(OrganizeEntity entity) {
        entity.setId(RandomUtil.uuId());
        entity.setCreatorUserId(userProvider.get().getUserId());
        // 拼上当前组织id
        String organizeIdTree = StringUtil.isNotEmpty(entity.getOrganizeIdTree()) ? entity.getOrganizeIdTree() + "," : "";
        entity.setOrganizeIdTree(organizeIdTree + entity.getId());
        if (!userProvider.get().getIsAdministrator()) {
            // 当前用户创建的组织要赋予权限
            OrganizeAdministratorEntity organizeAdministratorEntity = new OrganizeAdministratorEntity();
            organizeAdministratorEntity.setUserId(userProvider.get().getUserId());
            organizeAdministratorEntity.setOrganizeId(entity.getId());
            organizeAdministratorEntity.setThisLayerAdd(1);
            organizeAdministratorEntity.setThisLayerEdit(1);
            organizeAdministratorEntity.setThisLayerDelete(1);
            organizeAdministratorEntity.setThisLayerSelect(1);
            organizeAdministratorEntity.setSubLayerAdd(0);
            organizeAdministratorEntity.setSubLayerEdit(0);
            organizeAdministratorEntity.setSubLayerDelete(0);
            organizeAdministratorEntity.setSubLayerSelect(0);
            organizeAdministratorService.create(organizeAdministratorEntity);
        }
        this.save(entity);
        redisUtil.remove(cacheKeyUtil.getOrganizeInfoList());
    }

    @Override
    public boolean update(String id, OrganizeEntity entity) {
        entity.setId(id);
        entity.setLastModifyTime(DateUtil.getNowDate());
        entity.setLastModifyUserId(userProvider.get().getUserId());
        // 拼上当前组织id
        String organizeIdTree = StringUtil.isNotEmpty(entity.getOrganizeIdTree()) ? entity.getOrganizeIdTree() + "," : "";
        entity.setOrganizeIdTree(organizeIdTree + entity.getId());
        // 判断父级是否变化
        OrganizeEntity info = getInfo(id);
        boolean updateById = this.updateById(entity);
        if (info != null && !entity.getParentId().equals(info.getParentId())) {
            // 子集和父级都需要修改父级树
            update(entity, info.getCategory());
        }
        redisUtil.remove(cacheKeyUtil.getOrganizeInfoList());
        return updateById;
    }

    @Override
    public void update(OrganizeEntity entity, String category) {
        // 查询子级
        QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OrganizeEntity::getParentId, entity.getId());
        if (PermissionConst.COMPANY.equals(category)) {
            queryWrapper.lambda().eq(OrganizeEntity::getCategory, PermissionConst.COMPANY);
        } else {
            queryWrapper.lambda().eq(OrganizeEntity::getCategory, PermissionConst.DEPARTMENT);
        }
        List<OrganizeEntity> list = this.list(queryWrapper);
        // 递归修改子组织的父级id字段
        for (OrganizeEntity organizeEntity : list) {
            List<String> list1 = new ArrayList<>();
            getOrganizeIdTree(organizeEntity.getId(), list1);
            // 倒叙排放
            Collections.reverse(list1);
            StringBuffer organizeIdTree = new StringBuffer();
            for (String organizeParentId : list1) {
                organizeIdTree.append("," + organizeParentId);
            }
            String organizeParentIdTree = organizeIdTree.toString();
            if (StringUtil.isNotEmpty(organizeParentIdTree)) {
                organizeParentIdTree = organizeParentIdTree.replaceFirst(",", "");
            }
            organizeEntity.setOrganizeIdTree(organizeParentIdTree);
            this.updateById(organizeEntity);
            redisUtil.remove(cacheKeyUtil.getOrganizeInfoList());
        }
    }

    @Override
    public ActionResult<String> delete(String orgId) {
        String flag = this.allowDelete(orgId);
        if (flag == null) {
            OrganizeEntity organizeEntity = this.getInfo(orgId);
            if (organizeEntity != null) {
                this.removeById(orgId);
                redisUtil.remove(cacheKeyUtil.getOrganizeInfoList());
                return ActionResult.success(MsgCode.SU003.get());
            }
            return ActionResult.fail(MsgCode.FA003.get());
        } else {
            return ActionResult.fail("此记录与\"" + flag + "\"关联引用，不允许被删除");
        }
    }

    @Override
    @DSTransactional
    public boolean first(String id) {
        boolean isOk = false;
        //获取要上移的那条数据的信息
        OrganizeEntity upEntity = this.getById(id);
        Long upSortCode = upEntity.getSortCode() == null ? 0 : upEntity.getSortCode();
        //查询上几条记录
        QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .lt(OrganizeEntity::getSortCode, upSortCode)
                .eq(OrganizeEntity::getParentId, upEntity.getParentId())
                .orderByDesc(OrganizeEntity::getSortCode);
        List<OrganizeEntity> downEntity = this.list(queryWrapper);
        if (downEntity.size() > 0) {
            //交换两条记录的sort值
            Long temp = upEntity.getSortCode();
            upEntity.setSortCode(downEntity.get(0).getSortCode());
            downEntity.get(0).setSortCode(temp);
            this.updateById(downEntity.get(0));
            this.updateById(upEntity);
            isOk = true;
        }
        return isOk;
    }

    @Override
    @DSTransactional
    public boolean next(String id) {
        boolean isOk = false;
        //获取要下移的那条数据的信息
        OrganizeEntity downEntity = this.getById(id);
        Long upSortCode = downEntity.getSortCode() == null ? 0 : downEntity.getSortCode();
        //查询下几条记录
        QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .gt(OrganizeEntity::getSortCode, upSortCode)
                .eq(OrganizeEntity::getParentId, downEntity.getParentId())
                .orderByAsc(OrganizeEntity::getSortCode);
        List<OrganizeEntity> upEntity = this.list(queryWrapper);
        if (upEntity.size() > 0) {
            //交换两条记录的sort值
            Long temp = downEntity.getSortCode();
            downEntity.setSortCode(upEntity.get(0).getSortCode());
            upEntity.get(0).setSortCode(temp);
            this.updateById(upEntity.get(0));
            this.updateById(downEntity);
            isOk = true;
        }
        return isOk;
    }

    @Override
    public String allowDelete(String orgId) {
        // 组织底下是否有组织
        List<OrganizeEntity> list = getListByParentId(orgId);
        if (Objects.nonNull(list) && list.size() > 0) {
            return "组织";
        }
        // 组织底下是否有岗位
        List<PositionEntity> list1 = positionService.getListByOrganizeId(Collections.singletonList(orgId), false);
        if (Objects.nonNull(list1) && list1.size() > 0) {
            return "岗位";
        }
        // 组织底下是否有用户
        if (userRelationService.existByObj(PermissionConst.ORGANIZE, orgId)) {
            return "用户";
        }
        // 组织底下是否有角色
        if (organizeRelationService.existByObjTypeAndOrgId(PermissionConst.ROLE, orgId)) {
            return "角色";
        }
        return null;
    }

    @Override
    public List<OrganizeEntity> getOrganizeName(List<String> id) {
        List<OrganizeEntity> list = new ArrayList<>();
        if (id.size() > 0) {
            QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(OrganizeEntity::getId, id);
            queryWrapper.lambda().orderByAsc(OrganizeEntity::getSortCode).orderByDesc(OrganizeEntity::getCreatorTime);
            list = this.list(queryWrapper);
        }
        return list;
    }

    @Override
    public Map<String, OrganizeEntity> getOrganizeName(List<String> id, String keyword, boolean filterEnabledMark, String type) {
        Map<String, OrganizeEntity> list = Collections.EMPTY_MAP;
        if (id.size() > 0) {
            QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(OrganizeEntity::getId, id);
            if (StringUtil.isNotEmpty(keyword)) {
                queryWrapper.lambda().and(
                        t -> t.like(OrganizeEntity::getFullName, keyword)
                                .or().like(OrganizeEntity::getEnCode, keyword)
                );
            }
            if (StringUtil.isNotEmpty(type)) {
                queryWrapper.lambda().eq(OrganizeEntity::getCategory, type);
            }
            if (filterEnabledMark) {
                queryWrapper.lambda().eq(OrganizeEntity::getEnabledMark, 1);
            }
            list = getBaseOrgMaps(queryWrapper, ImmutableMap.of(
                    OrganizeEntity::getSortCode, true,
                    OrganizeEntity::getCreatorTime, false
            ), null);
//            Map<String, OrganizeEntity> orgList = new LinkedHashMap<>(id.size(), 1);
//            orgMaps.values().forEach(t -> {
//                if (StringUtil.isNotEmpty(t.getOrganizeIdTree())) {
//                    String[] split = t.getOrganizeIdTree().split(",");
//                    for (String orgId : split) {
//                        if (id.contains(orgId) && !orgList.containsKey(orgId)) {
//                            OrganizeEntity entity = orgMaps.get(orgId);
//                            if(entity == null){
//                                entity = getInfo(orgId);
//                            }
//                            if (entity != null) {
//                                orgList.put(orgId, entity);
//                            }
//                        }
//                    }
//                }
//            });
//            list = orgList;
        }
        return list;
    }

    @Override
    public List<String> getOrganize(String organizeParentId) {
        QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OrganizeEntity::getParentId, organizeParentId);
        queryWrapper.lambda().select(OrganizeEntity::getId);
        List<String> list = this.list(queryWrapper).stream().map(t -> t.getId()).collect(Collectors.toList());
        return list;
    }

    @Override
    public List<String> getUnderOrganizations(String organizeId, boolean filterEnabledMark) {
        QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
        if (filterEnabledMark) {
            queryWrapper.lambda().eq(OrganizeEntity::getEnabledMark, 1);
        }
        queryWrapper.lambda().like(OrganizeEntity::getOrganizeIdTree, organizeId);
        queryWrapper.lambda().ne(OrganizeEntity::getId, organizeId);
        queryWrapper.lambda().select(OrganizeEntity::getId);
        return this.list(queryWrapper).stream().map(OrganizeEntity::getId).collect(Collectors.toList());
    }

    @Override
    public List<String> getUnderOrganizationss(String organizeId) {
        List<String> totalIds = new ArrayList<>();
        if (!userProvider.get().getIsAdministrator()) {
            // 得到有权限的组织
            List<String> collect = organizeAdministratorService.getListByAuthorize().stream().map(OrganizeEntity::getId).collect(Collectors.toList());
            totalIds = totalIds.stream().filter(t -> collect.contains(t)).collect(Collectors.toList());
        } else {
            totalIds = getUnderOrganizations(organizeId, false);
        }
        return totalIds;
    }

    @Override
    public List<OrganizeEntity> getListByFullName(String fullName) {
        QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OrganizeEntity::getFullName, fullName);
        return this.list(queryWrapper);
    }

    @Override
    public List<OrganizeEntity> getListByParentId(String id) {
        QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OrganizeEntity::getParentId, id);
        return this.list(queryWrapper);
    }

    @Override
    public List<OrganizeEntity> getAllOrgByUserId(String userId) {
        List<String> ids = new ArrayList<>();
        userRelationService.getAllOrgRelationByUserId(userId).forEach(r -> {
            ids.add(r.getObjectId());
        });
        return this.listByIds(ids);
    }

    @Override
    public List<OrganizeEntity> getOrganizeNameSort(List<String> id) {
        List<OrganizeEntity> list = new ArrayList<>();
        for (String orgId : id) {
            QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(OrganizeEntity::getId, orgId);
            queryWrapper.lambda().select(OrganizeEntity::getFullName);
            OrganizeEntity entity = this.getOne(queryWrapper);
            if (entity != null) {
                list.add(entity);
            }
        }
        return list;
    }

    @Override
    public String getFullNameByOrgIdTree(Map<String, String> idNameMaps, String orgIdTree, String regex) {
        String fullName = "";
        if (StringUtil.isNotEmpty(orgIdTree)) {
            String[] split = orgIdTree.split(",");
            StringBuilder orgName = new StringBuilder();
            String tmpName;
            for (String orgId : split) {
                if (StringUtil.isEmpty(orgIdTree)) {
                    continue;
                }
                if((tmpName = idNameMaps.get(orgId)) != null){
                    orgName.append(regex).append(tmpName);
                }
            }
            if (orgName.length() > 0) {
                fullName = orgName.toString().replaceFirst(regex, "");
            }
        }
        return fullName;
    }

    @Override
    public List<OrganizeEntity> getDepartmentAll(String organizeId) {
        OrganizeEntity organizeCompany = getOrganizeCompany(organizeId);
        List<OrganizeEntity> organizeList = new ArrayList<>();
        if (organizeCompany != null) {
            getOrganizeDepartmentAll(organizeCompany.getId(), organizeList);
            organizeList.add(organizeCompany);
        }
        return organizeList;
    }

    @Override
    public OrganizeEntity getOrganizeCompany(String organizeId) {
        OrganizeEntity entity = getInfo(organizeId);
        return (entity != null && !PermissionConst.COMPANY.equals(entity.getCategory())) ? getOrganizeCompany(entity.getParentId()) : entity;
    }

    @Override
    public void getOrganizeDepartmentAll(String organizeId, List<OrganizeEntity> organizeList) {
        List<OrganizeEntity> organizeEntityList = getListByParentId(organizeId);
        for (OrganizeEntity entity : organizeEntityList) {
            if (!PermissionConst.COMPANY.equals(entity.getCategory())) {
                organizeList.add(entity);
                getOrganizeDepartmentAll(entity.getId(), organizeList);
            }
        }
    }

    @Override
    public List<OrganizeEntity> getOrganizeByParentId(String parentId) {
        QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OrganizeEntity::getParentId, parentId);
        return this.list(queryWrapper);
    }

    @Override
    public List<String> upWardRecursion(List<String> orgIDs, String orgID) {
        this.getOrgIDs(orgIDs,orgID);
        return orgIDs;
    }

    @Override
    public List<String> getOrgIdTree(OrganizeEntity entity) {
        List<String> orgIds= new ArrayList<>();
        if (entity != null) {
            String organizeIdTree = entity.getOrganizeIdTree();
            if (StringUtil.isNotEmpty(organizeIdTree)) {
                String[] split = organizeIdTree.split(",");
                for (String orgId : split) {
                    orgIds.add(orgId);
                }
            }
        }
        return orgIds;
    }

    @Override
    public Map<String, String> getInfoList() {
        if (redisUtil.exists(cacheKeyUtil.getOrganizeInfoList())) {
            return new HashMap<>(redisUtil.getMap(cacheKeyUtil.getOrganizeInfoList()));
        } else {
            Map<String, OrganizeEntity> orgs = getOrgMaps(null, false, null, OrganizeEntity::getFullName);
            Map<String, String> infoMap = new LinkedHashMap<>(orgs.size(), 1);
            orgs.forEach((k,v) -> infoMap.put(k, v.getFullName()));
            redisUtil.insert(cacheKeyUtil.getOrganizeInfoList(), infoMap);
            return infoMap;
        }
    }

    @Override
    public List<OrganizeEntity> getOrganizeChildList(List<String> list) {
        List<String> idList = new ArrayList<>();
        for(String id : list){
            List<String> underOrganizations = this.getUnderOrganizations(id, false);
            idList.addAll(underOrganizations);
            idList.add(id);
        }
        List<OrganizeEntity> listAll = this.getListAll(idList, null);
        return listAll;
    }

    @Override
    public String getOrganizeIdTree(OrganizeEntity entity) {
        List<String> list = new ArrayList<>();
        this.getOrganizeIdTree(entity.getParentId(), list);
        // 倒叙排放
        Collections.reverse(list);
        StringBuffer organizeIdTree = new StringBuffer();
        for (String organizeParentId : list) {
            organizeIdTree.append("," + organizeParentId);
        }
        String organizeParentIdTree = organizeIdTree.toString();
        if (StringUtil.isNotEmpty(organizeParentIdTree)) {
            organizeParentIdTree = organizeParentIdTree.replaceFirst(",", "");
        }
        return organizeParentIdTree;
    }

    @Override
    public OrganizeEntity getInfoByParentId(String parentId) {
        QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OrganizeEntity::getParentId, parentId);
        return this.getOne(queryWrapper);
    }

    private void getOrgIDs(List<String> orgIDs, String orgID) {
        OrganizeEntity info = this.getInfo(orgID);
        if (info != null){
            this.getOrgIDs(orgIDs,info.getParentId());
            orgIDs.add(info.getId());
        }
    }

    @Override
    public String getDefaultCurrentDepartmentId(OrganizeConditionModel organizeConditionModel) throws DataException {
        UserInfo userInfo = UserProvider.getUser();
        int currentFinded = 0;
        if(organizeConditionModel.getDepartIds() != null && !organizeConditionModel.getDepartIds().isEmpty() && organizeConditionModel.getDepartIds().contains(userInfo.getOrganizeId())) {
            currentFinded = 1;
        }
        if(currentFinded == 0 && organizeConditionModel.getDepartIds() != null && !organizeConditionModel.getDepartIds().isEmpty()) {
            List<String> idList = new ArrayList<>(16);
            // 获取所有组织
            if (organizeConditionModel.getDepartIds().size() > 0) {
                idList.addAll(organizeConditionModel.getDepartIds());
                organizeConditionModel.getDepartIds().forEach(t -> {
                    List<String> underOrganizations = this.getUnderOrganizations(t, false);
                    if (underOrganizations.size() > 0) {
                        idList.addAll(underOrganizations);
                    }
                });
            }
            List<OrganizeEntity> listAll = this.getListAll(idList, organizeConditionModel.getKeyword());
            List<OrganizeModel> organizeList = JsonUtil.getJsonToList(listAll, OrganizeModel.class);
//            List<String> collect = organizeList.stream().map(SumTree::getParentId).collect(Collectors.toList());
//            List<OrganizeModel> noParentId = organizeList.stream().filter(t->!collect.contains(t.getId()) && !"-1".equals(t.getParentId())).collect(Collectors.toList());
//            noParentId.forEach(t->{
//                if (StringUtil.isNotEmpty(t.getOrganizeIdTree())) {
//                    String[] split = t.getOrganizeIdTree().split(",");
//                    List<String> list = Arrays.asList(split);
//                    Collections.reverse(list);
//                    for (int i = 1; i < list.size(); i++) {
//                        String orgId = list.get(i);
//                        List<OrganizeModel> collect1 = organizeList.stream().filter(tt -> orgId.equals(tt.getId())).collect(Collectors.toList());
//                        if (collect1.size() > 0) {
//                            String[] split1 = StringUtil.isNotEmpty(t.getOrganizeIdTree()) ? t.getOrganizeIdTree().split(orgId) : new String[0];
//                            if (split1.length > 0) {
//                                t.setFullName(this.getFullNameByOrgIdTree(split1[1], "/"));
//                            }
//                            t.setParentId(orgId);
//                            break;
//                        }
//                    }
//                }
//            });

            List<String> orgLIdList = organizeList.stream().map(OrganizeModel::getId).collect(Collectors.toList());
            if(orgLIdList != null && !orgLIdList.isEmpty() && orgLIdList.contains(userInfo.getOrganizeId())) {
                currentFinded = 1;
            }
        }
        return (currentFinded == 1)?userInfo.getOrganizeId():"";
    }

    @Override
    public Map<String, Object> getAllOrgsTreeName() {
        Map<String, Object> map = new HashMap<>();
        QueryWrapper<OrganizeEntity> queryWrapper = new QueryWrapper<>();
        List<OrganizeEntity> list = this.list(queryWrapper);
        Map<String, String> collect = list.stream().collect(Collectors.toMap(OrganizeEntity::getId, OrganizeEntity::getFullName));
        for (OrganizeEntity org : list) {
            String[] split = org.getOrganizeIdTree().split(",");
            StringJoiner names = new StringJoiner("/");
            for (String id : split) {
                names.add(collect.get(id));
            }
            map.put(org.getOrganizeIdTree(), names.toString());
        }
        return map;
    }

    @Override
    public void removeOrganizeInfoList() {
        redisUtil.remove(cacheKeyUtil.getOrganizeInfoList());
    }
}
