package com.example.android.opengl;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import android.util.Log;

/** Assumes UTF-8 encoding. JDK 7+. */
public class myParser {
	
	private final int NUM_XYZ_ELS;
	private final int NUM_NXYZ_ELS;
	private final int NUM_RGBA_ELS;
	private final int NUM_ST_ELS;
	public Vector<Vector<Float>> v = new Vector<Vector<Float>>();
	private Vector<Integer> vOrder = new Vector<Integer>();
	private Vector<Vector<Float>> vn = new Vector<Vector<Float>>();
	private Vector<Integer> vnOrder = new Vector<Integer>();
	private Map<String, Vector<Float>> tempC = new HashMap<String, Vector<Float>>();
	private String currentObj;
	private Vector<Vector<Float>> c = new Vector<Vector<Float>>();
	private Vector<Vector<Float>> vt = new Vector<Vector<Float>>();
	private Vector<Integer> vtOrder = new Vector<Integer>();
	public Vector<Vector<Float>> interleavedVec = new Vector<Vector<Float>>();
	public float[] vertices;
	public short[] indices;
	public byte[] colors;
	public float[] normals;
	public byte[] normIndices;
	public float[] texture;
	public byte[] texIndices;
	public float[] interleavedArray;
	private final InputStream objFile;
	private final InputStream mtlFile;
	private int m_NumVertices = 0;
    private final int size;
    private FloatBuffer mFVertexBuffer;
    private ByteBuffer mColorBuffer;
    private ShortBuffer mIndexBuffer;
    private FloatBuffer mNormalBuffer;
    private ByteBuffer mNormIndexBuffer;
    private int vertexNum = 0;
    private Boolean objFlag = false;
    private Boolean objFlagPrev = false;

	public myParser(InputStream aObjFile, InputStream aMtlFile, int vert, int norm, int color, int tex){
		objFile = aObjFile;
		mtlFile = aMtlFile;
		NUM_XYZ_ELS = vert;
		NUM_NXYZ_ELS = norm;
		NUM_RGBA_ELS = color;
		NUM_ST_ELS = tex;
		size = NUM_XYZ_ELS+NUM_NXYZ_ELS+NUM_RGBA_ELS+NUM_ST_ELS;
	}

	public void run(){
		processLineByLine();
		makeCombVec();
		translateVec();
		createBuffer();
		m_NumVertices = indices.length;
	}
	
	public final void processLineByLine(){
		Scanner scanner =  new Scanner(mtlFile);
		while (scanner.hasNextLine()){
			processMtl(scanner.nextLine());
		}
		scanner.close();
		scanner =  new Scanner(objFile);
		while (scanner.hasNextLine()){
			processLine(scanner.nextLine());
		}
		if(objFlagPrev == true){
			spreadColor(vertexNum);
		}
		scanner.close();
		
	}
	

	protected void processLine(String aLine){
    //use a second Scanner to parse the content of each line 
	  	Scanner scanner = new Scanner(aLine);
	    if (scanner.hasNext()){
		      //assumes the line has a certain structure
		    String name = scanner.next();
	    	objFlagPrev = objFlag;
	    	if (name.equals("f")) {
	    		objFlag = true;
	    		addVrtice(scanner.next());
	    		addVrtice(scanner.next());
	    		addVrtice(scanner.next());
	    	}else{
	    		objFlag = false;
	    		if(objFlagPrev == true){
	    			spreadColor(vertexNum);
	    			vertexNum = 0;
	    		}
	    	}
		    if(name.equals("v")) {
	        	v.add(floatVec(scanner));
	        	vertexNum++;
		    }else if (name.equals("vn")) {
	        	vn.add(floatVec(scanner));
			}else if (name.equals("vt")) {
	        	vt.add(floatVec(scanner));
			}else if(name.equals("usemtl")){
				if(scanner.hasNext()){
					currentObj = scanner.next();					
				}else{
					currentObj = null;
				}
			}
	    }	 
	}
	
