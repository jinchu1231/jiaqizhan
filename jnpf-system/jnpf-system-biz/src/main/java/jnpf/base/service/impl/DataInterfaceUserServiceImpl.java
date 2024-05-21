package jnpf.base.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.base.entity.DataInterfaceUserEntity;
import jnpf.base.mapper.DataInterfaceUserMapper;
import jnpf.base.model.InterfaceOauth.InterfaceUserForm;
import jnpf.base.service.DataInterfaceUserService;
import jnpf.base.service.SuperServiceImpl;
import jnpf.util.AuthUtil;
import jnpf.util.DateUtil;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JNPF开发平台组
 * @version V3.4.7
 * @copyright 引迈信息技术有限公司
 * @date 2021/9/20 9:22
 */
@Service
@Slf4j
public class DataInterfaceUserServiceImpl extends SuperServiceImpl<DataInterfaceUserMapper, DataInterfaceUserEntity> implements DataInterfaceUserService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    protected AuthUtil authUtil;

    @Override
    public void saveUserList(InterfaceUserForm interfaceUserForm) {
        if (interfaceUserForm.getUserIds() != null) {
            List<String> userList = interfaceUserForm.getUserIds();
            List<DataInterfaceUserEntity> select = this.select(interfaceUserForm.getInterfaceIdentId());
            List<String> dbList = select.stream().map(DataInterfaceUserEntity::getUserId).collect(Collectors.toList());

            List<String> saveList = userList.stream().filter(t -> !dbList.contains(t)).collect(Collectors.toList());
            List<DataInterfaceUserEntity> updateList = select.stream().filter(t -> userList.contains(t.getUserId())).collect(Collectors.toList());
            List<DataInterfaceUserEntity> deleteList = select.stream().filter(t -> !userList.contains(t.getUserId())).collect(Collectors.toList());

            for (String userId : saveList) {
                DataInterfaceUserEntity entity = new DataInterfaceUserEntity();
                entity.setId(RandomUtil.uuId());
                entity.setUserKey(RandomUtil.uuId().substring(2));
                entity.setOauthId(interfaceUserForm.getInterfaceIdentId());
                entity.setUserId(userId);
                entity.setCreatorUserId(userProvider.get().getUserId());
                entity.setCreatorTime(DateUtil.getNowDate());
                this.save(entity);
            }
            for (DataInterfaceUserEntity updateE : updateList) {
                this.updateById(updateE);
            }
            for (DataInterfaceUserEntity deleteE : deleteList) {
                this.removeById(deleteE.getId());
            }
        }
    }

    @Override
    public List<DataInterfaceUserEntity> select(String oauthId) {
        QueryWrapper<DataInterfaceUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DataInterfaceUserEntity::getOauthId, oauthId);
        return this.list(queryWrapper);
    }

    @Override
    public String getInterfaceUserToken(String tenantId, String oauthId, String userKey) {
        List<DataInterfaceUserEntity> select = this.select(oauthId);
        if (CollectionUtil.isEmpty(select)) {
            return null;
        }
        if (StringUtil.isEmpty(userKey)) {
            throw new RuntimeException("未填写UserKey，请确认");
        }
        DataInterfaceUserEntity entity = select.stream().filter(item -> item.getUserKey().equals(userKey)).findFirst().orElse(null);
        if (entity == null) {
            throw new RuntimeException("UserKey不匹配，请填写正确的UserKey");
        }

        String token = authUtil.loginTempUser(entity.getUserId(), tenantId, true);
        return token;
    }
}
