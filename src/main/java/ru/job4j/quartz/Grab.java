package ru.job4j.quartz;

import org.quartz.SchedulerException;

public interface Grab {
    void init() throws SchedulerException;
}
