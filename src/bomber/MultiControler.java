package bomber;

import java.awt.event.KeyListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class MultiControler implements Control{

	private KeyControl kl;
	private GameControl gl;
	
	private ArrayList<Control> profiles=new ArrayList<Control>();
	private ArrayList<Integer> profilesID=new ArrayList<Integer>();
	
	public void rescan() {
		gl.rescan();
	}
	

	public String getControllerName(int i) {
		return gl.getDevice(i);
	}
	
	public MultiControler(String propertiesFile) {
		kl=new KeyControl();
		gl=new GameControl();
		FileInputStream input =null;
		Properties properties=null;
		try {
			input=new FileInputStream(propertiesFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        try {
        	properties = new Properties();
			properties.load(input);
        } catch (IOException e) {
			e.printStackTrace();
		}
        try {
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        //add profile to keyListener
        int keyProf=Integer.parseInt(properties.getProperty("KEYPROFILES"));
        for(int i=0;i<keyProf;i++) {
        	String file=properties.getProperty("KEYPROF"+i);
        	if(!file.isEmpty())
	        	if(i==0)
	        		kl.setProfile(0, file);//special case if 0
	        	else
	        		kl.addProfile(file);
        }
        
        //setup profiles
        int Nprofiles=Integer.parseInt(properties.getProperty("PROFILES"));
        for(int i=0;i<Nprofiles;i++) {
        	//System.out.println(i);
        	String type=properties.getProperty("P"+i+"TYPE");
        	int id=Integer.parseInt(properties.getProperty("P"+i));
        	profiles.add(type.equals("KEY")?kl:gl);
        	profilesID.add(id);
        }
	}

	public MultiControler(Properties properties) {
		kl=new KeyControl();
		gl=new GameControl();
        //add profile to keyListener
        int keyProf=Integer.parseInt(properties.getProperty("KEYPROFILES"));
        for(int i=0;i<keyProf;i++) {
        	String file=properties.getProperty("KEYPROF"+i);
        	if(!file.isEmpty())
	        	if(i==0)
	        		kl.setProfile(0, file);//special case if 0
	        	else
	        		kl.addProfile(file);
        }
        
        //setup profiles
        int Nprofiles=Integer.parseInt(properties.getProperty("PROFILES"));
        for(int i=0;i<Nprofiles;i++) {
        	//System.out.println(i);
        	String type=properties.getProperty("P"+i+"TYPE");
        	int id=Integer.parseInt(properties.getProperty("P"+i));
        	profiles.add(type.equals("KEY")?kl:gl);
        	profilesID.add(id);
        }
	}

	public KeyListener getKeyListener() {
		return kl;
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

	@Override
	public int MovementX(int profile) {
		return profiles.get(profile).MovementX(profilesID.get(profile));
	}

	@Override
	public int MovementY(int profile) {
		return profiles.get(profile).MovementY(profilesID.get(profile));
	}

	@Override
	public boolean action(int profile) {
		return profiles.get(profile).action(profilesID.get(profile));
	}

	@Override
	public boolean pauseActivate() {
		return kl.pauseActivate()||gl.pauseActivate();
	}

	@Override
	public void updateControl() {
		gl.updateControl();
	}

}
