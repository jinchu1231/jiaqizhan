package jnpf.onlinedev.util.onlineDevUtil;

import cn.hutool.core.util.ObjectUtil;
import jnpf.base.model.advancedquery.OnlineDynamicSqlModel;
import jnpf.database.model.superQuery.SuperQueryJsonModel;
import jnpf.emnus.SearchMethodEnum;
import jnpf.model.visualJson.FieLdsModel;
import jnpf.model.visualJson.config.ConfigModel;
import lombok.Data;
import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.DerivedColumn;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.VisitableCondition;

import java.util.ArrayList;
import java.util.List;

@Data
public class OnlineQuerySqlUtils {

    /**
     * 运算符
     */
    private SearchMethodEnum symbol;
    /**
     * 逻辑拼接符号
     */
    private boolean and;
    /**
     * 组件标识
     */
    private String jnpfKey;
    /**
     * 字段key
     */
    private String vModel;
    /**
     * 自定义的值
     */
    private Object fieldValue;
    /**
     * 自定义的值2
     */
    private Object fieldValueTwo;

    private VisitableCondition sqlCondition = null;
    private BindableColumn<Object> sqlColumn = null;
    private List<AndOrCriteriaGroup> groupList = new ArrayList<>();
    private boolean isSqlServer = false;
    private boolean isOracle = false;
    private boolean isAddMatchLogic = false;

    private List<String> dataList = new ArrayList<>();

    public List<AndOrCriteriaGroup> getSuperSql(List<SuperQueryJsonModel> conditionList, List<OnlineDynamicSqlModel> sqlModelList, String databaseProductName, String matchLogic) {
        isSqlServer = databaseProductName.equalsIgnoreCase("Microsoft SQL Server");
        isOracle = databaseProductName.equalsIgnoreCase("oracle");
        isAddMatchLogic = SearchMethodEnum.And.getSymbol().equalsIgnoreCase(matchLogic);
        List<AndOrCriteriaGroup> groupQueryList = new ArrayList<>();
        OnlineProductSqlUtils.superList(conditionList,false);
        for (SuperQueryJsonModel queryJsonModel : conditionList) {
            List<FieLdsModel> fieLdsModelList = queryJsonModel.getGroups();
            String logic = queryJsonModel.getLogic();
            and = SearchMethodEnum.And.getSymbol().equalsIgnoreCase(logic);
            List<AndOrCriteriaGroup> groupListAll = new ArrayList<>();
            for (FieLdsModel fieLdsModel : fieLdsModelList) {
                ConfigModel config = fieLdsModel.getConfig();
                sqlCondition = null;
                sqlColumn = null;
                groupList = new ArrayList<>();
                jnpfKey = config.getJnpfKey();
                symbol = SearchMethodEnum.getSearchMethod(fieLdsModel.getSymbol());
                vModel = fieLdsModel.getVModel();
                fieldValue = fieLdsModel.getFieldValueOne();
                fieldValueTwo = fieLdsModel.getFieldValueTwo();
                dataList = fieLdsModel.getDataList();
                String tableName = ObjectUtil.isNotEmpty(config.getRelationTable()) ? config.getRelationTable() : config.getTableName();
                OnlineDynamicSqlModel onlineDynamicSqlModel = sqlModelList.stream().filter(sql -> sql.getTableName().equals(tableName)).findFirst().orElse(null);
                if (onlineDynamicSqlModel != null) {
                    getSymbolWrapper(onlineDynamicSqlModel);
                    groupListAll.addAll(groupList);
                }
            }
            if (groupListAll.size() > 0) {
                if(isAddMatchLogic){
                    groupQueryList.add(SqlBuilder.and(DerivedColumn.of("1"), SqlBuilder.isEqualTo(and?1:2), groupListAll.toArray(new AndOrCriteriaGroup[groupListAll.size()])));
                }else{
                    groupQueryList.add(SqlBuilder.or(DerivedColumn.of("1"), SqlBuilder.isEqualTo(and?1:2), groupListAll.toArray(new AndOrCriteriaGroup[groupListAll.size()])));
                }
            }
        }
        return groupQueryList;
    }