	protected void processMtl(String aLine){
		  	Scanner scanner = new Scanner(aLine);
		    if (scanner.hasNext()){
			    String name = scanner.next();
			    if(name.equals("newmtl")){
			    	currentObj = scanner.next();
			    }
		    	if (name.equals("Kd")) {
		    		Vector<Float> temp = new Vector<Float>();
		    		while(scanner.hasNextFloat()){
		    			temp.add(255*scanner.nextFloat());		    			
		    		}
		    		tempC.put(currentObj,temp);
		    	}
		    }
		    scanner.close();
		}
	
	public void spreadColor(int amount){
		Vector<Float> temp = new Vector<Float>();
		temp = tempC.get(currentObj);
		temp.add(255f);
		for (int i=0; i< amount; i++){
			c.add(temp);
		}
	}
	
	public void addVrtice(String values){
		Scanner scanner = new Scanner(values);
		scanner.useDelimiter("/");
		int j=0;
		while (scanner.hasNext()) {
			try{
				int foo = Integer.parseInt(scanner.next())-1;
				int i=0;
				switch(j) {
			        case 0: vOrder.add(foo);
			        	break;
			        case 1: vtOrder.add(foo);
			        	break;
			        case 2: vnOrder.add(foo);
			        	break;
			        default: ;
			        	break;
				    }
			}catch(Exception e){ 
        		//Log.v("hi", e.getMessage());
			}
			j++;
		}
    	scanner.close();
	}
	
	public void makeCombVec(){
		Iterator<Integer> vIt = vOrder.iterator();
		Iterator<Integer> vnIt = vnOrder.iterator();
		Iterator<Integer> vtIt = vtOrder.iterator();
		Vector<Float> temp = new Vector<Float>();
		int i=0;
		while(vIt.hasNext()){
			int vInd = vIt.next();
			temp.addAll(v.elementAt(vInd));
			if(NUM_NXYZ_ELS > 0){
				i++;
				int vnInd = vnIt.next();
				temp.addAll(vn.elementAt(vnInd));
			}
			if(NUM_RGBA_ELS > 0){
				temp.addAll(c.elementAt(vInd));
			}
			if(NUM_ST_ELS > 0){
				int vtInd = vtIt.next();
				temp.addAll(vt.elementAt(vtInd));
			}
		}
		interleavedVec.add(temp);
	}
	
	
	public Vector<Float> floatVec(Scanner scanner){
		Vector<Float> temp = new Vector<Float>();
		float x = scanner.nextFloat();
		float z = scanner.nextFloat();
		float y = scanner.nextFloat();
		temp.add(x);
		temp.add(y);
		temp.add(-z);
		return temp;
	}
	
	
	public void translateVec(){
		vertices = floatVecToFloatArray(v);
		if(NUM_RGBA_ELS == 0){
			generateDefaultColor();
		} else {			
			colors = floatVecToByteArray(c);
		}
		if(NUM_NXYZ_ELS > 0){
			normals = spreadNormals();
			Log.v("parser", "done");
		}
		if(NUM_ST_ELS > 0){
			texture = floatVecToFloatArray(vt);
			texIndices = byteVecToArray(vtOrder);
		}
		indices = VecToShortArray(vOrder);
		interleavedArray = floatVecToFloatArray(interleavedVec);
		
	}
	
	private short[] VecToShortArray(Vector<Integer> tempVec) {
		Iterator<Integer> it = tempVec.iterator();
		short[] tempArr= new short[tempVec.size()]; 
		int i = 0;
		while(it.hasNext()){
			tempArr[i] =  it.next().shortValue();
			i++;
		}
		return tempArr;
	}

	private float[] spreadNormals() {
		float[] temp = new float[vnOrder.size()*vn.firstElement().size()];
		Iterator<Integer> it = vnOrder.iterator();
		int i = 0;
		while(it.hasNext()){
			Vector<Float> innerNormalsVec = vn.elementAt(it.next());
			Iterator<Float> init = innerNormalsVec.iterator();
			while(init.hasNext()){
				temp[i] = init.next();
				i++;
			}
		}
		return temp;
	}
			
		

