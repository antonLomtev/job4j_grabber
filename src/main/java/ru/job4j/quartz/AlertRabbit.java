package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class AlertRabbit {
    public static void main(String[] args) {
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            Properties config = new Properties();
            config.load(in);
            Class.forName(config.getProperty("driver-class-name"));
            try (Connection connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password"))) {
                Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
                scheduler.start();
                JobDataMap data = new JobDataMap();
                data.put("BD", connection);
                JobDetail job = newJob(Rabbit.class)
                        .usingJobData(data)
                        .build();
                SimpleScheduleBuilder times = simpleSchedule()
                        .withIntervalInSeconds(getInterval())
                        .repeatForever();
                Trigger trigger = newTrigger()
                        .startNow()
                        .withSchedule(times)
                        .build();
                scheduler.scheduleJob(job, trigger);
                Thread.sleep(10_000);
                scheduler.shutdown();
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    private static Integer getInterval() {
        Properties config = new Properties();
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            config.load(in);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return Integer.valueOf(config.getProperty("rabbit.interval"));
    }

    public static class Rabbit implements Job {

        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

            Connection connection = (Connection) jobExecutionContext.getJobDetail().getJobDataMap().get("BD");
            try (PreparedStatement ps = connection.prepareStatement("insert into rabbit(created_date) values (?);")) {
                ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                ps.execute();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.out.println("Rabbit runs here ...");
        }
    }
}
