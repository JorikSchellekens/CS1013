package view.zoomer;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.Vector;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.MouseEvent;
import view.zoomer.ZoomPan.ZoomPanDirection;

// *****************************************************************************************
/** Class to allow interactive zooming and panning of the Processing display. This is the 
 *  Processing 3.x implementation that uses Processing 3's event handling model. This should
 *  not be created directly, but instead it will be created at runtime by the <code>ZoomPan</code>
 *  class if it detects Processing 3.x core libraries. Despite this, the class has to remain
 *  public so that it can be registered by Processing's event handling model.
 *  @author Jo Wood and Aidan Slingsby, giCentre, City University London.
 *  @version 3.4.1, 25th February 2016. 
 */ 
// *****************************************************************************************

/* This file is part of giCentre utilities library. gicentre.utils is free software: you can 
 * redistribute it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * gicentre.utils is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this
 * source code (see COPYING.LESSER included with this source code). If not, see 
 * http://www.gnu.org/licenses/.
 */
public class ZoomPan30 implements ZoomPanable
{
	// ---------------------------- Object variables -----------------------------

	private ZoomPanState zoomPanState;                // zoomScale and panOffset is now stored in an instance of zoomPanState
	                                                  // All reporting of the zoom/offset and coordinate transformation in handled by this object
	private PVector zoomStartPosition,oldPosition;
	private double zoomStep;
	private boolean isZooming, isPanning,isMouseCaptured;
	private boolean allowZoomButton, allowPanButton;
	private int mouseMask = 0;
	private Vector<ZoomPanListener> listeners;
	private int zoomMouseButton=PConstants.RIGHT; 	  // Implies pan is the other button
	
	private Rectangle mouseBoundsMask=null; 		  // Zoom/pan bounding box (in screen space) mask for mouse controlled zooming/panning.
	
	double minZoomScaleX=Double.MIN_VALUE;
	double maxZoomScaleX=Double.MAX_VALUE;
	double minZoomScaleY=Double.MIN_VALUE;
	double maxZoomScaleY=Double.MAX_VALUE;
	
	private ZoomPanDirection zoomPanDirection=ZoomPanDirection.ZOOM_PAN_BOTH;
	
	
	// ------------------------------- Constructors ------------------------------- 

	/** Initialises the zooming and panning transformations for the given applet context. 
	 *  Can be used to have independent zooming in multiple windows by creating multiple
	 *  objects each with a different PApplet object.
	 *  @param aContext Applet context in which zooming and panning are to take place. 
	 */
	ZoomPan30(PApplet aContext, Rectangle mouseBounds)
	{
		zoomPanState=new ZoomPanState(aContext,null);
		if (aContext == null)
		{
			System.err.println("Warning: No applet context provided for ZoomPan.");
			return;
		}
		if (mouseBounds == null) {
			System.err.println("Warning, No mouseBounds provided for ZoomPan.");
		}
		allowZoomButton = true;
		allowPanButton = true;
		listeners = new Vector<ZoomPanListener>();
		mouseBoundsMask = mouseBounds;
		reset();
		aContext.registerMethod("mouseEvent", this);
	}

	/** Initialises the zooming and panning transformations for the given applet and graphics contexts. 
	 *  This version of the constructor allows a graphics context separate from the applet to be applied
	 *  so that buffered off-screen drawing can be applied. 
	 *  @param sketchContext Sketch context in which zooming and panning are to take place. 
	 *  @param graphics Graphics context in which to draw.
	 */
	ZoomPan30(PApplet sketchContext, PGraphics graphics, Rectangle mouseBounds)
	{
		zoomPanState=new ZoomPanState(sketchContext, graphics);

		if (sketchContext == null)
		{
			System.err.println("Warning: No applet context provided for ZoomPan.");
			return;
		}
		if (graphics == null)
		{
			System.err.println("Warning: No graphics context provided for ZoomPan.");
			return;
		}
		if (mouseBounds == null) {
			System.err.println("Warning, No mouseBounds provided for ZoomPan.");
		}
		allowZoomButton = true;
		allowPanButton = true;
		listeners = new Vector<ZoomPanListener>();
		mouseBoundsMask = mouseBounds;
		reset();
		
		sketchContext.registerMethod("mouseEvent", this);
	}

	// ------------------------------ Public methods -----------------------------

	/** Performs the zooming/panning transformation. This method should be called in the
	 *  draw() method before any drawing that is to be zoomed or panned. 
	 */
	public void transform()
	{    
		zoomPanState.transform();
	}
	
	/** Performs the zooming/panning transformation in the given graphics context. This version of transform()
	 *  can be used for transforming off-screen buffers that were not provided to the constructor. Can
	 *  be useful when a sketch temporarily creates an off-screen buffer that needs to be zoomed and panned
	 *  in the same way as the main PApplet.
	 */
	public void transform(PGraphics offScreenBuffer)
	{    
		zoomPanState.transform(offScreenBuffer);
	}
	
