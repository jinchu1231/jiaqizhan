package jnpf.base.util.job;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScheduleConfig {

    @Bean
    public JobDetail scheduleJobDetail() {
        JobDetail JobDetail = JobBuilder.newJob(Schedule.class)
                .storeDurably() //必须调用该方法，添加任务
                .build();
        return JobDetail;
    }

    @Bean
    public Trigger scheduleTrigger() {
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule("0 0/5 * * * ?");
        Trigger trigger = TriggerBuilder.newTrigger()
                .forJob(scheduleJobDetail())
                .withSchedule(cronScheduleBuilder) //对触发器配置任务
                .build();
        return trigger;
    }

}