    private void getSymbolWrapper(OnlineDynamicSqlModel onlineDynamicSqlModel) {
        SqlTable sqlTable = onlineDynamicSqlModel.getSqlTable();
        sqlColumn = sqlTable.column(vModel);
        List<AndOrCriteriaGroup> list = new ArrayList<>();
        switch (symbol) {
            case IsNull:
                sqlCondition = SqlBuilder.isNull();
                list.add(SqlBuilder.and(sqlTable.column(vModel),  sqlCondition));
                break;
            case IsNotNull:
                sqlCondition = SqlBuilder.isNotNull();
                list.add(SqlBuilder.and(sqlTable.column(vModel),  sqlCondition));
                break;
            case Equal:
                sqlCondition = SqlBuilder.isEqualTo(fieldValue);
                list.add(SqlBuilder.and(sqlTable.column(vModel), sqlCondition));
                break;
            case NotEqual:
                sqlCondition = SqlBuilder.isNotEqualTo(fieldValue);
                list.add(SqlBuilder.and(sqlTable.column(vModel), sqlCondition));
                break;
            case GreaterThan:
                sqlCondition = SqlBuilder.isGreaterThan(fieldValue);
                list.add(SqlBuilder.and(sqlTable.column(vModel), sqlCondition));
                break;
            case LessThan:
                sqlCondition = SqlBuilder.isLessThan(fieldValue);
                list.add(SqlBuilder.and(sqlTable.column(vModel), sqlCondition));
                break;
            case GreaterThanOrEqual:
                sqlCondition = SqlBuilder.isGreaterThanOrEqualTo(fieldValue);
                list.add(SqlBuilder.and(sqlTable.column(vModel), sqlCondition));
                break;
            case LessThanOrEqual:
                sqlCondition = SqlBuilder.isLessThanOrEqualTo(fieldValue);
                list.add(SqlBuilder.and(sqlTable.column(vModel), sqlCondition));
                break;
            case Like:
                if (isSqlServer) {
                    fieldValue = String.valueOf(fieldValue).replaceAll("\\[", "[[]");
                }
                sqlCondition = SqlBuilder.isLike("%" + fieldValue + "%");
                list.add(SqlBuilder.and(sqlTable.column(vModel), sqlCondition));
                break;
            case NotLike:
                if (isSqlServer) {
                    fieldValue = String.valueOf(fieldValue).replaceAll("\\[", "[[]");
                }
                sqlCondition = SqlBuilder.isNotLike("%" + fieldValue + "%");
                list.add(SqlBuilder.and(sqlTable.column(vModel), sqlCondition));
                break;
            case Included:
            case NotIncluded:
                getInWrapper(sqlTable,list);
                break;
            case Between:
                sqlCondition = SqlBuilder.isBetween(fieldValue).and(fieldValueTwo);
                list.add(SqlBuilder.and(sqlTable.column(vModel), SqlBuilder.isBetween(fieldValue).and(fieldValueTwo)));
                break;
            default:
                break;
        }
        if (list.size() > 0) {
            int n=1;
            if(symbol.equals(SearchMethodEnum.Included) || symbol.equals(SearchMethodEnum.NotIncluded) ){
                n=2;
            }
            if (and) {
                groupList.add(SqlBuilder.and(DerivedColumn.of("1"), SqlBuilder.isEqualTo(n), list.toArray(new AndOrCriteriaGroup[list.size()])));
            } else {
                groupList.add(SqlBuilder.or(DerivedColumn.of("1"), SqlBuilder.isEqualTo(n), list.toArray(new AndOrCriteriaGroup[list.size()])));
            }
        }
    }

    private void getInWrapper(SqlTable sqlTable,List<AndOrCriteriaGroup> list) {
        for (String value : dataList) {
            if (isSqlServer) {
                value = String.valueOf(value).replaceAll("\\[", "[[]");
            }
            switch (symbol) {
                case Included:
                    sqlCondition = SqlBuilder.isLike("%" + value + "%");
                    list.add(SqlBuilder.or(sqlTable.column(vModel), SqlBuilder.isLike("%" + value + "%")));
                    break;
                default:
                    sqlCondition = SqlBuilder.isNotLike("%" + value + "%");
                    list.add(SqlBuilder.or(sqlTable.column(vModel), SqlBuilder.isNotLike("%" + value + "%")));
                    break;
            }
        }
    }

}
