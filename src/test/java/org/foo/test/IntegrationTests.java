package org.foo.test;

import org.foo.Application;
import org.foo.data.IPersonRepository;
import org.foo.data.IPersonRepositoryCustom;
import org.foo.data.models.Person;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Repository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.IntStream;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
public class IntegrationTests {

    static final Logger log = LoggerFactory.getLogger(IntegrationTests.class);

    static int NUMBER_OF_REQUESTS = 4;

    static List<String> jobIdList = new ArrayList<>(NUMBER_OF_REQUESTS);

    RestTemplate template = new RestTemplate();

    @LocalServerPort
    String port;

    @Autowired
    PersonsRepository personsRepository;

    @Test
    public void t1_jobInitGetId() {

        log.info("/// 1.  several requests to initialize the Tasks");


        IntStream.range(0, NUMBER_OF_REQUESTS).forEach((i) -> {
            String jobId = template.getForObject("http://localhost:" + port + "/espring/persons/init", String.class);
            Assert.assertTrue(new IsInstanceOf(UUID.class).matches(UUID.fromString(jobId)));
            jobIdList.add(jobId);
        });
    }

    @Test
    public void t2_jobCheckByIdGetResponseToWait() {

        log.info("/// 2.  without delay trying to check the status of the Tasks");

        IntStream.range(0, NUMBER_OF_REQUESTS).forEach((i) -> {
            String msg = template.getForObject("http://localhost:" + port + "/espring/persons/birthdays?jobId=" + jobIdList.get(i), String.class);
            Assert.assertEquals(msg, "The job is in process.. try later");
        });
    }

    @Test//(timeout = 70000)
    public void t3_jobCheckByIdGetResponseWithData() throws InterruptedException {

        log.info("/// 3.  waiting ~1 minute and trying to get result of the Tasks ");

        template.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        Thread.sleep(61000);

        IntStream.range(0, NUMBER_OF_REQUESTS).forEach((i) -> {
            List<Map<String, String>> result = template.getForObject("http://localhost:" + port + "/espring/persons/birthdays?jobId=" + jobIdList.get(i), ArrayList.class);

            Assert.assertEquals(result.size(), personsRepository.NUMBER_OF_PERSONS);
            Assert.assertNotNull(result.get(0).get("birthday"));
            Assert.assertTrue(result.get(0).get("birthday").contains("days"));
        });

        log.info("/// 3.  test passed ");

    }
}

@ActiveProfiles("test")
@Repository
class PersonsRepository {
    /*
        The Data that fits in BSON document in order to process it in parallel stream.
        MongoDB has the maximum BSON document size equal to 16 megabytes by default.
     */
    final int NUMBER_OF_PERSONS = 250_000;
    List<Person> personsList = new ArrayList<>(NUMBER_OF_PERSONS);

    @Autowired
    IPersonRepository repository;

    @Autowired
    IPersonRepositoryCustom personRepositoryCustom;

    @PostConstruct
    public void doInitLoad() {

        repository.deleteAll();

        for (int i = 0; i < NUMBER_OF_PERSONS; i++) {
            personsList.add(new Person(
                            "Max" + i,
                            "Planck" + i,
                            LocalDate.now().withDayOfMonth(new Random().nextInt(27) + 1)
                    )
            );
        }
        repository.save(personsList);

    }
}