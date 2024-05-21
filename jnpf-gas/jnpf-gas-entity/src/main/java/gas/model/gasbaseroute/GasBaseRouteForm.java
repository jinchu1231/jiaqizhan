package gas.model.gasbaseroute;

import lombok.Data;
import java.util.List;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 路线管理
 * @版本： V3.6
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2024-05-21
 */
@Data
public class GasBaseRouteForm  {
    /** 主键 */
    private String id;

    /** 路线名称 **/
    @JsonProperty("name")
    private String name;
    /** 路线编码 **/
    @JsonProperty("code")
    private String code;
}
