package bomber;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class KeyControl implements KeyListener,Control {

    private static final int CODE_UP=90;
    private static final int CODE_DOWN=83;
	private static final int CODE_RIGHT=68;
	private static final int CODE_LEFT=81;
	private static final int CODE_ACTION=32;
	private static final int CODE_PAUSE=27;
	
	public int lastKey=-1;
	
	private ArrayList<Properties> profiles=new ArrayList<Properties>();
	
	private ArrayList<Boolean> actionkey=new ArrayList<Boolean>();;
	
	private ArrayList<Integer> dirBuffer=new ArrayList<Integer>();
	private boolean pauseImpulse=false;
	
	
	public KeyControl(String propertiesFile) {
		FileInputStream input =null;
		try {
			input=new FileInputStream(propertiesFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        try {
        	Properties properties = new Properties();
			properties.load(input);
			profiles.add(properties);
		} catch (IOException e) {
			e.printStackTrace();
		}
        try {
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		dirBuffer.add(0);
		actionkey.add(false);
	}
	
	public void setProfile(int i,String propertiesFile) {
		if(propertiesFile==null) {
			profiles.set(i,null);
			dirBuffer.set(i,0);
			actionkey.set(i,false);
			return;
		}
		FileInputStream input =null;
		try {
			//System.out.println(new File(propertiesFile).getAbsolutePath());
			input=new FileInputStream(propertiesFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        try {
        	Properties properties = new Properties();
			properties.load(input);
			profiles.set(i,properties);
		} catch (IOException e) {
			e.printStackTrace();
		}
        try {
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		dirBuffer.set(i,0);
		actionkey.set(i,false);
	}
	
	public void addProfile(String propertiesFile) {
		if(propertiesFile==null) {
			profiles.add(null);
			dirBuffer.add(0);
			actionkey.add(false);
			return;
		}
		FileInputStream input =null;
		try {
			input=new FileInputStream(propertiesFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        try {
        	Properties properties = new Properties();
			properties.load(input);
			profiles.add(properties);
		} catch (IOException e) {
			e.printStackTrace();
		}
        try {
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		dirBuffer.add(0);
		actionkey.add(false);
	}

	public KeyControl() {
    	Properties properties = new Properties();
		properties.put("CODE_UP", ""+CODE_UP);
		properties.put("CODE_DOWN", ""+CODE_DOWN);
		properties.put("CODE_RIGHT", ""+CODE_RIGHT);
		properties.put("CODE_LEFT", ""+CODE_LEFT);
		properties.put("CODE_ACTION", ""+CODE_ACTION);
		profiles.add(properties);
		dirBuffer.add(0);
		actionkey.add(false);
	}
	

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		lastKey=e.getKeyCode();
		for(int i=0;i<profiles.size();i++) {
			if(e.getKeyCode()==Integer.parseInt((String) (profiles.get(i).get("CODE_UP"))))
				dirBuffer.set(i,dirBuffer.get(i)|1);
			if(e.getKeyCode()==Integer.parseInt((String) (profiles.get(i).get("CODE_DOWN"))))
				dirBuffer.set(i,dirBuffer.get(i)|2);
			if(e.getKeyCode()==Integer.parseInt((String) (profiles.get(i).get("CODE_RIGHT"))))
				dirBuffer.set(i,dirBuffer.get(i)|4);
			if(e.getKeyCode()==Integer.parseInt((String) (profiles.get(i).get("CODE_LEFT"))))
				dirBuffer.set(i,dirBuffer.get(i)|8);
			if(e.getKeyCode()==Integer.parseInt((String) (profiles.get(i).get("CODE_ACTION"))))
				actionkey.set(i, true);

		}
		if(e.getKeyCode()==CODE_PAUSE)
			pauseImpulse=true;
		//System.out.println(Integer.toBinaryString(16+dirBuffer));
		//System.out.println(e.getKeyCode());
	}

	@Override
	public void keyReleased(KeyEvent e) {
		for(int i=0;i<profiles.size();i++) {
			if(e.getKeyCode()==Integer.parseInt((String) (profiles.get(i).get("CODE_UP"))))
				dirBuffer.set(i,dirBuffer.get(i)&14);
			if(e.getKeyCode()==Integer.parseInt((String) (profiles.get(i).get("CODE_DOWN"))))
				dirBuffer.set(i,dirBuffer.get(i)&13);
			if(e.getKeyCode()==Integer.parseInt((String) (profiles.get(i).get("CODE_RIGHT"))))
				dirBuffer.set(i,dirBuffer.get(i)&11);
			if(e.getKeyCode()==Integer.parseInt((String) (profiles.get(i).get("CODE_LEFT"))))
				dirBuffer.set(i,dirBuffer.get(i)&7);
			if(e.getKeyCode()==Integer.parseInt((String) (profiles.get(i).get("CODE_ACTION"))))
				actionkey.set(i, false);
		}
		//System.out.println(Integer.toBinaryString(16+dirBuffer));
	}

	public int MovementX() {
		return MovementX(0);
	}
	public int MovementY() {
		return MovementY(0);
	}

	public boolean action() {
		return action(0);
	}
	
	
	public int MovementX(int profile) {
		if(profile>=profiles.size() || profiles.get(profile)==null)return 0;
		return (dirBuffer.get(profile)/4)%2-(dirBuffer.get(profile)/8);
	}

	public int MovementY(int profile) {
		if(profile>=profiles.size()|| profiles.get(profile)==null)return 0;
		return (dirBuffer.get(profile)/2)%2-dirBuffer.get(profile)%2;
	}

	public boolean action(int profile) {
		if(profile>=profiles.size()|| profiles.get(profile)==null)return false;
		return actionkey.get(profile);
	}
	
	public boolean pauseActivate() {
		if(pauseImpulse) {
			pauseImpulse=false;
			return true;
		}
		return false;
	}

	@Override
	public void updateControl() {
		//nothing to do, keylistener work by themselves
	}

}
