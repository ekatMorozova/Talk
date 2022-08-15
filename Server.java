import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.LinkedList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Server
{
	public static final int PORT = 8080;
	public static LinkedList<ServerPr> Users = new LinkedList<>();
	public static Story story;
	
	public static void main(String[] args) throws IOException
	{
		new GUI();
		ServerSocket server = new ServerSocket(PORT);
		story = new Story();
		GUI.IO.printMsg("Server started...");
		try
		{
			while(true)
			{
			    Socket socket = server.accept();
			    try
			    {
			    	Users.add(new ServerPr(socket));
			    }
			    catch(IOException ex)
			    {
			    	socket.close();
			    }
			}
		}
		finally
		{
			server.close();
		}
	}
}

class ServerPr extends Thread
{
	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;
	private String nickname;
	
	public ServerPr(Socket socket) throws IOException
	{
		this.socket = socket;
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		Server.story.printStory(out);
		start();
	}
	
	@Override
	public void run()
	{
		String str;
		try
		{
			str = in.readLine();
			nickname = str;
			GUI.IO.printMsg("New user " + nickname);
			try
			{
				while(true)
				{
					str = in.readLine();
					if(str.equals("STOP"))
					{
						this.closeSocket();
						break;
					}
					if(!str.equals(null))
					{
						GUI.IO.printMsg("Echoing: " + str);
						Server.story.addStoryEl(str);
						for(ServerPr pr : Server.Users)
						{
							pr.send(str);
						}
					}
				}
			}
			catch(NullPointerException ignored)
			{
				
			}
		}
		catch(IOException ex)
		{
			this.closeSocket();
		}
	}
	
	public void send(String str)
	{
		try
		{
			out.write(str + "\n");
			out.flush();
		}
		catch(IOException ignored)
		{
			
		}
	}
	
	public void closeSocket()
	{
		try
		{
			if(!socket.isClosed()) 
    		{
    			socket.close();
    			in.close();
    			out.close();
    			for(ServerPr pr : Server.Users) 
    			{
    				if(pr.equals(this)) 
    				pr.interrupt();
    				Server.Users.remove(this);
    			}
    		}
		}
		catch(IOException ignored)
		{
			
		}
	}
}

class Story 
{   
    private LinkedList<String> story = new LinkedList<>();
    public void addStoryEl(String el) 
    {
        if (story.size() >= 10) 
        {
            story.removeFirst();
            story.add(el);
        }
        else 
        {
            story.add(el);
        }
    }
   
    public void printStory(BufferedWriter writer) 
    {
        if(story.size() > 0) 
        {
            try 
            {
                writer.write("History messages" + "\n");
                for (String vr : story) 
                {
                    writer.write(vr + "\n");
                }
                writer.write("/...." + "\n");
                writer.flush();
            }
            catch (IOException ignored) 
            {
            	
            }
            
        }
        
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
				IO.readMsg();
				IO.printMsg(IO.str);
			}
	    });
		this.lab = new JLabel("Enter: ");
		lab.setBounds(20, 426, 80, 25);
		this.area = new JTextArea();
		area.setLineWrap(true);
		area.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(area);
		scrollPane.setBounds(0, 0, 485, 420);
		add(lab);
		add(button);
		add(text);
		add(scrollPane);
		setTitle("Server");
		setSize(500, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	class IO
	{
		private static String str;
		
		static private String readMsg()
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
