package gas.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;
import java.util.List;
/**
 * 服务站
 *
 * @版本： V3.6
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2024-05-21
 */
@Data
@TableName("gas_base_service")
public class GasBaseServiceEntity  {
    @TableId(value ="F_ID"  )
    private String id;
    @TableField("F_RECEIVABLEID")
    private String receivableid;
    @TableField(value = "F_NAME" , updateStrategy = FieldStrategy.IGNORED)
    private String name;
    @TableField(value = "F_MANAGEMENTUNIT" , updateStrategy = FieldStrategy.IGNORED)
    private String managementunit;
    @TableField(value = "F_BUSINESSUNIT" , updateStrategy = FieldStrategy.IGNORED)
    private String businessunit;
    @TableField(value = "F_POSITION" , updateStrategy = FieldStrategy.IGNORED)
    private String position;
    @TableField(value = "F_CODE" , updateStrategy = FieldStrategy.IGNORED)
    private String code;
    @TableField(value = "F_CITY" , updateStrategy = FieldStrategy.IGNORED)
    private String city;
    @TableField(value = "F_COUNTY" , updateStrategy = FieldStrategy.IGNORED)
    private String county;
    @TableField(value = "F_DISTRICT_CODE" , updateStrategy = FieldStrategy.IGNORED)
    private String districtCode;
    @TableField(value = "F_ADDRESS" , updateStrategy = FieldStrategy.IGNORED)
    private String address;
    @TableField(value = "F_HIGHWAY_TYPE" , updateStrategy = FieldStrategy.IGNORED)
    private String highwayType;
    @TableField(value = "F_TYPE" , updateStrategy = FieldStrategy.IGNORED)
    private String type;
    @TableField(value = "F_ROAD_NAME" , updateStrategy = FieldStrategy.IGNORED)
    private String roadName;
    @TableField(value = "F_ROAD_CODE" , updateStrategy = FieldStrategy.IGNORED)
    private String roadCode;
    @TableField(value = "F_ROUTE_CODE" , updateStrategy = FieldStrategy.IGNORED)
    private String routeCode;
    @TableField(value = "F_PILE_NUMBER" , updateStrategy = FieldStrategy.IGNORED)
    private String pileNumber;
    @TableField(value = "F_ROUTE_NAME" , updateStrategy = FieldStrategy.IGNORED)
    private String routeName;
    @TableField(value = "F_DIRECTION" , updateStrategy = FieldStrategy.IGNORED)
    private String direction;
    @TableField(value = "F_DIRECTIONDRIVING" , updateStrategy = FieldStrategy.IGNORED)
    private String directiondriving;
    @TableField(value = "F_EXPORT" , updateStrategy = FieldStrategy.IGNORED)
    private String export;
    @TableField(value = "F_ENTRANCE" , updateStrategy = FieldStrategy.IGNORED)
    private String entrance;
    @TableField(value = "F_ADJACENTPROVINCES" , updateStrategy = FieldStrategy.IGNORED)
    private String adjacentprovinces;
    @TableField(value = "F_COMPLETIONTIME" , updateStrategy = FieldStrategy.IGNORED)
    private Date completiontime;
    @TableField(value = "F_LONGITUDE" , updateStrategy = FieldStrategy.IGNORED)
    private String longitude;
    @TableField(value = "F_LATITUDE" , updateStrategy = FieldStrategy.IGNORED)
    private String latitude;
    @TableField(value = "F_REMARKS" , updateStrategy = FieldStrategy.IGNORED)
    private String remarks;
    @TableField(value = "F_PICTURE" , updateStrategy = FieldStrategy.IGNORED)
    private String picture;
    @TableField("F_FLOW_ID")
    private String flowId;
    @TableField("F_FLOW_TASK_ID")
    private String flowTaskId;

}
