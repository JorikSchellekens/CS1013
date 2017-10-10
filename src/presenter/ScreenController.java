package presenter;

import java.util.ArrayList;
import java.util.Calendar;
import org.gicentre.utils.stat.BarChart;
import org.gicentre.utils.stat.XYChart;
import processing.core.PApplet;
import processing.core.PFont;

import view.*;
import model.Database;
import global.*;

public class ScreenController extends PApplet
{
	public static ScreenController screenControllerUI;

	StatsDisplay statsDisplay;
	SearchBar searchBar;
	PFont ourFont;
	Screen screenMenuButtons, screenStatsDisplay;
	BarChart chart;
	XYChart scatterPlot;
	Database database;
	AreaIndicator areaIndicator;

	private static final int SEARCH_BAR_MARGIN = 10;

	public static void main(String[] args) {
		PApplet.main("presenter.ScreenController");
	}

	@Override
	public void settings() {
		screenControllerUI = this;
		fullScreen();
//		size(1050, 700);
	}

	@Override
	public void setup() {
		database.initialise();
		chart = new BarChart(this);
		scatterPlot = new XYChart(this);
		ourFont = createFont("HelveticaRegular", 20);
		GlobalVariables.menuScreenActive = true;

		int menuButtonWidth = width / 8;
		int areaIndicatorWidth = width / 3;
		screenMenuButtons = new Screen(menuButtonWidth, screenControllerUI.height,
				0, 0);

		statsDisplay = new StatsDisplay(menuButtonWidth + screenControllerUI.width / 40, screenControllerUI.height / 15,
				chart, scatterPlot, screenControllerUI.width/2, ((int) (screenControllerUI.height/1.25)));
		searchBar = new SearchBar(menuButtonWidth + SEARCH_BAR_MARGIN, SEARCH_BAR_MARGIN, width - menuButtonWidth - areaIndicatorWidth - 2 * SEARCH_BAR_MARGIN);

		areaIndicator = new AreaIndicator(width - areaIndicatorWidth, 0, areaIndicatorWidth,
				height - 40, "England_and_wales/England_and_wales");
	}

	public void draw() {
		background(Colors.DARK_GREEN.getRGB());
		textFont(ourFont, 15);
		fill((Colors.PICTON_BLUE.darker()).getRGB());
		rect(0, 0, screenMenuButtons.getScreenWidth(), screenMenuButtons.getScreenHeight());
		stroke(1);
		areaIndicator.draw();
		statsDisplay.draw();
		searchBar.draw();
		screenMenuButtons.draw();

	}

