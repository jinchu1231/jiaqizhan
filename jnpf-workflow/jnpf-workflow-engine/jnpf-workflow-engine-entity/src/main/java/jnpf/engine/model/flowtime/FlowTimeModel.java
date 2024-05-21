package jnpf.engine.model.flowtime;

import jnpf.engine.model.flowengine.shuntjson.nodejson.ChildNodeList;
import lombok.Data;

import java.util.Date;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/6/18 11:38
 */
@Data
public class FlowTimeModel {
    /**
     * 是否开启限时设置
     */
    private Boolean on = false;
    /**
     * 开始时间
     */
    private Date date = new Date();
    /**
     * 通知
     */
    private ChildNodeList childNode = new ChildNodeList();
    /**
     * 事件
     */
    private ChildNodeList childNodeEvnet = new ChildNodeList();
    /**
     * 节点对象
     */
    private ChildNodeList childNodeList = new ChildNodeList();
}
