package org.foo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.log4j.Logger;
import org.foo.data.IPersonRepositoryCustom;
import org.foo.data.models.Job;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RabbitMqListener {
    Logger log = Logger.getLogger(RabbitMqListener.class);

    @Autowired
    IPersonRepositoryCustom personRepositoryCustom;

    @RabbitListener(queues = Application.TASKS_QUEUE)
    public void worker(String message) throws InterruptedException, IOException {
        log.info("worker msg: " + message);

//        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new JavaTimeModule());
//        Job job = mapper.readValue(message, Job.class);
//        log.info("job creation time : " + job.getСreationDate());

        personRepositoryCustom.writeSelected(message, null, null);
    }
}