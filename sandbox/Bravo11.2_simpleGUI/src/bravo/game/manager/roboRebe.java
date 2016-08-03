package bravo.game.manager;

enum roboHealth {obstacle, laser, bonus};

public class roboRebe {
	private int health = 100;
	private int score = 0;
	
	public roboRebe(){};
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
	};
	public void updateScore(int scoreAdd){
		score = score + scoreAdd;
		/*if (score%100 == 0){
			updateHealth(roboHealth.bonus);
		}*/
	}
	public int getHealth() {
		return health;
	}
	public int getScore() {
		return score;
	}
}
