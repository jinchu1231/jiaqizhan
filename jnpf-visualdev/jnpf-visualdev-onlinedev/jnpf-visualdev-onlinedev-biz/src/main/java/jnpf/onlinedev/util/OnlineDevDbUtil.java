package jnpf.onlinedev.util;

import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import jnpf.base.ActionResult;
import jnpf.base.model.VisualDevJsonModel;
import jnpf.base.util.VisualUtils;
import jnpf.constant.MsgCode;
import jnpf.database.model.entity.DbLinkEntity;
import jnpf.database.util.ConnUtil;
import jnpf.database.util.DynamicDataSourceUtil;
import jnpf.exception.DataException;
import jnpf.mapper.FlowFormDataMapper;
import jnpf.model.visualJson.TableModel;
import jnpf.util.FlowFormDataUtil;
import jnpf.util.TableFeildsEnum;
import jnpf.util.UserProvider;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.UpdateDSL;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 处理在线开发新增更新操作
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年3月22日10:58:29
 */
@Component
@Slf4j
public class OnlineDevDbUtil {

    @Autowired
    private FlowFormDataUtil flowDataUtil;
    @Autowired
    private FlowFormDataMapper flowFormDataMapper;
    @Autowired
    private UserProvider userProvider;

    /**
     * 删除有表单条数据
     * @param id
     * @param visualDevJsonModel
     * @return
     * @throws SQLException
     * @throws DataException
     */
    @DSTransactional
    public boolean deleteTable(String id, VisualDevJsonModel visualDevJsonModel, DbLinkEntity linkEntity) throws Exception {
        Integer primaryKeyPolicy = visualDevJsonModel.getFormData().getPrimaryKeyPolicy();
        Boolean logicalDelete = visualDevJsonModel.getFormData().getLogicalDelete();
        boolean isSnowFlake = primaryKeyPolicy == 1;
        List<TableModel> tableModels = visualDevJsonModel.getVisualTables();
        //主表
        TableModel mainTableModel = tableModels.stream().filter(t -> "1".equals(t.getTypeId())).findFirst().orElse(null);
        String mainTable = mainTableModel.getTable();
        DynamicDataSourceUtil.switchToDataSource(linkEntity);
        try {
            @Cleanup Connection conn = ConnUtil.getConnOrDefault(linkEntity);
            //获取主键
            if(primaryKeyPolicy==2 && !visualDevJsonModel.isFlowEnable()){
                primaryKeyPolicy =1;
            }
            String pKeyName = flowDataUtil.getKey(conn, mainTable, primaryKeyPolicy,visualDevJsonModel.isFlowEnable());

            SelectStatementProvider queryMain = SqlBuilder.select(SqlTable.of(mainTable).allColumns()).from(SqlTable.of(mainTable))
                    .where(SqlTable.of(mainTable).column(pKeyName), SqlBuilder.isEqualTo(id)).build().render(RenderingStrategies.MYBATIS3);
            List<Map<String, Object>> mainMapList = flowFormDataMapper.selectManyMappedRows(queryMain);
            mainMapList = VisualUtils.toLowerKeyList(mainMapList);

            DeleteStatementProvider mainDelete = SqlBuilder.deleteFrom(SqlTable.of(mainTable))
                    .where(SqlTable.of(mainTable).column(pKeyName), SqlBuilder.isEqualTo(id)).build().render(RenderingStrategies.MYBATIS3);
            if(logicalDelete){
                SqlTable sqlt = SqlTable.of(mainTable);
                UpdateDSL<UpdateModel> updateModelUpdateDSL = SqlBuilder.update(sqlt).set(sqlt.column(TableFeildsEnum.DELETEMARK.getField())).equalTo(1);
                updateModelUpdateDSL.set(sqlt.column(TableFeildsEnum.DELETETIME.getField())).equalTo(new Date());
                updateModelUpdateDSL.set(sqlt.column(TableFeildsEnum.DELETEUSERID.getField())).equalTo(userProvider.get().getUserId());
                UpdateStatementProvider mainUpdate= updateModelUpdateDSL.where(SqlTable.of(mainTable).column(pKeyName), SqlBuilder.isEqualTo(id)).build().render(RenderingStrategies.MYBATIS3);
                flowFormDataMapper.update(mainUpdate);
                return true;
            }
            flowFormDataMapper.delete(mainDelete);
            if (mainMapList.size() > 0) {
                if (tableModels.size() > 1) {
                    //去除主表
                    tableModels.remove(mainTableModel);
                    for (TableModel table : tableModels) {
                        //主表字段
                        String relationField = isSnowFlake ? table.getRelationField() : "f_flowtaskid";
                        String relationFieldValue = "";
                        for (Map<String, Object> objectMap : mainMapList) {
                            for(String key : objectMap.keySet()){
                                if(relationField.toLowerCase().equals(key.toLowerCase())){
                                    relationFieldValue = String.valueOf(objectMap.get(key));
                                }
                            }
                        }
                        //子表字段
                        String tableField = table.getTableField();
                        DeleteStatementProvider childDeleteProvider = SqlBuilder.deleteFrom(SqlTable.of(table.getTable()))
                                .where(SqlTable.of(table.getTable()).column(tableField), SqlBuilder.isEqualTo(relationFieldValue)).build().render(RenderingStrategies.MYBATIS3);
                        flowFormDataMapper.delete(childDeleteProvider);
                    }
                }
            }
        }finally {
            DynamicDataSourceUtil.clearSwitchDataSource();
        }
        return true;
    }

