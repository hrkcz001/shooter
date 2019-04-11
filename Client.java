import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;

class Client {

	public static void main (String [] args) throws Exception{

    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();

    Wind frame = new Wind(size);

  }

}

class WaitServerConnecter{

	final DataInputStream dis;
  final DataOutputStream dos;
	final Socket s;

	public WaitServerConnecter(String ip, int port) throws Exception{

		s = new Socket (ip,port);
		dis = new DataInputStream(s.getInputStream());
		dos = new DataOutputStream(s.getOutputStream());

	}

	public String getServerList(){

		String res;

	  try{

			res = dis.readUTF();
			s.close();
			return(res);

		}
		catch(Exception e) {

			return("false");

		}

	}

}

class GameServerConnection {

	final DataInputStream dis;
	final DataOutputStream dos;
  final String map;
	String username = "PLAYER";

	public GameServerConnection(String ip, int port) throws Exception{

		Scanner sc = new Scanner(System.in);
		Socket s = new Socket (ip,port);
		dis = new DataInputStream(s.getInputStream());
		dos = new DataOutputStream(s.getOutputStream());

	}

	public void enterServer() throws Exception {

		dos.writeUTF(username);
		String answ = dis.readUTF();
		System.out.println(answ);

		//

	}

	public void downloadMap() throws Exception {

			int length = Integer.parseInt(dis.readUTF());
			System.out.println(length);
			String[] map = new String[length];
			for (int i = 0;i<length; i++) {

				map[i] = dis.readUTF();

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

    String answ = new WaitServerConnecter("25.68.140.53",6900).getServerList();
		if(!answ.equals("false")){

				String[] server = answ.split(":");

				JTable table = new JTable(server);

				table.setSize(size.height * 3 / 4, size.width);
				table.setLocation(0, 0);

    }
    else{

     JLabel refused = new JLabel("Не установлено соединение с сервером. Проверьте подключение к интернету и повторите попытку.");
     refused.setFont(new Font("Arial", Font.BOLD, size.height / 50));
     refused.setForeground(Color.RED);
     refused.setLocation(size.width / 3, size.height / 2);
     add(refused);

    }


  }

}
