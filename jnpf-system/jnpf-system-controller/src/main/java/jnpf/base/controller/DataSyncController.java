package jnpf.base.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.model.dbsync.DbSyncForm;
import jnpf.base.model.dbsync.DbSyncVo;
import jnpf.base.service.DbLinkService;
import jnpf.base.service.DbSyncService;
import jnpf.database.datatype.db.interfaces.DtInterface;
import jnpf.database.datatype.sync.util.DtSyncUtil;
import jnpf.database.model.dto.PrepSqlDTO;
import jnpf.database.model.entity.DbLinkEntity;
import jnpf.database.sql.util.SqlFastUtil;
import lombok.Cleanup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 数据同步
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月27日 上午9:18
 */
@Tag(name = "数据同步", description = "DataSync")
@RestController
@RequestMapping("/api/system/DataSync")
public class DataSyncController {

    @Autowired
    private DbSyncService dbSyncService;
    @Autowired
    private DbLinkService dblinkService;

    /**
     * 验证连接
     *
     * @param dbSyncForm 页面参数
     * @return
     * @throws Exception
     */
    @Operation(summary = "验证连接")
    @Parameters({
            @Parameter(name = "dbSyncForm", description = "页面参数", required = true)
    })
    @SaCheckPermission("systemData.dataSync")
    @PostMapping("/Actions/checkDbLink")
    public ActionResult<DbSyncVo> checkDbLink(@RequestBody DbSyncForm dbSyncForm) throws Exception {
        String fromDbType;
        String toDbType;
        DbSyncVo vo = new DbSyncVo();
        try {
            DbLinkEntity dbLinkEntity = dblinkService.getResource(dbSyncForm.getDbConnectionFrom());
            DbLinkEntity dbLinkEntity1 = dblinkService.getResource(dbSyncForm.getDbConnectionTo());
            fromDbType = dbLinkEntity.getDbType();
            toDbType = dbLinkEntity1.getDbType();
            @Cleanup Connection conn = PrepSqlDTO.getConn(dbLinkEntity);
            @Cleanup Connection conn1 = PrepSqlDTO.getConn(dbLinkEntity1);
            if (conn.getMetaData().getURL().equals(conn1.getMetaData().getURL())) {
                return ActionResult.fail("数据库连接不能相同");
            }
            vo.setCheckDbFlag(true);
            vo.setTableList(SqlFastUtil.getTableList(dbLinkEntity, null));
            // 字段类型全部对应关系
            Map<String, List<String>> ruleMap = getConvertRules(fromDbType, toDbType).getData();
            Map<String, String> defaultRuleMap = getDefaultRules(fromDbType, toDbType).getData();
            // 默认类型置顶
            for (String key : defaultRuleMap.keySet()) {
                List<String> list = ruleMap.get(key);
                if(list != null){
                    String rule = defaultRuleMap.get(key);
                    list.remove(rule);
                    list.add(0, rule + " (默认)");
                    ruleMap.put(key, list);
                }
            }
            vo.setConvertRuleMap(ruleMap);
        }catch (Exception e){
            return ActionResult.fail("数据库连接失败");
        }
        return ActionResult.success(vo);
    }

    /**
     * 执行数据同步
     *
     * @param dbSyncForm 数据同步参数
     * @return ignore
     */
    @Operation(summary = "数据同步校验")
    @Parameters({
            @Parameter(name = "dbSyncForm", description = "页面参数", required = true)
    })
    @SaCheckPermission("systemData.dataSync")
    @PostMapping
    public ActionResult<Object> checkExecute(@RequestBody DbSyncForm dbSyncForm) {
        int status;
        try {
            status = dbSyncService.checkExecute(dbSyncForm.getDbConnectionFrom(), dbSyncForm.getDbConnectionTo(), dbSyncForm.getConvertRuleMap(), dbSyncForm.getDbTable());
        } catch (Exception e) {
            e.printStackTrace();
            return ActionResult.fail(e.getMessage());
        }
        if (status == -1) {
            return ActionResult.fail("请检查，同一数据库下无法同步数据");
        }
        return ActionResult.success(status);
    }

