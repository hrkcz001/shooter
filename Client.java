import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

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

	public void enterServer(String username) throws Exception {

		this.username = username;
		dos.writeUTF(username);
		String answ = dis.readUTF();
		System.out.println(answ);

	}

	public void downloadMap() throws Exception {

			int length = Integer.parseInt(dis.readUTF());
			System.out.println(length);
			String[] map = new String[length];
			for (int i = 0;i<length; i++) {

				map[i] = dis.readUTF();
				System.out.println(map[i]);

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

    MainPane main = new MainPane(this, size);

    add(main);
    setVisible(true);
    main.setVisible(true);

  }

}

class MainPane extends JPanel{

	Wind wind;
	Dimension size;
	int currentX;
	int currentY;
	JPanel startGamePanel;

  public MainPane(Wind wind, Dimension size) throws Exception{

    super();
    setSize(size);
    setLocation(0, 0);
    setBackground(new Color(100, 200, 100));
		setLayout(null);
		this.wind = wind;
		this.size = size;

		createServerTable();

		JButton startButton = new JButton("Start");
		startButton.setLocation(0, 0);
		startButton.setSize(100, 50);
		startButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent ae){

				startGamePanel.setLocation(size.width / 6, size.height / 10);
				startGamePanel.setVisible(true);

			}

		});

		add(startButton);

  }

	public void createServerTable(){

		startGamePanel = new JPanel();

		startGamePanel.setLocation(size.width / 6, size.height / 10);
		startGamePanel.setSize(size.width * 2 / 3, size.height * 3 / 4 + size.height / 20);
		startGamePanel.setBackground(new Color(255, 100, 255));
		startGamePanel.setLayout(null);

		startGamePanel.addMouseListener(new MouseAdapter(){

			public void mousePressed(MouseEvent e) {
					currentX = e.getX();
					currentY = e.getY();
			}

			public void mouseDragged(MouseEvent e) {
					startGamePanel.setLocation(startGamePanel.getLocation().x + e.getX() - currentX, startGamePanel.getLocation().y + e.getY() - currentY);
			}

		});
		startGamePanel.addMouseMotionListener(new MouseMotionAdapter() {
							public void mouseDragged(MouseEvent e) {

									startGamePanel.setLocation(startGamePanel.getLocation().x + e.getX() - currentX,
													startGamePanel.getLocation().y + e.getY() - currentY);
							}
		});

		JLabel close = new JLabel("close");
		close.setForeground(new Color(150, 255, 150));
		close.setLocation(size.width * 2 / 3 - size.width / 40, size.height / 640);
		close.setSize(size.width / 20, size.height / 40);
		close.setFont(new Font("Arial", Font.BOLD, size.height / 60));
		close.addMouseListener(new MouseAdapter(){

			public void mouseClicked(MouseEvent me){

				startGamePanel.setVisible(false);

			}

		});
		startGamePanel.add(close);

		JLabel reload = new JLabel("reload");
		reload.setForeground(new Color(150, 255, 150));
		reload.setLocation(size.width * 2 / 3 - size.width / 15, size.height / 640);
		reload.setSize(size.width / 20, size.height / 40);
		reload.setFont(new Font("Arial", Font.BOLD, size.height / 60));
		reload.addMouseListener(new MouseAdapter(){

			public void mouseClicked(MouseEvent me){

				Point curPoint = startGamePanel.getLocation();
				startGamePanel.setVisible(false);
				createServerTable();
				startGamePanel.setLocation(curPoint);
				startGamePanel.setVisible(true);

			}

		});
		startGamePanel.add(reload);

		WaitServerConnecter wsc = new WaitServerConnecter();
		Boolean connectionRes = wsc.setSocket("localhost",6900);

		if(connectionRes){

				JTextArea jta = new JTextArea("Player");
				jta.setSize(size.width * 2 / 3, size.height / 40);
				jta.setLocation(0, size.height / 40);
				jta.setBackground(new Color(150, 255, 150));
				jta.setForeground(new Color(255, 100, 255));
				startGamePanel.add(jta);

			  String answ = wsc.getServerList();
				String[] server = answ.split(":");
				String[][] tableData = new String[server.length - 1][3];

				for(int i = 1; i < server.length; i++){

					tableData[i - 1] = server[i].split("/");

				}

				TableModel tm = new DefaultTableModel(){
  				public boolean isCellEditable(int rowIndex, int columnIndex){
      			return(false);
  				}
					public Class<?> getColumnClass(int columnIndex) {
    				return(String.class);
					}
					public int getColumnCount() {
    				return(3);
					}
					public String getColumnName(int columnIndex) {
    				switch (columnIndex) {
        			case 0:
            			return "Name";
        			case 1:
            			return "Port";
        			case 2:
            			return "Players";
        		}
    				return "";
					}
					public int getRowCount() {
    				return(tableData.length);
					}
					public Object getValueAt(int rowIndex, int columnIndex){
						return(tableData[rowIndex][columnIndex]);
					}
				};

				JTable table = new JTable(tm);
				table.getColumnModel().setColumnSelectionAllowed(false);

				int height = size.height / 45;

				JPanel serverPanel = new JPanel();
				JScrollPane js = new JScrollPane(serverPanel);

				for(int i = 0; i < tableData.length; i++){

					JButton jb = new JButton("Connect");
					jb.setLocation(size.width * 2 / 3 - size.width / 9, height * i);
					jb.setSize(size.width / 9 - size.width / 150, height);
					final int tmp = i;
					jb.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent ae){

							try{
								setVisible(false);
								new ServerConnectionThread(Integer.parseInt(tableData[tmp][1]), jta.getText());
							}
							catch(Exception e){
								System.out.print(e);
							}

						}
					});

					serverPanel.add(jb);

				}

				table.setRowHeight(height);
				table.setLocation(0, 0);
				table.setSize(size.width * 2 / 3 - size.width / 9, height * tableData.length);
				serverPanel.add(table);
				serverPanel.setLayout(null);
				serverPanel.setPreferredSize(new Dimension(size.width * 2 / 3, height * tableData.length));
				js.setLocation(0, size.height / 20);
				js.setSize(size.width * 2 / 3, size.height * 3 / 4);
				js.getVerticalScrollBar().setPreferredSize(new Dimension(size.width / 150, 0));

				js.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				js.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

				table.getColumnModel().getColumn(0).setPreferredWidth(size.width / 3 - size.width / 27);
				table.getColumnModel().getColumn(1).setPreferredWidth(size.width / 6 - size.width / 27);
				table.getColumnModel().getColumn(2).setPreferredWidth(size.width / 6 - size.width / 27);

				startGamePanel.add(js);

				startGamePanel.setVisible(false);

				add(startGamePanel);

    }
    else{

     JLabel refused = new JLabel("Не установлено соединение с сервером");
     refused.setFont(new Font("Arial", Font.BOLD, size.height / 50));
     refused.setForeground(Color.RED);
     refused.setLocation(size.width / 5, size.height * 3 / 8 + size.height / 40);
		 refused.setSize(size.width, size.height / 50);
     startGamePanel.add(refused);

		 startGamePanel.setVisible(false);
		 add(startGamePanel);

    }

	}

}

class ServerConnectionThread extends Thread{

	int port;
	String username;

	public ServerConnectionThread(int port, String username){

		this.port = port;
		this.username = username;
		start();

	}

	public void run(){

		try{

			GameServerConnection gsc = new GameServerConnection("localhost", port);
			gsc.enterServer(username);
			gsc.downloadMap();

		}
		catch(Exception e){

			System.out.print(e);

		}

	}

}
