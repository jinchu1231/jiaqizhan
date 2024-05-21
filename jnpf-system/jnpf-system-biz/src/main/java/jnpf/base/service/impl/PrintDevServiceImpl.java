package jnpf.base.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.entity.DictionaryTypeEntity;
import jnpf.base.entity.OperatorRecordEntity;
import jnpf.base.entity.PrintDevEntity;
import jnpf.base.mapper.PrintDevMapper;
import jnpf.base.model.printdev.PrintDevTreeModel;
import jnpf.base.model.printdev.PrintOption;
import jnpf.base.model.printdev.PrintPagination;
import jnpf.base.model.printdev.PrintTableTreeModel;
import jnpf.base.model.printdev.vo.PrintDevVO;
import jnpf.base.service.DbLinkService;
import jnpf.base.service.DbTableService;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.service.DictionaryTypeService;
import jnpf.base.service.IPrintDevService;
import jnpf.base.service.SuperServiceImpl;
import jnpf.base.util.PrintDevUtil;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.MsgCode;
import jnpf.database.model.dbfield.JdbcColumnModel;
import jnpf.database.model.dto.PrepSqlDTO;
import jnpf.database.model.entity.DbLinkEntity;
import jnpf.database.util.DataSourceUtil;
import jnpf.database.util.DbTypeUtil;
import jnpf.database.util.JdbcUtil;
import jnpf.exception.DataException;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils2;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.NClob;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * 打印模板-服务实现类
 *
 * @author JNPF开发平台组 YY
 * @version V3.2.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月30日
 */
@Service
public class PrintDevServiceImpl extends SuperServiceImpl<PrintDevMapper, PrintDevEntity> implements IPrintDevService {

    @Autowired
    private DictionaryDataService dictionaryDataService;
    @Autowired
    private DictionaryTypeService dictionaryTypeService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private DataSourceUtil dataSourceUtil;
    @Autowired
    private DbLinkService dbLinkService;
    @Autowired
    private DbTableService dbTableService;
    @Autowired
    private PrintDevMapper printDevMapper;

