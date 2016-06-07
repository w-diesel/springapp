package org.foo.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.foo.Application;
import org.foo.data.IPersonRepositoryCustom;
import org.foo.data.models.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;


@RestController
@RequestMapping(path = "/persons/")
public class PersonRestController {

    static final Logger log = LoggerFactory.getLogger(PersonRestController.class);

    @Autowired
    IPersonRepositoryCustom personRepositoryCustom;

    @Autowired
    AmqpTemplate template;

    @ResponseStatus(ACCEPTED)
    @RequestMapping(path = "emit", method = GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity mqInitJob(
        @RequestParam(required = false, name = "byMonth") Integer month
    ) throws JsonProcessingException {

        ResponseEntity response;

        log.info("Emit to queue");

        if (month != null && (month < 1 || month > 12)) {
            response = new ResponseEntity(BAD_REQUEST);
        } else {
            if (month == null) month = LocalDate.now().getMonth().getValue();
            String jobId = UUID.randomUUID().toString();

            log.info(" request for a search of persons' birthday within month " + month);
            log.debug(" jobId " + jobId);

            personRepositoryCustom.initJob(jobId, month);

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

//            String json = mapper.writeValueAsString(job);
//            template.convertAndSend(Application.TASKS_QUEUE, json);
//            template.convertAndSend(Application.TASKS_QUEUE, json);
            template.convertAndSend(Application.TASKS_QUEUE, jobId);

            response = new ResponseEntity(jobId, OK);
        }

        return response;
    }


    @ResponseStatus(ACCEPTED)
    @RequestMapping(path = "init", method = GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity initJob(
            @RequestParam(required = false, name = "byMonth") Integer month
    ) {
        ResponseEntity response;

        if (month != null && (month < 1 || month > 12)) {
            response = new ResponseEntity(BAD_REQUEST);
        } else {
            if (month == null) month = LocalDate.now().getMonth().getValue();
            String jobId = UUID.randomUUID().toString();

            log.info(" request for a search of persons' birthday within month " + month);
            log.debug(" jobId " + jobId);

            personRepositoryCustom.writeSelected(jobId, null, month);
            response = new ResponseEntity(jobId, OK);
        }

        return response;
    }


    @RequestMapping(path = "/birthdays", method = GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity getResult(
            @RequestParam(required = false, name = "byMonth") Integer month,
            @RequestParam(name = "jobId") String jobId
    ) {
        log.debug(" Checking job with Id: " + jobId);

        ResponseEntity response;
        String msg = "";

        if (jobId == null) return null;
        Job job = personRepositoryCustom.checkJobByKey(jobId, month);

        if (job != null) {
            List payload = job.getPayload();

            if (job.getStatus() == Job.Status.PENDING) msg = "The job is in process.. try later";
            else if (payload.isEmpty()) msg = "No results were found";
            response = new ResponseEntity(msg, OK);

            if (msg.isEmpty()) response = new ResponseEntity(payload, OK);

        } else {
            response = new ResponseEntity("404", NOT_FOUND);
        }

        return response;
    }

}
