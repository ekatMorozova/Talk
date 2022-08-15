import java.net.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.*;

public class Client
{
	public static String ipAddr = "localhost";
	public static int port = 8080;
	public static void main(String[] args)
	{
		new ClientPr(ipAddr, port);
	}
}

class ClientPr
{
	private Socket socket;
	BufferedReader in;
	static BufferedWriter out;
	private String addr;
	private int port;
	static String nickname;
	public ClientPr(String addr, int port)
	{
		this.addr = addr;
		this.port = port;
		try 
		{
			this.socket = new Socket(addr, port);
		}
		catch (IOException ex) 
		{
			System.err.println("Socket failed.");
			ex.printStackTrace();
		}
		try
		{
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			new GUI();
		}
		catch(IOException ex)
		{
			ClientPr.this.closeSocket();
		}
	}
	
	class GUI extends JFrame
	{
		private static JTextField text;
		private static JButton button;
		private static JTextArea area;
		private static JLabel lab;
		
		GUI()
		{
			initUI();
		}
		private void initUI()
		{
			setLayout(null);
			this.text = new JTextField();
			text.setBounds(70, 430, 340, 20);
			this.button = new JButton("Send");
			button.setBounds(410, 430, 70, 20);
			button.addActionListener(new ActionListener() 	
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					if(nickname == null)
					{
						try
						{
							nickname = GUI.IO.readMsg();
							GUI.IO.printMsg(nickname);
							GUI.IO.printMsg("Hello " + nickname);
							out.write(nickname + "\n");
							out.flush();
							new ReadServerMsg().start();
						}
						catch(IOException ex)
						{
							System.err.println("Nickname failed.");
						}
					}
					else
					{
						String str;
						try
						{
							str = GUI.IO.readMsg();
							if(str.equals("STOP"))
							{
								ClientPr.out.write("Client " + ClientPr.nickname + " left the chat\n");
								ClientPr.this.closeSocket();
							}
							else
							{
								out.write(nickname + " >> " + str + "\n");
							}
							out.flush();
						}
						catch(IOException ex)
						{
							ClientPr.this.closeSocket();
						}
					}
				}
		    });
			this.lab = new JLabel("Enter: ");
			lab.setBounds(20, 426, 80, 25);
			this.area = new JTextArea("Enter your nickname: ");
			area.setLineWrap(true);
			area.setEditable(false);
			JScrollPane scrollPane = new JScrollPane(area);
			scrollPane.setBounds(0, 0, 485, 420);
			add(lab);
			add(button);
			add(text);
			add(scrollPane);
			setTitle("Client");
			setSize(500, 500);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setLocationRelativeTo(null);
			setVisible(true);
			setResizable(false);
		}
		
		class IO
		{
			private static String str;
			
			static String readMsg()
			{
				str = text.getText();
				return str;
			}
			static void printMsg(String str)
			{
				area.append(str);
				area.append("\n");
				text.setText(null);
			}
		}
	} 
	
	private class ReadServerMsg extends Thread
	{
		@Override
		public void run()
		{
			String str;
			try
			{
				while(true)
				{
					str = in.readLine();
					if (str.equals("STOP")) 
					{
						ClientPr.this.closeSocket();
						break;
				    }
					if(!str.equals(null))
					{
						GUI.IO.printMsg(str);
					}
				}
			}
			catch(IOException ex)
			{
				System.err.println("Reading failed");
				ClientPr.this.closeSocket();
			}
		}
	}
	
	private void closeSocket()
	{
		try
		{
			socket.close();
			in.close();
			out.close();
		}
		catch(IOException ex)
		{
			System.err.println("Socket closing failed");
			ex.printStackTrace();
		}
	}
}
