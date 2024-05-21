package jnpf.base.service;
import jnpf.base.entity.FilterEntity;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.model.filter.RuleInfo;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.select.SelectModel;

import java.util.List;
import java.util.Map;


public interface FilterService extends SuperService<FilterEntity> {
    void saveRuleList(String moduleId, VisualdevEntity visualdevEntity, Integer app, Integer pc, Map<String,String> tableMap);

    void updateRuleList(String moduleId, VisualdevEntity columnData, Integer app, Integer pc, Map<String,String> tableMap);


    void handleWhereCondition(SqlTable sqlTable, QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder where, String id, Map<String,SqlTable> subSqlTableMap, String databaseProductName, Map<String,Object> params);

    void handleWhereCondition(SqlTable sqlTable, QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder where, String id, Map<String,SqlTable> subSqlTableMap, String databaseProductName);

    // 获取过滤配置
    List<RuleInfo> getCondition(String id);
}
