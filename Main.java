import java.awt.*;
import javax.swing.*;

public class Main{

    public static void main(String[] args){

      JFrame frame = new JFrame();
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      Dimension size = Toolkit.getDefaultToolkit ().getScreenSize ();

      frame.setSize(size);
      frame.setLocation(0, 0);
      frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
      frame.setUndecorated(true);

      Screen screen = new Screen();

      frame.getContentPane().add(screen);

      frame.setVisible(true);

    }

}

class Screen extends Canvas{

  public Screen(){

    super();

  }

}
