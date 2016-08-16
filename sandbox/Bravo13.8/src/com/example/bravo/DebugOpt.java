package com.example.bravo;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class DebugOpt extends Activity {
	private Button send_button;
	private EditText towerText;
	private EditText ghostText;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug_opt);
		send_button = (Button)findViewById(R.id.TowerButton);
		towerText = (EditText)findViewById(R.id.TowerText);
		ghostText = (EditText)findViewById(R.id.GhostText);
		send_button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
			int towers = Integer.parseInt(towerText.getText().toString());
			int ghost = Integer.parseInt(ghostText.getText().toString());
			Intent resultData = new Intent();
			resultData.putExtra("towers", towers);
			resultData.putExtra("ghost", ghost);
			setResult(Activity.RESULT_OK, resultData);
			finish();
			}
		});
	}
	

}
