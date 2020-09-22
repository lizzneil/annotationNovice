package com.ann.example;

import com.ann.example.annotation.AutoImplement;
import com.ann.example.annotation.Mandatory;

import java.time.LocalDate;

@AutoImplement(as = "TestStringUser", builder = true)
public interface ITestUser {

    @Mandatory
    String getFirstName();

    @Mandatory
    String getLastName();

    LocalDate getDateOfBirth();

    String getPlaceOfBirth();

    String getPhone();

    String getAddress();
}