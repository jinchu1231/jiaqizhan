package jnpf.permission.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.base.service.ModuleService;
import jnpf.base.service.SuperServiceImpl;
import jnpf.constant.JnpfConst;
import jnpf.constant.PermissionConst;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.OrganizeRelationEntity;
import jnpf.permission.entity.PermissionGroupEntity;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.entity.UserRelationEntity;
import jnpf.permission.mapper.OrganizeRelationMapper;
import jnpf.permission.model.organize.OrganizeConditionModel;
import jnpf.permission.model.organize.OrganizeModel;
import jnpf.permission.service.AuthorizeService;
import jnpf.permission.service.OrganizeAdministratorService;
import jnpf.permission.service.OrganizeRelationService;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.PermissionGroupService;
import jnpf.permission.service.PositionService;
import jnpf.permission.service.RoleService;
import jnpf.permission.service.UserRelationService;
import jnpf.permission.service.UserService;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import jnpf.util.XSSEscape;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 组织关系 服务实现类
 * </p>
 *
 * @author YanYu
 * @since 2022-01-19
 */
@Service
public class OrganizeRelationServiceImpl extends SuperServiceImpl<OrganizeRelationMapper, OrganizeRelationEntity> implements OrganizeRelationService {

    @Autowired
    RoleService roleService;
    @Autowired
    PositionService positionService;
    @Autowired
    UserRelationService userRelationService;
    @Autowired
    UserService userService;
    @Autowired
    AuthorizeService authorizeService;
    @Autowired
    OrganizeService organizeService;
    @Autowired
    OrganizeAdministratorService organizeAdministratorService;
    @Autowired
    PermissionGroupService permissionGroupService;
    @Autowired
    private ModuleService moduleService;

    @Override
    public List<OrganizeRelationEntity> getRelationListByOrganizeId(List<String> organizeIds) {
        if (organizeIds.size() == 0) {
            return new ArrayList<>();
        }
        QueryWrapper<OrganizeRelationEntity> query = new QueryWrapper<>();
        query.lambda().in(OrganizeRelationEntity::getOrganizeId, organizeIds);
        query.lambda().orderByDesc(OrganizeRelationEntity::getCreatorTime);
        return this.list(query);
    }

    @Override
    public List<OrganizeRelationEntity> getRelationListByOrganizeId(List<String> organizeIds, String objectType) {
        QueryWrapper<OrganizeRelationEntity> query = new QueryWrapper<>();
        // 查询组织关系表集合
        if(StringUtil.isNotEmpty(objectType)) {
            query.lambda().eq(OrganizeRelationEntity::getObjectType, objectType);
        }
        if(organizeIds.size() > 0){
            query.lambda().in(OrganizeRelationEntity::getOrganizeId, organizeIds);
        } else {
            organizeIds.add("");
            query.lambda().in(OrganizeRelationEntity::getOrganizeId, organizeIds);
        }
        query.lambda().orderByDesc(OrganizeRelationEntity::getCreatorTime);
        return this.list(query);
    }

