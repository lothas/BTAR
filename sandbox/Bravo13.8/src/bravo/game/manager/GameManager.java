package bravo.game.manager;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.samples.colorblobdetect.MarkerDetector;

import com.MobileAnarchy.Android.Widgets.Joystick.JoystickView;
import com.example.bravo.MainActivity;
import com.example.bravo.R;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;
import bravo.game.manager.roboRebe;

public class GameManager {
	public static final int     NUM_OF_TOWERS = 6;
	private MainActivity mMainActivity;
	private TextView mTextDebug; //TopLeft
	private TextView mTextTimeStemp; //TopRight
	private TextView mTextBottomRight; //BottomRight
	private TextView mTextBravoVersion;
	private TextView mTextMiddle; //Middle
	private int mCoinDistribution;
	public int mNumOfTowers;
	private int mLazerDistribution;
	private Random mRandomGenerator;
	private boolean flagForDebug, gameStarted;
	enum direction {keepGO, stop, front, back};
	private boolean towerHitFlag = false;
	private boolean laserHitFlag = false;
	private roboRebe myRobotGameplay;
	
	private GameTimer mGameTimer;
	private int coinCollectedDelay=0;
	
	private static final String TAG = "gameMen";
	
	public GameManager(MainActivity activity) {
		gameStarted = false;
		setNumOfTowers(NUM_OF_TOWERS);
		mMainActivity = activity;
		mTextDebug = (TextView) mMainActivity.findViewById(R.id.ScoreTopRight);
		mTextDebug.setText(String.format("Score: 0"));
		mTextBravoVersion = (TextView) mMainActivity.findViewById(R.id.BravoVersion);
		mTextBravoVersion.setText(String.format("Bravo13.7_noDebug"));
		mTextTimeStemp = (TextView) mMainActivity.findViewById(R.id.HealthTopLeft);
		mTextTimeStemp.setText(String.format("Health: 100%%")); // 
		mTextBottomRight = (TextView) mMainActivity.findViewById(R.id.TextBottomRight);
		mTextBottomRight.setText(String.format(""));
		mTextMiddle = (TextView) mMainActivity.findViewById(R.id.TextMiddle);
		mTextMiddle.setText(String.format(""));
		myRobotGameplay = new roboRebe(this);
		
		mGameTimer = new GameTimer(mMainActivity);
		mGameTimer.start();
		
		mRandomGenerator = new Random();
		
		flagForDebug = true;
	}
	
