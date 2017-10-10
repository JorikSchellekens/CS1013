package global;

import java.text.SimpleDateFormat;

/**
 * Created by Jorik on 14/03/2017.
 */
public final class DatabaseConstants {
  // Connection settings:
  public static final String PORT = "9094";
  public static final String URL = "jdbc:h2:tcp://localhost/./data/housingDatabase";

  // Database credentials:
  // Do not change these
  public static final String USER = "UKHousing";
  public static final String PASS = "UKHousing";

  public static final String DATA_SET = "pp50k";
  public static final String SOURCE = "data/" + DATA_SET + ".csv";

  // Fields:
  public static final String PRICE =        "Price";
  public static final String DATE =         "Date";
  public static final String POSTCODE =     "Postcode";
  public static final String TYPE =         "Type";
  public static final String OLD_NEW =      "OldNew";
  public static final String NUM_NAME =     "NumName";
  public static final String STREET =       "Street";
  public static final String LOCALITY =     "Locality";
  public static final String TOWN =         "Town";
  public static final String DISTRICT =     "District";
  public static final String COUNTY =       "County";

  public static final String[] FIELDS = {
      PRICE,
      DATE,
      POSTCODE,
      TYPE,
      OLD_NEW,
      NUM_NAME,
      STREET,
      LOCALITY,
      TOWN,
      DISTRICT,
      COUNTY};

  public static final String ASCENDING = "asc";
  public static final String DESCENDING = "desc";

  // Date formatting:
  public static final SimpleDateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("dd/MM/yy hh:mm");
  public static final SimpleDateFormat OUTPUT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
}
