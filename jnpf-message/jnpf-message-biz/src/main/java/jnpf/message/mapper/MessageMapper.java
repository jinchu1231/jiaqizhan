package jnpf.message.mapper;

import jnpf.base.mapper.SuperMapper;
import jnpf.message.entity.MessageEntity;
import jnpf.message.entity.MessageReceiveEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 消息实例
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月27日 上午9:18
 */
public interface MessageMapper extends SuperMapper<MessageEntity> {

    List<MessageReceiveEntity> getMessageList(@Param("map") Map<String, Object> map);

    int getUnreadCount(@Param("userId") String userId,@Param("type") Integer type);

    List<MessageEntity> getInfoDefault(@Param("type") int type);
}
