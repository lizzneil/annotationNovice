package com.ann.example;


import com.ann.example.annotation.Mandatory;
import com.ann.example.annotation.PoetAutoImplement;

import java.time.LocalDate;


@PoetAutoImplement(as = "PoetUser", builder = true)
public interface IPoetUser {

    @Mandatory
    String getFirstName();

    @Mandatory
    String getLastName();

    LocalDate getDateOfBirth();

    String getPlaceOfBirth();

    String getPhone();

    String getAddress();
}