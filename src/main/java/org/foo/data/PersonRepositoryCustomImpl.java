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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PersonRepositoryCustomImpl implements IPersonRepositoryCustom {

    final static Logger log = LoggerFactory.getLogger(PersonRepositoryCustomImpl.class);

    @Autowired
    IPersonRepository personRepository;

    @Autowired
    Environment env;

    private MongoOperations mongoOps;

    @PostConstruct
    void initMongoOps() {
        try {
            mongoOps = new MongoTemplate(new SimpleMongoDbFactory(
                    new MongoClient(),
                    env.getProperty("spring.data.mongodb.database"))
            );
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Async
    @Override
    public void writeSelected(String key, List<Person> persons, Integer month) {

        Job job = new Job();
        job.setKey(key);
        job.setStatus(Job.Status.PENDING);

        mongoOps.insert(job);
        log.debug("  job stored (key): " + key);

        try {
            if (persons == null) persons = personRepository.findAll();   // "heavy" task
            if (persons.size() < 1000) Thread.sleep(60000);              // "heavy" task

            List<Map> selected = persons.stream().parallel()
                    .filter(person -> person.getDateOfBirth().getMonth().getValue() == month)
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

            Query searchQuery = new Query(Criteria.where("key").is(key));
            mongoOps.updateFirst(
                    searchQuery,
                    Update.update("payload", selected).set("status", Job.Status.DONE),
                    Job.class);

            log.debug("  job is done (key): " + key);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Job checkJobByKey(String key, Integer month) {
        return checkJobByKey(key, month, null);
    }

    @Override
    public Job checkJobByKey(String key, Integer month, Job.Status status) {
        Query searchQuery = new Query(Criteria.where("key").is(key));
        if (status != null) {
            searchQuery.addCriteria(Criteria.where("status").is(status));
        }
        return mongoOps.findOne(searchQuery, Job.class);
    }

}
