import org.gicentre.utils.move.*;
 
// Simple sketch to demonstrate the ZoomPan class for interactively
// zooming and panning a sketch's display.
// Version 1.3, 5th November, 2013
 
ZoomPan zoomer;    // This should be declared outside any methods.
 
void setup()
{
  size(1000, 1000);
  zoomer = new ZoomPan(this);  // Initialise the zoomer.
}
 
void draw()
{
  zoomer.transform();
   
  PVector mousePosition = zoomer.getMouseCoord();
  int mx =int(mousePosition.x);    // Equivalent to mouseX
  int my =int(mousePosition.y);    // Equivalent to mouseY
  
  println("mousex relative to window: " + mouseX+","+mouseY);
  println("Mouse at "+mx+","+my);

  background(218, 205, 192); 
  fill(#000000);
  rect(0,0, 1000,1000);
  fill(#ffffff);
  ellipse(2*width/8, 2*height/8, 50, 50);
  fill(#00ff00);
  ellipse(2*width/8, 6*height/8, 50, 50);
  fill(#0000ff);
  ellipse(6*width/8, 2*height/8, 50, 50);
  fill(#ff0000);
  ellipse(6*width/8, 6*height/8, 50, 50);
   
}