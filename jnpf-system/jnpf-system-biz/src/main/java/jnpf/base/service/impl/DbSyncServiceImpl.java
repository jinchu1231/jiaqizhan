package jnpf.base.service.impl;


import jnpf.base.service.DbLinkService;
import jnpf.base.service.DbSyncService;
import jnpf.base.service.DbTableService;
import jnpf.database.datatype.model.DtModelDTO;
import jnpf.database.datatype.sync.util.DtSyncUtil;
import jnpf.database.model.dbfield.DbFieldModel;
import jnpf.database.model.dbfield.JdbcColumnModel;
import jnpf.database.model.dbtable.DbTableFieldModel;
import jnpf.database.model.dto.PrepSqlDTO;
import jnpf.database.model.entity.DbLinkEntity;
import jnpf.database.source.DbBase;
import jnpf.database.sql.enums.base.SqlComEnum;
import jnpf.database.sql.model.SqlPrintHandler;
import jnpf.database.sql.param.FormatSqlKingbaseES;
import jnpf.database.sql.param.FormatSqlMySQL;
import jnpf.database.sql.param.FormatSqlOracle;
import jnpf.database.sql.util.SqlFastUtil;
import jnpf.database.util.DataSourceUtil;
import jnpf.database.util.JdbcUtil;
import jnpf.exception.DataException;
import jnpf.exception.DataTypeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;

