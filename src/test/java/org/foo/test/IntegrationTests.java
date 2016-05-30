package org.foo.test;

import org.foo.Application;
import org.foo.data.IPersonRepository;
import org.foo.data.IPersonRepositoryCustom;
import org.foo.data.models.Person;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
public class IntegrationTests {

    @LocalServerPort
    String port;

    @Autowired
    PersonsRepository personsRepository;

    @Test
    public void testFlow() throws InterruptedException {
        final RestTemplate template = new RestTemplate();

        String URL = "http://localhost:" + port + "/espring/persons";

        //////////////// job init
        String jobId = template.getForObject(URL + "/init", String.class);
        Assert.assertTrue(new IsInstanceOf(UUID.class).matches(UUID.fromString(jobId)));

        //////////////// checking the job status
        String msg = template.getForObject(URL + "/birthdays?jobId=" + jobId, String.class);
        Assert.assertEquals(msg, "The job is in process.. try later");

        //////////////// waiting for the job
        Thread.sleep(61000);
        template.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        List<Map<String, String>> result = template.getForObject(URL + "/birthdays?jobId=" + jobId, ArrayList.class);

        Assert.assertEquals(result.size(), personsRepository.NUMBER_OF_PERSONS);
        Assert.assertNotNull(result.get(0).get("birthday"));
        Assert.assertTrue(result.get(0).get("birthday").contains("days"));
    }

}

@ActiveProfiles("test")
@Repository
class PersonsRepository {
    /*
        The Data that fits in BSON document in order to process it in parallel stream.
        MongoDB has the maximum BSON document size equal to 16 megabytes.
     */
    static final int NUMBER_OF_PERSONS = 250_000;
    static List<Person> personsList = new ArrayList<>(NUMBER_OF_PERSONS);

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
                            LocalDate.now().withDayOfMonth(new Random().nextInt(31) + 1)
                    )
            );
        }
        repository.save(personsList);

    }
}