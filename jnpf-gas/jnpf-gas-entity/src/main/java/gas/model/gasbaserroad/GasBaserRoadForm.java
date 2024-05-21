package gas.model.gasbaserroad;

import lombok.Data;
import java.util.List;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 路段设置
 * @版本： V3.6
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2024-05-21
 */
@Data
public class GasBaserRoadForm  {
    /** 主键 */
    private String id;
    /** 乐观锁 **/
    @JsonProperty("version")
    private Integer version;

    /** 路段名称 **/
    @JsonProperty("name")
    private String name;
    /** 路段编码 **/
    @JsonProperty("code")
    private String code;
}
