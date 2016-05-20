package com.example.android.opengl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Vector;

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
	private Vector<Vector<Float>> c = new Vector<Vector<Float>>();
	private Vector<Vector<Float>> vt = new Vector<Vector<Float>>();
	private Vector<Integer> vtOrder = new Vector<Integer>();
	public float[] vertices;
	public byte[] colors;
	public byte[] indices;
	private float[] interleavedArray;
	private final InputStream fFilePath;
	private final InputStream fFilePath2;
	public int m_NumVertices = 0;
	private int arrayOffset = 0;
    private final int size;
    public FloatBuffer mFVertexBuffer;
    public ByteBuffer mColorBuffer;
    public ByteBuffer mIndexBuffer;
	

	public myParser(InputStream aFileName, InputStream aFileName2, int vert, int norm, int color, int tex){
		fFilePath = aFileName;
		fFilePath2 = aFileName2;
		NUM_XYZ_ELS = vert;
		NUM_NXYZ_ELS = norm;
		NUM_RGBA_ELS = color;
		NUM_ST_ELS = tex;
		size = NUM_XYZ_ELS+NUM_NXYZ_ELS+NUM_RGBA_ELS+NUM_ST_ELS;
	}

	public void run(){
		processLineByLine();
		translateVec();
		createBuffer();
	}
	
	public final void processLineByLine(){
		Scanner scanner =  new Scanner(fFilePath);
		while (scanner.hasNextLine()){
			findVertices(scanner.nextLine());
		}
		scanner.close();
	    m_NumVertices*=3;
		interleavedArray = new float[size*m_NumVertices];
		scanner =  new Scanner(fFilePath2);
		while (scanner.hasNextLine()){
			processLine(scanner.nextLine());
		}
		scanner.close();
	}
	
	public void translateVec(){
		vertices = floatVecToFloatArray(v);
		if(NUM_RGBA_ELS == 0){
			generateDefaultColor();
		} else {			
			colors = floatVecToByteArray(c);
		}
		indices = byteVecToArray(vOrder);
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

		mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
		mIndexBuffer.put(indices);
		mIndexBuffer.position(0);

	}
	
	public void generateDefaultColor(){
		colors = new byte[v.size()*4]; 
		//for(int i =0; i < colors.length; i++){
		//	colors[i] = (byte) 255;			
		//}
		for(int i =0; i < colors.length; i = i + 4){
			colors[i] = (byte) ((i*25)%255) ;
			colors[i+1] = (byte) ((i*50)%255);
			colors[i+2] = (byte) ((i*75)%255);
			colors[i+3] = (byte) 255;
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
	
	protected void findVertices(String aLine){
		Scanner scanner = new Scanner(aLine);
	    if (scanner.hasNext()){
	    	 String name = scanner.next();
	    	 if (name.equalsIgnoreCase("f")){
	    		 m_NumVertices++;
	    	 }
	    }
	    scanner.close();
	}
	
	protected void processLine(String aLine){
    //use a second Scanner to parse the content of each line 
	  	Scanner scanner = new Scanner(aLine);
	    if (scanner.hasNext()){
		      //assumes the line has a certain structure
		    String name = scanner.next();
		    if(name.equals("v")) {
	        	v.add(floatVec(scanner));
		    }else if (name.equals("vn")) {
	        	vn.add(floatVec(scanner));
			}else if (name.equals("c")) {
	        	c.add(floatVec(scanner));
			}else if (name.equals("vt")) {
	        	vt.add(floatVec(scanner));
			}else if (name.equals("f")) {
				orderVertices(scanner.next());
	        	orderVertices(scanner.next());
	        	orderVertices(scanner.next());
			}
	    }	 
	}
	
	public void orderVertices(String values){
		Scanner scanner = new Scanner(values);
		scanner.useDelimiter("/");
		int j=0;
		//System.out.println(arrayOffset);
		while (scanner.hasNext()) {
			try{
				int foo = Integer.parseInt(scanner.next())-1;
				int i=0;
				switch(j) {
			        case 0:
			        	vOrder.add(foo);
			        	for(i=0; i<NUM_XYZ_ELS ; i++){
			        		interleavedArray[arrayOffset+i]=v.elementAt(foo).elementAt(i);
			        	}
			        	for(i=0; i<NUM_RGBA_ELS ; i++){
		        			interleavedArray[arrayOffset+i+NUM_XYZ_ELS+NUM_NXYZ_ELS]=c.elementAt(foo).elementAt(i);
			        	}
			        	break;
			        case 1:
			        	vtOrder.add(foo);
			        	for(i=0; i<NUM_ST_ELS ; i++){
				        	interleavedArray[arrayOffset+i+NUM_XYZ_ELS+NUM_NXYZ_ELS+NUM_RGBA_ELS]=vt.elementAt(foo).elementAt(i);
			        	}
			        	i+=2;
			        	break;
			        case 2: 
			        	vnOrder.add(foo);
			        	for(i=0; i<NUM_NXYZ_ELS ; i++){
			        		interleavedArray[arrayOffset+i+NUM_XYZ_ELS]=vn.elementAt(foo).elementAt(i);
			        	}
			        	i+=3;
			        	break;
			        default: ;
			        	break;
				    }
			}catch(Exception e){ 
				System.out.println(e.getCause());
			}
			j++;
		}
    	arrayOffset += size;
    	scanner.close();
	}
	
	
	public Vector<Float> floatVec(Scanner scanner){
		Vector<Float> temp = new Vector<Float>();
		while (scanner.hasNextFloat()) {
			temp.add(scanner.nextFloat());
		}
		return temp;
	}

} 
