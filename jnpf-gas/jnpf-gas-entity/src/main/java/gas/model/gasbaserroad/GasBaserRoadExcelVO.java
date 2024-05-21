package gas.model.gasbaserroad;

import lombok.Data;
import java.sql.Time;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.alibaba.fastjson.annotation.JSONField;
import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelEntity;
import cn.afterturn.easypoi.excel.annotation.ExcelCollection;
import java.math.BigDecimal;
import java.util.List;
/**
 *
 * 路段设置
 * @版本： V3.6
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2024-05-21
 */
@Data
public class GasBaserRoadExcelVO{
    /** 路段名称 **/
    @JSONField(name = "name")
    @Excel(name = "路段名称(name)",orderNum = "1", isImportField = "true" )
    private String name;

    /** 路段编码 **/
    @JSONField(name = "code")
    @Excel(name = "路段编码(code)",orderNum = "1", isImportField = "true" )
    private String code;


}
