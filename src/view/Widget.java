package view;
import java.util.ArrayList;
import presenter.ScreenController;
import global.*;

import static presenter.ScreenController.screenControllerUI;

public class Widget {

    protected int xPosition, yPosition;
    protected int height;
    protected int width;
    protected boolean canDraw = true;

    public int getxPosition() {
        return xPosition;
    }

    public void setxPosition(int xPosition) {
        this.xPosition = xPosition;
    }

    public int getyPosition() {
        return yPosition;
    }

    public void setyPosition(int yPosition) {
        this.yPosition = yPosition;
    }

    public void setCanDraw(boolean canDraw) {
        this.canDraw = canDraw;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    Widget () {}

    public void draw () {}

    public boolean mouseOver() {
        return (ScreenController.screenControllerUI.mouseX >= xPosition
        && ScreenController.screenControllerUI.mouseX <= xPosition + width
        && ScreenController.screenControllerUI.mouseY >= yPosition
        && ScreenController.screenControllerUI.mouseY <= yPosition + height);
    }

}

class MenuButton extends Widget {

    private String buttonContent;
    private int buttonCase;

    MenuButton (int xPosition, int yPosition,  String buttonContent, int buttonCase, int width, int height) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.buttonContent = buttonContent;
        this.buttonCase = buttonCase;
        this.width = width;
        this.height = height;
    }

    public void draw() {
        if (!isButtonInactive()) {
            if (mouseOver()) {
                screenControllerUI.fill(Colors.PICTON_BLUE.getRGB());
                screenControllerUI.rect(xPosition, yPosition, width, height);
                screenControllerUI.fill(Colors.AZUREISH_WHITE.getRGB());

            } else {
                screenControllerUI.fill(Colors.AZUREISH_WHITE.getRGB());
                screenControllerUI.rect(xPosition, yPosition, width, height);
                screenControllerUI.fill(Colors.PICTON_BLUE.getRGB());
            }
            screenControllerUI.text(buttonContent, xPosition + 20, yPosition + 20);
        }
        else {
            screenControllerUI.fill(Colors.AZUREISH_WHITE.getRGB());
            screenControllerUI.rect(xPosition, yPosition, width, height);
            screenControllerUI.fill(Colors.PICTON_BLUE.getRGB());
            screenControllerUI.text(buttonContent + " function \nis not applicable...", xPosition + 20, yPosition + 20);

        }
    }

    public boolean isButtonInactive () {
        switch (buttonCase) {
            case 0:
                if (GlobalVariables.freqDistActive) {
                    return true;
                }
                break;
            case 1:
                if (GlobalVariables.freqDistActive) {
                    return true;
                }
                break;
            case 2:
                if (GlobalVariables.averageFieldActive || GlobalVariables.top10FieldActive ||
                        GlobalVariables.bottom10FieldActive || GlobalVariables.rangeFieldActive) {
                    return true;
                }
                break;
            case 3:
        }
        return false;
    }
}

class DropdownMenuSelector extends Widget {

    private String label;
    private int width, height;
    DropdownMenuSelector (int xPosition, int yPosition, String label, int width, int height) {
        this.xPosition = xPosition; this.yPosition = yPosition;
        this.label = label;
        this.width = width;
        this.height = height;
    }

    public void draw () {
            screenControllerUI.fill( Colors.PICTON_BLUE.getRGB());
            screenControllerUI.rect(xPosition, yPosition, width, height);
            screenControllerUI.fill(Colors.AZUREISH_WHITE.getRGB());
            screenControllerUI.textSize(18);
            screenControllerUI.text(label, xPosition + 5, yPosition + 20);
            screenControllerUI.textSize(15);
    }
}

class DropDownButton extends Widget {

    private int trigger;
    private String message;
    DropDownButton (int xPosition, int yPosition, String message, int trigger, int width, int height) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.trigger = trigger;
        this.message = message;
        this.width = width;
        this.height = height;
    }

    public void draw () {
        if (canDraw) {
            if (mouseOver()) {
                screenControllerUI.fill(Colors.PICTON_BLUE.getRGB());
                screenControllerUI.rect(xPosition, yPosition, width, height);
                screenControllerUI.fill(Colors.AZUREISH_WHITE.getRGB());
                screenControllerUI.textSize(15);
                screenControllerUI.text(message, xPosition + 5, yPosition + 20);
            } else {
                screenControllerUI.fill(Colors.AZUREISH_WHITE.getRGB());
                screenControllerUI.rect(xPosition, yPosition, width, height);
                screenControllerUI.fill(Colors.PICTON_BLUE.getRGB());
                screenControllerUI.textSize(15);
                screenControllerUI.text(message, xPosition + 5, yPosition + 20);
            }
        }
    }
}

class ClearMapQueryButton extends Widget
{
	protected String text;
	
	ClearMapQueryButton(int xPosition, int yPosition, int width, int height)
	{
		this.xPosition = xPosition;
		this.yPosition = yPosition;
		this.width = width;
		this.height = height;
		this.text = "Clear";
	}
	
	public void draw()
	{
        if (mouseOver()) {
            screenControllerUI.fill( Colors.PICTON_BLUE.getRGB());
            screenControllerUI.rect(xPosition, yPosition, width, height);
            screenControllerUI.fill(Colors.AZUREISH_WHITE.getRGB());
            screenControllerUI.textAlign(screenControllerUI.CENTER,screenControllerUI. CENTER);
            screenControllerUI.text(text, xPosition + (width / 2), yPosition + (height / 2));
            screenControllerUI.textAlign(screenControllerUI.LEFT,screenControllerUI. TOP);
        }
        else {
			screenControllerUI.fill(Colors.AZUREISH_WHITE.getRGB());
	        screenControllerUI.rect(xPosition, yPosition, width, height);
	        screenControllerUI.fill(Colors.PICTON_BLUE.getRGB());
	        screenControllerUI.textAlign(screenControllerUI.CENTER,screenControllerUI. CENTER);
	        screenControllerUI.text(text, xPosition + (width / 2), yPosition + (height / 2));
	        screenControllerUI.textAlign(screenControllerUI.LEFT,screenControllerUI. TOP);
        }

	}
	
	public void notifyMousePressed()
	{
		if (mouseOver())
		{
			GlobalVariables.currentQuery = new ArrayList<>();
			GlobalVariables.queryChanged = true;
		}
	}
}

class ResetMapButton extends ClearMapQueryButton
{
	ResetMapButton(int xPosition, int yPosition, int width, int height)
	{
		super(xPosition, yPosition, width, height);
		super.text = "Reset";
	}
	
	public void notifyMousePressed()
	{
		if (mouseOver())
		{
			GlobalVariables.mapReset = true;
		}
	}
}
