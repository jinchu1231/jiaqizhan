package jnpf.util;

import cn.hutool.core.bean.BeanUtil;
import jnpf.database.model.dbfield.DbFieldModel;
import jnpf.database.model.dbfield.base.DbFieldModelBase;
import jnpf.database.model.dbtable.DbTableFieldModel;
import jnpf.exception.WorkFlowException;
import jnpf.model.visualJson.TableModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ConcurrencyUtils {

    @Autowired
    private ServiceBaseUtil serviceUtil;

    /**
     * 根据枚举获取字段对象
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2023/1/14
     */
    public static DbFieldModel getDbFieldModel(TableFeildsEnum tableFeildsEnum) {
        DbFieldModel dbFieldModel = new DbFieldModel();
        BeanUtil.copyProperties(tableFeildsEnum, dbFieldModel);
        dbFieldModel.setIsPrimaryKey(tableFeildsEnum.getPrimaryKey());
        return dbFieldModel;
    }

    /**
     * 创建锁字段
     *
     * @param table
     * @param linkId
     * @throws Exception
     */
    public void createVersion(String table, String linkId) throws Exception {
        addFeild(table, linkId,TableFeildsEnum.VERSION);
    }

    /**
     * 创建flowTaskId
     *
     * @param table
     * @param linkId
     * @throws Exception
     */
    public void createFlowTaskId(String table, String linkId) throws Exception {
        addFeild(table, linkId,TableFeildsEnum.FLOWTASKID);
    }

    /**
     * 创建租户id
     *
     * @param table
     * @param linkId
     * @throws Exception
     */
    public void createTenantId(String table, String linkId) throws Exception {
        addFeild(table, linkId,TableFeildsEnum.TENANTID);
    }

    /**
     * 创建删除字段
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2023/1/14
     */
    public void creDeleteMark(String table, String linkId) throws Exception {
        addFeild(table, linkId,TableFeildsEnum.DELETEMARK);
        addFeild(table, linkId,TableFeildsEnum.DELETETIME);
        addFeild(table, linkId,TableFeildsEnum.DELETEUSERID);
    }

    /**
     * 创建流程引擎id字段
     *
     * @param
     * @return
     * @copyright 引迈信息技术有限公司
     * @date 2023/1/7
     */
    public void createFlowEngine(String table, String linkId) throws Exception {
        addFeild(table, linkId,TableFeildsEnum.FLOWID);
    }

    /**
     * 新增字段通用方法
     * @param table
     * @param linkId
     * @param tableFeildsEnum
     * @throws Exception
     */
    private void addFeild(String table, String linkId,TableFeildsEnum tableFeildsEnum) throws Exception {
        List<DbFieldModelBase> fieldList = serviceUtil.getDbTableModel(linkId, table);
        DbFieldModelBase dbFieldModel = fieldList.stream().filter(f -> f.getField().equalsIgnoreCase(tableFeildsEnum.getField())).findFirst().orElse(null);
        boolean hasVersion = dbFieldModel!=null;
        if (!hasVersion){
            DbTableFieldModel dbTableFieldModel = new DbTableFieldModel();
            DbFieldModel dbTableModel1 = this.getDbFieldModel(tableFeildsEnum);
            List<DbFieldModel> fieldOneList = new ArrayList<>();
            fieldOneList.add(dbTableModel1);
            dbTableFieldModel.setDbFieldModelList(fieldOneList);
            dbTableFieldModel.setUpdateNewTable(table);
            dbTableFieldModel.setUpdateOldTable(table);
            dbTableFieldModel.setDbLinkId(linkId);
            serviceUtil.addField(dbTableFieldModel);
        }
    }


    /**
     * 判断表是否是自增id
     * @param primaryKeyPolicy
     * @param dbLinkId
     * @param tableList
     * @return
     * @throws Exception
     */
    public  boolean checkAutoIncrement(int primaryKeyPolicy,String dbLinkId,List<TableModel> tableList) throws Exception {
        boolean isIncre = primaryKeyPolicy == 2;
        String strategy = primaryKeyPolicy == 1 ? "[雪花ID]" : "[自增长id]";
        for (TableModel tableModel : tableList) {
            List<DbFieldModel> data = serviceUtil.getFieldList(dbLinkId, tableModel.getTable());
            DbFieldModel dbFieldModel = data.stream().filter(DbFieldModel::getIsPrimaryKey).findFirst().orElse(null);
            if (dbFieldModel == null) {
                throw new WorkFlowException("表[" + tableModel.getTable() + " ]无主键!");
            }
            if (!isIncre == (dbFieldModel.getIsAutoIncrement() != null && dbFieldModel.getIsAutoIncrement())) {
                throw new WorkFlowException("主键策略:" + strategy + "，与表[" + tableModel.getTable() + "]主键策略不一致!");
            }
        }
        return true;
    }
}
