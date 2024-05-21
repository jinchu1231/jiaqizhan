package jnpf.permission.service.impl;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.xuyanwu.spring.file.storage.FileInfo;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.page.PageMethod;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import jnpf.base.Pagination;
import jnpf.base.UserInfo;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.service.SuperServiceImpl;
import jnpf.base.vo.DownloadVO;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.JnpfConst;
import jnpf.constant.PermissionConst;
import jnpf.consts.AuthConsts;
import jnpf.database.source.DbBase;
import jnpf.database.util.DataSourceUtil;
import jnpf.database.util.TenantDataSourceUtil;
import jnpf.exception.DataException;
import jnpf.message.model.OnlineUserModel;
import jnpf.message.model.OnlineUserProvider;
import jnpf.permission.entity.GroupEntity;
import jnpf.permission.entity.OrganizeAdministratorEntity;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.OrganizeRelationEntity;
import jnpf.permission.entity.PermissionGroupEntity;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.entity.RoleEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.entity.UserOldPasswordEntity;
import jnpf.permission.entity.UserRelationEntity;
import jnpf.permission.mapper.UserMapper;
import jnpf.permission.model.user.PaginationUser;
import jnpf.permission.model.user.UserByRoleVO;
import jnpf.permission.model.user.UserConditionModel;
import jnpf.permission.model.user.UserExportExceptionVO;
import jnpf.permission.model.user.UserExportVO;
import jnpf.permission.model.user.UserIdListVo;
import jnpf.permission.model.user.UserImportModel;
import jnpf.permission.model.user.UserImportVO;
import jnpf.permission.model.user.UserUpdateModel;
import jnpf.permission.service.GroupService;
import jnpf.permission.service.OrganizeAdministratorService;
import jnpf.permission.service.OrganizeRelationService;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.PermissionGroupService;
import jnpf.permission.service.PositionService;
import jnpf.permission.service.RoleService;
import jnpf.permission.service.UserOldPasswordService;
import jnpf.permission.service.UserRelationService;
import jnpf.permission.service.UserService;
import jnpf.util.CacheKeyUtil;
import jnpf.util.DateUtil;
import jnpf.util.ExcelUtil;
import jnpf.util.FileUploadUtils;
import jnpf.util.JsonUtil;
import jnpf.util.Md5Util;
import jnpf.util.PinYinUtil;
import jnpf.util.RandomUtil;
import jnpf.util.RedisUtil;
import jnpf.util.StringUtil;
import jnpf.util.UploaderUtil;
import jnpf.util.UserProvider;
import lombok.Cleanup;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static jnpf.consts.AuthConsts.TOKEN_PREFIX;
import static jnpf.util.Constants.ADMIN_KEY;

