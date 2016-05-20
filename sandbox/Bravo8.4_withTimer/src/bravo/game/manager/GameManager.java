package bravo.game.manager;

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
	private boolean flag;
	
	private GameTimer mGameTimer;
	
	private static final String TAG = "gameMen";
	
	public GameManager(MainActivity activity) {
		mMainActivity = activity;
		mTextDebug = (TextView) mMainActivity.findViewById(R.id.TextDebug);
		mTextDebug.setText(String.format("DebugText"));
		mTextBravoVersion = (TextView) mMainActivity.findViewById(R.id.TextBravoVersion);
		mTextBravoVersion.setText(String.format("Bravo8.3"));
		mTextTimeStemp = (TextView) mMainActivity.findViewById(R.id.TextTimeStemp);
		mTextTimeStemp.setText(String.format("TimeStempText"));
		mTextBottomRight = (TextView) mMainActivity.findViewById(R.id.TextBottomRight);
		mTextBottomRight.setText(String.format("BR"));
		
		mGameTimer = new GameTimer(mMainActivity);
		mGameTimer.start();
		
		
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
	
}
















































/*try {
if (flag == true){
	flag = false;
	mTextDebug.setText(String.format("try"));
}
} catch (Exception e) {
Log.d(TAG, "catch :" + e.getMessage());
}*/
