package jnpf.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.base.entity.ModuleDataAuthorizeLinkEntity;
import jnpf.base.mapper.ModuleDataAuthorizeLinkDataMapper;
import jnpf.base.service.ModuleDataAuthorizeLinkDataService;
import jnpf.base.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据权限方案
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Service
public class ModuleDataAuthorizeLinkDataServiceImpl extends SuperServiceImpl<ModuleDataAuthorizeLinkDataMapper, ModuleDataAuthorizeLinkEntity> implements ModuleDataAuthorizeLinkDataService {


	@Override
	public ModuleDataAuthorizeLinkEntity getLinkDataEntityByMenuId(String menuId,Integer type) {
		QueryWrapper<ModuleDataAuthorizeLinkEntity> linkEntityQueryWrapper = new QueryWrapper<>();
		linkEntityQueryWrapper.lambda().eq(ModuleDataAuthorizeLinkEntity::getModuleId,menuId).eq(ModuleDataAuthorizeLinkEntity::getDataType,type);
		List<ModuleDataAuthorizeLinkEntity> list = this.list(linkEntityQueryWrapper);
		if (list.size()>0){
			return list.get(0);
		}
		return new ModuleDataAuthorizeLinkEntity();
	}
}