    /**
     * 删除有表多条数据
     * @param idList
     * @param visualDevJsonModel
     * @return
     * @throws SQLException
     * @throws DataException
     */
    @DSTransactional
    public ActionResult deleteTables(List<String> idList, VisualDevJsonModel visualDevJsonModel, DbLinkEntity linkEntity) throws Exception {
        Boolean logicalDelete = visualDevJsonModel.getFormData().getLogicalDelete();
        List<TableModel> tableModels = visualDevJsonModel.getVisualTables();

        if (idList.size()==0){
            return ActionResult.fail(MsgCode.FA003.get());
        }

        TableModel tableModel = tableModels.stream().filter(t -> "1".equals(t.getTypeId())).findFirst().orElse(null);
        //取主表
        String mainTable = Optional.ofNullable(tableModel.getTable()).orElse("") ;
        //切换数据源
        try {
            DynamicDataSourceUtil.switchToDataSource(linkEntity);
            @Cleanup Connection conn = ConnUtil.getConnOrDefault(linkEntity);
            //获取主键
            Integer primaryKeyPolicy = visualDevJsonModel.getFormData().getPrimaryKeyPolicy();
            boolean isSnowFlake = primaryKeyPolicy == 1;
            if(primaryKeyPolicy==2 && !visualDevJsonModel.isFlowEnable()){
                primaryKeyPolicy=1;
            }
            String pKeyName = flowDataUtil.getKey(conn, mainTable,primaryKeyPolicy);

            //查询数据是否存在
            for (int i =0;i<idList.size();i++){
                String id = idList.get(i);
                SelectStatementProvider queryMain = SqlBuilder.select(SqlTable.of(mainTable).allColumns()).from(SqlTable.of(mainTable))
                    .where(SqlTable.of(mainTable).column(pKeyName), SqlBuilder.isEqualTo(id)).build().render(RenderingStrategies.MYBATIS3);
                List<Map<String, Object>> mainMapList =  flowFormDataMapper.selectManyMappedRows(queryMain);

                DeleteStatementProvider mainDelete = SqlBuilder.deleteFrom(SqlTable.of(mainTable))
                    .where(SqlTable.of(mainTable).column(pKeyName), SqlBuilder.isEqualTo(id)).build().render(RenderingStrategies.MYBATIS3);
                //假删操作
                if(logicalDelete){
                    SqlTable sqlt = SqlTable.of(mainTable);
                    UpdateDSL<UpdateModel> updateModelUpdateDSL = SqlBuilder.update(sqlt).set(sqlt.column(TableFeildsEnum.DELETEMARK.getField())).equalTo(1);
                    UpdateStatementProvider mainUpdate= updateModelUpdateDSL.where(SqlTable.of(mainTable).column(pKeyName), SqlBuilder.isEqualTo(id)).build().render(RenderingStrategies.MYBATIS3);
                    flowFormDataMapper.update(mainUpdate);
                }else{
                    flowFormDataMapper.delete(mainDelete);

                    if (mainMapList.size()>0){
                        //是否存在子表
                        if(tableModels.size()>1){
                            //去除主表
                            tableModels.remove(tableModel);
                            for (TableModel table : tableModels) {
                                //主表字段
                                String relationField = isSnowFlake ? table.getRelationField() : "f_flowtaskid";
                                String relationFieldValue = "";
                                for (Map<String, Object> objectMap : mainMapList) {
                                    for(String key : objectMap.keySet()){
                                        if(relationField.toLowerCase().equals(key.toLowerCase())){
                                            relationFieldValue = String.valueOf(objectMap.get(key));
                                        }
                                    }
                                }
                                //子表字段
                                String tableField = table.getTableField();
                                DeleteStatementProvider childDeleteProvider = SqlBuilder.deleteFrom(SqlTable.of(table.getTable()))
                                        .where(SqlTable.of(table.getTable()).column(tableField), SqlBuilder.isEqualTo(relationFieldValue)).build().render(RenderingStrategies.MYBATIS3);
                                flowFormDataMapper.delete(childDeleteProvider);
                            }
                        }
                    }
                }
            }
        } finally {
            DynamicDataSourceUtil.clearSwitchDataSource();
        }
        return ActionResult.success(MsgCode.SU003.get());
    }

    public Boolean getVersion(String table, DbLinkEntity linkEntity, Map dataMap,String id,Integer primaryKey) throws SQLException, DataException {
        boolean canUpdate = true;
        DynamicDataSourceUtil.switchToDataSource(linkEntity);
        try {
            @Cleanup Connection conn = ConnUtil.getConnOrDefault(linkEntity);
            String pKeyName = flowDataUtil.getKey(conn, table, primaryKey);
            SqlTable sqlTable = SqlTable.of(table);
            SelectStatementProvider render = SqlBuilder.select(sqlTable.column(TableFeildsEnum.VERSION.getField())).from(sqlTable).where(sqlTable.column(pKeyName), SqlBuilder.isEqualTo(id))
                    .and(sqlTable.column(TableFeildsEnum.VERSION.getField()), SqlBuilder.isEqualTo(dataMap.get(TableFeildsEnum.VERSION.getField()))).build().render(RenderingStrategies.MYBATIS3);
            List<Map<String, Object>> mapList = flowFormDataMapper.selectManyMappedRows(render);
            canUpdate = mapList.size() > 0;
        }finally {
            DynamicDataSourceUtil.clearSwitchDataSource();
        }
        return canUpdate;
    }

}