/**
 * 用户信息
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Service
public class UserServiceImpl extends SuperServiceImpl<UserMapper, UserEntity> implements UserService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private UserRelationService userRelationService;
    @Autowired
    private UserOldPasswordService userOldPasswordService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private PositionService positionService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private CacheKeyUtil cacheKeyUtil;
    @Autowired
    private DataSourceUtil dataSourceUtil;
    @Autowired
    private DictionaryDataService dictionaryDataService;
    @Autowired
    private OrganizeRelationService organizeRelationService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private OrganizeAdministratorService organizeAdministratorService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private PermissionGroupService permissionGroupService;

    @Override
    public List<UserEntity> getList(boolean filterEnabledMark) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        if (filterEnabledMark) {
            queryWrapper.lambda().eq(UserEntity::getEnabledMark, 1);
        }
        queryWrapper.lambda().orderByAsc(UserEntity::getSortCode).orderByDesc(UserEntity::getCreatorTime);;
        return this.list(queryWrapper);
    }

    @Override
    public List<UserEntity> getUserNameList(List<String> idList) {
        if (idList.size() > 0) {
            QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(UserEntity::getId, idList);
            return this.list(queryWrapper);
        }
        return new ArrayList<>();
    }

    @Override
    public List<UserEntity> getUserNameList(Set<String> idList) {
        if (idList.size() > 0) {
            QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().select(UserEntity::getId, UserEntity::getRealName).in(UserEntity::getId, idList);
            return this.list(queryWrapper);
        }
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getUserMap() {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(UserEntity::getId, UserEntity::getRealName,UserEntity::getAccount);
        Map<String, Object> userMap = new HashMap<>();
        this.list(queryWrapper).stream().forEach(user->userMap.put(user.getId(),user.getRealName()+"/"+user.getAccount()));
        return userMap;
    }
    @Override
    public Map<String, Object> getUserMap(String type) {
        return "id-fullName".equals(type) ?  getUserMap() : getUserNameAndIdMap();
    }

    @Override
    public Map<String, Object> getUserNameAndIdMap() {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(UserEntity::getId, UserEntity::getRealName,UserEntity::getAccount);
        Map<String, Object> userMap = new HashMap<>();
        this.list(queryWrapper).stream().forEach(user->userMap.put(user.getRealName()+"/"+user.getAccount(), user.getId()));
        return userMap;
    }


    @Override
    public UserEntity getByRealName(String realName) {
        UserEntity userEntity = new UserEntity();
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getRealName, realName);
        queryWrapper.lambda().select(UserEntity::getId);
        List<UserEntity> list = this.list(queryWrapper);
        if (list.size() > 0) {
            userEntity = list.get(0);
        }
        return userEntity;
    }

    @Override
    public List<UserEntity> getList(Pagination pagination, String organizeId, Boolean flag, Boolean filter, Integer enabledMark, String gender) {
        // 定义变量判断是否需要使用修改时间倒序
        boolean filterLastTime = false;
        String userId = userProvider.get().getUserId();
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        if (flag) {
            queryWrapper.lambda().ne(UserEntity::getId, userId);
        }
        if (filter) {
            queryWrapper.lambda().ne(UserEntity::getAccount, ADMIN_KEY);
        }
        //组织机构
        if (!StringUtil.isEmpty(organizeId)) {
            List<String> orgIdList = organizeService.getUnderOrganizationss(organizeId);
            orgIdList.add(organizeId);
            PageHelper.startPage((int) pagination.getCurrentPage(), (int) pagination.getPageSize(), false);
            //组织数量很多时解析SQL很慢, COUNT不解析SQL不去除ORDERBY
            PageMethod.getLocalPage().keepOrderBy(true);
            // 用户id
            List<String> query = new ArrayList<>(16);
            String dbSchema = null;
            // 判断是否为多租户
            if (configValueUtil.isMultiTenancy() && DbBase.DM.equalsIgnoreCase(dataSourceUtil.getDbType())) {
                dbSchema = dataSourceUtil.getDbSchema();
            }
            String keyword = null;
            if (StringUtil.isNotEmpty(pagination.getKeyword())) {
                keyword = "%" + pagination.getKeyword() + "%";
            }
            query = userMapper.query(orgIdList, keyword, dbSchema, enabledMark, gender);
            Long count = this.baseMapper.count(orgIdList, keyword, dbSchema, enabledMark, gender);
            PageInfo pageInfo = new PageInfo(query);
            // 赋值分页参数
            pagination.setTotal(count);
            pagination.setCurrentPage(pageInfo.getPageNum());
            pagination.setPageSize(pageInfo.getPageSize());
            if (pageInfo.getList().size() > 0) {
                // 存放返回结果
                QueryWrapper<UserEntity> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.lambda().in(UserEntity::getId, query);
                List<UserEntity> entityList = getBaseMapper().selectList(queryWrapper1);
//                List<UserEntity> entityList = new ArrayList<>(16);
//                for (Object userIds : pageInfo.getList()) {
//                    QueryWrapper<UserEntity> queryWrapper1 = new QueryWrapper<>();
//                    queryWrapper1.lambda().eq(UserEntity::getId, userIds);
//                    entityList.add(this.getOne(queryWrapper1));
//                }
                return entityList;
            } else {
                return new ArrayList<>();
            }
        }
        if (!userProvider.get().getIsAdministrator()) {
            // 通过权限转树
            List<OrganizeAdministratorEntity> listss = organizeAdministratorService.getOrganizeAdministratorEntity(userProvider.get().getUserId());
            Set<String> orgIds = new HashSet<>(16);
            // 判断自己是哪些组织的管理员
            listss.stream().forEach(t-> {
                if (t != null) {
                    if (t.getThisLayerSelect() != null && t.getThisLayerSelect() == 1) {
                        orgIds.add(t.getOrganizeId());
                    }
                    if (t.getSubLayerSelect() != null && t.getSubLayerSelect() == 1) {
                        List<String> underOrganizations = organizeService.getUnderOrganizations(t.getOrganizeId(), false);
                        orgIds.addAll(underOrganizations);
                    }
                }
            });
            List<String> list1 = new ArrayList<>(orgIds);
            // 得到所有有权限的组织
            List<OrganizeEntity> organizeName = new ArrayList<>(organizeService.getOrganizeName(list1, null, false, null).values());
            // 用户关系表得到所有的人
            List<String> collect = organizeName.stream().map(OrganizeEntity::getId).collect(Collectors.toList());
            List<UserRelationEntity> listByObjectIdAll = userRelationService.getListByOrgId(collect);
            List<String> collect1 = listByObjectIdAll.stream().map(UserRelationEntity::getUserId).distinct().collect(Collectors.toList());
            return getUserNames(collect1, pagination, false, ObjectUtil.equal(enabledMark, 1));
        }
        //关键字（账户、姓名、手机）
        if (!StringUtil.isEmpty(pagination.getKeyword())) {
            filterLastTime = true;
            queryWrapper.lambda().and(
                    t -> t.like(UserEntity::getAccount, pagination.getKeyword())
                            .or().like(UserEntity::getRealName, pagination.getKeyword())
                            .or().like(UserEntity::getMobilePhone, pagination.getKeyword())
            );
        }
        if (enabledMark != null) {
            queryWrapper.lambda().eq(UserEntity::getEnabledMark, enabledMark);
        }
        if (StringUtil.isNotEmpty(gender)) {
            queryWrapper.lambda().eq(UserEntity::getGender, gender);
        }
        //排序
        long count = this.count(queryWrapper);
        queryWrapper.lambda().select(UserEntity::getId);
        queryWrapper.lambda().orderByAsc(UserEntity::getSortCode).orderByDesc(UserEntity::getCreatorTime);
        if (filterLastTime) {
            queryWrapper.lambda().orderByDesc(UserEntity::getLastModifyTime);
        }
        Page<UserEntity> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize(), count, false);
        page.setOptimizeCountSql(false);
        IPage<UserEntity> iPage = this.page(page, queryWrapper);
        if(!iPage.getRecords().isEmpty()){
            List<String> ids = iPage.getRecords().stream().map(m->m.getId()).collect(Collectors.toList());
            queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(UserEntity::getId, ids);
            queryWrapper.lambda().orderByAsc(UserEntity::getSortCode).orderByDesc(UserEntity::getCreatorTime);
            if (filterLastTime) {
                queryWrapper.lambda().orderByDesc(UserEntity::getLastModifyTime);
            }
            iPage.setRecords(this.list(queryWrapper));
        }
        return pagination.setData(iPage.getRecords(), iPage.getTotal());
    }

    @Override
    public List<UserEntity> getList(Pagination pagination) {
        // 定义变量判断是否需要使用修改时间倒序
        boolean filterLastTime = false;
        String userId = UserProvider.getLoginUserId();
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().ne(UserEntity::getId, userId);
        queryWrapper.lambda().ne(UserEntity::getEnabledMark, 0);
        //关键字（账户、姓名、手机）
        if (!StringUtil.isEmpty(pagination.getKeyword())) {
            filterLastTime = true;
            queryWrapper.lambda().and(
                    t -> t.like(UserEntity::getAccount, pagination.getKeyword())
                            .or().like(UserEntity::getRealName, pagination.getKeyword())
                            .or().like(UserEntity::getMobilePhone, pagination.getKeyword())
            );
        }
        //排序
        queryWrapper.lambda().orderByAsc(UserEntity::getSortCode).orderByDesc(UserEntity::getCreatorTime);
        if (filterLastTime) {
            queryWrapper.lambda().orderByDesc(UserEntity::getLastModifyTime);
        }
        Page<UserEntity> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
        IPage<UserEntity> iPage = this.page(page, queryWrapper);
        return pagination.setData(iPage.getRecords(), iPage.getTotal());
    }

    @Override
    public List<UserEntity> getList(String keyword) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().ne(UserEntity::getEnabledMark, 0);
        if (StringUtil.isNotEmpty(keyword)) {
            //通过关键字查询
            queryWrapper.lambda().and(
                    t -> t.like(UserEntity::getAccount, keyword)
                            .or().like(UserEntity::getRealName, keyword)
                            .or().like(UserEntity::getMobilePhone, keyword)
            );
        }
//        queryWrapper.lambda().select(UserEntity::getId, UserEntity::getAccount, UserEntity::getRealName);
        Page<UserEntity> page = new Page<>(1, 50);
        IPage<UserEntity> iPage = this.page(page, queryWrapper);
        return iPage.getRecords();
    }

    @Override
    public List<UserEntity> getListByOrganizeId(String organizeId, String keyword) {
        List<String> userIds = userRelationService.getListByObjectId(organizeId, PermissionConst.ORGANIZE).stream()
                .map(UserRelationEntity::getUserId).collect(Collectors.toList());
        QueryWrapper<UserEntity> query = new QueryWrapper<>();
        if(userIds.size() > 0){
            query.lambda().in(UserEntity::getId, userIds);
            // 通过关键字查询
            if (StringUtil.isNotEmpty(keyword)) {
                query.lambda().and(
                        t -> t.like(UserEntity::getAccount, keyword)
                                .or().like(UserEntity::getRealName, keyword)
                );
            }
            // 只查询正常的用户
            query.lambda().eq(UserEntity::getEnabledMark, 1);
            query.lambda().orderByAsc(UserEntity::getSortCode).orderByDesc(UserEntity::getCreatorTime);
            return this.list(query);
        }
        return new ArrayList<>();
    }

    @Override
    public List<UserEntity> getListByManagerId(String managerId, String keyword) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getManagerId, managerId);
        // 通过关键字查询
        if (StringUtil.isNotEmpty(keyword)) {
            queryWrapper.lambda().and(
                    t -> t.like(UserEntity::getAccount, keyword)
                            .or().like(UserEntity::getRealName, keyword)
            );
        }
        // 只查询正常的用户
        queryWrapper.lambda().eq(UserEntity::getEnabledMark, 1);
        queryWrapper.lambda().orderByAsc(UserEntity::getSortCode).orderByDesc(UserEntity::getCreatorTime);
        return this.list(queryWrapper);
    }

    @Override
    public UserEntity getInfo(String id) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getId, String.valueOf(id));
        return this.getOne(queryWrapper);
    }

    @Override
    public UserEntity getUserByAccount(String account) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getAccount, account);
        return this.getOne(queryWrapper);
    }

    @Override
    public UserEntity getUserByMobile(String mobile) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getMobilePhone, mobile);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean isExistByAccount(String account) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getAccount, account);
        UserEntity entity = this.getOne(queryWrapper);
        if (entity != null) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean create(UserEntity entity) throws Exception {
        if (StringUtil.isNotEmpty(entity.getGroupId()) && entity.getGroupId().contains(",")){
            entity.setGroupId(null);
        }
        if (StringUtil.isEmpty(entity.getAccount())) {
            throw new DataException("账号不能为空");
        }
        if (StringUtil.isEmpty(entity.getRealName())) {
            throw new DataException("姓名不能为空");
        }
        //获取头像
        String oldHeadIcon = entity.getHeadIcon();
        if (StringUtil.isEmpty(oldHeadIcon)) {
            entity.setHeadIcon("001.png");
        } else {
            //获取头像
            String[] headIcon = oldHeadIcon.split("/");
            if (headIcon.length > 0) {
                entity.setHeadIcon(headIcon[headIcon.length - 1]);
            }
        }
        //添加用户 初始化
        String userId = RandomUtil.uuId();
        entity.setId(userId);
        entity.setSecretkey(RandomUtil.uuId());
        entity.setPassword("4a7d1ed414474e4033ac29ccb8653d9b");
        entity.setPassword(Md5Util.getStringMd5(entity.getPassword().toLowerCase() + entity.getSecretkey().toLowerCase()));
        entity.setIsAdministrator(0);
        entity.setCreatorUserId(UserProvider.getLoginUserId());
        saveOrUpdateCommon(userId, entity);
        this.save(entity);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(String userId, UserEntity entity) throws Exception {
        if (StringUtil.isEmpty(entity.getAccount())) {
            throw new DataException("账号不能为空");
        }
        if (StringUtil.isEmpty(entity.getRealName())) {
            throw new DataException("姓名不能为空");
        }
        //获取头像
        String oldHeadIcon = entity.getHeadIcon();
        if (StringUtil.isEmpty(oldHeadIcon)) {
            entity.setHeadIcon("001.png");
        }
        //更新用户
        entity.setId(userId);
        entity.setLastModifyTime(DateUtil.getNowDate());
        entity.setLastModifyUserId(UserProvider.getLoginUserId());
        //获取头像
        String headIcon = entity.getHeadIcon();
        if (StringUtil.isNotEmpty(headIcon)) {
            String[] headIcons = entity.getHeadIcon().split("/");
            if (headIcons.length > 0) {
                headIcon = headIcons[headIcons.length - 1];
            }
        }
        saveOrUpdateCommon(userId, entity);
        if (StringUtil.isNotEmpty(entity.getGroupId()) && entity.getGroupId().contains(",")){
            entity.setGroupId(null);
        }
        this.updateById(entity);
        return true;
    }

    private Boolean saveOrUpdateCommon(String userId, UserEntity entity){
        List<String> userAllOrgIds = Arrays.asList(entity.getOrganizeId().split(","));
        List<String> userAllPosIds = StringUtil.isNotEmpty(entity.getPositionId()) ? Arrays.asList(entity.getPositionId().split(",")) : new ArrayList<>();
        List<String> userAllRoleIds = StringUtil.isNotEmpty(entity.getRoleId()) ? Arrays.asList(entity.getRoleId().split(",")) : new ArrayList<>();

        // 更新用户关系（组织/岗位/角色）
        List<UserRelationEntity> relationList = new ArrayList<>();
        setUserRelation(relationList, PermissionConst.ORGANIZE, userAllOrgIds, entity);
        setUserRelation(relationList, PermissionConst.POSITION, userAllPosIds, entity);
        setUserRelation(relationList, PermissionConst.ROLE, userAllRoleIds, entity);
        if(userId != null) {
            // 删除用户关联
            userRelationService.deleteAllByUserId(userId);
        }
        if (relationList.size() > 0) {
            userRelationService.saveBatch(relationList);
        }

        /*========== 自动设置带有权限的默认组织、自动设置默认岗位 ==========*/
        String majorOrgId = "";
        String majorPosId = "0";
        UserEntity userEntity = this.getInfo(userId);
        if(userEntity != null){
            // 原本的主岗、主组织
            majorOrgId = userEntity.getOrganizeId();
            majorPosId = userEntity.getPositionId();
        }
        majorOrgId = organizeRelationService.autoGetMajorOrganizeId(userId, userAllOrgIds, majorOrgId, null);
        entity.setOrganizeId(majorOrgId);
        if(userAllPosIds.size() > 0){
            entity.setPositionId(organizeRelationService.autoGetMajorPositionId(userId, majorOrgId, majorPosId));
        }else {
            entity.setPositionId("");
        }
        entity.setQuickQuery(PinYinUtil.getFirstSpell(entity.getRealName()));
        //清理获取所有用户的redis缓存
        redisUtil.remove(cacheKeyUtil.getAllUser());
        return true;
    }

    /**
     * 设置用户关联对象
     */
    private void setUserRelation(List<UserRelationEntity> relationList, String objectType, List<String> ids, UserEntity userEntity){
        for (String id : ids) {
            UserRelationEntity relationEntity = new UserRelationEntity();
            relationEntity.setId(RandomUtil.uuId());
            relationEntity.setObjectType(objectType);
            relationEntity.setObjectId(id);
            relationEntity.setUserId(userEntity.getId());
            relationEntity.setCreatorTime(userEntity.getCreatorTime());
            relationEntity.setCreatorUserId(userEntity.getCreatorUserId());
            relationList.add(relationEntity);
        }
    }



    @Override
    @DSTransactional
    public void delete(UserEntity entity) {
        this.removeById(entity.getId());
        //删除用户关联
        QueryWrapper<UserRelationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserRelationEntity::getUserId, entity.getId());
        userRelationService.remove(queryWrapper);
    }


    @Override
    public void updatePassword(UserEntity entity) {
        entity.setSecretkey(RandomUtil.uuId());
        entity.setPassword(Md5Util.getStringMd5(entity.getPassword().toLowerCase() + entity.getSecretkey().toLowerCase()));
        entity.setChangePasswordDate(DateUtil.getNowDate());
        this.updateById(entity);

        //加入到旧密码记录表
        UserOldPasswordEntity userOldPasswordEntity = new UserOldPasswordEntity();
        userOldPasswordEntity.setOldPassword(entity.getPassword());
        userOldPasswordEntity.setSecretkey(entity.getSecretkey());
        userOldPasswordEntity.setUserId(entity.getId());
        userOldPasswordEntity.setAccount(entity.getAccount());
        userOldPasswordService.create(userOldPasswordEntity);
    }

    @Override
    public List<UserEntity> getUserName(List<String> id) {
        return getUserName(id, false);
    }


    /**
     * 查询用户名称
     *
     * @param id 主键值
     * @return
     */
    @Override
    public List<UserEntity> getUserName(List<String> id, boolean filterEnabledMark) {
        List<UserEntity> list = new ArrayList<>();
        // 达梦数据库无法null值入参
        id.removeAll(Collections.singleton(null));
        if (id.size() > 0) {
            QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(UserEntity::getId, id);
            if (filterEnabledMark) {
                queryWrapper.lambda().ne(UserEntity::getEnabledMark, 0);
            }
            list = this.list(queryWrapper);
        }
        return list;
    }

    @Override
    public List<UserEntity> getUserNames(List<String> id, Pagination pagination, Boolean flag, Boolean enabledMark) {
        List<UserEntity> list = new ArrayList<>();
        id.removeAll(Collections.singleton(null));
        if (id.size() > 0) {
            QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
            if (!StringUtil.isEmpty(pagination.getKeyword())) {
                queryWrapper.lambda().and(
                        t -> t.like(UserEntity::getRealName, pagination.getKeyword())
                                .or().like(UserEntity::getAccount, pagination.getKeyword())
                );
            }
            queryWrapper.lambda().in(UserEntity::getId, id);
            if (flag) {
                queryWrapper.lambda().ne(UserEntity::getId, UserProvider.getLoginUserId());
            }
            if (enabledMark) {
                queryWrapper.lambda().ne(UserEntity::getEnabledMark, 0);
            }
            queryWrapper.lambda().orderByAsc(UserEntity::getSortCode).orderByDesc(UserEntity::getCreatorTime);
//            queryWrapper.lambda().select(UserEntity::getId, UserEntity::getRealName, UserEntity::getAccount,
//                    UserEntity::getGender, UserEntity::getMobilePhone);
            Page<UserEntity> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
            IPage<UserEntity> iPage = this.page(page, queryWrapper);
            return pagination.setData(iPage.getRecords(), iPage.getTotal());
        }
        return pagination.setData(list, list.size());
    }

    @Override
    public List<UserEntity> getUserList(List<String> id) {
        List<UserEntity> list = new ArrayList<>();
        // 达梦数据库无法null值入参
        id.removeAll(Collections.singleton(null));
        if (id.size() > 0) {
            QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(UserEntity::getId, id);
            queryWrapper.lambda().eq(UserEntity::getEnabledMark, 0);
            queryWrapper.lambda().select(UserEntity::getId);
            list = this.list(queryWrapper);
        }
        return list;
    }

