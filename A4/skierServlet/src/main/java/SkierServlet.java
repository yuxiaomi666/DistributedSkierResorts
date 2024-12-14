import com.google.gson.Gson;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.rabbitmq.client.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.endpoints.internal.Value.Int;

@WebServlet(value = {"/skiers/*", "/resorts/*"})
public class SkierServlet extends HttpServlet {
  private static Connection connection;
  private static BlockingQueue<Channel> channelPool;
  private static final int POOL_SIZE = 10;

  public void init() throws ServletException {
    super.init();
    ConnectionFactory factory = new ConnectionFactory();
//    factory.setHost("54.245.22.9");
    factory.setHost("172.31.12.171"); // qiuying
//    factory.setHost("44.228.160.98"); // qiuying
    factory.setUsername("zqiuying"); // qiuying
    factory.setPassword("LoveCoding"); // qiuying

    try {
      connection = factory.newConnection();
      channelPool = new ArrayBlockingQueue<>(POOL_SIZE);
      for (int i = 0; i < POOL_SIZE; i++) {
        channelPool.add(connection.createChannel());
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new ServletException("Failed to create RabbitMQ connection or channel pool", e);
    }
  }
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("text/plain");
    String urlPath = req.getPathInfo();

    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing url");
      return;
    }

    String[] urlParts = urlPath.split("/");
    if (!isUrlValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing paramterers");
      return;
    }
    System.out.println("urlParts length is: " + urlParts.length);

    ReadLiftRides readLiftRides = ReadLiftRides.getInstance();
    // Get the total vertical for the skier for the specified ski day
    if (urlParts.length == 8) {
      try{
        Integer resortID = Integer.parseInt(urlParts[1]);
        String seasonID = urlParts[3];
        String dayID = urlParts[5];
        Integer skierID = Integer.parseInt(urlParts[7]);

        int verticalByResortAndDay = readLiftRides.getVerticalByResortAndDay(resortID,
            skierID, seasonID, dayID);
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("The total vertical for the skier for the specified ski day"
            + " is: " + verticalByResortAndDay);
      } catch (NumberFormatException e) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.getWriter().write("Invalid input numbers in parameters");
      }
    } else if (urlParts.length == 3) {
      // Get the total vertical for the skier for the specified resort
      try {
        Integer skierID = Integer.parseInt(urlParts[1]);
        Integer resortID = Integer.parseInt(req.getParameter("resort"));
        String season = req.getParameter("season");

        int verticalByResort = 0;
        if (season == null) {
          verticalByResort = readLiftRides.getVerticalByResort(resortID, skierID);
        } else {
          verticalByResort = readLiftRides.getVerticalByResort(resortID, skierID, season);
        }
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("The total vertical for the skier for the specified resort"
            + " is: " + verticalByResort);
      } catch (NumberFormatException e) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.getWriter().write("Invalid input numbers in parameters");
      }
    } else if (urlParts.length == 7) {
      // Get the number of unique skiers at resort/season/day
      try {
        Integer resortID = Integer.parseInt(urlParts[1]);
        String seasonID = urlParts[3];
        String dayID = urlParts[5];
        int uniqueSkiersCount = readLiftRides.getUniqueSkierByResortAndDay(resortID, seasonID,
            dayID);
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("The number of unique skiers at specific resort/season/day"
            + " is: " + uniqueSkiersCount);
      } catch (NumberFormatException e) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.getWriter().write("Invalid input numbers in parameters");
      }
    } else {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("Wrong request!");
    }
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("application/json");
    String urlPath = req.getPathInfo();

    Gson gson = new Gson();
    LiftRide liftRide = gson.fromJson(req.getReader(), LiftRide.class);

    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing parameters");
      return;
    }

    String[] urlParts = urlPath.split("/");
    if (urlParts.length != 8) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("Some of resortID, seasonID, dayID, skierID is missing");
      return;
    }

    Integer resortID = Integer.parseInt(urlParts[1]);
    Integer seasonID = Integer.parseInt(urlParts[3]);
    Integer dayID = Integer.parseInt(urlParts[5]);
    Integer skierID = Integer.parseInt(urlParts[7]);

    if (!isPOSTRequestValid(resortID, seasonID, dayID, skierID, liftRide)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("Some of resortID, seasonID, dayID, skierID is not valid. "
        + "please try again!");
    } else {
      FullLiftRide fullLiftRide = new FullLiftRide()
              .liftID(liftRide.getLiftID())
              .time(liftRide.getTime())
              .resortID(resortID)
              .seasonID(seasonID)
              .dayID(dayID)
              .skierID(skierID);

      Channel channel = null;
      try {
        channel = channelPool.take();
        String queueName = "lift_rides";
        channel.queueDeclare(queueName, true, false, false, null);
        String message = gson.toJson(fullLiftRide);
        channel.basicPublish("", queueName, null, message.getBytes());
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("Received and processed data for POST request!");
      } catch (Exception e) {
        System.out.println(e.getMessage());
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        res.getWriter().write("Failed to publish message");
      } finally {
        if (channel != null) {
          try {
            channelPool.put(channel);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
  }

  public void destroy() {
    super.destroy();
    try {
      for (Channel ch : channelPool) {
        if (ch != null) {
          ch.close();
        }
      }
      connection.close();
    } catch (Exception e) {
      System.err.println("Failed to close channels or connection: " + e.getMessage());
    }
  }

  private boolean isUrlValid(String[] urlPath) {
    return true;
  }

  // Validate whether resortID, seasonID, dayID, skierID is within certain range:
  //  resortID - between 1 and 10
  //  seasonID - 2024
  //  dayID - 1
  //  skierID - between 1 and 100000
  //  liftId - between 1 and 40
  //  time - between 1 and 360
  private boolean isPOSTRequestValid(Integer resortID, Integer seasonID, Integer dayID, Integer skierID, LiftRide liftRide) {
    // POST API: /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
    // urlPath  = "/1/seasons/2019/day/1/skier/123"
    // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
    if (resortID < 1 || resortID > 10 || seasonID != 2024 || skierID < 1 || skierID > 10000 || liftRide.getLiftID() < 1 || liftRide.getLiftID() > 40 || liftRide.getTime() < 1 || liftRide.getTime() > 360) {
      return false;
    } else {
      return true;
    }
  }

}
