package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import static global.DatabaseConstants.ASCENDING;
import static global.DatabaseConstants.DATE;
import static global.DatabaseConstants.DESCENDING;
import static global.DatabaseConstants.FIELDS;
import static global.DatabaseConstants.DATA_SET;
import static global.DatabaseConstants.PRICE;

/**
 * Created by Jorik on 22/03/2017.
 * Query api. This can be used by anyone to return their searches. A database would have to be built using the Database class
 * and searches executed through this.
 */
public class Query {

  private static final String queryString = "select %s from %s %s";

  private HashMap<String, ArrayList<String>> querySet;

  private String selection;

  private Calendar minDate;
  private Calendar maxDate;

  private String grouping;

  private String orderCategory;
  private String orderDirection;

  private int limit = 0;

  public Query(String selection) {
    this.selection = selection;
    querySet = new HashMap<>();
  }

  public boolean isValidField(String field){
    if (field == null) return false;
    for (String entry: FIELDS) {
      if (field.equals(entry)) return true;
    }
    return false;
  }

  public Query addEntry(String field, String entry) {
    return addEntries(field, entry);
  }

  public Query addEntries(String field, String... entries) {
    if (isValidField(field) && entries != null && entries.length != 0) {
      List<String> contents = querySet.get(field);
      if (contents == null) {
        querySet.put(field, new ArrayList(Arrays.asList(entries)));
      } else {
        contents.addAll(Arrays.asList(entries));
      }
    } else {
      System.err.println(field + " is not a valid table entry.");
    }
    return this;
  }

  public Query addTime(Calendar minDate, Calendar maxDate) {
    this.minDate = minDate;
    this.maxDate = maxDate;
    return this;
  }

  public Query addGrouping(String field) {
    grouping = field;
    return this;
  }

  public Query addAscendingOrder(String field) {
    orderDirection = ASCENDING;
    orderCategory = field;
    return this;
  }

  public Query addDescendingOrder(String field) {
    orderDirection = DESCENDING;
    orderCategory = field;
    return this;
  }

  public Query addLimit(int limit) {
    this.limit = limit;
    return this;
  }

  public Query removeField(String field) {
    if (field != null && querySet.keySet().contains(field)) {
      querySet.remove(field);
    } else {
      System.err.println("Could not remove field from query.");
    }
    return this;
  }

  public String[] getFields() {return querySet.keySet().toArray(new String[0]);}

  public List<String> getFieldEntries(String field) {
    return querySet.get(field);
  }

  public List execute(Class returnType) {
    Sql2o sql2o = Database.connect();
    try (Connection con = sql2o.open()) {
      return con.createQuery(String.format(queryString, selection, DATA_SET, queryBuilder())).executeAndFetch(returnType);
    }
  }

  public List execute() {
    Sql2o sql2o = Database.connect();
    try (Connection con = sql2o.open()) {
      return con.createQuery(String.format(queryString, selection, DATA_SET, queryBuilder())).executeAndFetch(String.class);
    }
  }

  public String queryBuilder() {
    String query = "";
    Set<String> keys = querySet.keySet();
    List<String> blocks = new ArrayList<>();

    if (keys.size() > 0) {
      for (String key : keys) {
        List<String> entries = querySet.get(key);
        List<String> conditions = new ArrayList<>();
        for (String entry : entries) {
          if (!key.equals(PRICE)) {
            conditions.add(String.join(" = ", key, "'" + entry + "'"));
          } else {
            conditions.add(String.join(" ", key, entry));
          }
        }
        blocks.add("(" + String.join((key.equals(PRICE)) ? " AND " : " OR ", conditions) + ")");
      }
    }

    if (minDate != null && maxDate != null) {
      blocks.add("(" + DATE + " >= '" + Database.calToString(minDate) +
          "' AND " + DATE + " <= '" + Database.calToString(maxDate) + "')");
    }

    query += String.join(" AND ", blocks);

    if (!query.isEmpty()) {
      query = "where " + query;
    }

    if (grouping != null) {
      query += String.format(" GROUP BY %s", grouping);
    }

    if (orderDirection != null && orderCategory != null) {
      query += " ORDER BY " + orderCategory + " " + orderDirection;
    }

    if (limit > 0) {
      query += String.format(" LIMIT %d", limit);
    }

    return query;
  }
}
