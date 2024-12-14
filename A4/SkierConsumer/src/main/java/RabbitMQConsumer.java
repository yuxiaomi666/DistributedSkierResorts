import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class RabbitMQConsumer {
    private static final String QUEUE_NAME = "lift_rides";
    private static final Gson gson = new Gson();
    private static final int NUM_CONSUMER_THREADS = 200;

    public static void main(String[] argv) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_CONSUMER_THREADS);
        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost("54.245.22.9");
//        factory.setHost("44.228.160.98"); //qiuying
        factory.setHost("172.31.12.171"); // qiuying
        factory.setPort(5672); // qiuying
        factory.setUsername("zqiuying"); // qiuying
        factory.setPassword("LoveCoding"); // qiuying

        Connection connection = factory.newConnection();
        System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");
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
        private static final LiftRidesDao liftRidesDao = LiftRidesDao.getInstance();
        ConsumerThread(Connection connection) {
            this.connection = connection;
        }

        @Override
        public void run() {
            try {
                Channel channel = connection.createChannel();
                channel.queueDeclare(QUEUE_NAME, true, false, false, null);

                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), "UTF-8");
                    try {
                        // convert message from RMQ to DynamoDB attribute value map
                        Map<String, AttributeValue> item =
                            convertFullLiftRideToAttributeValueMap(message);
                        // write lift ride message to DynamoDB
                        liftRidesDao.writeLiftRide(item);
                    } catch (JsonSyntaxException e) {
                        System.err.println("Failed to parse JSON: " + e.getMessage());
                    }
                };
                channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});
            } catch (Exception e) {
                System.out.println("Error in consumer thread: " + e.getMessage());
            }
        }

        private Map<String, AttributeValue> convertFullLiftRideToAttributeValueMap(
            String message) {
            FullLiftRide liftRide = gson.fromJson(message, FullLiftRide.class);
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("SkierID",
                AttributeValue.builder().n(String.valueOf(liftRide.getSkierID())).build());
            item.put("ResortID",
                AttributeValue.builder().n(String.valueOf(liftRide.getResortID())).build());
            item.put("LiftID",
                AttributeValue.builder().n(String.valueOf(liftRide.getLiftID())).build());
            item.put("Time",
                AttributeValue.builder().n(String.valueOf(liftRide.getTime())).build());
            item.put("SeasonDayID",
                AttributeValue.builder().s(liftRide.getSeasonID() +
                    "#" + liftRide.getDayID()).build());
            item.put("ResortSkierID",
                AttributeValue.builder().s(liftRide.getResortID() +
                    "#" + liftRide.getSkierID()).build());
            item.put("SeasonID",
                AttributeValue.builder().s(String.valueOf(liftRide.getSeasonID())).build());
            item.put("SeasonDayID#SkierID#Time",
                AttributeValue.builder().s(liftRide.getSeasonID() + "#" + liftRide.getDayID() + "#" + String.valueOf(liftRide.getSkierID()) + '#' + String.valueOf(liftRide.getTime())).build());
            return item;
        }
    }
}
