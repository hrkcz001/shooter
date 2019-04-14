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

	DataInputStream dis;
  DataOutputStream dos;
	Socket s;

	public Boolean setSocket(String ip, int port){

		try{

			s = new Socket (ip,port);
			dis = new DataInputStream(s.getInputStream());
			dos = new DataOutputStream(s.getOutputStream());

			return(true);

		}
		catch(Exception e){

			System.out.println(e);
			return(false);

		}

	}

	public String getServerList(){

		String res;

	  try{

			res = dis.readUTF();
			s.close();
			return(res);

		}
		catch(Exception e) {

			System.out.println(e);
			return("false");

		}

	}

}

class GameServerConnection {

	DataInputStream dis;
  DataOutputStream dos;
  String map;
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
		setLayout(null);

		WaitServerConnecter wsc = new WaitServerConnecter();
		Boolean connectionRes = wsc.setSocket("localhost",6900);

		if(connectionRes){

			  String answ = wsc.getServerList();
				String[] server = answ.split(":");
				String[] tableNames = {"Name", "Port", "Players"};
				String[][] tableData = new String[server.length - 1][3];

				for(int i = 1; i < server.length; i++){

					tableData[i - 1] = server[i].split("/");

				}

				JTable table = new JTable(tableData, tableNames);

				int height = (size.height * 3 / 4) / (server.length - 1);
				if(height < 20){

					height = 20;

				}

				JScrollPane  js = new JScrollPane(table);

				for(int i = 0; i < tableData.length; i++){

					JButton jb = new JButton("Connect");
					jb.setLocation(size.width * 5 / 6 - 1, height * i + height / 4);
					jb.setSize(size.width / 9, height);
					add(jb);

				}

				table.setRowHeight(height);
				table.setLocation(size.width / 6, size.height / 6);
				table.setSize(size.width * 2 / 3, size.height * 3 / 4);
				js.setLocation(size.width / 6, size.height / 6);
				js.setSize(size.width * 2 / 3, size.height * 4 / 5);

				table.getColumnModel().getColumn(0).setPreferredWidth(size.width / 3);
				table.getColumnModel().getColumn(1).setPreferredWidth(size.width / 6);
				table.getColumnModel().getColumn(2).setPreferredWidth(size.width / 6);

				add(js);

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
