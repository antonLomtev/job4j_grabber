package ru.job4j.grabber;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.model.Post;
import ru.job4j.quartz.Grab;
import ru.job4j.quartz.HabrCareerParse;
import ru.job4j.quartz.PsqlStore;
import ru.job4j.repo.Store;
import ru.job4j.utils.HabrCareerDateTimeParser;
import ru.job4j.utils.Parse;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class Grabber implements Grab {
    private final Parse parse;
    private final Store store;
    private final Scheduler scheduler;
    private final int time;

    public Grabber(Parse parse, Store store, Scheduler scheduler, int time) {
        this.parse = parse;
        this.store = store;
        this.scheduler = scheduler;
        this.time = time;
    }

    @Override
    public void init() throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("store", store);
        data.put("parse", parse);
        JobDetail job = newJob(GrabJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(time)
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    public static class GrabJob implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            JobDataMap map = context.getJobDetail().getJobDataMap();
            Store store = (Store) map.get("store");
            Parse parse = (Parse) map.get("parse");
            String link = "https://career.habr.com";
            List<Post> posts = parse.list(link);
            for (Post post : posts) {
                store.save(post);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        var config = new Properties();
        try (InputStream in = Grabber.class.getClassLoader().getResourceAsStream("app.properties")) {
            config.load(in);
        }
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        var parse = new HabrCareerParse(new HabrCareerDateTimeParser());
        var store = new PsqlStore(config);
        var time = Integer.parseInt(config.getProperty("time"));
        new Grabber(parse, store, scheduler, time).init();
    }
}
