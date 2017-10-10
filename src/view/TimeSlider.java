package view;

import global.GlobalVariables;
import java.awt.Color;
import java.util.Calendar;
import java.util.Locale;

import global.Colors;
import jdk.nashorn.internal.objects.Global;
import presenter.*;
import processing.core.PConstants;
import processing.core.PImage;

/**
 * Created by Jorik on 23/03/2017.
 */

public class TimeSlider extends Widget {
  private int sliderHeight;
  private int dateDisplayHeight;

  private Calendar startDate;
  private Calendar endDate;
  private int margin = 13;

  private TimeBar timeBar;
  private static TimeIndicator minIndicator;
  private static TimeIndicator maxIndicator;
  private int frameWhenLocked = 0;
  private static final int REFRESH_RATE = 10;

  private boolean minLockedToMouse = false;
  private boolean maxLockedToMouse = false;
  private DateDisplay dateDisplay;

  public static final int MIN_RANGE = 50;

  TimeSlider(Calendar startDate, Calendar endDate, int x, int y, int width, int height) {
    this.xPosition = x;
    this.yPosition = y;
    this.startDate = startDate;
    this.endDate = endDate;
    setHeight(height);
    this.width = width;
    this.timeBar = new TimeBar(x + sliderHeight / 2, y + sliderHeight / 2, sliderHeight - 2 * margin, width - sliderHeight);
    this.minIndicator = new TimeIndicator(x + sliderHeight / 2, y + sliderHeight / 2, sliderHeight - 2 * margin + 20, startDate);
    this.maxIndicator = new TimeIndicator(x + sliderHeight / 2 + mapCalendarToXOffset(endDate), y + sliderHeight / 2, sliderHeight - 2 * margin + 20, endDate);
    this.dateDisplay = new DateDisplay(x + sliderHeight / 2, y + height - dateDisplayHeight / 2, dateDisplayHeight, width - sliderHeight, startDate, endDate);
  }

  public void setHeight(int height) {
    this.height = height;
    this.sliderHeight = (height * 3) / 4;
    this.dateDisplayHeight = height / 4;
  }

  public static Calendar getMinDate() {
    return minIndicator.getCalendar();
  }

  public static Calendar getMaxDate() {
    return maxIndicator.getCalendar();
  }

  private int mapCalendarToXOffset(Calendar date) {
    return (int) ScreenController.map(date.getTimeInMillis(), startDate.getTimeInMillis(), endDate.getTimeInMillis(), 0, width - sliderHeight);
  }

  public Calendar mapXOffsetToCalendar(int x) {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis((long) ScreenController.screenControllerUI.map(x, 0, width - sliderHeight, startDate.getTimeInMillis(), endDate.getTimeInMillis()));
    return cal;
  }

  boolean mouseOverParts() {
    return (minIndicator.mouseOver() || maxIndicator.mouseOver() || timeBar.mouseOver());
  }

  public void notifyMousePressed() {
    int mouseX = ScreenController.screenControllerUI.mouseX;
    if (minIndicator.mouseOver()) {
      minLockedToMouse = true;
      frameWhenLocked = ScreenController.screenControllerUI.frameCount;
    } else if (maxIndicator.mouseOver()) {
      maxLockedToMouse = true;
      frameWhenLocked = ScreenController.screenControllerUI.frameCount;
    } else if (timeBar.mouseOver()) {
      TimeIndicator closestIndicator =
          (Math.abs(mouseX - minIndicator.getX()) <
              Math.abs(mouseX - maxIndicator.getX()))? minIndicator : maxIndicator;
      moveIndicator(closestIndicator, mouseX);
    }
  }

  public void notifyMouseReleased() {
    if (maxLockedToMouse || minLockedToMouse) {
      GlobalVariables.queryChanged = true;
    }
    minLockedToMouse = false;
    maxLockedToMouse = false;
  }

  public void moveIndicator(TimeIndicator indicator, int x) {
    indicator.setX(x);
    indicator.setCalendar(mapXOffsetToCalendar(x));
    indicator.notifyMoved();
  }

