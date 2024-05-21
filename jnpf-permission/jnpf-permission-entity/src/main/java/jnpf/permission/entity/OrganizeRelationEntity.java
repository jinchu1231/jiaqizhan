package jnpf.permission.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;


import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.base.entity.SuperExtendEntity;
import lombok.Data;

/**
 * <p>
 * 组织关系
 * </p>
 *
 * @author YanYu
 * @since 2022-01-19
 */
@Data
@TableName("base_organize_relation")
@Schema(description = "OrganizeRelation对象", name = "组织关系")
public class OrganizeRelationEntity extends SuperExtendEntity<String> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 组织主键
     */
    @TableField("F_ORGANIZE_ID")
    private String organizeId;

    /**
     * 对象类型（角色：role）
     */
    @TableField("F_OBJECT_TYPE")
    private String objectType;

    /**
     * 对象主键
     */
    @TableField("F_OBJECT_ID")
    private String objectId;

}
