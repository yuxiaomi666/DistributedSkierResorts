import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(value = "/skiers/*")
public class SkierServlet extends HttpServlet {
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("text/plain");
    String urlPath = req.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing paramterers");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

    if (!isUrlValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {
      res.setStatus(HttpServletResponse.SC_OK);
      // do any sophisticated processing with urlParts which contains all the url params
      // TODO: process url params in `urlParts`
      res.getWriter().write("It works!");
    }
  }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    // POST API: /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
    res.setContentType("text/plain");
    String urlPath = req.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing parameters");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

    if (!isPOSTRequestValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("Some of resortID, seasonID, dayID, skierID is not valid. "
          + "please try again!");
    } else {
      res.setStatus(HttpServletResponse.SC_OK);
      // TODO: process url params in `urlParts`
      res.getWriter().write("Received data for POST request!");
      System.out.println("Success POST request!");
    }
  }

  // Validate whether url contains resortID, seasonID, dayID, skierID
  // Validate whether resortID, seasonID, dayID, skierID is within certain range:
  //  resortID - between 1 and 10
  //  seasonID - 2024
  //  dayID - 1
  //  skierID - between 1 and 100000
  private boolean isUrlValid(String[] urlPath) {
    // urlPath  = "/1/seasons/2019/day/1/skier/123"
    // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
    return true;
  }
  private boolean isPOSTRequestValid(String[] urlPath) {
    // urlPath  = "/1/seasons/2019/day/1/skier/123"
    // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
    if (urlPath.length != 8) {
      return false;
    } else if (Integer.valueOf(urlPath[1]) < 1 || Integer.valueOf(urlPath[1]) > 10 || Integer.valueOf(urlPath[3]) != 2024 || Integer.valueOf(urlPath[5]) != 1 || Integer.valueOf(urlPath[7]) < 1 || Integer.valueOf(urlPath[1]) > 10000 ) {
      return false;
    } else {
      return true;
    }
  }
}
