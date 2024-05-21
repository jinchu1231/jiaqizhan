package jnpf.base.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 单据规则
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月27日 上午9:18
 */
@Data
@TableName("base_bill_rule")
public class BillRuleEntity extends SuperExtendEntity.SuperExtendDEEntity<String> implements Serializable {

    /**
     * 单据名称
     */
    @TableField("f_full_name")
    private String fullName;

    /**
     * 单据编码
     */
    @TableField("f_en_code")
    private String enCode;

    /**
     * 单据前缀
     */
    @TableField("f_prefix")
    private String prefix;

    /**
     * 日期格式
     */
    @TableField("f_date_format")
    private String dateFormat;

    /**
     * 流水位数
     */
    @TableField("f_digit")
    private Integer digit;

    /**
     * 流水起始
     */
    @TableField("f_start_number")
    private String startNumber;

    /**
     * 流水范例
     */
    @TableField("f_example")
    private String example;

    /**
     * 当前流水号
     */
    @TableField("f_this_number")
    private Integer thisNumber;

    /**
     * 输出流水号
     */
    @TableField("f_output_number")
    private String outputNumber;

    /**
     * 分类
     */
    @TableField("f_category")
    private  String category;

}
