package jnpf.model.flow;


import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.base.UserInfo;
import jnpf.database.model.entity.DbLinkEntity;
import jnpf.model.visualJson.FieLdsModel;
import jnpf.model.visualJson.TableModel;
import jnpf.permission.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description="数据模型")
public class DataModel {
    @Schema(description = "名称")
    private Map<String, Object> dataNewMap;
    @Schema(description = "字段列表")
    private List<FieLdsModel> fieLdsModelList;
    @Schema(description = "表列表")
    private List<TableModel> tableModelList;
    @Schema(description = "主表id")
    private String mainId;
    @Schema(description = "数据库链接")
    private DbLinkEntity link;
    @Schema(description = "转换")
    private Boolean convert;
    @Schema(description = "是否oracle")
    private Boolean isOracle;
    @Schema(description = "用户信息")
    private UserEntity userEntity;
    //是否开启安全锁
    @Schema(description = "安全锁策略")
    private Boolean concurrencyLock = false;
    @Schema(description = "主键策略")
    private Integer primaryKeyPolicy = 1;
    @Schema(description = "用户信息")
    private UserInfo userInfo;
    @Schema(description = "流程启用")
    private Boolean flowEnable = true;
    @Schema(description = "是否外链")
    private boolean linkOpen = false;

    @Schema(description = "流程表单权限")
    private List<Map<String,Object>> flowFormOperates;
}
