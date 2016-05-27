package org.foo.wsocket;

import org.foo.data.IPersonRepository;
import org.foo.data.IPersonRepositoryCustom;
import org.foo.data.models.Job;
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

    final static Logger log = LoggerFactory.getLogger(PersonWebSocketController.class);

    @Autowired
    IPersonRepository personRepository;

    @Autowired
    IPersonRepositoryCustom personRepositoryCustom;

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public ResponseEntity greeting() throws Exception {

        Job job;
        UUID jobId = UUID.randomUUID();
        final Integer month = LocalDate.now().getMonth().getValue();
        final String ID = jobId.toString();

        log.info(" request for a search of persons' birthday within month " + month);
        log.debug(" jobId " + jobId);

        personRepositoryCustom.writeSelected(ID, personRepository.findAll(), month);

        while (true) {
            Thread.sleep(1000);
            job = personRepositoryCustom.checkJobByKey(ID, month);
            log.debug(" sleeping for 1 sec " + job);
            if ( job !=null && job.getStatus().equals(Job.Status.DONE) ) break;
        }
        return new ResponseEntity(job.getPayload(), OK);
    }

}
