package jnpf.base.util;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import jnpf.database.model.entity.DbLinkEntity;
import jnpf.database.source.DbBase;
import jnpf.database.source.impl.DbPostgre;
import jnpf.database.util.ConnUtil;
import jnpf.database.util.DataSourceUtil;
import jnpf.database.util.DbTypeUtil;
import jnpf.database.util.DynamicDataSourceUtil;
import jnpf.database.util.TenantDataSourceUtil;
import jnpf.util.StringUtil;
import jnpf.util.TenantHolder;


public class SourceUtil {
    public static DataSourceConfig dbConfig(String dbName, DataSourceUtil linkEntity) {
        if (linkEntity == null) {
            if(TenantDataSourceUtil.isTenantAssignDataSource()){
                linkEntity = TenantDataSourceUtil.getTenantAssignDataSource(TenantHolder.getDatasourceId()).toDbLink(new DbLinkEntity());
            }else{
                linkEntity = DynamicDataSourceUtil.dataSourceUtil.init();
            }
            if (!"KingbaseES".equals(linkEntity.getDbType()) && !"PostgreSQL".equals(linkEntity.getDbType()) && StringUtil.isNotEmpty(dbName)) {
                linkEntity.setDbName(dbName);
            }
        }
        DataSourceConfig dsc = new DataSourceConfig();
        try {
            DbBase dbBase = DbTypeUtil.getDb(linkEntity);
            dsc.setDbType(dbBase.getMpDbType());
            dsc.setDriverName(dbBase.getDriver());
            dsc.setUsername(linkEntity.getUserName());
            dsc.setPassword(linkEntity.getPassword());
            dsc.setSchemaName(linkEntity.getDbSchema());

            // oracle 默认 schema = username
            if (dsc.getDbType().getDb().equalsIgnoreCase(DbType.ORACLE.getDb())
                    || dsc.getDbType().getDb().equalsIgnoreCase(DbType.KINGBASE_ES.getDb())) {
                dsc.setSchemaName(linkEntity.getUserName());
            }
            //postgre默认 public
            if (dsc.getDbType().getDb().equalsIgnoreCase(DbType.POSTGRE_SQL.getDb())) {
                if (StringUtil.isNotEmpty(dbName)) {
                    dsc.setSchemaName(dbName);
                } else if (StringUtil.isNotEmpty(linkEntity.getDbSchema())) {
                    dsc.setSchemaName(linkEntity.getDbSchema());
                } else {
                    dsc.setSchemaName(DbPostgre.DEF_SCHEMA);
                }
            }
            dsc.setUrl(ConnUtil.getUrl(linkEntity));
        } catch (Exception e) {
            e.getStackTrace();
        }
        return dsc;
    }

}
