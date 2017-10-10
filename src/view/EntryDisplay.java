package view;

import presenter.ScreenController;
import processing.core.PConstants;

import static presenter.ScreenController.screenControllerUI;

/**
 * Created by Jorik on 06/04/2017.
 */

public class EntryDisplay extends Widget {
    private String text;
    private int padding;
    private int textSize;

    EntryDisplay(String text, int height, int width, int padding, int x, int y) {
        this.text = text;
        this.height = height;
        this.padding = padding;
        this.width = width;
        this.textSize = height - 2 * padding;
        this.xPosition = x;
        this.yPosition = y;
    }

    EntryDisplay(String text, int height, int padding) {
        this.text = text;
        this.height = height;
        this.padding = padding;
        this.textSize = height - 2 * padding;
        ScreenController.screenControllerUI.textSize(textSize);
        this.width = (int) ScreenController.screenControllerUI.textWidth(text) + 2 * padding;
    }

    // There is a good reason for this.
    public void draw(int x, int y) {
        ScreenController.screenControllerUI.fill(240);
        ScreenController.screenControllerUI.noStroke();
        ScreenController.screenControllerUI.rect(x, y, width, height, padding);
        ScreenController.screenControllerUI.textAlign(PConstants.CORNER, PConstants.TOP);
        ScreenController.screenControllerUI.textSize(textSize);
        screenControllerUI.fill(0);
        ScreenController.screenControllerUI.text(text, x + padding, y + padding);
        ScreenController.screenControllerUI.stroke(0);
    }

    public void draw() {
        draw(xPosition, yPosition);
    }
}