    /**
     * 执行数据同步
     *
     * @param dbSyncForm 数据同步参数
     * @return ignore
     */
    @Operation(summary = "执行数据同步")
    @Parameters({
            @Parameter(name = "dbSyncForm", description = "页面参数", required = true)
    })
    @SaCheckPermission("systemData.dataSync")
    @PostMapping("/Actions/Execute")
    public ActionResult<String> execute(@RequestBody DbSyncForm dbSyncForm) {
        try{
            dbSyncService.executeImport(dbSyncForm.getDbConnectionFrom(), dbSyncForm.getDbConnectionTo(), dbSyncForm.getConvertRuleMap(), dbSyncForm.getDbTable());
        }catch (Exception e){
            e.printStackTrace();
            return ActionResult.fail("同步失败：" + e.getMessage());
        }
        return ActionResult.success("成功");
    }


    /**
     * 批量执行数据同步
     *
     * @param dbSyncForm 数据同步参数
     * @return ignore
     * @throws Exception ignore
     */
    @Operation(summary = "批量执行数据同步")
    @Parameters({
            @Parameter(name = "dbSyncForm", description = "页面参数", required = true)
    })
    @SaCheckPermission("systemData.dataSync")
    @PostMapping("/Actions/batchExecute")
    public ActionResult<Map<String, Integer>> batchExecute(@RequestBody DbSyncForm dbSyncForm) throws Exception {
        Map<String, Integer> result = dbSyncService.batchExecuteImport(dbSyncForm.getDbConnectionFrom(), dbSyncForm.getDbConnectionTo(), dbSyncForm.getConvertRuleMap(), dbSyncForm.getDbTableList());
        return ActionResult.success("操作成功", result);
    }




    /**
     * 获取数据类型默认转换规则
     * 一对一
     * @param fromDbType 被转换数据库类型
     * @param toDbType 转换数据库类型
     * @return 转换规则
     * @throws Exception 未找到数库
     */
    @GetMapping("/Actions/getDefaultRules")
    @SaCheckPermission("systemData.dataSync")
    @Operation(summary = "获取一对一数据类型默认转换规则")
    public static ActionResult<Map<String, String>> getDefaultRules(String fromDbType, String toDbType) throws Exception{
        Map<String, String> map = new LinkedHashMap<>();
        for (DtInterface dtInterface : DtInterface.getClz(fromDbType).getEnumConstants()) {
            DtInterface toFixCovert = DtSyncUtil.getToFixCovert(dtInterface, toDbType);
            if(toFixCovert != null){
                map.put(dtInterface.getDataType(), toFixCovert.getDataType());
            }
        }
        return ActionResult.success(map);
    }

    /**
     * 获取数据类型转换规则
     * 一对多
     * @param fromDbType 被转换数据库类型
     * @param toDbType 转换数据库类型
     * @return 转换规则
     * @throws Exception 未找到数库
     */
    @Operation(summary = "获取一对多数据类型转换规则")
    @SaCheckPermission("systemData.dataSync")
    @GetMapping("/Actions/getConvertRules")
    public static ActionResult<Map<String, List<String>>> getConvertRules(String fromDbType, String toDbType) throws Exception{
        Map<String, List<String>> map = new LinkedHashMap<>();
        for (DtInterface dtInterface : DtInterface.getClz(fromDbType).getEnumConstants()) {
            List<String> list = new LinkedList<>();
            DtInterface[] allConverts = DtSyncUtil.getAllConverts(dtInterface, toDbType);
            if(allConverts != null){
                for (DtInterface allConvert : allConverts) {
                    list.add(allConvert.getDataType());
                }
                map.put(dtInterface.getDataType(), list);
            }
        }
        return ActionResult.success(map);
    }

}
