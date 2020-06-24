package com.ldchina.datacenter.spring;

import com.ldchina.datacenter.mina.MinaTcpServerHandler;
import com.ldchina.datacenter.types.Sessions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Component
public class ScheduledTasks implements SchedulingConfigurer {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.setScheduler(setTaskExecutors());
    }
    @Bean
    public Executor setTaskExecutors(){
        return Executors.newScheduledThreadPool(2); // 5个线程来处理。
    }

    /**
     * 每天校时任务
     */
    @Scheduled(cron = "30 0 12 * * ?")//每天12:00:30秒执行一次
    public void setStationTime(){
        log.info("Synchronization time");
        String cmd = "TIME " + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        for (Map.Entry<String, Sessions> entry : MinaTcpServerHandler.sessionsMap.entrySet()) {
            entry.getValue().ioSession.write(cmd);
        }
    }
    /**
     * 站点线程状态检查
     */
    @Scheduled(fixedRate = 3* 60*1000)//每30秒执行一次
    public void stationThreadCheck(){
//        for(ReceiveThread trd : receiveThreadMap.values()){
//            if(trd.isAlive()){
//                log.info("["+trd.stationId+"]"+"线程正常.");
//            }else{
//                log.error("["+trd.stationId+"]"+"线程异常退出,现重新启动.");
//                trd.start();
//            }
//        }
    }
}