  private String calToString(Calendar cal) {
    return String.join("-",
        Integer.toString(cal.get(Calendar.DAY_OF_MONTH)),
        cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()),
        Integer.toString(cal.get(Calendar.YEAR)));
  }

  public void draw() {
    ScreenController.screenControllerUI.rectMode(PConstants.CORNER);
    ScreenController.screenControllerUI.fill(240);
    ScreenController.screenControllerUI.rect(xPosition, yPosition, width, height);
    timeBar.draw();
    dateDisplay.draw();

    int mouseX = ScreenController.screenControllerUI.mouseX;
    if (minLockedToMouse
        && mouseX < maxIndicator.getX() - MIN_RANGE && mouseX > xPosition + sliderHeight / 2) {
      moveIndicator(minIndicator, ScreenController.screenControllerUI.mouseX);
    }
    minIndicator.draw();

    if (maxLockedToMouse
        && mouseX > minIndicator.getX() + MIN_RANGE && mouseX < xPosition + width - sliderHeight / 2) {
      moveIndicator(maxIndicator, ScreenController.screenControllerUI.mouseX);
    }
    maxIndicator.draw();

    if ((minLockedToMouse || maxLockedToMouse) && (ScreenController.screenControllerUI.frameCount - frameWhenLocked) % (int) (ScreenController.screenControllerUI.frameRate / REFRESH_RATE) == 1) {
      GlobalVariables.queryChanged = true;
    }
  }

  private class TimeBar {
    // width is calculated from length and height. X, Y indicates the left origin of the bar where the indicator should be shown.
    private int x;
    private int y;
    private int height;
    private int width;
    private Color color = new Color(200, 200, 200);

    TimeBar(int x, int y, int height, int length) {
      this.x = x - height / 2;
      this.y = y - height / 2;
      this.height = height;
      this.width = length + height;
    }

    TimeBar(int x, int y, int height, int length, Color color) {
      this(x, y, height, length);
      this.color = color;
    }

    public Color getColor() {
      return color;
    }

    public void setColor(Color color) {
      this.color = color;
    }

    public boolean mouseOver() {
      return (
          ScreenController.screenControllerUI.mouseX >= x
      &&  ScreenController.screenControllerUI.mouseX <= x + width
      &&  ScreenController.screenControllerUI.mouseY >= y
      &&  ScreenController.screenControllerUI.mouseY <= y + height);
    }

    void draw() {
      ScreenController.screenControllerUI.rectMode(PConstants.CORNER);
      ScreenController.screenControllerUI.fill(color.getRGB());
      ScreenController.screenControllerUI.rect(x, y, width, height, height / 2);
    }

  }

  public class TimeIndicator {
    private int x;
    private int y;
    private Calendar date;

    private int lastFrameMoved = 0;
    private int secondsToDisplay = 1;

    private PImage photo = ScreenController.screenControllerUI.loadImage("trackerImage.png");

    TimeIndicator(int x, int y, int height, Calendar date) {
      photo.resize(0, height);
      this.x = x;
      setY(y);
      this.date = date;
    }

    public int getX() {
      return x;
    }

    public void setX(int x) {
      this.x = x;
    }

    public int getY() {
      return y;
    }

    public void setY(int y) {
      this.y = (int) (y + photo.height * 0.2222222);
    }

    public Calendar getCalendar() {
      return date;
    }

    public void setCalendar(Calendar date) {
      this.date = date;
    }

    void draw() {
      ScreenController.screenControllerUI.imageMode(PConstants.CENTER);
      ScreenController.screenControllerUI.image(photo, x, y);
      ScreenController.screenControllerUI.imageMode(PConstants.CORNER);
      drawDateNotifier();
    }

    public boolean mouseOver() {
      float mouseX = ScreenController.screenControllerUI.mouseX;
      float mouseY = ScreenController.screenControllerUI.mouseY;
      return (mouseX >= x - photo.width / 2
              && mouseX <= x + photo.width / 2
              && mouseY >= y - photo.width / 2
              && mouseY <= y + photo.width / 2);
    }

    public void notifyMoved() {
      lastFrameMoved = ScreenController.screenControllerUI.frameCount;
    }

    public void drawDateNotifier() {
      int alpha = 255 - (int) (255 * ((ScreenController.screenControllerUI.frameCount - lastFrameMoved) /
          (ScreenController.screenControllerUI.frameRate * secondsToDisplay)));
      if (alpha >= 0 && alpha <= 255) {
        ScreenController.screenControllerUI.textAlign(PConstants.CENTER);
        ScreenController.screenControllerUI.fill(Colors.DATE_INDICATOR_COLOR.getRGB(), alpha);
        ScreenController.screenControllerUI.text(calToString(date), x, y - photo.height);
        ScreenController.screenControllerUI.textAlign(PConstants.CORNER);
      }
    }
  }

  private class DateDisplay {
    private int x;
    private int y;
    private int length;
    private Calendar startDate;
    private Calendar endDate;

    public DateDisplay(int x, int y, int height, int length, Calendar startDate, Calendar endDate) {
      this.x = x;
      this.y = y;
      this.length = length;
      this.startDate = startDate;
      this.endDate = endDate;
    }

    void draw() {
      ScreenController.screenControllerUI.textSize(10);
      ScreenController.screenControllerUI.fill(20);

      ScreenController.screenControllerUI.textAlign(PConstants.LEFT);
      ScreenController.screenControllerUI.text(calToString(startDate), x, y);

      ScreenController.screenControllerUI.textAlign(PConstants.RIGHT);
      ScreenController.screenControllerUI.text(calToString(endDate), x + length, y);

      ScreenController.screenControllerUI.textAlign(PConstants.CORNER);
    }
  }
}