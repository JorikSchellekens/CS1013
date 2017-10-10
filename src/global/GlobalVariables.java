package global;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Conor C on 3/13/2017.
 */
public final class GlobalVariables {
    private GlobalVariables(){}

    public static boolean menuScreenActive, searchAndStatsScreenActive, houseDetailScreenActive, queryBarChartActive;

    public static boolean townFieldActive = true;
    public static boolean averageFieldActive = true;
    public static boolean districtFieldActive, countyFieldActive, postCodeActive, top10FieldActive, bottom10FieldActive;
    public static boolean stdDevFieldActive, rangeFieldActive, freqDistActive, queryChanged, barChartDisplay, listDisplay;
    public static boolean lineChartDisplay, pieChartDisplay;
    public static boolean mapReset;


    public static String queryArea = "";
    public static String queryType = "";
    public static List<String> currentQuery = new ArrayList<>();

    public static int currentEvent = -1;
}
