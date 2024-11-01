import com.rabbitmq.client.*;
import java.util.concurrent.*;

public class ConsumerApp {
    private static final String QUEUE_NAME = "lift_rides";
    private static final ConcurrentHashMap<Integer, FullLiftRide> liftRidesMap = new ConcurrentHashMap<>();
    private static final int NUM_CONSUMER_THREADS = 5;

    public static void main(String[] argv) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_CONSUMER_THREADS);
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("54.245.22.9");

        Connection connection = factory.newConnection();
        for (int i = 0; i < NUM_CONSUMER_THREADS; i++) {
            executorService.submit(new ConsumerWorker(connection, QUEUE_NAME, liftRidesMap));
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
}