	/** Resets the display to unzoomed and unpanned position.
	 */
	public void reset()
	{
		zoomPanState.setTransform(new AffineTransform());
		zoomPanState.setInvTransform(new AffineTransform());
		zoomPanState.setZoomScaleX(1);
		zoomPanState.setZoomScaleY(1);
		zoomPanState.setPanOffset(0,0);
		zoomStep    	    	= 1.05;
		isZooming       		= false;
		isPanning       		= false;
		isMouseCaptured 		= false;
		
		//inform listeners that zooming/panning has ended
		for (ZoomPanListener zoomPanListener:listeners){
			zoomPanListener.panEnded();
			zoomPanListener.zoomEnded();
		}
	}

	/** Adds a listener to be informed when some zooming or panning has finished.
	 *  @param zoomPanListener Listener to be informed when some zooming or panning has finished.
	 */
	public void addZoomPanListener(ZoomPanListener zoomPanListener)
	{
		listeners.add(zoomPanListener); 
	}

	/** Removes the given listener from those to be informed when zooming/panning has finished.
	 *  @param zoomPanListener Listener to remove.
	 *  @return True if listener found and removed.
	 */
	public boolean removeZoomPanListener(ZoomPanListener zoomPanListener)
	{
		return listeners.remove(zoomPanListener); 
	}

	/** Sets the key that must be pressed before mouse actions are active. By default, no key
	 *  is needed for the mouse to be active. Specifying a value allows normal mouse actions to
	 *  be intercepted without zooming or panning. To set the mouse mask to no key, specify a 
	 *  mouseMask value of 0. Mouse actions can be disabled entirely by setting the mouseMask
	 *  to a negative value.
	 *  @param mouseMask Keyboard modifier required to activate mouse actions. Valid values are
	 *  <code>CONTROL</code>, <code>SHIFT</code>, <code>ALT</code>, <code>0</code> and <code>-1</code>. 
	 */
	public void setMouseMask(int mouseMask)
	{
		if (mouseMask < 0)
		{
			this.mouseMask = -1;
			return;
		}

		switch (mouseMask)
		{
		/* Pre 2.0 code:
			case PConstants.CONTROL:
				this.mouseMask = InputEvent.CTRL_DOWN_MASK;
				break;
	
			case PConstants.SHIFT:
				this.mouseMask = InputEvent.SHIFT_DOWN_MASK;
				break;
	
			case PConstants.ALT:
				this.mouseMask = InputEvent.ALT_DOWN_MASK;
				break;
		*/
	
			case PConstants.CONTROL:
				this.mouseMask = PConstants.CONTROL;
				break;
	
			case PConstants.SHIFT:
				this.mouseMask = PConstants.SHIFT;
				break;
	
			case PConstants.ALT:
				this.mouseMask = PConstants.ALT;
				break;
			default:
				this.mouseMask = 0;
		}
	}  

	/** Reports the current mouse position in coordinate space. This method should be used
	 *  in preference to <code>mouseX </code>and <code>mouseY</code> if the current display 
	 *  has been zoomed or panned.
	 *  @return Coordinates of current mouse position accounting for any zooming or panning.
	 */
	public PVector getMouseCoord()
	{
		return getDispToCoord(new PVector(zoomPanState.getContext().mouseX,zoomPanState.getContext().mouseY));
	}

	/** Reports the current zoom scale. Can be used for drawing objects that maintain their
	 *  size when zooming.
	 *  @return Current zoom scale. 
	 */
	public double getZoomScale()
	{
		return zoomPanState.getZoomScale();
	}

	
	/** Reports the current zoom scale in X. Can be used for drawing objects that maintain their
	 *  size when zooming.
	 *  @return Current zoom scale. 
	 */
	public double getZoomScaleX()
	{
		return zoomPanState.getZoomScaleX();
	}

	
	/** Reports the current zoom scale in Y. Can be used for drawing objects that maintain their
	 *  size when zooming.
	 *  @return Current zoom scale. 
	 */
	public double getZoomScaleY()
	{
		return zoomPanState.getZoomScaleY();
	}

	/** Sets a new zoom scale. Can be used for programmatic control of zoomer, such as
	 *  eased interpolated zooming.
	 *  @param zoomScale New zoom scale. A value of 1 indicates no zooming, values above
	 *         0 and below 1 will shrink the display; values above 1 will enlarge the 
	 *         display. Values less than or equal to 0 will be ignored. 
	 */
	public void setZoomScale(double zoomScale)
	{
		setZoomScaleWithoutRecalculation(zoomScale,zoomScale);
		calcTransformation();
	}


