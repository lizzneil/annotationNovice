package com.ann.example;

import java.time.LocalDate;

public class ConcreteUser {

    public static void main(String[] args) {
        System.out.println("test processor");

        //用StringBuffer写出来的类
        IUser _user = StringUser.StringUserBuilder.create("li", "ZhiZe").address("beijing cao yang").dateOfBirth(LocalDate.of(1990, 11, 1)).phone("1391123456789").placeOfBirth("2020/1/2").build();
        System.out.println(_user.getFirstName() + "\t" + _user.getLastName() + "\t" + _user.getAddress());
        System.out.println("Y:" + _user.getDateOfBirth().getYear() + "  \tm:" + _user.getDateOfBirth().getMonthValue() + "\td:" + _user.getDateOfBirth().getDayOfMonth());
        System.out.println("phone:" + _user.getPhone() + "  \tPlaceOfBirth:" + _user.getPlaceOfBirth());


        System.out.println("<<<<<<<<<<<<<<<<------------------------------------------------------------>>>>>>>>>>>>>>>");
        //用java poet写出来的类。
        PoetUser _poetUser = PoetUser.PoetUserBuilder.create("li", "gabe").address("gabe beijing cao yang").dateOfBirth(LocalDate.of(1937, 11, 1)).phone("13900008888").placeOfBirth("1994/1/2").build();
        System.out.println(_poetUser.getFirstName() + "\t" + _poetUser.getLastName() + "\t" + _poetUser.getAddress());
        System.out.println("Y:" + _poetUser.getDateOfBirth().getYear() + "  \tm:" + _poetUser.getDateOfBirth().getMonthValue() + "\td:" + _poetUser.getDateOfBirth().getDayOfMonth());
        System.out.println("phone:" + _poetUser.getPhone() + "  \tPlaceOfBirth:" + _poetUser.getPlaceOfBirth());
    }
}
