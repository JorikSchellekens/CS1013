package view;

import global.Colors;
import global.DatabaseConstants;
import global.GlobalVariables;
import model.Database;
import org.gicentre.utils.stat.BarChart;
import org.gicentre.utils.stat.XYChart;
import presenter.ScreenController;
import processing.core.PConstants;
import static presenter.ScreenController.screenControllerUI;

public class StatsDisplay extends Widget {

    private BarChart chart;
    private XYChart scatterPlot;
    private float[] data;
    private float[] scatterPlotX;
    private float[] scatterPlotY;
    private EntryDisplay[] priceListBoxes;
    private int width, height;

    public StatsDisplay(int xPosition, int yPosition, BarChart chart, XYChart scatterPlot, int width, int height) {
        this.chart = chart;
        this.scatterPlot = scatterPlot;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.width = width;
        this.height = height;
    }

    public void draw () {
        ScreenController.screenControllerUI.textSize(15);
        ScreenController.screenControllerUI.fill(0);
        if (!GlobalVariables.currentQuery.isEmpty() && GlobalVariables.queryChanged) {
            data = determineAndCommenceCalculation();
            if (!GlobalVariables.freqDistActive) {
                generatePriceList(data);
            }
            if (GlobalVariables.freqDistActive) {
                int intervals = 100000;
                scatterPlotX = generateScatterPlotX(intervals);
                scatterPlotY = new float[scatterPlotX.length];
                for (int index = 0; index < scatterPlotY.length; index++) {
                    scatterPlotY[index] = index * intervals;
                }
            }
            GlobalVariables.queryChanged = false;
        }
        if (GlobalVariables.currentQuery != null && !GlobalVariables.currentQuery.equals("")) {
            if (GlobalVariables.pieChartDisplay) {
                screenControllerUI.fill(Colors.ASH_GREY.getRGB());
                screenControllerUI.text("WE NEED TO CONSTRUCT ADDITIONAL PYLONS.", xPosition, yPosition);
            }
            if (GlobalVariables.lineChartDisplay && GlobalVariables.freqDistActive && scatterPlotX != null && scatterPlotY != null) {
                scatterPlot.setMinX(0);
                scatterPlot.setMinY(0);
                scatterPlot.showXAxis(true);
                scatterPlot.showYAxis(true);
                scatterPlot.setPointColour(screenControllerUI.color(xPosition, yPosition, 10));
                scatterPlot.setPointSize(5);
                scatterPlot.setLineWidth(3);
                scatterPlot.setLineColour(Colors.DEEP_KOMARU.getRGB());
                scatterPlot.setData(scatterPlotY, scatterPlotX);
                scatterPlot.setXAxisLabel("PRICES (£)");
                scatterPlot.setYAxisLabel("NUMBER OF HOUSES \n");
                scatterPlot.draw(xPosition, yPosition, width, height);
            }
            if (GlobalVariables.listDisplay) {
                String toPrint = "";
                if (data != null) {
                    for (int i = 0; i < priceListBoxes.length; i++) {
                        priceListBoxes[i].draw();
                    }
                }
                screenControllerUI.fill(0);
                screenControllerUI.textAlign(PConstants.LEFT, PConstants.TOP);
                screenControllerUI.text(toPrint, xPosition, yPosition);
            }
            if (GlobalVariables.averageFieldActive || GlobalVariables.rangeFieldActive || GlobalVariables.stdDevFieldActive) {
                if(GlobalVariables.barChartDisplay && data != null) {
                    chart.setBarColour(screenControllerUI.color(Colors.DEEP_KOMARU.getRGB()));
                    chart.showCategoryAxis(true);
                    chart.showValueAxis(true);
                    chart.setMinValue(0);
                    chart.setBarGap(30);
                    chart.setData(data);
                    chart.setBarLabels(GlobalVariables.currentQuery.toArray(new String[0]));
                    chart.setCategoryAxisLabel("AREAS");
                    chart.setValueAxisLabel("PRICE IN POUNDS (£) \n\n");
                    if (data.length >= 5) {
//                        screenControllerUI.textSize(10);
                        chart.showCategoryAxis(false);
                    }
//                    else if (data.length >=8) {
//                        chart.showCategoryAxis(false);
//                    }
                    chart.draw(xPosition + 20, yPosition, width - 20, height);
                    screenControllerUI.textSize(18);
                }
            }
        }
    }