	/** Sets a new zoom scale for X and Y. Can be used for programmatic control of zoomer, such as
	 *  eased interpolated zooming.
	 *  @param zoomScaleX New horizontal zoom scale. A value of 1 indicates no zooming, values above
	 *         0 and below 1 will shrink the display; values above 1 will enlarge the 
	 *         display. Values less than or equal to 0 will be ignored.
	 *  @param zoomScaleY New vertical zoom scale. A value of 1 indicates no zooming, values above
	 *         0 and below 1 will shrink the display; values above 1 will enlarge the 
	 *         display. Values less than or equal to 0 will be ignored.  
	 */
	public void setZoomScale(double zoomScaleX,double zoomScaleY)
	{
		setZoomScaleWithoutRecalculation(zoomScaleX,zoomScaleY);
		calcTransformation();
	}

	
	/** Sets a new zoom scale in X. Can be used for programmatic control of zoomer, such as
	 *  eased interpolated zooming.
	 *  @param zoomScaleX New horizontal zoom scale. A value of 1 indicates no zooming, values above
	 *         0 and below 1 will shrink the display; values above 1 will enlarge the 
	 *         display. Values less than or equal to 0 will be ignored. 
	 */
	public void setZoomScaleX(double zoomScaleX)
	{
		setZoomScaleWithoutRecalculation(zoomScaleX,zoomPanState.getZoomScaleY());
		calcTransformation();
	}

	
	/** Sets a new zoom scale. Can be used for programmatic control of zoomer, such as
	 *  eased interpolated zooming.
	 *  @param zoomScaleY New vertical zoom scale. A value of 1 indicates no zooming, values above
	 *         0 and below 1 will shrink the display; values above 1 will enlarge the 
	 *         display. Values less than or equal to 0 will be ignored. 
	 */
	public void setZoomScaleY(double zoomScaleY)
	{
		setZoomScaleWithoutRecalculation(zoomPanState.getZoomScaleX(),zoomScaleY);
		calcTransformation();
	}

	/** Sets the zooming/panning direction
	 *  @param zoomPanDirection Direction of available zoom-pan transformations. 
	 */
	public void setZoomPanDirection(ZoomPanDirection zoomPanDirection){
		this.zoomPanDirection=zoomPanDirection;
	}

	/** Reports the zoom/zan behaviour type.
	 *  @return Reports the available zoom/pan options.
	 */
	public ZoomPanDirection getZoomPanDirection()
	{
		return zoomPanDirection;
	}
	
	/** Reports the current pan offset. Useful when wishing to use an interpolated panning
	 *  between this current value and some new pan offset.
	 *  @return Current pan offset. Negative coordinates indicate an offset to the left
	 *          or upwards, positive values to the right or downward.       
	 */
	public PVector getPanOffset()
	{
		return zoomPanState.getPanOffset();
	}

	
	/** Sets a new pan offset. Can be used for programmatic control of panning, such as
	 *  eased interpolated zooming and panning.
	 *  @param panX X coordinate of new pan offset. A value of 0 indicates no translation
	 *         of the display on the horizontal axis; a negative value indicates a 
	 *         translation to the left; a positive value indicates translation to the right.
	 *  @param panY Y coordinate of new pan offset. A value of 0 indicates no translation
	 *         of the display on the vertical axis; a negative value indicates a translation
	 *         upwards; a positive value indicates translation downwards.
	 *         
	 */
	public void setPanOffset(float panX, float panY)
	{
		zoomPanState.setPanOffset(panX, panY);
		calcTransformation();
	}

	/** Reports whether display is currently being zoomed (i.e. mouse is being dragged with 
	 *  zoom key/button pressed).
	 *  @return True if display is being actively zoomed. 
	 */
	public boolean isZooming()
	{
		return isZooming;
	}

	/** Reports whether display is currently being panned (ie mouse is being dragged with
	 *  pan key/button pressed).
	 *  @return True if display is being actively panned. 
	 */
	public boolean isPanning()
	{
		return isPanning;
	}

	/** Reports whether a mouse event has been captured by the zoomer. This allows zoom and 
	 *  pan events to be separated from other mouse actions. Usually only useful if the zoomer
	 *  uses some mouse mask.
	 *  @return True if mouse event has been captured by the zoomer. 
	 */
	public boolean isMouseCaptured()
	{
		return isMouseCaptured;
	}
	
	/** Determines whether or not zooming via a button press is permitted. By default zooming is 
	 *  enabled with the appropriate mouse button, but can be disabled by setting allowZoom to false.
	 *  Note that the scroll wheel will zoom whether or not the zoom button is activated.
	 *  @param allowZoom Zooming permitted via mouse button press if true.
	 */
	public void allowZoomButton(boolean allowZoom)
	{
		this.allowZoomButton = allowZoom;
	}
	
