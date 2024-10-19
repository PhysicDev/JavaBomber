package bomber;

import java.awt.Graphics;

import tge.tileset.Indexable;

public class Item implements Indexable {

	private int type=0;
	
	public Item(int type) {
		this.type=type;
	}

	@Override
	public void update() {
	}
	
	public void upgrade(Bomber b) {
		switch(type) {
			case 0:
				b.upgradeBomb();
				break;
			case 1:
				b.upgradePower();
				break;
			case 2:
				b.upgradeSpeed();
				break;
			default:
				break;
		}
	}

	@Override
	public void draw(Graphics g) {
	}

	@Override
	public double Zindex() {
		return 0;
	}

	@Override
	public int textureId() {
		return 5+type;
	}

	public int getType() {
		return type;
	}

}
