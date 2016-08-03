package bravo.game.manager;

import java.util.Random;

import org.opencv.samples.colorblobdetect.MarkerDetector;

import com.MobileAnarchy.Android.Widgets.Joystick.JoystickView;
import com.example.bravo.MainActivity;
import com.example.bravo.R;

import android.util.Log;
import android.widget.TextView;

import bravo.game.manager.roboRebe;

public class GameManager {
	public static final int     NUM_OF_TOWERS = 1;
	private MainActivity mMainActivity;
	private TextView mTextDebug; //TopLeft
	private TextView mTextTimeStemp; //TopRight
	private TextView mTextBottomRight; //BottomRight
	private TextView mTextBravoVersion;
	private String mText;
	private int mCoinDistribution, mNumOfTowers, mLazerDistribution;
	private Random mRandomGenerator;
	private boolean flagForDebug, gameStarted;
	enum direction {keepGO, stop, front, back};
	private boolean towerHitFlag = false;
	private boolean laserHitFlag = false;
	private roboRebe myRobotGameplay;
	private GameTimer mGameTimer;
	
	private static final String TAG = "gameMen";
	
	public GameManager(MainActivity activity) {
		gameStarted = false;
		mNumOfTowers = NUM_OF_TOWERS;
		mMainActivity = activity;
		mTextDebug = (TextView) mMainActivity.findViewById(R.id.ScoreTopRight);
		mTextDebug.setText(String.format("Score: 0"));
		mTextBravoVersion = (TextView) mMainActivity.findViewById(R.id.BravoVersion);
		mTextBravoVersion.setText(String.format("Bravo11.2"));
		mTextTimeStemp = (TextView) mMainActivity.findViewById(R.id.HealthTopLeft);
		mTextTimeStemp.setText(String.format("Health: 100%%")); // 
		mTextBottomRight = (TextView) mMainActivity.findViewById(R.id.TextBottomRight);
		mTextBottomRight.setText(String.format(""));
		myRobotGameplay = new roboRebe();
		
		mGameTimer = new GameTimer(mMainActivity);
		mGameTimer.start();
		
		mRandomGenerator = new Random();
		
		
		flagForDebug = true;
	}
	
	public void printTopRight(String text){
		mText = text;
		mMainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.
            	mTextDebug.setText(String.format(mText));
            }
        });
	}
	
	public void printTopLeft(String text){
		mText = text;
		mMainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.
            	mTextTimeStemp.setText(String.format(mText));
            }
        });
	}
	
	public void printBottomRight(String text){
		mText = text;
		mMainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.
            	mTextBottomRight.setText(String.format(mText));
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
			coinCollect();
		}else{
			js = robNotFound();				
		}
		Log.d("health","Health: " + myRobotGameplay.getHealth() + "%");
		sendJS(js);
	}
	
	public direction laserHit() {
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
					float distBack = getDist(mMainActivity.tracking.myRobot.getMiddleX(), 
							mMainActivity.tracking.myRobot.getMiddleY(),
							objBackX, objBackY);
					float distFront = getDist(mMainActivity.tracking.myRobot.getMiddleX(), 
							mMainActivity.tracking.myRobot.getMiddleY(),
							objFrontX, objFrontY);
					if(distBack < distFront){
						return direction.keepGO;
					} else {
						if(!laserHitFlag){
							myRobotGameplay.updateHealth(roboHealth.laser);
							printTopLeft("Health: " + myRobotGameplay.getHealth() + "%%");
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
		float sizeFactor = 3.2f;
		float robSize = mMainActivity.tracking.myRobot.getFront2BackLength();
		for(int i = 0; i < mNumOfTowers; i++){
			if(!mMainActivity.tracking.mDetectorArry[i].isDetected()){
				continue;
			}
			float objCenterX = mMainActivity.tracking.mDetectorArry[i].getMiddleX();
			float objCenterY = mMainActivity.tracking.mDetectorArry[i].getMiddleY();
			float centersDist = getDist(mMainActivity.tracking.myRobot.getMiddleX(), 
					mMainActivity.tracking.myRobot.getMiddleY(),
					objCenterX, objCenterY);
			float sizeDistRatio = centersDist/robSize;
			if ( sizeDistRatio < sizeFactor ){ //TODO: find optimal
				//stop robot and let it drive only one way
				
				float distBack = getDist(mMainActivity.tracking.myRobot.getBackX(), 
						mMainActivity.tracking.myRobot.getBackY(),
						objCenterX, objCenterY);
				float distFront = getDist(mMainActivity.tracking.myRobot.getFrontX(), 
						mMainActivity.tracking.myRobot.getFrontY(),
						objCenterX, objCenterY);
				if(!towerHitFlag){
					myRobotGameplay.updateHealth(roboHealth.obstacle);
					printTopLeft("Health: " + myRobotGameplay.getHealth() + "%%");
				}
				towerHitFlag = true;
				if(distBack < distFront){
					//send only front
					return direction.front;
				} else {
					//send only back
					return direction.back;
				}
			}
		}
		towerHitFlag = false;
		return direction.keepGO;
	}
	
	public void coinCollect(){
		float sizeFactor = 1f;
		float robSize = mMainActivity.tracking.myRobot.getFront2BackLength();
		for(int i = 0; i < mNumOfTowers; i++){
			if(!mMainActivity.tracking.mDetectorArry[i].isDetected()){
				continue;
			}
			float objCenterX = mMainActivity.tracking.mDetectorArry[i].getCoinX();
			float objCenterY = mMainActivity.tracking.mDetectorArry[i].getCoinY();
			float centersDist = getDist(mMainActivity.tracking.myRobot.getMiddleX(), 
					mMainActivity.tracking.myRobot.getMiddleY(),
					objCenterX, objCenterY);
			float sizeDistRatio = centersDist/robSize;
			if ( sizeDistRatio < sizeFactor ){ //TODO: find optimal
				myRobotGameplay.updateScore(10);
				printTopRight("Score: " + myRobotGameplay.getScore());
				CoinDistribution(true);
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
	
	public float getDist(float fX, float fY, float bX, float bY) {
		return (float)Math.sqrt((double)((fX - bX)*(fX - bX) + (fY - bY)*(fY - bY)));
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
	
}
















































/*try {
if (flag == true){
	flag = false;
	mTextDebug.setText(String.format("try"));
}
} catch (Exception e) {
Log.d(TAG, "catch :" + e.getMessage());
}*/