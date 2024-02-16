package ru.job4j.quartz;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import ru.job4j.repo.Store;
import ru.job4j.utils.Parse;

public interface Grab {
    void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException;
}
