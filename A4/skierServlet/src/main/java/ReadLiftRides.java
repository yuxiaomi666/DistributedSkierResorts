import java.util.HashSet;
import java.util.Set;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.Select;
import java.util.Map;
import java.util.HashMap;

public class ReadLiftRides {
  private final DynamoDbClient dynamoDbClient;
  private final static String TABLE_NAME = "CS6650LiftEvent";
  private final static String INDEX_NAME_1 = "GSI1";
  private final static String INDEX_NAME_2 = "GSI2";
  private final static String INDEX_NAME_3 = "GSI3";

  public ReadLiftRides() {
    this.dynamoDbClient = DynamoDbClient.builder().region(Region.US_WEST_2).build();
  }

  // Make LiftRides Singleton
  private static class SingletonHelper {
    private static final ReadLiftRides INSTANCE = new ReadLiftRides();
  }

  public static ReadLiftRides getInstance() {
    return SingletonHelper.INSTANCE;
  }

  // Get the total vertical for the skier for the specified ski day
  // example: /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
  // very slow??
//  public int getVerticalByResortAndDay(Integer resortID, Integer skierID, String seasonID,
//      String dayID) {
//    System.out.println("Querying DynamoDB with: ResortID = " + resortID + ", SeasonDayID = " + seasonID + "#" + dayID + ", skierID = " + skierID);
//    QueryRequest queryRequest = QueryRequest.builder()
//        .tableName(TABLE_NAME)
//        .indexName(INDEX_NAME_1)
//        .keyConditionExpression("ResortID = :v_id and SeasonDayID = :v_sid")
//        .filterExpression("SkierID = :v_skierID")
//        .expressionAttributeValues(Map.of(
//            ":v_id", AttributeValue.builder().n(String.valueOf(resortID)).build(),
//            ":v_sid", AttributeValue.builder().s(seasonID + "#" + dayID).build(),
//            ":v_skierID", AttributeValue.builder().n(String.valueOf(skierID)).build()
//        ))
//        .select(Select.COUNT)
//        .build();
//    return operateQuery(queryRequest);
//  }
  public int getVerticalByResortAndDay(Integer resortID, Integer skierID, String seasonID,
      String dayID) {
    QueryRequest queryRequest = QueryRequest.builder()
        .tableName(TABLE_NAME)
        .indexName(INDEX_NAME_1)
        .keyConditionExpression("ResortSkierID = :v_id and SeasonDayID = :v_sid")
        .expressionAttributeValues(Map.of(
            ":v_id",
            AttributeValue.builder().s(resortID + "#" + skierID).build(),
            ":v_sid", AttributeValue.builder().s(seasonID + "#" + dayID).build()
        ))
        .select(Select.COUNT)
        .build();
    return operateQuery(queryRequest);
  }

  // Get the total vertical for the skier the specified resort. If no season is
  // specified, return all seasons
  // example: /skiers/123/vertical?resort=1
  public int getVerticalByResort(Integer resortID, Integer skierID) {
    QueryRequest queryRequest = QueryRequest.builder()
        .tableName(TABLE_NAME)
        .indexName(INDEX_NAME_2)
        .keyConditionExpression("ResortID = :v_id and SkierID = :v_sid")
        .expressionAttributeValues(Map.of(
            ":v_id", AttributeValue.builder().n(String.valueOf(resortID)).build(),
            ":v_sid", AttributeValue.builder().n(String.valueOf(skierID)).build()
        ))
        .select(Select.COUNT)
        .build();
    return operateQuery(queryRequest);
  }

  // Get the total vertical for the skier the specified resort, with season specified
  // example: /skiers/123/vertical?resort=1&season=2018
  public int getVerticalByResort(Integer resortID, Integer skierID,
      String season) {
    QueryRequest queryRequest = QueryRequest.builder()
        .tableName(TABLE_NAME)
        .indexName(INDEX_NAME_2)
        .keyConditionExpression("ResortID = :v_id and SkierID = :v_sid")
        .filterExpression("SeasonID = :v_seasonID")
        .expressionAttributeValues(Map.of(
            ":v_id", AttributeValue.builder().n(String.valueOf(resortID)).build(),
            ":v_sid", AttributeValue.builder().n(String.valueOf(skierID)).build(),
            ":v_seasonID", AttributeValue.builder().s(String.valueOf(season)).build()
        ))
        .select(Select.COUNT)
        .build();
    return operateQuery(queryRequest);
  }

  // Get number of unique skiers at resort/season/day
  // example: /resorts/{resortID}/seasons/{seasonID}/day/{dayID}/skiers
  // Without project -> 100% error - OOM error
  public int getUniqueSkierByResortAndDay(Integer resortID, String seasonID,
      String dayID) {
    QueryRequest queryRequest = QueryRequest.builder()
        .tableName(TABLE_NAME)
        .indexName(INDEX_NAME_3)
        .keyConditionExpression("ResortID = :v_id and SeasonDayID = :v_sid")
        .expressionAttributeValues(Map.of(
            ":v_id", AttributeValue.builder().n(String.valueOf(resortID)).build(),
            ":v_sid", AttributeValue.builder().s(seasonID + "#" + dayID).build()
        ))
        .projectionExpression("SkierID")
        .build();
    QueryResponse response = dynamoDbClient.query(queryRequest);
    Set<String> uniqueSkiers = new HashSet<>();
    response.items().forEach(item -> uniqueSkiers.add(item.get("SkierID").n()));
    return uniqueSkiers.size();
  }

  private int operateQuery(QueryRequest queryRequest) {
    try {
      var response = dynamoDbClient.query(queryRequest);
      System.out.println("Count of items: " + response.count());
      return response.count();
    } catch (Exception e) {
      System.err.println("Query failed: " + e.getMessage());
      return -1;
    }
  }
}

