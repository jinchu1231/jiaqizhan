
package jnpf.message.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.base.service.SuperService;
import jnpf.message.entity.TemplateParamEntity;
import jnpf.message.model.messagetemplateconfig.MessageTemplateConfigPagination;

import java.util.List;
/**
 *
 * 消息模板（新）
 * 版本： V3.2.0
 * 版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * 作者： JNPF开发平台组
 * 日期： 2022-08-18
 */
public interface TemplateParamService extends SuperService<TemplateParamEntity> {

	QueryWrapper<TemplateParamEntity> getChild(MessageTemplateConfigPagination pagination, QueryWrapper<TemplateParamEntity> templateParamQueryWrapper);

	TemplateParamEntity getInfo(String id);

	List<TemplateParamEntity> getDetailListByParentId(String id);

	List<TemplateParamEntity> getParamList(String id,List<String> params);
}
