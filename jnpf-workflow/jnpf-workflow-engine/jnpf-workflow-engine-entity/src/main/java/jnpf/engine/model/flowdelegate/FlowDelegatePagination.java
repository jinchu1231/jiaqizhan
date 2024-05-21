package jnpf.engine.model.flowdelegate;

import jnpf.base.Pagination;
import lombok.Data;

/**
 * 流程设计
 *
 * @author JNPF开发平台组
 * @version V3.4.2
 * @copyright 引迈信息技术有限公司
 * @date 2022/10/14 9:23:27
 */
@Data
public class FlowDelegatePagination extends Pagination {
    private String myOrDelagateToMe;
}
