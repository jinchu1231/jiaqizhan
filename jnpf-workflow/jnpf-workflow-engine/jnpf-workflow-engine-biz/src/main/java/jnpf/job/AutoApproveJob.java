package jnpf.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * 超时设置定时器
 *
 * @author JNPF开发平台组
 * @version V3.4.2
 * @copyright 引迈信息技术有限公司
 * @date 2022/6/15 16:23
 */
public class AutoApproveJob extends QuartzJobBean {

    public static final String autoApprove = "idgenerator_AutoApprove";

    @Autowired
    private WorkTimeoutJobUtil workTimeoutJobUtil;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        workTimeoutJobUtil.approveModel(redisTemplate);
    }

}
