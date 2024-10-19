package bomber;


public interface Control {
	public int MovementX();
	public int MovementY();

	public boolean action();
	
	
	public int MovementX(int profile);

	public int MovementY(int profile);

	public boolean action(int profile);
	
	public boolean pauseActivate();
	
	//if the control need manual update to poll input
	public void updateControl();
}
