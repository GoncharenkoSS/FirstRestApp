package ru.mrSergey.FirstRestApp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mrSergey.FirstRestApp.models.Person;
import ru.mrSergey.FirstRestApp.repositories.PeopleRepository;
import ru.mrSergey.FirstRestApp.util.PersonNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PeopleService {

    private final PeopleRepository peopleRepository;

    @Autowired
    public PeopleService(PeopleRepository peopleRepository){
        this.peopleRepository = peopleRepository;
    }

    public List<Person> findAll(){
        return peopleRepository.findAll(Sort.by("name"));
    }

   public Person findOne(int id){
        Optional<Person> foundPerson = peopleRepository.findById(id);
        return foundPerson.orElseThrow(PersonNotFoundException::new); //Вызываем собственную ошибку изи класса util
    }
    @Transactional
    public void save(Person person){
        enrichPerson(person); //Добавляем перед сохранением
        peopleRepository.save(person);
    }
    //Метод добавления данных для Person в базу данных
    private void enrichPerson(Person person){
        person.setCratedAt((LocalDateTime.now()));
        person.setUpdatedAt((LocalDateTime.now()));
        person.setCreatedWho("ADMIN");
    }
}
