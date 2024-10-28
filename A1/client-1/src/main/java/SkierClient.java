import io.swagger.client.*;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.*;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class SkierClient {
  final static private int NUM_THREADS_PHASE_1 = 32;
  final static private int NUM_THREADS_PHASE_2 = 105;
  final static private int PHASE_2_ITERATION_COUNT = 2;
  private static final int NUM_REQUESTS_PHASE_1 = 1000;
  private static final int NUM_REQUESTS_PHASE_2 = 800;

  public static void main(String[] args) throws InterruptedException {
    final RequestCounterBarrier successCounter = new RequestCounterBarrier();
    final RequestCounterBarrier UnSuccessCounter = new RequestCounterBarrier();

    long startTime = System.currentTimeMillis();
    multiThreadLiftRideEvents(NUM_REQUESTS_PHASE_1, NUM_THREADS_PHASE_1, successCounter,
        UnSuccessCounter);
    for (int i = 0; i < PHASE_2_ITERATION_COUNT; i ++){
      multiThreadLiftRideEvents(NUM_REQUESTS_PHASE_2, NUM_THREADS_PHASE_2, successCounter,
          UnSuccessCounter);
    }

    long endTime = System.currentTimeMillis();
    long timeTaken = endTime - startTime;
    double throughput =
        (double) successCounter.getVal() / timeTaken * 1000;

    System.out.println("Number of threads used in phase 2: " + NUM_THREADS_PHASE_2);
    System.out.println("Total number of successful requests sent: " + successCounter.getVal());
    System.out.println("Total number of unsuccessful requests: " + UnSuccessCounter.getVal());
    System.out.println("Total run time: " + timeTaken);
    System.out.println("Total throughput(request per second): " + String.format("%.1f",
        throughput));
  }

  public static void multiThreadLiftRideEvents(int numberRequest, int numberOfThread,
      RequestCounterBarrier successCounter, RequestCounterBarrier UnSuccessCounter)
      throws InterruptedException {
    CountDownLatch completed = new CountDownLatch(numberOfThread);
    for (int i = 0; i < numberOfThread; i++) {
      Runnable thread =  () -> {
        singleThreadLiftRideEvents(numberRequest, completed, successCounter,
            UnSuccessCounter);
      };
      new Thread(thread).start();
    }
    completed.await();
  }
  public static void singleThreadLiftRideEvents(int numberRequest,
      CountDownLatch completed,
      RequestCounterBarrier successCounter, RequestCounterBarrier UnSuccessCounter){
    SkiersApi apiInstance = new SkiersApi();
    LiftRide body = new LiftRide(); // ResortIDSeasonsBody | Specify new Season value
    Integer resortID = generateRandomResortID(); // Integer | ID of the resort of interest
    String seasonID = "2024"; // Integer | ID of the resort of interest
    String dayID = "1"; // Integer | ID of the resort of interest
    Integer skierID = generateRandomSkierID(); // Integer | ID of the resort of interest

    // local
//      apiInstance.getApiClient().setBasePath("http://localhost:8080/skiers_servlet");
    //ec2
    apiInstance.getApiClient().setBasePath("http://52.38.37.53:8080/skierServlet_war");

    for (int i = 0; i < numberRequest; i++){
      try {
        apiInstance.writeNewLiftRide(body, resortID, seasonID, dayID, skierID);
        successCounter.inc();
      } catch (ApiException e) {
        UnSuccessCounter.inc();
        System.err.println("Exception when calling SkiersApi#writeNewLiftRide");
        System.err.println("Status Code: " + e.getCode());
        System.err.println("Response Body: " + e.getResponseBody());
        e.printStackTrace();
      }
    }
    completed.countDown();
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
