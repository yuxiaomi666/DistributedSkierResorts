import io.swagger.client.*;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SkierClient {
  final static private int NUM_THREADS_PHASE_1 = 32;
  final static private int NUM_THREADS_PHASE_2 = 105;
  final static private int PHASE_2_ITERATION_COUNT = 2;
  private static final int NUM_REQUESTS_PHASE_1 = 1000;
  private static final int NUM_REQUESTS_PHASE_2 = 800;

  public static void main(String[] args) throws InterruptedException, IOException {
    final RequestCounterBarrier successCounter = new RequestCounterBarrier();
    final RequestCounterBarrier UnSuccessCounter = new RequestCounterBarrier();
    ConcurrentLinkedQueue<RequestData> requestDataList = new ConcurrentLinkedQueue<>();
    final List<Integer> throughputList = new ArrayList<>();
    final AtomicInteger requestCountPerSecond = new AtomicInteger(0);

    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    scheduler.scheduleAtFixedRate(() -> {
      throughputList.add(requestCountPerSecond.getAndSet(0));
    }, 0, 1, TimeUnit.SECONDS);

    long startTime = System.currentTimeMillis();
    multiThreadLiftRideEvents(NUM_REQUESTS_PHASE_1, NUM_THREADS_PHASE_1, successCounter,
        UnSuccessCounter, requestDataList, requestCountPerSecond);
    for (int i = 0; i < PHASE_2_ITERATION_COUNT; i ++){
      multiThreadLiftRideEvents(NUM_REQUESTS_PHASE_2, NUM_THREADS_PHASE_2, successCounter,
          UnSuccessCounter, requestDataList, requestCountPerSecond);
    }

    long endTime = System.currentTimeMillis();
    scheduler.shutdown();
    long wallTime = endTime - startTime;
    double throughput =
        (double) successCounter.getVal() / wallTime * 1000;

    writeToCSV("/Users/yaxinyu/Documents/CS6650/2024fall/assignment/A1/requests.csv",
        requestDataList);
    writeThroughputToCSV("/Users/yaxinyu/Documents/CS6650/2024fall/assignment/A1/throughput.csv",
        throughputList);

    System.out.println("Number of threads used in phase 2: " + NUM_THREADS_PHASE_2);
    System.out.println("Total number of successful requests sent: " + successCounter.getVal());
    System.out.println("Total number of unsuccessful requests: " + UnSuccessCounter.getVal());
    System.out.println("Wall time is: " + wallTime);
    System.out.println("Total throughput(request per second): " + String.format("%.1f",
        throughput));

    printStatistics(requestDataList);
  }

  public static void multiThreadLiftRideEvents(int numberRequest, int numberOfThread,
      RequestCounterBarrier successCounter, RequestCounterBarrier UnSuccessCounter,
      ConcurrentLinkedQueue<RequestData> requestDataList, AtomicInteger requestCountPerSecond)
      throws InterruptedException {
    CountDownLatch completed = new CountDownLatch(numberOfThread);
    for (int i = 0; i < numberOfThread; i++) {
      Runnable thread =  () -> {
        singleThreadLiftRideEvents(numberRequest, completed, successCounter,
            UnSuccessCounter, requestDataList, requestCountPerSecond);
      };
      new Thread(thread).start();
    }
    completed.await();
  }
  public static void singleThreadLiftRideEvents(int numberRequest,
      CountDownLatch completed,
      RequestCounterBarrier successCounter, RequestCounterBarrier UnSuccessCounter,
      ConcurrentLinkedQueue<RequestData> requestDataList, AtomicInteger requestCountPerSecond){
    SkiersApi apiInstance = new SkiersApi();
    LiftRide body = new LiftRide(); // ResortIDSeasonsBody | Specify new Season value
    Integer resortID = generateRandomResortID(); // Integer | ID of the resort of interest
    String seasonID = "2024"; // Integer | ID of the resort of interest
    String dayID = "1"; // Integer | ID of the resort of interest
    Integer skierID = generateRandomSkierID(); // Integer | ID of the resort of interest

    // Use this to run at local
//      apiInstance.getApiClient().setBasePath("http://localhost:8080/skiers_servlet");
    //Use this to run at ec2
    apiInstance.getApiClient().setBasePath("http://52.38.37.53:8080/skierServlet_war");

    for (int i = 0; i < numberRequest; i++){
      long startTimeSingleRequest = System.currentTimeMillis();
      try {
        apiInstance.writeNewLiftRide(body, resortID,
            seasonID, dayID, skierID);
        long endTimeSingleRequest = System.currentTimeMillis();
        long latency = endTimeSingleRequest - startTimeSingleRequest;
        requestDataList.add(new RequestData(startTimeSingleRequest, "POST", latency,
            200));
        requestCountPerSecond.incrementAndGet();
        successCounter.inc();
      } catch (ApiException e) {
        UnSuccessCounter.inc();
        System.err.println("Exception when calling SkiersApi#writeNewLiftRide");
        System.err.println("Status Code: " + e.getCode());
        System.err.println("Response Body: " + e.getResponseBody());
        requestDataList.add(new RequestData(startTimeSingleRequest, "POST", 0,
            e.getCode()));
        e.printStackTrace();
      }
    }
    completed.countDown();
  }
  public static void printStatistics(ConcurrentLinkedQueue<RequestData> requestDataList){
    List<Long> latencies = requestDataList.stream().map(r -> r.latency).sorted().collect(
        Collectors.toList());
    double mean = latencies.stream().mapToDouble(d -> d).average().orElse(0.0);
    double median = latencies.size() % 2 == 0 ?
        (latencies.get(latencies.size() / 2 - 1) + latencies.get(latencies.size() / 2)) / 2.0 :
        latencies.get(latencies.size() / 2);
    double min = latencies.get(0);
    double max = latencies.get(latencies.size() - 1);
    int index = (int) Math.ceil(99.0 / 100.0 * latencies.size()) - 1;
    double p99 = latencies.get(Math.min(index, latencies.size() - 1));

    System.out.println("\n----- Statistics of requests above------");
    System.out.println("Mean response time (ms): " + String.format("%.1f", mean));
    System.out.println("Median response time (ms): " + median);
    System.out.println("Min response time (ms): " + min);
    System.out.println("Max response time (ms): " + max);
    System.out.println("P99 response time (ms): " + p99);
  }
  public static void writeToCSV(String filePath, ConcurrentLinkedQueue<RequestData> requestDataList) throws IOException {
    FileWriter writer = new FileWriter(filePath);
    writer.append("Start Time,Request Type,Latency,Response Code\n");
    for (RequestData data : requestDataList) {
      writer.append(String.format("%d,%s,%d,%d\n", data.startTime, data.requestType, data.latency, data.responseCode));
    }
    writer.flush();
    writer.close();
  }

  public static void writeThroughputToCSV(String filePath, List<Integer> throughputList) throws IOException {
    FileWriter writer = new FileWriter(filePath);
    writer.append("Second,Requests Per Second\n");
    int second = 1;
    for (Integer throughput : throughputList) {
      writer.append(String.format("%d,%d\n", second, throughput));
      second++;
    }
    writer.flush();
    writer.close();
  }
  public static Integer generateRandomResortID() {
    Random random = new Random();
    int randomNumber = 1 + random.nextInt(10);
    return randomNumber;
  }
  public static Integer generateRandomSkierID() {
    Random random = new Random();
    int randomNumber = 1 + random.nextInt(100000);
    return randomNumber;
  }
}