	/** Determines whether or not panning is permitted via a button press. By default panning is
	 *  enabled with the appropriate mouse button, but can be disabled by setting allowPan to false.
	 *  @param allowPan Panning permitted via mouse button press if true.
	 */
	public void allowPanButton(boolean allowPan)
	{
		this.allowPanButton = allowPan;
	}

	/** Updates zoom and pan transformation according to mouse activity.
	 *  @param e Mouse event.
	 */	
	public void mouseEvent(MouseEvent e)
	{   
		if (mouseMask == -1)
		{
			// If mouse has been disabled with a negative mouse mask, don't do anything.
			return;
		}
				
		if (e.getAction() == MouseEvent.RELEASE)
		{
			// Regardless of mouse mask, if the mouse is released, 
			// that is the end of the zooming and panning.

			boolean isZoomEnded = false;
			boolean isPanEnded = false;

			if (isZooming)
			{
				isZooming = false;
				isZoomEnded = true;
			}

			if (isPanning)
			{
				isPanning = false;
				isPanEnded = true;
			}

			zoomStep = 1.05;
			isMouseCaptured = false;

			// Inform all listeners that some zooming or panning has just finished.
			for (ZoomPanListener listener: listeners)
			{
				if (isZoomEnded)
				{
					listener.zoomEnded();
				}
				if (isPanEnded)
				{
					listener.panEnded();
				}
			}
		}

		// The remaining events only apply if the mouse mask is specified and it is pressed.

		if ((mouseMask < 0) || ((mouseMask == PConstants.SHIFT)   && !e.isShiftDown())
				 			|| ((mouseMask == PConstants.CONTROL) && !e.isControlDown())
				 			|| ((mouseMask == PConstants.ALT)     && !e.isAltDown()))
		{
			return;
		}

		// Only interpret the mousepressed event if the mouse is within mouseBoundsMask (or there's no mouseBoundsMask)
		if ((e.getAction() == MouseEvent.PRESS)	&& 
			((mouseBoundsMask==null) || (mouseBoundsMask.contains(zoomPanState.getContext().mouseX, zoomPanState.getContext().mouseY))))
		{
			isMouseCaptured   = true;

			zoomStartPosition = new PVector(e.getX(), e.getY());
			oldPosition = new PVector(e.getX(), e.getY());
		}
		// Dragging is allowed outside the mouseBoundsMask // Not anymore.
		else if (e.getAction() == MouseEvent.DRAG && ((mouseBoundsMask==null) || (mouseBoundsMask.contains(zoomPanState.getContext().mouseX, zoomPanState.getContext().mouseY))))
		{			
			// Check in case applet has been destroyed.
			if ((zoomPanState.getContext() == null) || (oldPosition == null))
			{
				return;
			}

			if ((zoomPanState.getContext().mouseButton==zoomMouseButton) && (allowZoomButton) && isMouseCaptured)
			{
				isZooming = true;

				if (zoomPanState.getContext().mouseY < oldPosition.y)
				{
					if (zoomPanDirection==ZoomPanDirection.ZOOM_PAN_BOTH)
						setZoomScaleWithoutRecalculation(zoomPanState.getZoomScaleX()*zoomStep,zoomPanState.getZoomScaleY()*zoomStep);
					else if (zoomPanDirection==ZoomPanDirection.ZOOM_PAN_HORIZONTAL||zoomPanDirection==ZoomPanDirection.ZOOM_HORIZONTAL_PAN_BOTH)
						setZoomScaleWithoutRecalculation(zoomPanState.getZoomScaleX()*zoomStep,zoomPanState.getZoomScaleY());
					else if (zoomPanDirection==ZoomPanDirection.ZOOM_PAN_VERTICAL||zoomPanDirection==ZoomPanDirection.ZOOM_VERTICAL_PAN_BOTH)
						setZoomScaleWithoutRecalculation(zoomPanState.getZoomScaleX(),zoomPanState.getZoomScaleY()*zoomStep);
				}
				else if (zoomPanState.getContext().mouseY > oldPosition.y)
				{
					if (zoomPanDirection==ZoomPanDirection.ZOOM_PAN_BOTH)
						setZoomScaleWithoutRecalculation(zoomPanState.getZoomScaleX()/zoomStep,zoomPanState.getZoomScaleY()/zoomStep);
					else if (zoomPanDirection==ZoomPanDirection.ZOOM_PAN_HORIZONTAL||zoomPanDirection==ZoomPanDirection.ZOOM_HORIZONTAL_PAN_BOTH)
						setZoomScaleWithoutRecalculation(zoomPanState.getZoomScaleX()/zoomStep,zoomPanState.getZoomScaleY());
					else if (zoomPanDirection==ZoomPanDirection.ZOOM_PAN_VERTICAL||zoomPanDirection==ZoomPanDirection.ZOOM_VERTICAL_PAN_BOTH)
						setZoomScaleWithoutRecalculation(zoomPanState.getZoomScaleX(),zoomPanState.getZoomScaleY()/zoomStep);
				}
				doZoom();
				zoomStep += 0.005;    // Accelerate zooming with prolonged drag.

			}
			else if (allowPanButton && isMouseCaptured)
			{        
				isPanning = true;
				
				if (zoomPanDirection==ZoomPanDirection.ZOOM_PAN_BOTH || zoomPanDirection==ZoomPanDirection.ZOOM_HORIZONTAL_PAN_BOTH  || zoomPanDirection==ZoomPanDirection.ZOOM_VERTICAL_PAN_BOTH)				
					//zoomPanState.panOffset.add(new PVector(e.getX()-oldPosition.x,e.getY()-oldPosition.y));
					zoomPanState.addPanOffset(e.getX()-oldPosition.x,e.getY()-oldPosition.y);
				else if (zoomPanDirection==ZoomPanDirection.ZOOM_PAN_HORIZONTAL)				
					//zoomPanState.panOffset.add(new PVector(e.getX()-oldPosition.x,0));
					zoomPanState.addPanOffset(e.getX()-oldPosition.x,0);
				else if (zoomPanDirection==ZoomPanDirection.ZOOM_PAN_VERTICAL)				
					//zoomPanState.panOffset.add(new PVector(0,e.getY()-oldPosition.y));
					zoomPanState.addPanOffset(0,e.getY()-oldPosition.y);
				
				calcTransformation(); 

			}

			oldPosition = new PVector(e.getX(),e.getY());
		}
		else
		{
			// Mouse wheel event if getCount() is not zero.
			if ((e.getAction() == MouseEvent.WHEEL) && (e.getCount() != 0)  && ((mouseBoundsMask==null) || (mouseBoundsMask.contains(zoomPanState.getContext().mouseX, zoomPanState.getContext().mouseY))))
			{
				isZooming=true;
				setZoomStartPosition(new PVector(e.getX(),e.getY()));

				if (e.getCount() > 0)
				{
					if (zoomPanDirection==ZoomPanDirection.ZOOM_PAN_BOTH)
						setZoomScaleWithoutRecalculation(zoomPanState.getZoomScaleX()*zoomStep,zoomPanState.getZoomScaleY()*zoomStep);
					else if (zoomPanDirection==ZoomPanDirection.ZOOM_HORIZONTAL_PAN_BOTH || zoomPanDirection==ZoomPanDirection.ZOOM_PAN_HORIZONTAL)
						setZoomScaleWithoutRecalculation(zoomPanState.getZoomScaleX()*zoomStep,zoomPanState.getZoomScaleY());
					else if (zoomPanDirection==ZoomPanDirection.ZOOM_VERTICAL_PAN_BOTH || zoomPanDirection==ZoomPanDirection.ZOOM_PAN_VERTICAL)
						setZoomScaleWithoutRecalculation(zoomPanState.getZoomScaleX(),zoomPanState.getZoomScaleY()*zoomStep);
					doZoom();
				}
				else if (e.getCount() < 0)
				{
					if (zoomPanDirection==ZoomPanDirection.ZOOM_PAN_BOTH)
						setZoomScaleWithoutRecalculation(zoomPanState.getZoomScaleX()/zoomStep,zoomPanState.getZoomScaleY()/zoomStep);
					else if (zoomPanDirection==ZoomPanDirection.ZOOM_HORIZONTAL_PAN_BOTH || zoomPanDirection==ZoomPanDirection.ZOOM_PAN_HORIZONTAL)
						setZoomScaleWithoutRecalculation(zoomPanState.getZoomScaleX()/zoomStep,zoomPanState.getZoomScaleY());
					else if (zoomPanDirection==ZoomPanDirection.ZOOM_VERTICAL_PAN_BOTH || zoomPanDirection==ZoomPanDirection.ZOOM_PAN_VERTICAL)
						setZoomScaleWithoutRecalculation(zoomPanState.getZoomScaleX(),zoomPanState.getZoomScaleY()/zoomStep);
					doZoom();
				}  
			}
		}
	}

