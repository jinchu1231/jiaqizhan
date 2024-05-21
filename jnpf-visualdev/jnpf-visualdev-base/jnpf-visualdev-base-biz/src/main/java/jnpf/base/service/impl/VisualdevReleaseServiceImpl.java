package jnpf.base.service.impl;


import jnpf.base.service.SuperServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.base.entity.VisualdevReleaseEntity;
import jnpf.base.mapper.VisualdevReleaseMapper;
import jnpf.base.service.VisualdevReleaseService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021/3/16
 */
@Service
public class VisualdevReleaseServiceImpl extends SuperServiceImpl<VisualdevReleaseMapper, VisualdevReleaseEntity> implements VisualdevReleaseService {

	@Override
	public long beenReleased(String id) {
		QueryWrapper<VisualdevReleaseEntity> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(VisualdevReleaseEntity::getId, id);
		return this.count(queryWrapper);
	}

	@Override
	public List<VisualdevReleaseEntity> selectorList() {
		QueryWrapper<VisualdevReleaseEntity> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().select(
				VisualdevReleaseEntity::getId,
				VisualdevReleaseEntity::getFullName,
				VisualdevReleaseEntity::getWebType,
				VisualdevReleaseEntity::getEnableFlow,
				VisualdevReleaseEntity::getType,
				VisualdevReleaseEntity::getCategory);
		return this.list(queryWrapper);
	}
}