    @Override
    public List<String> getPositionListByOrganizeId(List<String> organizeIds) {
        if(organizeIds.size() > 0){
            QueryWrapper<OrganizeRelationEntity> query = new QueryWrapper<>();
            // 查询组织关系表集合
            query.lambda().eq(OrganizeRelationEntity::getObjectType, PermissionConst.POSITION);
            query.lambda().in(OrganizeRelationEntity::getOrganizeId, organizeIds);
            query.lambda().orderByDesc(OrganizeRelationEntity::getCreatorTime);
            return this.list(query).stream().map(OrganizeRelationEntity::getObjectId).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public List<OrganizeRelationEntity> getRelationListByRoleId(String roleId) {
        QueryWrapper<OrganizeRelationEntity> query = new QueryWrapper<>();
        // 查询组织关系表集合
        query.lambda().eq(OrganizeRelationEntity::getObjectType, PermissionConst.ROLE);
        query.lambda().in(OrganizeRelationEntity::getObjectId, roleId);
        query.lambda().orderByDesc(OrganizeRelationEntity::getCreatorTime);
        return this.list(query);
    }

    @Override
    public List<OrganizeRelationEntity> getRelationListByRoleIdList(List<String> roleId) {
        if (roleId.size() == 0) {
            return Collections.EMPTY_LIST;
        }
        QueryWrapper<OrganizeRelationEntity> query = new QueryWrapper<>();
        // 查询组织关系表集合
        query.lambda().eq(OrganizeRelationEntity::getObjectType, PermissionConst.ROLE);
        query.lambda().in(OrganizeRelationEntity::getObjectId, roleId);
        query.lambda().orderByDesc(OrganizeRelationEntity::getCreatorTime);
        return this.list(query);
    }

    @Override
    public Boolean existByRoleIdAndOrgId(String roleId, String organizeId) {
        QueryWrapper<OrganizeRelationEntity> query = new QueryWrapper<>();
        query.lambda().eq(OrganizeRelationEntity::getObjectType, PermissionConst.ROLE);
        query.lambda().in(OrganizeRelationEntity::getObjectId, roleId);
        query.lambda().in(OrganizeRelationEntity::getOrganizeId, organizeId);
        return count(query) > 0;
    }

    @Override
    public Boolean existByObjTypeAndOrgId(String objectType, String organizeId) {
        return existByObjAndOrgId(objectType, null, organizeId);
    }

    @Override
    public Boolean existByObjAndOrgId(String objectType, String objId, String organizeId) {
        QueryWrapper<OrganizeRelationEntity> query = new QueryWrapper<>();
        query.lambda().eq(OrganizeRelationEntity::getObjectType, objectType);
        if(StringUtil.isNotEmpty(objId)){
            query.lambda().eq(OrganizeRelationEntity::getObjectId, objId);
        }
        query.lambda().in(OrganizeRelationEntity::getOrganizeId, organizeId);
        return count(query) > 0;
    }


    @Override
    public List<OrganizeRelationEntity> getRelationListByType(String objectType) {
        QueryWrapper<OrganizeRelationEntity> query = new QueryWrapper<>();
        // 查询组织关系表集合
        query.lambda().eq(OrganizeRelationEntity::getObjectType, objectType);
        return this.list(query);
    }

    @Override
    public List<OrganizeRelationEntity> getListByTypeAndOrgId(String objectType, String orgId) {
        QueryWrapper<OrganizeRelationEntity> query = new QueryWrapper<>();
        // 查询组织关系表集合
        query.lambda().eq(OrganizeRelationEntity::getObjectType, objectType)
                .eq(OrganizeRelationEntity::getOrganizeId, orgId);
        return this.list(query);
    }

    @Override
    public Boolean deleteAllByRoleId(String roleId) {
        QueryWrapper<OrganizeRelationEntity> query = new QueryWrapper<>();
        query.lambda().eq(OrganizeRelationEntity::getObjectType, PermissionConst.ROLE);
        query.lambda().eq(OrganizeRelationEntity::getObjectId, roleId);
        return this.remove(query);
    }


    /*========================== 自动切换岗位，组织相关 ==============================*/


    @Override
    public String autoGetMajorPositionId(String userId, String currentMajorOrgId, String currentMajorPosId2){
        userId = XSSEscape.escape(userId);
        currentMajorOrgId = XSSEscape.escape(currentMajorOrgId);
        String currentMajorPosId = XSSEscape.escape(currentMajorPosId2);
        // 属于该该组织底下的岗位
        List<PositionEntity> positionList = positionService.getListByOrgIdAndUserId(currentMajorOrgId, userId);
        if(positionList.size() > 0){
            // 默认岗位是否在此组织内，若存在不做切换
            if(positionList.stream().anyMatch(p -> p.getId().equals(currentMajorPosId))){
                return currentMajorPosId;
            }else{
                // 默认第一个岗位
                return positionList.get(0).getId();
            }
        }
        return "";
    }

    @Override
    public String autoGetMajorOrganizeId(String userId, List<String> userAllOrgIds, String currentMajorOrgId, String systemId){
        if(userAllOrgIds.size() > 0){
            if (userAllOrgIds.contains(currentMajorOrgId) && checkBasePermission(userId, currentMajorOrgId, systemId).size() > 0) {
                // 保持原默认组织不切换
                return currentMajorOrgId;
            }else{
                // 去除原本默认组织ID
                List<String> selectOrgIds = userAllOrgIds.stream().filter(usi-> !usi.equals(currentMajorOrgId)).collect(Collectors.toList());
                // 若不存在，强制切换有基本登录权限的角色
                for (String orgId : selectOrgIds) {
                    if (this.checkBasePermission(userId, orgId, systemId).size() > 0) {
                        // 这个组织ID底下角色存在基础登录权限
                        return orgId;
                    }
                }
            }
            // 随机赋值一个
            return userAllOrgIds.get(0);
        }else {
            return "";
        }
    }




    /*== 自动切换组织 ==*/

    @Override
    public void autoSetOrganize(List<String> allUpdateUserIds){
        if(allUpdateUserIds.size() > 0){
            for (UserEntity userEntity : userService.listByIds(allUpdateUserIds)) {
                String useId = userEntity.getId();
                String majorOrgId = userEntity.getOrganizeId();
                List<String> orgList = userRelationService.getListByObjectType(useId, PermissionConst.ORGANIZE)
                        .stream().map(UserRelationEntity::getObjectId).collect(Collectors.toList());
                String changeOrgId = this.autoGetMajorOrganizeId(useId, orgList, majorOrgId, null);
                if(!changeOrgId.equals(majorOrgId)){
                    // 切换默认组织
                    UserEntity updateUserEntity = new UserEntity();
                    updateUserEntity.setId(useId);
                    updateUserEntity.setOrganizeId(changeOrgId);
                    userService.updateById(updateUserEntity);
                }
            }
        }
    }

    @Override
    public void autoSetPosition(List<String> allUpdateUserIds){
        if(allUpdateUserIds.size() > 0){
            for (UserEntity user : userService.listByIds(allUpdateUserIds)) {
                String majorPosId = user.getPositionId();
                String changePositionId = this.autoGetMajorPositionId(user.getId(), user.getOrganizeId(), majorPosId);
                if(!changePositionId.equals(majorPosId)){
                    UserEntity updateUser = new UserEntity();
                    updateUser.setId(user.getId());
                    updateUser.setPositionId(changePositionId);
                    userService.updateById(updateUser);
                }
            }
        }
    }


    /*===================== 权限判断 =======================*/

    @Override
    public List<PermissionGroupEntity> checkBasePermission(String userId, String orgId, String systemId){
        List<PermissionGroupEntity> permissionGroupByUserId = permissionGroupService.getPermissionGroupByUserId(userId, orgId, false, systemId);
        return permissionGroupByUserId;
    }

    @Override
    public List<OrganizeRelationEntity> getRelationListByObjectIdAndType(String objectType, String objectId) {
        QueryWrapper<OrganizeRelationEntity> query = new QueryWrapper<>();
        query.lambda().eq(OrganizeRelationEntity::getObjectId, objectId);
        query.lambda().eq(OrganizeRelationEntity::getObjectType, objectType);
        return this.list(query);
    }

    @Override
    public List<String> getOrgIds(List<String> departIds, String type) {
        departIds = XSSEscape.escapeObj(departIds);
        List<String> idList = new ArrayList<>(16);
        // 获取所有组织
        if (departIds.size() > 0) {
            List<String> collect = departIds.stream().filter(JnpfConst.SYSTEM_PARAM.keySet()::contains).collect(Collectors.toList());
            String organizeId = UserProvider.getUser().getOrganizeId();
            collect.forEach(t -> {
                if (JnpfConst.CURRENT_ORG.equals(t) || JnpfConst.CURRENT_ORG_TYPE.equals(t)) {
                    idList.add(organizeId + "--" + PermissionConst.COMPANY);
                    idList.add(organizeId);
                } else if (JnpfConst.CURRENT_ORG_SUB.equals(t) || JnpfConst.CURRENT_ORG_SUB_TYPE.equals(t)) {
                    List<String> underOrganizations = organizeService.getUnderOrganizations(organizeId, true);
                    underOrganizations.add(organizeId);
                    underOrganizations.forEach(orgId -> {
                        idList.add(orgId + "--" + PermissionConst.COMPANY);
                        idList.add(orgId);
                    });
                } else if (JnpfConst.CURRENT_GRADE.equals(t) || JnpfConst.CURRENT_GRADE_TYPE.equals(t)) {
                    List<String> organizeUserList = organizeAdministratorService.getOrganizeUserList(JnpfConst.CURRENT_ORG_SUB);
                    organizeUserList.forEach(orgId -> {
                        idList.add(orgId + "--" + PermissionConst.COMPANY);
                        idList.add(orgId);
                    });
                }
            });
            departIds.removeAll(collect);
            idList.addAll(departIds);
            for (String departId : departIds) {
                String[] split = departId.split("--");
                if (split.length == 1 || split.length == 0) {
                    continue;
                }
                if (split.length > 1) {
                    if (PermissionConst.ORGANIZE.equals(split[1])) {
                        departId= split[0];
                    }
                    if (PermissionConst.DEPARTMENT.equals(split[1])) {
                        departId = split[0];
                    }
                }
                if (!PermissionConst.ROLE.equals(type)) {
                    List<String> underOrganizations = organizeService.getUnderOrganizations(departId, true);
                    if (underOrganizations.size() > 0) {
                        idList.addAll(underOrganizations);
                        idList.add(organizeId);
                    }
                }
            }
        }
        return idList.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public List<OrganizeModel> getOrgIdsList(OrganizeConditionModel organizeConditionModel) {
        List<String> ids = new ArrayList<>();
        List<String> orgIds = getOrgIds(organizeConditionModel.getDepartIds(), null);
        Map<String, String> orgIdNameMaps = organizeService.getInfoList();
        orgIds.forEach(t -> ids.add(t.split("--")[0]));
        List<OrganizeEntity> listAll = organizeService.getListAll(ids, organizeConditionModel.getKeyword());
        List<OrganizeModel> organizeList = JsonUtil.getJsonToList(listAll, OrganizeModel.class);
        organizeList.forEach(t->{
            t.setIcon("department".equals(t.getType()) ? "icon-ym icon-ym-tree-department1" : "icon-ym icon-ym-tree-organization3");
            t.setLastFullName(t.getFullName());
            if (StringUtil.isNotEmpty(t.getOrganizeIdTree())) {
                t.setOrganizeIds(Arrays.asList(t.getOrganizeIdTree().split(",")));
                t.setOrganize(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, t.getOrganizeIdTree(), "/"));
                String[] split = t.getOrganizeIdTree().split(",");
                List<String> list = Arrays.asList(split);
                Collections.reverse(list);
                for (int i = 1; i < list.size(); i++) {
                    String orgId = list.get(i);
                    List<OrganizeModel> collect1 = organizeList.stream().filter(tt -> orgId.equals(tt.getId())).collect(Collectors.toList());
                    if (collect1.size() > 0) {
                        String[] split1 = StringUtil.isNotEmpty(t.getOrganizeIdTree()) ? t.getOrganizeIdTree().split(orgId) : new String[0];
                        if (split1.length > 0) {
                            t.setFullName(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, split1[1], "/"));
                        }
                        t.setOrganize(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, t.getOrganizeIdTree(), "/"));
                        t.setParentId(orgId);
                        break;
                    }
                }
            }
        });
        return organizeList;
    }

}
