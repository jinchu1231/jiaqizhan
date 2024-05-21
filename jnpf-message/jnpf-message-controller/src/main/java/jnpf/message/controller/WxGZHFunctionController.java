package jnpf.message.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.message.entity.AccountConfigEntity;
import jnpf.message.entity.WechatUserEntity;
import jnpf.message.service.AccountConfigService;
import jnpf.message.service.WechatUserService;
import jnpf.message.util.weixingzh.WXGZHWebChatUtil;
import jnpf.message.util.weixingzh.aes.WXBizMsgCrypt;
import jnpf.permission.entity.SocialsUserEntity;
import jnpf.permission.service.SocialsUserService;
import jnpf.util.DateUtil;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.XSSEscape;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 发送消息模型
 *
 */
@Tag(name = "微信公众号事件接收", description = "WechatOpen")
@Controller
@RequestMapping("/api/message/WechatOpen")
@Slf4j
public class WxGZHFunctionController {

    @Autowired
    private AccountConfigService accountConfigService;
    @Autowired
    private WechatUserService wechatUserService;
    @Autowired
    private SocialsUserService socialsUserService;

    /**
     * 服务器基本配置链接微信公众号验证
     *
     * @param request 请求对象
     * @param response 响应对象
     * @return
     */
    @Operation(summary = "服务器基本配置链接微信公众号验证")
    @SaCheckPermission("msgCenter.sendConfig")
    @ResponseBody
    @Parameters({
            @Parameter(name = "enCode", description = "微信公众号账号配置编码", required = true)
    })
    @GetMapping("/token/{enCode}")
    public String token(@PathVariable("enCode") String enCode, HttpServletRequest request, HttpServletResponse response) throws Exception {
        //获取微信公众号账号配置
        AccountConfigEntity accountConfigEntity = accountConfigService.getInfoByEnCode(enCode,"7");
        if(ObjectUtil.isEmpty(accountConfigEntity)){
            log.info("未找到与编码相对应的微信公众号配置");
            return "";
        }
        //微信公众号服务器配置token
        String wxToken = accountConfigEntity.getAgentId();
        String signature = request.getParameter("signature");
        String echostr = XSSEscape.escape(request.getParameter("echostr"));
        String timestamp = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");

        String sortStr = WXGZHWebChatUtil.sort(wxToken,timestamp,nonce);
        String MySinStr = WXGZHWebChatUtil.shal(sortStr);
        if(StringUtil.isNotBlank(signature) && MySinStr.equals(signature)){
            return echostr;
        }else {
            log.info("微信公众号链接失败");
            return echostr;
        }
    }


