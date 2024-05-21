package jnpf.permission.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.base.Pagination;
import jnpf.base.entity.ModuleEntity;
import jnpf.base.entity.SystemEntity;
import jnpf.base.service.DbLinkService;
import jnpf.base.service.ModuleService;
import jnpf.base.service.SuperServiceImpl;
import jnpf.base.service.SystemService;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.JnpfConst;
import jnpf.constant.PermissionConst;
import jnpf.database.model.entity.DbLinkEntity;
import jnpf.database.util.DbTypeUtil;
import jnpf.database.util.TenantDataSourceUtil;
import jnpf.model.tenant.TenantAuthorizeModel;
import jnpf.permission.entity.OrganizeAdministratorEntity;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.entity.UserRelationEntity;
import jnpf.permission.mapper.OrganizeAdminIsTratorMapper;
import jnpf.permission.model.organizeadministrator.OrganizeAdministratorListVo;
import jnpf.permission.model.organizeadministrator.OrganizeAdministratorModel;
import jnpf.permission.service.OrganizeAdministratorService;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.UserRelationService;
import jnpf.permission.service.UserService;
import jnpf.util.DateUtil;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 机构分级管理员
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Service
public class OrganizeAdministratorServiceImpl extends SuperServiceImpl<OrganizeAdminIsTratorMapper, OrganizeAdministratorEntity> implements OrganizeAdministratorService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRelationService userRelationService;
    @Autowired
    private DbLinkService dbLinkService;
    @Autowired
    private SystemService systemService;
    @Autowired
    private ModuleService moduleService;
    @Autowired
    private ConfigValueUtil configValueUtil;


    @Override
    public OrganizeAdministratorEntity getOne(String userId, String organizeId) {
        QueryWrapper<OrganizeAdministratorEntity> queryWrapper = new QueryWrapper<>();
        try {
            DbLinkEntity dbLinkEntity = dbLinkService.getResource("0", UserProvider.getUser().getTenantId());
            if (DbTypeUtil.checkOracle(dbLinkEntity) || DbTypeUtil.checkDM(dbLinkEntity)) {
                queryWrapper.eq("dbms_lob.substr(F_USER_ID)", userId);
            } else if (DbTypeUtil.checkSQLServer(dbLinkEntity)) {
                queryWrapper.lambda().like(OrganizeAdministratorEntity::getUserId, userId);
            } else {
                queryWrapper.lambda().eq(OrganizeAdministratorEntity::getUserId, userId);
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
        queryWrapper.lambda().eq(OrganizeAdministratorEntity::getOrganizeId, organizeId);
        // 排序
        queryWrapper.lambda().orderByAsc(OrganizeAdministratorEntity::getSortCode)
                .orderByDesc(OrganizeAdministratorEntity::getCreatorTime);
        return this.getOne(queryWrapper);
    }

    @Override
    public List<OrganizeAdministratorEntity> getOrganizeAdministratorEntity(String userId) {
        return getOrganizeAdministratorEntity(userId, PermissionConst.ORGANIZE, false);
    }

    @Override
    public List<OrganizeAdministratorEntity> getOrganizeAdministratorEntity(String userId, String type, boolean filterMain) {
        List<OrganizeAdministratorEntity> list = new ArrayList<>();
        UserEntity entity1 = userService.getInfo(userId);
        if ((entity1 != null && entity1.getIsAdministrator() == 1) && !PermissionConst.ORGANIZE.equals(type)) {
            List<String> collect = new ArrayList<>();
            List<String> moduleAuthorize = new ArrayList<>();
            List<String> moduleUrlAddressAuthorize = new ArrayList<>();
            if (configValueUtil.isMultiTenancy()) {
                TenantAuthorizeModel tenantAuthorizeModel = TenantDataSourceUtil.getCacheModuleAuthorize(UserProvider.getUser().getTenantId());
                moduleAuthorize = tenantAuthorizeModel.getModuleIdList();
                moduleUrlAddressAuthorize = tenantAuthorizeModel.getUrlAddressList();
            }
            if (PermissionConst.SYSTEM.equals(type)) {
                collect = systemService.getList().stream().map(SystemEntity::getId).collect(Collectors.toList());
            } else if (PermissionConst.MODULE.equals(type)) {
                collect = moduleService.getList().stream().map(ModuleEntity::getId).collect(Collectors.toList());
                // ModuleServiceModel model = ModuleServiceModel.builder().filterFlowWork(false).build();
                // collect = moduleService.getList(model.getFilterFlowWork(), model.getModuleAuthorize(), model.getModuleUrlAddressAuthorize()).stream().map(ModuleEntity::getId).collect(Collectors.toList());
            }
            for (String t : collect) {
                OrganizeAdministratorEntity entity = new OrganizeAdministratorEntity();
                entity.setOrganizeId(t);
                entity.setId(RandomUtil.uuId());
                entity.setOrganizeType(type);
                entity.setUserId(userId);
                list.add(entity);
            }
            return list;
        }
        QueryWrapper<OrganizeAdministratorEntity> queryWrapper = new QueryWrapper<>();
        if (PermissionConst.ORGANIZE.equals(type)) {
            queryWrapper.lambda().isNull(OrganizeAdministratorEntity::getOrganizeType);
        } else if (StringUtil.isNotEmpty(type)) {
            queryWrapper.lambda().eq(OrganizeAdministratorEntity::getOrganizeType, type);
        }
        try {
            DbLinkEntity dbLinkEntity = dbLinkService.getResource("0", UserProvider.getUser().getTenantId());
            if (DbTypeUtil.checkOracle(dbLinkEntity) || DbTypeUtil.checkDM(dbLinkEntity)) {
                queryWrapper.eq("dbms_lob.substr(F_USER_ID)", userId);
            } else if (DbTypeUtil.checkSQLServer(dbLinkEntity)) {
                queryWrapper.lambda().like(OrganizeAdministratorEntity::getUserId, userId);
            } else {
                queryWrapper.lambda().eq(OrganizeAdministratorEntity::getUserId, userId);
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
        // 排序
        queryWrapper.lambda().orderByAsc(OrganizeAdministratorEntity::getSortCode)
                .orderByDesc(OrganizeAdministratorEntity::getCreatorTime);
        list = this.list(queryWrapper);
        return list;
    }

    @Override
    @Transactional
    public void create(OrganizeAdministratorEntity entity) {
        // 判断是新建还是删除
        QueryWrapper<OrganizeAdministratorEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OrganizeAdministratorEntity::getOrganizeId, entity.getOrganizeId());
        try {
            DbLinkEntity dbLinkEntity = dbLinkService.getResource("0", UserProvider.getUser().getTenantId());
            if (DbTypeUtil.checkOracle(dbLinkEntity) || DbTypeUtil.checkDM(dbLinkEntity)) {
                queryWrapper.eq("dbms_lob.substr(F_USER_ID)", entity.getUserId());
            } else if (DbTypeUtil.checkSQLServer(dbLinkEntity)) {
                queryWrapper.lambda().like(OrganizeAdministratorEntity::getUserId, entity.getUserId());
            } else {
                queryWrapper.lambda().eq(OrganizeAdministratorEntity::getUserId, entity.getUserId());
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
        // 查出数据是否重复
        OrganizeAdministratorEntity administratorEntity = this.getOne(queryWrapper);
        if (administratorEntity == null) {
            entity.setId(RandomUtil.uuId());
            entity.setCreatorUserId(UserProvider.getLoginUserId());
            entity.setCreatorTime(new Date());
        } else {
            entity.setId(administratorEntity.getId());
            entity.setCreatorUserId(UserProvider.getLoginUserId());
            entity.setLastModifyTime(new Date());
        }
        this.saveOrUpdate(entity);
    }

    @Override
    @Transactional
    public void createList(List<OrganizeAdministratorEntity> list, String userId) {
        Date creatorTime = new Date();
        QueryWrapper<OrganizeAdministratorEntity> queryWrapper = new QueryWrapper<>();
        try {
            DbLinkEntity dbLinkEntity = dbLinkService.getResource("0", UserProvider.getUser().getTenantId());
            if (DbTypeUtil.checkOracle(dbLinkEntity) || DbTypeUtil.checkDM(dbLinkEntity)) {
                queryWrapper.eq("dbms_lob.substr(F_USER_ID)", userId);
            } else if (DbTypeUtil.checkSQLServer(dbLinkEntity)) {
                queryWrapper.lambda().like(OrganizeAdministratorEntity::getUserId, userId);
            } else {
                queryWrapper.lambda().eq(OrganizeAdministratorEntity::getUserId, userId);
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
        queryWrapper.lambda().select(OrganizeAdministratorEntity::getCreatorTime);
        List<OrganizeAdministratorEntity> list1 = this.list(queryWrapper);
        if (list1.size() > 0) {
            Date creatorTime1 = list1.get(0).getCreatorTime();
            if (creatorTime1 != null) {
                creatorTime = creatorTime1;
            }
        }
        // 手动设置userId
        Date finalCreatorTime = creatorTime;
        list.forEach(t -> {
            t.setUserId(userId);
            t.setCreatorTime(finalCreatorTime);
        });
        this.remove(queryWrapper);
        for (OrganizeAdministratorEntity entity : list) {
//            // 查出数据是否重复
//            OrganizeAdministratorEntity administratorEntity = this.getOne(queryWrapper);
//            if (administratorEntity == null) {
                entity.setId(RandomUtil.uuId());
                entity.setCreatorUserId(UserProvider.getLoginUserId());
                entity.setCreatorTime(finalCreatorTime);
//            } else {
//                entity.setId(administratorEntity.getId());
//                entity.setCreatorUserId(UserProvider.getLoginUserId());
//                entity.setLastModifyTime(new Date());
//            }
            this.saveOrUpdate(entity);
        }
        userService.delCurUser(null, userId);
    }

    @Override
    public boolean update(String organizeId, OrganizeAdministratorEntity entity) {
        entity.setId(entity.getId());
        entity.setLastModifyTime(DateUtil.getNowDate());
        entity.setLastModifyUserId(UserProvider.getLoginUserId());
        return this.updateById(entity);
    }

    @Override
    public boolean deleteByUserId(String userId) {
        QueryWrapper<OrganizeAdministratorEntity> queryWrapper = new QueryWrapper<>();
        try {
            DbLinkEntity dbLinkEntity = dbLinkService.getResource("0", UserProvider.getUser().getTenantId());
            if (DbTypeUtil.checkOracle(dbLinkEntity) || DbTypeUtil.checkDM(dbLinkEntity)) {
                queryWrapper.eq("dbms_lob.substr(F_USER_ID)", userId);
            } else if (DbTypeUtil.checkSQLServer(dbLinkEntity)) {
                queryWrapper.lambda().like(OrganizeAdministratorEntity::getUserId, userId);
            } else {
                queryWrapper.lambda().eq(OrganizeAdministratorEntity::getUserId, userId);
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
        boolean remove = this.remove(queryWrapper);
        userService.delCurUser(null, userId);
        return remove;
    }

    @Override
    public OrganizeAdministratorEntity getInfo(String id) {
        QueryWrapper<OrganizeAdministratorEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OrganizeAdministratorEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public void delete(OrganizeAdministratorEntity entity) {
        this.removeById(entity.getId());
    }

    @Override
    public OrganizeAdministratorEntity getInfoByOrganizeId(String organizeId) {
        QueryWrapper<OrganizeAdministratorEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OrganizeAdministratorEntity::getOrganizeId, organizeId);
        return this.getOne(queryWrapper);
    }

    @Override
    public List<OrganizeAdministratorEntity> getListByOrganizeId(List<String> organizeIdList) {
        QueryWrapper<OrganizeAdministratorEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(OrganizeAdministratorEntity::getOrganizeId, organizeIdList);
        return this.list(queryWrapper);
    }

    @Override
    public List<OrganizeAdministratorListVo> getList(Pagination pagination) {
        List<OrganizeAdministratorEntity> list = getOrganizeAdministratorEntity(UserProvider.getLoginUserId(), PermissionConst.ORGANIZE, false);
        Map<String, String> orgIdNameMaps = organizeService.getInfoList();
        List<String> organizeIdList;
        Map<String, OrganizeEntity> orgMaps = organizeService.getOrgMaps(null, true, null);
        // 存放所有的有资格管理的组织id
        if (userProvider.get().getIsAdministrator()) {
            organizeIdList = new ArrayList<>(orgMaps.keySet());
        } else {
            Set<String> orgId = new HashSet<>(16);
            // 判断自己是哪些组织的管理员
            list.stream().forEach(t-> {
                if (t != null) {
                    // t.getThisLayerAdd() == 1 || t.getThisLayerEdit() == 1 || t.getThisLayerDelete() == 1 || (StringUtil.isNotEmpty(String.valueOf(t.getSubLayerSelect())) && t.getThisLayerSelect() == 1)
                    if (t.getSubLayerSelect() != null && t.getThisLayerSelect() == 1) {
                        orgId.add(t.getOrganizeId());
                    }
                    // t.getSubLayerAdd() == 1 || t.getSubLayerEdit() == 1 || t.getSubLayerDelete() == 1 || (StringUtil.isNotEmpty(String.valueOf(t.getSubLayerSelect())) && t.getSubLayerSelect() == 1)
                    if (t.getSubLayerSelect() != null && t.getSubLayerSelect() == 1) {
                        List<String> underOrganizations = organizeService.getUnderOrganizations(t.getOrganizeId(), true);
                        orgId.addAll(underOrganizations);
                    }
                }
            });
            organizeIdList = new ArrayList<>(orgId);
        }
        if (organizeIdList.size() < 1) {
            organizeIdList.add("");
        }
        List<OrganizeAdministratorEntity> list1 = getListByOrganizeId(organizeIdList);
        List<String> userIdList = list1.stream().map(OrganizeAdministratorEntity::getUserId).distinct().collect(Collectors.toList());
        List<String> finalOrganizeIdList = organizeIdList;
        List<String> userLists = new ArrayList<>();
        List<String> finalUserLists = userLists;
        userIdList.forEach(t -> {
            List<String> collect = userRelationService.getListByUserId(t).stream().filter(ur -> PermissionConst.ORGANIZE.equals(ur.getObjectType())).map(UserRelationEntity::getObjectId).collect(Collectors.toList());
            List<String> collect1 = finalOrganizeIdList.stream().filter(collect::contains).collect(Collectors.toList());
            if (collect1.size() > 0) {
                finalUserLists.add(t);
            }
        });
        userLists = userLists.stream().distinct().collect(Collectors.toList());
        // 验证这些人是否有权限
//        if (list.stream().anyMatch(t -> PermissionConst.SYSTEM.equals(t.getOrganizeType()))
//                || list.stream().anyMatch(t -> PermissionConst.SYSTEM.equals(t.getOrganizeType()))) {
//
//        }
        List<UserEntity> userList = userService.getUserNames(userLists, pagination, true, false);
        userList.forEach(t -> {
            // 创建时间
            Date date = getOrganizeAdministratorEntity(t.getId()).stream().sorted(Comparator.comparing(OrganizeAdministratorEntity::getCreatorTime)).map(OrganizeAdministratorEntity::getCreatorTime).findFirst().orElse(null);
            t.setCreatorTime(date);
            // 所属组织
            List<UserRelationEntity> orgRelationByUserId = userRelationService.getAllOrgRelationByUserId(t.getId());
            StringBuilder orgName = new StringBuilder();
            orgRelationByUserId.stream().forEach(or -> {
                OrganizeEntity organizeEntity = orgMaps.get(or.getObjectId());
                if (organizeEntity != null && StringUtil.isNotEmpty(organizeEntity.getOrganizeIdTree())) {
                    String fullNameByOrgIdTree = organizeService.getFullNameByOrgIdTree(orgIdNameMaps, organizeEntity.getOrganizeIdTree(), "/");
                    orgName.append("," + fullNameByOrgIdTree);
                }
            });
            // 组织名称
            String org = orgName.length() > 0 ? orgName.toString().replaceFirst(",", "") : "";
            t.setOrganizeId(org);
        });
        // 处理所属组织和创建时间
        List<OrganizeAdministratorListVo> jsonToList = JsonUtil.getJsonToList(userList, OrganizeAdministratorListVo.class);
        jsonToList = jsonToList.stream().filter(t -> t != null && t.getCreatorTime() != null).sorted(Comparator.comparing(OrganizeAdministratorListVo::getCreatorTime).reversed()).collect(Collectors.toList());
        return jsonToList;
    }

    @Override
    public List<String> getOrganizeUserList(String type) {
        if (UserProvider.getUser().getIsAdministrator()) {
            return organizeService.getList(true).stream().map(OrganizeEntity::getId).collect(Collectors.toList());
        }
        List<OrganizeAdministratorEntity> list = getOrganizeAdministratorEntity(UserProvider.getLoginUserId());
        Set<String> orgId = new HashSet<>(16);
        // 判断自己是哪些组织的管理员
        list.stream().forEach(t -> {
            if (t != null) {
                if (t.getSubLayerSelect() != null && t.getThisLayerSelect() == 1) {
                    orgId.add(t.getOrganizeId());
                }
                if (t.getSubLayerSelect() != null && t.getSubLayerSelect() == 1) {
                    List<String> underOrganizations = organizeService.getUnderOrganizations(t.getOrganizeId(), true);
                    orgId.addAll(underOrganizations);
                }
            }
        });
        List<String> orgIds = new ArrayList<>(orgId);
        if (JnpfConst.CURRENT_ORG_SUB.equals(type)) {
            return orgIds;
        }
        List<String> userList = userRelationService.getListByObjectIdAll(orgIds).stream().map(UserRelationEntity::getUserId).collect(Collectors.toList());
        return userList;
    }

    @Override
    public List<OrganizeEntity> getListByAuthorize() {
        // 通过权限转树
        List<OrganizeAdministratorEntity> listss = getOrganizeAdministratorEntity(UserProvider.getLoginUserId());
        Set<String> orgIds = new HashSet<>(16);
        // 判断自己是哪些组织的管理员
        listss.stream().forEach(t-> {
            if (t != null) {
                if (t.getThisLayerSelect() != null && t.getThisLayerSelect() == 1) {
                    orgIds.add(t.getOrganizeId());
                }
                if (t.getSubLayerSelect() != null && t.getSubLayerSelect() == 1) {
                    List<String> underOrganizations = organizeService.getUnderOrganizations(t.getOrganizeId(), true);
                    orgIds.addAll(underOrganizations);
                }
            }
        });
        List<String> list1 = new ArrayList<>(orgIds);
        // 得到所有有权限的组织
        List<OrganizeEntity> organizeName = organizeService.getOrganizeName(list1);
        return organizeName;
    }

    @Override
    public OrganizeAdministratorModel getOrganizeAdministratorList() {
        // 通过权限转树
        List<OrganizeAdministratorEntity> list = getOrganizeAdministratorEntity(userProvider.get().getUserId());
        List<String> addList = new ArrayList<>();
        List<String> editList = new ArrayList<>();
        List<String> deleteList = new ArrayList<>();
        List<String> selectList = new ArrayList<>();
        // 判断自己是哪些组织的管理员
        list.forEach(t -> {
            if (t != null) {
                //查询
                if (t.getThisLayerSelect() != null && t.getThisLayerSelect() == 1) {
                    selectList.add(t.getOrganizeId());
                    //修改
                    if (t.getThisLayerEdit() != null && t.getThisLayerEdit() == 1) {
                        editList.add(t.getOrganizeId());
                    }
                    //删除
                    if (t.getThisLayerDelete() != null && t.getThisLayerDelete() == 1) {
                        deleteList.add(t.getOrganizeId());
                    }
                    //新增
                    if (t.getThisLayerAdd() != null && t.getThisLayerAdd() == 1) {
                        addList.add(t.getOrganizeId());
                    }
                }
                //查询
                if (t.getSubLayerSelect() != null && t.getSubLayerSelect() == 1) {
                    List<String> underOrganizations = organizeService.getUnderOrganizations(t.getOrganizeId(), false);
                    selectList.addAll(underOrganizations);
                    //修改
                    if (t.getSubLayerEdit() != null && t.getSubLayerEdit() == 1) {
                        editList.addAll(underOrganizations);
                    }
                    //删除
                    if (t.getSubLayerDelete() != null && t.getSubLayerDelete() == 1) {
                        deleteList.addAll(underOrganizations);
                    }
                    //新增
                    if (t.getSubLayerAdd() != null && t.getSubLayerAdd() == 1) {
                        addList.addAll(underOrganizations);
                    }
                }
            }
        });
        OrganizeAdministratorModel model = new OrganizeAdministratorModel(addList,editList,deleteList,selectList);
        return model;
    }

    @Override
    public List<OrganizeAdministratorEntity> getInfoByUserId(String userId, String tenantId) {
        QueryWrapper<OrganizeAdministratorEntity> queryWrapper = new QueryWrapper<>();
        try {
            if (StringUtil.isEmpty(tenantId)) {
                tenantId = UserProvider.getUser().getTenantId();
            }
            DbLinkEntity dbLinkEntity = dbLinkService.getResource("0", tenantId);
            if (DbTypeUtil.checkOracle(dbLinkEntity) || DbTypeUtil.checkDM(dbLinkEntity)) {
                queryWrapper.eq("dbms_lob.substr(F_USER_ID)", userId);
            } else if (DbTypeUtil.checkSQLServer(dbLinkEntity)) {
                queryWrapper.lambda().like(OrganizeAdministratorEntity::getUserId, userId);
            } else {
                queryWrapper.lambda().eq(OrganizeAdministratorEntity::getUserId, userId);
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
        queryWrapper.lambda().eq(OrganizeAdministratorEntity::getEnabledMark, 1);
        return this.list(queryWrapper);
    }

}