	public void createBuffer(){
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4); //4 bytes per float
		vbb.order(ByteOrder.nativeOrder());
		
		mFVertexBuffer = vbb.asFloatBuffer();
		mFVertexBuffer.put(vertices);
		mFVertexBuffer.position(0);

		mColorBuffer = ByteBuffer.allocateDirect(colors.length);
		mColorBuffer.put(colors);
		mColorBuffer.position(0);
		
		ByteBuffer vbbb = ByteBuffer.allocateDirect(vertices.length * 2); //2 bytes per short
		vbbb.order(ByteOrder.nativeOrder());

		mIndexBuffer = vbbb.asShortBuffer();
		mIndexBuffer.put(indices);
		mIndexBuffer.position(0);
		
		if(NUM_NXYZ_ELS > 0){
			ByteBuffer vbc = ByteBuffer.allocateDirect(normals.length * 4); //4 bytes per float
			vbc.order(ByteOrder.nativeOrder());
			
			mNormalBuffer = vbc.asFloatBuffer();
			mNormalBuffer.put(normals);
			mNormalBuffer.position(0);
			
		}
		/*if(NUM_NXYZ_ELS > 0){
			normals = floatVecToFloatArray(vn);
			normIndices = byteVecToArray(vnOrder);
		}
		if(NUM_ST_ELS > 0){
			texture = floatVecToFloatArray(vt);
			texIndices = byteVecToArray(vtOrder);
		}*/

	}
	
	public void generateDefaultColor(){
		colors = new byte[v.size()*4]; 
		//for(int i =0; i < colors.length; i++){
		//	colors[i] = (byte) 255;			
		//}
		for(int i =0; i < colors.length; i = i + 4){
			colors[i] =     (byte) ((i*50)%255);
			colors[i + 1] = (byte) ((i*100)%255);	
			colors[i + 2] = (byte) ((i*200)%255);	
			colors[i + 3] = (byte) 255;	
		}
	}
	
	public float[] floatVecToFloatArray(Vector<Vector<Float>> tempVec){
		Iterator<Vector<Float>> it = tempVec.iterator();
		float[] tempArr= new float[tempVec.size()*tempVec.firstElement().size()]; 
		int i = 0;
		while(it.hasNext()){
			Iterator<Float> init = it.next().iterator();
			while(init.hasNext()){
				tempArr[i] = init.next();
				i++;
			}
		}
		return tempArr;
	}
	
	public byte[] floatVecToByteArray(Vector<Vector<Float>> tempVec){
		Iterator<Vector<Float>> it = tempVec.iterator();
		byte tempArr[] = new byte[tempVec.size()*tempVec.firstElement().size()]; 
		int i = 0;
		while(it.hasNext()){
			Iterator<Float> init = it.next().iterator();
			while(init.hasNext()){
				tempArr[i] = init.next().byteValue();;
				i++;
			}
		}
		return tempArr;
	}
	
	public byte[] byteVecToArray(Vector<Integer> tempVec){
		Iterator<Integer> it = tempVec.iterator();
		byte[] tempArr= new byte[tempVec.size()]; 
		int i = 0;
		while(it.hasNext()){
			tempArr[i] =  it.next().byteValue();
			i++;
		}
		return tempArr;
	}

	public int getM_NumVertices() {
		return m_NumVertices;
	}

	public FloatBuffer getFVertexBuffer() {
		return mFVertexBuffer;
	}

	public ByteBuffer getColorBuffer() {
		return mColorBuffer;
	}

	public ShortBuffer getIndexBuffer() {
		return mIndexBuffer;
	}
	
	public FloatBuffer getNormalBuffer() {
		return mNormalBuffer;
	}
	/*protected void findVertices(String aLine){
		Scanner scanner = new Scanner(aLine);
	    if (scanner.hasNext()){
	    	 String name = scanner.next();
	    	 if (name.equals("f")){
	    		 m_NumVertices++;
	    	 }
	    }
	    scanner.close();
	}*/
	

} 
