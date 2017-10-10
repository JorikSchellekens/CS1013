package model;

import static global.DatabaseConstants.ASCENDING;
import static global.DatabaseConstants.DATA_SET;
import static global.DatabaseConstants.DESCENDING;
import static global.DatabaseConstants.OUTPUT_DATE_FORMAT;
import static global.DatabaseConstants.PASS;
import static global.DatabaseConstants.PRICE;
import static global.DatabaseConstants.SOURCE;
import static global.DatabaseConstants.URL;
import static global.DatabaseConstants.USER;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import org.h2.tools.Server;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import presenter.ScreenController;
import view.TimeSlider;

/**
 * Created by Jorik and Patrick on 14/03/2017.
 * Database class creates a local database.
 */

public class Database {
  private static Sql2o sql2o;

  /**
   * Run main when setting up a new table.
   * Do not run with
   */
//  public static void main(String[] args) {
//    try {
//     initialise();
//     dropHousingTable();
//     sqlTablePopulate();
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//  }

  private static void startDB() throws SQLException {
    Server.createTcpServer().start();
  }

  public static Sql2o connect() {
   return new Sql2o(URL, USER, PASS);
  }

  public static void initialise() {
    boolean dbsetup = false;
    while (!dbsetup) {
      try {
        startDB();
        sql2o = connect();
        dbsetup = true;
      } catch (SQLException exception) {
        exception.printStackTrace();
      }
    }
  }

  // Table management methods:  
  private static void dropHousingTable()
  {
    try (Connection con = sql2o.open()) {
      con.createQuery("DROP TABLE IF EXISTS " + DATA_SET + " ;").executeUpdate();
    }
  }
  
  private static void sqlTablePopulate()
  {
	  // ID, Duration, NumNam2, PPDCategory, and RecordStatus are only used for ppcomplete
	  try (Connection con = sql2o.open()) {
	      con.createQuery("CREATE TABLE IF NOT EXISTS " + DATA_SET + "("
//	    	 	  + "ID VARCHAR(MAX),"
	              + "Price INT,"
	              + "Date DATE,"
	              + "Postcode VARCHAR(MAX),"
	              + "Type VARCHAR(1),"
	              + "OldNew VARCHAR(1),"
//	              + "Duration VARCHAR(MAX),"
	              + "NumName VARCHAR(MAX),"
//	              + "NumName2 VARCHAR(MAX),"
	              + "Street VARCHAR(MAX),"
	              + "Locality VARCHAR(MAX),"
	              + "Town VARCHAR(MAX),"
	              + "District VARCHAR(MAX),"
	              + "County VARCHAR(MAX),"
//	              + "PPDCategory VARCHAR(MAX),"
//	              + "RecordStatus VARCHAR(MAX),"
          + ")"
	      + " AS SELECT * FROM CSVREAD('" + SOURCE + "')").executeUpdate();
	    }catch (NullPointerException e) {
	    }
  }
 
  // Queries

  public static float[] averageHousePrices(String field, String... names) {
    float prices[] = new float[names.length];
    for (int index = 0; index < names.length; index++) {
      prices[index] = averagePrice(field, names[index]);
    }
    return prices;
  }
  
  public static float[] standardDeviation(String field, String... names) {
	    float prices[] = new float[names.length];
	    for (int index = 0; index < names.length; index++) {
	      prices[index] = standardDeviation(field, names[index]);
	    }
	    return prices;
	  }
  
  public static float[] housePriceRanges(String field, String... names) {
	    float prices[] = new float[names.length];
	    for (int index = 0; index < names.length; index++) {
	      prices[index] = priceRange(field, names[index]);
	    }
	    return prices;
  }
  
  public static float[] pricesTopOrBottom(String field, String[] names, String ascOrDesc, int limit) {
	  	List array = pricesListTopOrBottom(field, names, ascOrDesc, limit);
	  	float[] prices = new float[array.size()];
	  	for (int index = 0; index < array.size(); index++)
	  	{
	  		prices[index] = Float.parseFloat((String) array.get(index));
	  	}
	   
	    return prices;
  }

