package jnpf.message.model.websocket;

import jnpf.message.entity.ImContentEntity;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 消息分页返回模型
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-07-05
 */
@Data
@Builder
public class PaginationMessageVo implements Serializable {

    /**
     * 消息列表
     */
    private List<ImContentEntity> list;

    /**
     * 分页参数
     */
    private PaginationMessageModel pagination;

    /**
     * 方法名
     */
    private String method;
}
