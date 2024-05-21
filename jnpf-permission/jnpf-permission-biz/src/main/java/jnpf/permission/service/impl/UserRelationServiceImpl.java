package jnpf.permission.service.impl;

import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.base.UserInfo;
import jnpf.base.service.SuperServiceImpl;
import jnpf.constant.PermissionConst;
import jnpf.permission.entity.PermissionEntityBase;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.entity.UserRelationEntity;
import jnpf.permission.mapper.UserRelationMapper;
import jnpf.permission.model.permission.PermissionModel;
import jnpf.permission.model.userrelation.UserRelationForm;
import jnpf.permission.service.OrganizeRelationService;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.PositionService;
import jnpf.permission.service.UserRelationService;
import jnpf.permission.service.UserService;
import jnpf.permission.util.PermissionUtil;
import jnpf.util.RandomUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户关系
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Service
public class UserRelationServiceImpl extends SuperServiceImpl<UserRelationMapper, UserRelationEntity> implements UserRelationService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private UserService userService;
    @Autowired
    private PositionService positionService;
    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private OrganizeRelationService organizeRelationService;
    @Autowired
    private UserRelationService userRelationService;

    @Override
    public List<UserRelationEntity> getListByUserId(String userId) {
        return getListByUserIdAll(Collections.singletonList(userId));
    }

    @Override
    public List<UserRelationEntity> getListByUserIdAndObjType(String userId, String objectType) {
        QueryWrapper<UserRelationEntity> query = new QueryWrapper<>();
        query.lambda().in(UserRelationEntity::getUserId, userId);
        query.lambda().in(UserRelationEntity::getObjectType, objectType);
        query.lambda().orderByAsc(UserRelationEntity::getSortCode).orderByDesc(UserRelationEntity::getCreatorTime);
        return this.list(query);
    }

    @Override
    public List<UserRelationEntity> getListByUserIdAll(List<String> userId) {
        if (userId.size() > 0) {
            QueryWrapper<UserRelationEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(UserRelationEntity::getUserId, userId);
            return this.list(queryWrapper);
        }
        return new ArrayList<>();
    }

    @Override
    public List<UserRelationEntity> getListByObjectId(String objectId) {
        return getListByObjectId(objectId, null);
    }

    @Override
    public List<UserRelationEntity> getListByObjectId(String objectId, String objectType) {
        QueryWrapper<UserRelationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserRelationEntity::getObjectId, objectId);
        if(objectType != null){
            queryWrapper.lambda().eq(UserRelationEntity::getObjectType, objectType);
        }
        queryWrapper.lambda().orderByAsc(UserRelationEntity::getSortCode).orderByDesc(UserRelationEntity::getCreatorTime);
        return this.list(queryWrapper);
    }

    @Override
    public List<UserRelationEntity> getListByObjectIdAll(List<String> objectId) {
        List<UserRelationEntity> list = new ArrayList<>();
        if (objectId.size() > 0) {
            QueryWrapper<UserRelationEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(UserRelationEntity::getObjectId, objectId);
            list = this.list(queryWrapper);
        }
        return list;
    }

    @Override
    public void deleteAllByObjId(String objId) {
        QueryWrapper<UserRelationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserRelationEntity::getObjectId, objId);
        this.remove(queryWrapper);
    }

    @Override
    public void deleteAllByUserId(String userId) {
        QueryWrapper<UserRelationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserRelationEntity::getUserId, userId);
        queryWrapper.lambda().ne(UserRelationEntity::getObjectType, PermissionConst.GROUP);
        userRelationService.remove(queryWrapper);
    }

    @Override
    public UserRelationEntity getInfo(String id) {
        QueryWrapper<UserRelationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserRelationEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    @DSTransactional
    public void save(String objectId, List<UserRelationEntity> entitys) {
        List<UserRelationEntity> existList = this.getListByObjectId(objectId);
        List<UserRelationEntity> relationList = new ArrayList<>();
        for (int i = 0; i < entitys.size(); i++) {
            UserRelationEntity entity = entitys.get(i);
            entity.setId(RandomUtil.uuId());
            entity.setSortCode(Long.parseLong(i + ""));
            entity.setCreatorUserId(UserProvider.getLoginUserId());
            if (existList.stream().filter(t -> t.getUserId().equals(entity.getUserId())).count() == 0) {
                relationList.add(entity);
            }
        }
        for (UserRelationEntity entity : relationList) {
            this.save(entity);
        }
    }

    @Override
    public void save(List<UserRelationEntity> list) {
        for (UserRelationEntity entity : list) {
            this.save(entity);
        }
    }

    @Override
    @DSTransactional
    public void delete(String[] ids) {
        for (String item : ids) {
            QueryWrapper<UserRelationEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(UserRelationEntity::getId, item);
            this.remove(queryWrapper);
        }
    }


    @Override
    @DSTransactional
    public void saveObjectId(String objectId, UserRelationForm userRelationForm) {
        // 修改前的岗位绑定人员ID
        List<String> beforeUserIds = userRelationService.getListByObjectId(objectId)
                .stream().map(UserRelationEntity::getUserId).collect(Collectors.toList());
        //清除原有成员数据
        deleteAllByObjId(objectId);
        UserInfo userInfo = userProvider.get();
        List<UserRelationEntity> list = new ArrayList<>();
        int i = 0;
        for (String userId : userRelationForm.getUserIds()) {
            UserRelationEntity entity = new UserRelationEntity();
            entity.setId(RandomUtil.uuId());
            entity.setSortCode(Long.parseLong(i + ""));
            entity.setObjectId(objectId);
            entity.setObjectType(userRelationForm.getObjectType());
            entity.setCreatorUserId(userInfo.getUserId());
            entity.setUserId(userId);
            list.add(entity);
            i++;
        }
        save(objectId, list);

        // 并集：所有未修改的人员
        List<String> unUpdateUserId = beforeUserIds.stream().filter(b-> userRelationForm.getUserIds()
                .contains(b)).collect(Collectors.toList());
        // 差集：所有修改过的人员(包括：删除此岗位、添加此岗位的人员)
        beforeUserIds.addAll(userRelationForm.getUserIds());
        List<String> allUpdateIds = beforeUserIds.stream().filter(u-> !unUpdateUserId.contains(u))
                .collect(Collectors.toList());

        if (PermissionConst.POSITION.equals(userRelationForm.getObjectType())) {
            // 自动切换岗位
            organizeRelationService.autoSetPosition(allUpdateIds);
        }
    }

    @Override
    public void roleSaveByUserIds(String roleId, List<String> userIds) {
        //清除原有成员数据
        deleteAllByObjId(roleId);
        String currentUserId = userProvider.get().getId();
        List<UserRelationEntity> userRelationList = new ArrayList<>();
        for (String userId : userIds) {
            UserRelationEntity entity = new UserRelationEntity();
            entity.setId(RandomUtil.uuId());
            entity.setObjectId(roleId);
            entity.setObjectType(PermissionConst.ROLE);
            entity.setCreatorUserId(currentUserId);
            entity.setUserId(userId);
            userRelationList.add(entity);
        }
        this.saveBatch(userRelationList);
    }

    @Override
    public List<UserRelationEntity> getListByObjectType(String objectType) {
        QueryWrapper<UserRelationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserRelationEntity::getObjectType, objectType);
        return this.list(queryWrapper);
    }

    @Override
    public List<UserRelationEntity> getRelationByUserIds(List<String> userIds) {
        if (userIds.size() == 0) {
            return new ArrayList<>();
        }
        QueryWrapper<UserRelationEntity> query = new QueryWrapper<>();
        query.lambda().in(UserRelationEntity::getUserId, userIds);
        query.lambda().eq(UserRelationEntity::getObjectType, PermissionConst.ORGANIZE);
        return this.list(query);
    }

    @Override
    public List<UserRelationEntity> getListByObjectType(String userId, String objectType) {
        QueryWrapper<UserRelationEntity> query = new QueryWrapper<>();
        query.lambda().eq(UserRelationEntity::getUserId, userId).eq(UserRelationEntity::getObjectType, objectType);
        query.lambda().orderByAsc(UserRelationEntity::getSortCode).orderByDesc(UserRelationEntity::getCreatorTime);
        return this.list(query);
    }

    @Override
    public List<UserRelationEntity> getAllOrgRelationByUserId(String userId){
        return this.getListByObjectType(userId,PermissionConst.ORGANIZE);
    }

    @Override
    public List<PermissionModel> getObjectVoList(String objectType) {
        String userId = userProvider.get().getUserId();
        UserEntity userEntity = userService.getInfo(userId);
        String majorOrgId = userProvider.get().getOrganizeId();

        // 组装对应组织/岗位/角色对象
        switch (objectType) {
            case PermissionConst.ORGANIZE:
                // 使用in查询减少数据库查询次数
                List<String> ids = new ArrayList<>();
                this.getListByObjectType(userId, objectType).forEach(r -> ids.add(r.getObjectId()));
                List<PermissionModel> permissionModels = setModel(organizeService.getOrgEntityList(ids, false), majorOrgId);
                permissionModels.forEach(p->p.setFullName(PermissionUtil.getLinkInfoByOrgId(p.getId(), organizeService, false)));
                return permissionModels;
            case PermissionConst.POSITION:
                // 岗位遵循一对多关系
                List<PositionEntity> positionList = positionService.getListByUserId(userId);
                if (positionList.size() > 0) {
                    return setModel(positionList.stream().filter(p -> p.getOrganizeId().equals(majorOrgId))
                            .collect(Collectors.toList()), userEntity.getPositionId());
                }
            default:
                return new ArrayList<>();
        }
    }

    /**
     * 设置返回模型
     *
     * @param permissionList
     * @param majorId
     */
    private <T extends PermissionEntityBase> List<PermissionModel> setModel (List<T> permissionList, String majorId){
        List<PermissionModel> voList = new ArrayList<>();
        permissionList.forEach(p -> {
            PermissionModel model = new PermissionModel();
            if (p.getId().equals(majorId)) {
                model.setIsDefault(true);
            } else {
                model.setIsDefault(false);
            }
            model.setFullName(p.getFullName());
            model.setId(p.getId());
            model.setFullName(p.getFullName());
            voList.add(model);
        });
        return voList;
    }

    @Override
    public Boolean existByObj(String objectType, String objectId) {
        QueryWrapper<UserRelationEntity> query = new QueryWrapper<>();
        query.lambda()
                .eq(UserRelationEntity::getObjectType, objectType)
                .eq(UserRelationEntity::getObjectId, objectId);
        return this.count(query) > 0;
    }

    @Override
    public List<UserRelationEntity> getListByRoleId(String roleId) {
        List<UserRelationEntity> list = new ArrayList<>();
        organizeRelationService.getRelationListByRoleId(roleId).forEach(o->{
            QueryWrapper<UserRelationEntity> query = new QueryWrapper<>();
            query.lambda()
                    .eq(UserRelationEntity::getObjectType, PermissionConst.ORGANIZE)
                    .eq(UserRelationEntity::getObjectId, o.getOrganizeId());
            list.addAll(this.list(query));
        });
        return list;
    }

    @Override
    public List<UserRelationEntity> getListByUserId(String userId, String objectType) {
        QueryWrapper<UserRelationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserRelationEntity::getUserId, userId);
        queryWrapper.lambda().eq(UserRelationEntity::getObjectType, objectType);
        return this.list(queryWrapper);
    }

    @Override
    public List<UserRelationEntity> getListByOrgId(List<String> orgIdList) {
        if (orgIdList.size() > 0) {
            QueryWrapper<UserRelationEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(UserRelationEntity::getObjectType, PermissionConst.ORGANIZE).in(UserRelationEntity::getObjectId, orgIdList);
            return this.list(queryWrapper);
        }
        return new ArrayList<>();
    }

}
