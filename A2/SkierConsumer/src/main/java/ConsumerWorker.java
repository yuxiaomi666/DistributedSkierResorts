import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public class ConsumerWorker implements Runnable {
  private static final Gson gson = new Gson();
  private final Connection connection;
  private final String queueName;
  private final ConcurrentHashMap<Integer, FullLiftRide> liftRidesMap;

  public ConsumerWorker(Connection connection, String queueName, ConcurrentHashMap<Integer, FullLiftRide> liftRidesMap) {
    this.connection = connection;
    this.queueName = queueName;
    this.liftRidesMap = liftRidesMap;
  }

  @Override
  public void run() {
    try {
      Channel channel = connection.createChannel();
      channel.queueDeclare(queueName, true, false, false, null);
      System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");

      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        try {
          FullLiftRide liftRide = gson.fromJson(message, FullLiftRide.class);
          Integer key = liftRide.getLiftID();
          liftRidesMap.put(key, liftRide);
          System.out.println(" [x] Received '" + liftRide + "'");
        } catch (JsonSyntaxException e) {
          System.err.println("Failed to parse JSON: " + e.getMessage());
        }
      };

      channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
    } catch (Exception e) {
      System.out.println("Error in consumer thread: " + e.getMessage());
    }
  }
}

