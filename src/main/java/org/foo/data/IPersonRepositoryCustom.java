package org.foo.data;

import org.foo.data.models.Job;
import org.foo.data.models.Person;

import java.util.List;


public interface IPersonRepositoryCustom {

    void writeSelected(String key, List<Person> persons, Integer month);

    Job checkJobByKey(String key, Integer month);

    Job checkJobByKey(String key, Integer month, Job.Status status);

}
