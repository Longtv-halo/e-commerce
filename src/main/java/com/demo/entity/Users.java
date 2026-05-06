package com.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;


@Entity
@Data
public class Users {

	@Id
    @GeneratedValue
    private Long id;

	private String name;
}