    /**
     * 微信公众号事件请求
     *
     * @param request 请求对象
     * @param response 响应对象
     * @return
     * @throws Exception
     */
    @Operation(summary = "微信公众号事件请求")
    @SaCheckPermission("msgCenter.sendConfig")
    @ResponseBody
    @PostMapping("/token/{enCode}")
    public String tokenPost(@PathVariable("enCode") String enCode,HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("微信公众号请求事件");
        //获取微信公众号账号配置
        AccountConfigEntity accountConfigEntity = accountConfigService.getInfoByEnCode(enCode,"7");
        if(ObjectUtil.isEmpty(accountConfigEntity)){
            log.info("未找到与编码相对应的微信公众号配置");
            return "";
        }
        //微信公众号服务器配置token
        String wxToken = accountConfigEntity.getAgentId();
        //微信公众号服务器配置EncodingAesKey
        String encodingAesKey = accountConfigEntity.getBearer();
        //微信公众号AppId
        String wxAppId = accountConfigEntity.getAppId();

        // 获取系统配置
        String msgSignature  = request.getParameter("msg_signature");
        String encrypt_type = request.getParameter("encrypt_type");

        String signature = request.getParameter("signature");
        String echostr = XSSEscape.escape(request.getParameter("echostr"));
        String timestamp = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");

        String sortStr = WXGZHWebChatUtil.sort(wxToken,timestamp,nonce);
        String MySinStr = WXGZHWebChatUtil.shal(sortStr);
        //验签
        if(StringUtil.isNotBlank(signature) && MySinStr.equals(signature)){
            //事件信息
            Map<String ,String> map = WXGZHWebChatUtil.parseXml(request);
            //事件信息
            String Event = map.get("Event");
            String openid = map.get("FromUserName");
            //公众号原始id
            String gzhId = map.get("ToUserName");
            if("aes".equals(encrypt_type)) {
                WXBizMsgCrypt pc = new WXBizMsgCrypt(wxToken, encodingAesKey, wxAppId);
                String encrypt = map.get("Encrypt");
                String format = "<xml><ToUserName><![CDATA[toUser]]></ToUserName><Encrypt><![CDATA[%1$s]]></Encrypt></xml>";
                String fromXML = String.format(format, encrypt);
                // 获取解密后消息明文
                String result = pc.decryptMsg(msgSignature, timestamp, nonce, fromXML);
                Map<String, String> resultMap = new HashMap<>();
                resultMap = WXGZHWebChatUtil.xmlToMap(result);
                // 获取解密后事件信息
                Event = resultMap.get("Event");
                openid = resultMap.get("FromUserName");
                gzhId = resultMap.get("ToUserName");
            }

            String appId = accountConfigEntity.getAppId();
            String appsecret = accountConfigEntity.getAppSecret();
            String token = WXGZHWebChatUtil.getAccessToken(appId,appsecret);
            if("subscribe".equals(Event)){
                //用户关注事件
                if(StringUtil.isNotBlank(token)){
                    JSONObject rstObj = WXGZHWebChatUtil.getUsetInfo(token,openid);
                    if(rstObj.containsKey("unionid")){
                        String unionid = rstObj.getString("unionid");
                        SocialsUserEntity socialsUserEntity = socialsUserService.getInfoBySocialId(unionid,"wechat_open");
//                        SocialsUserInfo socialsUserInfo = socialsUserService.getUserInfo("wechat_applets",unionid,null);
                        if(socialsUserEntity==null){
                            log.info("微信公众号未绑定系统账号，请登录小程序绑定");
                            return "";
                        }else{
                            WechatUserEntity wechatUserEntity = wechatUserService.getInfoByGzhId(socialsUserEntity.getUserId(),gzhId);
                            if(wechatUserEntity==null){
                                WechatUserEntity entity = new WechatUserEntity();
                                entity.setId(RandomUtil.uuId());
                                 entity.setUserId(socialsUserEntity.getUserId());
                                entity.setGzhId(gzhId);
                                entity.setCloseMark(1);
                                entity.setCreatorTime(DateUtil.getNowDate());
                                entity.setOpenId(openid);
                                wechatUserService.create(entity);
                                return "";
                            }else {
                                if(wechatUserEntity.getCloseMark()==0){
                                    wechatUserEntity.setCloseMark(1);
                                }
                                wechatUserEntity.setOpenId(openid);
                                wechatUserEntity.setLastModifyTime(DateUtil.getNowDate());
                                wechatUserService.update(wechatUserEntity.getId(),wechatUserEntity);
                            }
                            return "";
                        }
                    }else{
                        log.info("微信公众号未绑定系统账号，请登录小程序绑定");
                        return "";
                    }
                }else{
                    log.error("微信公众号token错误，请查看配置");
                    return "";
                }
            }else if("unsubscribe".equals(Event)){
                //用户取消关注事件
                if(StringUtil.isNotBlank(token)){
                    JSONObject rstObj = WXGZHWebChatUtil.getUsetInfo(token,openid);
                    if(rstObj.containsKey("unionid")){
                        String unionid = rstObj.getString("unionid");
                        SocialsUserEntity socialsUserEntity = socialsUserService.getInfoBySocialId(unionid,"wechat_open");
                        if(socialsUserEntity==null){
                            log.info("微信公众号未绑定系统账号，请登录小程序绑定");
                        }else{
                            WechatUserEntity wechatUserEntity = wechatUserService.getInfoByGzhId(socialsUserEntity.getUserId(),gzhId);
                            if(wechatUserEntity==null){
                                WechatUserEntity entity = new WechatUserEntity();
                                entity.setId(RandomUtil.uuId());
                                entity.setUserId(socialsUserEntity.getUserId());
                                entity.setGzhId(gzhId);
                                entity.setCloseMark(0);
                                entity.setCreatorTime(DateUtil.getNowDate());
                                entity.setOpenId(openid);
                                wechatUserService.create(entity);
                                return "";
                            }else {
                                if(wechatUserEntity.getCloseMark()==1){
                                    wechatUserEntity.setCloseMark(0);
                                }
                                wechatUserEntity.setOpenId(openid);
                                wechatUserEntity.setLastModifyTime(DateUtil.getNowDate());
                                wechatUserService.update(wechatUserEntity.getId(),wechatUserEntity);
                                return "";
                            }
                        }
                    }else{
                        log.info("微信公众号未绑定系统账号，请登录小程序绑定");
                        return "";
                    }
                }else{
                    log.error("微信公众号token错误，请查看配置");
                    return "";
                }
                return "";
            }else {
                return "";
            }
        }else {
            log.info("微信公众号事件请求失败");
            return echostr;
        }
    }

}
