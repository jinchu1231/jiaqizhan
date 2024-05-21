package jnpf.base.model.enums;

import jnpf.base.model.printdev.PrintFieldModel;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 打印模板-结果集字段Key
 *
 * @author JNPF开发平台组 YY
 * @version V3.2.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月30日
 */
public enum ResultColumnKeysEnum {
    /**
     * 表类别
     */
    TABLE_CAT,
    /**
     * 表模式
     */
    TABLE_SCHEM,
    /**
     * 表名称
     */
    TABLE_NAME,
    /**
     * 列名称
     */
    COLUMN_NAME,
    /**
     * 来自 java.sql.Types 的 SQL 类型
     */
    DATA_TYPE,
    /**
     * 数据源依赖的类型名称，对于 UDT，该类型名称是完全限定的
     */
    TYPE_NAME,
    /**
     * 列的大小
     */
    COLUMN_SIZE,
    /**
     * 小数部分的位数。对于 DECIMAL_DIGITS 不适用的数据类型，则返回 Null。
     */
    DECIMAL_DIGITS,
    /**
     * 基数（通常为 10 或 2）
     */
    NUM_PREC_RADIX,
    /**
     * 是否允许使用 NULL。 columnNoNulls - 可能不允许使用NULL值， columnNullable - 明确允许使用NULL值， columnNullableUnknown - 不知道是否可使用 null
     */
    NULLABLE,
    /**
     * 描述列的注释（可为null）
     */
    REMARKS,
    /**
     * 该列的默认值，当值在单引号内时应被解释为一个字符串（可为null）
     */
    COLUMN_DEF,
    /**
     * 未使用
     */
    SQL_DATA_TYPE,
    /**
     * 未使用
     */
    SQL_DATETIME_SUB,
    /**
     * 对于 char 类型，该长度是列中的最大字节数
     */
    CHAR_OCTET_LENGTH,
    /**
     * 表中的列的索引（从 1 开始）
     */
    ORDINAL_POSITION,
    /**
     * 是否允许使用 NULL， columnNoNulls - 可能不允许使用NULL值， columnNullable - 明确允许使用NULL值， columnNullableUnknown - 不知道是否可使用 null
     */
    IS_NULLABLE,
    /**
     * 表的类别，它是引用属性的作用域（如果 DATA_TYPE 不是 REF，则为null）
     */
    SCOPE_CATLOG,
    /**
     * 表的模式，它是引用属性的作用域（如果 DATA_TYPE 不是 REF，则为null）
     */
    SCOPE_SCHEMA,
    /**
     * 表名称，它是引用属性的作用域（如果 DATA_TYPE 不是 REF，则为null）
     */
    SCOPE_TABLE,
    /**
     * 不同类型或用户生成 Ref 类型、来自 java.sql.Types 的 SQL 类型的源类型（如果 DATA_TYPE 不是 DISTINCT 或用户生成的 REF，则为null）
     */
    SOURCE_DATA_TYPE,
    /**
     * 指示此列是否自动增加，YES --- 如果该列自动增加 ， NO --- 如果该列不自动增加， 空字符串 --- 如果不能确定该列是否是自动增加参数
     */
    IS_AUTOINCREMENT;

    public static Map<ResultColumnKeysEnum, Consumer<String>> getCommon(PrintFieldModel printFieldModel){
        Map<ResultColumnKeysEnum, Consumer<String>> map = new HashMap<>();
        map.put(ResultColumnKeysEnum.COLUMN_NAME, printFieldModel::setColumnSize);
        map.put(ResultColumnKeysEnum.DATA_TYPE, printFieldModel::setDataType);
        map.put(ResultColumnKeysEnum.TYPE_NAME, printFieldModel::setTypeName);
        map.put(ResultColumnKeysEnum.COLUMN_SIZE, printFieldModel::setColumnSize);
        map.put(ResultColumnKeysEnum.DECIMAL_DIGITS, printFieldModel::setDecimalDigits);
        map.put(ResultColumnKeysEnum.NUM_PREC_RADIX, printFieldModel::setNumPrecRadix);
        map.put(ResultColumnKeysEnum.REMARKS, printFieldModel::setReMarks);
        map.put(ResultColumnKeysEnum.COLUMN_DEF, printFieldModel::setColumnDef);
        map.put(ResultColumnKeysEnum.CHAR_OCTET_LENGTH, printFieldModel::setCharOctetLength);
        map.put(ResultColumnKeysEnum.ORDINAL_POSITION, printFieldModel::setOrdinalPosition);
        map.put(ResultColumnKeysEnum.IS_NULLABLE, printFieldModel::setIsNullAble);
        map.put(ResultColumnKeysEnum.IS_AUTOINCREMENT, printFieldModel::setIsAutoIncrement);
        return map;
    }

}
