package jnpf.message.model.imrecord;

import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.base.Pagination;
import lombok.Data;

import java.io.Serializable;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-07-05
 */
@Data
public class PaginationImRecordModel extends Pagination implements Serializable {

    /**
     * 接收人id
     */
    @Schema(description = "接收人id")
    private String toUserId;

    /**
     * 发送人id
     */
    @Schema(description = "发送人id")
    private String formUserId;

}
