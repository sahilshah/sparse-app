package com.sahils.sparsereconstruction;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

public class SparseRenderer implements GLSurfaceView.Renderer{
	private float[] mModelMatrix = new float[16];
	private float[] mViewMatrix = new float[16];
	private float[] mProjectionMatrix = new float[16];
	private float[] mMVPMatrix = new float[16];

	private final FloatBuffer mModel;
	private int mMVPMatrixHandle;
	private int mPositionHandle;
	private int mColorHandle;
	private final int mBytesPerFloat = 4;
	private final int mStrideBytes = 7 * mBytesPerFloat;
	private final int mPositionOffset = 0;

	private final int mPositionDataSize = 3;
	private final int mColorOffset = 3;
	private final int mColorDataSize = 4;

	private float meanX,meanY,meanZ;
	
	public SparseRenderer(JSONObject recon_json) {

		List<Float> my3dModelData = new ArrayList<Float>();
		try {
			JSONObject x = recon_json.getJSONObject("points");
			Iterator<?> keys = x.keys();
			int i = 0;
			float xp=0.0f,yp=0.0f,zp=0.0f;
			meanX = meanY = meanZ = 0.0f;
			while (keys.hasNext()) {
//				Log.i("JSON_PARSE", "Found key" + Integer.toString(i));
				String key = (String) keys.next();
				if (x.get(key) instanceof JSONObject) {
					i++;
					JSONObject pnt = (JSONObject) x.get(key);
					JSONArray c_arr = (JSONArray) pnt.get("color");
					JSONArray p_arr = (JSONArray) pnt.get("coordinates");
					xp = (float) p_arr.getDouble(0);
					yp = (float) p_arr.getDouble(1);
					zp = (float) p_arr.getDouble(2);
					if(Math.abs(xp) > 10 || Math.abs(yp) > 10 || Math.abs(zp) > 10){
						Log.i("BIG NUM",String.format("%d %f %f %f", i,xp,yp,zp));
						i--;
					}
					else{
						meanX += xp;
						meanY += yp;
						meanZ += zp;
					}
					my3dModelData.add(xp);
					my3dModelData.add(yp);
					my3dModelData.add(zp);
					my3dModelData.add((float) c_arr.getDouble(0)/255.0f);
					my3dModelData.add((float) c_arr.getDouble(1)/255.0f);
					my3dModelData.add((float) c_arr.getDouble(2)/255.0f);
					my3dModelData.add(1.0f);
					
				}
			}
			meanX /= (float)(i+1);
			meanY /= (float)(i+1);
			meanZ /= (float)(i+1);
			Log.i("JSON", String.format("Centroid is %f %f %f",meanX,meanY,meanZ));
		} catch (JSONException e) {
			Log.i("JSON_PARSE", "Parse error in Renderer");
		}

//		Log.i("DEBUG", "Array is: " + my3dModelData.toString());

		float[] temp_fa = new float[my3dModelData.size()];
		int i = 0;
		for (Float f : my3dModelData) {
			temp_fa[i++] = (f != null ? f : 0.0f); // Or whatever default you
													// want.
		}

		// Initialize the buffers.
		mModel = ByteBuffer.allocateDirect(temp_fa.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mModel.put(temp_fa).position(0);
		Log.i("DEBUG", "Size is: " + mModel.capacity());
	}

	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
		// Set the background clear color to gray.
		GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);

		// Position the eye behind the origin.
		final float eyeX = 0.0f;
		final float eyeY = 0.0f;
		final float eyeZ = 10.0f;

		// We are looking toward the distance
		final float lookX = 3.0f;
		final float lookY = -8.0f;
		final float lookZ = -5.0f;

