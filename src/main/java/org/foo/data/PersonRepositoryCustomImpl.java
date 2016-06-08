package org.foo.data;

import com.mongodb.MongoClient;
import org.foo.data.models.Job;
import org.foo.data.models.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PersonRepositoryCustomImpl implements IPersonRepositoryCustom {

    static final Logger log = LoggerFactory.getLogger(PersonRepositoryCustomImpl.class);

    @Autowired
    IPersonRepository personRepository;

    @Autowired
    Environment env;

    private MongoOperations mongoOps;

    @PostConstruct
    void initMongoOps() {
        try {
            mongoOps = new MongoTemplate(
                    new SimpleMongoDbFactory(
                            new MongoClient(), env.getProperty("spring.data.mongodb.database")
                    )
            );
        } catch (UnknownHostException e) {
            log.info(e.getLocalizedMessage());
        }
    }

    @Override
    public String saveJob(String key, Integer month) {

        Job job = new Job(key, month);
        mongoOps.insert(job);
        log.debug("  job stored (key): " + key);

        return key;
    }

    @Override
    public void writeSelected(String key, List<Person> persons, Integer month) {

        Query searchQuery = new Query(Criteria.where("_id").is(key));
//        Job job = mongoOps.findOne(searchQuery, Job.class);
        Job job = mongoOps.findById(key, Job.class);
        if (job == null){
            throw new RuntimeException(" the job was not found (id) " + key);
        }

        final Integer monthNumber = job.getCriteria();

        try {
            if (persons == null) persons = personRepository.findAll(); // fetching all the data as a "long running task"
            if (persons.size() < 1000) Thread.sleep(60000);            // or waiting for 1 minute

            log.debug("  start of processing persons' data (stream)  job : " + key);

            List<Map> selected = persons.stream()//.parallel()
                    .filter(person -> person.getDateOfBirth().getMonth().getValue() == monthNumber)
                    .map(person -> {
                        Map dto = new HashMap<String, String>(2);
                        int daysToBirthday = person.getDateOfBirth().getDayOfMonth() - LocalDate.now().getDayOfMonth();
                        String pattern = daysToBirthday < 0 ? "days passed: %d" : "days left: %d";
                        dto.put("name", person.getFirstName());
                        dto.put("birthday", new Formatter().format(pattern, Math.abs(daysToBirthday)).toString());
                        return dto;
                    })
                    .collect(Collectors.toList());

            log.debug("  finished select of persons (stream)  job (key): " + key);
            mongoOps.updateFirst(
                    searchQuery,
                    Update.update("payload", selected)
                            .set("status", Job.Status.DONE)
                            .set("completionTime", LocalDateTime.now()),
                    Job.class);

            log.debug("  job is done (key): " + key);

        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public Job checkJobByKey(String key, Integer month) {
        return checkJobByKey(key, month, null);
    }

    @Override
    public Job checkJobByKey(String key, Integer month, Job.Status status) {
        Query searchQuery = new Query(Criteria.where("_id").is(key));
        if (status != null) {
            searchQuery.addCriteria(Criteria.where("status").is(status));
        }
        return mongoOps.findOne(searchQuery, Job.class);
    }

}
