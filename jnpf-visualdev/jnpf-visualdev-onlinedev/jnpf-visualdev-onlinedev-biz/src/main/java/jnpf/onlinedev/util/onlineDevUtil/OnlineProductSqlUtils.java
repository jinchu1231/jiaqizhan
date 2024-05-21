package jnpf.onlinedev.util.onlineDevUtil;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import jnpf.base.model.ColumnDataModel;
import jnpf.base.model.Template6.ColumnListField;
import jnpf.base.model.VisualDevJsonModel;
import jnpf.base.model.advancedquery.OnlineDynamicSqlModel;
import jnpf.constant.PermissionConst;
import jnpf.database.model.entity.DbLinkEntity;
import jnpf.database.model.superQuery.SuperJsonModel;
import jnpf.database.model.superQuery.SuperQueryJsonModel;
import jnpf.database.util.ConnUtil;
import jnpf.database.util.DynamicDataSourceUtil;
import jnpf.emnus.SearchMethodEnum;
import jnpf.exception.DataException;
import jnpf.model.visualJson.FieLdsModel;
import jnpf.model.visualJson.TableModel;
import jnpf.model.visualJson.analysis.FormAllModel;
import jnpf.model.visualJson.analysis.FormColumnModel;
import jnpf.model.visualJson.analysis.FormEnum;
import jnpf.model.visualJson.config.ConfigModel;
import jnpf.onlinedev.model.OnlineDevListModel.OnlineColumnChildFieldModel;
import jnpf.onlinedev.model.OnlineDevListModel.OnlineColumnFieldModel;
import jnpf.onlinedev.model.OnlineDevListModel.VisualColumnSearchVO;
import jnpf.onlinedev.model.PaginationModel;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.entity.UserRelationEntity;
import jnpf.permission.service.AuthorizeService;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.UserRelationService;
import jnpf.permission.service.UserService;
import jnpf.util.DateTimeFormatConstant;
import jnpf.util.FlowFormDataUtil;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import jnpf.util.TableFeildsEnum;
import jnpf.util.context.RequestContext;
import jnpf.util.context.SpringContext;
import jnpf.util.visiual.JnpfKeyConsts;
import lombok.Cleanup;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.DerivedColumn;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.select.SelectModel;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 生成在线sql语句
 *
 * @author JNPF开发平台组
 * @version V3.2.8
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021/11/8
 */
public class OnlineProductSqlUtils {
    private static FlowFormDataUtil flowDataUtil = SpringContext.getBean(FlowFormDataUtil.class);
    private static UserService userService = SpringContext.getBean(UserService.class);
    private static UserRelationService userRelationService = SpringContext.getBean(UserRelationService.class);
    private static OrganizeService organizeService = SpringContext.getBean(OrganizeService.class);
    private static AuthorizeService authorizeUtil = SpringContext.getBean(AuthorizeService.class);

