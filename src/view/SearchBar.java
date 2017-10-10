package view;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

import global.Colors;
import global.GlobalVariables;
import presenter.ScreenController;
import processing.core.PConstants;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

/**
 * Created by Jorik on 29/03/2017.
 */
public class SearchBar extends Widget {

  private String entry = "";
  private String ghost_text;
  private int cursor = 0;
  private boolean focused = false;
  private int promptWidth;
  private int fontSize = 17;
  private int margin = 2;
  private int radius;
  private float blinkDuration = 2;
  private Color promptColor = Colors.AZUREISH_WHITE;
  private Color backgroundColor = Colors.PICTON_BLUE;
  private Color textColor = Colors.PICTON_BLUE;
  private Color textTwoColor = Colors.AZUREISH_WHITE;
  private int cursorColor;


  public SearchBar(int xPosition, int yPosition, int width) {
    this.xPosition = xPosition;
    this.yPosition = yPosition;
    this.width = width;
    this.height = 25;
    this.fontSize = height - 2 * margin;
    this.radius = 3;
    this.ghost_text = determineGhostText();


    // Register as observer for mouse and keys
    ScreenController.screenControllerUI.registerMethod("mouseEvent", this);
    ScreenController.screenControllerUI.registerMethod("keyEvent", this);
  }

  public void mouseEvent(MouseEvent e) {
    if (e.getAction() == MouseEvent.PRESS) {
      if (mouseOver()) {
        focused = true;
      } else {
        focused = false;
      }
    }
  }

  public void keyEvent(KeyEvent keyEvent) {
    if (focused && keyEvent.getAction() == KeyEvent.PRESS) {
      int key = keyEvent.getKeyCode();
      switch (key) {
        case '\n':
          GlobalVariables.currentQuery = new ArrayList<>(Arrays.asList(entry.split(", ")));
//          System.out.println(GlobalVariables.currentQuery);
          reset();
          break;
        case '\b':
          delAtCursor();
          break;
        case 37:
          moveCursorLeft();
          break;
        case 39:
          moveCursorRight();
          break;
        default:
          insertAtCursor(keyEvent.getKey());
          break;
        case 38:
        case 46:
      }
    }
  }

  public void draw() {
    this.ghost_text = determineGhostText();
    ScreenController.screenControllerUI.textSize(fontSize);
    setPromptWidth(determinePreField());
    ScreenController.screenControllerUI.strokeWeight(1);
    ScreenController.screenControllerUI.stroke(200);
    ScreenController.screenControllerUI.fill(promptColor.getRGB());
    ScreenController.screenControllerUI.rect(xPosition, yPosition, promptWidth, height, radius, 0, 0, radius);
    ScreenController.screenControllerUI.fill((focused)? backgroundColor.brighter().getRGB(): backgroundColor.getRGB());
    ScreenController.screenControllerUI.rect(xPosition + promptWidth, yPosition, width - promptWidth, height, 0, radius, radius, 0);
    ScreenController.screenControllerUI.fill(textColor.getRGB());
    ScreenController.screenControllerUI.textAlign(PConstants.LEFT, PConstants.BOTTOM);
    ScreenController.screenControllerUI.text(determinePreField(), xPosition + margin, yPosition + height);
    ScreenController.screenControllerUI.stroke(0);
    ScreenController.screenControllerUI.strokeWeight(1);
    drawSearch();
    ScreenController.screenControllerUI.textAlign(PConstants.LEFT, PConstants.CENTER);

  }

  private static String determinePreField () {
    String toBuild = "";
    if (GlobalVariables.averageFieldActive) {
      toBuild = "AVG. PRICES in ";
    }
    else if (GlobalVariables.top10FieldActive) {
      toBuild = "HIGHEST PRICES in ";
    }
    else if (GlobalVariables.bottom10FieldActive) {
      toBuild = "LOWEST PRICES in ";
    }
    else if (GlobalVariables.stdDevFieldActive) {
      toBuild = "STD. DEVIATIONS OF PRICE in ";
    }
    else if (GlobalVariables.rangeFieldActive) {
      toBuild = "PRICE RANGE in ";
    }
    else if (GlobalVariables.freqDistActive) {
      toBuild = "FREQ. DISTRIBUTION in ";
    }

    return toBuild;
  }

