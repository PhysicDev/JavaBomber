package bomber;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import tge.Updatable;
import tge.Utilities;
import tge.tileset.Grid;

public class Bomber implements Updatable {
	
	
	//constants:
	private static final double MARGIN=0.499;
	public static final double SPEED=1.5d;
	public static final double LPER=1d/Game.LOGIC;
	public static final double GPER=1d/Game.GRAPH;
	
	public static final int POWER_MAX=8;
	public static final int BOMB_MAX=8;
	public static final int SPEED_MAX=8;
	
	public static BufferedImage palette=null;
	
	private static byte online=0;
	private static int nextId=0;
	private static int animFrame=(int)(0.1d*Game.GRAPH);
	
	//position/animation variables
	private double x,y;
	private double x_,y_;
	private int dir=1;
	private double vx=0,vy=0; 
	private int Ix,Iy;//immunité de collision (pour les bombes)
	
	//texture
	private BufferedImage[] sprite;
	private int animloop=0;
	
	//environment variables;
	private Map environment;
	public boolean isControl=false;
	private int ControlProfile=0;

	//state variables
	private int maximumBomb=1;
	private int currentBomb=0;
	private int speed=1;
	private double speedFact=1.0;
	private int power=1;
	private int player_id;
	private boolean dead=false;

	public Bomber(int x,int y, Map map) {
		this(x,y,map,nextId);
	}
	
	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}
	
	public void setCollisionImmunity(int x,int y) {
		Ix=x;
		Iy=y;
	}
	
	public Bomber(int x,int y, Map map,int P_id) {
		
		if(P_id>32|| P_id<0) 
			throw new IllegalArgumentException("P_id can only have value between 0 and 32");
		if((online&(1<<P_id))!=0)
			throw new IllegalArgumentException("A bomber already use this id");
		
		try {
			sprite=Utilities.loadTileset("./images/assets/walking.png", 24, 32);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(palette==null) {
			try {
				palette=ImageIO.read(new File("./images/assets/palette.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		online|=(1<<P_id);
		player_id=P_id;
		for(BufferedImage bi:sprite) {
			Utilities.replaceColors(bi, palette, P_id);
		}

		for(int i=0;i<8;i++)
			if((online&(1<<i))==0) {
				nextId=i;
				break;
			}
		
		this.x=x+0.5;
		this.y=y+0.5;
		x_=this.x;
		y_=this.y;
		//this.collisionGrid=world;
		environment=map;
	}


	@Override
	protected void finalize() {
		online&=~(1<<player_id);//make player id free for next bomber
	}

	@Override
	public void draw(Graphics g) {
		if(!dead) {
			//debug
			//g.setColor(color);
			//g.drawRect((int)((x_-.5)*environment.getScale()+1+environment.getPosX())
			//		 , (int)((y_-.5)*environment.getScale()+1+environment.getPosY())
			//		 , (int)environment.getScale()-2, (int)environment.getScale()-2);
			
			
			//frame Updater
			if(vx!=0||vy!=0) {
				animloop++;
				if(animloop>8*animFrame)
					animloop=animFrame;
			}else {
				animloop=animFrame-1;
			}
			
			//graphic rendering
			g.drawImage(sprite[4*(animloop/animFrame)+dir], (int)((x_-.75)*environment.getScale()+1+environment.getPosX()),
							        (int)((y_-1.5)*environment.getScale()+1+environment.getPosY()),
							       (int)(environment.getScale() *1.5), (int)environment.getScale()*2, null);

			//simple position update (usefull when logic frame rate is lower than graphics frame rate or when i will add online multiplayer)
			if(environment.getState()==Map.IN_GAME) {
				x_+=GPER*SPEED*vx*speedFact;
				y_+=GPER*SPEED*vy*speedFact;
			}
		}
	}

	@Override //useless class in this context (its supposed to be used to help the render engine to know what is on top of what)
	public double Zindex() {
		return 0;
	}

	public boolean inObstacle(double x,double y){
		return x<0||y<0||x>environment.getWidth()||y>environment.getHeight()||((Ix!=(int)x || Iy!=(int)y)&&environment.solid((int)x,(int)y));//collisionGrid.get((int)(x), (int)(y))!=0);
	}
	
	@Override
	public void update() {
		if(!dead) {
			if(isControl) {
				
				/***
				 * MOVEMENT MANAGMENT :::
				 */
				vx=environment.controller.MovementX(ControlProfile);
				vy=environment.controller.MovementY(ControlProfile);
			}
			
			//selection new direction (for sprites)
			if(vx==-1)
				dir=2;
			else if(vx==1)
				dir=0;
			else if(vy==1)
				dir=1;
			else if(vy==-1)
				dir=3;
			
			if(vx!=0 && vy!=0) {
				//standard stuff
				x+=LPER*SPEED*vx*speedFact;
				if(inObstacle(x+MARGIN*vx,y+MARGIN)||inObstacle(x+MARGIN*vx,y-MARGIN)) {
					x=Math.round(x)-0.5*vx;
					vx=0;
				}
				y+=LPER*SPEED*vy*speedFact;
				if(inObstacle(x+MARGIN,y+MARGIN*vy)||inObstacle(x-MARGIN,y+MARGIN*vy)) {
					y=Math.round(y)-0.5*vy;
					vy=0;
				}
			}else {
				boolean test=vx==0;
				boolean indirectMove=false;
				if(test) {
					y+=LPER*SPEED*vy*speedFact;
					boolean Y1=inObstacle(x+MARGIN,y+MARGIN*vy);
					boolean Y2=inObstacle(x-MARGIN,y+MARGIN*vy);
					
					if(Y1||Y2) {
						y=Math.round(y)-0.5*vy;
						vy=0;
						indirectMove=!(Y1&&Y2);
						if(!Y1)
							vx=1;
						else if(!Y2)
							vx=-1;
					}
				}
				x+=LPER*SPEED*vx*speedFact;
				if(indirectMove && vx*(x_%1-0.5)<0 && vx*(x%1-0.5)>0)
					x=(int)x+0.5;
				boolean X1=inObstacle(x+MARGIN*vx,y+MARGIN);
				boolean X2=inObstacle(x+MARGIN*vx,y-MARGIN);
				if(X1||X2) {
					x=Math.round(x)-0.5*vx;
					vx=0;
					if(vy==0) {
						indirectMove=!(X1&&X2);
						if(!X1)
							vy=1;
						else if(!X2)
							vy=-1;
					}
				}
				
				if(!test) {
					y+=LPER*SPEED*vy*speedFact;

					if(indirectMove && vy*(y_%1-0.5)<0 && vy*(y%1-0.5)>0)
						y=(int)y+0.5;
					if(inObstacle(x+MARGIN,y+MARGIN*vy)||inObstacle(x-MARGIN,y+MARGIN*vy)) {
						y=Math.round(y)-0.5*vy;
						vy=0;
					}
				}
			}

			//selection new direction (for sprites) (in case speed direction change with collision)
			if(vx==-1)
				dir=2;
			else if(vx==1)
				dir=0;
			else if(vy==1)
				dir=1;
			else if(vy==-1)
				dir=3;
			
			//update graphics variables
			x_=x;
			y_=y;
			
			//attempt to collect item;
			environment.collect((int)x,(int)y,this);

			//deactivate collision immunity if needed
			if(Math.hypot(Ix+0.5-x, Iy+0.5-y)>MARGIN*2) {
				Ix=-1;
				Iy=-1;
			}
			
			

			/**
			 * BOMB PLACING
			 */
			//place bomb attempt
			if(isControl & environment.controller.action(ControlProfile)) {
				if(Math.hypot(Ix+0.5-x, Iy+0.5-y)>MARGIN*2 && currentBomb<Math.min(BOMB_MAX, maximumBomb)) {
					//System.out.println("add Bomb !");
					Bomb b=new Bomb(Math.min(this.power,POWER_MAX),this);
					environment.addBomb((int)x, (int)y,b);
					currentBomb++;
					//set collision immunity with the newly placed bomb
					Ix=(int) x;
					Iy=(int) y;
				}
			}
			
			/**
			 * DEATH CHECK
			 */
			if(environment.deadly((int)x, (int)y))
				dead=true;
		}
	}
	
	public void reloadBomb() {
		currentBomb--;
	}
	
	public void upgradeBomb() {
		maximumBomb++;
	}
	
	public void upgradePower() {
		this.power++;
	}
	
	public void upgradeSpeed() {
		this.speed++;
		speedFact=0.875+Math.pow(Math.min(SPEED_MAX,this.speed)*0.75+0.25,2)/8;
		//System.out.println(speedFact+" "+LPER*SPEED*speedFact);
	}

	public void setControlProfile(int controlProfile) {
		ControlProfile = controlProfile;
	}

	public boolean isDead() {
		return dead;
	}
	
	public int getPlayerId() {
		return player_id;
	}
}
