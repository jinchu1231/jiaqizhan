package jnpf.util;

import cn.hutool.core.util.ObjectUtil;
import jnpf.database.model.entity.DbLinkEntity;
import jnpf.database.util.ConnUtil;
import jnpf.database.util.DynamicDataSourceUtil;
import jnpf.exception.DataException;
import jnpf.mapper.FlowFormDataMapper;
import jnpf.model.form.FormCheckModel;
import jnpf.model.visualJson.FieLdsModel;
import jnpf.model.visualJson.TableModel;
import jnpf.util.visiual.JnpfKeyConsts;
import lombok.Cleanup;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 验证表单数据
 *
 * @author JNPF开发平台组
 * @version V3.4.2
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2022/5/25
 */
@Component
public class FormCheckUtils {
    @Autowired
    private FlowFormDataUtil flowDataUtil;
    @Autowired
    private FlowFormDataMapper flowFormDataMapper;

    public String checkForm(List<FieLdsModel> formFieldList, Map<String, Object> dataMap, DbLinkEntity linkEntity, List<TableModel> tableModelList, Integer policy, Boolean logicalDelete, String id) {
        List<FieLdsModel> fields = new ArrayList<>();
        FormPublicUtils.recursionFieldsExceptChild(fields, formFieldList);
        String checkErrorMessage ="";
        //查询返回对应条数
        int i = 0;
        //符合条件的控件
        List<FieLdsModel> mainFields = checkInputUnique(fields);
        try {
            //切换数据源
            DynamicDataSourceUtil.switchToDataSource(linkEntity);
            @Cleanup Connection conn = ConnUtil.getConnOrDefault(linkEntity);
            List<FormCheckModel> formCheckModels = new ArrayList<>();
            for (FieLdsModel fieLdsModel : mainFields) {
                Object o = dataMap.get(fieLdsModel.getVModel());
                if (ObjectUtil.isNotNull(o) && StringUtil.isNotEmpty(o.toString())) {
                    o=String.valueOf(o).trim();
                    dataMap.put(fieLdsModel.getVModel(),o);
                    String tableName = fieLdsModel.getConfig().getTableName();
                    SqlTable sqlTable = SqlTable.of(tableName);
                    String fieldName=fieLdsModel.getVModel();
                    if(fieldName.toLowerCase().contains("_jnpf_")){//附表字段名称
                        fieldName= fieldName.split("_jnpf_")[1];
                    }
                    QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder where = SqlBuilder.select(sqlTable.column(fieldName)).from(sqlTable).where(sqlTable.column(fieldName), SqlBuilder.isEqualTo(o.toString()));
                    if (StringUtils.isNotEmpty(id)){
                        String relationField;
                        //判断是否主表
                        TableModel tab = tableModelList.stream().filter(tableModel -> tableModel.getTable().equalsIgnoreCase(tableName)).findFirst().orElse(null);
                        if ("1".equals(tab.getTypeId())){
                            relationField = flowDataUtil.getKey(conn,tableName,policy);
                        }else {
                            relationField = tab.getTableField();
                        }
                        where.and(sqlTable.column(relationField), SqlBuilder.isNotEqualTo(id));
                    }
                    if(logicalDelete){
                        where.and(sqlTable.column(TableFeildsEnum.DELETEMARK.getField()),SqlBuilder.isNull());
                    }
                    SelectStatementProvider render = where.build().render(RenderingStrategies.MYBATIS3);
                    FormCheckModel formCheckModel = new FormCheckModel();
                    formCheckModel.setLabel(fieLdsModel.getConfig().getLabel());
                    formCheckModel.setStatementProvider(render);
                    formCheckModels.add(formCheckModel);
                }
            }

            //主副表数据库判重
            for (FormCheckModel formCheckModel : formCheckModels) {
                int count = flowFormDataMapper.selectManyMappedRows(formCheckModel.getStatementProvider()).size();
                if (count > 0 ) {
                    checkErrorMessage = formCheckModel.getLabel();
                    i++;
                }
            }

            //子表当前表单数据判重
            List<FieLdsModel> childFieldList = fields.stream().filter(f -> JnpfKeyConsts.CHILD_TABLE.equals(f.getConfig().getJnpfKey())).collect(Collectors.toList());
            for (FieLdsModel fieLdsModel : childFieldList) {
                List<FieLdsModel> fieLdsModels = checkInputUnique(fieLdsModel.getConfig().getChildren());
                List<Map<String, Object>> childMapList = (List)dataMap.get(fieLdsModel.getVModel());
                if (childMapList!=null){
                    for (FieLdsModel childField : fieLdsModels) {
                        List<String> childValues = childMapList.stream().filter(ChildTbMap -> ChildTbMap.get(childField.getVModel())!=null)
                            .map(ChildTbMap -> String.valueOf(ChildTbMap.get(childField.getVModel())).trim()).collect(Collectors.toList());

                        if (childValues.size() > 0) {
                            HashSet<String> child = new HashSet<>(childValues);
                            if (child.size() != childValues.size()){
                                checkErrorMessage = childField.getConfig().getLabel();
                            }
//                            String tableName = childField.getConfig().getRelationTable();
//                            SqlTable sqlTable = SqlTable.of(tableName);
//                            QueryExpressionDSL<org.mybatis.dynamic.sql.select.SelectModel>.QueryExpressionWhereBuilder where = SqlBuilder.select(sqlTable.column(childField.getVModel())).from(sqlTable).where(sqlTable.column(childField.getVModel()), SqlBuilder.isIn(childValues));
//                            if (StringUtils.isNotEmpty(id)){
//                                String relationField;
//                                //判断是否主表
//                                TableModel tab = tableModelList.stream().filter(tableModel -> tableModel.getTable().equalsIgnoreCase(tableName)).findFirst().orElse(null);
//                                relationField = tab.getTableField();
//                                where.and(sqlTable.column(relationField),SqlBuilder.isNotEqualTo(id));
//                            }
//                            SelectStatementProvider render = where.build().render(RenderingStrategies.MYBATIS3);
//                            FormCheckModel formCheckModel = new FormCheckModel();
//                            formCheckModel.setLabel(childField.getConfig().getLabel());
//                            formCheckModel.setStatementProvider(render);
//                            formCheckModels.add(formCheckModel);
                        }
                    }
                }
            }

            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DynamicDataSourceUtil.clearSwitchDataSource();
        }
      return checkErrorMessage;
    }