	public void printMiddle(String text){
		final String msg = text;
		mMainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.
            	mTextMiddle.setText(String.format(msg));
            }
        });
	}
	
	public void printTopRight(String text){
		final String msg = text;
		mMainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.
            	mTextDebug.setText(String.format(msg));
            }
        });
	}
	
	public void printTopLeft(String text){
		final String msg = text;
		mMainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.
            	mTextTimeStemp.setText(String.format(msg));
            }
        });
	}
	
	public void printBottomRight(String text){
		final String msg = text;
		mMainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.
            	mTextBottomRight.setText(String.format(msg));
            }
        });
	}
	
	public int CoinDistribution(boolean genarateNewCoin){
		if (genarateNewCoin == true){
			mCoinDistribution = mRandomGenerator.nextInt(mNumOfTowers);
			mMainActivity.tracking.setCoinDistribution(mCoinDistribution); 
		}
		return mCoinDistribution;
	}
	
	public boolean GameIsRuning(){
		return gameStarted;
	}
	
	public void GameStarted(){
		gameStarted = true;		
		CoinDistribution(true); //Generating new coin
	}
	
	public void rotateCoin(){
		for(int i = 0; i < mNumOfTowers; i++){
			if (mMainActivity.tracking.mDetectorArry[i].hasCoin() == true){
				mMainActivity.tracking.mDetectorArry[i].updateCoinAngle();
				break;
			}
		}
	}
	
	public void objCheck(){
		direction js = direction.keepGO;
		direction laserDirection = direction.keepGO;
		if(mMainActivity.tracking.myRobot.isDetected()){
			js = objHitCheck();
			laserDirection = laserHit();
			if(laserDirection != direction.keepGO){
				js = laserDirection;
			}
			if(coinCollectedDelay == 0){
				coinCollect();
			}
		}else{
			js = robNotFound();				
		}
		sendJS(js);
	}
	
	public void printDebug(List<MatOfPoint> a, List<MatOfPoint> b, List<MatOfPoint> c, int many) {
		//double rollAngle = mMainActivity.mGLView.mRenderer.getRollAngle();
		//float t0 = mMainActivity.tracking.mDetectorArry[0].getFront2BackLengthInRealWorld(rollAngle);
		//float t1 = mMainActivity.tracking.mDetectorArry[1].getFront2BackLengthInRealWorld(rollAngle);
		//float t2 = mMainActivity.tracking.mDetectorArry[2].getFront2BackLengthInRealWorld(rollAngle);
		//float t3 = mMainActivity.tracking.mDetectorArry[3].getFront2BackLengthInRealWorld(rollAngle);
		//printBottomRight("rollAngle: " + rollAngle + "\nt0 length: " + t0 + "\nt1 length: " + t1 + "\nt2 length: " + t2 + "\nt3 length: " + t3);
		Iterator<MatOfPoint> AIterator = a.iterator();
		Iterator<MatOfPoint> BIterator = b.iterator();
		Iterator<MatOfPoint> CIterator = c.iterator();
		int i = 0, j = 0, z = 0;;
    	while (AIterator.hasNext()) {
    		MatOfPoint contour = AIterator.next();
    		if (Imgproc.contourArea(contour) > 0.1){
    			Imgproc.drawContours(mMainActivity.tracking.mRgba, a, i, new Scalar(100, 100, 0), -1);
    			i++;
    		}
       	}
    	while (BIterator.hasNext()) {
    		MatOfPoint contour = BIterator.next();
    		if (Imgproc.contourArea(contour) > 0.1){
    			Imgproc.drawContours(mMainActivity.tracking.mRgba, b, j, new Scalar(0, 100, 100), -1);
    			j++;
    		}
       	}
    	while (CIterator.hasNext()) {
    		MatOfPoint contour = CIterator.next();
    		if (Imgproc.contourArea(contour) > 0.1){
    			Imgproc.drawContours(mMainActivity.tracking.mRgba, c, z, new Scalar(100, 0, 100), -1);
    			z++;
    		}
       	}
    	printBottomRight("A : " + i + "\nB : " + j + "\nC : " + z);
    	
	}

	public direction laserHit() {
		double rollAngle = mMainActivity.mGLView.mRenderer.getRollAngle();
		float sizeFactor = 2;
		float robSize = mMainActivity.tracking.myRobot.getFront2BackLength();
		for(int i = 0; i < mNumOfTowers; i++){
			if(!mMainActivity.tracking.mDetectorArry[i].isDetected()){
				continue;
			}
			if(mMainActivity.tracking.mDetectorArry[i].hasLaser()){
				float objCenterX = mMainActivity.tracking.mDetectorArry[i].getMiddleX();
				float objCenterY = mMainActivity.tracking.mDetectorArry[i].getMiddleY();
				float objBackX = mMainActivity.tracking.mDetectorArry[i].getBackX();
				float objBackY = mMainActivity.tracking.mDetectorArry[i].getBackY();
				float objFrontX = mMainActivity.tracking.mDetectorArry[i].getFrontX();
				float objFrontY = mMainActivity.tracking.mDetectorArry[i].getFrontY();
				double objIncline = -Math.tan(mMainActivity.tracking.mDetectorArry[i].getAngleFromXAxisInRadians());
				double objConst = objCenterY - objCenterX*objIncline;
				mMainActivity.tracking.myRobot.drawLaserLine(mMainActivity.tracking.mRgba,-1,(int)(objIncline*-1+objConst),1900,(int)(objIncline*1900+objConst)); //debug
				double centersDist = getLineDist(objIncline,-1,objConst,mMainActivity.tracking.myRobot.getMiddleX(),
						mMainActivity.tracking.myRobot.getMiddleY());
				
				double sizeDistRatio = centersDist/robSize;
				if ( sizeDistRatio < sizeFactor ){ //TODO: find optimal
					//stop robot and let it drive only one way
					float distBack = getDistInRealWorld(mMainActivity.tracking.myRobot.getMiddleX(), 
							mMainActivity.tracking.myRobot.getMiddleY(),
							objBackX, objBackY,rollAngle);
					float distFront = getDistInRealWorld(mMainActivity.tracking.myRobot.getMiddleX(), 
							mMainActivity.tracking.myRobot.getMiddleY(),
							objFrontX, objFrontY,rollAngle);
					if(distBack < distFront){
						return direction.keepGO;
					} else {
						if(!laserHitFlag){
							myRobotGameplay.updateHealth(roboHealth.laser);
						}
						laserHitFlag = true;
						return direction.stop;
					}
				}
			}
		}
		return direction.keepGO;
	}

	public direction objHitCheck(){
		double rollAngle = mMainActivity.mGLView.mRenderer.getRollAngle();
		float sizeFactor = 3.5f;
		float robSize = mMainActivity.tracking.myRobot.getFront2BackLength();
		for(int i = 0; i < mNumOfTowers; i++){
			if(!mMainActivity.tracking.mDetectorArry[i].isDetected()){
				continue;
			}
			float objCenterX = mMainActivity.tracking.mDetectorArry[i].getMiddleX();
			float objCenterY = mMainActivity.tracking.mDetectorArry[i].getMiddleY();
			float centersDist = getDistInRealWorld(mMainActivity.tracking.myRobot.getMiddleX(), 
					mMainActivity.tracking.myRobot.getMiddleY(),
					objCenterX, objCenterY,rollAngle);
			float sizeDistRatio = centersDist/robSize;
			if ( sizeDistRatio < sizeFactor ){ //TODO: find optimal
				//stop robot and let it drive only one way
				
				float distBack = getDistInRealWorld(mMainActivity.tracking.myRobot.getBackX(), 
						mMainActivity.tracking.myRobot.getBackY(),
						objCenterX, objCenterY,rollAngle);
				float distFront = getDistInRealWorld(mMainActivity.tracking.myRobot.getFrontX(), 
						mMainActivity.tracking.myRobot.getFrontY(),
						objCenterX, objCenterY,rollAngle);
				if(!towerHitFlag){
					myRobotGameplay.updateHealth(roboHealth.obstacle);
				}
				towerHitFlag = true;
				if(distBack < distFront){
					//send only front
					framePrints(true,direction.back,i);
					return direction.back;
				} else {
					//send only back
					framePrints(true,direction.front,i);
					return direction.front;
				}
			}
		}
		framePrints(false,direction.back,0);
		towerHitFlag = false;
		return direction.keepGO;
	}
	
	private void framePrints(Boolean hit, direction directionMove, int towerNum) {
		// TODO
		if (hit) {
			double rollAngle = mMainActivity.mGLView.mRenderer.getRollAngle();
			mMainActivity.tracking.mDetectorArry[towerNum].drawHitCirclel(mMainActivity.tracking.mRgba, rollAngle, 2);
			if (directionMove == direction.back) {
				printMiddle("HIT: GO BACK");
			} else {
				printMiddle("HIT: GO FORTH");
			}
		}else{
			printMiddle("");
		}
	}

	public void coinCollect(){
		double rollAngle = mMainActivity.mGLView.mRenderer.getRollAngle();
		float sizeFactor = 1f;
		float robSize = mMainActivity.tracking.myRobot.getFront2BackLength();
		for(int i = 0; i < mNumOfTowers; i++){
			if(!mMainActivity.tracking.mDetectorArry[i].isDetected()){
				continue;
			}
			float objCenterX = mMainActivity.tracking.mDetectorArry[i].getCoinX();
			float objCenterY = mMainActivity.tracking.mDetectorArry[i].getCoinY();
			float centersDist = getDistInRealWorld(mMainActivity.tracking.myRobot.getMiddleX(), 
					mMainActivity.tracking.myRobot.getMiddleY(),
					objCenterX, objCenterY,rollAngle);
			float sizeDistRatio = centersDist/robSize;
			if ( sizeDistRatio < sizeFactor ){ //TODO: find optimal
				CoinDistribution(true);
				myRobotGameplay.updateScore(100);
				coinCollectedDelay = 1;
			}
		}
	}
	
	public void sendJS(direction js){
		double jsX = mMainActivity.joystick.getX();
		double jsY = mMainActivity.joystick.getY();
		if(js == direction.back){
			mMainActivity.joystick.haltDrive(true);
			if(jsX < 45 && jsX > -45 && jsY < 0){
				mMainActivity.joystick.haltDrive(false);
			}
		}else if(js == direction.front){
			mMainActivity.joystick.haltDrive(true);
			if(jsX < 45 && jsX > -45 && jsY > 0){
				mMainActivity.joystick.haltDrive(false);
			}
		}else if(js == direction.stop){
			mMainActivity.joystick.haltDrive(true);
		}else{
			mMainActivity.joystick.haltDrive(false);
		}
	}
	
	public direction robNotFound(){
		//send robot not to move
		return direction.stop;
	}
	
	public float getDistInRealWorld(float frontX, float frontY, float backX, float backY, double rollAngle) {
		double lengthX = (double)(frontX - backX);
		if (rollAngle > 40){
			rollAngle = rollAngle*0.9 + 4;
		}
		if (rollAngle > 60){
			rollAngle = 60;
		}
		double lengthY = ((double)(frontY - backY)) / Math.cos(Math.toRadians(rollAngle));  
		return (float)Math.sqrt((double)(lengthX*lengthX + lengthY*lengthY));
	}
	
	public double getLineDist(double a, double b, double c, double x, double y) {
		return Math.abs(a*x+b*y+c)/Math.sqrt(a*a+b*b);
	}
	
	public int laserDistribution(boolean genarateLazer){
		if (genarateLazer == true){
			mLazerDistribution = mRandomGenerator.nextInt(mNumOfTowers);
		}
		return mLazerDistribution;
	}
	
	public void laserOn(){
		mMainActivity.tracking.mDetectorArry[mLazerDistribution].setLaser(true);
	}
	
	public void laserOff(){
		for(int i = 0; i < mNumOfTowers; i++){
			mMainActivity.tracking.mDetectorArry[i].setLaser(false);
		}
		laserHitFlag = false;
	}
	
	public void laserWarningOn(){ 
		mMainActivity.tracking.mDetectorArry[mLazerDistribution].setLaserWarning(true);
	}
	
	public void laserWarningOff(){
		for(int i = 0; i < mNumOfTowers; i++){
			mMainActivity.tracking.mDetectorArry[i].setLaserWarning(false);
		}
	}
	
	public void setNumOfTowers(int num){
		mNumOfTowers = num;
	}
	
	public void setCoinCollectedDelay(int val){
		coinCollectedDelay = val;
	}
	public int getCoinCollectedDelay(){
		return coinCollectedDelay;
	}
}
















































/*try {
if (flag == true){
	flag = false;
	mTextDebug.setText(String.format("try"));
}
} catch (Exception e) {
Log.d(TAG, "catch :" + e.getMessage());
}*/