	/** Sets the minimum permitted zoom scale (i.e. how far zoomed out a view is allowed to be). If the
	 *  current zoom level is smaller than the new minimum, the zoom scale will be set to the new 
	 *  minimum value. A value above zero but less than one means that the view will be smaller than
	 *  its natural size. A value greater than one means the view will be larger than its natural size.
	 *  @param minZoomScale Minimum permitted zoom scale.
	 */
	public void setMinZoomScale(double minZoomScale)
	{
		this.minZoomScaleX=minZoomScale;
		this.minZoomScaleY=minZoomScale;
		
		if (zoomPanState.getZoomScaleX() < minZoomScale)
		{
			setZoomScaleX(minZoomScale);
		}
		if (zoomPanState.getZoomScaleY() < minZoomScale)
		{
			setZoomScaleY(minZoomScale);
		}
	}

	/** Sets the minimum permitted zoom scale in X (i.e. how far zoomed out a view is allowed to be). If the
	 *  current zoom level is smaller than the new minimum, the zoom scale will be set to the new 
	 *  minimum value. A value above zero but less than one means that the view will be smaller than
	 *  its natural size. A value greater than one means the view will be larger than its natural size.
	 *  @param minZoomScaleX Minimum horizontal zoom scale.
	 */
	public void setMinZoomScaleX(double minZoomScaleX)
	{
		this.minZoomScaleX=minZoomScaleX;
		
		if (zoomPanState.getZoomScaleX() < minZoomScaleX)
		{
			setZoomScaleX(minZoomScaleX);
		}
	}

