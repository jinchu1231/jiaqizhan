package jnpf.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.page.PageMethod;
import jnpf.base.service.SuperServiceImpl;
import jnpf.constant.PermissionConst;
import jnpf.permission.entity.AuthorizeEntity;
import jnpf.permission.entity.OrganizeAdministratorEntity;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.OrganizeRelationEntity;
import jnpf.permission.entity.RoleEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.entity.UserRelationEntity;
import jnpf.permission.mapper.RoleMapper;
import jnpf.permission.model.role.RolePagination;
import jnpf.permission.service.AuthorizeService;
import jnpf.permission.service.OrganizeAdministratorService;
import jnpf.permission.service.OrganizeRelationService;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.RoleService;
import jnpf.permission.service.UserRelationService;
import jnpf.permission.service.UserService;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统角色
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Service
public class RoleServiceImpl extends SuperServiceImpl<RoleMapper, RoleEntity> implements RoleService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private UserRelationService userRelationService;
    @Autowired
    private AuthorizeService authorizeService;
    @Autowired
    private OrganizeRelationService organizeRelationService;
    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private OrganizeAdministratorService organizeAdministratorService;

    @Override
    public List<RoleEntity> getList(boolean filterEnabledMark) {
        QueryWrapper<RoleEntity> queryWrapper = new QueryWrapper<>();
        if (filterEnabledMark) {
            queryWrapper.lambda().eq(RoleEntity::getEnabledMark, 1);
        }
        queryWrapper.lambda().orderByAsc(RoleEntity::getSortCode).orderByDesc(RoleEntity::getCreatorTime);
        return this.list(queryWrapper);
    }

    @Override
    public List<RoleEntity> getList(RolePagination pagination, Integer globalMark) {
        // 需要查询哪些组织
        List<String> orgIds = new ArrayList<>();
        // 所有有权限的组织
        Set<String> orgId = new HashSet<>(16);
        if (!userProvider.get().getIsAdministrator()) {
            // 通过权限转树
            List<OrganizeAdministratorEntity> listss = organizeAdministratorService.getOrganizeAdministratorEntity(userProvider.get().getUserId());
            // 判断自己是哪些组织的管理员
            listss.forEach(t -> {
                if (t != null) {
                    if (t.getThisLayerSelect() != null && t.getThisLayerSelect() == 1) {
                        orgId.add(t.getOrganizeId());
                    }
                    if (t.getSubLayerSelect() != null && t.getSubLayerSelect() == 1) {
                        List<String> underOrganizations = organizeService.getUnderOrganizations(t.getOrganizeId(), false);
                        orgId.addAll(underOrganizations);
                    }
                }
            });
        } else {
            orgId.addAll(organizeService.getOrgMapsAll(OrganizeEntity::getId).keySet());
        }

        if (!StringUtil.isEmpty(pagination.getOrganizeId())) {
            List<String> underOrganizations = organizeService.getUnderOrganizations(pagination.getOrganizeId(), false);
            // 判断哪些组织时有权限的
            List<String> collect = underOrganizations.stream().filter(orgId::contains).collect(Collectors.toList());
            orgIds.add(pagination.getOrganizeId());
            orgIds.addAll(collect);
            orgIds.add(pagination.getOrganizeId());
        } else {
            if (orgId.size() == 0) {
                return new ArrayList<>();
            }
            QueryWrapper<RoleEntity> queryWrapper = new QueryWrapper<>();
            if (StringUtil.isNotEmpty(pagination.getKeyword())) {
                queryWrapper.lambda().and(
                        t -> t.like(RoleEntity::getFullName, pagination.getKeyword())
                                .or().like(RoleEntity::getEnCode, pagination.getKeyword())
                );
            }
            if (globalMark > -1) {
                queryWrapper.lambda().eq(RoleEntity::getGlobalMark, globalMark);
            }
            if (!userProvider.get().getIsAdministrator()) {
                queryWrapper.lambda().ne(RoleEntity::getGlobalMark, 1);
                List<String> collect = organizeRelationService.getRelationListByOrganizeId(new ArrayList<>(orgId), PermissionConst.ROLE).stream().map(OrganizeRelationEntity::getObjectId).collect(Collectors.toList());
                if (collect.size() == 0) {
                    collect.add("");
                }
                queryWrapper.lambda().in(RoleEntity::getId, collect);
            }
            if (pagination.getEnabledMark() != null) {
                queryWrapper.lambda().eq(RoleEntity::getEnabledMark, pagination.getEnabledMark());
            }
            long count = this.count(queryWrapper);
            queryWrapper.lambda().orderByAsc(RoleEntity::getSortCode).orderByDesc(RoleEntity::getCreatorTime);
            Page<RoleEntity> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize(), count, false);
            page.setOptimizeCountSql(false);
            IPage<RoleEntity> iPage = this.page(page, queryWrapper);
            return pagination.setData(iPage.getRecords(), page.getTotal());
        }

        String keyword = "";
        if (!StringUtil.isEmpty(pagination.getKeyword())) {
            keyword = "%" + pagination.getKeyword() + "%";
        }
        PageHelper.startPage((int) pagination.getCurrentPage(), (int) pagination.getPageSize(), false);
        PageMethod.getLocalPage().keepOrderBy(true);
        List<String> query = this.baseMapper.query(orgIds, keyword, globalMark, pagination.getEnabledMark());
        Long count = this.baseMapper.count(orgIds, keyword, globalMark, pagination.getEnabledMark());
        PageInfo pageInfo = new PageInfo(query);
        // 赋值分页参数
        pagination.setTotal(count);
        pagination.setCurrentPage(pageInfo.getPageNum());
        pagination.setPageSize(pageInfo.getPageSize());
        if (pageInfo.getList() != null && pageInfo.getList().size() > 0) {
            QueryWrapper<RoleEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(RoleEntity::getId, pageInfo.getList());
            queryWrapper.lambda().orderByAsc(RoleEntity::getSortCode).orderByDesc(RoleEntity::getCreatorTime);
            return this.list(queryWrapper);
        }
        return new ArrayList<>();
    }

    @Override
    public List<RoleEntity> getListByUserId(String userId) {
        QueryWrapper<RoleEntity> query = new QueryWrapper<>();
        List<String> roleRelations = userRelationService.getListByObjectType(userId, PermissionConst.ROLE).stream()
                .map(UserRelationEntity::getObjectId).collect(Collectors.toList());
        if(roleRelations.size() > 0){
            query.lambda().in(RoleEntity::getId, roleRelations);
            return this.list(query);
        }else {
            return new ArrayList<>();
        }
    }

    @Override
    public List<RoleEntity> getListByUserIdAndOrgId(String userId, String orgId) {
        return getListByUserId(userId).stream()
                .filter(role-> organizeRelationService.existByRoleIdAndOrgId(role.getId(), orgId))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getRoleIdsByCurrentUser() {
        UserEntity userEntity = userService.getInfo(UserProvider.getLoginUserId());
        return getAllRoleIdsByUserIdAndOrgId(userEntity.getId(), userEntity.getOrganizeId());
    }

    @Override
    public List<String> getRoleIdsByCurrentUser(String orgId) {
        UserEntity userEntity = userService.getInfo(UserProvider.getLoginUserId());
        return getAllRoleIdsByUserIdAndOrgId(userEntity.getId(), orgId);
    }

    @Override
    public List<String> getAllRoleIdsByUserIdAndOrgId(String userId, String orgId) {
        // 用户当前组织下的角色
        List<String> roleIds = getListByUserIdAndOrgId(userId, orgId).stream()
                .map(RoleEntity::getId).collect(Collectors.toList());
        // 用户绑定的全局角色
        List<String> globalRoleIds = userRelationService.getListByUserIdAndObjType(userId, PermissionConst.ROLE).stream()
                .map(UserRelationEntity::getObjectId).collect(Collectors.toList());
        globalRoleIds = roleService.getListByIds(globalRoleIds, null, false).stream().filter(r -> "1".equals(String.valueOf(r.getGlobalMark())))
                .map(RoleEntity::getId).collect(Collectors.toList());
        roleIds.addAll(globalRoleIds);
        return roleIds.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public RoleEntity getInfo(String id) {
        QueryWrapper<RoleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RoleEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public Boolean isExistByFullName(String fullName, String id, Integer globalMark) {
        QueryWrapper<RoleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RoleEntity::getFullName, fullName);
        queryWrapper.lambda().eq(RoleEntity::getGlobalMark, globalMark);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(RoleEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public Boolean isExistByEnCode(String enCode, String id) {
        QueryWrapper<RoleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RoleEntity::getEnCode, enCode);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(RoleEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public Boolean update(String id, RoleEntity entity) {
        entity.setId(id);
        entity.setLastModifyTime(new Date());
        entity.setLastModifyUserId(UserProvider.getLoginUserId());
        return this.updateById(entity);
    }

    @Override
    public void create(RoleEntity entity) {
        entity.setCreatorUserId(UserProvider.getLoginUserId());
        this.save(entity);
    }

    @Override
    @Transactional
    public void delete(RoleEntity entity) {
        if (entity != null) {
            this.removeById(entity.getId());
            QueryWrapper<AuthorizeEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(AuthorizeEntity::getObjectId, entity.getId());
            authorizeService.remove(queryWrapper);
            QueryWrapper<UserRelationEntity> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(UserRelationEntity::getObjectId, entity.getId());
            userRelationService.remove(wrapper);
        }
    }
    @Override
    public List<RoleEntity> getListByIds(List<String> id, String keyword, boolean filterEnabledMark) {
        List<RoleEntity> roleList = new ArrayList<>();
        if (id.size() > 0) {
            QueryWrapper<RoleEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(RoleEntity::getId, id);
            if (filterEnabledMark) {
                queryWrapper.lambda().eq(RoleEntity::getEnabledMark, 1);
            }
            if (StringUtil.isNotEmpty(keyword)) {
                queryWrapper.lambda().and(
                        t -> t.like(RoleEntity::getFullName, keyword)
                                .or().like(RoleEntity::getEnCode, keyword)
                );
            }
            roleList = this.list(queryWrapper);
        }
        return roleList;
    }

    @Override
    public List<RoleEntity> getSwaptListByIds(Set<String> roleIds) {
        if (roleIds.size()>0){
            QueryWrapper<RoleEntity> roleWrapper = new QueryWrapper<>();
            roleWrapper.lambda().select(RoleEntity::getFullName,RoleEntity::getId).in(RoleEntity::getId,roleIds);
            List<RoleEntity> list = roleService.list(roleWrapper);
            return list;
        }
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getRoleMap() {
        QueryWrapper<RoleEntity> roleWrapper = new QueryWrapper<>();
        roleWrapper.lambda().select(RoleEntity::getFullName,RoleEntity::getId);
        List<RoleEntity> list = roleService.list(roleWrapper);
        return list.stream().collect(Collectors.toMap(RoleEntity::getId,RoleEntity::getFullName));
    }
    @Override
    public Map<String, Object> getRoleMap(String type) {
        if ("id-fullName".equals(type)){
            return roleService.getRoleMap();
        } else {
            return roleService.getRoleNameAndIdMap();
        }
    }
    @Override
    public Map<String, Object> getRoleNameAndIdMap() {
        QueryWrapper<RoleEntity> roleWrapper = new QueryWrapper<>();
        roleWrapper.lambda().select(RoleEntity::getFullName,RoleEntity::getId, RoleEntity::getEnCode);
        List<RoleEntity> list = roleService.list(roleWrapper);
        return list.stream().collect(Collectors.toMap(role->role.getFullName() + "/" + role.getEnCode(),RoleEntity::getId));
    }

    @Override
    public RoleEntity getInfoByFullName(String fullName) {
        QueryWrapper<RoleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RoleEntity::getFullName, fullName);
        return this.getOne(queryWrapper);
    }

    @Override
    public List<RoleEntity> getGlobalList() {
        QueryWrapper<RoleEntity> query = new QueryWrapper<>();
        query.lambda().eq(RoleEntity::getGlobalMark, 1).eq(RoleEntity::getEnabledMark, 1);
        return this.list(query);
    }

    @Override
    public List<RoleEntity> getGlobalList(List<String> ids) {
        if (ids.size() == 0) {
            return Collections.EMPTY_LIST;
        }
        QueryWrapper<RoleEntity> query = new QueryWrapper<>();
        query.lambda().in(RoleEntity::getId, ids);
        query.lambda().eq(RoleEntity::getGlobalMark, 1).eq(RoleEntity::getEnabledMark, 1);
        return this.list(query);
    }

    @Override
    public Boolean existCurRoleByOrgId(String orgId) {
        List<UserRelationEntity> roleRelationList = userRelationService
                .getListByObjectType(userProvider.get().getUserId(), PermissionConst.ROLE);
        for(UserRelationEntity userRelationEntity : roleRelationList){
            if(organizeRelationService.existByRoleIdAndOrgId(userRelationEntity.getObjectId(), orgId)){
                return true;
            }
        }
        return false;
    }

    @Override
    public List<RoleEntity> getCurRolesByOrgId(String orgId) {
        String userId = userProvider.get().getUserId();
        List<UserRelationEntity> userRelations = userRelationService.getListByObjectType(userId, PermissionConst.ROLE);
        List<RoleEntity> roles = new ArrayList<>();
        userRelations.forEach(ur->{
            // 获取全局角色
            RoleEntity roleEntity = this.getInfo(ur.getObjectId());
            if(roleEntity != null && roleEntity.getGlobalMark() != null && roleEntity.getGlobalMark() == 1 && roleEntity.getEnabledMark() == 1) {
                roles.add(roleEntity);
            }else {
                organizeRelationService.getRelationListByRoleId(ur.getObjectId()).forEach(or -> {
                    if (roleEntity.getEnabledMark() == 1 && or.getOrganizeId().equals(orgId)) {
                        roles.add(roleEntity);
                    }
                });
            }
        });
        return roles;
    }

    @Override
    public List<RoleEntity> getRolesByOrgId(String orgId) {
        List<String> ids = new ArrayList<>();
        organizeRelationService.getListByTypeAndOrgId(PermissionConst.ROLE, orgId).forEach(o->{
            ids.add(o.getObjectId());
        });
        QueryWrapper<RoleEntity> query = new QueryWrapper<>();
        if(ids.size() > 0){
            query.lambda().in(RoleEntity::getId, ids);
            return this.list(query);
        }else {
            return new ArrayList<>();
        }
    }

    @Override
    public String getBindInfo(String roleId, List<String> reduceOrgIds){
        if(reduceOrgIds.size() > 0){
            StringBuilder info = new StringBuilder();
            RoleEntity roleEntity = this.getInfo(roleId);
            List<UserRelationEntity> bingUserByRoleList = userRelationService.getListByObjectId(roleId, PermissionConst.ROLE);
            if(bingUserByRoleList.size() < 1){
                return null;
            }
            info.append("已绑定用户：");
            boolean bindFlag = false;
            for (UserRelationEntity bingUser : bingUserByRoleList) {
                String userId = bingUser.getUserId();
                if(roleEntity.getGlobalMark() == 1){
                    UserEntity user = userService.getInfo(userId);
                    info.append("[ ").append(user.getRealName()).append("/").append(user.getAccount()).append(" ] ");
                    bindFlag = true;
                }else {
                    // 这个用户所绑定的组织
                    List<UserRelationEntity> bingUserByOrg = userRelationService.getListByObjectType(userId, PermissionConst.ORGANIZE);
                    for (UserRelationEntity bingOrg : bingUserByOrg) {
                        String orgId = bingOrg.getObjectId();
                        if(reduceOrgIds.contains(orgId)){
                            OrganizeEntity org = organizeService.getInfo(orgId);
                            UserEntity user = userService.getInfo(bingOrg.getUserId());
                            info.append("[").append(org.getFullName()).append("：用户（").append(user.getRealName()).append("）]; ");
                            bindFlag = true;
                        }
                    }
                }
            }

            if(bindFlag){
                return info.toString();
            }else {
                return null;
            }
        }else {
            return null;
        }
    }
}
