package bravo.game.manager;

import java.util.Random;

import org.opencv.samples.colorblobdetect.MarkerDetector;

import com.MobileAnarchy.Android.Widgets.Joystick.JoystickView;
import com.example.bravo.MainActivity;
import com.example.bravo.R;

import android.util.Log;
import android.widget.TextView;

public class GameManager {
	private MainActivity mMainActivity;
	private TextView mTextDebug; //TopLeft
	private TextView mTextTimeStemp; //TopRight
	private TextView mTextBottomRight; //BottomRight
	private TextView mTextBravoVersion;
	private String mText;
	private int mCoinDistribution, mNumOfTowers;
	private Random mRandomGenerator;
	private boolean flag, gameStarted;
	enum direction {keepGO, stop, front, back};
	
	private GameTimer mGameTimer;
	
	private static final String TAG = "gameMen";
	
	public GameManager(MainActivity activity) {
		gameStarted = false;
		mNumOfTowers = 3;
		mMainActivity = activity;
		mTextDebug = (TextView) mMainActivity.findViewById(R.id.TextDebug);
		mTextDebug.setText(String.format("DebugText"));
		mTextBravoVersion = (TextView) mMainActivity.findViewById(R.id.TextBravoVersion);
		mTextBravoVersion.setText(String.format("Bravo10.4_after_exams"));
		mTextTimeStemp = (TextView) mMainActivity.findViewById(R.id.TextTimeStemp);
		mTextTimeStemp.setText(String.format("TimeStempText"));
		mTextBottomRight = (TextView) mMainActivity.findViewById(R.id.TextBottomRight);
		mTextBottomRight.setText(String.format("BR"));
		
		mGameTimer = new GameTimer(mMainActivity);
		mGameTimer.start();
		
		mRandomGenerator = new Random();
		
		
		flag = true;
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
		if(mMainActivity.tracking.mDetectorArry[0].isDetected()){
			js = objHitCheck();
			coinCollect();
		}else{
			js = robNotFound();				
		}
		sendJS(js);
	}
	
	public direction objHitCheck(){
		float sizeFactor = 4;
		float robSize = mMainActivity.tracking.mDetectorArry[0].getFront2BackLength();
		for(int i = 1; i < mNumOfTowers; i++){
			if(!mMainActivity.tracking.mDetectorArry[i].isDetected()){
				continue;
			}
			float objCenterX = mMainActivity.tracking.mDetectorArry[i].getMiddleX();
			float objCenterY = mMainActivity.tracking.mDetectorArry[i].getMiddleY();
			float centersDist = getDist(mMainActivity.tracking.mDetectorArry[0].getMiddleX(), 
					mMainActivity.tracking.mDetectorArry[0].getMiddleY(),
					objCenterX, objCenterY);
			float sizeDistRatio = centersDist/robSize;
			if ( sizeDistRatio < sizeFactor ){ //TODO: find optimal
				//stop robot and let it drive only one way
				
				float distBack = getDist(mMainActivity.tracking.mDetectorArry[0].getBackX(), 
						mMainActivity.tracking.mDetectorArry[0].getBackY(),
						objCenterX, objCenterY);
				float distFront = getDist(mMainActivity.tracking.mDetectorArry[0].getFrontX(), 
						mMainActivity.tracking.mDetectorArry[0].getFrontY(),
						objCenterX, objCenterY);
				if(distBack < distFront){
					//send only front
					return direction.front;
				} else {
					//send only back
					return direction.back;
				}
			}
		}
		return direction.keepGO;
	}
	
	public void coinCollect(){
		float sizeFactor = 1f;
		float robSize = mMainActivity.tracking.mDetectorArry[0].getFront2BackLength();
		for(int i = 1; i < mNumOfTowers; i++){
			if(!mMainActivity.tracking.mDetectorArry[i].isDetected()){
				continue;
			}
			float objCenterX = mMainActivity.tracking.mDetectorArry[i].getCoinX();
			float objCenterY = mMainActivity.tracking.mDetectorArry[i].getCoinY();
			float centersDist = getDist(mMainActivity.tracking.mDetectorArry[0].getMiddleX(), 
					mMainActivity.tracking.mDetectorArry[0].getMiddleY(),
					objCenterX, objCenterY);
			float sizeDistRatio = centersDist/robSize;
			if ( sizeDistRatio < sizeFactor ){ //TODO: find optimal
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
}
















































/*try {
if (flag == true){
	flag = false;
	mTextDebug.setText(String.format("try"));
}
} catch (Exception e) {
Log.d(TAG, "catch :" + e.getMessage());
}*/