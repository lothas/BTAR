package bravo.game.manager;

import com.example.bravo.MainActivity;

import android.content.Context;

enum roboHealth {obstacle, laser, bonus};

public class roboRebe {
	private GameManager mGameManager;
	private int health = 100;
	private int score = 0;
	
	public roboRebe(GameManager gameManger){
		mGameManager = gameManger;
	};
	public void updateHealth(roboHealth reason){
		int healthFactor = 0;
		if(reason == roboHealth.obstacle){
			healthFactor = -5;
		}
		else if(reason == roboHealth.laser){
			healthFactor = -20;
		}
		else if(reason == roboHealth.bonus){
			if(health > 90){
				healthFactor = 100 - health;
			}else{
				healthFactor = 10;				
			}
		}
		health = health + healthFactor;
		if(health < 0)
			health = 0;
		mGameManager.printTopLeft("Health: " + health + "%%");
	};
	public void updateScore(int scoreAdd){
		score = score + scoreAdd;
		if (score%1000 == 0){
			updateHealth(roboHealth.bonus);
		}
		mGameManager.printTopRight("Score: " + score);
	}
	public int getHealth() {
		return health;
	}
	public int getScore() {
		return score;
	}
}
