# multithreading
多线程


Java CompletableFuture多线程应用，在实际开发中，为了提高应用程序效率，该博客主要是在解决高并发调用第三方接口时设计，在同步情况下调用第三方接口200次，需要时间约为28秒左右，效率低，app体验差；需对其优化，故使用多线程，提高其效率，优化后，在10个线程下并发调用，需要时间约为5~7秒左右。

一.线程池

线程池就是首先创建一些线程，它们的集合称为线程池。使用线程池可以很好地提高性能，线程池在系统启动时即创建大量空闲的线程，程序将一个任务传给线程池，线程池就会启动一条线程来执行这个任务，执行结束以后，该线程并不会死亡，而是再次返回线程池中成为空闲状态，等待执行下一个任务。

java线程池类型

1.newCachedThreadPool

2.newFixedThreadPool

3.newSingleThreadExecutor

4.newScheduleThreadPool

5.newSingleThreadScheduledExecutor


代码AsyncConfig类创建了几种类型线程池，详情见demo

二.多线程使用

1.newFixedThreadPool

个人创建一个固定线程数量的线程池，使用该线程池运行

		List<String> cfList = Arrays.asList("h1", "h2", "h3", "h4", "h1", "h2", "h3", "h4");
        ExecutorService executor = Executors.newFixedThreadPool(10);

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
   
调用join()方法等到线程执行完毕

在不指定executor情况下，默认线程池大小为可用核数乘以2 - 1,底层调用计算Runtime.getRuntime().availableProcessors() - 1。

三.@Async注解无效

因为@Transactional和@Async注解的实现都是基于Spring的AOP，而AOP的实现是基于动态代理模式实现的。那么注解失效的原因就很明显了，有可能因为调用方法的是对象本身而不是代理对象，因为没有经过Spring容器。

当在方法内部使用多线程，并且使用容器初始化时候的线程池及AsyncConfig类中的线程池时，发现并未走多线程，仍是同步情况。比如测试类AsyncAnnotationServiceTest的testMyAsync2方法，代码如下:

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
	//        调用本类doWork方法，虽然加了异步注解，仍是同步执行，并未使用到线程池
	//            respDatas.put(value, this.doWork(value));
	//        }
        
        AsyncAnnotationService bean = applicationContext.getBean(AsyncAnnotationService.class);
        for (String value: list) {
        //  通过IOC容器获取相应的bean调用，使用了AsyncConfig类中的线程池
            respDatas.put(value, bean.doWork(value));
        }
        CompletableFuture.allOf(respDatas.values().toArray(new CompletableFuture[respDatas.size()])).join();

        Date dateTime2 = new Date();

        System.out.println(dateTime2.getTime() - dateTime.getTime());

    }

方法一定要从另一个类中调用，也就是从类的外部调用，类的内部调用是无效的。如果需要从类的内部调用，需要先获取其代理类。
