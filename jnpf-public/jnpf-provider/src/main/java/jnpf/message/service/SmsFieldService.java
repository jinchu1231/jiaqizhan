
package jnpf.message.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.base.service.SuperService;
import jnpf.message.entity.SmsFieldEntity;
import jnpf.message.model.messagetemplateconfig.MessageTemplateConfigPagination;

import java.util.List;
import java.util.Map;

/**
 *
 * 消息模板（新）
 * 版本： V3.2.0
 * 版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * 作者： JNPF开发平台组
 * 日期： 2022-08-18
 */
public interface SmsFieldService extends SuperService<SmsFieldEntity> {

	QueryWrapper<SmsFieldEntity> getChild(MessageTemplateConfigPagination pagination, QueryWrapper<SmsFieldEntity> smsFieldQueryWrapper);

	SmsFieldEntity getInfo(String id);

	List<SmsFieldEntity> getDetailListByParentId(String id);

	List<SmsFieldEntity> getParamList(String id,List<String> params);

	Map<String,Object> getParamMap(String templateId,Map<String,Object> map);
}
