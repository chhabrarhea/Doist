package com.example.todo.utils;

public  class  Constants {
    static String audioFilePath="";
    public synchronized void setAudioFilePath(String path){
        audioFilePath=path;
    }
}