	/** Sets the minimum permitted zoom scale in Y(i.e. how far zoomed out a view is allowed to be). If the
	 *  current zoom level is smaller than the new minimum, the zoom scale will be set to the new 
	 *  minimum value. A value above zero but less than one means that the view will be smaller than
	 *  its natural size. A value greater than one means the view will be larger than its natural size.
	 *  @param minZoomScaleY Minimum vertical zoom scale.
	 */
	public void setMinZoomScaleY(double minZoomScaleY)
	{
		this.minZoomScaleY=minZoomScaleY;
		
		if (zoomPanState.getZoomScaleY() < minZoomScaleY)
		{
			setZoomScaleY(minZoomScaleY);
		}
	}

	/** Sets the maximum permitted zoom scale (i.e. how far zoomed in a view is allowed to be). If the
	 *  current zoom level is larger than the new maximum, the zoom scale will be set to the new 
	 *  maximum value. A value above zero but less than one means that the view will be smaller than
	 *  its natural size. A value greater than one means the view will be larger than its natural size.
	 *  @param maxZoomScale Maximum permitted zoom scale.
	 */
	public void setMaxZoomScale(double maxZoomScale)
	{
		this.maxZoomScaleX=maxZoomScale;
		this.maxZoomScaleY=maxZoomScale;
		
		if (zoomPanState.getZoomScaleX() > maxZoomScale)
		{
			setZoomScaleX(maxZoomScale);
		}
		if (zoomPanState.getZoomScaleY() > maxZoomScale)
		{
			setZoomScaleY(maxZoomScale);
		}
	}

	/** Sets the maximum permitted zoom scale in X (i.e. how far zoomed in a view is allowed to be). If the
	 *  current zoom level is larger than the new maximum, the zoom scale will be set to the new 
	 *  maximum value. A value above zero but less than one means that the view will be smaller than
	 *  its natural size. A value greater than one means the view will be larger than its natural size.
	 *  @param maxZoomScaleX Maximum horizontal zoom scale.
	 */
	public void setMaxZoomScaleX(double maxZoomScaleX)
	{
		this.maxZoomScaleX=maxZoomScaleX;
		
		if (zoomPanState.getZoomScaleX() > maxZoomScaleX)
		{
			setZoomScaleX(maxZoomScaleX);
		}
	}
	
	/** Sets the maximum permitted zoom scale in Y (i.e. how far zoomed in a view is allowed to be). If the
	 *  current zoom level is larger than the new maximum, the zoom scale will be set to the new 
	 *  maximum value. A value above zero but less than one means that the view will be smaller than
	 *  its natural size. A value greater than one means the view will be larger than its natural size.
	 *  @param maxZoomScaleY Maximum vertical zoom scale.
	 */
	public void setMaxZoomScaleY(double maxZoomScaleY)
	{
		this.maxZoomScaleY=maxZoomScaleY;
		
		if (zoomPanState.getZoomScaleY() > maxZoomScaleY)
		{
			setZoomScale(maxZoomScaleY);
		}
	}

	/** Sets the maximum permitted panning offsets. The coordinates provided should be the unzoomed ones.
	 *  So to prevent panning past the 'edge' of the unzoomed display, values would be set to 0. Setting
	 *  values of (10,40) would allow the display to be panned 10 unzoomed pixels to the left or right
	 *  of the unzoomed display area and 40 pixels up or down.
	 *  @param maxX Maximum number of unzoomed pixels by which the display can be panned in the x-direction.
	 *  @param maxY Maximum number of unzoomed pixels by which the display can be panned in the y-direction.
	 */
	public void setMaxPanOffset(float maxX, float maxY)
	{
		zoomPanState.setMaxPanOffset(maxX,maxY);
		calcTransformation();
	}
		
