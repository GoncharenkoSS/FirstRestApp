package ru.mrSergey.FirstRestApp.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.mrSergey.FirstRestApp.dto.PersonDTO;
import ru.mrSergey.FirstRestApp.models.Person;
import ru.mrSergey.FirstRestApp.services.PeopleService;
import ru.mrSergey.FirstRestApp.util.PersonErrorResponse;
import ru.mrSergey.FirstRestApp.util.PersonNotCreatedException;
import ru.mrSergey.FirstRestApp.util.PersonNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/people")
public class PeopleController {

    private final PeopleService peopleService;

    @Autowired
    public PeopleController(PeopleService peopleService) {
        this.peopleService = peopleService;
    }

    @GetMapping()
    public List<PersonDTO> getPeople() {
        //Автоматически Jackson конвертирует эти объекты в JSON из класса !!ДТО!!
        return peopleService.findAll().stream().map(this::convertToPersonDTO).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Person getPerson(@PathVariable("id") int id) {
        return peopleService.findOne(id);
    }

    @PostMapping
    public ResponseEntity<HttpStatus> create(
            @RequestBody @Valid PersonDTO personDTO, /*С этой аннотацией Джексон сконвертирует JSON в объект People*/
            BindingResult bindingResult) { /*Ловим ошибку валидации*/
        if (bindingResult.hasErrors()) {
            StringBuilder errorMsg = new StringBuilder();/* Построение красивого поля с ошибкой валидации*/
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                errorMsg.append(error.getField())/*На каком поле была совершена ошибка*/
                        .append(" - ")
                        .append(error.getDefaultMessage())/*Какая ошибка была на данном поле*/
                        .append(";");
            }
            throw new PersonNotCreatedException(errorMsg.toString());
        }
        peopleService.save(convertToPerson(personDTO));

        //отправляем HTTP ответ с пустым телом и со статусом 200
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @ExceptionHandler//Аннотация для своих исключений(!!!!!!!!!!если ID человека не найдено!!!!!!!!!!!!!)
    private ResponseEntity<PersonErrorResponse> handleException(PersonNotFoundException e) {
        PersonErrorResponse response = new PersonErrorResponse(
                "Человек с данным ID не найден", System.currentTimeMillis());
        //В HTTP ответе тело ответа(response) и статус в заголовке
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler//Аннотация для своих исключений(!!!!!!!!!!!!если была нарушена валидация!!!!!!!!)
    private ResponseEntity<PersonErrorResponse> handleException(PersonNotCreatedException e) {
        PersonErrorResponse response = new PersonErrorResponse(
                e.getMessage(), System.currentTimeMillis());
        //В HTTP ответе тело ответа(response) и статус в заголовке
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    //Метод конвертирования из PersonDTO in Person
    private Person convertToPerson(PersonDTO personDTO) {
        ModelMapper modelMapper = new ModelMapper();
        //Зависимость modelmapper автоматически конвертирует из PersonDTO in Person как показано ниже.
        //person.setName(personDTO.getName());
        return modelMapper.map(personDTO, Person.class);
    }

    private PersonDTO convertToPersonDTO(Person person) {
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(person, PersonDTO.class);
    }

}