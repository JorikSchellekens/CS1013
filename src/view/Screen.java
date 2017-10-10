package view;

import global.DatabaseConstants;
import global.GlobalVariables;
import model.Database;
import presenter.ScreenController;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;


public class Screen {


    private ArrayList<Widget> widgetList;
    private int screenWidth, screenHeight;
    private int screenX, screenY;

    public Screen(int screenWidth, int screenHeight, int screenX, int screenY) {
        this.screenX = screenX;
        this.screenY = screenY;
        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;
        widgetList = new ArrayList<Widget>();
        int adjust;
        //Process to add in a (dropdown || list) type menu for selecting search areas
        widgetList.add(new DropdownMenuSelector(screenX, screenY, "SEARCH IN", screenWidth, screenHeight / 20));
        adjust = screenHeight / 20;
        String[] fieldsArea = {"Towns", "Counties", "Districts", "Post Codes"};
        for (int i = 0; i < 4; i++) {
            widgetList.add(new DropDownButton(screenX, screenY + adjust, fieldsArea[i], 13, screenWidth, screenHeight / 20));
            adjust += screenHeight / 20;
        }
        //Process to add in a (dropdown || list) type menu for selecting query types
        widgetList.add(new DropdownMenuSelector(screenX, screenY + ((screenHeight / 20) * 5), "SEARCH TYPE", screenWidth, screenHeight / 20));
        adjust = screenHeight / 20;
        String[] fieldsType = {"Avg. Price", "Top 15", "Bottom 15", "Std. Deviation", "Range", "Freq. Distribution"};
        for (int i = 0; i < 6; i++) {
            widgetList.add(new DropDownButton(screenX, screenY + ((screenHeight / 20) * 5) + adjust, fieldsType[i], 18, screenWidth, screenHeight / 20));
            adjust += screenHeight / 20;
        }
        //Adds in buttons which select the statistical display types
        widgetList.add(new DropdownMenuSelector(screenX, screenY + ((screenHeight / 20) * 12), "GRAPH TYPE", screenWidth, screenHeight / 20));
        String[] buttonLabel = {"Bar Chart", "List", "Line Chart", "Pie Chart"};
        adjust = screenHeight / 20;
        for (int i = 0; i < 4; i++) {
            widgetList.add(new DropDownButton(screenX, screenY + ((screenHeight / 20) * 12) + adjust, buttonLabel[i], 18, screenWidth, screenHeight / 20));
            adjust += screenHeight / 20;
        }
        //Adds in ability to select a time period for a certain query
        try {
            Calendar endDate = Calendar.getInstance();
            endDate.setTime(DatabaseConstants.OUTPUT_DATE_FORMAT.parse(Database.endDate()));
            Calendar startDate = Calendar.getInstance();
            startDate.setTime(DatabaseConstants.OUTPUT_DATE_FORMAT.parse((Database.startDate())));
            widgetList.add(new TimeSlider(endDate, startDate, 0, ScreenController.screenControllerUI.height - 40, ScreenController.screenControllerUI.width, 40));
//            System.out.println(widgetList.size());
        } catch (ParseException ignore) {
        }
    }


    public void draw() {
        for (int i = 0; i < widgetList.size(); i++) {
            Widget widgetToDraw = (Widget) widgetList.get(i);
            determineDataViewButtons();
            widgetToDraw.draw();
        }
    }

    public void determineDataViewButtons() {
        Widget widget;

        if (GlobalVariables.averageFieldActive || GlobalVariables.rangeFieldActive ||
                GlobalVariables.stdDevFieldActive) {
            widget = widgetList.get(13);
            widget.setCanDraw(true);
            widget = widgetList.get(14);
            widget.setCanDraw(true);
            widget.setyPosition((screenHeight / 20) * 14);
            widget = widgetList.get(15);
            widget.setCanDraw(false);
            widget = widgetList.get(16);
            widget.setCanDraw(false);
        } else if (GlobalVariables.top10FieldActive || GlobalVariables.bottom10FieldActive) {
            widget = widgetList.get(13);
            widget.setCanDraw(false);
            widget = widgetList.get(14);
            widget.setCanDraw(true);
            widget.setyPosition((screenHeight / 20) * 13);
            widget = widgetList.get(15);
            widget.setCanDraw(false);
            widget = widgetList.get(16);
            widget.setCanDraw(false);
        } else if
                (GlobalVariables.freqDistActive) {
            widget = widgetList.get(13);
            widget.setCanDraw(false);
            widget = widgetList.get(14);
            widget.setCanDraw(false);
            widget = widgetList.get(15);
            widget.setCanDraw(true);
            widget.setyPosition((screenHeight / 20) * 13);
            widget = widgetList.get(16);
            widget.setCanDraw(false);
        }
    }

    public int getScreenEvent() {
        for (int i = 0; i < widgetList.size(); i++) {
            Widget widget = widgetList.get(i);
            if (widget.mouseOver() && widget.canDraw) {
                return i;
            }
        }
        return -1;
    }

    public Widget getWidget(int i) {
        return widgetList.get(i);
    }

    public void notifyMouseReleased() {
        ((TimeSlider) widgetList.get(17)).notifyMouseReleased();
    }

    public Calendar getMinDate() {
        return ((TimeSlider) widgetList.get(17)).getMinDate();
    }

    public Calendar getMaxDate() {
        return ((TimeSlider) widgetList.get(17)).getMaxDate();
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }
}