	public void mousePressed() {
		areaIndicator.notifyMapMousePressed();
		int event;
		if (GlobalVariables.menuScreenActive) {
			event = screenMenuButtons.getScreenEvent();
			switch (event) {
				case 1:
					GlobalVariables.townFieldActive = true;
					GlobalVariables.districtFieldActive = false;
					GlobalVariables.countyFieldActive = false;
					GlobalVariables.postCodeActive = false;
//					System.out.println("Towns = " + GlobalVariables.townFieldActive);
//					System.out.println(event);
					break;
				case 2:
					GlobalVariables.townFieldActive = false;
					GlobalVariables.districtFieldActive = false;
					GlobalVariables.countyFieldActive = true;
					GlobalVariables.postCodeActive = false;
//					System.out.println("Counties = " + GlobalVariables.countyFieldActive);
//					System.out.println(event);
					break;
				case 3:
					GlobalVariables.townFieldActive = false;
					GlobalVariables.districtFieldActive = true;
					GlobalVariables.countyFieldActive = false;
					GlobalVariables.postCodeActive = false;
//					System.out.println("Districts = " + GlobalVariables.districtFieldActive);
//					System.out.println(event);
					break;
				case 4:
					GlobalVariables.townFieldActive = false;
					GlobalVariables.districtFieldActive = false;
					GlobalVariables.countyFieldActive = false;
					GlobalVariables.postCodeActive = true;
//					System.out.println("Post Codes = " + GlobalVariables.postCodeActive);
//					System.out.println(event);
					break;
				case 6:
					GlobalVariables.averageFieldActive = true;
					GlobalVariables.top10FieldActive = false;
					GlobalVariables.bottom10FieldActive = false;
					GlobalVariables.stdDevFieldActive = false;
					GlobalVariables.rangeFieldActive = false;
					GlobalVariables.freqDistActive = false;
//					System.out.println("Average Price = " + GlobalVariables.averageFieldActive);
//					System.out.println(event);
					break;
				case 7:
					GlobalVariables.averageFieldActive = false;
					GlobalVariables.top10FieldActive = true;
					GlobalVariables.bottom10FieldActive = false;
					GlobalVariables.stdDevFieldActive = false;
					GlobalVariables.rangeFieldActive = false;
					GlobalVariables.freqDistActive = false;
//					System.out.println("Top 10 = " + GlobalVariables.top10FieldActive);
//					System.out.println(event);
					break;
				case 8:
					GlobalVariables.averageFieldActive = false;
					GlobalVariables.top10FieldActive = false;
					GlobalVariables.bottom10FieldActive = true;
					GlobalVariables.stdDevFieldActive = false;
					GlobalVariables.rangeFieldActive = false;
					GlobalVariables.freqDistActive = false;
//					System.out.println("Bottom 10 = " + GlobalVariables.bottom10FieldActive);
//					System.out.println(event);
					break;
				case 9:
					GlobalVariables.averageFieldActive = false;
					GlobalVariables.top10FieldActive = false;
					GlobalVariables.bottom10FieldActive = false;
					GlobalVariables.stdDevFieldActive = true;
					GlobalVariables.rangeFieldActive = false;
					GlobalVariables.freqDistActive = false;
//					System.out.println("Standard Deviation = " + GlobalVariables.stdDevFieldActive);
//					System.out.println(event);
					break;
				case 10:
					GlobalVariables.averageFieldActive = false;
					GlobalVariables.top10FieldActive = false;
					GlobalVariables.bottom10FieldActive = false;
					GlobalVariables.stdDevFieldActive = false;
					GlobalVariables.rangeFieldActive = true;
					GlobalVariables.freqDistActive = false;
//					System.out.println("Range = " + GlobalVariables.rangeFieldActive);
//					System.out.println(event);
					break;
				case 11:
					GlobalVariables.averageFieldActive = false;
					GlobalVariables.top10FieldActive = false;
					GlobalVariables.bottom10FieldActive = false;
					GlobalVariables.stdDevFieldActive = false;
					GlobalVariables.rangeFieldActive = false;
					GlobalVariables.freqDistActive = true;
//					System.out.println("Frequency Distribution = " + GlobalVariables.freqDistActive);
//					System.out.println(event);
					break;
				case 13:
					GlobalVariables.barChartDisplay = true;
					GlobalVariables.listDisplay = false;
					GlobalVariables.lineChartDisplay = false;
					GlobalVariables.pieChartDisplay = false;
//					System.out.println(event);
					break;
				case 14:
					GlobalVariables.barChartDisplay = false;
					GlobalVariables.listDisplay = true;
					GlobalVariables.lineChartDisplay = false;
					GlobalVariables.pieChartDisplay = false;
//					System.out.println(event);
					break;
				case 15:
					GlobalVariables.barChartDisplay = false;
					GlobalVariables.listDisplay = false;
					GlobalVariables.lineChartDisplay = true;
					GlobalVariables.pieChartDisplay = false;
//					System.out.println(event);
					break;
				case 16:
					GlobalVariables.barChartDisplay = false;
					GlobalVariables.listDisplay = false;
					GlobalVariables.lineChartDisplay = false;
					GlobalVariables.pieChartDisplay = true;
//					System.out.println(event);
					break;	
				case 17:
					((TimeSlider) screenMenuButtons.getWidget(event)).notifyMousePressed();
					break;
				case -1:
					GlobalVariables.currentEvent = event;
//					System.out.println(GlobalVariables.currentEvent);
			}

			switch (event) {
				case 0:
				case 5:
				case 12:
					break;
				case 1:
				case 2:
				case 3:
				case 4:
					GlobalVariables.currentQuery = new ArrayList<>();
				default:
					GlobalVariables.queryChanged = true;
			}

		}
	}

	public void mouseReleased() {
		screenMenuButtons.notifyMouseReleased();
	}

	public Calendar getMaxDate() {
		return screenMenuButtons.getMaxDate();
	}

	public Calendar getMinDate() {
		return screenMenuButtons.getMinDate();
	}
}
