package jnpf.base.model.cacheManage;

import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.base.Page;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2023年11月15日19:29:50
 */
@Data
public class PaginationCacheManage extends Page {
    @Schema(description = "开始时间")
    private Long overdueStartTime;
    @Schema(description = "结束时间")
    private Long overdueEndTime;
}
