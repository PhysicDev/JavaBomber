package bomber;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;

import tge.Updatable;
import tge.Utilities;
import tge.tileset.AdvancedTilesetRenderer;
import tge.tileset.Grid;
import tge.tileset.Indexable;
import tge.tileset.SmartTileset;
import tge.tileset.TilesetRenderer;

public class Map implements Updatable{
	
	public class BomberCompare implements Comparator<Bomber>{
		@Override
		public int compare(Bomber o1, Bomber o2) {
			return o1.getPlayerId()-o2.getPlayerId();
		}
		
	}
	
	//static methods
	private static final int INDEX_MAPPER(Indexable test) {
		if(test==null)
			return 0;
		return test.textureId();
	}
	private static final int DEFLAG(Integer val) {
		if(val==null)
			return 0;
		return FIRE[(int) Math.ceil(val/3f)];
	}
	private static final int IDENTITY(int i) {
		return i;}


	//constants
	//data
	private static final int[] FIRE=new int[] {0,1,1,2,3,3,2,1,1};
	private static final int DEFLAG_LIFE=24;
	private static final int START_COUNTDOWN=Game.LOGIC*3;
	private static final String[] START_TEXT=new String[] {"3","2","1","GO!"};
	
	//states constant
	public static final int NOT_STARTED=-1;
	public static final int IN_GAME=0;
	public static final int PAUSED=1;
	public static final int DRAW=2;
	public static final int WIN=3;
		
	//controller
	public static Control controller;
	
	//game parent instance
	public static Game game;
	
	//rendering variable
	public BufferedImage background=null;
	private Font mainfont=null;

	
	
	
	//utilities variable
	
	//variable used to count frame, most of the time just increase every frame but can be used differently depending of the game state
	private long Lframes=-START_COUNTDOWN;
	
	Random R=new Random();

	//map info variable
	private int X,Y;
	private int playerAmount;
	private double[] itemsRatio=new double[]{0.3,0.6,0.9};
	private int gameState=NOT_STARTED;
	//private String message="waiting ...";

	//grid data
	private Grid<Integer> grid;
	private Grid<Indexable> assets;
	private Grid<Integer> deflag;
	private ArrayList<Bomber> players =new ArrayList<Bomber>(); 
	
	
	
	
	public void linkProfile(int bomber,int profile) {
		players.get(bomber).isControl=true;
		players.get(bomber).setControlProfile(profile);
	}
	
