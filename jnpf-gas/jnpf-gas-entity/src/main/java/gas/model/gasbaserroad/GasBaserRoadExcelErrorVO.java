

package gas.model.gasbaserroad;

import lombok.Data;
import cn.afterturn.easypoi.excel.annotation.Excel;
import com.alibaba.fastjson.annotation.JSONField;
/**
 *
 * 路段设置
 * @版本： V3.6
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2024-05-21
 */
@Data
public class GasBaserRoadExcelErrorVO extends GasBaserRoadExcelVO{

	@Excel(name = "异常原因",orderNum = "999")
    @JSONField(name = "errorsInfo")
	private String errorsInfo;
}