		// Set our up vector. This is where our head would be pointing were we
		// holding the camera.
		final float upX = 0.0f;
		final float upY = 1.0f;
		final float upZ = 0.0f;

		Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY,
				lookZ, upX, upY, upZ);

		final String vertexShader = "uniform mat4 u_MVPMatrix;      \n" // A
																		// constant
																		// representing
																		// the
																		// combined
																		// model/view/projection
																		// matrix.
				+ "attribute vec4 a_Position;     \n" // Per-vertex position
														// information we will
														// pass in.
				+ "attribute vec4 a_Color;        \n" // Per-vertex color
														// information we will
														// pass in.

				+ "varying vec4 v_Color;          \n" // This will be passed
														// into the fragment
														// shader.

				+ "void main()                    \n" // The entry point for our
														// vertex shader.
				+ "{                              \n"
				+ "   v_Color = a_Color;          \n" // Pass the color through
														// to the fragment
														// shader.
														// It will be
														// interpolated across
														// the triangle.
				+ "   gl_PointSize = 15.0;  \n"
				+ "   gl_Position = u_MVPMatrix   \n" // gl_Position is a
														// special variable used
														// to store the final
														// position.
				+ "               * a_Position;   \n" // Multiply the vertex by
														// the matrix to get the
														// final point in
				+ "}                              \n"; // normalized screen
														// coordinates.

		final String fragmentShader = "precision mediump float;       \n" // Set
																			// the
																			// default
																			// precision
																			// to
																			// medium.
																			// We
																			// don't
																			// need
																			// as
																			// high
																			// of
																			// a
																			// precision
																			// in
																			// the
																			// fragment
																			// shader.
				+ "varying vec4 v_Color;          \n" // This is the color from
														// the vertex shader
														// interpolated across
														// the
														// triangle per
														// fragment.
				+ "void main()                    \n" // The entry point for our
														// fragment shader.
				+ "{                              \n"
				+ "   gl_FragColor = v_Color;     \n" // Pass the color directly
														// through the pipeline.
				+ "}                              \n";

		// Load in the vertex shader.
		int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

		if (vertexShaderHandle != 0) {
			// Pass in the shader source.
			GLES20.glShaderSource(vertexShaderHandle, vertexShader);

			// Compile the shader.
			GLES20.glCompileShader(vertexShaderHandle);

			// Get the compilation status.
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS,
					compileStatus, 0);

			// If the compilation failed, delete the shader.
			if (compileStatus[0] == 0) {
				GLES20.glDeleteShader(vertexShaderHandle);
				vertexShaderHandle = 0;
			}
		}

		if (vertexShaderHandle == 0) {
			throw new RuntimeException("Error creating vertex shader.");
		}

		// Load in the fragment shader shader.
		int fragmentShaderHandle = GLES20
				.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

		if (fragmentShaderHandle != 0) {
			// Pass in the shader source.
			GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);

			// Compile the shader.
			GLES20.glCompileShader(fragmentShaderHandle);

			// Get the compilation status.
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(fragmentShaderHandle,
					GLES20.GL_COMPILE_STATUS, compileStatus, 0);

			// If the compilation failed, delete the shader.
			if (compileStatus[0] == 0) {
				GLES20.glDeleteShader(fragmentShaderHandle);
				fragmentShaderHandle = 0;
			}
		}

		if (fragmentShaderHandle == 0) {
			throw new RuntimeException("Error creating fragment shader.");
		}

		// Create a program object and store the handle to it.
		int programHandle = GLES20.glCreateProgram();

		if (programHandle != 0) {
			// Bind the vertex shader to the program.
			GLES20.glAttachShader(programHandle, vertexShaderHandle);

			// Bind the fragment shader to the program.
			GLES20.glAttachShader(programHandle, fragmentShaderHandle);

			// Bind attributes
			GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
			GLES20.glBindAttribLocation(programHandle, 1, "a_Color");

			// Link the two shaders together into a program.
			GLES20.glLinkProgram(programHandle);

			// Get the link status.
			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS,
					linkStatus, 0);

			// If the link failed, delete the program.
			if (linkStatus[0] == 0) {
				GLES20.glDeleteProgram(programHandle);
				programHandle = 0;
			}
		}

		if (programHandle == 0) {
			throw new RuntimeException("Error creating program.");
		}

		// Set program handles. These will later be used to pass in values to
		// the program.
		mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle,
				"u_MVPMatrix");
		mPositionHandle = GLES20.glGetAttribLocation(programHandle,
				"a_Position");
		mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");

		// Tell OpenGL to use this program when rendering.
		GLES20.glUseProgram(programHandle);
	}

	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) {
		// Set the OpenGL viewport to the same size as the surface.
		GLES20.glViewport(0, 0, width, height);

		// Create a new perspective projection matrix. The height will stay the
		// same
		// while the width will vary as per aspect ratio.
		final float ratio = (float) width / height;
		final float left = -ratio;
		final float right = ratio;
		final float bottom = -1.0f;
		final float top = 1.0f;
		final float near = 1.0f;
		final float far = 10.0f;

		Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near,
				far);
	}

	@Override
	public void onDrawFrame(GL10 glUnused) {
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

		Matrix.setIdentityM(mModelMatrix, 0);
//		Matrix.rotateM(mModelMatrix, 0, mAngleX, 1.0f, 0.0f, 0.0f);
		Matrix.rotateM(mModelMatrix, 0, mAngleY, 0.0f, 1.0f, 0.0f);
		drawPointCloud(mModel);
	}

	private void drawPointCloud(final FloatBuffer aPointCloudBuffer) {
		// Pass in the position information
		aPointCloudBuffer.position(mPositionOffset);
		GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize,
				GLES20.GL_FLOAT, false, mStrideBytes, aPointCloudBuffer);

		GLES20.glEnableVertexAttribArray(mPositionHandle);

		// Pass in the color information
		aPointCloudBuffer.position(mColorOffset);
		GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize,
				GLES20.GL_FLOAT, false, mStrideBytes, aPointCloudBuffer);

		GLES20.glEnableVertexAttribArray(mColorHandle);

		// This multiplies the view matrix by the model matrix, and stores the
		// result in the MVP matrix
		// (which currently contains model * view).
		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

		// This multiplies the modelview matrix by the projection matrix, and
		// stores the result in the MVP matrix
		// (which now contains model * view * projection).
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
		GLES20.glDrawArrays(GLES20.GL_POINTS, 0,mModel.capacity()/7);
	}

	public volatile float mAngleX;
	public volatile float mAngleY;

	public float getAngleX() {
        return mAngleX;
    }

    public void setAngleX(float angle) {
        mAngleX = angle;
    }

    public float getAngleY() {
        return mAngleY;
    }

    public void setAngleY(float angle) {
        mAngleY = angle;
    }

    
    public final void zoom(float mult){
      Matrix.scaleM(mMVPMatrix, 0, mult, mult, mult);
  }
	
}
