package gas.model.gasbaseservice;

import lombok.Data;
import java.util.List;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 服务区设置
 * @版本： V3.6
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2024-05-21
 */
@Data
public class GasBaseServiceForm  {
    /** 主键 */
    private String id;

    /** 服务区名 **/
    @JsonProperty("name")
    private String name;
    /** 服务区编码 **/
    @JsonProperty("code")
    private String code;
    /** 管理单位 **/
    @JsonProperty("managementunit")
    private Object managementunit;
    /** 经营单位 **/
    @JsonProperty("businessunit")
    private Object businessunit;
    /** 所处地市 **/
    @JsonProperty("city")
    private Object city;
    /** 所处区县 **/
    @JsonProperty("county")
    private Object county;
    /** 行政区划 **/
    @JsonProperty("districtCode")
    private String districtCode;
    /** 服务区地址 **/
    @JsonProperty("address")
    private String address;
    /** 服务类型 **/
    @JsonProperty("type")
    private Object type;
    /** 公路类型 **/
    @JsonProperty("highwayType")
    private Object highwayType;
    /** 路段名称 **/
    @JsonProperty("roadName")
    private Object roadName;
    /** 路线名称 **/
    @JsonProperty("routeName")
    private Object routeName;
    /** 路段编号 **/
    @JsonProperty("roadCode")
    private String roadCode;
    /** 路线编码 **/
    @JsonProperty("routeCode")
    private String routeCode;
    /** 服务位置 **/
    @JsonProperty("position")
    private Object position;
    /** 桩号 **/
    @JsonProperty("pileNumber")
    private String pileNumber;
    /** 方向 **/
    @JsonProperty("direction")
    private Object direction;
    /** 行车方向 **/
    @JsonProperty("directiondriving")
    private String directiondriving;
    /** 是否入省口 **/
    @JsonProperty("entrance")
    private Object entrance;
    /** 是否出省口 **/
    @JsonProperty("export")
    private Object export;
    /** 相邻省份 **/
    @JsonProperty("adjacentprovinces")
    private Object adjacentprovinces;
    /** 建成时间 **/
    @JsonProperty("completiontime")
    private String completiontime;
    /** 经度 **/
    @JsonProperty("longitude")
    private String longitude;
    /** 纬度 **/
    @JsonProperty("latitude")
    private String latitude;
    /** 备注 **/
    @JsonProperty("remarks")
    private String remarks;
    /** 图片 **/
    @JsonProperty("picture")
    private Object picture;
}
