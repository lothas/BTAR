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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.graphics.Bitmap;
import android.opengl.GLUtils;

/**
 * A two-dimensional square for use as a drawn object in OpenGL ES 1.0/1.1.
 */
public class example {

	private Bitmap mBitmap;
	
	private int mTextureId = -1;
	
    private final FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;
    private final FloatBuffer colorListBuffer;
    private final FloatBuffer normalsBuffer;
    private final ShortBuffer drawOrderNormalsBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float squareCoords[] = {
    	 1.000000f, -1.000000f, -1.000000f,
   		 1.000000f, -1.000000f, 1.000000f,
   		 -1.000000f, -1.000000f, 1.000000f,
   		 -1.000000f, -1.000000f, -1.000000f,
   		 1.000000f, 1.000000f, -1.000000f,
   		 0.999999f, 1.000000f, 1.000001f,
   		 -1.000000f, 1.000000f, 1.000000f,
   		 -1.000000f, 1.000000f, -1.000000f
    }; // top right

    private final short drawOrder[] = { 
    		0, 4, 5,
            0, 5, 1,
            1, 5, 6,
            1, 6, 2,
            2, 6, 7,
            2, 7, 3,
            3, 7, 4,
            3, 4, 0,
            4, 7, 6,
            4, 6, 5,
            3, 0, 1,
            3, 1, 2

    }; // order to draw vertices
    float maxColor = 1f;
    float color[] = { 
    		maxColor, 0, 0, maxColor,        // red
            maxColor, maxColor, 0, maxColor, // yellow
            maxColor, maxColor, 0, maxColor, // yellow
            maxColor, 0, 0, maxColor,        // red
            // Back.
            0, maxColor, 0, maxColor,        // green
            0, 0, maxColor, maxColor,        // blue
            0, 0, maxColor, maxColor,        // blue
            0, maxColor, 0, maxColor,
            
            0, maxColor, 0, maxColor,        // green
            0, 0, maxColor, maxColor,        // blue
            0, 0, maxColor, maxColor,        // blue
            0, maxColor, 0, maxColor,
            
            0, maxColor, 0, maxColor,        // green
            0, 0, maxColor, maxColor,        // blue
            0, 0, maxColor, maxColor,        // blue
            0, maxColor, 0, maxColor         // gree
    };
    
    static float normals[] = {
    		0.000000f, 0.000000f, -1.000000f,
    		 -1.000000f, -0.000000f, -0.000000f,
    		 -0.000000f, -0.000000f, 1.000000f,
    		 -0.000001f, 0.000000f, 1.000000f,
    		 1.000000f, -0.000000f, 0.000000f,
    		 1.000000f, 0.000000f, 0.000001f,
    		 0.000000f, 1.000000f, -0.000000f,
    		 -0.000000f, -1.000000f, 0.000000f
    };
    
    private final short drawOrderNormals[] = { 
    		1, 1, 1,
    		 1, 1, 1,
    		 2, 2, 2,
    		 2, 2, 2,
    		 3, 3, 3,
    		 4, 4, 4,
    		 5, 5, 5,
    		 6, 6, 6,
    		 7, 7, 7,
    		 7, 7, 7,
    		 8, 8, 8,
    		 8, 8, 8
    };
    
    static float texture[] = {
    		0.748573f, 0.750412f,
    		 0.749279f, 0.501284f,
    		 0.999110f, 0.501077f,
    		 0.999455f, 0.750380f,
    		 0.250471f, 0.500702f,
    		 0.249682f, 0.749677f,
    		 0.001085f, 0.750380f,
    		 0.001517f, 0.499994f,
    		 0.499422f, 0.500239f,
    		 0.500149f, 0.750166f,
    		 0.748355f, 0.998230f,
    		 0.500193f, 0.998728f,
    		 0.498993f, 0.250415f,
    		 0.748953f, 0.250920f
    };
    
    private final short drawOrdertexture[] = { 
    		1, 2, 3,
    		 1, 3, 4,
    		 5, 6, 7,
    		 5, 7, 8,
    		 9, 10, 5,
    		 10, 6, 5,
    		 2, 1, 9,
    		 1, 10, 9,
    		 1, 11, 10,
    		 11, 12, 10,
    		 2, 9, 13,
    		 2, 13, 14
    };

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public example() {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
        // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);
        
        ByteBuffer cdlb = ByteBuffer.allocateDirect(color.length * 4);
        cdlb.order(ByteOrder.nativeOrder());
        colorListBuffer = cdlb.asFloatBuffer();
        colorListBuffer.put(color);
        colorListBuffer.position(0);
        
        ByteBuffer bbb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
        normals.length * 4);
        bbb.order(ByteOrder.nativeOrder());
        normalsBuffer = bbb.asFloatBuffer();
        normalsBuffer.put(normals);
        normalsBuffer.position(0);
                
        ByteBuffer dlbb = ByteBuffer.allocateDirect(
        // (# of coordinate values * 2 bytes per short)
        drawOrderNormals.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawOrderNormalsBuffer = dlbb.asShortBuffer();
        drawOrderNormalsBuffer.put(drawOrderNormals);
        drawOrderNormalsBuffer.position(0);
        
        
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param gl - The OpenGL ES context in which to draw this shape.
     */
    public void draw(GL10 gl) {
        // Since this shape uses vertex arrays, enable them
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        // draw the shape
        
                
        gl.glVertexPointer( // point to vertex data:
                COORDS_PER_VERTEX,
                GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorListBuffer);
        
        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
        gl.glNormalPointer(3, GL10.GL_FLOAT, normalsBuffer);
        
        
        
        gl.glEnable(gl.GL_DEPTH_TEST);
        
        gl.glDepthFunc(gl.GL_LESS);
        gl.glDrawElements(  // draw shape:
                GL10.GL_TRIANGLES,
                drawOrder.length, GL10.GL_UNSIGNED_SHORT,
                drawListBuffer);
        
        
        
        
        /*gl.glDrawElements(  // draw shape:
                GL10.GL_TRIANGLES,
                drawOrderNormals.length, GL10.GL_UNSIGNED_SHORT,
                drawOrderNormalsBuffer);*/

        // Disable vertex array drawing to avoid
        // conflicts with shapes that don't use it
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
    }
    
    private void loadGLTexture(GL10 gl) { // New function
		// Generate one texture pointer...
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		mTextureId = textures[0];

		// ...and bind it to our array
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureId);

		// Create Nearest Filtered Texture
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
				GL10.GL_LINEAR);

		// Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
				GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
				GL10.GL_REPEAT);

		// Use the Android GLUtils to specify a two-dimensional texture image
		// from our bitmap
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mBitmap, 0);
	}
}