    /**
     * 生成列表查询sql
     *
     * @param sqlModels
     * @param visualDevJsonModel
     * @param columnFieldList
     * @param linkEntity
     * @return
     */
    public static void getColumnListSql(List<OnlineDynamicSqlModel> sqlModels, VisualDevJsonModel visualDevJsonModel,
                                        List<String> columnFieldList, DbLinkEntity linkEntity) {
        List<OnlineColumnFieldModel> childFieldList;
        try {
            columnFieldList = columnFieldList.stream().distinct().collect(Collectors.toList());
            ColumnDataModel columnData = visualDevJsonModel.getColumnData();
            List<ColumnListField> modelList = JsonUtil.getJsonToList(columnData.getColumnList(), ColumnListField.class);

            @Cleanup Connection conn = ConnUtil.getConnOrDefault(linkEntity);
            List<TableModel> tableModelList = visualDevJsonModel.getVisualTables();
            String databaseProductName = conn.getMetaData().getDatabaseProductName().trim();
            boolean isClobDbType = databaseProductName.equalsIgnoreCase("oracle") || databaseProductName.equalsIgnoreCase("DM DBMS");
            //主表
            TableModel mainTable = tableModelList.stream().filter(model -> model.getTypeId().equals("1")).findFirst().orElse(null);
            //获取主键
            Integer primaryKeyPolicy = visualDevJsonModel.getFormData().getPrimaryKeyPolicy();
            String pKeyName = flowDataUtil.getKey(conn, mainTable.getTable(), primaryKeyPolicy);
            //列表中区别子表正则
            String reg = "^[jnpf_]\\S*_jnpf\\S*";

            //列表主表字段
            List<String> mainTableFields = columnFieldList.stream().filter(s -> !s.matches(reg) && !s.toLowerCase().contains(JnpfKeyConsts.CHILD_TABLE_PREFIX)).collect(Collectors.toList());
            mainTableFields.add(pKeyName);
            if (!visualDevJsonModel.isFlowEnable() && primaryKeyPolicy == 2) {
                primaryKeyPolicy = 1;
                mainTableFields.add(flowDataUtil.getKey(conn, mainTable.getTable(), primaryKeyPolicy));
            }

            if (visualDevJsonModel.getFormData().getConcurrencyLock()) {
                mainTableFields.add(TableFeildsEnum.VERSION.getField());
            }
            //有开启流程得需要查询流程引擎信息
            if (visualDevJsonModel.isFlowEnable()) {
                String s = TableFeildsEnum.FLOWID.getField();
                if (isClobDbType) {
                    s = TableFeildsEnum.FLOWID.getField().toUpperCase();
                }
                mainTableFields.add(s);
            }

            if (columnData != null && ObjectUtil.isNotEmpty(columnData.getType()) && columnData.getType() == 3) {
                String groupField = visualDevJsonModel.getColumnData().getGroupField();
                boolean contains = columnFieldList.contains(groupField);
                if (!contains) {
                    if (groupField.startsWith("jnpf_")) {
                        columnFieldList.add(groupField);
                    } else {
                        mainTableFields.add(groupField);
                    }
                }
            }

            //列表子表字段
            childFieldList = columnFieldList.stream().filter(s -> s.matches(reg)).map(child -> {
                OnlineColumnFieldModel fieldModel = new OnlineColumnFieldModel();
                String s1 = child.substring(child.lastIndexOf("jnpf_")).replace("jnpf_", "");
                String s2 = child.substring(child.indexOf("_") + 1, child.lastIndexOf("_jnpf"));
                fieldModel.setTableName(s2);
                fieldModel.setField(s1);
                fieldModel.setOriginallyField(child);
                return fieldModel;
            }).collect(Collectors.toList());

            //取列表用到的表
            List<String> ColumnTableNameList = childFieldList.stream().map(t -> t.getTableName().toLowerCase()).collect(Collectors.toList());
            List<TableModel> tableModelList1 = tableModelList.stream().filter(t -> ColumnTableNameList.contains(t.getTable().toLowerCase())).collect(Collectors.toList());
            List<OnlineColumnChildFieldModel> classifyFieldList = new ArrayList<>(10);
            for (TableModel t : tableModelList1) {
                OnlineColumnChildFieldModel childFieldModel = new OnlineColumnChildFieldModel();
                childFieldModel.setTable(t.getTable());
                childFieldModel.setRelationField(t.getRelationField());
                childFieldModel.setTableField(t.getTableField());
                classifyFieldList.add(childFieldModel);
            }

            for (OnlineDynamicSqlModel dycModel : sqlModels) {
                if (dycModel.isMain()) {
                    List<BasicColumn> mainSqlColumns = getBasicColumns(mainTableFields, sqlModels, dycModel, modelList, isClobDbType);
                    dycModel.setColumns(mainSqlColumns);
                } else {
                    if (classifyFieldList.size() > 0) {
                        Map<String, List<OnlineColumnFieldModel>> mastTableCols = childFieldList.stream().collect(Collectors.groupingBy(OnlineColumnFieldModel::getTableName));
                        List<OnlineColumnFieldModel> onlineColumnFieldModels = Optional.ofNullable(mastTableCols.get(dycModel.getTableName())).orElse(new ArrayList<>());
                        List<BasicColumn> mastSqlCols = getBasicColumnsChild(modelList, dycModel, onlineColumnFieldModels, isClobDbType);
                        dycModel.setColumns(mastSqlCols);
                    }
                }
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        } catch (DataException e) {
            e.printStackTrace();
        }
    }


    public static List<BasicColumn> getGroupBySqlTable(List<OnlineDynamicSqlModel> sqlModels, VisualDevJsonModel visualDevJsonModel,
                                                       List<String> columnFieldList, boolean isClobDbType) {
        List<OnlineColumnFieldModel> childFieldList;
        List<BasicColumn> basicColumns = new ArrayList<>();
        try {
            columnFieldList = columnFieldList.stream().distinct().collect(Collectors.toList());

            List<TableModel> tableModelList = visualDevJsonModel.getVisualTables();

            //列表中区别子表正则
            String reg = "^[jnpf_]\\S*_jnpf\\S*";

            //列表主表字段
            List<String> mainTableFields = columnFieldList.stream().filter(s -> !s.matches(reg)
                    && !s.toLowerCase().contains(JnpfKeyConsts.CHILD_TABLE_PREFIX)).collect(Collectors.toList());

            if (visualDevJsonModel.getFormData().getConcurrencyLock()) {
                mainTableFields.add(TableFeildsEnum.VERSION.getField());
            }

            //有开启流程得需要查询流程引擎信息
            if (visualDevJsonModel.isFlowEnable()) {
                String s = TableFeildsEnum.FLOWID.getField();
                if (isClobDbType) {
                    s = TableFeildsEnum.FLOWID.getField().toUpperCase();
                }
                mainTableFields.add(s);
            }

            //列表子表字段
            childFieldList = columnFieldList.stream().filter(s -> s.matches(reg)).map(child -> {
                OnlineColumnFieldModel fieldModel = new OnlineColumnFieldModel();
                String s1 = child.substring(child.lastIndexOf("jnpf_")).replace("jnpf_", "");
                String s2 = child.substring(child.indexOf("_") + 1, child.lastIndexOf("_jnpf"));
                fieldModel.setTableName(s2);
                fieldModel.setField(s1);
                fieldModel.setOriginallyField(child);
                return fieldModel;
            }).collect(Collectors.toList());

            //取列表用到的表
            List<String> ColumnTableNameList = childFieldList.stream().map(t -> t.getTableName().toLowerCase()).collect(Collectors.toList());
            List<TableModel> tableModelList1 = tableModelList.stream().filter(t -> ColumnTableNameList.contains(t.getTable().toLowerCase())).collect(Collectors.toList());
            List<OnlineColumnChildFieldModel> classifyFieldList = new ArrayList<>(10);
            for (TableModel t : tableModelList1) {
                OnlineColumnChildFieldModel childFieldModel = new OnlineColumnChildFieldModel();
                childFieldModel.setTable(t.getTable());
                childFieldModel.setRelationField(t.getRelationField());
                childFieldModel.setTableField(t.getTableField());
                classifyFieldList.add(childFieldModel);
            }

            List<ColumnListField> modelList = JsonUtil.getJsonToList(visualDevJsonModel.getColumnData().getColumnList(), ColumnListField.class);
            for (OnlineDynamicSqlModel dycModel : sqlModels) {
                if (dycModel.isMain()) {
                    List<BasicColumn> mainSqlColumns = getBasicColumns(mainTableFields, sqlModels, dycModel, modelList, isClobDbType);
                    basicColumns.addAll(mainSqlColumns);
                } else {
                    if (classifyFieldList.size() > 0) {
                        Map<String, List<OnlineColumnFieldModel>> mastTableCols = childFieldList.stream().collect(Collectors.groupingBy(OnlineColumnFieldModel::getTableName));
                        List<OnlineColumnFieldModel> onlineColumnFieldModels = Optional.ofNullable(mastTableCols.get(dycModel.getTableName())).orElse(new ArrayList<>());
                        List<BasicColumn> mastSqlCols = getBasicColumnsChild(modelList, dycModel, onlineColumnFieldModels, isClobDbType);
                        basicColumns.addAll(mastSqlCols);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return basicColumns;
    }

    /**
     *
     */
    public static void getConditionSql(QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder form, String databaseProductName, List<VisualColumnSearchVO> searchVOList,
                                       List<OnlineDynamicSqlModel> sqlModelList) throws SQLException {
        //成对注释1---进来前有切库。切回主库查询用户相关信息
        DynamicDataSourceUtil.switchToDataSource(null);
        try {
            boolean isOracle = databaseProductName.equalsIgnoreCase("oracle");
            boolean isPostgre = databaseProductName.equalsIgnoreCase("PostgreSQL");
            boolean isSqlServer = databaseProductName.equalsIgnoreCase("Microsoft SQL Server");
            for (int k = 0; k < searchVOList.size(); k++) {
                VisualColumnSearchVO vo = searchVOList.get(k);
                String jnpfKey = "jnpfkey";
                if (Objects.nonNull(vo.getConfig())) {
                    jnpfKey = vo.getConfig().getJnpfKey();
                }
                String tableName = vo.getTable();
                OnlineDynamicSqlModel sqlModel = sqlModelList.stream().filter(sql -> sql.getTableName().equals(tableName)).findFirst().orElse(null);
                SqlTable sqlTable = sqlModel.getSqlTable();
                String searchType = vo.getSearchType();
                String vModel = vo.getField();
                String format;

                boolean isSearchMultiple = vo.getSearchMultiple() != null && vo.getSearchMultiple();
                //搜索或字段其中一个为多选, 都按照多选处理
                boolean isMultiple = isSearchMultiple || vo.getMultiple() != null && vo.getMultiple();

                List<String> dataValues = new ArrayList<>();
                SqlColumn<Object> sqlColumn = sqlTable.column(vModel);
                List<AndOrCriteriaGroup> groupList = new ArrayList<>();
                if (isMultiple) {
                    Object tmpValue = vo.getValue();
                    boolean isComSelect = JnpfKeyConsts.COMSELECT.equals(jnpfKey);
                    if (isComSelect) {
                        //左侧树情况左侧条件为单选查询, 所以组织查询全部转为字符串重新处理
                        //组织单独处理, 多选是二维数组, 单选是数组
                        tmpValue = JSONArray.toJSONString(vo.getValue());
                    }
                    if (tmpValue instanceof String) {
                        //多选的情况, 若为字符串则处理成多选的字符串数组
                        tmpValue = OnlineSwapDataUtils.convertValueToString((String) tmpValue, true, isComSelect);
                        dataValues = JSON.parseArray((String) tmpValue, String.class);
                    } else {
                        dataValues = JsonUtil.getJsonToList(tmpValue, String.class);
                    }
                } else {
                    boolean isCurOrg = JnpfKeyConsts.CURRORGANIZE.equals(jnpfKey);
                    if (isCurOrg) {
                        //所属组织只保存了当前组织的ID, 没有存储组织数组, 将过滤条件中的组织数组取最后一个ID
                        vo.setValue(OnlineSwapDataUtils.getLastOrganizeId(vo.getValue()));
                    }
                    dataValues.add(vo.getValue().toString());
                }
                String value = vo.getValue().toString();
                if (isMultiple || JnpfKeyConsts.CHECKBOX.equals(jnpfKey) || JnpfKeyConsts.CASCADER.equals(jnpfKey)
                        || JnpfKeyConsts.CURRORGANIZE.equals(jnpfKey) || JnpfKeyConsts.ADDRESS.equals(jnpfKey) || JnpfKeyConsts.CUSTOMUSERSELECT.equals(jnpfKey)
                ) {
                    searchType = "2" ;
                }
                if (JnpfKeyConsts.COM_INPUT.equals(jnpfKey) || JnpfKeyConsts.TEXTAREA.equals(jnpfKey)) {
                    if ("3".equals(searchType)) {
                        searchType = "2" ;
                    }
                }
                if ("1".equals(searchType)) {
                    form.and(sqlColumn, SqlBuilder.isEqualTo(value));
                } else if ("2".equals(searchType)) {
                    if (JnpfKeyConsts.CUSTOMUSERSELECT.equals(jnpfKey)) {
                        // 分组 组织 岗位 角色 用户
                        for (String userVal : dataValues) {
                            convertUserSelectData(sqlColumn, groupList, userVal,true);
                        }
                    }
                    if (isMultiple) {
                        for (String val : dataValues) {
                            if (isSqlServer) {
                                val = val.replaceAll("\\[", "[[]");
                            }
                            groupList.add(SqlBuilder.or(sqlColumn, SqlBuilder.isLike("%" + val + "%")));
                        }
                    }
                    if (isSqlServer) {
                        value = value.replaceAll("\\[", "[[]");
                    }
                    value = "%" + value + "%" ;
                    if (groupList.size() > 0) {
                        form.and(sqlColumn, SqlBuilder.isLike(value), groupList);
                    } else {
                        form.and(sqlColumn, SqlBuilder.isLike(value));
                    }
                } else if ("3".equals(searchType)) {
                    switch (jnpfKey) {
                        case JnpfKeyConsts.MODIFYTIME:
                        case JnpfKeyConsts.CREATETIME:
                        case JnpfKeyConsts.DATE:
                            JSONArray timeStampArray = (JSONArray) vo.getValue();
                            Long o1 = (Long) timeStampArray.get(0);
                            Long o2 = (Long) timeStampArray.get(1);
                            format = StringUtil.isEmpty(vo.getFormat()) ? "yyyy-MM-dd HH:mm:ss" : DateTimeFormatConstant.getFormat(vo.getFormat());
                            //时间戳转string格式
                            String startTime = OnlinePublicUtils.getDateByFormat(o1, format);
                            String endTime = OnlinePublicUtils.getDateByFormat(o2, format);
                            //处理创建和修改时间查询条件范围
                            if (JnpfKeyConsts.CREATETIME.equals(jnpfKey) || JnpfKeyConsts.MODIFYTIME.equals(jnpfKey)) {
                                endTime = endTime.substring(0, 10);
                            }
                            String firstTimeDate = OnlineDatabaseUtils.getTimeFormat(startTime);
                            String lastTimeDate = OnlineDatabaseUtils.getLastTimeFormat(endTime);
                            if (isOracle || isPostgre) {
                                form.and(sqlTable.column(vModel), SqlBuilder.isBetween(new Date(o1)).and(new Date(o2)));
                            } else {
                                form.and(sqlTable.column(vModel), SqlBuilder.isBetween(firstTimeDate).and(lastTimeDate));
                            }
                            break;
                        case JnpfKeyConsts.TIME:
                            List<String> stringList = JsonUtil.getJsonToList(value, String.class);
                            form.and(sqlTable.column(vModel), SqlBuilder.isBetween(stringList.get(0)).and(stringList.get(1)));
                            break;
                        case JnpfKeyConsts.NUM_INPUT:
                        case JnpfKeyConsts.CALCULATE:
                            BigDecimal firstValue = null;
                            BigDecimal secondValue = null;
                            JSONArray objects = (JSONArray) vo.getValue();
                            for (int i = 0; i < objects.size(); i++) {
                                Object n = objects.get(i);
                                if (ObjectUtil.isNotEmpty(n)) {
                                    if (i == 0) {
                                        firstValue = new BigDecimal(String.valueOf(n));
                                    } else {
                                        secondValue = new BigDecimal(String.valueOf(n));
                                    }
                                }
                            }
                            if (firstValue != null) {
                                form.and(sqlTable.column(vModel), SqlBuilder.isGreaterThanOrEqualTo(firstValue));
                            }
                            if (secondValue != null) {
                                form.and(sqlTable.column(vModel), SqlBuilder.isLessThanOrEqualTo(secondValue));
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        } finally {
            //成对注释1---完成后。切回数据库
            DynamicDataSourceUtil.clearSwitchDataSource();
        }
    }

    /**
     * 封装搜索数据
     */
    public static void queryList(List<FormAllModel> formAllModel, VisualDevJsonModel visualDevJsonModel, PaginationModel paginationModel) {
        String moduleId = paginationModel.getMenuId();
        boolean isApp = !RequestContext.isOrignPc();
        ColumnDataModel columnData = isApp ? visualDevJsonModel.getAppColumnData() : visualDevJsonModel.getColumnData();
        //数据权限
        if(StringUtil.isNotEmpty(moduleId) && columnData.getUseDataPermission()!=null && columnData.getUseDataPermission()) {
            List<SuperJsonModel> authorizeListAll = authorizeUtil.getConditionSql(moduleId);
            for (SuperJsonModel superJsonModel : authorizeListAll) {
                List<SuperQueryJsonModel> conditionList = superJsonModel.getConditionList();
                for (SuperQueryJsonModel superQueryJsonModel : conditionList) {
                    List<FieLdsModel> fieLdsModelList = superQueryJsonModel.getGroups();
                    for (FieLdsModel fieLdsModel : fieLdsModelList) {
                        tabelName(fieLdsModel, formAllModel);
                    }
                }
            }
            visualDevJsonModel.setAuthorize(authorizeListAll);
        }

        //数据过滤
        if(columnData != null){
            SuperJsonModel ruleList = isApp ? columnData.getRuleListApp() : columnData.getRuleList();
            SuperJsonModel ruleJsonModel = ruleList != null ? ruleList : new SuperJsonModel();
            List<SuperQueryJsonModel> ruleJsonModelList = ruleJsonModel.getConditionList();
            for (SuperQueryJsonModel ruleQueryModel : ruleJsonModelList) {
                List<FieLdsModel> fieLdsModelList = ruleQueryModel.getGroups();
                for (FieLdsModel fieLdsModel : fieLdsModelList) {
                    fieLdsModel.setVModel(fieLdsModel.getId());
                    tabelName(fieLdsModel, formAllModel);
                }
            }
            visualDevJsonModel.setRuleQuery(ruleJsonModel);
        }

        //高级搜索
        String superQueryJson = paginationModel.getSuperQueryJson();
        if (StringUtil.isNotEmpty(superQueryJson)) {
            SuperJsonModel queryJsonModel = JsonUtil.getJsonToBean(superQueryJson, SuperJsonModel.class);
            List<SuperQueryJsonModel> superQueryListAll = queryJsonModel.getConditionList();
            for (SuperQueryJsonModel superQueryJsonModel : superQueryListAll) {
                List<FieLdsModel> fieLdsModelList = superQueryJsonModel.getGroups();
                for (FieLdsModel fieLdsModel : fieLdsModelList) {
                    fieLdsModel.setVModel(fieLdsModel.getId());
                    tabelName(fieLdsModel, formAllModel);
                }
            }
            visualDevJsonModel.setSuperQuery(queryJsonModel);
        }

        //列表搜索
        String queryJson = paginationModel.getQueryJson();
        if (StringUtil.isNotEmpty(queryJson)) {
            Map<String, Object> keyJsonMap = JsonUtil.stringToMap(queryJson);
            List<FieLdsModel> searchVOListAll = JsonUtil.getJsonToList(columnData.getSearchList(), FieLdsModel.class);
            searchVOListAll.addAll(treeRelation(columnData, formAllModel));
            List<FieLdsModel> searchVOList = new ArrayList<>();
            for (String model : keyJsonMap.keySet()) {
                FieLdsModel fieLdsModel = searchVOListAll.stream().filter(t -> model.equals(t.getId())).findFirst().orElse(null);
                Object object = keyJsonMap.get(model);
                if (fieLdsModel != null) {
                    ConfigModel config = fieLdsModel.getConfig();
                    Integer searchType = fieLdsModel.getSearchType();
                    String jnpfKey = config.getJnpfKey();
                    //模糊搜索
                    boolean isMultiple = fieLdsModel.getSearchMultiple() || fieLdsModel.getMultiple();
                    List<String> type = JnpfKeyConsts.SelectIgnore;
                    //文本框搜索
                    List<String> base = JnpfKeyConsts.BaseSelect;
                    if (isMultiple || type.contains(jnpfKey)) {
                        if(object instanceof String){
                            object = new ArrayList(){{add(String.valueOf(keyJsonMap.get(model)));}};
                        }
                        searchType = 4;
                    }
                    if (base.contains(jnpfKey) && searchType == 3) {
                        searchType = 2;
                    }
                    String symbol = searchType == 1 ? SearchMethodEnum.Equal.getSymbol() : searchType == 3 ? SearchMethodEnum.Between.getSymbol() : searchType == 2 ? SearchMethodEnum.Like.getSymbol() : SearchMethodEnum.Included.getSymbol();
                    fieLdsModel.setSymbol(symbol);
                    if (object instanceof List) {
                        fieLdsModel.setFieldValue(JsonUtil.getObjectToString(object));
                    } else {
                        fieLdsModel.setFieldValue(String.valueOf(object));
                    }
                    tabelName(fieLdsModel, formAllModel);
                    searchVOList.add(fieLdsModel);
                }
            }
            SuperQueryJsonModel queryJsonModel = new SuperQueryJsonModel();
            queryJsonModel.setGroups(searchVOList);

            SuperJsonModel superJsonModel = new SuperJsonModel();
            superJsonModel.setConditionList(new ArrayList() {{
                add(queryJsonModel);
            }});
            visualDevJsonModel.setQuery(superJsonModel);
        }

        //keyword 关键词搜索
        if (StringUtil.isNotEmpty(queryJson)) {
            Map<String, Object> keyJsonMap = JsonUtil.stringToMap(queryJson);
            if(keyJsonMap.get(JnpfKeyConsts.JNPFKEYWORD) != null){
                String keyWord = String.valueOf(keyJsonMap.get(JnpfKeyConsts.JNPFKEYWORD));
                List<FieLdsModel> searchVOListAll = JsonUtil.getJsonToList(columnData.getSearchList(), FieLdsModel.class);
                List<FieLdsModel> collect = searchVOListAll.stream().filter(t -> t.getIsKeyword()).collect(Collectors.toList());
                List<FieLdsModel> searchVOList = new ArrayList<>();
                for(FieLdsModel fieLdsModel:collect){
                    fieLdsModel.setFieldValue(keyWord);
                    fieLdsModel.setSymbol(SearchMethodEnum.Like.getSymbol());
                    tabelName(fieLdsModel, formAllModel);
                    searchVOList.add(fieLdsModel);
                }
                SuperQueryJsonModel queryJsonModel = new SuperQueryJsonModel();
                queryJsonModel.setLogic( SearchMethodEnum.Or.getSymbol());
                queryJsonModel.setGroups(searchVOList);
                SuperJsonModel superJsonModel = new SuperJsonModel();
                superJsonModel.setConditionList(new ArrayList() {{
                    add(queryJsonModel);
                }});
                visualDevJsonModel.setKeyQuery(superJsonModel);
            }
        }
    }

    /**
     * 赋值表名
     *
     * @param fieLdsModel
     * @param formAllModel
     */
    private static void tabelName(FieLdsModel fieLdsModel, List<FormAllModel> formAllModel) {
        //主表数据
        List<FormAllModel> mast = formAllModel.stream().filter(t -> FormEnum.mast.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        //列表子表数据
        List<FormAllModel> mastTable = formAllModel.stream().filter(t -> FormEnum.mastTable.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        //子表
        List<FormAllModel> childTable = formAllModel.stream().filter(t -> FormEnum.table.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        if (fieLdsModel != null) {
            String vModel = fieLdsModel.getId().split("-")[0];
            String field = fieLdsModel.getId().split("-").length > 1 ? fieLdsModel.getId().split("-")[1] : "";
            FormAllModel mastModel = mast.stream().filter(t -> vModel.equals(t.getFormColumnModel().getFieLdsModel().getVModel())).findFirst().orElse(null);
            if (mastModel != null) {
                fieLdsModel.getConfig().setTableName(mastModel.getFormColumnModel().getFieLdsModel().getConfig().getTableName());
                fieLdsModel.getConfig().setJnpfKey(mastModel.getFormColumnModel().getFieLdsModel().getConfig().getJnpfKey());
            }
            FormAllModel mastTableModel = mastTable.stream().filter(t -> vModel.equals(t.getFormMastTableModel().getVModel())).findFirst().orElse(null);
            if (mastTableModel != null) {
                fieLdsModel.getConfig().setTableName(mastTableModel.getFormMastTableModel().getMastTable().getFieLdsModel().getConfig().getTableName());
                fieLdsModel.getConfig().setJnpfKey(mastTableModel.getFormMastTableModel().getMastTable().getFieLdsModel().getConfig().getJnpfKey());
            }
            FormAllModel childTableModel = childTable.stream().filter(t -> vModel.equals(t.getChildList().getTableModel())).findFirst().orElse(null);
            if (childTableModel != null) {
                fieLdsModel.getConfig().setTableName(childTableModel.getChildList().getTableName());
                List<FormColumnModel> childList = childTableModel.getChildList().getChildList();
                for (FormColumnModel formColumnModel : childList) {
                    FieLdsModel childFieLdsModel = formColumnModel.getFieLdsModel();
                    String childModel = childFieLdsModel.getVModel();
                    if (childModel.equals(field)) {
                        fieLdsModel.getConfig().setJnpfKey(childFieLdsModel.getConfig().getJnpfKey());
                    }
                }
            }
            convertUserSelectData(fieLdsModel);
        }
    }

    private static void convertUserSelectData(FieLdsModel fieLdsModel){
        List<String> symbolList = new ArrayList<>();
        symbolList.add(SearchMethodEnum.Equal.getSymbol());
        symbolList.add(SearchMethodEnum.NotEqual.getSymbol());
        String jnpfKey = fieLdsModel.getConfig().getJnpfKey();
        String fieldValue = fieLdsModel.getFieldValue();
        String symbol = fieLdsModel.getSymbol();
        if(StringUtil.isNotEmpty(fieldValue)){
            switch (jnpfKey){
                case JnpfKeyConsts.CUSTOMUSERSELECT:
                    if(!symbolList.contains(symbol)){
                        List<String> dataValues =   new ArrayList<>();
                        List<String> values =   new ArrayList<>();
                        try {
                            values = JsonUtil.getJsonToList(fieldValue,String.class);
                        }catch (Exception e){
                        }
                        dataValues.addAll(values);
                        for(String userVal :values){
                            String userValue = userVal.substring(0, userVal.indexOf("--"));
                            UserEntity userEntity = userService.getInfo(userValue);
                            if (userEntity != null) {
                                dataValues.add(userValue);
                                //在用户关系表中取出
                                List<UserRelationEntity> groupRel = Optional.ofNullable(userRelationService.getListByUserId(userValue, PermissionConst.GROUP)).orElse(new ArrayList<>());
                                List<UserRelationEntity> orgRel = Optional.ofNullable(userRelationService.getListByUserId(userValue, PermissionConst.ORGANIZE)).orElse(new ArrayList<>());
                                List<UserRelationEntity> posRel = Optional.ofNullable(userRelationService.getListByUserId(userValue, PermissionConst.POSITION)).orElse(new ArrayList<>());
                                List<UserRelationEntity> roleRel = Optional.ofNullable(userRelationService.getListByUserId(userValue, PermissionConst.ROLE)).orElse(new ArrayList<>());
                                if (groupRel.size() > 0) {
                                    for (UserRelationEntity split : groupRel) {
                                        dataValues.add(split.getObjectId());
                                    }
                                }
                                if (StringUtil.isNotEmpty(userEntity.getOrganizeId())) {
                                    //向上递归 查出所有上级组织
                                    List<String> allUpOrgIDs = new ArrayList<>();
                                    organizeService.upWardRecursion(allUpOrgIDs,userEntity.getOrganizeId());
                                    for (String orgID : allUpOrgIDs) {
                                        dataValues.add(orgID);
                                    }
                                }
                                if (posRel.size() > 0) {
                                    for (UserRelationEntity split : posRel) {
                                        dataValues.add(split.getObjectId());
                                    }
                                }
                                if (roleRel.size() > 0) {
                                    for (UserRelationEntity split : roleRel) {
                                        dataValues.add(split.getObjectId());
                                    }
                                }
                            }
                        }
                        fieLdsModel.setFieldValue(JsonUtil.getObjectToString(dataValues));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 高级搜索
     * @param conditionList
     * @param convertUser
     */
    public static void superList(List<SuperQueryJsonModel> conditionList,boolean convertUser) {
        List<String> dateControl = JnpfKeyConsts.DateSelect;
        List<String> numControl = JnpfKeyConsts.NumSelect;
        for (SuperQueryJsonModel queryJsonModel : conditionList) {
            List<FieLdsModel> fieLdsModelList = queryJsonModel.getGroups();
            for (FieLdsModel fieLdsModel : fieLdsModelList) {
                List<String> dataList = new ArrayList<>();
                ConfigModel config = fieLdsModel.getConfig();
                String vModel = fieLdsModel.getVModel().trim();
                String jnpfKey = config.getJnpfKey();
                if (vModel.split("_jnpf_").length > 1) {
                    vModel = vModel.split("_jnpf_")[1];
                }
                if (vModel.split("-").length > 1) {
                    vModel = vModel.split("-")[1];
                }
                String value = fieLdsModel.getFieldValue();
                Object fieldValue = fieLdsModel.getFieldValue();
                Object fieldValueTwo = fieLdsModel.getFieldValue();
                if (fieLdsModel.getFieldValue() == null) {
                    fieldValue = "";
                }
                List<String> controlList = new ArrayList() {{
                    addAll(numControl);
                    addAll(dateControl);
                    add(JnpfKeyConsts.TIME);
                }};
                //处理数据
                if (controlList.contains(jnpfKey) && StringUtil.isNotEmpty(value)
                        && !SearchMethodEnum.Like.getSymbol().equals(fieLdsModel.getSymbol())) {
                    int num = 0;
                    List<String> data = new ArrayList<>();
                    try {
                        data.addAll(JsonUtil.getJsonToList(value, String.class));
                    } catch (Exception e) {
                        data.add(value);
                        data.add(value);
                    }
                    String valueOne = data.get(0);
                    String valueTwo = data.get(1);
                    //数字
                    if (numControl.contains(jnpfKey)) {
                        fieldValue = new BigDecimal(valueOne);
                        fieldValueTwo = new BigDecimal(valueTwo);
                        // 精度处理
                        Integer precision = fieLdsModel.getPrecision();
                        if (ObjectUtil.isNotEmpty(precision)) {
                            String zeroNum = "0." + StringUtils.repeat("0", precision);
                            DecimalFormat numFormat = new DecimalFormat(zeroNum);
                            fieldValue = new BigDecimal(numFormat.format(new BigDecimal(valueOne)));
                            if (valueTwo != null) {
                                fieldValueTwo = new BigDecimal(numFormat.format(new BigDecimal(valueTwo)));
                            }
                        }
                        num++;
                    }
                    //日期
                    if (dateControl.contains(jnpfKey)) {
                        fieldValue = new Date();
                        fieldValueTwo = new Date();
                        if (ObjectUtil.isNotEmpty(valueOne)) {
                            fieldValue = new Date(Long.valueOf(valueOne));
                        }
                        if (ObjectUtil.isNotEmpty(valueTwo)) {
                            fieldValueTwo = new Date(Long.valueOf(valueTwo));
                        }
                        num++;
                    }
                    if (num == 0) {
                        fieldValue = valueOne;
                        fieldValueTwo = valueTwo;
                    }
                }
                try {
                    List<List<String>> list = JsonUtil.getJsonToBean(value, List.class);
                    Set<String> dataAll = new HashSet<>();
                    for (int i = 0; i < list.size(); i++) {
                        List<String> list1 = new ArrayList<>();
                        for (int k = 0; k < list.get(i).size(); k++) {
                            list1.add(list.get(i).get(k));
                        }
                        dataAll.add(JSONArray.toJSONString(list1));
                    }
                    dataList = new ArrayList<>(dataAll);
                } catch (Exception e) {
                    try {
                        Set<String> dataAll = new HashSet<>();
                        List<String> list = JsonUtil.getJsonToList(value, String.class);
                        List<String> mast = new ArrayList() {{
                            add(JnpfKeyConsts.CASCADER);
                            add(JnpfKeyConsts.ADDRESS);
                            add(JnpfKeyConsts.COMSELECT);
                            add(JnpfKeyConsts.CURRORGANIZE);
                        }};
                        if (mast.contains(jnpfKey)) {
                            dataAll.add(JSONArray.toJSONString(list));
                        } else {
                            for (int k = 0; k < list.size(); k++) {
                                String data = list.get(k);
                                dataAll.add(data);
                            }
                        }
                        dataList.addAll( new ArrayList<>(dataAll));
                    } catch (Exception e1) {
                        dataList.add(value);
                    }
                }
                if (dataList.size() == 0) {
                    dataList.add("jnpfNullList");
                }
                fieLdsModel.setVModel(vModel);
                fieLdsModel.setFieldValueOne(fieldValue);
                fieLdsModel.setFieldValueTwo(fieldValueTwo);
                fieLdsModel.setDataList(dataList);
                if(convertUser){
                    convertUserSelectData(fieLdsModel);
                }
            }
        }
    }

    /**
     * 树形查询
     *
     * @return
     */
    private static List<FieLdsModel> treeRelation(ColumnDataModel columnData, List<FormAllModel> formAllModel) {
        //主表数据
        List<FormAllModel> mast = formAllModel.stream().filter(t -> FormEnum.mast.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        //列表子表数据
        List<FormAllModel> mastTable = formAllModel.stream().filter(t -> FormEnum.mastTable.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        //子表
        List<FormAllModel> childTable = formAllModel.stream().filter(t -> FormEnum.table.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        List<FieLdsModel> fieLdsModelList = new ArrayList<>();
        List<FieLdsModel> searchVOListAll = JsonUtil.getJsonToList(columnData.getSearchList(), FieLdsModel.class);
        String treeDataSource = columnData.getTreeDataSource();
        String treeRelation = columnData.getTreeRelation();
        boolean isTree = searchVOListAll.stream().filter(t -> t.getId().equals(treeRelation)).count() == 0;
        if (isTree && StringUtil.isNotEmpty(treeRelation)) {
            String vModel = treeRelation.split("-")[0];
            FormAllModel mastModel = mast.stream().filter(t -> vModel.equals(t.getFormColumnModel().getFieLdsModel().getVModel())).findFirst().orElse(null);
            if (mastModel != null) {
                FieLdsModel fieLdsModel = mastModel.getFormColumnModel().getFieLdsModel();
                fieLdsModel.setId(vModel);
                fieLdsModel.setSearchType(1);
                Boolean multiple = fieLdsModel.getMultiple();
                fieLdsModel.setSymbol(multiple && !"organize".equals(treeDataSource) ? SearchMethodEnum.Like.getSymbol() : SearchMethodEnum.Equal.getSymbol());
                fieLdsModelList.add(fieLdsModel);
            }
            FormAllModel mastTableModel = mastTable.stream().filter(t -> vModel.equals(t.getFormMastTableModel().getVModel())).findFirst().orElse(null);
            if (mastTableModel != null) {
                FieLdsModel fieLdsModel = mastTableModel.getFormMastTableModel().getMastTable().getFieLdsModel();
                fieLdsModel.setId(vModel);
                fieLdsModel.setSearchType(1);
                Boolean multiple = fieLdsModel.getMultiple();
                fieLdsModel.setSymbol(multiple && !"organize".equals(treeDataSource) ? SearchMethodEnum.Like.getSymbol() : SearchMethodEnum.Equal.getSymbol());
                fieLdsModelList.add(fieLdsModel);
            }
            FormAllModel childTableModel = childTable.stream().filter(t -> vModel.equals(t.getChildList().getTableModel())).findFirst().orElse(null);
            if (childTableModel != null) {
                List<FormColumnModel> childList = childTableModel.getChildList().getChildList();
                for (FormColumnModel formColumnModel : childList) {
                    FieLdsModel fieLdsModel = formColumnModel.getFieLdsModel();
                    Boolean multiple = fieLdsModel.getMultiple();
                    if (treeRelation.equals(vModel + "-" + fieLdsModel.getVModel())) {
                        fieLdsModel.setSymbol(multiple && !"organize".equals(treeDataSource) ? SearchMethodEnum.Like.getSymbol() : SearchMethodEnum.Equal.getSymbol());
                        fieLdsModel.setId(vModel + "-" + fieLdsModel.getVModel());
                        fieLdsModel.setSearchType(1);
                        fieLdsModelList.add(fieLdsModel);
                    }
                }
            }
        }
        return fieLdsModelList;
    }

    /**
     * 查询
     *
     * @return
     */
    public static void getSuperSql(QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder where, List<SuperJsonModel> superJsonModelList, List<OnlineDynamicSqlModel> sqlModelList, String databaseProductName, String tableName) {
        List<AndOrCriteriaGroup> groupQueryList = new ArrayList<>();
        for (SuperJsonModel superJsonModel : superJsonModelList) {
            List<AndOrCriteriaGroup> groupList = getSuperSql(where, superJsonModel, sqlModelList, databaseProductName, tableName, true);
            boolean and = superJsonModel.getAuthorizeLogic();
            String matchLogic = superJsonModel.getMatchLogic();
            boolean isAddMatchLogic = SearchMethodEnum.And.getSymbol().equalsIgnoreCase(matchLogic);
            if (groupList.size() > 0) {
                AndOrCriteriaGroup andGroup = SqlBuilder.and(DerivedColumn.of("1"), SqlBuilder.isEqualTo(isAddMatchLogic ? 1 : 2), groupList.toArray(new AndOrCriteriaGroup[groupList.size()]));
                AndOrCriteriaGroup orGroup = SqlBuilder.or(DerivedColumn.of("1"), SqlBuilder.isEqualTo(isAddMatchLogic ? 1 : 2), groupList.toArray(new AndOrCriteriaGroup[groupList.size()]));
                groupQueryList.add(and ? andGroup : orGroup);
            }
        }
        if (groupQueryList.size() > 0) {
            where.and(DerivedColumn.of("1"), SqlBuilder.isEqualTo(1), groupQueryList);
        }
    }

    /**
     * 查询
     *
     * @return
     */
    public static List<AndOrCriteriaGroup> getSuperSql(QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder where, SuperJsonModel superJsonModel, List<OnlineDynamicSqlModel> sqlModelList, String databaseProductName, String tableName, boolean authorizeLogic) {
        List<AndOrCriteriaGroup> groupList = new ArrayList<>();
        OnlineQuerySqlUtils onlineQuerySqlUtils = new OnlineQuerySqlUtils();
        List<SuperQueryJsonModel> conditionList = new ArrayList<>();
        List<SuperQueryJsonModel> conditionListAll = superJsonModel.getConditionList();
        String matchLogic = superJsonModel.getMatchLogic();
        for (SuperQueryJsonModel queryJsonModel : conditionListAll) {
            List<FieLdsModel> fieLdsModelList = new ArrayList<>();
            List<FieLdsModel> groupsList = queryJsonModel.getGroups();
            for (FieLdsModel fieLdsModel : groupsList) {
                String table = StringUtil.isNotEmpty(fieLdsModel.getConfig().getRelationTable()) ?
                        fieLdsModel.getConfig().getRelationTable():fieLdsModel.getConfig().getTableName();
                if (StringUtil.isEmpty(tableName) || table.equals(tableName)) {
                    fieLdsModelList.add(fieLdsModel);
                }
            }
            SuperQueryJsonModel queryModel = new SuperQueryJsonModel();
            queryModel.setLogic(queryJsonModel.getLogic());
            queryModel.setGroups(fieLdsModelList);
            if (fieLdsModelList.size() > 0) {
                conditionList.add(queryModel);
            }
        }
        if (conditionList.size() > 0) {
            groupList.addAll(onlineQuerySqlUtils.getSuperSql(conditionList, sqlModelList, databaseProductName, matchLogic));
        }
        if (!authorizeLogic && groupList.size() > 0) {
            where.and(DerivedColumn.of("1"), SqlBuilder.isEqualTo(SearchMethodEnum.And.getSymbol().equalsIgnoreCase(matchLogic) ? 1 : 2), groupList);
        }
        return groupList;
    }

    /**
     * 将用户选择控件的数据转换为Dynamic查询条件
     *
     * @param sqlColumn
     * @param userGroup
     * @param userVal
     */
    private static void convertUserSelectData(BindableColumn sqlColumn, List<AndOrCriteriaGroup> userGroup, String userVal, boolean isLike) {
        // 分组 组织 岗位 角色 用户
        String userValue = userVal.substring(0, userVal.indexOf("--"));
        UserEntity userEntity = userService.getInfo(userValue);
        if (userEntity != null) {
            String idValue;
            //在用户关系表中取出
            //在用户关系表中取出
            List<UserRelationEntity> groupRel = Optional.ofNullable(userRelationService.getListByUserId(userValue, PermissionConst.GROUP)).orElse(new ArrayList<>());
            List<UserRelationEntity> orgRel = Optional.ofNullable(userRelationService.getListByUserId(userValue, PermissionConst.ORGANIZE)).orElse(new ArrayList<>());
            List<UserRelationEntity> posRel = Optional.ofNullable(userRelationService.getListByUserId(userValue, PermissionConst.POSITION)).orElse(new ArrayList<>());
            List<UserRelationEntity> roleRel = Optional.ofNullable(userRelationService.getListByUserId(userValue, PermissionConst.ROLE)).orElse(new ArrayList<>());
            if (groupRel.size() > 0) {
                for (UserRelationEntity split : groupRel) {
                    idValue = "%" + split.getObjectId() + "%";
                    AndOrCriteriaGroup aog = isLike ? SqlBuilder.or(sqlColumn, SqlBuilder.isLike(idValue)) : SqlBuilder.and(sqlColumn, SqlBuilder.isNotLike(idValue));
                    userGroup.add(aog);
                }
            }
            if (StringUtil.isNotEmpty(userEntity.getOrganizeId())) {
                //向上递归 查出所有上级组织
                List<String> allUpOrgIDs = new ArrayList<>();
                organizeService.upWardRecursion(allUpOrgIDs, userEntity.getOrganizeId());
                for (String orgID : allUpOrgIDs) {
                    idValue = "%" + orgID + "%";
                    AndOrCriteriaGroup aog = isLike ? SqlBuilder.or(sqlColumn, SqlBuilder.isLike(idValue)) : SqlBuilder.and(sqlColumn, SqlBuilder.isNotLike(idValue));
                    userGroup.add(aog);
                }
            }
            if (posRel.size() > 0) {
                for (UserRelationEntity split : posRel) {
                    idValue = "%" + split.getObjectId() + "%";
                    AndOrCriteriaGroup aog = isLike ? SqlBuilder.or(sqlColumn, SqlBuilder.isLike(idValue)) : SqlBuilder.and(sqlColumn, SqlBuilder.isNotLike(idValue));
                    userGroup.add(aog);
                }
            }
            if (roleRel.size() > 0) {
                for (UserRelationEntity split : roleRel) {
                    idValue = "%" + split.getObjectId() + "%";
                    AndOrCriteriaGroup aog = isLike ? SqlBuilder.or(sqlColumn, SqlBuilder.isLike(idValue)) : SqlBuilder.and(sqlColumn, SqlBuilder.isNotLike(idValue));
                    userGroup.add(aog);
                }
            }
        }
    }

    public static List<BasicColumn> getBasicColumns(List<String> mainTableFields, List<OnlineDynamicSqlModel> sqlModels, OnlineDynamicSqlModel dycModel,
                                                    List<ColumnListField> columnFieldListAll, boolean isClobDbType) {
        List<BasicColumn> mainSqlColumns = mainTableFields.stream().map(m -> {
            ColumnListField columnListField = columnFieldListAll.stream().filter(item -> item.getProp().equals(m)).findFirst().orElse(null);
            if (isClobDbType && columnListField != null) {
                String jnpfKey = columnListField.getJnpfKey();
                if (JnpfKeyConsts.getTextField().contains(jnpfKey)) {
                    if (sqlModels.size() > 1) {//连表会出现《表名.字段》clob处理需要包含表名
                        return SqlTable.of("dbms_lob.substr(" + dycModel.getTableName()).column(m + ")").as(m);
                    } else {
                        return SqlTable.of(dycModel.getTableName()).column("dbms_lob.substr(" + m + ")").as(m);
                    }
                }
            }
            return dycModel.getSqlTable().column(m);
        }).collect(Collectors.toList());
        return mainSqlColumns;
    }

    private static List<BasicColumn> getBasicColumnsChild(List<ColumnListField> modelList, OnlineDynamicSqlModel dycModel,
                                                          List<OnlineColumnFieldModel> onlineColumnFieldModels, boolean isClobDbType) {
        SqlTable mastSqlTable = dycModel.getSqlTable();
        List<BasicColumn> mastSqlCols = onlineColumnFieldModels.stream().map(m -> {
            ColumnListField columnListField = modelList.stream().filter(item -> item.getProp().equals(m.getOriginallyField())).findFirst().orElse(null);
            if (isClobDbType && columnListField != null) {
                String jnpfKey = columnListField.getConfig().getJnpfKey();
                if (JnpfKeyConsts.getTextField().contains(jnpfKey)) {
                    return SqlTable.of("dbms_lob.substr(" + dycModel.getTableName()).column(m.getField() + ")").as(m.getOriginallyField());
                }
            }
            return mastSqlTable.column(m.getField()).as(m.getOriginallyField());
        }).collect(Collectors.toList());
        return mastSqlCols;
    }


}
