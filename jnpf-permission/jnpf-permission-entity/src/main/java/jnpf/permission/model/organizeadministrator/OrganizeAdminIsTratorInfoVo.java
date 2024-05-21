package jnpf.permission.model.organizeadministrator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 *
 * 机构分级管理员
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Data
public class OrganizeAdminIsTratorInfoVo {

    /** 自然主键 **/
    @Schema(description = "自然主键")
    private String id;
    /** 用户主键 **/
    @Schema(description = "用户主键")
    private String userId;
    /** 机构主键 **/
    @Schema(description = "机构主键")
    private String organizeId;
    /** 机构类型 **/
    @Schema(description = "机构类型")
    private String organizeType;
    /** 本层添加 **/
    @Schema(description = "本层添加")
    private Integer thisLayerAdd;
    /** 本层编辑 **/
    @Schema(description = "本层编辑")
    private Integer thisLayerEdit;
    /** 本层删除 **/
    @Schema(description = "本层删除")
    private Integer thisLayerDelete;
    /** 子层添加 **/
    @Schema(description = "子层添加")
    private Integer subLayerAdd;
    /** 子层编辑 **/
    @Schema(description = "子层编辑")
    private Integer subLayerEdit;
    /** 子层删除 **/
    @Schema(description = "子层删除")
    private Integer subLayerDelete;
    /** 描述 **/
    @Schema(description = "描述")
    private String description;


}
