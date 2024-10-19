package bomber;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicScrollBarUI;

import tge.Sim;
import tge.object.BackgroundPanel;
import tge.object.SlidingPanel;
import tge.tileset.GridRenderer;

public class Game extends Sim {

    // Assume this is the image that should appear in each cell (you'll load it appropriately)
    private static BufferedImage miniature;
	private static Font mainfont=null;
	
	private JButton leave;
	private JButton resume;
	private JLabel pauseText;
	private OptionPanel LevelOption;
	
	public static MultiControler controller;
	
	private static Properties options;
	private static Properties control;
	public static Properties lang;
	
	public void setLabel(String label) {
		pauseText.setText(label);}
	
	public void setPauseMenuVisible(boolean visible) {
		leave.setVisible(visible);
		resume.setVisible(visible);
	}

	public void setEndMenuVisible(boolean visible) {
		leave.setVisible(visible);
		resume.setVisible(false);
	}
	
	static class CustomScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
        	trackColor = Color.DARK_GRAY;  // Thumb color
            thumbColor = Color.LIGHT_GRAY; // Track color
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            g.setColor(thumbColor);
            g.fillRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height);
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(trackColor);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
    }

    
	public class OptionMenu extends JPanel{
	    Icon up = new ImageIcon("images/assets/up.png");
	    Icon down = new ImageIcon("images/assets/down.png");
		private ArrayList<JRadioButton> langues;
		private ArrayList<String> prefix;
		private int[] SX=new int[]{1920,1280,720,800};
		private int[] SY=new int[]{1080,720,576,800};
		private JRadioButton[] res=new JRadioButton[SX.length];
		private JCheckBox fullScreen;
		
		private Properties[] keyProperties;
		private File[] keyProfile;
		private int[][] mappings;
		private int[] transposeProf;
		private JLabel[] playLab;
		
		public void updateOption() {
			//update language
			for(int i=0;i<langues.size();i++) {
				if(langues.get(i).isSelected()) {
					options.setProperty("LANG",prefix.get(i));
					break;
				}
			}
			//update graphics
			for(int i=0;i<res.length;i++) {
				if(res[i].isSelected()) {
					options.setProperty("RESX",""+SX[i]);
					options.setProperty("RESY",""+SY[i]);
					break;
				}
			}
			options.setProperty("FULLSCREEN",fullScreen.isSelected()?"1":"0");
			
			out:for(int i=0;i<keyProfile.length;i++) {
				for(int j=0;j<5;j++) {
					if(mappings[i][j]!=-1)
						break;
					if(j==4)
						continue out;
				}
				if(keyProfile[i]==null) {
					keyProfile[i]=new File("./properties/keyProfile"+(i+1)+".properties");
					control.setProperty("KEYPROF"+i,"./properties/keyProfile"+(i+1)+".properties");
				}
				if(keyProperties[i]==null) {
					keyProperties[i]=new Properties();
				}
				keyProperties[i].setProperty("CODE_UP",""+mappings[i][0]);
				keyProperties[i].setProperty("CODE_DOWN",""+mappings[i][1]);
				keyProperties[i].setProperty("CODE_RIGHT",""+mappings[i][2]);
				keyProperties[i].setProperty("CODE_LEFT",""+mappings[i][3]);
				keyProperties[i].setProperty("CODE_ACTION",""+mappings[i][4]);
				try {
					FileOutputStream output = new FileOutputStream(keyProfile[i]);
					keyProperties[i].store(output, null);
					output.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
			}
			
			//control.setProperty("PROFILES",transposeProf.length+"");
			String[][] temp=new String[8][2];
			for(int i=0;i<8;i++) {
				temp[i]=new String[]{control.get("P"+i)+"",""+control.get("P"+i+"TYPE")};
			}
			for(int i=0;i<8;i++) {
				control.setProperty("P"+transposeProf[i],temp[i][0]);
				control.setProperty("P"+transposeProf[i]+"TYPE",temp[i][1]);
			}

			//save control
			FileOutputStream output;
			try {
				output = new FileOutputStream("./properties/controlMap.properties");
				control.store(output, null);
				output.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			//save options
			try {
				output = new FileOutputStream("./properties/options.properties");
				options.store(output, null);
				output.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		
		private JRadioButton radio(String text) {
			JRadioButton output=new JRadioButton(text);
			output.setFont(mainfont.deriveFont(20f));
			output.setOpaque(false);
			// Create custom icons for selected and unselected states
	        Icon unselectedIcon = new ImageIcon("./images/assets/radio.png"); 
	        Icon selectedIcon = new ImageIcon("./images/assets/radiofull.png");    

	        output.setIcon(unselectedIcon);      
	        output.setSelectedIcon(selectedIcon);
	        output.setFocusPainted(false);  
			return output;
		}
		
		 private JPanel createItemPanel(JLabel label, JPanel parentPanel,int i) {
			 
		        JPanel itemPanel = new JPanel(new BorderLayout(4,4));

		        // Panel to hold the label

		        // Panel to hold the buttons
		        JPanel buttonPanel = new JPanel(new GridBagLayout());
		        GridBagConstraints gbc = new GridBagConstraints();
		        //gbc.fill=GridBagConstraints.NONE;
		        
		        JButton upButton = new JButton("");
		        upButton.setIcon(up);
		        upButton.setOpaque(false);
		        upButton.setBackground(new Color(0,0,0,0));
		        upButton.setPreferredSize(new Dimension(20,20));
		        upButton.setFocusable(false);
		        upButton.setBorder(null);
		        JButton downButton = new JButton("");
		        downButton.setIcon(down);
		        downButton.setOpaque(false);
		        downButton.setBackground(new Color(0,0,0,0));
		        downButton.setFocusable(false);
		        downButton.setPreferredSize(new Dimension(20,20));
		        downButton.setBorder(null);

		        // Move up action
		        JLabel player=simpleLabel(lang.get("PLAYER")+" "+(i+1)+" : ",25);
		        playLab[i]=player;
		        upButton.addActionListener(new ActionListener() {
				 	
					@Override
					public void actionPerformed(ActionEvent e) {
						int pos=0;
						while(transposeProf[pos]!=i)pos++;
						System.out.println(pos);
						if(pos==0)
							return;
						pos--;
						transposeProf[pos+1]=transposeProf[pos];
						transposeProf[pos]=i;
						playLab[pos+1]=playLab[pos];
						playLab[pos]=player;
						player.setText(lang.get("PLAYER")+" "+(pos+1)+" : ");
						playLab[pos+1].setText(lang.get("PLAYER")+" "+(pos+2)+" : ");

			        	System.out.println(pos);
			        	System.out.println("action down : "+i);
			        	System.out.println(Arrays.toString(transposeProf));
						moveItem(itemPanel, parentPanel, -1);
						
				}});

		        // Move down action
		        downButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						int pos=0;
						while(transposeProf[pos]!=i)pos++;
						System.out.println(pos);
						if(pos==playLab.length-1)
							return;
						pos++;
						transposeProf[pos-1]=transposeProf[pos];
						transposeProf[pos]=i;
						playLab[pos-1]=playLab[pos];
						playLab[pos]=player;
						player.setText(lang.get("PLAYER")+" "+(pos+1)+" : ");
						playLab[pos-1].setText(lang.get("PLAYER")+" "+(pos)+" : ");

			        	System.out.println(pos);
			        	System.out.println("action down : "+i);
			        	System.out.println(Arrays.toString(transposeProf));
						moveItem(itemPanel, parentPanel, 1);
						
				}});
		        buttonPanel.add(upButton,gbc);
		        buttonPanel.add(downButton,gbc);
		        buttonPanel.add(player,gbc);
		        itemPanel.add(label, BorderLayout.CENTER);
		        
		        itemPanel.add(buttonPanel, BorderLayout.WEST);

		        return itemPanel;
		    }

		    private void moveItem(JPanel itemPanel, JPanel parentPanel, int direction) {
		        int index = parentPanel.getComponentZOrder(itemPanel);
		        int newIndex = index + direction;

		        // Ensure the new index is within bounds
		        if (newIndex >= 0 && newIndex < parentPanel.getComponentCount()) {
		            parentPanel.remove(itemPanel);
		            parentPanel.add(itemPanel, newIndex);
		            parentPanel.revalidate();
		            parentPanel.repaint();
		        }
		    }
		
		public JPanel KeyBoardProfile(int prof,File profile){
			JPanel key=new JPanel();
			JLabel keytitle=simpleLabel(lang.get("KEYPROF")+" "+(prof+1),30);

			int[] mapping=new int[]{-1,-1,-1,-1,-1};
			mappings[prof]=mapping;
			if(profile==null) {
				this.keyProperties[prof]=null;
			}else {
				try {

					FileInputStream input=new FileInputStream(profile);
		        	Properties properties = new Properties();
					properties.load(input);
					this.keyProperties[prof]=properties;
					keyProfile[prof]=profile;
					input.close();
					
					mapping[0]=Integer.parseInt(""+properties.get("CODE_UP"));
					mapping[1]=Integer.parseInt(""+properties.get("CODE_DOWN"));
					mapping[2]=Integer.parseInt(""+properties.get("CODE_RIGHT"));
					mapping[3]=Integer.parseInt(""+properties.get("CODE_LEFT"));
					mapping[4]=Integer.parseInt(""+properties.get("CODE_ACTION"));
					
				}catch(Exception e) {
				}
			}
			
			key.setLayout(new GridBagLayout());
	        GridBagConstraints gbc = new GridBagConstraints();
	        gbc.gridwidth = GridBagConstraints.REMAINDER;     
	        gbc.insets=new Insets(5,5,5,5);
	        gbc.weightx = 1;
	        gbc.weighty = 1;
	        
	        key.add(keytitle,gbc);
	        JButton UP=simpleButton(""+lang.get("UP")+" : "+((mapping[0]!=-1)?KeyEvent.getKeyText(mapping[0]):(""+lang.get("NONE"))),Color.decode("#888888"));
	        JButton DOWN=simpleButton(""+lang.get("DOWN")+" : "+((mapping[1]!=-1)?KeyEvent.getKeyText(mapping[1]):(""+lang.get("NONE"))),Color.decode("#888888"));
	        JButton RIGHT=simpleButton(""+lang.get("RIGHT")+" : "+((mapping[2]!=-1)?KeyEvent.getKeyText(mapping[2]):(""+lang.get("NONE"))),Color.decode("#888888"));
	        JButton LEFT=simpleButton(""+lang.get("LEFT")+" : "+((mapping[3]!=-1)?KeyEvent.getKeyText(mapping[3]):(""+lang.get("NONE"))),Color.decode("#888888"));
	        JButton BOMB=simpleButton(""+lang.get("BOMB")+" : "+((mapping[4]!=-1)?KeyEvent.getKeyText(mapping[4]):(""+lang.get("NONE"))),Color.decode("#888888"));
	        key.add(UP,gbc);
	        key.add(DOWN,gbc);
	        key.add(RIGHT,gbc);
	        key.add(LEFT,gbc);
	        key.add(BOMB,gbc);
	        
	        UP.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					AwaitKey Akey=new AwaitKey(mapping,0,(KeyControl)(controller.getKeyListener()),UP,lang.get("UP")+" : ");
					Akey.start();
					refocus();
			}});
	        DOWN.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					AwaitKey Akey=new AwaitKey(mapping,1,(KeyControl)(controller.getKeyListener()),DOWN,lang.get("DOWN")+" : ");
					Akey.start();
					refocus();
			}});
	        RIGHT.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					AwaitKey Akey=new AwaitKey(mapping,2,(KeyControl)(controller.getKeyListener()),RIGHT,lang.get("RIGHT")+" : ");
					Akey.start();
					refocus();
			}});
	        LEFT.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					AwaitKey Akey=new AwaitKey(mapping,3,(KeyControl)(controller.getKeyListener()),LEFT,lang.get("LEFT")+" : ");
					Akey.start();
					refocus();
			}});
	        BOMB.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					AwaitKey Akey=new AwaitKey(mapping,4,(KeyControl)(controller.getKeyListener()),BOMB,lang.get("BOMB")+" : ");
					Akey.start();
					refocus();
			}});
	        
	        
			return key;
		}
		

	    private JPanel createReorderablePanel(JLabel[] labels) {
	        JPanel panel = new JPanel();
	        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

	        for (int i = 0; i < labels.length; i++) {
	            JPanel itemPanel = createItemPanel(labels[i], panel,i);
	            panel.add(itemPanel);
	        }

	        return panel;
	    }
		
		private class AwaitKey extends Thread  {
			
			int[] map;
			int i;
			KeyControl kl;
			JButton parent;
			String text;
			
			public AwaitKey(int[] map,int i,KeyControl kl,JButton parent,String text) {
				this.map=map;
				this.i=i;
				this.kl=kl;
				this.parent=parent;
				this.text=text;
			}
			
			public void run() {
			   System.out.println("key binding ... awaiting keys ....");
			   kl.lastKey=-1;
			   int loop=0;
			   while(kl.lastKey==-1) { 
				   /**wait**/
				   try {Thread.sleep(20);
				   } catch (InterruptedException e){}
				   int L=loop/15;
				   parent.setText(text+".".repeat(L)+" ".repeat(4-L));
				   loop++;
				   loop%=4*15;
			   }

			   System.out.println("key pressed : "+kl.lastKey);
			   //abort key : escape
			   if(kl.lastKey!=27)
			   	   map[i]=kl.lastKey;
			   else
				   System.out.println("abort key binding");
			   parent.setText(text+KeyEvent.getKeyText(map[i]));
			}
		}
		
		public OptionMenu() {
			this.setBackground(new Color(250,250,250,180));
	        
			
			this.setLayout(new GridBagLayout());

			
			JLabel ltitle=simpleLabel(""+lang.get("LANGUAGE"),50);
			
			//option langues
			langues=new ArrayList<JRadioButton>();
			prefix=new ArrayList<String>();
			//on recupère tous les fichiers de langues 
			File[] files = new File("./properties/lang/").listFiles();
			File LANG=new File("./properties/lang/"+options.getProperty("LANG")+".properties");
	        if (files != null) {
	            for (File file : files) {
	                if (file.isFile()) {
	                	//attempt to open and read it:
	                	try {
	                		FileInputStream input=new FileInputStream(file);
	            			Properties temp = new Properties();
	            			temp.load(input);
	            			input.close();
	            			String name=temp.getProperty("LANG");
	            			JRadioButton rad=radio(name);
	            			prefix.add(temp.getProperty("PRE"));
	            			langues.add(rad);
	            			if(file.getCanonicalFile().equals(LANG.getCanonicalFile()))
	            				rad.setSelected(true);
		            	} 
	                	catch (Exception e) {}
		            }
		        }
	        }
	        /**
			JRadioButton LoptF = radio("Francais");
			LoptF.setSelected(true);
			JRadioButton LoptE = radio("English");**/
			ButtonGroup BGroup = new ButtonGroup();
			for(JRadioButton rad:langues)
				BGroup.add(rad);

			JLabel note=simpleLabel(""+lang.get("OPTTEXT"),15);
			JLabel gtitle=simpleLabel(""+lang.get("GRAPH"),50);
			
			for(int i=0;i<SX.length;i++) {
				JRadioButton rad=radio(SX[i]+"x"+SY[i]);
				res[i]=rad;
				if(SX[i]==Integer.parseInt(options.getProperty("RESX"))&& SY[i]==Integer.parseInt(options.getProperty("RESY")))
					rad.setSelected(true);
			}
			ButtonGroup GGroup = new ButtonGroup();
			for(JRadioButton rad:res)
				GGroup.add(rad);
			
			//keyprofile
			
			int keyProf=Integer.parseInt(control.getProperty("KEYPROFILES"));
			JPanel[] profile=new JPanel[keyProf];
			keyProperties=new Properties[keyProf];
			mappings=new int[keyProf][];
			keyProfile=new File[keyProf];
		    for(int i=0;i<keyProf;i++) {
		      	String file=control.getProperty("KEYPROF"+i);
		       	if(!file.isEmpty()) {
		       		profile[i]=KeyBoardProfile(i,new File(file));
		       	}else {
		       		profile[i]=KeyBoardProfile(i,null);
		       	}
		    }
			
			
			fullScreen=new JCheckBox(lang.getProperty("FULLSCREEN"));
			fullScreen.setFont(mainfont.deriveFont(20f));
			fullScreen.setOpaque(false);
			// Create custom icons for selected and unselected states
	        Icon unselectedIcon = new ImageIcon("./images/assets/box.png"); 
	        Icon selectedIcon = new ImageIcon("./images/assets/boxfull.png");    

	        fullScreen.setIcon(unselectedIcon);      
	        fullScreen.setSelectedIcon(selectedIcon);
	        fullScreen.setFocusPainted(false);  
			if(Integer.parseInt(options.getProperty("FULLSCREEN"))==1)
				fullScreen.setSelected(true);
	        
			JLabel ctitle=simpleLabel(""+lang.get("CONTROL"),50);
			
			
			JButton resetController=simpleButton(""+lang.get("SCAN"),Color.decode("#7bc0f5"));
			
			resetController.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					controller.rescan();
			}});

	        GridBagConstraints gbc = new GridBagConstraints();
	        gbc.gridwidth = GridBagConstraints.REMAINDER;     
	        gbc.insets=new Insets(5,25,5,25);
	        //gbc.weighty=1;
	        gbc.fill = GridBagConstraints.BOTH;
	        this.add(note,gbc);
	        this.add(ltitle,gbc);
			for(JRadioButton rad:langues)
				this.add(rad,gbc);
	        
	        this.add(gtitle,gbc);
			for(JRadioButton rad:res)
				this.add(rad,gbc);
			this.add(fullScreen,gbc);
	        this.add(ctitle,gbc);
	        for(JPanel pan:profile) {
	        	this.add(pan,gbc);
	        }
	        //this.add(resetController,gbc);
			int prof=Integer.parseInt(control.getProperty("PROFILES"));
	        JLabel[] labs=new JLabel[prof];
	        transposeProf=new int[prof];
	        playLab=new JLabel[prof];
	        for(int i=0;i<prof;i++) {
	        	String PR=control.getProperty("P"+i+"TYPE")+"";
	        	int pos=Integer.parseInt(control.getProperty("P"+i));
				JLabel lab=simpleLabel((PR.equals("KEY")?(lang.get("KEYPROF")+" "+pos):(lang.get("STICK")+" : "+controller.getControllerName(pos))),20);

				lab.setHorizontalAlignment(JLabel.LEFT);
				labs[i]=lab;
				transposeProf[i]=i;
	        }
	        
	        
	        
	        
	        JPanel profiles=createReorderablePanel(labs);
	        this.add(simpleLabel(lang.get("PROF")+"",35),gbc);
			this.add(profiles,gbc);
		}
	    
	}
	
	
	
	public static class OptionPanel extends JPanel{

		public static final int BOT=2;
		public static final int HUMAN=1;
		public static final int NONE=0;
		
		int[] Ptype;
		
		public OptionPanel() {
			this.setBackground(new Color(150,150,150,180));
	        
			this.setSize(300, 1000);
			this.setPreferredSize(new Dimension(300, 1000));
	        // Make the grid scrollable
	        JScrollPane scrollPane = new JScrollPane(this);
	        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

	        JScrollBar Vscroll = scrollPane.getVerticalScrollBar();
	        Vscroll.setUnitIncrement(24);
	        Vscroll.setBackground(Color.BLACK);
	        Vscroll.setUI(new CustomScrollBarUI());
	        Vscroll.setBorder(null);
			
		}
	    public void updatePanel(Map level) {
	    	Ptype=new int[level.getPlayerAmount()];
	        this.removeAll();
			this.setLayout(new GridBagLayout());
	        GridBagConstraints gbc = new GridBagConstraints();
	        gbc.gridwidth = GridBagConstraints.REMAINDER;     
	        gbc.insets=new Insets(5,25,5,25);
	        System.out.println(level.getPlayerAmount());
        	JLabel title1=new JLabel("joueurs");
        	title1.setFont(mainfont.deriveFont(45f));
        	add(title1,gbc);
	        for(int i=0;i<level.getPlayerAmount();i++) {
	        	JButton bombButton=simpleButton(level.game.lang.get("PLAYER")+" "+(i+1)+" : "+level.game.lang.get("HUMAN"),Color.decode("#a8d741"));
	        	Ptype[i]=1;
	        	int pos=i;//for action listener
	        	bombButton.addActionListener(new ActionListener() {
	    			@Override
	    			public void actionPerformed(ActionEvent e) {
	    				Ptype[pos]=(Ptype[pos]+1)%2;
	    				
	    				switch(Ptype[pos]) {
	    					case HUMAN:
	    						updateButton(bombButton,level.game.lang.get("PLAYER")+" "+(pos+1)+" : "+level.game.lang.get("HUMAN"),Color.decode("#a8d741"));
	    						break;
	    					case BOT:
	    						updateButton(bombButton,level.game.lang.get("PLAYER")+" "+(pos+1)+" : "+level.game.lang.get("AI"),Color.decode("#eabc45"));
	    						break;
	    					case NONE:
	    						updateButton(bombButton,level.game.lang.get("PLAYER")+" "+(pos+1)+" : "+level.game.lang.get("NONE"),Color.decode("#c9c9c9"));
	    						break;
	    				}
	    				
	    				
	    				
	    		}});
	        	add(bombButton,gbc);
	        }
	        

	    }
	    
	}
	
	
	
	//Cette classe à été écrite par chat GPT puis modifiée par moi (donc attention)
	public static class FileGridPanel extends JPanel {
		
		
		
		public static final int cellSize=200;
		
	    // Constructor that accepts the folder path
	    public FileGridPanel(File folder,Game game) {

	        setLayout(new BorderLayout());

	        // Create a JPanel to hold the grid of files
	        JPanel gridPanel = new SlidingPanel("./images/assets/background_bombs.png",128,SlidingPanel.LEFT);
	        gridPanel.setBackground(Color.decode("#555555"));
	        
	        int GSize=(int) (Integer.parseInt(""+options.get("RESX"))/(cellSize*1.6));
	        gridPanel.setLayout(new GridLayout(0,GSize, 10, 10)); // Grid layout with 3 columns, adjust as needed

	        // Get list of files in the folder
	        File[] files = folder.listFiles();
	        if (files != null) {
	            for (File file : files) {
	                if (file.isFile()) {
	                    // Create a panel for each file (cell)
	                    JPanel cellPanel = createCellPanel(file,game);
	                    gridPanel.add(cellPanel);
	                }
	            }
	        }

	        // Make the grid scrollable
	        JScrollPane scrollPane = new JScrollPane(gridPanel);
	        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

	        JScrollBar Vscroll = scrollPane.getVerticalScrollBar();
	        Vscroll.setUnitIncrement(24);
	        Vscroll.setBackground(Color.BLACK);
	        Vscroll.setUI(new CustomScrollBarUI());
	        
	        
	        Vscroll.setBorder(null);

	        // Add the scroll pane to the main panel
	        add(scrollPane, BorderLayout.CENTER);
	    }

	    private BufferedImage renderLevel(String level){
	    	Map m =null;
	    	try {
				m=new Map(level,300,300,true);
			} catch (IOException e) {
				e.printStackTrace();
			}
	    	BufferedImage output=new BufferedImage(300,300,BufferedImage.TYPE_INT_RGB);
	    	m.draw(output.getGraphics());
	    	m=null;
	    	System.gc();
			return output;
	    }
	    
	    // Method to create each cell panel
	    private JPanel createCellPanel(File file,Game game) {
	        String name=file.getName().substring(0, file.getName().length()-4);
	        
	        File imageFile = new File("./images/mini/"+name+".png");
	        //attempt to load images :
	        Image scaledImage = null;
	        if(!imageFile.exists()) {
	        	scaledImage= miniature.getScaledInstance(cellSize, cellSize, Image.SCALE_SMOOTH);
	        	try {
					ImageIO.write(renderLevel(file.getAbsolutePath()), "png", imageFile);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
	        }
	        try {
				scaledImage=ImageIO.read(imageFile).getScaledInstance(cellSize, cellSize, Image.SCALE_SMOOTH);;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			//Image scaledImage = miniature.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
	        JPanel cellPanel = new JPanel();
	        cellPanel.setLayout(new BorderLayout());
	        cellPanel.setPreferredSize(new Dimension((int)(cellSize*1.5), (int)(cellSize*1.5))); // Adjust size as needed
	        cellPanel.setBackground(new Color(150,150,150,180));//Color.decode("#888888"));
	        // Create a label for the file title
	        JLabel titleLabel = new JLabel(name, SwingConstants.CENTER);
	        titleLabel.setFont(mainfont.deriveFont(30f));
	        // Create a clickable button that displays the image
	        JButton imageButton = new JButton(new ImageIcon(scaledImage));
	        imageButton.setPreferredSize(new Dimension(cellSize, cellSize)); // Square image button
	        imageButton.setFocusPainted(false);
	        imageButton.setBorderPainted(false);
	        imageButton.setContentAreaFilled(false);
	        imageButton.addMouseListener((MouseListener) new MouseAdapter() {

	            @Override
	            public void mouseEntered(MouseEvent me) {
	            	cellPanel.setBackground(new Color(200,200,200,180));
	            }
	            @Override
	            public void mouseExited(MouseEvent me) {
	            	cellPanel.setBackground(new Color(150,150,150,180));
	            }
	        });

	        // Add an action listener to handle the click event
	        imageButton.addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                game.load(file.getAbsolutePath());  // Call the load method when clicked
	            }
	        });

	        // Add the image button and title to the cell panel
	        cellPanel.add(imageButton, BorderLayout.CENTER);
	        cellPanel.add(titleLabel, BorderLayout.SOUTH);  // Title at the bottom of the cell

	        return cellPanel;
	    }
	    
	    

	}
	
	private void load(String file) {
        System.out.println("Loading file: " + file);
        this.LoadLevel(file);
        this.loadMenu("optionLevel");//Sim.GAME_SCREEN);
        this.paused=false;
        ((OptionPanel) LevelOption).updatePanel(level);
		//this.level.startGame();
    }
	
	public static final int LOGIC=60;
	public static final int GRAPH=60;
	
	
	private static Color background=Color.decode("#10B838");
	private Map level=null;
	
	Random R=new Random(12345);
	
	public int getSizeY() {
		return level.getWidth();}
	public int getSizeX() {
		return level.getHeight();}
	GridRenderer<Integer> debug;
	
	public Game(int X,int Y,double size){
		this();
		level=new Map(X,Y,size);
	}
	
	public Game(int X,int Y){
		this(X,Y,16);
	}
	
	private Properties DefaultProperties() {
		Properties output=new Properties();
		output.setProperty("LANG", "FR");
		output.setProperty("RESX", "1980");
		output.setProperty("RESY", "1080");
		output.setProperty("FULLSCREEN", "1");
		return output;
	}
	
	public Game() {

		Map.game=this;

		//load options
		try {
			FileInputStream input=new FileInputStream("./properties/options.properties");
			options = new Properties();
			options.load(input);
			input.close();
		} catch (Exception e) {
			//if error then we reset the option menu
			options=DefaultProperties();
			FileOutputStream output;
			try {
				output = new FileOutputStream("./properties/options.properties");
				options.store(output, null);
				output.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			System.err.println("option properties was not found reseting option file");
		}
		
		//load Control
		try {
			FileInputStream input=new FileInputStream("./properties/controlMap.properties");
			control = new Properties();
			control.load(input);
			input.close();
		} catch (Exception e) {}
		
		//loading language file
		String defaultLang="FR";
		try {
			FileInputStream input=new FileInputStream("./properties/lang/"+options.getProperty("LANG")+".properties");
			lang = new Properties();
			lang.load(input);
			input.close();
		} catch (Exception e) {
			try {
				FileInputStream input=new FileInputStream("./properties/lang/"+defaultLang+".properties");
				lang = new Properties();
				lang.load(input);
				input.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		MultiControler mc=new MultiControler(control);
		controller=mc;
		
		try {
			miniature=ImageIO.read(new File("./images/assets/placeholder.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
            // Load the TTF font from the file path
            File fontFile = new File("font/cube.ttf"); // Change to your TTF file path
            mainfont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
        } catch (FontFormatException | IOException e) {e.printStackTrace();}
		
		//in game menu setup
		this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets=new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.BOTH;

		leave=simpleButton(lang.getProperty("QUIT"),Color.decode("#a8d741"));
		resume=simpleButton(lang.getProperty("RESUME"),Color.decode("#7bc0f5"));
		
		leave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				level.setState(Map.PAUSED);
				leave.setVisible(false);
				resume.setVisible(false);
				loadMenu("title");
				paused=false;
		}});
		
		resume.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				level.setState(Map.IN_GAME);
				setPauseMenuVisible(false);
				leave.setVisible(false);
				resume.setVisible(false);
				pauseText.setText("");
		}});
		
		pauseText=new JLabel("");
		pauseText.setFont(mainfont.deriveFont(60f));
		pauseText.setHorizontalAlignment(JLabel.CENTER);
		pauseText.setForeground(Color.decode("#ffffff"));
		this.add(pauseText,gbc);
		this.add(resume,gbc);
		this.add(leave, gbc);
		setPauseMenuVisible(false);
		
		//Menu level select
        File folder = new File("./bomberlevels/");
        FileGridPanel FGP=new FileGridPanel(folder,this);
        addMenu(FGP, "select");
		
        //Menu option select:
        JPanel optionback=new SlidingPanel("./images/assets/background_bombs.png",128,SlidingPanel.LEFT);
        

        
        optionback.setLayout(new GridBagLayout());
		JButton start=simpleButton(lang.getProperty("START"),Color.decode("#a8d741"));

		LevelOption=new OptionPanel();

		
		
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadMenu(Sim.GAME_SCREEN);
				int profId=0;
				for(int i=0;i<level.getPlayerAmount();i++)
					if(LevelOption.Ptype[i]==OptionPanel.HUMAN) {
				        level.linkProfile(i,profId);
				        profId++;
					}
				//remove unused player
				for(int i=0;i<level.getPlayerAmount();i++)
					if(LevelOption.Ptype[i]==OptionPanel.NONE)
						level.removePlayer(i);
		        
				level.startGame();
		}});
		
		optionback.add(LevelOption,gbc);
		optionback.add(start,gbc);
		addMenu(optionback,"optionLevel");
        
		
		//option MENU
		JPanel mainopt=new SlidingPanel("./images/assets/background_bombs.png",128,SlidingPanel.LEFT);
		mainopt.setLayout(new GridBagLayout());
		
		
		JLabel titleopt=new JLabel(lang.getProperty("OPTION"));
		titleopt.setHorizontalAlignment(JLabel.CENTER);
		titleopt.setFont(mainfont.deriveFont(60f));
		OptionMenu commandPanel=new OptionMenu();

        // Make the option menu scrollable
        JScrollPane scrollPane = new JScrollPane(commandPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(600,Integer.parseInt(options.getProperty("RESY"))-300));
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        //scrollPane.setSi
        JScrollBar Vscroll = scrollPane.getVerticalScrollBar();
        Vscroll.setUnitIncrement(24);
        Vscroll.setBackground(Color.BLACK);
        Vscroll.setUI(new CustomScrollBarUI());
        Vscroll.setBorder(null);
        
		JButton quitopt=simpleButton(lang.getProperty("BACK"),Color.decode("#f5a87b"));
		JButton apply=simpleButton(lang.getProperty("APPLY"),Color.decode("#a8d741"));
		
		mainopt.add(titleopt,gbc);
		mainopt.add(scrollPane,gbc);
		mainopt.add(quitopt,gbc);
		mainopt.add(apply,gbc);
		
		addMenu(mainopt, "OPTION");
		
		quitopt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadMenu("title");
				paused=false;
		}});
		
		apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				commandPanel.updateOption();
		}});
        
        //Main Menu
        JPanel main=new SlidingPanel("./images/assets/background_bombs.png",128,SlidingPanel.LEFT);
        //have to create a copy of the sliding each time because it cause conflict with the card layout
        JPanel title=new BackgroundPanel("./images/assets/title_bomb.png");
        title.setSize(600,200);
        title.setPreferredSize(new Dimension(600,200));
        title.setBackground(new Color(0,0,0,0));
        main.setLayout(new GridBagLayout());
        //gbc.fill = GridBagConstraints.HORIZONTAL;
        
		JButton play=simpleButton(lang.getProperty("PLAY"),Color.decode("#a8d741"));
		JButton option=simpleButton(lang.getProperty("OPTION"),Color.decode("#7bc0f5"));
		JButton quit=simpleButton(lang.getProperty("QUIT"),Color.decode("#f5a87b"));
		
		main.add(title,gbc);
		main.add(play, gbc);
		main.add(option, gbc);
		main.add(quit, gbc);
		
		play.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadMenu("select");
				paused=false;
		}});
		
		option.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadMenu("OPTION");
				paused=false;
		}});
		
		quit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sim.dispose();
		}});
		
		addMenu(main,"title");
		
	}
	
	private static void updateButton(JButton button,String string,Color c) {
		button.setText(string);
		button.setBorder(BorderFactory.createLineBorder(c, 3));
		button.addMouseListener((MouseListener) new MouseAdapter() {
	        @Override
	        public void mouseEntered(MouseEvent me) {
	        	button.setBorder(BorderFactory.createLineBorder(c, 5));
	        	button.setBackground(new Color(255,255,255,230));
	        	
	        }
	        @Override
	        public void mouseExited(MouseEvent me) {
	        	button.setBorder(BorderFactory.createLineBorder(c, 3));
	        	button.setBackground(new Color(255,255,255,150));
	        	
	        }
		});
	}
	
	private void refocus() {
        sim.requestFocusInWindow();
	}
	
	private static JButton simpleButton(String string,Color c) {
		JButton output=new JButton(string);
		output.setPreferredSize(new Dimension(300,50));
		output.setSize(300,50);
		output.setMinimumSize(new Dimension(300,50));
		output.setFont(mainfont.deriveFont(35f));
		//output.setForeground(c);
		output.setBackground(new Color(255,255,255,150));
		output.setBorder(BorderFactory.createLineBorder(c, 3));
		output.setContentAreaFilled(true);
        output.setFocusPainted(false);  
		
		output.addMouseListener((MouseListener) new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent me) {
        		output.setBorder(BorderFactory.createLineBorder(c, 5));
        		output.setBackground(new Color(255,255,255,230));
            	
            }
            @Override
            public void mouseExited(MouseEvent me) {
        		output.setBorder(BorderFactory.createLineBorder(c, 3));
        		output.setBackground(new Color(255,255,255,150));
            	
            }
        });
		return output;
	}
	
	private static JLabel simpleLabel(String label,float size) {
		JLabel output=new JLabel(label);
		output.setHorizontalAlignment(JLabel.CENTER);
		output.setFont(mainfont.deriveFont(size));
		return output;
	}
	
	public void LoadLevel(String lvl) {
		level=null;
		System.gc();//to clean unused asset
		try {
			System.out.println(sim.getContentPane().getWidth()+" "+sim.getContentPane().getHeight());
			level=new Map(lvl,sim.getContentPane().getWidth(),sim.getContentPane().getHeight());
		} catch (IOException e) {
			e.printStackTrace();
		}
		//level.reducePlayer(2);
		
	}
	
	public static void main(String... args) {
		
		
		String lvl1="./bomberlevels/classic.txt";
		String lvl2="./bomberlevels/small.txt";
		//GAME SETUP
		Game game=new Game();
		
		
		Game.FrameLimiterLogic=LOGIC;
		Game.FrameLimiterGraphic=GRAPH;
		
		
        

		// FRAME SETUP
		JFrame frame=game.setupFrame();

        game.loadMenu("title");
        game.paused=false;
        frame.setVisible(true);

		//START GAME
        game.StartLoop();
		
		
	}

	@Override
	public JFrame setupFrame() {
		/**
		KeyControl kl=new KeyControl();
		kl.setProfile(0,"./properties/player1.properties");
		kl.addProfile("./properties/player2.properties");//"./properties/inputs.properties");**/
		//GameControl control=new GameControl();
		
		Map.controller=controller;
		JFrame frame=super.setupFrame();
		BufferedImage favicon=null;
		try {
			favicon = ImageIO.read(new File("./images/assets/iconBomb.png"));
		} catch (IOException e) {e.printStackTrace();}
		frame.setIconImage(favicon);
		frame.setSize(Integer.parseInt(options.getProperty("RESX")),Integer.parseInt(options.getProperty("RESY")));
		if(Integer.parseInt(options.getProperty("FULLSCREEN"))==1) {
			frame.setExtendedState(Frame.MAXIMIZED_BOTH);
			frame.setUndecorated(true);
		}
		frame.setTitle(lang.getProperty("TITLE"));
		frame.setResizable(false);
		frame.setFocusable(true);
		frame.setFocusTraversalKeysEnabled(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addKeyListener(controller.getKeyListener());
		return frame;
	}
	
	@Override
	public int GraphicLoop() {
		super.GraphicLoop();
		return 0;
	}
	
	@Override
	public int LogicLoop() {
		super.LogicLoop();
		if(level!=null)
			level.update();
		return 0;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		//this.frameDebug(g);
		g.setFont(mainfont.deriveFont(30f));
		g.setColor(background);
		g.fillRect(0,0,sim.getContentPane().getWidth(),sim.getContentPane().getHeight());
		if(level!=null)
			level.draw(g);
	}
}