	private void initRenderer() {
		
		try {
			background=Utilities.tileImage(ImageIO.read(new File("./images/assets/ground.png")),X*16,Y*16);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		AdvancedTilesetRenderer<Integer> tileset=new AdvancedTilesetRenderer<Integer>();
		try {
			tileset.loadTileset("./images/assets/bomber.png",16,20);
		} catch (IOException e) {
			e.printStackTrace();
		}
		tileset.setMapper(Map::IDENTITY);
		grid.addGridRenderer(tileset);
		

		TilesetRenderer<Indexable> items=new TilesetRenderer<Indexable>();
		try {
			items.loadTileset("./images/assets/items.png",16);
		} catch (IOException e) {
			e.printStackTrace();
		}
		items.setMapper(Map::INDEX_MAPPER);
		assets.addGridRenderer(items);
		
		SmartTileset<Integer> st=null;//new SmartTileset<Integer>();
		try {
			st=new SmartTileset<Integer>(new String[]{"./images/assets/deflag_1.png","./images/assets/deflag_2.png","./images/assets/deflag_3.png"},16);
		} catch (IOException e) {
			e.printStackTrace();
		}
		st.setMapper(Map::DEFLAG);
		//System.out.println(st.testMapper(null));
		//System.exit(0);
		deflag.addGridRenderer(st);
		

        File fontFile = new File("font/cube.ttf"); // Change to your TTF file path
        try {
			mainfont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Map(int x, int y, double size) {
		grid=new Grid<Integer>(x,y,size);
		assets=new Grid<Indexable>(x,y,size);
		deflag=new Grid<Integer>(x,y,size);
		deflag.fill(0);
		X=x;
		Y=y;
		
		for(int i=0;i<X*Y;i++) {
			grid.set(i,R.nextDouble()>0.5?1:0);
			//System.out.println(grid.get(i));
		}
		
		 initRenderer();
	}
	public Map(String fileName,int W, int H) throws IOException{
		this(fileName,W,H,false);
	}
	
	public Map(String fileName,int W, int H,boolean mini) throws IOException{
		
			/**
			 * LEVEL LOADER :
			 */
		
			BufferedReader reader = new BufferedReader(new FileReader(fileName));

	        // Read the first line for width and height
	        String[] dimensions = reader.readLine().split(",");
	        int width = Integer.parseInt(dimensions[0].trim());
	        int height = Integer.parseInt(dimensions[1].trim());
	        
	        //cellX
	        double Sx=(double)W/(double)width;
	        double Sy=(double)H/(double)height;
	        
	        double scale=Math.min(Sx, Sy);
	        double Px=(W-scale*width)/2;
	        double Py=(H-scale*height)/2;
	        

	        // Initialize the grid
	        int[] grid_buffer = new int[height*width];

	        // Read the subsequent lines and populate the grid
	        int pos=0;
	        for (int i = 0; i < height; i++) {
	            String[] line = reader.readLine().split(",");
	            for (int j = 0; j < width; j++) {
	            	int val=Integer.parseInt(line[j].trim());
	            	if(val==3)
	            		playerAmount++;
	            	grid_buffer[pos] = val;//Integer.parseInt(line[j].trim());
	            	pos++;
	            }
	        }
	        //read for more instruction:
	        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
	            String[] data=line.trim().split("\\s+");
	            if(data[0].charAt(0)!='%')
		            switch(data[0]) {
	            		case("SR")://soft ratio
	            			if(data.length<1)
	            				throw new IOException("error while reading argument of instruction at line : "+line);
	            			//block removal
	            			double ratio=Double.parseDouble(data[1]);
	            			for(int i=0;i<grid_buffer.length;i++)
	            				if(grid_buffer[i]==2 && R.nextDouble()>ratio && !mini)
	            					grid_buffer[i]=0;
	            			break;
	            		case("LT")://loot table
	            			if(data.length<4)
	            				throw new IOException("error while reading argument of instruction at line : "+line);

            				double Rbomb=Double.parseDouble(data[1]);
            				double Rfire=Double.parseDouble(data[2])+Rbomb;
            				double RSpeed=Double.parseDouble(data[3])+Rfire;
            				itemsRatio=new double[] {Rbomb,Rfire,RSpeed};
	            			break;
	            		default:
		            }
	        }

	        reader.close();
	        X=width;
	        Y=height;
	        grid=new Grid<Integer>(X,Y,scale);
			assets=new Grid<Indexable>(X,Y,scale);
			deflag=new Grid<Integer>(X,Y,scale);
			
			grid.setPosX(Px);
			grid.setPosY(Py);
			assets.setPosX(Px);
			assets.setPosY(Py);
			deflag.setPosX(Px);
			deflag.setPosY(Py);
			
			deflag.fill(0);
			
			int[] order=new int[playerAmount];
			
			for(int i=0;i<playerAmount;i++)
				order[i]=i;

			//random permutation
			for(int i=0;i<playerAmount;i++) {
				int next=i+R.nextInt(playerAmount-i);
				int temp=order[i];
				order[i]=order[next];
				order[next]=temp;
			}
			pos=0;
			int id=0;
	        for(int y=0;y<Y;y++)
		        for(int x=0;x<X;x++) {
		        	if(grid_buffer[pos]==3) {
		        		grid_buffer[pos]=0;
		    	        players.add(new Bomber(x,y,this,order[id]));
		    	        id++;
		        	}
		        	pos++;
		        }
	        
	        Collections.sort(players,new BomberCompare());
	        
	        for(int i=0;i<width*height;i++) {
	        	grid.set(i,grid_buffer[i]);
	        }

			 initRenderer();
	}
	
	
	
	@Override
	public void draw(Graphics g) {
		//g.setColor(background);
		//g.fillRect((int)grid.getPosX(), (int)grid.getPosY(), (int)(grid.getSizeX()*grid.getScale()),(int)(grid.getSizeY()*grid.getScale()));
		g.drawImage(background, (int)grid.getPosX(),(int)grid.getPosY()+ (int)(grid.getScale()/4d),(int)(grid.getScale()*X),(int)(grid.getScale()*Y),null);
		
		grid.draw(g);
		assets.draw(g);
		deflag.draw(g);
		for(Bomber b:players)
			b.draw(g);
		/**
		if(gameState!=IN_GAME) {
			g.setColor(Color.WHITE);
			g.setFont(mainfont.deriveFont(60f));
			int textWidth = g.getFontMetrics().stringWidth(message);
			g.drawString(message,(int)(grid.getPosX()+grid.getScale()*X/2d)-textWidth/2,(int)(grid.getPosY()+grid.getScale()*Y/2d));
		}**/
		if(Lframes<START_COUNTDOWN/3 && gameState==IN_GAME) {
			int dat=(int) ((Lframes+START_COUNTDOWN)/(START_COUNTDOWN/3));
			if(Lframes==START_COUNTDOWN/3)
				game.setLabel("");
			else
				game.setLabel(START_TEXT[dat]);
			/**
			g.setColor(Color.WHITE);
			g.setFont(mainfont.deriveFont(60f));
			int textWidth = g.getFontMetrics().stringWidth(message);
			g.drawString(message,(int)(grid.getPosX()+grid.getScale()*X/2d)-textWidth/2,(int)(grid.getPosY()+grid.getScale()*Y/2d));**/
		}else if(gameState==IN_GAME) {
			game.setLabel("");
		}
		
	}

	@Override
	public double Zindex() {
		return 0;
	}

	@Override
	public void update() {
		controller.updateControl();
		//System.out.println(gameState);
		switch(gameState) {
			case IN_GAME:
				
				//frame managment
				Lframes++;
				
				//if frame negative (ie we just start the game)
				//we cannot move
				if(Lframes<0)
					break;
				
				//check for victory
				int alive=0;
				Bomber winner=null;
				for(Bomber b:players)
					if(!b.isDead()) {
						alive++;
						winner=b;
					}
				if(alive==0) {
					gameState=DRAW;
					game.setLabel(game.lang.getProperty("DRAW"));
					game.setEndMenuVisible(true);
					//message="DRAW ...";
				}else if(alive==1){
					gameState=WIN;
					game.setLabel(game.lang.getProperty("WINPRE")+" "+(winner.getPlayerId()+1)+" "+game.lang.getProperty("WIN"));
					game.setEndMenuVisible(true);
					//message="PLAYER "+(winner.getPlayerId()+1)+" win !!!";
				}
				
				//update everything
				for(Bomber b:players)
					b.update();
				int pos=0;
				for(Indexable el:assets) {
					//update bombs
					if(el!=null) {
						el.update();
						if(el instanceof Bomb && ((Bomb)el).GetCountdown()<0) {
							assets.set(pos,null);
							((Bomb)el).getOwner().reloadBomb();
							startExplosion(pos%X,pos/X,((Bomb)el).getPower());
						}	
					}
					
					//update deflagrations
					if(deflag.get(pos)!=0) {
						deflag.set(pos,deflag.get(pos)-1);
						if(deflag.get(pos)==0 && grid.get(pos)==2) {
							grid.set(pos, 0);
							double rval=R.nextDouble();
							for(int i=0;i<itemsRatio.length;i++)
								if(rval<itemsRatio[i]) {
									assets.set(pos, new Item(i));
									break;
								}
						}
					}
					pos++;
				}

				//pause button check
				if(controller.pauseActivate()) {
					gameState=PAUSED;
					game.setPauseMenuVisible(true);
				}
				
				break;
			case NOT_STARTED:
				Lframes=-START_COUNTDOWN;
				break;
			case WIN:
				break;
			case DRAW:
				break;
			case PAUSED:
				game.setLabel(game.lang.getProperty("PAUSE"));
				if(controller.pauseActivate()) {
					gameState=IN_GAME;
					game.setPauseMenuVisible(false);
				}
				break;
			default:
				gameState=PAUSED;
				game.setPauseMenuVisible(true);
				game.setLabel(game.lang.getProperty("PAUSE"));
		}
	}
	
	public void startExplosion(int x,int y,int power) {
		deflag.set(x, y, DEFLAG_LIFE);
		int work=15;
		for(int i=1;i<=power;i++) {
			if((work & 1)>0)
				if(x+i<X) {
					if(!spreadDeflag(x+i,y))
						work-=1;}
				else
					work-=1;
			
			if((work & 2)>0)
				if(x-i>0) {
					if(!spreadDeflag(x-i,y))
						work-=2;}
				else
					work-=2;

			if((work & 4)>0)
				if(y+i<Y) {
					if(!spreadDeflag(x,y+i))
						work-=4;}
				else
					work-=4;

			if((work & 8)>0)
				if(y-i>0) {
					if(!spreadDeflag(x,y-i))
						work-=8;}
				else
					work-=8;
		}
	}
	
	private boolean spreadDeflag(int x,int y) {
		if(grid.get(x,y)==1)
			return false;
		deflag.set(x, y, DEFLAG_LIFE);
		if(grid.get(x,y)==2)
			return false;
		if(assets.get(x, y) instanceof Bomb) {
			((Bomb)assets.get(x,y)).ignite();
			return false;
		}
		if(assets.get(x, y) instanceof Item) {
			assets.set(x,y,null);
			return false;
		}
		return true;
	}
	
	//use this to remove unused player
	public void removePlayer(int Pid) {
		for(Bomber b:players) {
			if(b.getPlayerId()==Pid) {
				players.remove(b);
				return;
			}
		}
	}

	public int getWidth() {
		return X;
	}
	public int getHeight() {
		return Y;
	}
	
	public boolean solid(int x,int y) {
		return grid.get(x,y)!=0 || (assets.get(x,y) instanceof Bomb);
	}
	
	public boolean deadly(int x,int y) {
		return deflag.get(x,y)!=0;
	}
	
	public void addBomb(int x,int y,Bomb b) {
		if(deflag.get(x,y)!=0)
			b.ignite();//if bomb spawn in fire ignite it
		assets.set(x, y,b);
		for(Bomber p:players) {
			if(Math.hypot(x+0.5-p.getX(), y+0.5-p.getY())<1)
				p.setCollisionImmunity(x, y);
		}
	}
	
	public double getScale() {
		return grid.getScale();
	}

	public double getPosX() {
		return grid.getPosX();
	}

	public double getPosY() {
		return grid.getPosY();
	}

	public void collect(int x, int y, Bomber bomber) {
		if(assets.get(x,y) instanceof Item) {
			((Item)assets.get(x,y)).upgrade(bomber);
			assets.set(x, y,null);
		}
	}
	
	public void startGame() {
		gameState=IN_GAME;
	}

	public int getPlayerAmount() {
		return playerAmount;
	}
	
	public int getState() {
		return this.gameState;
	}
	
	public void setState(int state) {
		this.gameState=state;
	}
}
