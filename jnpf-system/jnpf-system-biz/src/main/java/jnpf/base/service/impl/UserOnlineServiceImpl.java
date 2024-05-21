package jnpf.base.service.impl;

import jnpf.base.Pagination;
import jnpf.base.UserInfo;
import jnpf.base.model.UserOnlineModel;
import jnpf.base.service.UserOnlineService;
import jnpf.util.AuthUtil;
import jnpf.util.CacheKeyUtil;
import jnpf.util.PageUtil;
import jnpf.util.RedisUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 在线用户
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月26日 上午9:18
 */
@Service
public class UserOnlineServiceImpl implements UserOnlineService {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private CacheKeyUtil cacheKeyUtil;

    @Override
    public List<UserOnlineModel> getList(Pagination page) {
        List<UserOnlineModel> userOnlineList = new ArrayList<>();
        List<String> tokens = UserProvider.getLoginUserListToken();
        for (String token : tokens) {
            UserInfo userInfo = UserProvider.getUser(token);
            if(userInfo.getId() != null){
                if(UserProvider.isTempUser(userInfo)){
                    //临时用户不显示
                    continue;
                }
                UserOnlineModel userOnlineModel = new UserOnlineModel();
                userOnlineModel.setUserId(userInfo.getUserId());
                userOnlineModel.setUserName((userInfo.getUserName()) + "/" + userInfo.getUserAccount());
                userOnlineModel.setLoginIPAddress(userInfo.getLoginIpAddress());
                userOnlineModel.setLoginAddress(userInfo.getLoginIpAddressName());
                userOnlineModel.setOrganize(userInfo.getOrganize());
                userOnlineModel.setLoginTime(userInfo.getLoginTime());
                userOnlineModel.setTenantId(userInfo.getTenantId());
                userOnlineModel.setToken(token);
                userOnlineModel.setDevice(userInfo.getLoginDevice());
                userOnlineModel.setLoginBrowser(userInfo.getBrowser());
                userOnlineModel.setLoginSystem(userInfo.getLoginPlatForm());
                userOnlineList.add(userOnlineModel);
            }
        }
        String tenantId =userProvider.get().getTenantId();
        userOnlineList = userOnlineList.stream().filter(t -> String.valueOf(t.getTenantId()).equals(String.valueOf(tenantId))).collect(Collectors.toList());
        if(!StringUtil.isEmpty(page.getKeyword())){
            userOnlineList=userOnlineList.stream().filter(t->t.getUserName().contains(page.getKeyword())).collect(Collectors.toList());
        }
        userOnlineList.sort(Comparator.comparing(UserOnlineModel::getLoginTime).reversed());
        page.setTotal(userOnlineList.size());
        userOnlineList = PageUtil.getListPage((int) page.getCurrentPage(), (int) page.getPageSize(), userOnlineList);
        return userOnlineList;
    }

    @Override
    public void delete(String... token) {
        AuthUtil.kickoutByToken(token);
    }
}
