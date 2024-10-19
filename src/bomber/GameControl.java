package bomber;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

//je ne savais pas comment récuperer les inputs d'une mannette donc j'ai fait appel à chat GPT, 
//donc le code est peut être mal optimisé et sujet à être amélioré
public class GameControl implements Control {
	
	// This method forces the JInput ControllerEnvironment to refresh using reflection
    private void forceControllerEnvironmentRefresh() {
        try {
            ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
            
            // Access private "controller" field via reflection
            Field field = ControllerEnvironment.class.getDeclaredField("controllers");
            field.setAccessible(true); // allow access to private field

            // Set the value to null to reset it
            field.set(ce, null);

            // Invoke the protected method to reinitialize controllers
            Method method = ControllerEnvironment.class.getDeclaredMethod("getControllers");
            method.setAccessible(true);
            method.invoke(ce);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to refresh ControllerEnvironment");
        }
    }
	
	public ArrayList<Controller> controls=new ArrayList<Controller>();
	private ArrayList<Boolean> actionkey=new ArrayList<Boolean>();
	private ArrayList<Integer> dirBuffer=new ArrayList<Integer>();
	private boolean pauseImpulse=false;
	private boolean pausePressed=false;

	private static final String MOVEX="x";
	private static final String MOVEY="y";
	private static final String PAUSE="7";
	private static final String ACTION="2";
	
	private static final float DEADZONE=0.2f;

	
	public String getDevice(int i) {
		
		return i<controls.size()?controls.get(i).getName():"null";
	}
	
	public void rescan() {
		forceControllerEnvironmentRefresh();
		System.setProperty("net.java.games.input.useDefaultPlugin", "true");

        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();

        if (controllers.length == 0) {
            System.out.println("No controllers found!");
            return;
        }

        // Loop through controllers and find a game controller
        for (Controller controller : controllers) {
            if (controller.getType() == Controller.Type.GAMEPAD || controller.getType() == Controller.Type.STICK) {
                System.out.println("Found controller: " + controller.getName());
                controls.add(controller);
                actionkey.add(false);
                dirBuffer.add(0);
               
            }
        }
	}
	
	public GameControl(){
        // Get the list of all connected controllers
	 	System.setProperty("net.java.games.input.useDefaultPlugin", "true");
	 	
	 	
        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();

        if (controllers.length == 0) {
            System.out.println("No controllers found!");
            return;
        }

        // Loop through controllers and find a game controller
        for (Controller controller : controllers) {
            if (controller.getType() == Controller.Type.GAMEPAD || controller.getType() == Controller.Type.STICK) {
                System.out.println("Found controller: " + controller.getName());
                controls.add(controller);
                actionkey.add(false);
                dirBuffer.add(0);
               
            }
        }

	}
	
	 public static void main(String[] args) {
		 	GameControl test=new GameControl();
		 	while(true) {
		 		test.updateControl();
                // Optional: add a delay to prevent CPU overload
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
		 	}
	    }
	 
	 public void updateControl() {
		 for(int i=0;i<controls.size();i++) {
			 controls.get(i).poll();
             Component[] components = controls.get(i).getComponents();
             int newDir=0;
             for (Component component : components) {
                 Component.Identifier id = component.getIdentifier();

                 float value = component.getPollData();
                 if (component.isAnalog()) {
                	 if(id.getName()==MOVEX && Math.abs(value) > DEADZONE) 
                		 newDir+=value>0?4:8;
                	 if(id.getName()==MOVEY && Math.abs(value) > DEADZONE)
                		 newDir+=value>0?2:1;
                 } else {
                	 //pause button check
                	 if(id.getName()==PAUSE) {
                		 if(value== 1.0f) {
                			 if(pausePressed)
                				 pauseImpulse=false;
                			 else {
                				 pausePressed=true;
                				 pauseImpulse=true;
                			 }
                		 }else
                			 pausePressed=false;
                	 }
                	 //action reading
                	 if(id.getName()==ACTION) {
                		 actionkey.set(i,value==1.0f);
                	 }
                 }
             }
             dirBuffer.set(i, newDir);
             // System.out.println("direction : "+newDir);
		 }
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
		if(profile>=controls.size())return 0;
		return (dirBuffer.get(profile)/4)%2-(dirBuffer.get(profile)/8);
	}

	public int MovementY(int profile) {
		if(profile>=controls.size())return 0;
		return (dirBuffer.get(profile)/2)%2-dirBuffer.get(profile)%2;
	}


	public boolean action(int profile) {
		if(profile>=controls.size())return false;
		return actionkey.get(profile);
	}

	@Override
	public boolean pauseActivate() {
		return pauseImpulse;
	}

}
