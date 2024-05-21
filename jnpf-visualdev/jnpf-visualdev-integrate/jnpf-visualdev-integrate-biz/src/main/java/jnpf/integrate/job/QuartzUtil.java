package jnpf.integrate.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;

@Slf4j
public class QuartzUtil {

    private static final SchedulerFactory schedulerFactory = new StdSchedulerFactory();

    public static void addJob(String jobName, String cron, Class<? extends Job> jobClass, JobDataMap jobDataMap, Date startDate, Date endDate) {
        if (jobDataMap == null) {
            jobDataMap = new JobDataMap();
        }
        JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName).setJobData(jobDataMap).build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobName)
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .startAt(startDate == null ? new Date() : startDate)
                .endAt(endDate != null ? endDate : null)
                .build();
        try {
            //获取实例化的 Scheduler。
            Scheduler scheduler = getScheduler();
            //将任务及其触发器放入调度器
            scheduler.scheduleJob(jobDetail, trigger);
            //调度器开始调度任务
            if (!scheduler.isShutdown()) {
                scheduler.start();
            }
        } catch (SchedulerException e) {
            log.error("新增调度失败:"+e.getMessage());
        }
    }

    private static Scheduler getScheduler() {
        try {
            return schedulerFactory.getScheduler();
        } catch (SchedulerException e) {
            e.getMessage();
        }
        return null;
    }

    public static void deleteJob(String jobName) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName);
            Scheduler scheduler = getScheduler();
            scheduler.pauseTrigger(triggerKey);
            scheduler.unscheduleJob(triggerKey);
            scheduler.deleteJob(JobKey.jobKey(jobName));
        } catch (SchedulerException e) {
            log.error("删除调度失败:"+e.getMessage());
        }
    }

    private static boolean isTriKey(String jobName) {
        boolean flag = false;
        try {
            Scheduler sched = schedulerFactory.getScheduler();
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName);
            CronTrigger trigger = (CronTrigger) sched.getTrigger(triggerKey);
            flag = trigger != null;
        } catch (Exception e) {
            e.getMessage();
        }
        return flag;
    }

}
