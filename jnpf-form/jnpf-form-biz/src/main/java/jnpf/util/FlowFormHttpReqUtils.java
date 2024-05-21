package jnpf.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import jnpf.base.ActionResult;
import jnpf.base.ActionResultCode;
import jnpf.config.JnpfOauthConfig;
import jnpf.entity.FlowFormEntity;
import jnpf.exception.WorkFlowException;
import jnpf.util.wxutil.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 流程表单 http请求处理表单
 *
 * @author JNPF开发平台组
 * @version V3.4.5
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2022/10/21
 */
@Component
public class FlowFormHttpReqUtils {

    private static JnpfOauthConfig jnpfOauthConfig;

    @Autowired
    public void setJnpfOauthConfig(JnpfOauthConfig jnpfOauthConfig) {
        FlowFormHttpReqUtils.jnpfOauthConfig = jnpfOauthConfig;
    }

    public Map<String, Object> info(FlowFormEntity flowFormEntity, String id, String token) {
        String requestURL = this.getReqURL(flowFormEntity, id);
        JSONObject jsonObject = HttpUtil.httpRequest(requestURL, "GET" , null, token);
        ActionResult actionResult = JSON.toJavaObject(jsonObject, ActionResult.class);
        if (actionResult == null) {
            return new HashMap<>();
        }
        Object data = actionResult.getData();
        return data != null ? JsonUtil.entityToMap(data) : new HashMap<>();
    }

    public boolean isUpdate(FlowFormEntity flowFormEntity, String id, String token) {
        String requestURL = this.getReqURL(flowFormEntity, id);
        JSONObject jsonObject = HttpUtil.httpRequest(requestURL, "GET" , null, token);
        ActionResult actionResult = JSON.toJavaObject(jsonObject, ActionResult.class);
        return actionResult != null && actionResult.getData() != null;
    }

    public void create(FlowFormEntity flowFormEntity, String id, String token, Map<String, Object> map) throws WorkFlowException {
        String requestURL = this.getReqURL(flowFormEntity, id);
        JSONObject jsonObject = HttpUtil.httpRequest(requestURL, "POST" , JsonUtil.getObjectToString(map), token);
        ActionResult actionResult = JSON.toJavaObject(jsonObject, ActionResult.class);
        boolean b = actionResult!=null && ActionResultCode.Success.getCode().equals(actionResult.getCode());
        if (!b) {
            String msg = actionResult!=null?actionResult.getMsg():"未找到接口";
            throw new WorkFlowException(msg);
        }
    }

    public void update(FlowFormEntity flowFormEntity, String id, String token, Map<String, Object> map) throws WorkFlowException {
        String requestURL = this.getReqURL(flowFormEntity, id);
        JSONObject jsonObject = HttpUtil.httpRequest(requestURL, "PUT" , JsonUtil.getObjectToString(map), token);
        ActionResult actionResult = JSON.toJavaObject(jsonObject, ActionResult.class);
        boolean b = actionResult!=null && ActionResultCode.Success.getCode().equals(actionResult.getCode());
        if (!b) {
            String msg = actionResult!=null?actionResult.getMsg():"未找到接口";
            throw new WorkFlowException(msg);
        }
    }

    public void saveOrUpdate(FlowFormEntity flowFormEntity, String id, String token, Map<String, Object> map) throws WorkFlowException {
        boolean update = this.isUpdate(flowFormEntity, id, token);
        if (update) {
            this.update(flowFormEntity, id, token, map);
        } else {
            this.create(flowFormEntity, id, token, map);
        }
    }


    private String getReqURL(FlowFormEntity flowFormEntity, String id) {
        HttpServletRequest request = ServletUtil.getRequest();
        //请求来源
        String requestURL = flowFormEntity.getInterfaceUrl();
        boolean isHttp = requestURL.toLowerCase().startsWith("http" );
        if (!isHttp) {
            //补全(内部)
            requestURL = jnpfOauthConfig.getJnpfDomain() + requestURL;
        }
        return requestURL + "/" + id;
    }


}