    @Override
    public List<PrintDevEntity> getList(PrintPagination paginationPrint) {
        QueryWrapper<PrintDevEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtil.isNotEmpty(paginationPrint.getKeyword())) {
            queryWrapper.lambda().and(
                    t-> t.like(PrintDevEntity::getFullName, paginationPrint.getKeyword()).or().like(PrintDevEntity::getEnCode, paginationPrint.getKeyword())
            );
        }
        if (StringUtil.isNotEmpty(paginationPrint.getCategory())) {
            queryWrapper.lambda().eq(PrintDevEntity::getCategory, paginationPrint.getCategory());
        }
        if (paginationPrint.getEnabledMark() != null) {
            queryWrapper.lambda().eq(PrintDevEntity::getEnabledMark, paginationPrint.getEnabledMark());
        }
        queryWrapper.lambda().orderByAsc(PrintDevEntity::getSortCode).orderByDesc(PrintDevEntity::getCreatorTime);
        Page<PrintDevEntity> page = new Page<>(paginationPrint.getCurrentPage(), paginationPrint.getPageSize());
        IPage<PrintDevEntity> iPage = this.page(page, queryWrapper);
        return paginationPrint.setData(iPage.getRecords(), page.getTotal());
    }

    @Override
    public List<PrintOption> getPrintTemplateOptions(List<String> ids) {
        QueryWrapper<PrintDevEntity> wrapper = new QueryWrapper<>();
        wrapper.lambda().in(PrintDevEntity::getId,ids);
        wrapper.lambda().eq(PrintDevEntity::getEnabledMark,1);
        List<PrintDevEntity> list = this.getBaseMapper().selectList(wrapper);
        List<PrintOption> options = JsonUtil.getJsonToList(list, PrintOption.class);
        return options;
    }


    @Override
    public List<PrintDevVO> getTreeModel() throws Exception {
        QueryWrapper<PrintDevEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByAsc(PrintDevEntity::getSortCode).orderByDesc(PrintDevEntity::getCreatorTime);
        return setTreeModel(this.list(queryWrapper));
    }

    @Override
    public List<PrintDevVO> getTreeModel(Integer type) {
        QueryWrapper<PrintDevEntity> query = new QueryWrapper<>();
        query.lambda().eq(PrintDevEntity::getType, type);
        query.lambda().eq(PrintDevEntity::getEnabledMark,1);
        query.lambda().orderByAsc(PrintDevEntity::getSortCode).orderByDesc(PrintDevEntity::getCreatorTime);
        List<PrintDevEntity> printEntityList = this.list(query);
        return setTreeModel(printEntityList);
    }

    private List<PrintDevVO> setTreeModel(List<PrintDevEntity> printEntityList) {
        String encode = "printDev";
        //数据字典缺失
        DictionaryTypeEntity DictionaryType = dictionaryTypeService.getInfoByEnCode(encode);
        if(DictionaryType == null){
            throw new DataException(MsgCode.PRI002.get());
        }
        List<DictionaryDataEntity> dicDataList = dictionaryDataService.
                getList(DictionaryType.getId());
        List<PrintDevTreeModel> modelAll = new LinkedList<>();
        //设置树形主节点（不显示没有子集的）
        for (DictionaryDataEntity dicEntity : dicDataList) {
            PrintDevTreeModel model = new PrintDevTreeModel();
            model.setFullName(dicEntity.getFullName());
            model.setId(dicEntity.getId());
            Long num = printEntityList.stream().filter(t -> t.getCategory().equals(dicEntity.getEnCode())).count();
            //编码底下存在的子节点总数
            if (num > 0) {
                model.setNum(Integer.parseInt(num.toString()));
                modelAll.add(model);
            }
        }
        List<String> userId = new ArrayList<>();
        printEntityList.forEach(t -> {
            userId.add(t.getCreatorUserId());
            if (StringUtil.isNotEmpty(t.getLastModifyUserId())) {
                userId.add(t.getLastModifyUserId());
            }
        });
        List<UserEntity> userList = userService.getUserName(userId);
        //设置子节点分支
        for (PrintDevEntity printEntity : printEntityList) {
            DictionaryDataEntity dicDataEntity = dicDataList.stream()
                    .filter(t -> t.getEnCode().equals(printEntity.getCategory())).findFirst().orElse(null);
            //如果字典存在则装入容器
            PrintDevTreeModel model = JsonUtil.getJsonToBean(printEntity, PrintDevTreeModel.class);
            if (dicDataEntity != null) {
                //创建者
                UserEntity creatorUser = userList.stream().filter(t -> t.getId().equals(model.getCreatorUser())).findFirst().orElse(null);
                model.setCreatorUser(creatorUser != null ? creatorUser.getRealName() + "/" + creatorUser.getAccount() : "");
                //修改人
                UserEntity lastmodifyuser = userList.stream().filter(t -> t.getId().equals(model.getLastModifyUser())).findFirst().orElse(null);
                model.setLastModifyUser(lastmodifyuser != null ? lastmodifyuser.getRealName() + "/" + lastmodifyuser.getAccount() : "");

                model.setParentId(dicDataEntity.getId());
                modelAll.add(model);
            }
        }
        List<SumTree<PrintDevTreeModel>> trees = TreeDotUtils.convertListToTreeDot(modelAll);
        List<PrintDevVO> list = JsonUtil.getJsonToList(trees, PrintDevVO.class);
        return list;
    }

    @Override
    public Boolean checkNameExist(String fullName, String id) {
        QueryWrapper<PrintDevEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(PrintDevEntity::getFullName, fullName);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(PrintDevEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public List<OperatorRecordEntity> getFlowTaskOperatorRecordList(String taskId) {
        List<OperatorRecordEntity> operatorRecordList = null;
        try {
            // TODO 不同库之间的表名大小写问题
            String sql = "SELECT * FROM flow_task_operator_record WHERE f_task_id = ? ORDER BY f_handle_time";
            /*@Cleanup Connection conn = ConnUtil.getConn(dataSourceUtil);
            if(configValueUtil.isMultiTenancy()){
                if(DbTypeUtil.checkPostgre(dataSourceUtil)){
                    JdbcUtil.creUpDe(new PrepSqlDTO("SET SEARCH_path To " + userProvider.get().getTenantDbConnectionString() + "").withConn(dataSourceUtil, null));
                }
            }*/
            operatorRecordList = JdbcUtil.queryCustomMods(new PrepSqlDTO(sql, taskId).withConn(dataSourceUtil, null), OperatorRecordEntity.class);
            //已办人员
            List<UserEntity> userList = userService.getList(false);
            operatorRecordList.forEach(or -> {
                or.setHandleTime(or.getHandleTimeOrigin().getTime());
                UserEntity userName = userList.stream().filter(t -> t.getId().equals(or.getHandleId())).findFirst().orElse(null);
                or.setUserName(userName != null ? userName.getRealName() + "/" + userName.getAccount() : "");
            });
        } catch (Exception e) {
            e.getMessage();
        }
        return operatorRecordList;
    }

    @Override
    public Map<String, Object> getDataBySql(String dbLinkId, String sqlTempLate) throws Exception {
        Map<String, Object> map = new TreeMap<>();
        MultiConsumer<Integer, List<List<JdbcColumnModel>>, DbLinkEntity>  consumer = (i, dataList, dbLinkEntity)->{
            if (i == 0) {
                //数据信息
                map.put("headTable", getDataMap(dataList));
            } else {
                //查询子表数据信息
                map.put("T" + i, getDataMap(dataList));
            }
        };
        sqlPrintCommon(dbLinkId, sqlTempLate, consumer);
        return map;
    }

    @FunctionalInterface
    private interface MultiConsumer<T, S, U>{
        void accept(T t, S s, U u);
    }

    @Override
    public List<SumTree<PrintTableTreeModel>> getPintTabFieldStruct(String dbLinkId, String sqlTempLate) throws Exception {
        List<PrintTableTreeModel> treeList = new ArrayList<>();
        MultiConsumer<Integer, List<List<JdbcColumnModel>>, DbLinkEntity>  consumer = (i, dataList, dbLinkEntity)->{
            PrintTableTreeModel printTable = new PrintTableTreeModel();
            Set<String> tableNameSet = new HashSet<>();
            String parentId;
            String headTable;
            if (i == 0) {
                parentId = "headTable";
                headTable = "主表:";
            } else {
                //查询子表 ==============
                parentId = "T" + i;
                headTable = parentId + ":";
            }
            printTable.setId(parentId);
            treeSetField(treeList, tableNameSet, dbLinkEntity, dataList.get(0), parentId);
            if(tableNameSet.size() > 0){
                printTable.setFullName(headTable + String.join("|", tableNameSet));
            }else {
                printTable.setFullName(headTable);
            }
            printTable.setParentId("struct");
            treeList.add(printTable);
        };
        sqlPrintCommon(dbLinkId, sqlTempLate, consumer);
        return TreeDotUtils2.convertListToTreeDot(treeList);
    }


    private void sqlPrintCommon(String dbLinkId, String sqlTempLate, MultiConsumer<Integer, List<List<JdbcColumnModel>>, DbLinkEntity> consumer) throws Exception{
        DbLinkEntity dbLinkEntity = dbLinkService.getResource(dbLinkId);
        //转换json
        List<Map<String, Object>> sqlList = JsonUtil.getJsonToList(JSONArray.parseArray(sqlTempLate));
        // 遍历Sql语句
        for (int i = 0; i < sqlList.size(); i++) {
            List<List<JdbcColumnModel>> dataList;
            try {
                // 获取表字段信息集合
                String sql = sqlList.get(i).get("sql").toString();
                String addition;
                if(DbTypeUtil.checkOracle(dbLinkEntity)){
                    addition = "SELECT major.* FROM\n" +
                            "\t(SELECT 1 from dual) temp\n" +
                            "LEFT JOIN \n" +
                            " \t({sql}) major\n" +
                            "ON \n" +
                            "\t1 = 1";
                }else {
                    addition = "SELECT major.* FROM\n" +
                            "\t(SELECT 1 AS tempColumn) AS temp\n" +
                            "LEFT JOIN \n" +
                            " \t({sql}) AS major\n" +
                            "ON \n" +
                            "\t1 = 1";
                }
                sql = addition.replace("{sql}", sql);
                dataList = (JdbcUtil.queryJdbcColumns(new PrepSqlDTO(sql).withConn(dbLinkEntity)).get());
            } catch (DataException e) {
                throw new Exception(MsgCode.PRI005.get().replace("{index}", Integer.toString(i + 1)) + e.getMessage());
            }
            if (i == 0) {
                // 查询主表 ==============
                //获取表头信息(第一条且单条信息)
                if (dataList.size() > 1) {
                    throw new Exception(MsgCode.PRI003.get());
                } else if (dataList.size() == 0) {
                    throw new Exception(MsgCode.PRI004.get());
                }
            }
            consumer.accept(i, dataList, dbLinkEntity);
        }
    }

    public void treeSetField(List<PrintTableTreeModel> list, Set<String> tableNameSet, DbLinkEntity dbLinkEntity, List<JdbcColumnModel> dbJdbcModelList, String parentId) {
        for (Map<String, String> mapOne : getFieldMap(dbLinkEntity, dbJdbcModelList, tableNameSet)) {
            PrintTableTreeModel fieldModel = new PrintTableTreeModel();
            fieldModel.setId(mapOne.get("field"));
            fieldModel.setFullName(mapOne.get("fieldName"));
            fieldModel.setParentId(parentId);
            list.add(fieldModel);
        }
    }

    private List<Map<String, Object>> getDataMap(List<List<JdbcColumnModel>> dbJdbcModelList) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (List<JdbcColumnModel> mods : dbJdbcModelList) {
            Map<String, Object> map = new HashMap<>(16);
            for (JdbcColumnModel mod : mods) {
                Object value = mod.getValue();
                if(value != null){
                    // 对打印时间相关的值进行处理
//                    if(mod.getDataType().equalsIgnoreCase("dateTime")){
//                        LocalDateTime localDateTime = null;
//                        if(value instanceof LocalDateTime) localDateTime = (LocalDateTime)value;
//                        if(value instanceof Timestamp) localDateTime = ((Timestamp) value).toLocalDateTime();
//                        if(localDateTime != null) value = localDateTime.getYear() + "年"
//                                + localDateTime.getMonthValue() + "月" + localDateTime.getDayOfMonth() + "日";
//                    }
                    if(mod.getDataType().equalsIgnoreCase("decimal")){
                        assert value instanceof BigDecimal;
                        value = ((BigDecimal)value).toPlainString();
                    }
                }else {
                    value = "";
                }
                map.put(mod.getLabel(), value);
            }
            mapList.add(map);
        }
        return mapList;
    }

    /**
     * 过滤NClob类型
     */
    public Object filterNClob(Object value){
        if(value instanceof NClob){
            try {
                InputStream inputStream = ((NClob) value).getAsciiStream();
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                return new String(bytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    private List<Map<String, String>> getFieldMap(DbLinkEntity dbLinkEntity, List<JdbcColumnModel> dbJdbcModelList, Set<String> tableNameSet) {
        List<Map<String, String>> mapList = new ArrayList<>();
        for (JdbcColumnModel model : dbJdbcModelList) {
            // 获取表名
            Map<String, String> map = new HashMap<>();
            map.put("field", model.getLabel());
            // 部分数据库，无法从元数据中查出表名，比如Oracle
            if(StringUtil.isNotEmpty(model.getTable())){
                tableNameSet.add(model.getTable());
                String fieldComment = PrintDevUtil.compareGetColumnComment(dbLinkEntity, model.getTable(), model.getField());
                fieldComment = StringUtil.isNotEmpty(fieldComment) ? " (" + fieldComment : "";
                map.put("fieldName", model.getField() + fieldComment);
            }else {
                map.put("fieldName", model.getField());
            }
            mapList.add(map);
        }
        return mapList;
    }

    @Override
    public void creUpdateCheck(PrintDevEntity printDevEntity, Boolean fullNameCheck, Boolean encodeCheck) {
        String fullName = printDevEntity.getFullName();
        String encode = printDevEntity.getEnCode();
        // 名称长度验证
        if(fullName.length() > 80){
            throw  new DataException(MsgCode.EXIST005.get());
        }
        QueryWrapper<PrintDevEntity> query = new QueryWrapper<>();
        //重名验证
        if(fullNameCheck){
            query.lambda().eq(PrintDevEntity::getFullName, fullName);
            if (printDevMapper.selectList(query).size() > 0) {
                throw  new DataException(MsgCode.EXIST003.get());
            }
        }
        //编码验证
        if(encodeCheck){
            query.clear();
            query.lambda().eq(PrintDevEntity::getEnCode, encode);
            if (printDevMapper.selectList(query).size() > 0) {
                throw new DataException(MsgCode.EXIST002.get());
            }
        }
    }

    @SneakyThrows
    @Override
    public Map<String, Object> getDataMap(PrintDevEntity entity, String formId) {
        Map<String, Object> printDataMap;
        try {
            printDataMap = this.getDataBySql(
                    entity.getDbLinkId(),
                    entity.getSqlTemplate().replaceAll("@formId", "'" + formId + "'"));
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        List<Map<String, Object>> headTableList = (List<Map<String, Object>>) printDataMap.get("headTable");
        printDataMap.remove("headTable");
        for (Map map : headTableList) {
            printDataMap.putAll(map);
        }
        Map<String, Object> dataMap = new HashMap<>(16);
        dataMap.put("printData", JsonUtil.getJsonToBean(JsonUtil.getObjectToStringAsDate(printDataMap), Map.class));
        String printTemplate = entity.getPrintTemplate();
        printTemplate = "<div style=\"page-break-after:always\">"+printTemplate+"</p>";
        dataMap.put("printTemplate", printTemplate);

        List<OperatorRecordEntity> operatorRecordList = this.getFlowTaskOperatorRecordList(formId);
        dataMap.put("operatorRecordList", operatorRecordList);
        return dataMap;
    }

}
