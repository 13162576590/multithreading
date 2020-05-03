package com.multithreading.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AsyncAnnotationService {

    @Async
    public CompletableFuture<String> doWork(String value){
        System.out.println("--------start work------" + Thread.currentThread().getName());
        try {
            Thread.sleep(1000);
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("-------end work----------");
        return CompletableFuture.completedFuture(value);
    }

}
