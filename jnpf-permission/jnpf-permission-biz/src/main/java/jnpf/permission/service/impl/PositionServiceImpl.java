package jnpf.permission.service.impl;

import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jnpf.base.service.SuperServiceImpl;
import jnpf.constant.PermissionConst;
import jnpf.permission.entity.AuthorizeEntity;
import jnpf.permission.entity.OrganizeAdministratorEntity;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.entity.UserRelationEntity;
import jnpf.permission.mapper.PositionMapper;
import jnpf.permission.model.position.PaginationPosition;
import jnpf.permission.service.AuthorizeService;
import jnpf.permission.service.OrganizeAdministratorService;
import jnpf.permission.service.OrganizeRelationService;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.PositionService;
import jnpf.permission.service.UserRelationService;
import jnpf.util.CacheKeyUtil;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.RedisUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 岗位信息
 *
 * @copyright 引迈信息技术有限公司
 * @author JNPF开发平台组
 * @version V3.1.0
 * @date 2019年9月26日 上午9:18
 */
@Service
public class PositionServiceImpl extends SuperServiceImpl<PositionMapper, PositionEntity> implements PositionService {

    @Autowired
    private AuthorizeService authorizeService;
    @Autowired
    private UserRelationService userRelationService;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private CacheKeyUtil cacheKeyUtil;
    @Autowired
    private OrganizeRelationService organizeRelationService;
    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private OrganizeAdministratorService organizeAdministratorService;

    @Override
    public List<PositionEntity> getList(boolean filterEnabledMark) {
        QueryWrapper<PositionEntity> queryWrapper = new QueryWrapper<>();
        if (filterEnabledMark) {
            queryWrapper.lambda().eq(PositionEntity::getEnabledMark, 1);
        }
        queryWrapper.lambda().orderByAsc(PositionEntity::getSortCode).orderByDesc(PositionEntity::getCreatorTime);
        return this.list(queryWrapper);
    }

    @Override
    public List<PositionEntity> getPosList(List<String> idList) {
        if (idList.size()>0){
            QueryWrapper<PositionEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(PositionEntity::getId,idList).select(PositionEntity::getId, PositionEntity::getFullName, PositionEntity::getEnabledMark);
            return this.list(queryWrapper);
        }
        return new ArrayList<>();
    }

    @Override
    public List<PositionEntity> getPosList(Set<String> idList) {
        if (idList.size()>0){
            QueryWrapper<PositionEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().orderByAsc(PositionEntity::getSortCode).orderByDesc(PositionEntity::getCreatorTime);
            queryWrapper.lambda().select(PositionEntity::getId, PositionEntity::getFullName).in(PositionEntity::getId,idList);
            return this.list(queryWrapper);
        }
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getPosMap( String type) {
        return "id-fullName".equals(type) ?  getPosMap() : getPosEncodeAndName();
    }
    @Override
    public Map<String, Object> getPosMap() {
        QueryWrapper<PositionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(PositionEntity::getId,PositionEntity::getFullName);
        return this.list(queryWrapper).stream().collect(Collectors.toMap(PositionEntity::getId,PositionEntity::getFullName));
    }

    @Override
    public Map<String, Object> getPosEncodeAndName() {
        QueryWrapper<PositionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(PositionEntity::getId,PositionEntity::getFullName,PositionEntity::getEnCode);
        return this.list(queryWrapper).stream().collect(Collectors.toMap(p->p.getFullName() + "/" + p.getEnCode(),PositionEntity::getId));
    }

    @Override
    public List<PositionEntity> getPosRedisList() {
        if(redisUtil.exists(cacheKeyUtil.getPositionList())){
            return JsonUtil.getJsonToList(redisUtil.getString(cacheKeyUtil.getPositionList()).toString(), PositionEntity.class);
        }
        QueryWrapper<PositionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(PositionEntity::getEnabledMark,1);

        List<PositionEntity> list=this.list(queryWrapper);
        if(list.size()>0){
            redisUtil.insert(cacheKeyUtil.getPositionList(), JsonUtil.getObjectToString(list),300);
        }
        return list;
    }

    @Override
    public List<PositionEntity> getList(PaginationPosition paginationPosition) {
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
        if (orgId.size() == 0) {
            return new ArrayList<>();
        }
        QueryWrapper<PositionEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtil.isNotEmpty(paginationPosition.getKeyword())) {
            queryWrapper.lambda().and(
                    t -> t.like(PositionEntity::getFullName, paginationPosition.getKeyword())
                            .or().like(PositionEntity::getEnCode, paginationPosition.getKeyword())
            );
        }
        if (paginationPosition.getEnabledMark() != null) {
            queryWrapper.lambda().eq(PositionEntity::getEnabledMark, paginationPosition.getEnabledMark());
        }
        if (StringUtil.isNotEmpty(paginationPosition.getEnCode())) {
            queryWrapper.lambda().eq(PositionEntity::getType, paginationPosition.getEnCode());
        }
        if (StringUtil.isNotEmpty(paginationPosition.getOrganizeId())) {
            List<String> underOrganizations = organizeService.getUnderOrganizations(paginationPosition.getOrganizeId(), false);
            // 判断哪些组织时有权限的
            List<String> collect = underOrganizations.stream().filter(orgId::contains).collect(Collectors.toList());
            orgIds.add(paginationPosition.getOrganizeId());
            orgIds.addAll(collect);
            orgIds.add(paginationPosition.getOrganizeId());
            queryWrapper.lambda().in(PositionEntity::getOrganizeId, orgIds);
        } else {
            queryWrapper.lambda().in(PositionEntity::getOrganizeId, orgId);
        }
        long count = this.count(queryWrapper);
        queryWrapper.lambda().select(PositionEntity::getId, PositionEntity::getEnCode, PositionEntity::getCreatorTime,
                PositionEntity::getOrganizeId, PositionEntity::getEnabledMark, PositionEntity::getFullName,
                PositionEntity::getSortCode, PositionEntity::getType);
        queryWrapper.lambda().orderByAsc(PositionEntity::getSortCode).orderByDesc(PositionEntity::getCreatorTime);
        Page<PositionEntity> page = new Page<>(paginationPosition.getCurrentPage(), paginationPosition.getPageSize(), count, false);
        page.setOptimizeCountSql(false);
        IPage<PositionEntity> iPage = this.page(page, queryWrapper);
        return paginationPosition.setData(iPage.getRecords(), page.getTotal());
    }

