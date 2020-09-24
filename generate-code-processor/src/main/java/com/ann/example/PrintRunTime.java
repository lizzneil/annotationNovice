package com.ann.example;

public class PrintRunTime {
    public static void main(String[] arg){
        runMethod();
    }


    @Deprecated
    public static void runMethod() {


        try {
            Thread.sleep(1234L);
        } catch (InterruptedException var5) {
            var5.printStackTrace();
        }

    }
}
