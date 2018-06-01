package com.bullhorn.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import com.bullhorn.service.Consumer;

@Service
public class ConsumerAsyncService {
    @Autowired
    private TaskExecutor taskExecutor;

    private Consumer consumer;

    public void executeAsynchronously() {
        taskExecutor.execute(consumer);
    }

    public ConsumerAsyncService(@Qualifier("consumer") Consumer consumer) {
        this.consumer = consumer;
    }
}