package jnpf.base.util;

import com.alibaba.fastjson.JSONArray;
import jnpf.database.source.DbBase;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import jnpf.util.visiual.JnpfKeyConsts;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.select.SelectModel;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class OnlineFilterUtil {
    /**
     * 表字段名和对应的表对象映射
     */
    Map<String, SqlTable> subSqlTableMap;
    /**
     * 额外参数
     */
    Map<String, Object> params;
    /**
     * 字段说明
     */
    private String fieldName;
    /**
     * 运算符
     */
    private String operator;
    /**
     * 逻辑拼接符号
     */
    private String logic;
    /**
     * 组件标识
     */
    private String jnpfKey;
    /**
     * 字段key
     */
    private String field;
    /**
     * 自定义的值
     */
    private String fieldValue;
    /**
     * 自定义的值2
     */
    private String fieldValue2;

    private List<String> selectIgnore;

    /**
     * 显示类型
     */
    private String showLevel;


    /**
     * 数据库类型
     */
    private String dbType;
    /**
     * 日期格式
     */
    private String format;

    /**
     * 数字精度
     */
    private String precision;

    /**
     * @param where    where对象
     * @param sqlTable sql表对象,默认传主表
     * @return
     */
    public QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder solveValue(QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder where, SqlTable sqlTable) {

        if (!this.preHandle()) return where;


        MyType myType = myControl(jnpfKey);
        if (fieldValue == null) {
            fieldValue = "";
        }
        try {
            ArrayList splitKey = new ArrayList<String>() {{
                add(JnpfKeyConsts.DATE);
                add(JnpfKeyConsts.TIME);
                add(JnpfKeyConsts.NUM_INPUT);
                add(JnpfKeyConsts.CREATETIME);
                add(JnpfKeyConsts.MODIFYTIME);
            }};

            if (splitKey.contains(jnpfKey) && "between".equals(operator)) {
                List<String> data = JsonUtil.getJsonToList(fieldValue, String.class);
                fieldValue = data.get(0);
                fieldValue2 = data.get(1);
            }
            selectIgnore = new ArrayList<String>() {{
                add(JnpfKeyConsts.COMSELECT);
                add(JnpfKeyConsts.ADDRESS);
                add(JnpfKeyConsts.CASCADER);
                add(JnpfKeyConsts.CHECKBOX);
                add(JnpfKeyConsts.DEPSELECT);
            }};


            String fieldKey = "";
            // 替换子表的sqlTable
            if (field.indexOf("-" ) > 0) {
                fieldKey = field.split("-" )[0];
                sqlTable = this.subSqlTableMap.get(fieldKey);
                field = field.split("-" )[1];
            }
            // 替换副表的字段
            if (field.indexOf("_jnpf_" ) > 0) {

                sqlTable = this.subSqlTableMap.get(field);
                field = field.split("_jnpf_" )[1];
            }


            myType.judge(where, sqlTable, field);
            return where;
        } catch (Exception e) {
            return where;
        }
    }

    /**
     * 前置异常或边界情况处理
     */
    private boolean preHandle() {
        if (params != null) {
            // 判断是否只需处理子副表,忽略主表
            Boolean onlySubTable = (Boolean) params.get("onlySubTable" );
            // 如果是主表
            if (onlySubTable && !field.contains("_jnpf_" ) && !field.contains("-" )) {
                return false;
            }
            // 不拼接副表
            if (onlySubTable && field.contains("_jnpf_" )) {
                return false;
            }
        }

        return true;
    }

    /**
     * 判断控件的所属类型
     *
     * @param jnpfKey 控件标识
     * @return 控件类型
     */
    public MyType myControl(String jnpfKey) {
        MyType myType;
        switch (jnpfKey) {
            case JnpfKeyConsts.COM_INPUT:
            case JnpfKeyConsts.TEXTAREA:
            case JnpfKeyConsts.BILLRULE:
            case JnpfKeyConsts.POPUPTABLESELECT:
            case JnpfKeyConsts.RELATIONFORM:
            case JnpfKeyConsts.RELATIONFORM_ATTR:
            case JnpfKeyConsts.POPUPSELECT:
            case JnpfKeyConsts.POPUPSELECT_ATTR:
                myType = new BasicControl();
                break;
            case JnpfKeyConsts.CALCULATE:
            case JnpfKeyConsts.NUM_INPUT:
                myType = new NumControl();
                break;
            case JnpfKeyConsts.DATE:
            case JnpfKeyConsts.CREATETIME:
            case JnpfKeyConsts.MODIFYTIME:
                myType = new DateControl();
                break;
            case JnpfKeyConsts.TIME:
                myType = new TimeControl();
                break;
            default:
                myType = new SelectControl();
        }
        return myType;
    }


    /**
     * 基础类型
     */
    class BasicControl extends MyType {

        @Override
        void judge(QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder where, SqlTable sqlTable, String field) {
            if ("&&".equals(logic)) {
                switch (operator) {
                    case "null":
                        List<AndOrCriteriaGroup> group = new ArrayList<>();
                        group.add(SqlBuilder.or(sqlTable.column(field), SqlBuilder.isEqualTo("" )));
                        where.and(sqlTable.column(field), SqlBuilder.isNull(), group);
                        break;
                    case "notNull":
                        List<AndOrCriteriaGroup> group2 = new ArrayList<>();
                        group2.add(SqlBuilder.and(sqlTable.column(field), SqlBuilder.isNotEqualTo("" )));
                        where.and(sqlTable.column(field), SqlBuilder.isNotNull(), group2);
                        break;
                    case "==":
                        where.and(sqlTable.column(field), SqlBuilder.isEqualTo(fieldValue));
                        break;
                    case "<>":
                        where.and(sqlTable.column(field), SqlBuilder.isNotEqualTo(fieldValue));
                        break;
                    case "like":
                        convertSqlServerLike();
                        where.and(sqlTable.column(field), SqlBuilder.isLike("%" + fieldValue + "%" ));
                        break;
                    case "notLike":
                        convertSqlServerLike();
                        where.and(sqlTable.column(field), SqlBuilder.isNotLike("%" + fieldValue + "%" ));
                        break;
                }

            } else {
                switch (operator) {
                    case "null":
                        List<AndOrCriteriaGroup> group = new ArrayList<>();
                        group.add(SqlBuilder.or(sqlTable.column(field), SqlBuilder.isEqualTo("" )));
                        where.or(sqlTable.column(field), SqlBuilder.isNull(), group);
                        break;
                    case "notNull":
                        List<AndOrCriteriaGroup> group2 = new ArrayList<>();
                        group2.add(SqlBuilder.and(sqlTable.column(field), SqlBuilder.isNotEqualTo("" )));
                        where.or(sqlTable.column(field), SqlBuilder.isNotNull(), group2);
                        break;
                    case "==":
                        where.or(sqlTable.column(field), SqlBuilder.isEqualTo(fieldValue));
                        break;
                    case "<>":

                        where.or(sqlTable.column(field), SqlBuilder.isNotEqualTo(fieldValue));

                        break;
                    case "like":
                        convertSqlServerLike();
                        where.or(sqlTable.column(field), SqlBuilder.isLike("%" + fieldValue + "%" ));
                        break;
                    case "notLike":
                        convertSqlServerLike();
                        where.or(sqlTable.column(field), SqlBuilder.isNotLike("%" + fieldValue + "%" ));
                        break;
                }


            }
        }
    }

    class NumControl extends MyType {
        @Override
        void judge(QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder where, SqlTable sqlTable, String field) {
            // 转换数字类型;
            BigDecimal num1 = null;
            BigDecimal num2 = null;
            if(StringUtil.isNotEmpty(fieldValue)){
                num1 = new BigDecimal(fieldValue);
            }
            if (StringUtil.isNotEmpty(fieldValue2)) {
                num2 = new BigDecimal(fieldValue2);
            }
            // 精度处理
            String fieldPrecisionValue;
            String fieldPrecisionValue2;
            if (StringUtils.isNotBlank(precision)) {
                String zeroNum = "0." + StringUtils.repeat("0" , Integer.parseInt(precision));
                DecimalFormat numFormat = new DecimalFormat(zeroNum);
                fieldPrecisionValue = numFormat.format(new BigDecimal(fieldValue));
                num1 = new BigDecimal(fieldPrecisionValue);
                if (fieldValue2 != null) {
                    fieldPrecisionValue2 = numFormat.format(new BigDecimal(fieldValue2));
                    num2 = new BigDecimal(fieldPrecisionValue2);
                }
            }

            if ("&&".equals(logic)) {
                switch (operator) {
                    case "null":
                        List<AndOrCriteriaGroup> group = new ArrayList<>();
                        where.and(sqlTable.column(field), SqlBuilder.isNull(), group);
                        break;
                    case "notNull":
                        List<AndOrCriteriaGroup> group2 = new ArrayList<>();
                        where.and(sqlTable.column(field), SqlBuilder.isNotNull(), group2);
                        break;
                    case "==":
                        where.and(sqlTable.column(field), SqlBuilder.isEqualTo(num1));
                        break;
                    case "<>":

                        where.and(sqlTable.column(field), SqlBuilder.isNotEqualTo(num1));

                        break;
                    case ">":
                        where.and(sqlTable.column(field), SqlBuilder.isGreaterThan(num1));

                        break;
                    case "<":
                        where.and(sqlTable.column(field), SqlBuilder.isLessThan(num1));
                        break;
                    case ">=":
                        where.and(sqlTable.column(field), SqlBuilder.isGreaterThanOrEqualTo(num1));
                        break;
                    case "<=":
                        where.and(sqlTable.column(field), SqlBuilder.isLessThanOrEqualTo(num1));
                        break;
                    case "between":
                        where.and(sqlTable.column(field), SqlBuilder.isGreaterThanOrEqualTo(num1));
                        where.and(sqlTable.column(field), SqlBuilder.isLessThanOrEqualTo(num2));
                        break;
                }
            } else {
                switch (operator) {
                    case "null":
                        List<AndOrCriteriaGroup> group = new ArrayList<>();
                        where.or(sqlTable.column(field), SqlBuilder.isNull(), group);
                        break;
                    case "notNull":
                        List<AndOrCriteriaGroup> group2 = new ArrayList<>();
                        where.or(sqlTable.column(field), SqlBuilder.isNotNull(), group2);
                        break;
                    case "==":
                        where.or(sqlTable.column(field), SqlBuilder.isEqualTo(num1));
                        break;
                    case "<>":
                        where.or(sqlTable.column(field), SqlBuilder.isNotEqualTo(num1));

                        break;
                    case ">":
                        where.or(sqlTable.column(field), SqlBuilder.isGreaterThan(num1));
                        break;
                    case "<":
                        where.or(sqlTable.column(field), SqlBuilder.isLessThan(num1));
                        break;
                    case ">=":
                        where.or(sqlTable.column(field), SqlBuilder.isGreaterThanOrEqualTo(num1));
                        break;
                    case "<=":
                        where.or(sqlTable.column(field), SqlBuilder.isLessThanOrEqualTo(num1));
                        break;
                    case "between":
                        where.or(sqlTable.column(field), SqlBuilder.isGreaterThanOrEqualTo(num1));
                        where.and(sqlTable.column(field), SqlBuilder.isLessThanOrEqualTo(num2));
                        break;
                }
            }
        }


    }

    class DateControl extends MyType {
        @Override
        void judge(QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder where, SqlTable sqlTable, String field) {
            Long time = null;
            Long time2 = null;
            Date date = new Date();
            Date date2 = new Date();
            if (StringUtils.isNoneBlank(fieldValue)) {
                time = Long.valueOf(fieldValue);
                date = new Date(time);
            }
            if (StringUtils.isNoneBlank(fieldValue2)) {
                time2 = Long.valueOf(fieldValue2);
                // 日期类型的要加上当天的23:59:59
                if(JnpfKeyConsts.DATE.equals(jnpfKey)){
                    date2 = new Date(time2+ 60 * 60 * 24 * 1000-1000);
                }else{
                    date2 = new Date(time2);
                }

            }

            if ("&&".equals(logic)) {
                switch (operator) {
                    case "null":
                        List<AndOrCriteriaGroup> group = new ArrayList<>();
                        where.and(sqlTable.column(field), SqlBuilder.isNull(), group);
                        break;
                    case "notNull":
                        List<AndOrCriteriaGroup> group2 = new ArrayList<>();
                        where.and(sqlTable.column(field), SqlBuilder.isNotNull(), group2);
                        break;
                    case "==":
                        where.and(sqlTable.column(field), SqlBuilder.isEqualTo(date));
                        break;
                    case "<>":
                        where.and(sqlTable.column(field), SqlBuilder.isNotEqualTo(date));
                        break;
                    case ">":
                        where.and(sqlTable.column(field), SqlBuilder.isGreaterThan(date));
                        break;
                    case "<":
                        where.and(sqlTable.column(field), SqlBuilder.isLessThan(date));
                        break;
                    case ">=":
                        where.and(sqlTable.column(field), SqlBuilder.isGreaterThanOrEqualTo(date));
                        break;
                    case "<=":
                        where.and(sqlTable.column(field), SqlBuilder.isLessThanOrEqualTo(date));
                        break;
                    case "between":
                        where.and(sqlTable.column(field), SqlBuilder.isGreaterThanOrEqualTo(date));
                        where.and(sqlTable.column(field), SqlBuilder.isLessThanOrEqualTo(date2));
                        break;
                }


            } else {
                switch (operator) {
                    case "null":
                        List<AndOrCriteriaGroup> group = new ArrayList<>();
                        where.or(sqlTable.column(field), SqlBuilder.isNull(), group);
                        break;
                    case "notNull":
                        List<AndOrCriteriaGroup> group2 = new ArrayList<>();
                        where.or(sqlTable.column(field), SqlBuilder.isNotNull(), group2);
                        break;
                    case "==":
                        where.or(sqlTable.column(field), SqlBuilder.isEqualTo(date));
                        break;
                    case "<>":
                        where.or(sqlTable.column(field), SqlBuilder.isNotEqualTo(date));
                        break;
                    case ">":
                        where.or(sqlTable.column(field), SqlBuilder.isGreaterThan(date));
                        break;
                    case "<":
                        where.or(sqlTable.column(field), SqlBuilder.isLessThan(date));
                        break;
                    case ">=":
                        where.or(sqlTable.column(field), SqlBuilder.isGreaterThanOrEqualTo(date));
                        break;
                    case "<=":
                        where.or(sqlTable.column(field), SqlBuilder.isLessThanOrEqualTo(date));
                        break;
                    case "between":
                        where.or(sqlTable.column(field), SqlBuilder.isGreaterThanOrEqualTo(date));
                        where.and(sqlTable.column(field), SqlBuilder.isLessThanOrEqualTo(date2));
                        break;
                }


            }
        }

    }

    class TimeControl extends MyType {
        @Override
        void judge(QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder where, SqlTable sqlTable, String field) {
            if ("&&".equals(logic)) {
                switch (operator) {
                    case "null":
                        List<AndOrCriteriaGroup> group = new ArrayList<>();
                        where.and(sqlTable.column(field), SqlBuilder.isNull(), group);
                        break;
                    case "notNull":
                        List<AndOrCriteriaGroup> group2 = new ArrayList<>();
                        where.and(sqlTable.column(field), SqlBuilder.isNotNull(), group2);
                        break;
                    case "==":
                        where.and(sqlTable.column(field), SqlBuilder.isEqualTo(fieldValue));
                        break;
                    case "<>":
                        where.and(sqlTable.column(field), SqlBuilder.isNotEqualTo(fieldValue));
                        break;
                    case ">":
                        where.and(sqlTable.column(field), SqlBuilder.isGreaterThan(fieldValue));
                        break;
                    case "<":
                        where.and(sqlTable.column(field), SqlBuilder.isLessThan(fieldValue));
                        break;
                    case ">=":
                        where.and(sqlTable.column(field), SqlBuilder.isGreaterThanOrEqualTo(fieldValue));
                        break;
                    case "<=":
                        where.and(sqlTable.column(field), SqlBuilder.isLessThanOrEqualTo(fieldValue));
                        break;
                    case "between":
                        where.and(sqlTable.column(field), SqlBuilder.isGreaterThanOrEqualTo(fieldValue));
                        where.and(sqlTable.column(field), SqlBuilder.isLessThanOrEqualTo(fieldValue2));
                        break;
                }

            } else {
                switch (operator) {
                    case "null":
                        List<AndOrCriteriaGroup> group = new ArrayList<>();
                        where.or(sqlTable.column(field), SqlBuilder.isNull(), group);
                        break;
                    case "notNull":
                        List<AndOrCriteriaGroup> group2 = new ArrayList<>();
                        where.or(sqlTable.column(field), SqlBuilder.isNotNull(), group2);
                        break;
                    case "==":
                        where.or(sqlTable.column(field), SqlBuilder.isEqualTo(fieldValue));
                        break;
                    case "<>":
                        where.or(sqlTable.column(field), SqlBuilder.isNotEqualTo(fieldValue));
                        break;
                    case ">":
                        where.or(sqlTable.column(field), SqlBuilder.isGreaterThan(fieldValue));
                        break;
                    case "<":
                        where.or(sqlTable.column(field), SqlBuilder.isLessThan(fieldValue));
                        break;
                    case ">=":
                        where.or(sqlTable.column(field), SqlBuilder.isGreaterThanOrEqualTo(fieldValue));
                        break;
                    case "<=":
                        where.or(sqlTable.column(field), SqlBuilder.isLessThanOrEqualTo(fieldValue));
                        break;
                    case "between":
                        where.or(sqlTable.column(field), SqlBuilder.isGreaterThanOrEqualTo(fieldValue));
                        where.and(sqlTable.column(field), SqlBuilder.isLessThanOrEqualTo(fieldValue2));
                        break;
                }

            }
        }
    }

    /**
     * 下拉控件类型
     */
    class SelectControl extends MyType {
        @Override
        void judge(QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder where, SqlTable sqlTable, String field) {
            List list = new ArrayList<>();

            if (selectIgnore.contains(jnpfKey) && StringUtils.isBlank(fieldValue)) {
                fieldValue = "[]";
            }
            if ("&&".equals(logic)) {
                switch (operator) {
                    case "null":
                        List<AndOrCriteriaGroup> group = new ArrayList<>();
                        if(!DbBase.ORACLE.equals(dbType)){
                            group.add(SqlBuilder.or(sqlTable.column(field), SqlBuilder.isEqualTo("")));
                        }
                        group.add(SqlBuilder.or(sqlTable.column(field), SqlBuilder.isEqualTo("[]")));
                        where.and(sqlTable.column(field), SqlBuilder.isNull(), group);
                        break;
                    case "notNull":
                        List<AndOrCriteriaGroup> group2 = new ArrayList<>();
                        if(!DbBase.ORACLE.equals(dbType)){
                            group2.add(SqlBuilder.and(sqlTable.column(field), SqlBuilder.isNotEqualTo("")));
                        }
                        group2.add(SqlBuilder.and(sqlTable.column(field), SqlBuilder.isNotEqualTo("[]")));
                        where.and(sqlTable.column(field), SqlBuilder.isNotNull(), group2);
                        break;
                    case "==":
                        convertSqlServerLike();
                        where.and(sqlTable.column(field), SqlBuilder.isLike(fieldValue));
                        break;
                    case "<>":
                        where.and(sqlTable.column(field), SqlBuilder.isNotLike(fieldValue));
                        break;
                    case "like":
                        convertSqlServerLike();
                        where.and(sqlTable.column(field), SqlBuilder.isLike("%" + fieldValue + "%" ));
                        break;
                    case "notLike":
                        convertSqlServerLike();
                        where.and(sqlTable.column(field), SqlBuilder.isNotLike("%" + fieldValue + "%" ));
                        break;
                    case "in":
                        List<String> dataList = this.solveListValue(fieldValue);
                        if (dataList.size() > 0) {
                            List<AndOrCriteriaGroup> group3 = new ArrayList<>();
                            String valueFirst = "";
                            for (int i = 0; i < dataList.size(); i++) {
                                String value = dataList.get(i);
                                value = convertSqlServerLike(value);
                                AndOrCriteriaGroup condition = null;
                                if (i == 0) {
                                    valueFirst = value;
                                } else {
                                    condition = SqlBuilder.or(sqlTable.column(field), SqlBuilder.isLike("%" + value + "%" ));
                                    group3.add(condition);
                                }

                            }
                            where.and(sqlTable.column(field), SqlBuilder.isLike("%" + valueFirst + "%" ), group3);
                        }
                        if(JnpfKeyConsts.CASCADER.equals(jnpfKey) || JnpfKeyConsts.COMSELECT.equals(jnpfKey) || JnpfKeyConsts.ADDRESS.equals(jnpfKey)){
                            where.and(sqlTable.column(field), SqlBuilder.isNotNull());
                            where.and(sqlTable.column(field), SqlBuilder.isNotEqualTo("[]"));
                        }
                        break;
                    case "notIn":
                        List<String> dataList2 = this.solveListValue(fieldValue);
                        if (dataList2.size() > 0) {
                            for (int i = 0; i < dataList2.size(); i++) {
                                String value = dataList2.get(i);
                                where.and(sqlTable.column(field), SqlBuilder.isNotLike("%" + value + "%" ));
                            }
                        }
                        if(JnpfKeyConsts.CASCADER.equals(jnpfKey) || JnpfKeyConsts.COMSELECT.equals(jnpfKey) || JnpfKeyConsts.ADDRESS.equals(jnpfKey)){
                            where.and(sqlTable.column(field), SqlBuilder.isNotNull());
                            where.and(sqlTable.column(field), SqlBuilder.isNotEqualTo("[]"));
                        }
                        break;
                }

            } else {

                switch (operator) {
                    case "null":
                        List<AndOrCriteriaGroup> group = new ArrayList<>();
                        if(!DbBase.ORACLE.equals(dbType)){
                            group.add(SqlBuilder.or(sqlTable.column(field), SqlBuilder.isEqualTo("")));
                        }
                        group.add(SqlBuilder.or(sqlTable.column(field), SqlBuilder.isEqualTo("[]")));
                        where.or(sqlTable.column(field), SqlBuilder.isNull(), group);
                        break;
                    case "notNull":
                        List<AndOrCriteriaGroup> group2 = new ArrayList<>();
                        if(!DbBase.ORACLE.equals(dbType)){
                            group2.add(SqlBuilder.and(sqlTable.column(field), SqlBuilder.isNotEqualTo("")));
                        }
                        group2.add(SqlBuilder.and(sqlTable.column(field), SqlBuilder.isNotEqualTo("[]")));
                        where.or(sqlTable.column(field), SqlBuilder.isNotNull(), group2);
                        break;
                    case "==":
                        where.or(sqlTable.column(field), SqlBuilder.isEqualTo(fieldValue));
                        break;
                    case "<>":

                        where.or(sqlTable.column(field), SqlBuilder.isNotEqualTo(fieldValue));
                        break;
                    case "like":
                        convertSqlServerLike();
                        where.or(sqlTable.column(field), SqlBuilder.isLike("%" + fieldValue + "%" ));
                        break;
                    case "notLike":
                        convertSqlServerLike();
                        where.or(sqlTable.column(field), SqlBuilder.isNotLike("%" + fieldValue + "%" ));
                        break;
                    case "in":
                        if (selectIgnore.contains(jnpfKey)) {
                            convertSqlServerLike();
                            where.or(sqlTable.column(field), SqlBuilder.isLike(fieldValue));
                        } else {
                            if (list.size() > 0) {
                                where.or(sqlTable.column(field), SqlBuilder.isIn(list));
                            }

                        }
                        if(JnpfKeyConsts.CASCADER.equals(jnpfKey) || JnpfKeyConsts.COMSELECT.equals(jnpfKey) || JnpfKeyConsts.ADDRESS.equals(jnpfKey)){
                            where.and(sqlTable.column(field), SqlBuilder.isNotNull());
                            where.and(sqlTable.column(field), SqlBuilder.isNotEqualTo("[]"));
                        }
                        break;
                    case "notIn":
                        if (selectIgnore.contains(jnpfKey)) {
                            List<String> data = JsonUtil.getJsonToList(fieldValue, String.class);
                            if (data.size() > 0) {
                                where.or(sqlTable.column(field), SqlBuilder.isNotLike(data));
                            }

                        } else {
                            if (list != null && list.size() > 0) {
                                where.or(sqlTable.column(field), SqlBuilder.isNotIn(list));
                            }

                        }
                        if(JnpfKeyConsts.CASCADER.equals(jnpfKey) || JnpfKeyConsts.COMSELECT.equals(jnpfKey) || JnpfKeyConsts.ADDRESS.equals(jnpfKey)){
                            where.and(sqlTable.column(field), SqlBuilder.isNotNull());
                            where.and(sqlTable.column(field), SqlBuilder.isNotEqualTo("[]"));
                        }
                        break;
                }

            }
        }

        private ArrayList<String> solveListValue(String fieldValue) {
            ArrayList<String> result = new ArrayList<>();
            try {
                List<List> list = JsonUtil.getJsonToList(fieldValue, List.class);
                for (List listSub : list) {
                    result.add(JSONArray.toJSONString(listSub));
                    // 组织选择需要取最后每个数组最后一个
                    String value = (String)listSub.get(listSub.size() - 1);
                    result.add(value);
                }

            }catch (Exception e){
                List<String> list = JsonUtil.getJsonToList(fieldValue, String.class);
                result.add(JSONArray.toJSONString(list));
                result.addAll(list);
            }
            return result;
        }

    }

    private abstract class MyType {
        abstract void judge(QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder where, SqlTable sqlTable, String field);
    }

    /**
     * SQLSERVER数据库 like括号语法
     *
     * @param val
     * @return
     */
    private String convertSqlServerLike(String val) {
        if (DbBase.SQL_SERVER.equals(dbType)) {
            val = val.replaceAll("\\[" , "[[]" );
        }
        return val;
    }

    private void convertSqlServerLike() {
        if (DbBase.SQL_SERVER.equals(dbType)) {
            fieldValue = convertSqlServerLike(fieldValue);
        }
    }


}