    /**
     * 输入框唯一
     *
     * @param fields
     * @return
     */
    private static List<FieLdsModel> checkInputUnique(List<FieLdsModel> fields) {
        List<FieLdsModel> inputUnique = fields.stream().filter(field -> JnpfKeyConsts.COM_INPUT.equals(field.getConfig().getJnpfKey())
                && field.getConfig().getUnique()).collect(Collectors.toList());
        return inputUnique;
    }


    public long getCount(String id, SqlTable sqlTable, TableModel tableModel, DbLinkEntity linkEntity, Integer primaryKeyPolicy){
        int count = 0;
        try {
            DynamicDataSourceUtil.switchToDataSource(linkEntity);
            @Cleanup Connection conn = ConnUtil.getConnOrDefault(linkEntity);
            String key = flowDataUtil.getKey(conn, tableModel.getTable(), primaryKeyPolicy);
            SelectStatementProvider countRender = SqlBuilder.select(sqlTable.column(key)).from(sqlTable).where(sqlTable.column(key), SqlBuilder.isEqualTo(id)).build().render(RenderingStrategies.MYBATIS3);
            count = flowFormDataMapper.selectManyMappedRows(countRender).size();
        } catch (DataException e) {
            e.printStackTrace();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        } finally {
            DynamicDataSourceUtil.clearSwitchDataSource();
        }
        return count;
    }


}