	/** Transforms the given point from display to coordinate space. Display space is that which
	 *  has been subjected to zooming and panning. Coordinate space is the original space into 
	 *  which objects have been placed before zooming and panning. For most drawing operations you
	 *  should not need to use this method. It is available for those operations that do not draw
	 *  directly, but need to know the transformation between coordinate and screen space.
	 *  @param p 2D point in zoomed display space.
	 *  @return Location of point in original coordinate space. 
	 */
	public PVector getDispToCoord(PVector p)
	{
		return zoomPanState.getDispToCoord(p);
	}

	/** Transforms the given point from coordinate to display space. Display space is that which
	 *  has been subjected to zooming and panning. Coordinate space is the original space into 
	 *  which objects have been placed before zooming and panning. For most drawing operations you
	 *  should not need to use this method. It is available for those operations that do not draw
	 *  directly, but need to know the transformation between coordinate and screen space.
	 *  @param p 2D point in original coordinate space.
	 *  @return Location of point in zoomed display space. 
	 */
	public PVector getCoordToDisp(PVector p)
	{
		return zoomPanState.getCoordToDisp(p);
	}

	/** Sets mouse button for zooming. If this is set to either LEFT or RIGHT, the other button (RIGHT or LEFT)
	 *  will be set for panning.
	 *  @param zoomMouseButton Zoom mouse button (must be either PConstants.LEFT or PConstants.RIGHT
	 */
	public void setZoomMouseButton(int zoomMouseButton)
	{
		if (zoomMouseButton==PConstants.LEFT || zoomMouseButton==PConstants.RIGHT)
		{
			this.zoomMouseButton=zoomMouseButton;
		}
		else
		{
			System.err.println("setZoomMouseButton: Parameter must be LEFT, RIGHT or CENTER");
		}
	}
	
	/** Replacement for Processing's <code>text()</code> method for faster and more accurate placement of 
	 *  characters in Java2D mode when a zoomed font is to be displayed. This method is not necessary when
	 *  text is not subject to scaling via zooming, nor is is necessary in <code>P2D</code>, <code>P3D</code>
	 *  or <code>OpenGL</code> modes.
	 *  @param textToDisplay Text to be displayed.
	 *  @param xPos x-position of the the text to display in original unzoomed screen coordinates.
	 *  @param yPos y-position of the the text to display in original unzoomed screen coordinates.
	 */ 
	public void text(String textToDisplay, float xPos, float yPos)
	{
		// Call the static version providing the applet context that was given to the constructor.
		ZoomPan.text(zoomPanState.getContext(),textToDisplay, xPos,yPos);
	}

	/** Provides a copy (cloned snapshot) of the current ZoomPanState.
	 *  You can assume that this will not change its state.
	 *  @return Copy of the current zoomPanState.
	 */
	public ZoomPanState getZoomPanState()
	{
		return (ZoomPanState)zoomPanState.clone();
	}

	// ----------------------------- Private and package methods -----------------------------


	/** Zooms in or out depending on the current values of zoomStartPosition and zoomScale.
	 */
	void doZoom()
	{
		// Find coordinate-space location of first mouse click.
		PVector pCoord = getDispToCoord(new PVector(zoomStartPosition.x,zoomStartPosition.y));

		// Do the zooming transformation.   
		calcTransformation();

		// Find new pixel location of original mouse click location.
		PVector newZoomStartPosition = getCoordToDisp(pCoord);

		// Translate by change in click position.
		//panOffset.setLocation(panOffset.x + zoomStartPosition.x - newZoomStartPosition.x,
		//                    panOffset.y + zoomStartPosition.y - newZoomStartPosition.y);
		//zoomPanState.panOffset.add(new PVector(zoomStartPosition.x-newZoomStartPosition.x,zoomStartPosition.y-newZoomStartPosition.y));
		zoomPanState.addPanOffset(zoomStartPosition.x-newZoomStartPosition.x,zoomStartPosition.y-newZoomStartPosition.y);

		// Finish off transformation by incorporating shifted click position.
		calcTransformation();
	}

	/** Reports the mouse mask being used.
	 *  @return Mouse mask being used to identify zoom/pan control.
	 */
	int getMouseMask()
	{
		// This method is of package-wide scope to allow inner classes to have access to it.
		return mouseMask;
	}

	/** Reports the zoom step being used.
	 *  @return The amount of zooming that occurs when display zoomed by 1 unit.
	 */
	double getZoomStep()
	{
		// This method is of package-wide scope to allow inner classes to have access to it.
		return zoomStep;
	}

