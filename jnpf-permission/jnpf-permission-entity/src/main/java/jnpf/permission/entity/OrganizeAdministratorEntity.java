package jnpf.permission.entity;

import jnpf.base.entity.SuperExtendEntity;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 *
 * 机构分级管理员
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Data
@TableName("base_organize_administrator")
public class OrganizeAdministratorEntity extends SuperExtendEntity.SuperExtendDEEntity<String> {
    /**
     * 用户主键
     */
    @TableField("F_USER_ID")
    private String userId;

    /**
     * 机构主键
     */
    @TableField("F_ORGANIZE_ID")
    private String organizeId;

    /**
     * 机构类型
     */
    @TableField("F_ORGANIZE_TYPE")
    private String organizeType;

    /**
     * 本层添加
     */
    @TableField("F_THIS_LAYER_ADD")
    private Integer thisLayerAdd;

    /**
     * 本层编辑
     */
    @TableField("F_THIS_LAYER_EDIT")
    private Integer thisLayerEdit;

    /**
     * 本层删除
     */
    @TableField("F_THIS_LAYER_DELETE")
    private Integer thisLayerDelete;

    /**
     * 子层添加
     */
    @TableField("F_SUB_LAYER_ADD")
    private Integer subLayerAdd;

    /**
     * 子层编辑
     */
    @TableField("F_SUB_LAYER_EDIT")
    private Integer subLayerEdit;

    /**
     * 子层删除
     */
    @TableField("F_SUB_LAYER_DELETE")
    private Integer subLayerDelete;

    /**
     * 本层查看
     */
    @TableField("F_THIS_LAYER_SELECT")
    private Integer thisLayerSelect;

    /**
     * 子层查看
     */
    @TableField("F_SUB_LAYER_SELECT")
    private Integer subLayerSelect;

}
