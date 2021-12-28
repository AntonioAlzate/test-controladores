package com.co.sofka.test.controladores.repository;


import com.co.sofka.test.controladores.models.Widget;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WidgetRepository extends MongoRepository<Widget, Long> {
}