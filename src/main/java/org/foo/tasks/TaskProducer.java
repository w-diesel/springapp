package org.foo.tasks;

import org.apache.log4j.Logger;
import org.foo.Application;
import org.foo.data.IPersonRepositoryCustom;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class TaskProducer implements ITaskProducer {

    Logger log = Logger.getLogger(TaskProducer.class);

    @Autowired
    Environment env;

    @Autowired
    IPersonRepositoryCustom personRepositoryCustom;

    @Autowired
    private AmqpTemplate template;

    @Override
    public String sendTask(Integer criteria, String jobId) {

        if (jobId == null) {
            jobId = UUID.randomUUID().toString();
        }

        int NumberRetries = Integer.valueOf(env.getProperty("org.foo.rabbitmq.retries_action_number", "1"));

        personRepositoryCustom.saveJob(jobId, criteria);

        while (NumberRetries != 0) {
            try {
                template.convertAndSend(Application.TASKS_QUEUE, jobId);
                NumberRetries = 0;
            } catch (org.springframework.amqp.AmqpConnectException e) {
                log.warn("cannot perform action. check connection to rabbitmq server," +
                        " the retry will be performed in 1 minute");
                try {
                    TimeUnit.SECONDS.sleep(60);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                --NumberRetries;
            }
        }
        return jobId;
    }

}