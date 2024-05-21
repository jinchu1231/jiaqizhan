package jnpf.base.model.dblink;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.database.model.entity.DbLinkEntity;
import jnpf.database.source.impl.DbOracle;
import jnpf.util.JsonUtil;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库基础表单对象
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class DbLinkBaseForm {

    /**
     * 排序码
     */
    @Schema(description ="排序码")
    private long sortCode;

    @Schema(description ="连接名")
    @NotBlank(message = "必填")
    private String fullName;

    @Schema(description ="数据库类型编码")
    @NotBlank(message = "必填")
    private String dbType;

    @Schema(description ="用户")
    @NotBlank(message = "必填")
    private String userName;

    @Schema(description ="数据库名")
    private String serviceName;

    @Schema(description ="密码")
    @NotBlank(message = "必填")
    private String password;

    @Schema(description ="端口")
    @NotBlank(message = "必填")
    private String port;

    @Schema(description ="ip地址")
    @NotBlank(message = "必填")
    private String host;

    @Schema(description ="模式")
    private String dbSchema;

    @Schema(description ="表空间")
    private String tableSpace;

    @Schema(description ="oracle扩展（true:开启，false:关闭）")
    private Boolean oracleExtend;

    @Schema(description ="oracle连接类型")
    private String oracleLinkType;

    @Schema(description ="oracle服务名")
    private String oracleService;

    @Schema(description ="oracle角色")
    private String oracleRole;



    /**
     * 根据表单对象返回连接实体类
     * @param dbLinkBaseForm 连接表单对象
     * @return 连接实体对象
     */
    public DbLinkEntity getDbLinkEntity(DbLinkBaseForm dbLinkBaseForm){
        DbLinkEntity entity = JsonUtil.getJsonToBean(dbLinkBaseForm, DbLinkEntity.class);
        if (dbLinkBaseForm.getOracleExtend()) {
            entity.setOracleExtend(1);
        } else {
            entity.setOracleExtend(0);
        }
        entity.setDbTableSpace(dbLinkBaseForm.getTableSpace());
        entity.setDbName(dbLinkBaseForm.getServiceName());
        Map<String,String> oracleParam = new HashMap<>(16);
        oracleParam.put(DbOracle.ORACLE_LINK_TYPE,dbLinkBaseForm.getOracleLinkType());
        oracleParam.put(DbOracle.ORACLE_SERVICE,dbLinkBaseForm.getOracleService());
        oracleParam.put(DbOracle.ORACLE_ROLE,dbLinkBaseForm.getOracleRole());
        entity.setOracleParam(JSONUtil.toJsonStr(oracleParam));
        return entity;
    }

}
