package com.bullhorn.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import com.bullhorn.service.Consumer;

@Service
public class ConsumerAsyncService {
    @Autowired
    private TaskExecutor taskExecutor;
    
    @Autowired
    Consumer consumer;
    
    public void executeAsynchronously() {
        taskExecutor.execute(consumer);
    }
}