package com.myst;

public class Timer {
    public static double getTime(){
//        converts to seconds
        return (double) System.nanoTime() / 1000000000L;
    }
}
