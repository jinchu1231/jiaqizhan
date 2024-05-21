package jnpf.service;

import jnpf.base.ActionResult;
import jnpf.exception.LoginException;
import jnpf.model.LoginVO;

import java.util.Map;

public interface AuthService {
    ActionResult<LoginVO> login(Map<String, String> parameters) throws LoginException;

    ActionResult kickoutByToken(String... tokens);

    ActionResult kickoutByUserId(String userId, String tenantId);
}
