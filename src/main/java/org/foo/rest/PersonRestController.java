package org.foo.rest;

import org.foo.data.IPersonRepositoryCustom;
import org.foo.data.models.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;


@RestController
@RequestMapping(path = "/persons/")
public class PersonRestController {

    final static Logger log = LoggerFactory.getLogger(PersonRestController.class);

    @Autowired
    IPersonRepositoryCustom personRepositoryCustom;

    @ResponseStatus(ACCEPTED)
    @RequestMapping(path = "init", method = GET)
    public String initJob(
            @RequestParam(required = false, name = "byMonth") Integer month
    ) {
        if (month == null) month = LocalDate.now().getMonth().getValue();
        UUID jobId = UUID.randomUUID();

        log.info(" request for a search of persons' birthday within month " + month);
        log.debug(" jobId " + jobId);

        personRepositoryCustom.writeSelected(jobId.toString(), null, month);

        return jobId.toString();
    }


    @RequestMapping(path = "/birthdays", method = GET)
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