	/** Sets the new zoom-scaling programmatically. Unlike the public method setZoomScale()
	 *  this version is for internal use where recalculation of transformations is handled
	 *  elsewhere.
	 *  @param zoomScaleX New horizontal zoom scale to be used.
	 *  @param zoomScaleY New vertical zoom scale to be used.
	 */
	void setZoomScaleWithoutRecalculation(double zoomScaleX,double zoomScaleY)
	{
		// This method is of package-wide scope to allow inner classes to have access to it.
		// Limit zoom to min/max constraints.
		synchronized (this) 
		{
			zoomPanState.setZoomScaleX(zoomScaleX);
			zoomPanState.setZoomScaleY(zoomScaleY);
			zoomPanState.setZoomScaleX(Math.min(zoomPanState.getZoomScaleX(),maxZoomScaleX));
			zoomPanState.setZoomScaleX(Math.max(zoomPanState.getZoomScaleX(),minZoomScaleX));
			zoomPanState.setZoomScaleY(Math.min(zoomPanState.getZoomScaleY(),maxZoomScaleY));
			zoomPanState.setZoomScaleY(Math.max(zoomPanState.getZoomScaleY(),minZoomScaleY));
		}
	}

	/** Programmatically sets the start position of a zooming activity. Normally, while the mouse
	 *  is held down on a given point, all zooming is relative to this position. This gets reset
	 *  whenever a new point is selected with the mouse. This method allows that position to be
	 *  set programmatically, for example for use with a mouse wheel zooming without a mouse press. 
	 *  @param zoomStartPosition Position in screen coordinates of the start of a zoom activity.
	 */
	void setZoomStartPosition(PVector zoomStartPosition)
	{
		this.zoomStartPosition = zoomStartPosition;
	}

	/** Finds the affine transformations that converts between original and display coordinates. 
	 *  Updates both the forward transformation (for display) and inverse transformation (for 
	 *  decoding of mouse locations. 
	 */
	private void calcTransformation()
	{    
		double centreX = (zoomPanState.getGraphics().width*(1-zoomPanState.getZoomScaleX()))/2;
		double centreY = (zoomPanState.getGraphics().height*(1-zoomPanState.getZoomScaleY()))/2;

		zoomPanState.setTransform(new AffineTransform());
		zoomPanState.setInvTransform(new AffineTransform());

		//scale depending on the type
		if (zoomPanDirection == ZoomPanDirection.ZOOM_PAN_BOTH)
		{
			PVector panOffset = zoomPanState.getPanOffset();
			
			zoomPanState.getTransform().translate(centreX+panOffset.x,centreY+panOffset.y);
			zoomPanState.getTransform().scale(zoomPanState.getZoomScaleX(),zoomPanState.getZoomScaleY());
			zoomPanState.getInvTransform().scale(1/zoomPanState.getZoomScaleX(),1/zoomPanState.getZoomScaleY());
			zoomPanState.getInvTransform().translate(-centreX-panOffset.x, -centreY-panOffset.y);
		}
		else if (zoomPanDirection == ZoomPanDirection.ZOOM_PAN_VERTICAL)
		{
			PVector panOffset = zoomPanState.getPanOffset();
			
			zoomPanState.getTransform().translate(0,centreY+panOffset.y);
			zoomPanState.getTransform().scale(1,zoomPanState.getZoomScaleY());
			zoomPanState.getInvTransform().scale(1,1/zoomPanState.getZoomScaleY());
			zoomPanState.getInvTransform().translate(0, -centreY-panOffset.y);
		}
		else if (zoomPanDirection == ZoomPanDirection.ZOOM_PAN_HORIZONTAL)
		{
			PVector panOffset = zoomPanState.getPanOffset();
			
			zoomPanState.getTransform().translate(centreX+panOffset.x,0);
			zoomPanState.getTransform().scale(zoomPanState.getZoomScaleX(),1);
			zoomPanState.getInvTransform().scale(1/zoomPanState.getZoomScaleX(),1);
			zoomPanState.getInvTransform().translate(-centreX-panOffset.x,0);
		}
	}
	
	/** Reports the name of the given mouse action. Used for debugging only.
	 *  @param mouseAction Action to describe.
	 *  @return Text describing the given mouse action.
	 */
	static String getActionName(int mouseAction)
	{
		switch(mouseAction)
		{
			case MouseEvent.PRESS:
				return "PRESS ("+mouseAction+")";
			case MouseEvent.RELEASE:
				return "RELEASE ("+mouseAction+")";
			case MouseEvent.CLICK:
				return "CLICK ("+mouseAction+")";
			case MouseEvent.DRAG:
				return "DRAG ("+mouseAction+")";
			case MouseEvent.MOVE:
				return "MOVE ("+mouseAction+")";
			case MouseEvent.ENTER:
				return "ENTER ("+mouseAction+")";
			case MouseEvent.EXIT:
				return "WHEEL ("+mouseAction+")";
			default:
				return "Unknown mouse action: "+mouseAction;
		}
	}
}