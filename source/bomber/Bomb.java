package bomber;

import java.awt.Graphics;

import tge.tileset.Indexable;

public class Bomb implements Indexable {
	
	public static final int LIFE_SPAND=Game.LOGIC*3;
	
	private int power=2;
	private Bomber owner;
	
	private int countdown=LIFE_SPAND;
	
	public int GetCountdown() {
		return countdown;
	}
	
	public void ignite() {//start explosion
		countdown=4;
	}

	public Bomb(int pow,Bomber owner) {
		this.owner=owner;
		power=pow;
	}

	@Override
	public void draw(Graphics g) {

	}

	@Override
	public double Zindex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void update() {
		countdown--;
	}

	@Override
	public int textureId() {
		int pos=(countdown/8)%6;
		if(pos==4)
			pos=2;
		if(pos==5)
			pos=1;
		//System.out.println(pos);
		return 1+pos;
	}

	public int getPower() {
		return power;
	}

	public Bomber getOwner() {
		return owner;
	}

}
