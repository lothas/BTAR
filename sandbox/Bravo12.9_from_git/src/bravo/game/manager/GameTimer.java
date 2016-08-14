package bravo.game.manager;

import java.util.Timer;
import java.util.TimerTask;

import com.example.bravo.MainActivity;
import com.example.bravo.R;

import android.widget.TextView;

public class GameTimer {
	protected static final int START_TIME = 5;
	protected static final String TAG = "FP";
	private String mText;
	private MainActivity mMainActivity;
	private TextView mTextTime;
	int sec = 0;
	int min = 5;
	
	public GameTimer(MainActivity Activity){
		mMainActivity = Activity;
		mTextTime = (TextView) mMainActivity.findViewById(R.id.TimeTopCenter);
		mTextTime.setText(String.format("TIME"));
	}
	
	Timer timer = new Timer();
	TimerTask task = new TimerTask() {	
		int tenMilSec = 0;
		int sec = 0;
		int min = START_TIME;
		public void run() {
			printTime("" + min + ":" + sec + ":" + tenMilSec);
			if (mMainActivity.mGameManager.GameIsRuning() == false){return;}
			updateGame(min, sec, tenMilSec); //update all game activities that require time;			
			if (tenMilSec == 0) {
				tenMilSec = 99;
				if (sec == 0) {
					sec = 59;
					min--;
				} else {
					sec--;
				}
			}else {
				tenMilSec--;
			}
		}
		
	};
	
	public void start() {
		timer.scheduleAtFixedRate(task, 10, 10);
	}
	
	public void printTime(String text){
		mText = text;
		mMainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.
            	mTextTime.setText(String.format(mText));
            }
        });
	}
	
	public void resetCoin(){
		mMainActivity.mGameManager.CoinDistribution(true);
	}
	
	public void coinUpdate(int min, int sec, int tMilSec){
		if (((sec == 30) || (sec == 0)) && (tMilSec == 0)){
			resetCoin(); // to change position of the coin
		}
		mMainActivity.mGameManager.rotateCoin();		
	}
	
	private void updateGame(int min, int sec, int tMilSec){
		coinUpdate(min, sec, tMilSec);
		lazerUpdate(min, sec, tMilSec);
	}
	
	private void lazerUpdate(int min, int sec, int tMilSec){
		if ((min > START_TIME - 1) && ((sec > 45) || (sec == 0))){
			return;
		}
		if((sec % 10 == 5) && (tMilSec == 0)){
			mMainActivity.mGameManager.laserDistribution(true);
		}
		for(int i = 1; i < 4; i++){
			if((sec % 10 == i ) && (tMilSec == 0)){
				mMainActivity.mGameManager.laserWarningOn();
			}
			if((sec % 10 == i - 1) && (tMilSec == 50)){
				mMainActivity.mGameManager.laserWarningOff();
			}
		}
		if((sec % 10 == 0) && (tMilSec == 0)){
			mMainActivity.mGameManager.laserOn();
		}
		if((sec % 10 == 8) && (tMilSec == 0)){
			mMainActivity.mGameManager.laserOff();
		}
	}
}
