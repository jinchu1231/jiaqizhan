package jnpf.base.model.synthirdinfo;

import lombok.Data;

/**
 * 第三方工具的对象同步表
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/4/26 16:18
 */
@Data
public class SynThirdMsgModel {
    private String sysObjId;
    private String thirdObjId;
    private Integer isSynOk;
    private String errorMsg;
    private String synstate;
}
