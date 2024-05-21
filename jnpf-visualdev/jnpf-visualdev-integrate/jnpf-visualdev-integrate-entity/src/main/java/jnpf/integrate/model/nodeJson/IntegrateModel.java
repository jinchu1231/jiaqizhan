package jnpf.integrate.model.nodeJson;

import jnpf.base.UserInfo;
import lombok.Data;

/**
 * 版本： V3.0.0
 * 版权： 引迈信息技术有限公司
 * 作者： 管理员/admin
 * 日期： 2020-10-21 14:23:30
 */
@Data
public class IntegrateModel {
    private UserInfo userInfo;
    private String id;
    private String cron;
    private Long startTime = System.currentTimeMillis();
    private Long endTime = System.currentTimeMillis();
    private Integer endTimeType = 1;
    private Integer endLimit = 1;
    private Integer num = 0;
    private Integer state = 0;
    private Long time = System.currentTimeMillis();
}
