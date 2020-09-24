package com.ann.example;

import com.ann.example.annotation.AutoImplement;
import com.ann.example.processor.AutoGenerateProcessor;


import java.lang.reflect.Field;

public class InjectUtils {
    static void injectMethod(AutoGenerateProcessor aProcessor){
        Field[] fields = aProcessor.getClass().getDeclaredFields();
        for (Field f:fields){
            f.getAnnotation(AutoImplement.class);
        }

    }
}
