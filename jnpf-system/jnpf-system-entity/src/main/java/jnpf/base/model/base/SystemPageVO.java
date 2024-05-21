package jnpf.base.model.base;

import jnpf.base.Page;
import lombok.Data;

/**
 * 类功能
 *
 * @author JNPF开发平台组 YanYu
 * @version v3.4.6
 * @copyrignt 引迈信息技术有限公司
 * @date 2023-02-27
 */
@Data
public class SystemPageVO extends Page {

    private String enabledMark;

    private Boolean selector;
}
