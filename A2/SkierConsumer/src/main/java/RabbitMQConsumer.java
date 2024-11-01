import com.google.gson.JsonSyntaxException;
import com.rabbitmq.client.*;
import com.google.gson.Gson;
import java.util.concurrent.*;

public class RabbitMQConsumer {
    private static final String QUEUE_NAME = "lift_rides";
    private static final Gson gson = new Gson();
    private static final ConcurrentHashMap<Integer, FullLiftRide> liftRidesMap = new ConcurrentHashMap<>();
    private static final int NUM_CONSUMER_THREADS = 5;

    public static void main(String[] argv) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_CONSUMER_THREADS);
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("54.245.22.9");

        Connection connection = factory.newConnection();
        for (int i = 0; i < NUM_CONSUMER_THREADS; i++) {
            executorService.submit(new ConsumerThread(connection));
        }

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.err.println("Main thread interrupted: " + e.getMessage());
        } finally {
            connection.close();
            executorService.shutdownNow();
        }
    }

    static class ConsumerThread implements Runnable {
        private final Connection connection;

        ConsumerThread(Connection connection) {
            this.connection = connection;
        }

        @Override
        public void run() {
            try {
                Channel channel = connection.createChannel();
                channel.queueDeclare(QUEUE_NAME, true, false, false, null);
                System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");

                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), "UTF-8");
                    try {
                        FullLiftRide liftRide = gson.fromJson(message, FullLiftRide.class);
                        Integer key = liftRide.getLiftID();
                        liftRidesMap.put(key, liftRide);
                        System.out.println(" [x] Received '" + liftRide + "'");
                    } catch (JsonSyntaxException e) {
                        System.err.println("Failed to parse JSON: " + e.getMessage());
                    }
                };
                channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});
            } catch (Exception e) {
                System.out.println("Error in consumer thread: " + e.getMessage());
            }
        }
    }
}


//import com.google.gson.JsonSyntaxException;
//import com.rabbitmq.client.*;
//import com.google.gson.Gson;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class RabbitMQConsumer {
//
//    private final static String QUEUE_NAME = "lift_rides";
//    private static final Gson gson = new Gson();
//    //Lift ride message in a hashmap with liftID as key
//    private static final ConcurrentHashMap<Integer, FullLiftRide> liftRidesMap = new ConcurrentHashMap<>();
//    private static final int NUM_CONSUMER_THREADS = 5;
//
//    public static void main(String[] argv) throws Exception {
//        ExecutorService executorService = Executors.newFixedThreadPool(NUM_CONSUMER_THREADS);
//        ConnectionFactory factory = new ConnectionFactory();
//        // remote Consumer
//        factory.setHost("54.245.22.9");
////        Gson gson = new Gson();
//
//        try {
//            Connection connection = factory.newConnection();
//            Channel channel = connection.createChannel();
//            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
//            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
//
//            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
//                String message = new String(delivery.getBody(), "UTF-8");
//                try {
//                    FullLiftRide liftRide = gson.fromJson(message, FullLiftRide.class);
//                    Integer key = liftRide.getLiftID();
//                    liftRidesMap.put(key, liftRide);
//                    System.out.println(" [x] Received '" + liftRide + "'");
//                } catch (JsonSyntaxException e) {
//                    System.err.println("Failed to parse JSON: " + e.getMessage());
//                }
//            };
//            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
//            });
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }
//    }
//}
//