  // Gets the average price of houses in a field and field value
  public static float averagePrice(String field, String name) {
    try (Connection con = sql2o.open()) {
      return con.createQuery("SELECT AVG(Price) "
              + "FROM " + DATA_SET
              + " WHERE " + field + " = '" + name 
              + "' AND Date >= '" + OUTPUT_DATE_FORMAT.format(TimeSlider.getMinDate().getTime())
              + "' AND Date <= '" + OUTPUT_DATE_FORMAT.format(TimeSlider.getMaxDate().getTime())
              + "';").executeAndFetch(Integer.class).get(0);
    } catch (NullPointerException e) {
//      System.err.println("No average found for " + field + " " + name);
      return 0;
    }
  }

//Gets a price range for value in a field
  public static float priceRange(String field, String name)
  {
    try (Connection con = sql2o.open()) {
      return con.createQuery("SELECT MAX(Price) - MIN(Price)"
          + " FROM " + DATA_SET 
          + " WHERE " + field + " = '" + name 
          + "' AND Date >= '" + OUTPUT_DATE_FORMAT.format(TimeSlider.getMinDate().getTime())
          + "' AND Date <= '" + OUTPUT_DATE_FORMAT.format(TimeSlider.getMaxDate().getTime())
          + "' ;").executeAndFetch(Float.class).get(0);
    }catch (NullPointerException e) {
//        System.err.println("No " + field + " field in database");
        return 0;
    }
  }
  
  //Gets a list of prices for a value in a field, either ascending or descending, and a given amount
  public static List pricesListTopOrBottom(String field, String[] names, String ascOrDesc, int limit)
  {
    Query query = new Query(PRICE).addEntries(field, names).addTime(TimeSlider.getMinDate(), TimeSlider.getMaxDate());

    if (ascOrDesc.equals(ASCENDING)) {
      query.addAscendingOrder(PRICE);
    } else if (ascOrDesc.equals(DESCENDING)) {
      query.addDescendingOrder(PRICE);
    }

    query.addLimit(limit);

    return query.execute();
  }

  public static float[] freqDistributionXAxis(String field, String[] areas, int intervals) {
      float highest = 0;
    Query query = new Query(PRICE);
    for (String area: areas) {
      query.addEntry(field, area);
    }
    List<Integer> values = query.addAscendingOrder(PRICE).addTime(ScreenController.screenControllerUI.getMinDate(), ScreenController.screenControllerUI.getMaxDate()).execute(Integer.class);
    if (values.size() > 0) {
        highest = values.get(values.size() - 1);
    }

    float[] count = new float[(int) ((highest - 1) / intervals) + 1];

    for (int price: values) {
      count[(price - 1)  / intervals] = count[(price - 1) / intervals] + 1;
    }
    return count;
  }
  
  //Gets the latest date
  public static String endDate()
  {
    try (Connection con = sql2o.open()) {
      return con.createQuery("SELECT Date "
          + "FROM " + DATA_SET
          + " ORDER BY Date ASC"
          + " LIMIT 1"
          + " ;").executeAndFetch(String.class).get(0);
    } catch (NullPointerException | IndexOutOfBoundsException e) {
//        System.err.println("Table empty");
//        System.exit(0);
        return null;
    }
  }
  
  //Gets the earliest date
  public static String startDate()
  {
    try (Connection con = sql2o.open()) {
      return con.createQuery("SELECT Date "
          + "FROM " + DATA_SET
          + " ORDER BY Date DESC"
          + " LIMIT 1"
          + " ;").executeAndFetch(String.class).get(0);
    } catch (NullPointerException | IndexOutOfBoundsException e) {
//        System.err.println("Table empty");
//        System.exit(0);
        return null;
        
    }
    	
  }
  
//Gets the standard deviation of the price for value in a field
  public static float standardDeviation(String field, String name)
  {
    try (Connection con = sql2o.open()) {
      return con.createQuery("SELECT STDDEV(price)"
          + " FROM " + DATA_SET 
          + " WHERE " + field + " = '" + name 
          + "' AND Date >= '" + OUTPUT_DATE_FORMAT.format(TimeSlider.getMinDate().getTime())
          + "' AND Date <= '" + OUTPUT_DATE_FORMAT.format(TimeSlider.getMaxDate().getTime())
          + "' ;").executeAndFetch(Float.class).get(0);
    }catch (NullPointerException e) {
//        System.err.println("No " + field + " field in database");
        return 0;
    }
  }

  public static String calToString(Calendar cal) {
    return OUTPUT_DATE_FORMAT.format(cal.getTime());
  }
  

  public static List getRelated(String field, List<String> names, String returnField) {
      return new Query(returnField).addEntries(field, names.toArray(new String[0])).addGrouping(returnField).execute();
  }
}