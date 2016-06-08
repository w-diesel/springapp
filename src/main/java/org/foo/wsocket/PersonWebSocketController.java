package org.foo.wsocket;

import org.foo.data.IPersonRepository;
import org.foo.data.IPersonRepositoryCustom;
import org.foo.data.models.Job;
import org.foo.tasks.ITaskProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;

@Controller
public class PersonWebSocketController {

    static final Logger log = LoggerFactory.getLogger(PersonWebSocketController.class);

    @Autowired
    ITaskProducer taskProducer;

    @Autowired
    IPersonRepository personRepository;

    @Autowired
    IPersonRepositoryCustom personRepositoryCustom;

    @MessageMapping("/hello")
    @SendTo("/topic/birthdays")
    public ResponseEntity greeting() throws InterruptedException {

        Job job;
        final Integer month = LocalDate.now().getMonth().getValue();

        log.info(" request for a search of persons' birthday within month " + month);
        String jobId = taskProducer.sendTask(month, null);
        log.debug(" jobId " + jobId);

        while (true) {

            Thread.sleep(1000);
            job = personRepositoryCustom.checkJobByKey(jobId, month, Job.Status.DONE);
            log.debug(" sleeping for 1 sec " + job);
            if ( job !=null && job.getStatus().equals(Job.Status.DONE) ) break;

        }
        return new ResponseEntity(job.getPayload(), OK);
    }

}
