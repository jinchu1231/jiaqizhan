package jnpf.base.model.printdev;

import lombok.Data;

/**
 * 打印模板数字段模型对象
 *
 * @author JNPF开发平台组 YY
 * @version V3.2.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月30日
 */
@Data
public class PrintFieldModel {

    /**
     * 表模式
     */
    private String tableSchema;
    /**
     * 表名称
     */
    private String tableName;
    /**
     * 列名称
     */
    private String columnName;
    /**
     * 数据类型
     */
    private String dataType;
    /**
     * 数据源依赖的类型名称
     */
    private String typeName;
    /**
     * 列的大小
     */
    private String columnSize;
    /**
     * 小数部分的位数。对于 DECIMAL_DIGITS 不适用的数据类型，则返回 Null。
     */
    private String decimalDigits;
    /**
     * 是否允许使用 NULL。 columnNoNulls - 可能不允许使用NULL值， columnNullable - 明确允许使用NULL值， columnNullableUnknown - 不知道是否可使用 null
     */
    private String numPrecRadix;
    /**
     * 描述列的注释（可为null）
     */
    private String reMarks;
    /**
     * 该列的默认值，当值在单引号内时应被解释为一个字符串（可为null）
     */
    private String columnDef;
    /**
     * 对于 char 类型，该长度是列中的最大字节数
     */
    private String charOctetLength;
    /**
     * 表中的列的索引（从 1 开始）
     */
    private String ordinalPosition;
    /**
     * 是否允许使用 NULL， columnNoNulls - 可能不允许使用NULL值， columnNullable - 明确允许使用NULL值， columnNullableUnknown - 不知道是否可使用 null
     */
    private String isNullAble;
    /**
     * 指示此列是否自动增加，YES --- 如果该列自动增加 ， NO --- 如果该列不自动增加， 空字符串 --- 如果不能确定该列是否是自动增加参数
     */
    private String isAutoIncrement;
}