//    /**
//     * 有判断redis来获取所有用户信息
//     *
//     * @return
//     */
//    @Override
//    public List<UserAllModel> getAll() {
//        String catchKey = cacheKeyUtil.getAllUser();
//        if (redisUtil.exists(catchKey)) {
//            return JsonUtil.getJsonToList(redisUtil.getString(catchKey).toString(), UserAllModel.class);
//        }
//        List<UserEntity> list = this.getEnableMarkList("1");
//        //获取全部部门信息
//        List<OrganizeEntity> departmentList = organizeService.getList();
//        //获取全部岗位信息
//        List<PositionEntity> positionList = positionService.getList();
//        //获取全部角色信息
//        List<RoleEntity> roleList = roleService.getList();
//        List<UserAllModel> models = JsonUtil.getJsonToList(list, UserAllModel.class);
//        for (UserAllModel model : models) {
//            //部门名称
//            OrganizeEntity deptEntity = departmentList.stream().filter(t -> t.getId().equals(model.getOrganizeId())).findFirst().orElse(new OrganizeEntity());
//            if (StringUtil.isNotEmpty(deptEntity.getFullName())) {
//                model.setDepartment(deptEntity.getFullName());
//                model.setDepartmentId(deptEntity.getId());
//            }
//            //组织名称
//            OrganizeEntity organizeEntity = departmentList.stream().filter(t -> t.getId().equals(String.valueOf(deptEntity.getParentId()))).findFirst().orElse(new OrganizeEntity());
//            if (organizeEntity != null) {
//                model.setOrganizeId(organizeEntity.getId());
//                model.setOrganize(organizeEntity.getFullName());
//            }
//            //岗位名称(多个)
//            if (model.getPositionId() != null) {
//                List<String> positionName = new ArrayList<>();
//                for (String id : model.getPositionId().split(",")) {
//                    String name = positionList.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(new PositionEntity()).getFullName();
//                    if (!StringUtil.isEmpty(name)) {
//                        positionName.add(name);
//                    }
//                }
//                model.setPositionName(String.join(",", positionName));
//            }
//            //角色名称(多个)
//            if (model.getRoleId() != null) {
//                List<String> roleName = new ArrayList<>();
//                for (String id : model.getRoleId().split(",")) {
//                    String name = roleList.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(new RoleEntity()).getFullName();
//                    if (!StringUtil.isEmpty(name)) {
//                        roleName.add(name);
//                    }
//                }
//                model.setRoleName(String.join(",", roleName));
//            }
//            //主管名称
//            String managerName = list.stream().filter(t -> t.getId().equals(model.getManagerId())).findFirst().orElse(new UserEntity()).getRealName();
//            if (StringUtil.isNotEmpty(managerName)) {
//                model.setManagerName(managerName);
//            }
//            model.setHeadIcon(UploaderUtil.uploaderImg(model.getHeadIcon()));
//        }
//        String allUser = JsonUtil.getObjectToString(models);
//        redisUtil.insert(cacheKeyUtil.getAllUser(), allUser, 300);
//        return models;
//    }

//    /**
//     * 直接从数据库获取所有用户信息（不过滤冻结账号）
//     *
//     * @return
//     */
//    @Override
//    public List<UserAllModel> getAll() {
//        String catchKey = cacheKeyUtil.getAllUser();
//        if (redisUtil.exists(catchKey)) {
//            return JsonUtil.getJsonToList(redisUtil.getString(catchKey).toString(), UserAllModel.class);
//        }
//        List<UserEntity> list = this.getEnableMarkList("1");
//        //获取全部部门信息
//        List<OrganizeEntity> departmentList = organizeService.getList();
//        //获取全部岗位信息
//        List<PositionEntity> positionList = positionService.getList();
//        //获取全部角色信息
//        List<RoleEntity> roleList = roleService.getList();
//        List<UserAllModel> models = JsonUtil.getJsonToList(list, UserAllModel.class);
//        for (UserAllModel model : models) {
//            //部门名称
//            OrganizeEntity deptEntity = departmentList.stream().filter(t -> t.getId().equals(model.getOrganizeId())).findFirst().orElse(new OrganizeEntity());
//            if (StringUtil.isNotEmpty(deptEntity.getFullName())) {
//                model.setDepartment(deptEntity.getFullName());
//                model.setDepartmentId(deptEntity.getId());
//            }
//            //组织名称
//            OrganizeEntity organizeEntity = departmentList.stream().filter(t -> t.getId().equals(String.valueOf(deptEntity.getParentId()))).findFirst().orElse(new OrganizeEntity());
//            if (organizeEntity != null) {
//                model.setOrganizeId(organizeEntity.getId());
//                model.setOrganize(organizeEntity.getFullName());
//            }
//            //岗位名称(多个)
//            if (model.getPositionId() != null) {
//                List<String> positionName = new ArrayList<>();
//                for (String id : model.getPositionId().split(",")) {
//                    String name = positionList.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(new PositionEntity()).getFullName();
//                    if (!StringUtil.isEmpty(name)) {
//                        positionName.add(name);
//                    }
//                }
//                model.setPositionName(String.join(",", positionName));
//            }
//            //角色名称(多个)
//            if (model.getRoleId() != null) {
//                List<String> roleName = new ArrayList<>();
//                for (String id : model.getRoleId().split(",")) {
//                    String name = roleList.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(new RoleEntity()).getFullName();
//                    if (!StringUtil.isEmpty(name)) {
//                        roleName.add(name);
//                    }
//                }
//                model.setRoleName(String.join(",", roleName));
//            }
//            //主管名称
//            String managerName = list.stream().filter(t -> t.getId().equals(model.getManagerId())).findFirst().orElse(new UserEntity()).getRealName();
//            if (StringUtil.isNotEmpty(managerName)) {
//                model.setManagerName(managerName);
//            }
//            model.setHeadIcon(UploaderUtil.uploaderImg(model.getHeadIcon()));
//        }
//        String allUser = JsonUtil.getObjectToString(models);
//        redisUtil.insert(cacheKeyUtil.getAllUser(), allUser, 300);
//        return models;
//    }

