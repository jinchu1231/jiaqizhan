package jnpf.message.mapper;

import jnpf.base.mapper.SuperMapper;
import jnpf.message.entity.ImReplyEntity;
import jnpf.message.model.ImReplyListModel;

import java.util.List;

/**
 * 聊天会话
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-05-28
 */
public interface ImReplyMapper extends SuperMapper<ImReplyEntity> {

    /**
     * 聊天会话列表
     * @return
     */
    List<ImReplyListModel> getImReplyList();

}
