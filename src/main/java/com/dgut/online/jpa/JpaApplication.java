package com.dgut.online.jpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.support.ExtendedJpaRepository;

/**
 * @ClassName JpaApplication
 * @Description: TODO
 * @Author Administrator
 * @Date 2019/11/14 0014
 * @Version V1.0
 **/
@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = ExtendedJpaRepository.class)
public class JpaApplication {
    public static void main(String[] args) {
        SpringApplication.run(JpaApplication.class,args);
    }
}