/**
 * 数据同步
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Slf4j
@Service
public class DbSyncServiceImpl implements DbSyncService {

    @Autowired
    private DbLinkService dblinkService;
    @Autowired
    private DbTableService dbTableService;
    @Autowired
    private SqlPrintHandler sqlPrintHandler;
    @Autowired
    private DataSourceUtil dataSourceUtil;


    private static Properties props;

    static {
        Properties props = new Properties();
        props.setProperty("remarks", "true"); //设置可以获取remarks信息
        props.setProperty("useInformationSchema", "true");//设置可以获取tables remarks信息
        DbSyncServiceImpl.props = props;
    }

    @Override
    public Integer checkExecute(String fromId, String toId, Map<String, String> convertRuleMap, String table) throws Exception {
        DbLinkEntity dbLinkFrom;
        DbLinkEntity dbLinkTo;
        if("0".equals(fromId)){
            dbLinkFrom = dataSourceUtil.init();
        }else {
            dbLinkFrom = DbLinkEntity.newInstance(fromId);
        }
        if("0".equals(toId)){
            dbLinkTo = dataSourceUtil.init();
        }else {
            dbLinkTo = DbLinkEntity.newInstance(toId);
        }
        //验证一（同库无法同步数据）
        if (fromId.equals(toId) ||
                (Objects.equals(dbLinkFrom.getHost(), dbLinkTo.getHost()) &&
                        (Objects.equals(dbLinkFrom.getPort(), dbLinkTo.getPort()) &&
                                (Objects.equals(dbLinkFrom.getDbName(), dbLinkTo.getDbName())
                                )))){
            if(DbBase.ORACLE.equals(dbLinkFrom.getDbType()) || DbBase.DM.equals(dbLinkFrom.getDbType())){
                if(dbLinkFrom.getUserName().equals(dbLinkTo.getUserName())){
                    return -1;
                }
            }else {
                return -1;
            }
        }
        //验证二（表存在）
        if (dbTableService.isExistTable(toId, table)) {
            //验证三（验证表数据）
            if (SqlFastUtil.tableDataExist(toId, table)) {
                //被同步表存在数据
                return 3;
            }
        }
        // 表不存在
        if (!dbTableService.isExistTable(toId, table)) {
            return 2;
        }
        return 0;
    }

    @Override
    public void executeImport(String dbLinkIdFrom, String dbLinkIdTo, Map<String, String> convertRuleMap, String table) throws Exception {
        executeTableCommon(dbLinkIdFrom, dbLinkIdTo, convertRuleMap, table);
        sqlPrintHandler.print();
    }

    @Override
    public Map<String, Integer> batchExecuteImport(String dbLinkIdFrom, String dbLinkIdTo, Map<String, String> convertRuleMap, List<String> tableList) throws Exception {
        Map<String, Integer> messageMap = new HashMap<>(16);
        for (int i = 0; i < tableList.size(); i++) {
            String table = tableList.get(i);
            int total = tableList.size();
            try{
                executeTableCommon(dbLinkIdFrom, dbLinkIdTo, convertRuleMap, table);
                messageMap.put(table, 1);
                log.info("表：（" + table + "）同步成功！" + "(" + (i + 1) + "/" + total + ")");
            }catch (Exception e){
                e.printStackTrace();
                messageMap.put(table, 0);
                log.info("表：（" + table + "）同步失败！" + "(" + (i + 1) + "/" + total + ")");
            }
        }
        return messageMap;
    }

    /**
     * 【主要】同步建表操作
     */
    private void executeTableCommon(String fromLinkId, String toLinkId, Map<String, String> convertRuleMap, String table) throws Exception {
        sqlPrintHandler.tableInfo(table);
        DbLinkEntity dbLinkFrom = dblinkService.getResource(fromLinkId);
        DbLinkEntity dbLinkTo = dblinkService.getResource(toLinkId);
        // 1、删除To表
        try{
            if(!sqlPrintHandler.getPrintFlag()) SqlFastUtil.dropTable(dbLinkTo, table);
        }catch (Exception ignore){}
        // 2、创建To表
        DbTableFieldModel tableMod = convertFileDataType(dbTableService.getDbTableModel(fromLinkId, table), convertRuleMap, dbLinkFrom.getDbType(), dbLinkTo.getDbType());
        SqlFastUtil.creTable(dbLinkTo, tableMod);
        // 3、同步数据 From -> To
        SqlFastUtil.batchInsert(table, dbLinkTo, getInsertMapList(dbLinkFrom, dbLinkTo.getDbType(), table));
    }

    /**
     * 获取插入数据map
     */
    public List<Map<String, Object>> getInsertMapList(DbLinkEntity dbLinkFrom, String toDbType, String table) throws Exception {
        List<List<JdbcColumnModel>> modelList = JdbcUtil.queryJdbcColumns(new PrepSqlDTO(SqlComEnum.SELECT_TABLE.getOutSql(table)).withConn(dbLinkFrom)).get();
        List<Map<String, Object>> insertMapList = new ArrayList<>();
        for (List<JdbcColumnModel> jdbcColumnModels : modelList) {
            Map<String, Object> map = new HashMap<>();
            for (JdbcColumnModel jdbcColumnModel : jdbcColumnModels) {
                map.put(jdbcColumnModel.getField(), checkValue(jdbcColumnModel, dbLinkFrom.getDbType()));
                FormatSqlOracle.nullValue(toDbType, jdbcColumnModel, map); // Oracle空串处理
                FormatSqlKingbaseES.nullValue(toDbType, jdbcColumnModel, map); // KingbaseES空串处理
            }
            insertMapList.add(map);
        }
        return insertMapList;
    }

    // 不同数据库之间，特殊数据类型与值校验
    private Object checkValue(JdbcColumnModel model, String dbType){
        Function<String, Boolean> checkVal = (dataType) ->
                model.getDataType().equalsIgnoreCase(dataType) && model.getValue() != null;
        switch (dbType){
            case DbBase.MYSQL:
                /* MySQL设置tinyint类型且长度为1时，JDBC读取时会变成BIT类型，java类型为Boolean类型。
                   1:true , 0:false */
                if(checkVal.apply("BIT")) return String.valueOf(model.getValue());
            case DbBase.ORACLE:
                if(checkVal.apply("NCLOB")) return String.valueOf(model.getValue());
                return FormatSqlOracle.timestamp(model.getValue());
            case DbBase.SQL_SERVER:
            case DbBase.KINGBASE_ES:
            case DbBase.DM:
            case DbBase.POSTGRE_SQL:
                // TODO 等待补充
            default:
                return model.getValue();
        }
    }

    /**
     * 【处理字段类型】
     */
    private DbTableFieldModel convertFileDataType(DbTableFieldModel dbTableFieldModel, Map<String, String> convertRuleMap,
                                                  String fromDbEncode, String toDbEncode) throws Exception {
        String table = dbTableFieldModel.getTable();
        List<DbFieldModel> fields = dbTableFieldModel.getDbFieldModelList();
        // 规则Map里的（默认）去除
        if(convertRuleMap != null){
            convertRuleMap.forEach((key, val) ->{
                convertRuleMap.put(key, val.replace(" (默认)", ""));
            });
        }
        for (DbFieldModel field : fields) {
            try {
                // 设置转换数据类型
                field.getDtModelDTO().setConvertTargetDtEnum(DtSyncUtil.getToCovert(fromDbEncode, toDbEncode, field.getDataType(), convertRuleMap));
                if(toDbEncode.equals(DbBase.MYSQL)){
                    FormatSqlMySQL.checkMysqlFieldPrimary(field, table);
                }
            }catch (DataException d){
                System.out.println("表_" + table + ":" + d.getMessage());
                DataException dataException = new DataException("目前还未支持数据类型" + toDbEncode + "." + table + "（" + field.getDataType() + "）");
                dataException.printStackTrace();
                // 类型寻找失败转换成字符串
                field.setDataType(DtModelDTO.getStringFixedDt(toDbEncode));
                throw dataException;
            }catch (Exception e) {
                e.printStackTrace();
                if(e instanceof DataTypeException){
                    throw e;
                }
                log.info(e.getMessage());
            }
        }
        return dbTableFieldModel;
    }








}
