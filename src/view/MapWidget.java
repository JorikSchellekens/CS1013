package view;

import global.Colors;
import global.DatabaseConstants;
import global.GlobalVariables;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.Database;
import org.gicentre.geomap.GeoMap;
import presenter.ScreenController;
import processing.core.PVector;
import view.zoomer.ZoomPan;

/**
 * Created by Jorik on 29/03/2017.
 */
public class MapWidget extends Widget {
  int height;
  int width;
  boolean mousePressed;
  int queryId;

  GeoMap geoMap;
  // this uses a modified version of zoomPan
  ZoomPan zoomer;

  public MapWidget(int x, int y, int width, int height, String mapShapeFile) {
    this.xPosition = x;
    this.yPosition = y;
    this.height = height;
    this.width = width;
    this.geoMap = new GeoMap(x, y, width, height, ScreenController.screenControllerUI);
    geoMap.readFile(mapShapeFile);
    this.zoomer = new ZoomPan(ScreenController.screenControllerUI, new Rectangle(xPosition, yPosition, width, height));
    zoomer.setMaxZoomScale(15);
    zoomer.setMinZoomScale(0.75);
    mousePressed = false;
  }

  public void draw() {
    ScreenController.screenControllerUI.fill(Colors.DARK_GREEN.getRGB());
	  ScreenController.screenControllerUI.rect(xPosition, yPosition, width, height);
    ScreenController.screenControllerUI.clip(xPosition + 1, yPosition, width, height);
    ScreenController.screenControllerUI.fill(Colors.AZUREISH_WHITE.getRGB());
    ScreenController.screenControllerUI.pushMatrix();
    zoomer.transform();
    geoMap.draw();
    ScreenController.screenControllerUI.fill(255, 0, 0);
    highlightCounties();
    ScreenController.screenControllerUI.popMatrix();
    ScreenController.screenControllerUI.noClip();
    if(GlobalVariables.mapReset)
    {
    	this.reset();
    }
  }
  
  public void notifyMousePressed()
  {
	  addAreas();
  }
  
  public void reset() {
	  GlobalVariables.mapReset = false;
	 zoomer.setZoomScale(1);
	 zoomer.setPanOffset(0, 0);
  }

  public void highlightCounties() {
    PVector mousePosition = zoomer.getMouseCoord();
    int id = geoMap.getID(mousePosition.x, mousePosition.y);
    geoMap.draw(id);


    // Draw selected
    if (GlobalVariables.countyFieldActive && GlobalVariables.currentQuery != null && !GlobalVariables.currentQuery.isEmpty())
    {
      try
      {
        for (String part: GlobalVariables.currentQuery)
        {
          queryId = geoMap.getAttributeTable().findRow(part, 2).getInt("id");
          geoMap.draw(queryId);
        }
      }
      catch (NullPointerException ignore)
      {
//        System.out.println("County does not exist");
      }

    }
  }

  public void addAreas() {
    PVector mousePosition = zoomer.getMouseCoord();
    int id = geoMap.getID(mousePosition.x, mousePosition.y);
    String countyName = "";
    // Add selected
    if (id != -1) {
      countyName = geoMap.getAttributeTable().findRow(Integer.toString(id), 0).getString("ctyua15nm");
    }
    if (GlobalVariables.countyFieldActive && id != -1)
    {
      mousePressed = false;
    } else if (GlobalVariables.townFieldActive && !countyName.isEmpty() && id != -1) {
      List towns = Database.getRelated(DatabaseConstants.COUNTY, new ArrayList<>(Arrays.asList(countyName)), DatabaseConstants.TOWN);
      for (Object town: towns) {
        addOrRemoveQuery((String) town);
      }
    } else if (GlobalVariables.districtFieldActive && !countyName.isEmpty() && id != -1) {
      List towns = Database.getRelated(DatabaseConstants.COUNTY, new ArrayList<>(Arrays.asList(countyName)), DatabaseConstants.DISTRICT);
    }
  }

  private void addOrRemoveQuery(String query) {
    if (GlobalVariables.currentQuery.contains(query)) {
      GlobalVariables.currentQuery.remove(query);
    } else {
      GlobalVariables.currentQuery.add(query);
      GlobalVariables.queryChanged = true;
    }
  }
}
