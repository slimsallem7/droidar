package gl;

import gui.CustomGestureListener;
import listeners.EventListener;
import system.EventManager;
import system.TouchEventInterface;
import util.Log;
import actions.Action;
import actions.EventListenerGroup;
import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import commands.Command;

/**
 * This is the custom {@link GLSurfaceView} which is used to render the OpenGL
 * content.
 * 
 * @author Spobo
 * 
 */
public class CustomGLSurfaceView extends GLSurfaceView implements
		TouchEventInterface {

	private static final long TOUCH_INPUT_SLEEP_TIME = 20;

	/**
	 * enables the opengl es debug output but reduces the frame-rate a lot!
	 */
	private static final boolean DEBUG_OUTPUT_ENABLED = false;

	private static final String LOG_TAG = "CustomGLSurfaceView";

	public EventListener onTouchMoveAction;

	private GestureDetector myGestureDetector;

	public CustomGLSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initGLSurfaceView(context);
	}

	public CustomGLSurfaceView(Context context) {
		super(context);
		initGLSurfaceView(context);
	}

	private void initGLSurfaceView(Context context) {
		if (DEBUG_OUTPUT_ENABLED) {
			// Turn on error-checking and logging
			setDebugFlags(DEBUG_CHECK_GL_ERROR | DEBUG_LOG_GL_CALLS);
		}

		this.setFocusableInTouchMode(true);
		myGestureDetector = new GestureDetector(context,
				new CustomGestureListener(this));

		// Set 8888 pixel format because that's required for
		// a translucent window:
		this.setEGLConfigChooser(8, 8, 8, 8, 16, 0);

		// Use a surface format with an Alpha channel:
		this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
	}

	@Override
	public boolean dispatchTrackballEvent(MotionEvent event) {
		if (EventManager.getInstance().onTrackballEvent(event)) {
			return true;
		}
		return super.onTrackballEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		myGestureDetector.onTouchEvent(event);

		requestFocus();

		try {
			// Sleep 20ms to not flood the thread
			Thread.sleep(TOUCH_INPUT_SLEEP_TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (onTouchMoveAction != null) {
				onTouchMoveAction.onReleaseTouchMove();
			}
		}
		return true;
	}

	@Override
	public Command getOnDoubleTabCommand() {
		return null;
	}

	@Override
	public Command getOnLongPressCommand() {
		return null;
	}

	@Override
	public Command getOnTabCommand() {
		return null;
	}

	@Override
	public void onDoubleTap(MotionEvent e) {
		ObjectPicker.getInstance().setDoubleClickPosition(e.getX(), e.getY());
	}

	@Override
	public void onLongPress(MotionEvent e) {
		ObjectPicker.getInstance().setLongClickPosition(e.getX(), e.getY());
	}

	@Override
	public void onSingleTab(MotionEvent e) {
		ObjectPicker.getInstance().setClickPosition(e.getX(), e.getY());
	}

	@Override
	public void setOnDoubleTabCommand(Command c) {
	}

	@Override
	public void setOnLongPressCommand(Command c) {
	}

	@Override
	public void setOnTabCommand(Command c) {
	}

	public void addOnTouchMoveAction(Action action) {
		Log.d("EventManager", "Adding onTouchMoveAction");
		onTouchMoveAction = addActionToTarget(onTouchMoveAction, action);
	}

	/**
	 * TODO change onTouchMoveAction to a List
	 * 
	 * @param target
	 * @param action
	 * @return
	 */
	@Deprecated
	private static EventListener addActionToTarget(EventListener target,
			EventListener action) {
		if (target == null) {
			target = action;
			Log.d(LOG_TAG, "Setting target command to " + action + "");
		} else if (target instanceof EventListenerGroup) {
			((EventListenerGroup) target).add(action);
			Log.d(LOG_TAG, "Adding " + action + " to existing actiongroup.");
		} else {
			EventListenerGroup g = new EventListenerGroup();
			g.add(target);
			g.add(action);
			target = g;
			Log.d(LOG_TAG, "Adding " + action + " to new actiongroup.");
		}
		return target;
	}

	@Override
	public void onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		if (onTouchMoveAction != null) {
			onTouchMoveAction.onTouchMove(e1, e2, distanceX, distanceY);
		}
	}

}
