package model;

import java.text.ParseException;

import static global.DatabaseConstants.INPUT_DATE_FORMAT;
import static global.DatabaseConstants.OUTPUT_DATE_FORMAT;

/**
 * Created by Jorik on 09/03/2017.
 */
public class HouseParser {
  private In housingDataStream;
  private String[] content;
  int current;

  public HouseParser(String dataFile) {
    housingDataStream = new In(dataFile);
    content = housingDataStream.readAllLines();
    current = 0;
    System.out.println(this.size());
  }


  public Building getNextBuilding() {
    if (hasNext()) {
      String[] fields = content[current].split("(,\")|(\",)|,(?![\\w\\s-]+\")");
      if (fields.length != 11) {
    	  return getNextBuilding();
      } else {
    	      Building building = null;
			try {
				building = new Building(
				  Integer.parseInt(fields[0]),
				  OUTPUT_DATE_FORMAT.format(INPUT_DATE_FORMAT.parse(fields[1])),
				  fields[2],
				  fields[3],
				  fields[4],
				  fields[5],
				  fields[6],
				  fields[7],
				  fields[8],
				  fields[9],
				  fields[10]
			);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	      current++;
	 //     System.out.println(fields[5]);
	      return building;
      }
    } else return null;
  }

  public boolean hasNext() {
    return (current < content.length);
  }
  
  public int size() {
	  return content.length;
  }

}
