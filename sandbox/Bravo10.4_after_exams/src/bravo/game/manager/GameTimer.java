package bravo.game.manager;

import java.util.Timer;
import java.util.TimerTask;

import com.example.bravo.MainActivity;
import com.example.bravo.R;

import android.widget.TextView;

public class GameTimer {
	private String mText;
	private MainActivity mMainActivity;
	private TextView mTextTime;
	int sec = 0;
	int min = 5;
	
	public GameTimer(MainActivity Activity){
		mMainActivity = Activity;
		mTextTime = (TextView) mMainActivity.findViewById(R.id.TextTime);
		mTextTime.setText(String.format("TIME"));
	}
	
	Timer timer = new Timer();
	TimerTask task = new TimerTask() {	
		int tenMilSec = 0;
		int sec = 0;
		int min = 5;
		public void run() {
			printTime("" + min + ":" + sec + ":" + tenMilSec);
			if (mMainActivity.mGameManager.GameIsRuning() == false){return;}
			coinUpdate(min, sec, tenMilSec);
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
		if (((sec == 30) | (sec == 0)) & (tMilSec == 0)){
			resetCoin(); // to change position of the coin
		}
		mMainActivity.mGameManager.rotateCoin();		
	}
}
