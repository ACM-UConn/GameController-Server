package GameControllerServer;

import java.awt.Robot;

// Use Bash scripts to control the current window and play game
// Goals: use Bash script to make game window the focus, and use button bindings passed from Binding to play the game.

public class SystemControl {
    int x = 0;
    
    public static void main(String[] args) {
        SystemControl myObj = new SystemControl();
        System.out.println(myObj.x);
    }
}
