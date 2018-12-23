package com.netcracker.superproject;

import com.netcracker.superproject.persistence.EntityManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SuperprojectApplication {

    public static void main(String[] args) {

        EntityManager em = new EntityManager();

        SpringApplication.run(SuperprojectApplication.class, args);

        em.dropTables();
        em.createTables();
    }
}
