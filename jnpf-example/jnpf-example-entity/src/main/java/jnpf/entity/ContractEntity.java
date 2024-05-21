package jnpf.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jnpf.base.entity.SuperBaseEntity;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * Contract
 * 版本： V3.0.0
 * 版权： 引迈信息技术有限公司(https://www.jnpfsoft.com)
 * 作者： JNPF开发平台组
 * 日期： 2020-12-31
 */
@Data
@TableName("test_contract")
public class ContractEntity extends SuperBaseEntity.SuperTBaseEntity<String> implements Serializable {

    @TableField("F_CONTRACTNAME")
    private String contractName;

    @TableField("F_MYTELEPHONE")
    private String mytelePhone;

    @TableField("F_FILEJSON")
    private String fileJson;

}
