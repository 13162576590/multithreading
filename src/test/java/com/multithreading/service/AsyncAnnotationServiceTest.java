package com.multithreading.service;

import com.multithreading.SpringbootDemoApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringbootDemoApplication.class)
public class AsyncAnnotationServiceTest {

    @Autowired
    private AsyncAnnotationService asyncAnnotationService;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void asyncTest() throws Exception {
        Date dateTime = new Date();
        System.out.println(dateTime.getTime());
        List<CompletableFuture> cfList = Stream.of("h1", "h2", "h3", "h4").map(v -> {
            return asyncAnnotationService.doWork(v);
        }).collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        CompletableFuture rs = CompletableFuture.allOf(cfList.toArray(new CompletableFuture[cfList.size()])).whenComplete((v, t) -> {
            cfList.forEach(cf -> {
                sb.append(cf.getNow(null)).append(",");
            });
        });


        CompletableFuture.allOf(cfList.toArray(new CompletableFuture[cfList.size()])).join();

        try {
            rs.get(1, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("------------------------" + sb.toString());

        Date dateTime2 = new Date();

        System.out.println(dateTime2.getTime() - dateTime.getTime());

    }

    @Test
    public void executorTest() throws Exception {
        System.out.println("executorTest");
        Date dateTime = new Date();

//        List<CompletableFuture> cfList = Stream.of("h1", "h2", "h3", "h4").map(v -> {
//            return asyncAnnotationService.doWork(v);
//        }).collect(Collectors.toList());

        List<String> cfList = Arrays.asList("h1", "h2", "h3", "h4", "h1", "h2", "h3", "h4");
        ExecutorService executor = Executors.newFixedThreadPool(8);

        cfList.stream().map(a -> CompletableFuture.supplyAsync(() -> {
            // 操作代码.....
            try {
                //demo1
//                System.out.println("======" + a);
//                Thread.sleep(2000);

                //demo2
                this.doWork(a);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return a;
        }, executor)).collect(Collectors.toList()).stream().map(CompletableFuture::join).collect(Collectors.toList());
        Date dateTime2 = new Date();

        System.out.println((dateTime2.getTime() - dateTime.getTime()) / 1000);

    }

    @Test
    public void testMyAsync() throws Exception {
        Date dateTime = new Date();
        System.out.println(dateTime.getTime());

        List<String> list = Arrays.asList("h1", "h2", "h3", "h4");


        Map<String, CompletableFuture<String>> respDatas = new HashMap<> ();
        for (String value: list) {
            respDatas.put(value, this.doWork(value));
        }
        CompletableFuture.allOf(respDatas.values().toArray(new CompletableFuture[respDatas.size()])).join();

        Date dateTime2 = new Date();

        System.out.println(dateTime2.getTime() - dateTime.getTime());

    }


//    @Async
    @Async("myAsync")
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


    @Test
    public void testMyAsync2() throws Exception {
        Date dateTime = new Date();
        System.out.println(dateTime.getTime());

        List<String> list = Arrays.asList("h1", "h2", "h3", "h4", "h1", "h2", "h3", "h4");


        Map<String, Future<String>> respDatas = new HashMap<> ();
//        for (String value: list) {
//            respDatas.put(value, this.doWork(value));
//        }

        AsyncAnnotationService bean = applicationContext.getBean(AsyncAnnotationService.class);
        for (String value: list) {
            respDatas.put(value, bean.doWork(value));
        }
        CompletableFuture.allOf(respDatas.values().toArray(new CompletableFuture[respDatas.size()])).join();

        Date dateTime2 = new Date();

        System.out.println(dateTime2.getTime() - dateTime.getTime());

    }

    @Async("myAsync")
    public Future doWork2(String value){
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