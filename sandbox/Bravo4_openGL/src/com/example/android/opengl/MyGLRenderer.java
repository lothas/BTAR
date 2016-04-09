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
    private float mAngle;
    
    private float objX;
    private float objY;
    
    private int screenWidth;
    private int screenHeight;
    
    private  MarkerDetector blueRedDetector;
    
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Set the background frame color
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        mTriangle = new Triangle();
        mSquare = new Square();
        
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
    	gl.glLoadIdentity();
    	//gl.glViewport(0, 0, screenWidth, screenHeight);
        
    	// SQUARE A
    	// Save the current matrix.
    	drawObj(gl, blueRedDetector);
    	
    	// SQUARE B
    	// Save the current matrix.
    	gl.glPushMatrix();
    	gl.glViewport(0, 0, screenWidth, screenHeight);
    	gl.glTranslatef(1, 1, 0);
    	gl.glRotatef(mAngle, 0, 0, 1);
    	gl.glScalef(0.1f, 0.1f, 0.1f);
    	// Draw square A.
    	mSquare.draw(gl);
    	// Restore the last matrix.
    	gl.glPopMatrix();
    	
    	// SQUARE B
    	// Save the current matrix.
    	gl.glPushMatrix();
    	gl.glViewport(0, 0, screenWidth, screenHeight);
    	gl.glTranslatef(-1, -1, 0);
    	gl.glRotatef(mAngle, 0, 0, 1);
    	gl.glScalef(0.4f, 0.4f, 0.1f);
    	// Draw square A.
    	mSquare.draw(gl);
    	// Restore the last matrix.
    	gl.glPopMatrix();
    	
    	
    }

    @Override
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
    
    public float pixel2worldY(int y){
    	return 1 - 2*((float)y) / screenHeight;
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
    
    public void drawObj(GL10 gl, MarkerDetector mDetector){
    	if (mDetector.isDetected() == true){
	    	gl.glPushMatrix();
	    	gl.glViewport(0, 0, screenWidth, screenHeight);
	    	gl.glTranslatef(pixel2worldX(mDetector.getMiddleX()), pixel2worldY(mDetector.getMiddleY()), 0);
	    	Log.i(TAG, "MyGLRenderer: mDetector.getAngleFromXAxis() = " + mDetector.getAngleFromXAxis());
	    	gl.glRotatef(mDetector.getAngleFromXAxis(), 0, 0, 1);
	    	float scale = scaleTrackObjects(mDetector);
	    	Log.i(TAG, "MyGLRenderer: sacle = " + scale);
	    	gl.glScalef(scale, scale, scale);
	    	// Draw square A.
	    	mSquare.draw(gl);
	    	// Restore the last matrix.
	    	gl.glPopMatrix();
    	}
    }
}