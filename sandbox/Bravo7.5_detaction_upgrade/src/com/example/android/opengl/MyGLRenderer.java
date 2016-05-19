/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.opengl;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.opencv.samples.colorblobdetect.MarkerDetector;

import com.example.bravo.MainActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

/**
 * Provides drawing instructions for a GLSurfaceView object. This class
 * must override the OpenGL ES drawing lifecycle methods:
 * <ul>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceCreated}</li>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onDrawFrame}</li>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceChanged}</li>
 * </ul>
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {
	private static final String  TAG              = "MyGLRenderer";
	//-------------------------Static----------------------------//
    private static final int     NO_COLOR = -1;
	private static final int     BLUE_COLOR = 1;
	private static final int     GREEN_COLOR = 2;
	private static final int     RED_COLOR = 3;
	//-------------------------Fields--------------------------//
    private Triangle mTriangle;
    private Square mSquare;
    private example mExample;
    private float mAngle;
    
    private float objX;
    private float objY;
    
    private int screenWidth;
    private int screenHeight;
    
    private  MarkerDetector blueRedDetector;
    
    SensorManager sensorManager;
	int orientationSensor;
	float headingAngle;
	float pitchAngle;
	float rollAngle;
  //-------------------------Activity------------------------//
  	MainActivity                 mMainActivity;	
  	
  	//-------------------------Methods-------------------------//
    
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Set the background frame color
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        mTriangle = new Triangle();
        mSquare = new Square();
        mExample = new example();
        
        screenWidth = 1000;
        screenHeight = 1000;
        
        objX = -0.5f; 
        objY = 0.8f;
        
        blueRedDetector = new MarkerDetector();
        blueRedDetector.prepareGame(screenWidth, screenHeight, BLUE_COLOR, RED_COLOR);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
    	// Clears the screen and depth buffer.
    	gl.glClearColor(0,0,0,0);
    	gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
    	// Replace the current matrix with the identity matrix
    	gl.glViewport(0, 0, screenWidth, screenWidth);
    	//gl.glViewport(0, 0, screenWidth, screenHeight);
    	gl.glLoadIdentity();
    	
    	
        
    	
    	runOrientationSensor(); //seting headingAngle pitchAngle rollAngle. //TODO only need to declre once
    	
    	// SQUARE A
    	drawObj(gl, blueRedDetector);
    	// DRAW GTID
    	/*drawGrid(gl,1,1,false);
    	drawGrid(gl,1,-1,false);
    	drawGrid(gl,0,1,false);
    	drawGrid(gl,1,0,false);
    	drawGrid(gl,0,0,false);
    	drawGrid(gl,-1,0,false);
    	drawGrid(gl,0,-1,false);
    	drawGrid(gl,-1,1,false);
    	drawGrid(gl,-1,-1,false);*/
    }

    
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // Adjust the viewport based on geometry changes
        // such as screen rotations
        gl.glViewport(0, 0, width, height);

        // make adjustments for screen ratio
        float ratio = (float) width / height;
        gl.glMatrixMode(GL10.GL_PROJECTION);        // set matrix to projection mode
        gl.glLoadIdentity();                        // reset the matrix to its default state
        gl.glFrustumf(-ratio, ratio, -1, 1, 3, 7);  // apply the projection matrix
    
    }

    /**
     * Returns the rotation angle of the triangle shape (mTriangle).
     *
     * @return - A float representing the rotation angle.
     */
    public float getAngle() {
        return mAngle;
    }

    /**
     * Sets the rotation angle of the triangle shape (mTriangle).
     */
    public void setAngle(float angle) {
        mAngle = angle;
    }
    
    public void setScreenWidthHeight(int width, int height){
    	screenWidth = width; 
    	screenHeight = height;
    }
    
    public void setCordinates(float x, float y){
   	 	objX = x; 
        objY = y;
   }
    
    public float pixel2worldX(int x){
    	return 2*((float)x) / screenWidth - 1;
    }
    
    public float pixel2worldY(float y){
    	// we want ower world to be scaled 1:1 so points 1 and -1 of the Y axis are out side the screen
    	//that is why we use (screenHeight/ screenWidth) insted of 1
    	float hightWidth = ((float)screenHeight / (float)screenWidth); // the (float) are importent.
    	return y * hightWidth - (1f - hightWidth);  
    }
    
    /*public float pixel2worldYscaled(int y){
    	return 1 - 2*((float)y) / screenHeight;
    }*/
    
    public float pixel2worldYscaled(int y){// need to add later
    	float tYemp = 1 - 2*((float)y) / screenHeight;
    	return pixel2worldY(tYemp);
    }
    
    public float scaleTrackObjects(MarkerDetector mDetector){
    	Log.i(TAG, "MyGLRenderer: mDetector xF - xB = " + (mDetector.getFrontX() - mDetector.getBackX()));
    	Log.i(TAG, "MyGLRenderer: mDetector yF - yB = " + (mDetector.getFrontY() - mDetector.getBackY()));
    	Log.i(TAG, "MyGLRenderer: mDetector.getFront2BackLength() = " + mDetector.getFront2BackLength());
    	return 5*(mDetector.getFront2BackLength()) / mDetector.getFrameWidth();
    }
    
    public void setTrackObjects(MarkerDetector mDetector){
    	blueRedDetector = mDetector;
    }
    
    public void setMainActivityContext(MainActivity activity){
    	mMainActivity = activity;
    }
    
    public void runOrientationSensor(){
    	sensorManager = (SensorManager) mMainActivity.getSystemService(mMainActivity.SENSOR_SERVICE);
		orientationSensor = Sensor.TYPE_ORIENTATION;
		sensorManager.registerListener(sensorEventListener,sensorManager.getDefaultSensor(orientationSensor),SensorManager.SENSOR_DELAY_NORMAL);
    }
    
    final SensorEventListener sensorEventListener = new SensorEventListener() {
		public void onSensorChanged(SensorEvent sensorEvent) {
			if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
				headingAngle = sensorEvent.values[0];
				pitchAngle = sensorEvent.values[1];
				rollAngle = sensorEvent.values[2];
				Log.d(TAG, "Heading: " + String.valueOf(headingAngle));
				Log.d(TAG, "Pitch: " + String.valueOf(pitchAngle));
				Log.d(TAG, "Roll: " + String.valueOf(rollAngle));
			}
		}

		public void onAccuracyChanged(Sensor senor, int accuracy) {
			// Not used
		}
	};
    
    public void drawObj(GL10 gl, MarkerDetector mDetector){
		if (mDetector.isDetected() == false) {
			return;
		}
		//gl.glViewport(0, 0, screenWidth, screenHeight);
		// Save the current matrix.
		gl.glPushMatrix();
		//gl.glTranslatef(pixel2worldX(mDetector.getMiddleX()), pixel2worldY(mDetector.getMiddleY()), 0);
		gl.glTranslatef(pixel2worldX(mDetector.getMiddleX()), pixel2worldYscaled(mDetector.getMiddleY()), 0);
		Log.i(TAG, "MyGLRenderer: mDetector.getAngleFromXAxis() = " + mDetector.getAngleFromXAxis());
		
		float scale = 0.5f * scaleTrackObjects(mDetector);
		Log.i(TAG, "MyGLRenderer: sacle = " + scale);
		gl.glScalef(scale, scale, scale); //(screenHeight/screenWidth)*
		
		float rotateOnZ = mDetector.getAngleFromXAxis();
		//gl.glRotatef(-rollAngle, 1, 0, 0);
		gl.glRotatef(rotateOnZ, 0, 0, 1);
		gl.glRotatef(rollAngle, (float)Math.cos(Math.toRadians(rotateOnZ)), -(float)Math.sin(Math.toRadians(rotateOnZ)), 0);
		
		
		// Draw mExample A.
		mExample.draw(gl);
		// Restore the last matrix.
		gl.glPopMatrix();
	}
    
    public void drawGrid(GL10 gl,float x, float y, boolean flag){
		// Save the current matrix.
		gl.glPushMatrix();
		//gl.glTranslatef(pixel2worldX(mDetector.getMiddleX()), pixel2worldY(mDetector.getMiddleY()), 0);
		gl.glTranslatef(x, pixel2worldY(y), 0);
		
		float scale = 0.1f;
		Log.i(TAG, "MyGLRenderer: sacle = " + scale);
		gl.glScalef(scale, scale, scale); 
		gl.glRotatef(45,0,0,1);				
		// Draw mExample A.
		//mExample.draw(gl);
		if (flag){
			mTriangle.draw(gl);
		}
		mSquare.draw(gl);
		// Restore the last matrix.
		gl.glPopMatrix();
	}
    
    
    
}