    public float[] determineAndCommenceCalculation () {
        if (GlobalVariables.averageFieldActive) {
            if (GlobalVariables.townFieldActive) {
                return Database.averageHousePrices(DatabaseConstants.TOWN, GlobalVariables.currentQuery.toArray(new String[0]));
            }
            else if (GlobalVariables.countyFieldActive) {
                return Database.averageHousePrices(DatabaseConstants.COUNTY, GlobalVariables.currentQuery.toArray(new String[0]));
            }
            else if (GlobalVariables.postCodeActive) {
                return Database.averageHousePrices(DatabaseConstants.POSTCODE, GlobalVariables.currentQuery.toArray(new String[0]));
            }
            else if (GlobalVariables.districtFieldActive) {
                return Database.averageHousePrices(DatabaseConstants.DISTRICT, GlobalVariables.currentQuery.toArray(new String[0]));
            }
        }
        else if (GlobalVariables.rangeFieldActive) {
            if (GlobalVariables.townFieldActive) {
                return Database.housePriceRanges(DatabaseConstants.TOWN, GlobalVariables.currentQuery.toArray(new String[0]));
            }
            else if (GlobalVariables.countyFieldActive) {
                return Database.housePriceRanges(DatabaseConstants.COUNTY, GlobalVariables.currentQuery.toArray(new String[0]));
            }
            else if (GlobalVariables.districtFieldActive) {
                return Database.housePriceRanges(DatabaseConstants.DISTRICT, GlobalVariables.currentQuery.toArray(new String[0]));
            }
            else if (GlobalVariables.postCodeActive) {
                return Database.housePriceRanges(DatabaseConstants.POSTCODE, GlobalVariables.currentQuery.toArray(new String[0]));
            }
        }
        else if (GlobalVariables.top10FieldActive) {
            if (GlobalVariables.townFieldActive) {
                return Database.pricesTopOrBottom(DatabaseConstants.TOWN, GlobalVariables.currentQuery.toArray(new String[0]),
                        DatabaseConstants.DESCENDING, 15);
            }
            else if (GlobalVariables.countyFieldActive) {
                return Database.pricesTopOrBottom(DatabaseConstants.COUNTY, GlobalVariables.currentQuery.toArray(new String[0]),
                        DatabaseConstants.DESCENDING, 15);
            }
            else if (GlobalVariables.districtFieldActive) {
                return Database.pricesTopOrBottom(DatabaseConstants.DISTRICT, GlobalVariables.currentQuery.toArray(new String[0]),
                        DatabaseConstants.DESCENDING, 15);
            }
        }
        else if (GlobalVariables.bottom10FieldActive) {
            if (GlobalVariables.townFieldActive) {
                return Database.pricesTopOrBottom(DatabaseConstants.TOWN, GlobalVariables.currentQuery.toArray(new String[0]),
                        DatabaseConstants.ASCENDING, 15);
            }
            else if (GlobalVariables.countyFieldActive) {
                return Database.pricesTopOrBottom(DatabaseConstants.COUNTY, GlobalVariables.currentQuery.toArray(new String[0]),
                        DatabaseConstants.ASCENDING, 15);
            }
            else if (GlobalVariables.districtFieldActive) {
                return Database.pricesTopOrBottom(DatabaseConstants.DISTRICT, GlobalVariables.currentQuery.toArray(new String[0]),
                        DatabaseConstants.ASCENDING, 15);
            }
        }
        else if (GlobalVariables.stdDevFieldActive) {
            if (GlobalVariables.townFieldActive) {
                return Database.standardDeviation(DatabaseConstants.TOWN, GlobalVariables.currentQuery.toArray(new String[0]));
            }
            else if (GlobalVariables.countyFieldActive) {
                return Database.standardDeviation(DatabaseConstants.COUNTY, GlobalVariables.currentQuery.toArray(new String[0]));
            }
            else if (GlobalVariables.districtFieldActive) {
                return Database.standardDeviation(DatabaseConstants.DISTRICT, GlobalVariables.currentQuery.toArray(new String[0]));
            }
        }
        return (new float[0]);
    }

    public float[] generateScatterPlotX (int intervals) {
        if (GlobalVariables.townFieldActive) {
            return Database.freqDistributionXAxis(DatabaseConstants.TOWN, GlobalVariables.currentQuery.toArray(new String[0]), intervals);
        }
        else if (GlobalVariables.countyFieldActive) {
            return Database.freqDistributionXAxis(DatabaseConstants.COUNTY, GlobalVariables.currentQuery.toArray(new String[0]), intervals);
        }
        else if (GlobalVariables.districtFieldActive) {
            return Database.freqDistributionXAxis(DatabaseConstants.DISTRICT, GlobalVariables.currentQuery.toArray(new String[0]), intervals);
        }
        return (new float[0]);
    }

    public void generatePriceList (float[] data) {
        String toReturn;
        String toAddIn = "";
        this.priceListBoxes = new EntryDisplay[data.length];
        if (GlobalVariables.rangeFieldActive)
            toAddIn = " price range = ";
        else if (GlobalVariables.averageFieldActive)
            toAddIn = " average price = ";

        int yAdjust = 0;
        if (!GlobalVariables.bottom10FieldActive && !GlobalVariables.top10FieldActive) {
            for (int i = 0; i < GlobalVariables.currentQuery.size(); i++) {
                toReturn = Integer.toString(i + 1) + ". " + GlobalVariables.currentQuery.get(i) + toAddIn + " £" + data[i] + "\n";
                this.priceListBoxes[i] = new EntryDisplay(toReturn, height / 20, width, ((height / 20) / 4), xPosition, yPosition + yAdjust);
                yAdjust += height / 20 + ((height / 20) / 4);
            }
        } else {
            for (int i = 0; i < data.length; i++) {
                toReturn = Integer.toString(i + 1) + ". " + toAddIn + " £" + data[i] + "\n";
                this.priceListBoxes[i] = new EntryDisplay(toReturn, height / 20, width, ((height / 20) / 4), xPosition, yPosition + yAdjust);
                yAdjust += height / 20 + ((height / 20) / 4);
            }
        }
    }
}