//    /**
//     * 直接从数据库获取所有用户信息（不过滤冻结账号）
//     *
//     * @return
//     */
//    @Override
//    public List<UserAllModel> getDbUserAll() {
//        List<UserEntity> list = this.getList();
//        //获取全部部门信息
//        List<OrganizeEntity> departmentList = organizeService.getList();
//        //获取全部岗位信息
//        List<PositionEntity> positionList = positionService.getList();
//        //获取全部角色信息
//        List<RoleEntity> roleList = roleService.getList();
//        List<UserAllModel> models = JsonUtil.getJsonToList(list, UserAllModel.class);
//        for (UserAllModel model : models) {
//            //部门名称
//            OrganizeEntity organize = departmentList.stream().filter(t -> t.getId().equals(model.getOrganizeId())).findFirst().orElse(new OrganizeEntity());
//            if (StringUtil.isNotEmpty(organize.getFullName())) {
//                model.setDepartment(organize.getFullName());
//            }
//            //组织名称
//            String organizeName = departmentList.stream().filter(t -> t.getId().equals(String.valueOf(organize.getParentId()))).findFirst().orElse(new OrganizeEntity()).getFullName();
//            if (StringUtil.isNotEmpty(organizeName)) {
//                model.setOrganize(organizeName);
//            }
//            //岗位名称(多个)
//            if (model.getPositionId() != null) {
//                List<String> positionName = new ArrayList<>();
//                for (String id : model.getPositionId().split(",")) {
//                    String name = positionList.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(new PositionEntity()).getFullName();
//                    positionName.add(name);
//                }
//                model.setPositionName(String.join(",", positionName));
//            }
//            //角色名称(多个)
//            if (model.getRoleId() != null) {
//                List<String> roleName = new ArrayList<>();
//                for (String id : model.getRoleId().split(",")) {
//                    String name = roleList.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(new RoleEntity()).getFullName();
//                    roleName.add(name);
//                }
//                model.setRoleName(String.join(",", roleName));
//            }
//            //主管名称
//            String managerName = list.stream().filter(t -> t.getId().equals(model.getManagerId())).findFirst().orElse(new UserEntity()).getRealName();
//            if (StringUtil.isNotEmpty(managerName)) {
//                model.setManagerName(managerName);
//            }
//            model.setHeadIcon(UploaderUtil.uploaderImg(model.getHeadIcon()));
//        }
//        return models;
//    }

    @Override
    public UserEntity getUserEntity(String account) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getAccount, account);
        return this.baseMapper.selectOne(queryWrapper);
    }

    @Override
    public List<String> getListId() {
        return this.baseMapper.getListId();
    }

    @Override
    public void update(UserEntity entity, String type) {
        UpdateWrapper<UserEntity> wrapper = new UpdateWrapper<>();
        if ("Position".equals(type)) {
            wrapper.lambda().set(UserEntity::getPositionId, entity.getPositionId());
        } else {
            wrapper.lambda().set(UserEntity::getRoleId, entity.getRoleId());
        }
        wrapper.lambda().eq(UserEntity::getId, entity.getId());
        this.update(wrapper);
    }

    @Override
    public void updateLastTime(UserEntity entity, String type) {
        UpdateWrapper<UserEntity> wrapper = new UpdateWrapper<>();
        if ("Position".equals(type)) {
            wrapper.lambda().set(UserEntity::getPositionId, entity.getPositionId());
        } else {
            wrapper.lambda().set(UserEntity::getRoleId, entity.getRoleId());
        }
        wrapper.lambda().set(UserEntity::getLastModifyTime, new Date());
        wrapper.lambda().set(UserEntity::getLastModifyUserId, entity.getLastModifyUserId());
        wrapper.lambda().eq(UserEntity::getId, entity.getId());
        this.update(wrapper);
    }

    @Override
    public boolean isSubordinate(String id, String managerId) {
        int num = 0;
        return recursionSubordinates(id, managerId, num);
    }

    @Override
    public DownloadVO exportExcel(String dataType, String selectKey, PaginationUser pagination) {
        List<UserEntity> entityList = new ArrayList<>();
        if ("0".equals(dataType)) {
            entityList = getList(pagination, pagination.getOrganizeId(), false, true, null, null);
        } else if ("1".equals(dataType)) {
            entityList = getList(false);
        }
        List<UserExportVO> modeList = new ArrayList<>();
        Map<String, OrganizeEntity> orgMaps = null;
        // 长度超过300代表是全部数据
        if (entityList.size() > 300) {
            orgMaps = organizeService.getOrgMaps(null, true, null);
        }
        // 得到民族集合
        List<DictionaryDataEntity> dataServiceList = dictionaryDataService.getListByTypeDataCode("Nation");
        Map<String, String> dataServiceMap = dataServiceList.stream().filter(t -> ObjectUtil.equal(t.getEnabledMark(), 1)).collect(Collectors.toMap(DictionaryDataEntity::getId, DictionaryDataEntity::getFullName));
        // 得到证件类型
        List<DictionaryDataEntity> dataServiceList1 = dictionaryDataService.getListByTypeDataCode("certificateType");
        Map<String, String> dataServiceMap1 = dataServiceList1.stream().filter(t -> ObjectUtil.equal(t.getEnabledMark(), 1)).collect(Collectors.toMap(DictionaryDataEntity::getId, DictionaryDataEntity::getFullName));
        // 得到文化程度
        List<DictionaryDataEntity> dataServiceList2 = dictionaryDataService.getListByTypeDataCode("Education");
        Map<String, String> dataServiceMap2 = dataServiceList2.stream().filter(t -> ObjectUtil.equal(t.getEnabledMark(), 1)).collect(Collectors.toMap(DictionaryDataEntity::getId, DictionaryDataEntity::getFullName));
        // 得到职级
        List<DictionaryDataEntity> dataServiceList3 = dictionaryDataService.getListByTypeDataCode("Rank");
        Map<String, String> dataServiceMap3 = dataServiceList3.stream().filter(t -> ObjectUtil.equal(t.getEnabledMark(), 1)).collect(Collectors.toMap(DictionaryDataEntity::getId, DictionaryDataEntity::getFullName));
        // 得到性别
        List<DictionaryDataEntity> dataServiceList4 = dictionaryDataService.getListByTypeDataCode("sex");
        Map<String, String> dataServiceMap4 = dataServiceList4.stream().filter(t -> ObjectUtil.equal(t.getEnabledMark(), 1)).collect(Collectors.toMap(DictionaryDataEntity::getEnCode, DictionaryDataEntity::getFullName));
        for (UserEntity entity : entityList) {
            UserExportVO model = new UserExportVO();
            model.setAccount(entity.getAccount());
            model.setRealName(entity.getRealName());
            // 组织
            // 定义多组织集合
            StringJoiner stringJoiner = new StringJoiner(";");
            // 获取该用户的所有组织关系
            List<UserRelationEntity> allOrgRelationByUserId = userRelationService.getAllOrgRelationByUserId(entity.getId());
            Map<String, String> orgIdNameMaps = organizeService.getInfoList();
            for (UserRelationEntity userRelationEntity : allOrgRelationByUserId) {
                String id = userRelationEntity.getObjectId();
                OrganizeEntity organize = null;
                // 得到该组织信息
                if (orgMaps != null) {
                    organize = orgMaps.get(id);
                } else {
                    organize = organizeService.getInfo(id);
                }
                // 得到父级id树
                if (organize != null && ObjectUtil.equal(organize.getEnabledMark(), 1) && StringUtil.isNotEmpty(organize.getOrganizeIdTree())) {
                    stringJoiner.add(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, organize.getOrganizeIdTree(), "/"));
                }
            }
            model.setOrganizeId(stringJoiner.toString());
            // 主管
            UserEntity info = getInfo(entity.getManagerId());
            if (Objects.nonNull(info) && StringUtil.isNotEmpty(info.getRealName()) && StringUtil.isNotEmpty(info.getAccount())) {
                model.setManagerId(info.getRealName() + "/" + info.getAccount());
            }
            // 岗位
            List<UserRelationEntity> listByObjectType = userRelationService.getListByObjectType(entity.getId(), PermissionConst.POSITION);
            StringBuffer positionName = new StringBuffer();
            for (UserRelationEntity userRelationEntity : listByObjectType) {
                if (StringUtil.isNotEmpty(userRelationEntity.getObjectId())) {
                    PositionEntity positionEntity = positionService.getInfo(userRelationEntity.getObjectId());
                    if (Objects.nonNull(positionEntity) && ObjectUtil.equal(positionEntity, 1)) {
                        positionName.append("," + positionEntity.getFullName() + "/" + positionEntity.getEnCode());
                    }
                }
            }
            // 判断岗位是否需要导出
            if (positionName.length() > 0) {
                model.setPositionId(positionName.toString().replaceFirst(",", ""));
            }

            // 角色
            List<UserRelationEntity> listByObjectType1 = userRelationService.getListByObjectType(entity.getId(), PermissionConst.ROLE);
            StringBuffer roleName = new StringBuffer();
            for (UserRelationEntity userRelationEntity : listByObjectType1) {
                if (StringUtil.isNotEmpty(userRelationEntity.getObjectId())) {
                    RoleEntity roleEntity = roleService.getInfo(userRelationEntity.getObjectId());
                    if (Objects.nonNull(roleEntity) && ObjectUtil.equal(roleEntity.getEnabledMark(), 1)) {
                        roleName.append("," + roleEntity.getFullName());
                    }
                }
            }
            if (roleName.length() > 0) {
                model.setRoleId(roleName.toString().replaceFirst(",", ""));
            }

            model.setDescription(entity.getDescription());
            // 性别
            if (dataServiceMap4.containsKey(entity.getGender())) {
                model.setGender(dataServiceMap4.get(entity.getGender()));
            }
            // 民族
            if (dataServiceMap.containsKey(entity.getNation())) {
                model.setNation(dataServiceMap.get(entity.getNation()));
            }
            model.setNativePlace(entity.getNativePlace());
            // 证件类型
            if (dataServiceMap1.containsKey(entity.getCertificatesType())) {
                model.setCertificatesType(dataServiceMap1.get(entity.getCertificatesType()));
            }
            model.setCertificatesNumber(entity.getCertificatesNumber());
            // 文化程度
            if (dataServiceMap2.containsKey(entity.getEducation())) {
                dataServiceMap2.get(entity.getEducation());
            }
            // 生日
            SimpleDateFormat sf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (entity.getBirthday() != null) {
                String birthday = sf1.format(entity.getBirthday());
                model.setBirthday(birthday);
            }
            model.setTelePhone(entity.getTelePhone());
            model.setLandline(entity.getLandline());
            model.setMobilePhone(entity.getMobilePhone());
            model.setEmail(entity.getEmail());
            model.setUrgentContacts(entity.getUrgentContacts());
            model.setUrgentTelePhone(entity.getUrgentTelePhone());
            model.setPostalAddress(entity.getPostalAddress());
            model.setSortCode(entity.getSortCode() == null ? 0 : entity.getSortCode());
            // 设置状态
            if (entity.getEnabledMark() == null) {
                model.setEnabledMark("禁用");
            } else {
                if (entity.getEnabledMark() == 2) {
                    model.setEnabledMark("锁定");
                } else if (entity.getEnabledMark() == 1) {
                    model.setEnabledMark("正常");
                } else {
                    model.setEnabledMark("禁用");
                }
            }
            // 入职时间
            if (entity.getEntryDate() != null) {
                String entryDate = sf1.format(entity.getEntryDate());
                model.setEntryDate(entryDate);
            }
            // 职级
            if (dataServiceMap3.containsKey(entity.getRanks())) {
                model.setRanks(dataServiceMap3.get(entity.getRanks()));
            }
            modeList.add(model);
        }
        return exportUtil(selectKey, "用户信息", modeList, 0);
    }

    private DownloadVO exportUtil(String selectKey, String explain, List modeList, int type) {
        List list = JsonUtil.listToJsonField(JsonUtil.getJsonToList(modeList, UserExportVO.class));
        if (type == 1) {
            list = JsonUtil.listToJsonField(JsonUtil.getJsonToList(modeList, UserExportExceptionVO.class));
        }
        List<ExcelExportEntity> entitys = new ArrayList<>();
        String[] splitData = selectKey.split(",");
        if (splitData.length > 0) {
            for (int i = 0; i < splitData.length; i++) {
                if (splitData[i].equals("account")) {
                    entitys.add(new ExcelExportEntity("账号", "account"));
                }
                if (splitData[i].equals("realName")) {
                    entitys.add(new ExcelExportEntity("姓名", "realName"));
                }
                if (splitData[i].equals("gender")) {
                    entitys.add(new ExcelExportEntity("性别", "gender"));
                }
                if (splitData[i].equals("email")) {
                    entitys.add(new ExcelExportEntity("电子邮箱", "email"));
                }
                if (splitData[i].equals("organizeId")) {
                    entitys.add(new ExcelExportEntity("所属组织", "organizeId"));
                }
                if (splitData[i].equals("managerId")) {
                    entitys.add(new ExcelExportEntity("直属主管", "managerId"));
                }
                if (splitData[i].equals("positionId")) {
                    entitys.add(new ExcelExportEntity("岗位", "positionId"));
                }
                if (splitData[i].equals("ranks")) {
                    entitys.add(new ExcelExportEntity("职级", "ranks"));
                }
                if (splitData[i].equals("roleId")) {
                    entitys.add(new ExcelExportEntity("角色", "roleId"));
                }
                if (splitData[i].equals("sortCode")) {
                    entitys.add(new ExcelExportEntity("排序", "sortCode"));
                }
                if (splitData[i].equals("enabledMark")) {
                    entitys.add(new ExcelExportEntity("状态", "enabledMark"));
                }
                if (splitData[i].equals("description")) {
                    entitys.add(new ExcelExportEntity("说明", "description", 25));
                }
                if (splitData[i].equals("nation")) {
                    entitys.add(new ExcelExportEntity("民族", "nation"));
                }
                if (splitData[i].equals("nativePlace")) {
                    entitys.add(new ExcelExportEntity("籍贯", "nativePlace"));
                }
                if (splitData[i].equals("entryDate")) {
                    entitys.add(new ExcelExportEntity("入职时间", "entryDate"));
                }
                if (splitData[i].equals("certificatesType")) {
                    entitys.add(new ExcelExportEntity("证件类型", "certificatesType"));
                }
                if (splitData[i].equals("certificatesNumber")) {
                    entitys.add(new ExcelExportEntity("证件号码", "certificatesNumber"));
                }
                if (splitData[i].equals("education")) {
                    entitys.add(new ExcelExportEntity("文化程度", "education"));
                }
                if (splitData[i].equals("birthday")) {
                    entitys.add(new ExcelExportEntity("出生年月", "birthday"));
                }
                if (splitData[i].equals("telePhone")) {
                    entitys.add(new ExcelExportEntity("办公电话", "telePhone"));
                }
                if (splitData[i].equals("landline")) {
                    entitys.add(new ExcelExportEntity("办公座机", "landline"));
                }
                if (splitData[i].equals("mobilePhone")) {
                    entitys.add(new ExcelExportEntity("手机号码", "mobilePhone"));
                }
                if (splitData[i].equals("urgentContacts")) {
                    entitys.add(new ExcelExportEntity("紧急联系", "urgentContacts"));
                }
                if (splitData[i].equals("urgentTelePhone")) {
                    entitys.add(new ExcelExportEntity("紧急电话", "urgentTelePhone"));
                }
                if (splitData[i].equals("postalAddress")) {
                    entitys.add(new ExcelExportEntity("通讯地址", "postalAddress", 25));
                }
                if (splitData[i].equals("errorsInfo")) {
                    entitys.add(new ExcelExportEntity("异常原因", "errorsInfo", 50));
                }
            }
        }
        ExportParams exportParams = new ExportParams(null, "用户信息");
        exportParams.setType(ExcelType.XSSF);

        DownloadVO vo = DownloadVO.builder().build();
        try {
            @Cleanup Workbook workbook = new HSSFWorkbook();
            if (entitys.size() > 0) {
                workbook = ExcelExportUtil.exportExcel(exportParams, entitys, list);
            }
            String name = explain + DateUtil.dateNow("yyyyMMdd") + "_" + RandomUtil.uuId() + ".xlsx";
            MultipartFile multipartFile = ExcelUtil.workbookToCommonsMultipartFile(workbook, name);
            String temporaryFilePath = configValueUtil.getTemporaryFilePath();
            FileInfo fileInfo = FileUploadUtils.uploadFile(multipartFile, temporaryFilePath, name);
            vo.setName(fileInfo.getFilename());
            vo.setUrl(UploaderUtil.uploaderFile(fileInfo.getFilename() + "#" + "Temporary") + "&name=" + name);
        } catch (Exception e) {
            log.error("用户信息导出Excel错误:" + e.getMessage());
        }
        return vo;
    }

    @Override
    public Map<String, Object> importPreview(List<UserExportVO> personList) {
        List<Map<String, Object>> dataRow = new ArrayList<>();
        List<Map<String, Object>> columns = new ArrayList<>();
        for (int i = 0; i < personList.size(); i++) {
            Map<String, Object> dataRowMap = new HashMap<>();
            UserExportVO model = personList.get(i);
            dataRowMap.put("account", model.getAccount());
            dataRowMap.put("realName", model.getRealName());
            dataRowMap.put("organizeId", model.getOrganizeId());
            dataRowMap.put("managerId", model.getManagerId());
            dataRowMap.put("positionId", model.getPositionId());
            dataRowMap.put("roleId", model.getRoleId());
            dataRowMap.put("description", model.getDescription());
            dataRowMap.put("gender", model.getGender());
            dataRowMap.put("nation", model.getNation());
            dataRowMap.put("nativePlace", model.getNativePlace());
            dataRowMap.put("certificatesType", model.getCertificatesType());
            dataRowMap.put("certificatesNumber", model.getCertificatesNumber());
            dataRowMap.put("education", model.getEducation());
            dataRowMap.put("birthday", model.getBirthday());
            dataRowMap.put("telePhone", model.getTelePhone());
            dataRowMap.put("landline", model.getLandline());
            dataRowMap.put("mobilePhone", model.getMobilePhone());
            dataRowMap.put("email", model.getEmail());
            dataRowMap.put("urgentContacts", model.getUrgentContacts());
            dataRowMap.put("urgentTelePhone", model.getUrgentTelePhone());
            dataRowMap.put("postalAddress", model.getPostalAddress());
            dataRowMap.put("sortCode", model.getSortCode());
            dataRowMap.put("enabledMark", model.getEnabledMark());
            dataRowMap.put("entryDate", model.getEntryDate());
            dataRowMap.put("ranks", model.getRanks());
            dataRow.add(dataRowMap);
        }
        for (int i = 1; i <= personList.size(); i++) {
            Map<String, Object> columnsMap = new HashMap<>();
            columnsMap.put("AllowDBNull", true);
            columnsMap.put("AutoIncrement", false);
            columnsMap.put("AutoIncrementSeed", 0);
            columnsMap.put("AutoIncrementStep", 1);
            columnsMap.put("Caption", this.getColumns(i));
            columnsMap.put("ColumnMapping", 1);
            columnsMap.put("ColumnName", this.getColumns(i));
            columnsMap.put("Container", null);
            columnsMap.put("DataType", "System.String, mscorlib, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089");
            columnsMap.put("DateTimeMode", 3);
            columnsMap.put("DefaultValue", null);
            columnsMap.put("DesignMode", false);
            columnsMap.put("Expression", "");
            columnsMap.put("ExtendedProperties", "");
            columnsMap.put("MaxLength", -1);
            columnsMap.put("Namespace", "");
            columnsMap.put("Ordinal", 0);
            columnsMap.put("Prefix", "");
            columnsMap.put("ReadOnly", false);
            columnsMap.put("Site", null);
            columnsMap.put("Table", personList);
            columnsMap.put("Unique", false);
            columns.add(columnsMap);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("dataRow", dataRow);
        map.put("columns", columns);
        return map;
    }

    @Override
    public UserImportVO importData(List<UserExportVO> dataList) {
//        List<UserImportModel> importModels = new ArrayList<>(16);
        List<UserExportExceptionVO> exceptionList = new ArrayList<>(16);
        // 得到民族集合
        List<DictionaryDataEntity> dataServiceList = dictionaryDataService.getListByTypeDataCode("Nation");
        BiMap<String, String> dataServiceMap = HashBiMap.create(dataServiceList.stream().collect(Collectors.toMap(DictionaryDataEntity::getId, DictionaryDataEntity::getFullName)));
        // 得到证件类型
        List<DictionaryDataEntity> dataServiceList1 = dictionaryDataService.getListByTypeDataCode("certificateType");
        BiMap<String, String> dataServiceMap1 = HashBiMap.create(dataServiceList1.stream().collect(Collectors.toMap(DictionaryDataEntity::getId, DictionaryDataEntity::getFullName)));
        // 得到文化程度
        List<DictionaryDataEntity> dataServiceList2 = dictionaryDataService.getListByTypeDataCode("Education");
        BiMap<String, String> dataServiceMap2 = HashBiMap.create(dataServiceList2.stream().collect(Collectors.toMap(DictionaryDataEntity::getId, DictionaryDataEntity::getFullName)));
        // 得到职级
        List<DictionaryDataEntity> dataServiceList3 = dictionaryDataService.getListByTypeDataCode("Rank");
        BiMap<String, String> dataServiceMap3 = HashBiMap.create(dataServiceList3.stream().collect(Collectors.toMap(DictionaryDataEntity::getId, DictionaryDataEntity::getFullName)));
        // 得到性别
        List<DictionaryDataEntity> dataServiceList4 = dictionaryDataService.getListByTypeDataCode("sex");
        BiMap<String, String> dataServiceMap4 = HashBiMap.create(dataServiceList4.stream().collect(Collectors.toMap(DictionaryDataEntity::getEnCode, DictionaryDataEntity::getFullName)));
//        // 去除重复的account
//        Map<String, Long> collect = dataList.stream().filter(t -> StringUtil.isNotBlank(t.getAccount())).collect(Collectors.groupingBy(t -> t.getAccount(), Collectors.counting()));
//        List<String> collect1 = collect.entrySet().stream().filter(entry -> entry.getValue() > 1).map(entry -> entry.getKey()).collect(Collectors.toList());
//        for (String account : collect1) {
//            List<UserExportVO> collect2 = dataList.stream().filter(t -> account.equals(t.getAccount())).collect(Collectors.toList());
//            dataList.removeAll(collect2);
//            exceptionList.addAll(collect2);
//        }
//        Map<String, UserExportVO> userExportVOMap = dataList.stream().collect(Collectors.toMap(UserExportVO::getAccount, Function.identity()));

        //记录成功了几条
        int sum = 0;
        //记录第几条失败
        int num = 0;
        for (UserExportVO exportVO : dataList) {
            UserImportModel model = new UserImportModel();
            UserExportExceptionVO exceptionVO = JsonUtil.getJsonToBean(exportVO, UserExportExceptionVO.class);
            StringJoiner exceptionMsg = new StringJoiner("；");
            // 处理账号
            if (StringUtil.isNotEmpty(exportVO.getAccount())) {
                UserEntity userByAccount = getUserByAccount(exportVO.getAccount());
                if (Objects.nonNull(userByAccount)) {
                    // 账号重复
                    exceptionMsg.add("账号已存在");
                }
                String regex = "^[a-z0-9A-Z\u4e00-\u9fa5]+$";
                if (!exportVO.getAccount().matches(regex)) {
                    // 账号重复
                    exceptionMsg.add("账户不能含有特殊符号");
                }
                model.setAccount(exportVO.getAccount());
            } else {
                // 账号为空
                exceptionMsg.add("账号不能为空");
            }
            // 处理姓名
            if (StringUtil.isEmpty(exportVO.getRealName())) {
                // 姓名为空
                exceptionMsg.add("姓名不能为空");
            }
            model.setRealName(exportVO.getRealName());
            // 处理组织id
            String organizeId = exportVO.getOrganizeId();
            if (StringUtil.isEmpty(organizeId)) {
                // 判断如果所属组织为空，则为错误数据
                exceptionMsg.add("所属组织不能为空");
            } else {
                StringJoiner orgName = new StringJoiner("、");
                // 处理多级组织
                String[] organizeIds = organizeId.split(";");
                // 储存字段
                StringJoiner orgIds = new StringJoiner(",");
                // 处理单个组织
                for (String id : organizeIds) {
                    String[] split = id.split("/");
                    // 定义一个标志，当前部门如果不存在则存到错误集合中
                    if (split.length > 0) {
                        for (int i = 0; i < split.length; i++) {
                            String orgId = split[i];
                            OrganizeEntity organizeEntity = organizeService.getInfoByFullName(orgId);
                            if (organizeEntity != null) {
                                if (i == split.length-1) {
                                    orgIds.add(organizeEntity.getId());
                                }
                            } else {
                                orgName.add(id);
                                break;
                            }
                        }
                    }
                }
                if (orgName.length() > 0) {
                    exceptionMsg.add("找不到该所属组织（" + orgName.toString() + "）");
                } else {
                    model.setOrganizeId(orgIds.toString());
                }
            }
            // 处理性别
            if (StringUtil.isEmpty(exportVO.getGender())) {
                // 性别为必填项，不给默认为错误，不给默认值
                exceptionMsg.add("性别不能为空");
            } else {
                if (dataServiceMap4.containsValue(exportVO.getGender())) {
                    model.setGender(dataServiceMap4.inverse().get(exportVO.getGender()));
                } else {
                    exceptionMsg.add("找不到该性别");
                }
            }
            // 处理主管id
            String managerId = exportVO.getManagerId();
            if (StringUtil.isNotEmpty(managerId)) {
                String[] split1 = managerId.split("/");
                if (split1.length > 0) {
                    String account = split1[split1.length - 1];
                    UserEntity entity = getUserByAccount(account);
                    if (Objects.nonNull(entity) && StringUtil.isNotEmpty(entity.getAccount())) {
                        model.setManagerId(entity.getId());
                    }
                }
            }
            String tmpOrganizeId = StringUtil.isEmpty(model.getOrganizeId()) ? "" : model.getOrganizeId();
            // 处理岗位id
            String positionId = exportVO.getPositionId();
            if (StringUtil.isNotEmpty(positionId)) {
                StringBuilder positionIdBuffer = new StringBuilder();
                String[] positionIds = positionId.split(",");
                for (String id : positionIds) {
                    // 岗位名称+编码
                    String[] positionName = id.split("/");
                    // 无编码无名称代表是无用数据，不予保存
                    if (positionName.length > 1) {
                        // 通过名称和编码获取岗位信息
                        List<PositionEntity> positionEntityList = positionService.getListByFullName(positionName[0], positionName[1]);
                        if (positionEntityList != null && positionEntityList.size() > 0) {
                            PositionEntity positionEntity = positionEntityList.get(0);
                            String[] split = tmpOrganizeId.split(",");
                            boolean flag = false;
                            for (String orgId : split) {
                                List<PositionEntity> list = positionService.getListByOrganizeId(Collections.singletonList(orgId), false);
                                if (list.stream().anyMatch(t -> t.getId().equals(positionEntity.getId()))) {
                                    flag = true;
                                    break;
                                }
                            }
                            if (flag) {
                                positionIdBuffer.append("," + positionEntity.getId());
                            }
                        }
                    }
                }
                model.setPositionId(positionIdBuffer.toString().replaceFirst(",", ""));
            }
            // 处理角色id
            if (StringUtil.isNotEmpty(exportVO.getRoleId())) {
                String[] roleNames = exportVO.getRoleId().split(",");
                StringBuilder roleId = new StringBuilder();
                for (String roleName : roleNames) {
                    RoleEntity roleEntity = roleService.getInfoByFullName(roleName);
                    if (roleEntity == null) {
                        continue;
                    }
                    // 角色不是全局的情况下 需要验证是否跟组织挂钩
                    String[] split = tmpOrganizeId.split(",");
                    boolean flag = false;
                    for (String orgId : split) {
                        if (organizeRelationService.existByRoleIdAndOrgId(roleEntity.getId(), orgId)) {
                            flag = true;
                            break;
                        }
                    }
                    if (Objects.nonNull(roleEntity) && (roleEntity.getGlobalMark() == 1 || flag)) {
                        roleId.append(",").append(roleEntity.getId());
                    }
                }
                model.setRoleId(roleId.toString().replaceFirst(",", ""));
            }
            model.setDescription(exportVO.getDescription());
            // 处理民族
            if (StringUtil.isNotEmpty(exportVO.getNation())) {
                if (dataServiceMap.containsValue(exportVO.getNation())) {
                    model.setNation(dataServiceMap.inverse().get(exportVO.getNation()));
                }
            }
            model.setNativePlace(exportVO.getNativePlace());
            // 处理证件类型
            if (StringUtil.isNotEmpty(exportVO.getCertificatesType())) {
                if (dataServiceMap1.containsValue(exportVO.getCertificatesType())) {
                    model.setCertificatesType(dataServiceMap1.inverse().get(exportVO.getCertificatesType()));
                }
            }
            model.setCertificatesNumber(exportVO.getCertificatesNumber());
            // 处理文化程度
            if (StringUtil.isNotEmpty(exportVO.getEducation())) {
                if (dataServiceMap2.containsValue(exportVO.getEducation())) {
                    model.setEducation(dataServiceMap2.inverse().get(exportVO.getEducation()));
                }
            }
            // 处理生日
            if (StringUtil.isNotEmpty(exportVO.getBirthday())) {
                Date date = DateUtil.stringToDate(exportVO.getBirthday());
                model.setBirthday(date);
            }
            model.setTelePhone(exportVO.getTelePhone());
            model.setMobilePhone(exportVO.getMobilePhone());
            model.setLandline(exportVO.getLandline());
            model.setEmail(exportVO.getEmail());
            model.setUrgentContacts(exportVO.getUrgentContacts());
            model.setUrgentTelePhone(exportVO.getUrgentTelePhone());
            model.setPostalAddress(exportVO.getPostalAddress());
            model.setSortCode(exportVO.getSortCode() == null ? 0 : exportVO.getSortCode());
            // 入职时间
            if (StringUtil.isNotEmpty(exportVO.getEntryDate())) {
                Date date = DateUtil.stringToDate(exportVO.getEntryDate());
                model.setEntryDate(date);
            }
            // 设置状态
            if ("锁定".equals(exportVO.getEnabledMark())) {
                model.setEnabledMark(2);
            } else if ("正常".equals(exportVO.getEnabledMark())) {
                model.setEnabledMark(1);
            } else {
                model.setEnabledMark(0);
            }
            // 处理证件类型
            if (StringUtil.isNotEmpty(exportVO.getRanks())) {
                if (dataServiceMap3.containsValue(exportVO.getRanks())) {
                    model.setRanks(dataServiceMap3.inverse().get(exportVO.getRanks()));
                }
            }
            if (exceptionMsg.length() > 0) {
                exceptionVO.setErrorsInfo(exceptionMsg.toString());
                exceptionList.add(exceptionVO);
                continue;
            }
            UserEntity entitys = JsonUtil.getJsonToBean(model, UserEntity.class);
            entitys.setHeadIcon("001.png");
            entitys.setPassword("4a7d1ed414474e4033ac29ccb8653d9b");
            try {
                create(entitys);
                sum++;
            } catch (Exception e) {
                if (e instanceof DataException) {
                    exceptionVO.setErrorsInfo(e.getMessage());
                } else {
                    exceptionVO.setErrorsInfo("数据有误");
                }
                exceptionList.add(exceptionVO);
                log.error("导入第" + (num + 1) + "条数据失败");
            }
        }
        UserImportVO vo = new UserImportVO();
        vo.setSnum(sum);
        if (exceptionList.size() > 0) {
            vo.setResultType(1);
            vo.setFailResult(exceptionList);
            vo.setFnum(exceptionList.size());
            return vo;
        } else {
            vo.setResultType(0);
            return vo;
        }
    }

    @Override
    public void getOrganizeIdTree(String organizeId, StringBuffer organizeParentIdList) {
        OrganizeEntity entity = organizeService.getInfo(organizeId);
        if (Objects.nonNull(entity) && StringUtil.isNotEmpty(entity.getParentId())) {
            // 记录id
            organizeParentIdList.append(organizeId + ",");
            getOrganizeIdTree(entity.getParentId(), organizeParentIdList);
        }
    }

    @Override
    public DownloadVO exportExceptionData(List<UserExportExceptionVO> dataList) {
        DownloadVO vo = exportUtil("account,realName,gender,email,organizeId,managerId,positionId,roleId,sortCode,enabledMark,description,nation," +
                        "nativePlace,entryDate,certificatesType,certificatesNumber,education,birthday,telePhone,landline,mobilePhone,urgentContacts," +
                        "urgentTelePhone,postalAddress,ranks,errorsInfo"
                , "错误报告", dataList, 1);
        return vo;
    }

    @Override
    public List<UserEntity> getUserName(List<String> id, Pagination pagination) {
        List<UserEntity> list = new ArrayList<>();
        id.removeAll(Collections.singleton(null));
        if (id.size() > 0) {
            QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
            if (!StringUtil.isEmpty(pagination.getKeyword())) {
                queryWrapper.lambda().and(
                        t -> t.like(UserEntity::getRealName, pagination.getKeyword())
                                .or().like(UserEntity::getAccount, pagination.getKeyword())
                );
            }
            queryWrapper.lambda().in(UserEntity::getId, id);
            queryWrapper.lambda().ne(UserEntity::getEnabledMark, 0);
            queryWrapper.lambda().select(UserEntity::getId, UserEntity::getRealName, UserEntity::getAccount,
                    UserEntity::getGender, UserEntity::getHeadIcon, UserEntity::getMobilePhone);
            Page<UserEntity> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
            IPage<UserEntity> iPage = this.page(page, queryWrapper);
            return pagination.setData(iPage.getRecords(), iPage.getTotal());
        }
        return pagination.setData(list, list.size());
    }

    @Override
    public List<UserEntity> getListByRoleId(String roleId) {
        List<UserEntity> list = new ArrayList<>();
        // 根据roleId获取，用户与组织的关联对象集合
        userRelationService.getListByRoleId(roleId).forEach(u->{
            list.add(this.getInfo(u.getUserId()));
        });
        return list;
    }

    @Override
    public List<UserEntity> getListByRoleIds(List<String> roleIds) {
        QueryWrapper<UserRelationEntity> query = new QueryWrapper<>();
        query.lambda().eq(UserRelationEntity::getObjectType, "role").in(UserRelationEntity::getObjectId, roleIds);
        List<UserRelationEntity> list = userRelationService.list(query);
        if(CollectionUtil.isNotEmpty(list)){
            List<String> userIds = list.stream().map(UserRelationEntity::getUserId).collect(Collectors.toList());
            return listByIds(userIds);
        }
        return new ArrayList<>();
    }

    @Override
    public Boolean delCurRoleUser(List<String> objectIdAll) {
        // 判断角色下面的人
        List<String> member = permissionGroupService.list(objectIdAll)
                .stream().filter(t -> StringUtil.isNotEmpty(t.getPermissionMember())).map(PermissionGroupEntity::getPermissionMember).collect(Collectors.toList());
        List<String> userIdList = this.getUserIdList(member, null);
        delCurUser(null, userIdList.stream().toArray(String[]::new));
        return true;
    }

    @Override
    public Boolean delCurUser(String message, String... userIds) {
        List<String> list = Arrays.asList(userIds);
        // 发送消息
        String[] token = OnlineUserProvider.getOnlineUserList().stream()
                .filter(t -> list.contains(t.getUserId()))
                .map(OnlineUserModel::getToken).toArray(String[]::new);
        List<String> tokens = Arrays.stream(token).map(t -> t.contains(AuthConsts.TOKEN_PREFIX) ? t : TOKEN_PREFIX + " " + t).collect(Collectors.toList());
        //清除websocket登录状态
        List<OnlineUserModel> users = OnlineUserProvider.getOnlineUserList().stream().filter(t -> tokens.contains(t.getToken())).collect(Collectors.toList());
        if (!ObjectUtils.isEmpty(users)) {
            for (OnlineUserModel user : users) {
                JSONObject obj = new JSONObject();
                obj.put("method", "logout");
                obj.put("msg", "权限已变更，请重新登录！");
                if(user != null) {
                    OnlineUserProvider.sendMessage(user, obj);
                }
                //先移除对象， 并推送下线信息， 避免网络原因导致就用户未断开 新用户连不上WebSocket
                OnlineUserProvider.removeModel(user);
                //通知所有在线，有用户离线
                for (OnlineUserModel item : OnlineUserProvider.getOnlineUserList().stream().filter(t -> !Objects.equals(user.getUserId(), t.getUserId()) && !Objects.equals(user.getTenantId(),t.getTenantId())).collect(Collectors.toList())) {
                    if (!item.getUserId().equals(user.getUserId())) {
                        JSONObject objs = new JSONObject();
                        objs.put("method", "logout");
                        //推送给前端
                        OnlineUserProvider.sendMessage(item, objs);
                    }
                }
            }
        }
        list.forEach(UserProvider::logoutByUserId);
        return true;
    }

    @Override
    public List<UserEntity> getAdminList() {
        QueryWrapper<UserEntity> query = new QueryWrapper<>();
        query.lambda().eq(UserEntity::getIsAdministrator, 1);
        query.lambda().orderByAsc(UserEntity::getSortCode).orderByDesc(UserEntity::getCreatorTime);
        return list(query);
    }

    @Override
    public Boolean setAdminListByIds(List<String> adminIds) {
        // 将所有的管理员取消
        QueryWrapper<UserEntity> query = new QueryWrapper<>();
        query.lambda().eq(UserEntity::getIsAdministrator, 1);
        // admin不允许移除管理员
        query.lambda().ne(UserEntity::getAccount, ADMIN_KEY);
        List<UserEntity> list1 = this.list(query);
        for (UserEntity entity : list1) {
            entity.setIsAdministrator(0);
            this.updateById(entity);
        }
        // 重新赋值管理员
        List<UserEntity> list = new ArrayList<>();
        adminIds.stream().forEach(adminId -> {
            UserEntity userEntity = new UserEntity();
            userEntity.setId(adminId);
            userEntity.setIsAdministrator(1);
            // admin无需添加
            if (!ADMIN_KEY.equals(userEntity.getAccount())) {
                list.add(userEntity);
            }
        });
        return this.updateBatchById(list);
    }

    @Override
    public List<UserEntity> getList(List<String> orgIdList, String keyword) {
        // 得到用户关系表
        List<UserRelationEntity> listByObjectId = userRelationService.getListByOrgId(orgIdList);
        if (listByObjectId.size() == 0) {
            return new ArrayList<>();
        }
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(UserEntity::getId, listByObjectId.stream().map(UserRelationEntity::getUserId).collect(Collectors.toList())).and(
                t -> t.like(UserEntity::getRealName, keyword)
                        .or().like(UserEntity::getAccount, keyword)
        );
        return this.list(queryWrapper);
    }

    @Override
    public List<UserEntity> getList(Pagination pagination, Boolean filterCurrentUser) {
        // 定义变量判断是否需要使用修改时间倒序
        boolean filterLastTime = false;
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        if (filterCurrentUser) {
            String userId = userProvider.get().getUserId();
            queryWrapper.lambda().ne(UserEntity::getId, userId);
        }
        queryWrapper.lambda().ne(UserEntity::getEnabledMark, 0);
        //关键字（账户、姓名、手机）
        if (!StringUtil.isEmpty(pagination.getKeyword())) {
            filterLastTime = true;
            queryWrapper.lambda().and(
                    t -> t.like(UserEntity::getAccount, pagination.getKeyword())
                            .or().like(UserEntity::getRealName, pagination.getKeyword())
                            .or().like(UserEntity::getMobilePhone, pagination.getKeyword())
            );
        }
        //排序
        queryWrapper.lambda().orderByAsc(UserEntity::getSortCode).orderByDesc(UserEntity::getCreatorTime);
        if (filterLastTime) {
            queryWrapper.lambda().orderByDesc(UserEntity::getLastModifyTime);
        }
        Page<UserEntity> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
        IPage<UserEntity> iPage = this.page(page, queryWrapper);
        return pagination.setData(iPage.getRecords(), iPage.getTotal());
    }

    @Override
    public List<UserByRoleVO> getListByAuthorize(String organizeId, jnpf.base.Page page) {
        List<UserByRoleVO> jsonToList = new ArrayList<>(16);
        List<String> collect0 = organizeAdministratorService.getListByAuthorize().stream().map(OrganizeEntity::getId).collect(Collectors.toList());
        // 有权限的组织
        Map<String, OrganizeEntity> orgMaps = organizeService.getOrganizeName(collect0, null, true, null);
        Map<String, String> orgIdNameMaps = organizeService.getInfoList();
        //判断是否搜索关键字
        if (StringUtil.isNotEmpty(page.getKeyword())) {
            //通过关键字查询
            List<UserEntity> list =  getList(new ArrayList<>(orgMaps.keySet()), page.getKeyword());
            //遍历用户给要返回的值插入值
            for (UserEntity entity : list) {
                UserByRoleVO vo = new UserByRoleVO();
                vo.setHeadIcon(UploaderUtil.uploaderImg(entity.getHeadIcon()));
                vo.setId(entity.getId());
                vo.setFullName(entity.getRealName() + "/" + entity.getAccount());
                vo.setEnabledMark(entity.getEnabledMark());
                vo.setIsLeaf(true);
                vo.setHasChildren(false);
                vo.setIcon("icon-ym icon-ym-tree-user2");
                vo.setType("user");
                vo.setGender(entity.getGender());
                List<UserRelationEntity> listByUserId = userRelationService.getListByUserId(entity.getId()).stream().filter(t -> t != null && PermissionConst.ORGANIZE.equals(t.getObjectType())).collect(Collectors.toList());
                StringBuilder stringBuilder = new StringBuilder();
                listByUserId.forEach(t -> {
                    OrganizeEntity organizeEntity = orgMaps.get(t.getObjectId());
                    if (organizeEntity != null) {
                        String fullNameByOrgIdTree = organizeService.getFullNameByOrgIdTree(orgIdNameMaps, organizeEntity.getOrganizeIdTree(), "/");
                        if (StringUtil.isNotEmpty(fullNameByOrgIdTree)) {
                            stringBuilder.append("," + fullNameByOrgIdTree);
                        }
                    }
                });
                if (stringBuilder.length() > 0) {
                    vo.setOrganize(stringBuilder.toString().replaceFirst(",", ""));
                }
                jsonToList.add(vo);
            }
            return jsonToList;
        }
        //获取所有组织
        List<OrganizeEntity> collect = new ArrayList<>(orgMaps.values());
        //判断时候传入组织id
        //如果传入组织id，则取出对应的子集
        if (!"0".equals(organizeId)) {
            //通过组织查询部门及人员
            OrganizeEntity organizeEntity = orgMaps.get(organizeId);
            if (organizeEntity != null) {
                // 取出子组织
                List<OrganizeEntity> collect1 = collect.stream().filter(t -> !t.getId().equals(organizeEntity.getId()) && t.getOrganizeIdTree().contains(organizeEntity.getId())).collect(Collectors.toList());
                // 判断组织关系中是否有子部门id
                List<OrganizeEntity> organizeEntities = new ArrayList<>();
                for (OrganizeEntity entity : collect1) {
                    OrganizeEntity organizeEntity1 = orgMaps.get(entity.getId());
                    if (organizeEntity1 != null) {
                        organizeEntities.add(organizeEntity1);
                    }
                }
                // 得到子集的子集
                List<OrganizeEntity> collect2 = collect.stream().filter(t -> t.getOrganizeIdTree().contains(organizeId)).collect(Collectors.toList());
                // 移除掉上级不是同一个的
                List<OrganizeEntity> collect3 = new ArrayList<>();
                collect2.forEach(t -> {
                    organizeEntities.forEach(oe -> {
                        if (!oe.getId().equals(t.getId()) && t.getOrganizeIdTree().contains(oe.getId())) {
                            collect3.add(t);
                        }
                    });
                });
                organizeEntities.removeAll(collect3);

                //取出组织下的人员
                List<UserEntity> entityList = getListByOrganizeId(organizeId, null);
                for (UserEntity entity : entityList) {
                    UserByRoleVO vo = new UserByRoleVO();
                    vo.setId(entity.getId());
                    vo.setHeadIcon(UploaderUtil.uploaderImg(entity.getHeadIcon()));
                    vo.setFullName(entity.getRealName() + "/" + entity.getAccount());
                    vo.setEnabledMark(entity.getEnabledMark());
                    vo.setIsLeaf(true);
                    vo.setHasChildren(false);
                    vo.setIcon("icon-ym icon-ym-tree-user2");
                    vo.setType("user");
                    vo.setGender(entity.getGender());
                    List<UserRelationEntity> listByUserId = userRelationService.getListByUserId(entity.getId()).stream().filter(t -> t != null && PermissionConst.ORGANIZE.equals(t.getObjectType())).collect(Collectors.toList());
                    StringJoiner stringJoiner = new StringJoiner(",");
                    listByUserId.forEach(t -> {
                        OrganizeEntity organizeEntity1 = orgMaps.get(t.getObjectId());
                        if (organizeEntity1 != null) {
                            String fullNameByOrgIdTree = organizeService.getFullNameByOrgIdTree(orgIdNameMaps, organizeEntity1.getOrganizeIdTree(), "/");
                            if (StringUtil.isNotEmpty(fullNameByOrgIdTree)) {
                                stringJoiner.add(fullNameByOrgIdTree);
                            }
                        }
                    });
                    vo.setOrganize(stringJoiner.toString());
                    jsonToList.add(vo);
                }
                for (OrganizeEntity entitys : organizeEntities) {
                    UserByRoleVO vo = new UserByRoleVO();
                    vo.setId(entitys.getId());
                    vo.setType(entitys.getCategory());
                    vo.setFullName(entitys.getFullName());
                    if ("department".equals(entitys.getCategory())) {
                        vo.setIcon("icon-ym icon-ym-tree-department1");
                    } else {
                        vo.setIcon("icon-ym icon-ym-tree-organization3");
                    }
                    vo.setHasChildren(true);
                    vo.setIsLeaf(false);
                    vo.setEnabledMark(entitys.getEnabledMark());
                    if (StringUtil.isNotEmpty(entitys.getOrganizeIdTree())) {
                        String[] split = entitys.getOrganizeIdTree().split(organizeEntity.getId());
                        if (split.length > 1) {
                            vo.setFullName(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, split[1], "/"));
                        }
                    }
                    jsonToList.add(vo);
                }
            }
            return jsonToList;
        }
        List<String> list = new ArrayList<>(16);
        for (OrganizeEntity organizeEntity : collect) {
            if (organizeEntity != null && organizeEntity.getEnabledMark() == 1) {
                UserByRoleVO userByRoleVO = new UserByRoleVO();
                userByRoleVO.setId(organizeEntity.getId());
                userByRoleVO.setType(organizeEntity.getCategory());
                if ("department".equals(organizeEntity.getCategory())) {
                    userByRoleVO.setIcon("icon-ym icon-ym-tree-department1");
                } else {
                    userByRoleVO.setIcon("icon-ym icon-ym-tree-organization3");
                }
                userByRoleVO.setHasChildren(true);
                userByRoleVO.setIsLeaf(false);
                userByRoleVO.setEnabledMark(organizeEntity.getEnabledMark());
                // 处理断层
                if (StringUtil.isNotEmpty(organizeEntity.getOrganizeIdTree())) {
                    List<String> list1 = new ArrayList<>();
                    String[] split = organizeEntity.getOrganizeIdTree().split(",");
                    list1 = Arrays.asList(split);
                    Collections.reverse(list1);
                    for (String orgId : list1) {
                        OrganizeEntity organizeEntity1 = orgMaps.get(orgId);
                        if (organizeEntity1 != null && !organizeEntity1.getId().equals(organizeEntity.getId())) {
                            // 记录id
                            list.add(organizeEntity.getId());
                            break;
                        }
                    }
                }
                if (!list.contains(organizeEntity.getId())) {
                    jsonToList.add(userByRoleVO);
                }
            }
        }
        jsonToList.forEach(t -> {
            OrganizeEntity entity = orgMaps.get(t.getId());
            if (entity != null) {
                t.setFullName(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, entity.getOrganizeIdTree(), "/"));
            }
            t.setParentId(entity.getParentId());
        });
        return jsonToList;
    }

    @Override
    public List<String> getUserIdList(List<String> userIds, String type) {
        Set<String> allUserId = new HashSet<>(userIds);
        String organizeId = UserProvider.getUser().getOrganizeId();
        List<String> newUserIds = new ArrayList<>(userIds);
        newUserIds.forEach(t -> {
            String[] split = t.split(",");
            for (String id : split) {
                allUserId.add(id);
            }
        });
        userIds.forEach(userId -> {
            // 处理系统参数
            if (JnpfConst.SYSTEM_PARAM.containsKey(userId)) {
                if (JnpfConst.CURRENT_GRADE.equals(userId) || JnpfConst.CURRENT_GRADE_TYPE.equals(userId)) {
                    List<String> organizeUserList = organizeAdministratorService.getOrganizeUserList(JnpfConst.CURRENT_ORG_SUB);
                    organizeUserList.forEach(t -> allUserId.add(t + "--" + PermissionConst.COMPANY));
                } else {
                    if (StringUtil.isNotEmpty(organizeId)) {
                        allUserId.add(organizeId + "--" + PermissionConst.COMPANY);
                    }
                    if (JnpfConst.CURRENT_ORG_SUB.equals(userId) || JnpfConst.CURRENT_ORG_SUB_TYPE.equals(userId)) {
                        List<String> underOrganizations = organizeService.getUnderOrganizations(organizeId, true);
                        underOrganizations.add(organizeId);
                        underOrganizations.forEach(t -> allUserId.add(t + "--" + PermissionConst.COMPANY));
                    }
                }
            }
        });
        Set<String> userRelationEntities = new LinkedHashSet<>();
        if (allUserId != null) {
            allUserId.forEach(userId -> {
//                if (StringUtil.isEmpty(type) || PermissionConst.USER.equals(type)) {
                    String[] split = userId.split("--");
                    if (split.length > 1) {
                        String orgType = split[1];
                        List<String> listByObjectId = new ArrayList<>(16);
                        if (PermissionConst.COMPANY.equalsIgnoreCase(orgType) || PermissionConst.DEPARTMENT.equalsIgnoreCase(orgType)) {
//                            // 得到子组织Id
//                            List<String> orgIds = organizeService.getUnderOrganizations(split[0], true);
//                            orgIds.add(split[0]);
                            List<String> orgIds = new ArrayList<>();
                            orgIds.add(split[0]);
                            listByObjectId = userRelationService.getListByOrgId(orgIds).stream().map(UserRelationEntity::getUserId).collect(Collectors.toList());
                        } else if ("user".equalsIgnoreCase(orgType)) {
                            userRelationEntities.add(split[0]);
                        } else {
                            if (PermissionConst.ROLE.equalsIgnoreCase(orgType)) {
                                orgType = PermissionConst.ROLE;
                            } else if (PermissionConst.ORGANIZE.equalsIgnoreCase(orgType)) {
                                orgType = PermissionConst.ORGANIZE;
                            } else if (PermissionConst.POSITION.equalsIgnoreCase(orgType)) {
                                orgType = PermissionConst.POSITION;
                            } else if (PermissionConst.GROUP.equalsIgnoreCase(orgType)) {
                                orgType = PermissionConst.GROUP;
                            }
                            listByObjectId = userRelationService.getListByObjectId(split[0], orgType).stream().map(UserRelationEntity::getUserId).collect(Collectors.toList());
                        }
                        userRelationEntities.addAll(listByObjectId);
                    } else if (split.length > 0) {
                        userRelationEntities.add(split[0]);
                    }
//                } else {
//                        String[] split = userId.split("--");
//                        if (split.length > 1) {
//                            String orgType = split[1];
//                            if (PermissionConst.ROLE.equals(type)) {
//                                if (PermissionConst.COMPANY.equalsIgnoreCase(orgType) || PermissionConst.DEPARTMENT.equalsIgnoreCase(orgType)) {
//                                    // 得到子组织Id
//                                    List<String> orgIds = organizeService.getUnderOrganizations(split[0], true);
//                                    orgIds.add(split[0]);
//                                    List<String> roleIdsByOrgIds = organizeRelationService.getRelationListByOrganizeId(orgIds, type).stream().map(OrganizeRelationEntity::getObjectId).collect(Collectors.toList());
//                                    List<String> roleIds = roleService.getListByIds(roleIdsByOrgIds, null, true).stream().map(RoleEntity::getId).collect(Collectors.toList());
//                                    userRelationEntities.addAll(roleIds);
//                                }
//                            } else if (PermissionConst.GROUP.equals(type)) {
//                                if (PermissionConst.COMPANY.equalsIgnoreCase(orgType) || PermissionConst.DEPARTMENT.equalsIgnoreCase(orgType)) {
//                                    // 得到子组织Id
//                                    List<String> orgIds = organizeService.getUnderOrganizations(split[0], true);
//                                    orgIds.add(split[0]);
//                                    List<String> roleIdsByOrgIds = organizeRelationService.getRelationListByOrganizeId(orgIds, type).stream().map(OrganizeRelationEntity::getObjectId).collect(Collectors.toList());
//                                    List<String> roleIds = groupService.getListByIds(roleIdsByOrgIds, true).stream().map(GroupEntity::getId).collect(Collectors.toList());
//                                    userRelationEntities.addAll(roleIds);
//                                }
//                            }
//                        }
//                }
            });
        }
        return new ArrayList<>(userRelationEntities);
    }

    @Override
    public List<UserIdListVo> getObjList(List<String> userIds, Pagination pagination, String type) {
        List<UserIdListVo> jsonToList = new ArrayList<>();
        List<String> userRelationEntities = getUserIdList(userIds, type);
        if (StringUtil.isEmpty(type) || PermissionConst.USER.equals(type)) {
            //获取所有组织
            Map<String, String> orgIdNameMaps = organizeService.getInfoList();
            // 得到所有的用户id关系
            List<UserEntity> userEntityList = getUserNames(userRelationEntities, pagination, false, true);
            jsonToList = JsonUtil.getJsonToList(userEntityList, UserIdListVo.class);
            jsonToList.forEach(userIdListVo -> {
                List<UserRelationEntity> listByObjectType = userRelationService.getListByObjectType(userIdListVo.getId(), PermissionConst.ORGANIZE);
                StringJoiner orgName = new StringJoiner(",");
                listByObjectType.forEach(userRelationEntity -> {
                    OrganizeEntity info = organizeService.getInfo(userRelationEntity.getObjectId());
                    if (info != null) {
                        String fullNameByOrgIdTree = organizeService.getFullNameByOrgIdTree(orgIdNameMaps, info.getOrganizeIdTree(), "/");
                        orgName.add(fullNameByOrgIdTree);
                    }
                });
                userIdListVo.setOrganize(orgName.toString());
                userIdListVo.setType("user");

                userIdListVo.setFullName(userIdListVo.getRealName() + "/" + userIdListVo.getAccount());
                userIdListVo.setHeadIcon(UploaderUtil.uploaderImg(userIdListVo.getHeadIcon()));
            });
        }
        else if (PermissionConst.ROLE.equals(type))  {
            List<RoleEntity> roleEntityList = roleService.getListByIds(userRelationEntities, null, true);
            jsonToList = JsonUtil.getJsonToList(roleEntityList, UserIdListVo.class);
            jsonToList.forEach(userIdListVo -> {
                userIdListVo.setType("role");
                userIdListVo.setIcon("icon-ym icon-ym-generator-group1");
            });
        } else if (PermissionConst.GROUP.equals(type)) {
            List<GroupEntity> groupEntityList = groupService.getListByIds(userRelationEntities, true);
            jsonToList = JsonUtil.getJsonToList(groupEntityList, UserIdListVo.class);
            jsonToList.forEach(userIdListVo -> {
                userIdListVo.setType("group");
                userIdListVo.setIcon("icon-ym icon-ym-generator-group1");
            });
        }
        return jsonToList;
    }

    private String getColumns(Integer key) {
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "账号");
        map.put(2, "姓名");
        map.put(3, "性别");
        map.put(4, "手机");
        map.put(5, "说明");
        map.put(6, "状态");
        map.put(7, "排序");
        map.put(8, "是否管理员");
        map.put(9, "锁定标志");
        map.put(10, "添加时间");
        map.put(11, "部门");
        return map.get(key);
    }

    /**
     * 判断上级是否直属主管的值是否为我的下属
     *
     * @param id
     * @param managerId
     * @param num
     */
    private boolean recursionSubordinates(String id, String managerId, int num) {
        UserEntity entity = getInfo(managerId);
        num++;
        if (entity != null && entity.getId().equals(id)) {
            return true;
        }
        if (num < 10) {
            if (entity != null) {
                return recursionSubordinates(id, entity.getManagerId(), num);
            }
            return false;
        } else {
            return false;
        }
    }

    @Override
    public String getDefaultCurrentUserId(UserConditionModel userConditionModel) throws DataException {
        UserInfo userInfo = UserProvider.getUser();
        int currentFinded = 0;
        if(userConditionModel.getUserIds() != null && !userConditionModel.getUserIds().isEmpty() && userConditionModel.getUserIds().contains(userInfo.getUserId())) {
            currentFinded = 1;
        }
        if (currentFinded == 0 && userConditionModel.getDepartIds() != null && !userConditionModel.getDepartIds().isEmpty()) {
            List<OrganizeEntity> orgList = organizeService.getOrgEntityList(userConditionModel.getDepartIds(), true);
            List<String> orgLIdList = orgList.stream().map(OrganizeEntity::getId).collect(Collectors.toList());
            if(orgLIdList != null && !orgLIdList.isEmpty()) {
                List<String> userIds = userRelationService.getListByObjectIdAll(orgLIdList).stream().map(UserRelationEntity::getUserId).collect(Collectors.toList());
                if(userIds != null && !userIds.isEmpty() && userIds.contains(userInfo.getUserId())) {
                    currentFinded = 1;
                }
            }
        }
        if (currentFinded == 0 && userConditionModel.getRoleIds() != null && !userConditionModel.getRoleIds().isEmpty()) {
            List<RoleEntity> roleList = roleService.getListByIds(userConditionModel.getRoleIds(), null, false);
            List<String> roleIdList = roleList.stream().filter(t -> t.getEnabledMark() == 1).map(RoleEntity::getId).collect(Collectors.toList());
            if(roleIdList != null && !roleIdList.isEmpty()) {
                List<String> userIds = userRelationService.getListByObjectIdAll(roleIdList).stream().map(UserRelationEntity::getUserId).collect(Collectors.toList());
                if(userIds != null && !userIds.isEmpty() && userIds.contains(userInfo.getUserId())) {
                    currentFinded = 1;
                }
            }
        }
        if (currentFinded == 0 && userConditionModel.getPositionIds() != null && !userConditionModel.getPositionIds().isEmpty()) {
            List<PositionEntity> positionList = positionService.getPosList(userConditionModel.getPositionIds());
            List<String> positionIdList = positionList.stream().filter(t -> t.getEnabledMark() == 1).map(PositionEntity::getId).collect(Collectors.toList());
            if(positionIdList != null && !positionIdList.isEmpty()) {
                List<String> userIds = userRelationService.getListByObjectIdAll(positionIdList).stream().map(UserRelationEntity::getUserId).collect(Collectors.toList());
                if(userIds != null && !userIds.isEmpty() && userIds.contains(userInfo.getUserId())) {
                    currentFinded = 1;
                }
            }
        }
        if (currentFinded == 0 && userConditionModel.getGroupIds() != null && !userConditionModel.getGroupIds().isEmpty()) {
            List<GroupEntity> groupList = groupService.getListByIds(userConditionModel.getGroupIds(), true);
            List<String> groupIdList = groupList.stream().map(GroupEntity::getId).collect(Collectors.toList());
            if(groupIdList != null && !groupIdList.isEmpty()) {
                List<String> userIds = userRelationService.getListByObjectIdAll(groupIdList).stream().map(UserRelationEntity::getUserId).collect(Collectors.toList());
                if(userIds != null && !userIds.isEmpty() && userIds.contains(userInfo.getUserId())) {
                    currentFinded = 1;
                }
            }
        }
        return (currentFinded == 1)?userInfo.getUserId():"";
    }

    @Override
    public List<UserIdListVo> selectedByIds(List<String> ids) {
        List<UserIdListVo> list = new ArrayList<>();
        if (ids != null) {
            //获取所有组织
            Map<String, String> orgIdNameMaps = organizeService.getInfoList();
            ids.forEach(selectedId -> {
                if (StringUtil.isNotEmpty(selectedId)) {
                    // 判断是否为系统参数
                    if (JnpfConst.SYSTEM_PARAM.containsKey(selectedId)) {
                        UserIdListVo vo = new UserIdListVo();
                        vo.setId(selectedId);
                        vo.setFullName(JnpfConst.SYSTEM_PARAM.get(selectedId));
                    }
                    String[] split = selectedId.split("--");
                    // 截取type后获取详情
                    if (split.length > 1) {
                        String type = split[1];
                        if (PermissionConst.COMPANY.equalsIgnoreCase(type) || PermissionConst.DEPARTMENT.equalsIgnoreCase(type)) {
                            OrganizeEntity organizeEntity = organizeService.getInfo(split[0]);
                            if (organizeEntity != null) {
                                UserIdListVo vo = JsonUtil.getJsonToBean(organizeEntity, UserIdListVo.class);
                                if ("department".equals(organizeEntity.getCategory())) {
                                    vo.setIcon("icon-ym icon-ym-tree-department1");
                                } else if ("company".equals(organizeEntity.getCategory())) {
                                    vo.setIcon("icon-ym icon-ym-tree-organization3");
                                }
                                vo.setOrganize(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, organizeEntity.getOrganizeIdTree(), "/"));
                                vo.setOrganizeIds(organizeService.getOrgIdTree(organizeEntity));
                                vo.setType(organizeEntity.getCategory());
                                list.add(vo);
                            }
                        } else if (PermissionConst.ROLE.equalsIgnoreCase(type)) {
                            RoleEntity roleEntity = roleService.getInfo(split[0]);
                            if (roleEntity != null) {
                                UserIdListVo vo = JsonUtil.getJsonToBean(roleEntity, UserIdListVo.class);
                                // 获取角色的所属组织
                                List<OrganizeRelationEntity> relationListByRoleId = organizeRelationService.getRelationListByRoleId(vo.getId());
                                StringJoiner orgName = new StringJoiner(",");
                                relationListByRoleId.forEach(organizeRelationEntity -> {
                                    String organizeId = organizeRelationEntity.getOrganizeId();
                                    OrganizeEntity entity = organizeService.getInfo(organizeId);
                                    if (entity != null) {
                                        String fullNameByOrgIdTree = organizeService.getFullNameByOrgIdTree(orgIdNameMaps, entity.getOrganizeIdTree(), "/");
                                        orgName.add(fullNameByOrgIdTree);
                                    }
                                });
                                vo.setOrganize(orgName.toString());
                                vo.setType("role");
                                vo.setIcon("icon-ym icon-ym-generator-role");
                                list.add(vo);
                            }
                        } else if (PermissionConst.POSITION.equalsIgnoreCase(type)) {
                            PositionEntity positionEntity = positionService.getInfo(split[0]);
                            if (positionEntity != null) {
                                UserIdListVo vo = JsonUtil.getJsonToBean(positionEntity, UserIdListVo.class);
                                OrganizeEntity info = organizeService.getInfo(positionEntity.getOrganizeId());
                                if (info != null) {
                                    vo.setOrganize(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, info.getOrganizeIdTree(), "/"));
                                }
                                vo.setType("position");
                                vo.setIcon("icon-ym icon-ym-tree-position1");
                                list.add(vo);
                            }
                        } else if (PermissionConst.GROUP.equalsIgnoreCase(type)) {
                            GroupEntity groupEntity = groupService.getInfo(split[0]);
                            if (groupEntity != null) {
                                UserIdListVo vo = JsonUtil.getJsonToBean(groupEntity, UserIdListVo.class);
                                vo.setIcon("icon-ym icon-ym-generator-group1");
                                vo.setType("group");
                                list.add(vo);
                            }
                        } else if ("user".equalsIgnoreCase(type)) {
                            UserEntity userEntity = this.getInfo(split[0]);
                            if (userEntity != null) {
                                UserIdListVo vo = JsonUtil.getJsonToBean(userEntity, UserIdListVo.class);
                                List<UserRelationEntity> listByObjectType = userRelationService.getListByObjectType(userEntity.getId(), PermissionConst.ORGANIZE);
                                StringJoiner orgName = new StringJoiner(",");
                                listByObjectType.forEach(userRelationEntity -> {
                                    OrganizeEntity info = organizeService.getInfo(userRelationEntity.getObjectId());
                                    if (info != null) {
                                        String fullNameByOrgIdTree = organizeService.getFullNameByOrgIdTree(orgIdNameMaps, info.getOrganizeIdTree(), "/");
                                        orgName.add(fullNameByOrgIdTree);
                                    }
                                });
                                vo.setOrganize(orgName.toString());
                                vo.setType("user");
                                vo.setHeadIcon(UploaderUtil.uploaderImg(vo.getHeadIcon()));
                                vo.setFullName(vo.getRealName() + "/" + vo.getAccount());
                                list.add(vo);
                            }
                        } else {
                            UserIdListVo vo = new UserIdListVo();
                            vo.setId(split[0]);
                            vo.setFullName(JnpfConst.SYSTEM_PARAM.get(selectedId));
                            vo.setType(split[1]);
                            list.add(vo);
                        }
                    } else {
                        UserEntity userEntity = this.getInfo(split[0]);
                        if (userEntity != null) {
                            UserIdListVo vo = JsonUtil.getJsonToBean(userEntity, UserIdListVo.class);
                            List<UserRelationEntity> listByObjectType = userRelationService.getListByObjectType(userEntity.getId(), PermissionConst.ORGANIZE);
                            StringJoiner orgName = new StringJoiner(",");
                            listByObjectType.forEach(userRelationEntity -> {
                                OrganizeEntity info = organizeService.getInfo(userRelationEntity.getObjectId());
                                if (info != null) {
                                    String fullNameByOrgIdTree = organizeService.getFullNameByOrgIdTree(orgIdNameMaps, info.getOrganizeIdTree(), "/");
                                    orgName.add(fullNameByOrgIdTree);
                                }
                            });
                            vo.setOrganize(orgName.toString());
                            vo.setType("user");
                            vo.setHeadIcon(UploaderUtil.uploaderImg(vo.getHeadIcon()));
                            vo.setFullName(vo.getRealName() + "/" + vo.getAccount());
                            list.add(vo);
                        }
                    }
                }
            });
        }
        return list;
    }

    @Override
    public Boolean updateByIdAndTenantId(UserUpdateModel userUpdateModel) {
        //判断是否为多租户
        if (configValueUtil.isMultiTenancy()) {
            TenantDataSourceUtil.switchTenant(userUpdateModel.getTenantId());
        }
        return updateById(userUpdateModel.getEntity());
    }
    @Override
    public UserEntity getInfoByAccount(String userId, String tenantId) {
        //判断是否为多租户
        if (configValueUtil.isMultiTenancy()) {
            TenantDataSourceUtil.switchTenant(tenantId);
        }
        return getUserByAccount(userId);
    }
    @Override
    public List<String> getFullNameByIds(List<String> ids) {
        List<String> list = new ArrayList<>();
        if (ids != null) {
            ids.forEach(selectedId -> {
                if (StringUtil.isNotEmpty(selectedId)) {
                    String[] split = selectedId.split("--");
                    // 截取type后获取详情
                    if (split.length > 1) {
                        String type = split[1];
                        if (PermissionConst.COMPANY.equalsIgnoreCase(type) || PermissionConst.DEPARTMENT.equalsIgnoreCase(type)) {
                            OrganizeEntity organizeEntity = organizeService.getInfo(split[0]);
                            if (organizeEntity != null) {
                                list.add(organizeEntity.getFullName());
                            }
                        } else if (PermissionConst.ROLE.equalsIgnoreCase(type)) {
                            RoleEntity roleEntity = roleService.getInfo(split[0]);
                            if (roleEntity != null) {
                                list.add(roleEntity.getFullName());
                            }
                        } else if (PermissionConst.POSITION.equalsIgnoreCase(type)) {
                            PositionEntity positionEntity = positionService.getInfo(split[0]);
                            if (positionEntity != null) {
                                list.add(positionEntity.getFullName());
                            }
                        } else if (PermissionConst.GROUP.equalsIgnoreCase(type)) {
                            GroupEntity groupEntity = groupService.getInfo(split[0]);
                            if (groupEntity != null) {
                                list.add(groupEntity.getFullName());
                            }
                        } else if ("user".equalsIgnoreCase(type)) {
                            UserEntity userEntity = this.getInfo(split[0]);
                            if (userEntity != null) {
                                list.add(userEntity.getRealName());
                            }
                        }
                    } else {
                        UserEntity userEntity = this.getInfo(split[0]);
                        if (userEntity != null) {
                            list.add(userEntity.getRealName());
                        }
                    }
                }
            });
        }
        return list;
    }

}