  private static String determineGhostText() {
    String toBuild = "";
    if (GlobalVariables.townFieldActive) {
      toBuild = "TOWNS ";
    }
    else if (GlobalVariables.countyFieldActive) {
      toBuild += "COUNTIES ";
    }
    else if (GlobalVariables.districtFieldActive) {
      toBuild += "DISTRICTS ";
    }
    else if (GlobalVariables.postCodeActive) {
      toBuild += "POST CODES ";
    }

    return toBuild;
  }

  private void insertAtCursor(char c) {
    entry = (entry.substring(0, cursor) + c + entry.substring(cursor, entry.length())).toUpperCase();
    cursor ++;
  }

  private void delAtCursor() {
    if (cursor != 0) {
      entry = entry.substring(0, cursor - 1) + entry.substring(cursor, entry.length());
      cursor--;
    }
  }

  private void moveCursorLeft() {
    if (cursor != 0) {
      cursor--;
    }
  }

  private void moveCursorRight() {
    if (cursor < entry.length()) {
      cursor++;
    }
  }

  private void setPromptWidth(String text) {
    this.promptWidth = (int) ScreenController.screenControllerUI.textWidth(text) + 2 * margin;
  }

  private void drawCursor() {
    int time = ScreenController.screenControllerUI.millis();
    cursorColor = ScreenController.screenControllerUI.lerpColor(textColor.getRGB(), backgroundColor.brighter().getRGB(), (time % (1000 * blinkDuration)) / (blinkDuration * 1000));
    ScreenController.screenControllerUI.stroke(cursorColor);
    if (!textLongerThanAreaSearchArea()) {
      ScreenController.screenControllerUI.line(xPosition + promptWidth + margin + getCursorOffsetLength(),
          yPosition + height - margin, xPosition + promptWidth + margin + getCursorOffsetLength(),
          yPosition + margin);
    } else {
      ScreenController.screenControllerUI.line(xPosition + width - margin
      - (getEntryLength() - getCursorOffsetLength()), yPosition + margin, xPosition + width - margin - (getEntryLength() - getCursorOffsetLength()), yPosition + height - margin);
    }
    ScreenController.screenControllerUI.stroke(0);
  }

  private void drawSearch() {
    ScreenController.screenControllerUI.clip(xPosition + promptWidth + margin, yPosition, width - promptWidth - margin, height);
    if (entry.isEmpty()) {
      ScreenController.screenControllerUI.fill(240);
      ScreenController.screenControllerUI.text(ghost_text, xPosition + promptWidth + margin, yPosition + height);
    } else {
      if (!textLongerThanAreaSearchArea()) {
        ScreenController.screenControllerUI.fill(textTwoColor.getRGB());
        ScreenController.screenControllerUI.text(entry, xPosition + promptWidth + margin,
            yPosition + height);
      } else {
        ScreenController.screenControllerUI.textAlign(PConstants.RIGHT, PConstants.BOTTOM);
        ScreenController.screenControllerUI.text(entry, xPosition + width - margin, yPosition + height);
      }
    }

    if (focused) {
      drawCursor();
    }
    ScreenController.screenControllerUI.noClip();
  }

  private boolean textLongerThanAreaSearchArea () {
    return ScreenController.screenControllerUI.textWidth(entry) > width - promptWidth - margin * 2;
  }

  private float getEntryLength() {
    return ScreenController.screenControllerUI.textWidth(entry);
  }

  private float getCursorOffsetLength() {
    return ScreenController.screenControllerUI.textWidth(entry.substring(0, cursor));
  }

  private void reset() {
    entry = "";
    focused = false;
    cursor = 0;
    GlobalVariables.queryChanged = true;
  }

}