    @Override
    public List<PositionEntity> getListByUserId(String userId) {
        QueryWrapper<PositionEntity> query = new QueryWrapper<>();
        List<String> ids = new ArrayList<>();
        userRelationService.getListByObjectType(userId, PermissionConst.POSITION).forEach(r->{
            ids.add(r.getObjectId());
        });
        if(ids.size() > 0){
            query.lambda().in(PositionEntity::getId, ids);
            return this.list(query);
        }else {
            return new ArrayList<>();
        }
    }

    @Override
    public PositionEntity getInfo(String id) {
        QueryWrapper<PositionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(PositionEntity::getId,id);
        return this.getOne(queryWrapper);
    }

    @Override
    public PositionEntity getByFullName(String fullName) {
        PositionEntity positionEntity = new PositionEntity();
        QueryWrapper<PositionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(PositionEntity::getFullName, fullName);
        queryWrapper.lambda().select(PositionEntity::getId);
        List<PositionEntity> list = this.list(queryWrapper);
        if (list.size() > 0) {
            positionEntity = list.get(0);
        }
        return positionEntity;
    }

    @Override
    public boolean isExistByFullName(PositionEntity entity, boolean isFilter) {
        QueryWrapper<PositionEntity> queryWrapper = new QueryWrapper<>();
        if(entity != null) {
            queryWrapper.lambda().eq(PositionEntity::getFullName, entity.getFullName());
        }
        //是否需要过滤
        if (isFilter) {
            queryWrapper.lambda().ne(PositionEntity::getId, entity.getId());
        }
        List<PositionEntity> entityList = this.list(queryWrapper);
        for (PositionEntity positionEntity : entityList) {
            //如果组织id相同则代表已存在
            if (entity != null && entity.getOrganizeId().equals(positionEntity.getOrganizeId())){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isExistByEnCode(PositionEntity entity, boolean isFilter) {
        QueryWrapper<PositionEntity> queryWrapper = new QueryWrapper<>();
        if(entity != null){
            queryWrapper.lambda().eq(PositionEntity::getEnCode, entity.getEnCode());
            if (isFilter) {
                queryWrapper.lambda().ne(PositionEntity::getId, entity.getId());
            }
        }
//        List<PositionEntity> entityList = this.list(queryWrapper);
//        for (PositionEntity positionEntity : entityList) {
//            //如果组织id相同则代表已存在
//            if (entity != null && entity.getOrganizeId().equals(positionEntity.getOrganizeId())){
//                return true;
//            }
//        }
        return this.list(queryWrapper).size() > 0;
    }

    @Override
    public void create(PositionEntity entity) {
        if (StringUtil.isEmpty(entity.getId())) {
            entity.setId(RandomUtil.uuId());
        }
        entity.setCreatorUserId(userProvider.get().getUserId());
        this.save(entity);
    }

    @Override
    public boolean update(String id, PositionEntity entity) {
        entity.setId(id);
        entity.setLastModifyTime(new Date());
        entity.setLastModifyUserId(userProvider.get().getUserId());
        return this.updateById(entity);
    }

    @Override
    @DSTransactional
    public void delete(PositionEntity entity) {
        this.removeById(entity.getId());
        QueryWrapper<UserRelationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserRelationEntity::getObjectId,entity.getId());
        userRelationService.remove(queryWrapper);
        QueryWrapper<AuthorizeEntity> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(AuthorizeEntity::getObjectId,entity.getId());
        authorizeService.remove(wrapper);
    }

    @Override
    @DSTransactional
    public boolean first(String id) {
        boolean isOk = false;
        //获取要上移的那条数据的信息
        PositionEntity upEntity = this.getById(id);
        Long upSortCode = upEntity.getSortCode() == null ? 0 : upEntity.getSortCode();
        //查询上几条记录
        QueryWrapper<PositionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .lt(PositionEntity::getSortCode, upSortCode)
                .eq(PositionEntity::getOrganizeId,upEntity.getOrganizeId())
                .orderByDesc(PositionEntity::getSortCode);
        List<PositionEntity> downEntity = this.list(queryWrapper);
        if(downEntity.size()>0){
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
        PositionEntity downEntity = this.getById(id);
        Long upSortCode = downEntity.getSortCode() == null ? 0 : downEntity.getSortCode();
        //查询下几条记录
        QueryWrapper<PositionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .gt(PositionEntity::getSortCode, upSortCode)
                .eq(PositionEntity::getOrganizeId,downEntity.getOrganizeId())
                .orderByAsc(PositionEntity::getSortCode);
        List<PositionEntity> upEntity = this.list(queryWrapper);
        if(upEntity.size()>0){
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
    public List<PositionEntity> getPositionName(List<String> id, boolean filterEnabledMark) {
        List<PositionEntity> roleList = new ArrayList<>();
        if (id.size() > 0) {
            QueryWrapper<PositionEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(PositionEntity::getId, id);
            if (filterEnabledMark) {
                queryWrapper.lambda().eq(PositionEntity::getEnabledMark, 1);
            }
            roleList = this.list(queryWrapper);
        }
        return roleList;
    }

    @Override
    public List<PositionEntity> getPositionName(List<String> id, String keyword) {
        List<PositionEntity> roleList = new ArrayList<>();
        if (id.size() > 0) {
            QueryWrapper<PositionEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(PositionEntity::getId, id);
            //关键字（名称、编码）
            if (!StringUtil.isEmpty(keyword)) {
                queryWrapper.lambda().and(
                        t->t.like(PositionEntity::getFullName,keyword)
                                .or().like(PositionEntity::getEnCode,keyword)
                );
            }
            roleList = this.list(queryWrapper);
        }
        return roleList;
    }

    @Override
    public List<PositionEntity> getListByOrganizeId(List<String> organizeIds, boolean enabledMark) {
        if (organizeIds.size() == 0) {
            return new ArrayList<>();
        }
        QueryWrapper<PositionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(PositionEntity::getOrganizeId, organizeIds);
        if (enabledMark) {
            queryWrapper.lambda().eq(PositionEntity::getEnabledMark, 1);
        }
        queryWrapper.lambda().orderByAsc(PositionEntity::getSortCode).orderByDesc(PositionEntity::getCreatorTime);
        return this.list(queryWrapper);
    }

    @Override
    public List<PositionEntity> getListByOrgIdAndUserId(String organizeId, String userId) {
        // 用户绑定的所有岗位
        List<String> positionIds = userRelationService.getListByUserIdAndObjType(userId, PermissionConst.POSITION).stream()
                .map(UserRelationEntity::getObjectId).collect(Collectors.toList());
        if(positionIds.size() > 0){
            List<PositionEntity> positionEntities = this.listByIds(positionIds);
            return positionEntities.stream().filter(p-> p.getOrganizeId().equals(organizeId)).collect(Collectors.toList());
        }else {
            return new ArrayList<>();
        }
    }

    @Override
    public List<PositionEntity> getListByFullName(String fullName, String enCode) {
        QueryWrapper<PositionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(PositionEntity::getFullName, fullName).eq(PositionEntity::getEnCode, enCode);
        return this.list(queryWrapper);
    }

    @Override
    public List<PositionEntity> getCurPositionsByOrgId(String orgId) {
        String userId = userProvider.get().getUserId();
        List<UserRelationEntity> userRelations = userRelationService.getListByObjectType(userId, PermissionConst.POSITION);
        List<PositionEntity> positions = new ArrayList<>();
        userRelations.forEach(ur->{
            PositionEntity entity = this.getInfo(ur.getObjectId());
            if(entity.getOrganizeId().equals(orgId)){
                positions.add(entity);
            }
        });
        return positions;
    }
}
