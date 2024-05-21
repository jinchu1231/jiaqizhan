package jnpf.base.service.impl;


import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jnpf.base.mapper.DbLinkMapper;
import jnpf.base.model.dblink.PaginationDbLink;
import jnpf.base.service.DbLinkService;
import jnpf.base.service.DbTableService;
import jnpf.base.service.SuperServiceImpl;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.MsgCode;
import jnpf.database.model.dto.PrepSqlDTO;
import jnpf.database.model.entity.DbLinkEntity;
import jnpf.database.source.DbBase;
import jnpf.database.util.ConnUtil;
import jnpf.database.util.DataSourceUtil;
import jnpf.database.util.DbTypeUtil;
import jnpf.database.util.DynamicDataSourceUtil;
import jnpf.database.util.TenantDataSourceUtil;
import jnpf.exception.DataException;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.TenantHolder;
import jnpf.util.UserProvider;
import lombok.Cleanup;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Date;
import java.util.List;

/**
 * 数据连接
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Service
public class DbLinkServiceImpl extends SuperServiceImpl<DbLinkMapper, DbLinkEntity> implements DbLinkService, InitializingBean {

    @Autowired
    private DbLinkService dblinkService;
    @Autowired
    private DataSourceUtil dataSourceUtils;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private DbTableService dbTableService;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private DynamicRoutingDataSource dynamicRoutingDataSource;

    @Override
    public List<DbLinkEntity> getList() {
        QueryWrapper<DbLinkEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByAsc(DbLinkEntity::getSortCode)
                .orderByDesc(DbLinkEntity::getCreatorTime);
        return this.list(queryWrapper);
    }

    @Override
    public List<DbLinkEntity> getList(PaginationDbLink pagination) {
        // 定义变量判断是否需要使用修改时间倒序
        boolean flag = false;
        QueryWrapper<DbLinkEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtil.isNotEmpty(pagination.getKeyword())) {
            flag = true;
            queryWrapper.lambda().and(
                    t -> t.like(DbLinkEntity::getFullName, pagination.getKeyword())
                            .or().like(DbLinkEntity::getHost, pagination.getKeyword())
            );
        }
        if (StringUtil.isNotEmpty(pagination.getDbType())) {
            flag = true;
            queryWrapper.lambda().eq(DbLinkEntity::getDbType, pagination.getDbType());
        }
        queryWrapper.lambda().orderByAsc(DbLinkEntity::getSortCode)
                .orderByDesc(DbLinkEntity::getCreatorTime);
        if (flag) {
            queryWrapper.lambda().orderByDesc(DbLinkEntity::getLastModifyTime);
        }
        Page<DbLinkEntity> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
        IPage<DbLinkEntity> iPage = this.page(page, queryWrapper);
        return pagination.setData(iPage.getRecords(), page.getTotal());
    }

    @Override
    public DbLinkEntity getInfo(String id) {
        QueryWrapper<DbLinkEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DbLinkEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean isExistByFullName(String fullName, String id) {
        QueryWrapper<DbLinkEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DbLinkEntity::getFullName, fullName);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(DbLinkEntity::getId, id);
        }
        return this.count(queryWrapper) > 0;
    }

    @Override
    public DbLinkEntity getInfoByFullName(String fullName) {
        QueryWrapper<DbLinkEntity> query = new QueryWrapper<>();
        query.lambda().eq(DbLinkEntity::getFullName, fullName);
        return getOne(query);
    }

    @Override
    public void create(DbLinkEntity entity) {
        entity.setId(RandomUtil.uuId());
        this.save(entity);
    }

    @Override
    public boolean update(String id, DbLinkEntity entity) {
        entity.setId(id);
        return this.updateById(entity);
    }

    @Override
    public void delete(DbLinkEntity entity) {
        this.removeById(entity.getId());
    }

    @Override
    @DSTransactional
    public boolean first(String id) {
        boolean isOk = false;
        //获取要上移的那条数据的信息
        DbLinkEntity upEntity = this.getById(id);
        Long upSortCode = upEntity.getSortCode() == null ? 0 : upEntity.getSortCode();
        //查询上几条记录
        QueryWrapper<DbLinkEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .lt(DbLinkEntity::getSortCode, upSortCode)
                .orderByDesc(DbLinkEntity::getSortCode);
        List<DbLinkEntity> downEntity = this.list(queryWrapper);
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
        DbLinkEntity downEntity = this.getById(id);
        Long upSortCode = downEntity.getSortCode() == null ? 0 : downEntity.getSortCode();
        //查询下几条记录
        QueryWrapper<DbLinkEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .gt(DbLinkEntity::getSortCode, upSortCode)
                .orderByAsc(DbLinkEntity::getSortCode);
        List<DbLinkEntity> upEntity = this.list(queryWrapper);
        if (upEntity.size() > 0) {
            //交换两条记录的sort值
            Long temp = downEntity.getSortCode();
            downEntity.setSortCode(upEntity.get(0).getSortCode());
            downEntity.setLastModifyTime(new Date());
            upEntity.get(0).setSortCode(temp);
            this.updateById(upEntity.get(0));
            this.updateById(downEntity);
            isOk = true;
        }
        return isOk;
    }

    @Override
    public boolean testDbConnection(DbLinkEntity entity) throws Exception{
        //判断字典数据类型编码是否错误，大小写不敏感
        DbBase db = DbTypeUtil.getDb(entity);
        if(db == null){
            throw new DataException(MsgCode.DB001.get());
        }
        @Cleanup Connection conn = ConnUtil.getConn(entity.getUserName(), entity.getPassword(), ConnUtil.getUrl(entity));
        return conn != null;
    }

    /**
     * 设置数据源
     * @param dbLinkId 数据连接id
     * @throws DataException ignore
     */
    @Override
    public DbLinkEntity getResource(String dbLinkId) throws Exception {
        DbLinkEntity dbLinkEntity = new DbLinkEntity();
        //多租户是否开启
        if("0".equals(dbLinkId)){
            if(TenantDataSourceUtil.isTenantAssignDataSource()){
                // 默认数据库, 租户管理指定租户数据源
                dbLinkEntity = TenantDataSourceUtil.getTenantAssignDataSource(TenantHolder.getDatasourceId()).toDbLink(new DbLinkEntity());
                dbLinkEntity.setId("0");
            }else {
                // 默认数据库查询，从配置获取数据源信息
                BeanUtils.copyProperties(dataSourceUtils, dbLinkEntity);
                dbLinkEntity.setId("0");
                // 是系统默认的多租户
                TenantDataSourceUtil.initDataSourceTenantDbName(dbLinkEntity);
            }
        }else {
            try {
                DynamicDataSourceUtil.switchToDataSource(null);
                dbLinkEntity = dblinkService.getInfo(dbLinkId);
            }finally {
                DynamicDataSourceUtil.clearSwitchDataSource();
            }
        }
        // 添加并且切换数据源
        return dbLinkEntity;
    }

    @Override
    public DbLinkEntity getResource(String dbLinkId, String tenantId) throws Exception {
        // 判断是否为多租户
        if (configValueUtil.isMultiTenancy()) {
            TenantDataSourceUtil.switchTenant(tenantId);
        }
        return dblinkService.getResource(dbLinkId);
    }

    @Override
    public void afterPropertiesSet(){
        PrepSqlDTO.DB_LINK_FUN = (dbLinkId)-> {
            try {
                return (DbLinkEntity) getResource(dbLinkId);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        };
    }

}
