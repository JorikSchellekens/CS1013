package view;

import global.GlobalVariables;

/**
 * Created by Jorik on 29/03/2017.
 */
public class AreaIndicator extends Widget {
  private MapWidget map;
  private LastSearchedDisplay lastSearchedDisplay;
  private int width;
  private int height;
  private ClearMapQueryButton clearMapQueryButton;
  private ResetMapButton resetMapButton;



  public AreaIndicator(int x, int y, int width, int height, String shapeFile) {
    this.xPosition = x;
    this.yPosition = y;
    this.width = width;
    this.height = height;
    this.lastSearchedDisplay = new LastSearchedDisplay(x, y, width);
    int searchDisplayHeight = lastSearchedDisplay.getHeight();
    this.map = new MapWidget(x, y + searchDisplayHeight, width, height - searchDisplayHeight, shapeFile);
    int buttonHeight = 20;
    clearMapQueryButton = new ClearMapQueryButton(xPosition, yPosition + height - buttonHeight, width / 2, buttonHeight);
    resetMapButton = new ResetMapButton(xPosition + width / 2, yPosition + height - buttonHeight, width / 2, buttonHeight);
  }

  public void draw() {
    map.draw();
    clearMapQueryButton.draw();
    resetMapButton.draw();
    if (GlobalVariables.queryChanged) {
      lastSearchedDisplay.setEntries(GlobalVariables.currentQuery);
    }
    lastSearchedDisplay.draw();

  }
  
  public void notifyMapMousePressed() {
	  map.notifyMousePressed();
	  clearMapQueryButton.notifyMousePressed();
	  resetMapButton.notifyMousePressed();
  }
}
