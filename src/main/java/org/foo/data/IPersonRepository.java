package org.foo.data;

import org.foo.data.models.Person;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IPersonRepository extends MongoRepository<Person, String> {

    List<Person> findByLastName(@Param("q") String lastName);

    List<Person> findByFirstName(@Param("q") String firstName);

}