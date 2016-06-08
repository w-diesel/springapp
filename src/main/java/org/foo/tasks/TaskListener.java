package org.foo.tasks;

import org.apache.log4j.Logger;
import org.foo.Application;
import org.foo.data.IPersonRepositoryCustom;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TaskListener {
    Logger log = Logger.getLogger(TaskListener.class);

    @Autowired
    IPersonRepositoryCustom personRepositoryCustom;

    @Async
    @RabbitListener(queues = Application.TASKS_QUEUE)
    public void worker(String message) throws InterruptedException, IOException {
        log.info(" starting work on task id (queue worker): " + message);

//        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new JavaTimeModule());
//        Job job = mapper.readValue(message, Job.class);
//        log.info("job creation time : " + job.getStartTime());

        //TODO send no ack in case of exception, for redelivery
        personRepositoryCustom.writeSelected(message, null, null);
    }
}