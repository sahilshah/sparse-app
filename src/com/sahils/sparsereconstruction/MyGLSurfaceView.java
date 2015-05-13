package com.sahils.sparsereconstruction;

import org.json.JSONObject;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MyGLSurfaceView extends GLSurfaceView {

	private final SparseRenderer mRenderer;

	public MyGLSurfaceView(Context context, JSONObject json_obj) {
		super(context);
		
		// Create an OpenGL ES 2.0 context.
		setEGLContextClientVersion(2);

		// Set the Renderer for drawing on the GLSurfaceView
		mRenderer = new SparseRenderer(json_obj);
		setRenderer(mRenderer);

		// Render the view only when there is a change in the drawing data
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}

	private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
	private float mPreviousX;
	private float mPreviousY;

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		// MotionEvent reports input details from the touch screen
		// and other input controls. In this case, you are only
		// interested in events where the touch position changed.

		float x = e.getX();
		float y = e.getY();

		switch (e.getAction()) {
		case MotionEvent.ACTION_MOVE:

			float dx = x - mPreviousX;
			float dy = y - mPreviousY;

			mRenderer.setAngleX(mRenderer.getAngleX()
					+ (dy * TOUCH_SCALE_FACTOR)); // = 180.0f / 320
			mRenderer.setAngleX(mRenderer.getAngleY()
					+ (dx * TOUCH_SCALE_FACTOR)); // = 180.0f / 320
			requestRender();
			break;
		case MotionEvent.ACTION_DOWN:
			mPreviousX = x;
			mPreviousY = y;
			break;
		}

		return true;
	}

}