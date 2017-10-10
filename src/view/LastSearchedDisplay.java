package view;

import static presenter.ScreenController.screenControllerUI;

import java.util.ArrayList;
import java.util.List;

import global.Colors;
import processing.core.PConstants;

/**
 * Created by Jorik on 05/04/2017.
 */

class LastSearchedDisplay extends Widget {

  private List<List<EntryDisplay>> entries = new ArrayList<>();
  private int entryDisplayHeight;
  private static final int TEXT_SIZE = 13;
  private static final int TEXT_HEADER_SIZE = 15;
  private static final int HEADER_MARGIN = 15;
  private static final int ENTRY_MARGIN = 3;

  LastSearchedDisplay (int xPosition, int yPosition, int width) {
    this.xPosition = xPosition;
    this.yPosition = yPosition;
    this.width = width;
    this.entryDisplayHeight = TEXT_SIZE + ENTRY_MARGIN;
    this.height = calculateHeight();
  }

  public void draw () {
    screenControllerUI.fill(Colors.PICTON_BLUE.getRGB());
    screenControllerUI.rect(xPosition, yPosition, width, height);
    screenControllerUI.fill(Colors.AZUREISH_WHITE.getRGB());
    screenControllerUI.textSize(TEXT_HEADER_SIZE);
    screenControllerUI.textAlign(PConstants.CENTER, PConstants.CENTER);
    screenControllerUI.text("AREAS SEARCHED", xPosition + width / 2, yPosition + TEXT_HEADER_SIZE / 2 + HEADER_MARGIN);
    drawEntries();
  }

  public void setEntries(List<String> names) {
    this.entries = new ArrayList<>();
    int count = 0;
    for (String name : names) {
      if (!name.isEmpty()) {
        count ++;
        EntryDisplay newDisplay = new EntryDisplay(name, entryDisplayHeight, ENTRY_MARGIN);
        if (entries.size() == 0 || getLineWidth(entries.get(entries.size() - 1)) + newDisplay.getWidth() + ENTRY_MARGIN > width) {
          entries.add(new ArrayList<>());
        }

        entries.get(entries.size() - 1).add(newDisplay);
      }
    }
    this.height = calculateHeight();
  }

  public int getLineWidth(List<EntryDisplay> line) {
      return line.stream().mapToInt(entry -> entry.getWidth()).sum() + (line.size() + 1) * ENTRY_MARGIN;
  }

  private int calculateHeight() {
    return entries.size() * entryDisplayHeight + ENTRY_MARGIN * entries.size() + HEADER_MARGIN * 2 + TEXT_HEADER_SIZE;
  }

  public void drawEntries() {
    int yOffset = yPosition + TEXT_HEADER_SIZE + 2 * HEADER_MARGIN;
    for (List<EntryDisplay> line : entries) {
      int xOffset = xPosition + (width - getLineWidth(line)) / 2;
      for (EntryDisplay entry : line) {
        entry.draw(xOffset, yOffset);
        xOffset += entry.getWidth() + ENTRY_MARGIN;
      }
      yOffset += entryDisplayHeight + ENTRY_MARGIN;
    }
  }

}
