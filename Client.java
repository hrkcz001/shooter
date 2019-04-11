import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;

class Client {

	public static void main (String [] args) throws Exception {

    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();

    Wind frame = new Wind(size);

  }

}

class WaitServerConnecter {
	DataInputStream dis;
	DataOutputStream dos;
  Scanner sc;
	Socket s;
	public WaitServerConnecter(String ip, int port) {
		try {
		sc = new Scanner(System.in);
		s = new Socket (ip,port);
		dis = new DataInputStream(s.getInputStream());
		dos = new DataOutputStream(s.getOutputStream());
	  }
		catch (Exception e) {
			System.out.println(e);
		}
	}
	public String getServerList (String name) {
		String req = name;
		String res = "";
	  try {
			dos.writeUTF(req);
			res = dis.readUTF();
			closeSocket();
			return res;
		}
		catch (Exception e) {
			System.out.println(e);
			return "false";
		}
	}
	public void closeSocket () {
		try {
		sc.close();
		s.close();
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
}

class GameServerConnection {
	DataInputStream dis;
	DataOutputStream dos;
  String map;
	public GameServerConnection (String ip, int port) {
		try {
		Scanner sc = new Scanner(System.in);
		Socket s = new Socket (ip,port);
		dis = new DataInputStream(s.getInputStream());
		dos = new DataOutputStream(s.getOutputStream());
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}

	public void enterServer (String username) {
		try {
		dos.writeUTF(username);
		String answ = dis.readUTF();
		System.out.println(answ);
		//дальнейшие действия
		}
	  catch (Exception e) {
	  	System.out.println(e);
	  }
	}

	public void downloadMap () {
		try {
			System.out.println("Downloading the Map");
			//
			int length = Integer.parseInt(dis.readUTF());
			System.out.println(length);
			String[] map = new String[length];
			for (int i = 0;i<length; i++) {
				map[i] = dis.readUTF();
				//System.out.println(map[i]);
			}
			//
			System.out.println("Downloading of the Map is done");
		}
		catch (Exception e) {
			System.out.println("Downloading of the Map failed");
			System.out.println(e);
		}

	}
}

class Screen extends JPanel{

    public void paintComponent(Graphics g){

      g.setColor(Color.ORANGE);
      g.drawRect(0, 0, 200, 200);

    }

}

class Wind extends JFrame{

  public Wind(Dimension size) throws Exception{

    super("Shooter");
    setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setUndecorated(true);
    setResizable(false);
    setLayout(null);

    MainPane main = new MainPane(size);

    add(main);

    setVisible(true);
    main.setVisible(true);

  }

}

class MainPane extends JPanel{

  public MainPane(Dimension size) throws Exception{

    super();
    setSize(size);
    setLocation(0, 0);
    setBackground(Color.GREEN);

    String answ = new WaitServerConnecter("localhost",6900).getServerList("Valera");
		if(!answ.equals("false")){

		    String[] server = answ.split(":");

    }
    else{

     JLabel refused = new JLabel("Не установлено соединение с сервером. \n Проверьте подключение к интернету и повторите попытку.");
     refused.setFont(new Font("Arial", Font.BOLD, size.height / 50));
     refused.setForeground(Color.RED);
     refused.setLocation(size.width / 3, size.height / 2);
     add(refused);

    }


  }

}
