import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.TextArea;
import java.awt.TextField;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneLayout;
import javax.swing.JLabel;

// Logout code: #logout4242

public class Main {
    public static void main(String[] args) {
        System.out.println("HelloWorld!");
        ComunicatorServer server = new ComunicatorServer(4242);
        server.startThread();
        ComunicatorServerGui gui = new ComunicatorServerGui(server);
        gui.startThread();
    }
}

class Message {
    public String command;
    public String nickname;
    public String message;

    public Message(String stringMessage){
        stringMessage.trim();
        this.command = new String(stringMessage.substring(0, stringMessage.indexOf(" ")));
        stringMessage = new String(stringMessage.substring(stringMessage.indexOf(" ")+1));
        this.nickname = new String(stringMessage.substring(0, stringMessage.indexOf(" ") ));
        stringMessage = new String(stringMessage.substring(stringMessage.indexOf(" ")+1));
        this.message = stringMessage;
    }

    public void print() {
        System.out.println("[" + this.command + "][" + this.nickname + "]: " + this.message );
    } 

}

class ComunicatorServer {
    
    private int tcp;
    private ServerSocket serverSocket;
    private ArrayList<Socket> clients;
    public ArrayList<Message> messagesList;
    
    public ComunicatorServer(int tcp){
        this.tcp = tcp;
        this.clients =  new ArrayList<Socket>();
        this.messagesList = new ArrayList<Message>();

        try {
            this.serverSocket = new ServerSocket(4242);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startThread(){
        
        this.startListningForConnections();
        this.startListningForMessages();
    }
    public void startListningForConnections() {
        ConnectionListner connectionListner = new ConnectionListner(clients, serverSocket);
        try {
            Thread lsitnerThread = new Thread(connectionListner);
            lsitnerThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startListningForMessages(){
        MessageMennager messageMennager = new MessageMennager(this);
        messageMennager.startThread();
    }

    public ArrayList<Socket> getClientsList() {
        return this.clients;
    }

    public ServerSocket getServerSocket() {
        return this.serverSocket;
    }

}

class ConnectionListner implements Runnable {
    
    ArrayList<Socket> clients;
    ServerSocket serverSocket;

    public ConnectionListner(ArrayList<Socket> clients, ServerSocket serverSocket){
        this.clients = clients;
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        while(true) {

            try {
                Socket clientSocket = this.serverSocket.accept();                
                this.clients.add(clientSocket);
                System.out.println("Connection get: " + clientSocket);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            
        }
    }
}

class MessageMennager implements Runnable {
    
    private ArrayList<Socket> clients;
    private ServerSocket serverSocket;
    Message message;
    private ComunicatorServer server;

    public MessageMennager(ComunicatorServer server) {
        this.clients = server.getClientsList();
        this.serverSocket = server.getServerSocket();
        this.message = null;
        this.server = server;
    }

    @Override
    public void run() {
        this.messageMenage();
    }

    public void startThread() {
        try {
            Thread thread = new Thread(this);
            thread.start(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void messageMenage() {
        System.out.println("Started!");
        while(true) {
            
            ArrayList<Socket> clientsArray = new ArrayList<Socket>(this.server.getClientsList());
            for(Socket s : clientsArray){
                synchronized(this) {
                    try {
                        InputStreamReader streamReader = new InputStreamReader(s.getInputStream());
                        BufferedReader reader = new BufferedReader(streamReader);    
                        
                        if(reader.ready()) {
                            Message message = new Message(reader.readLine());
                       
                            if (message != null) {
                                message.print();
                                server.messagesList.add(message);
                                this.codeAction(message, s);
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void codeAction(Message message, Socket socket) {
        String codeAction = message.command;
        switch (codeAction) {
            case "#message4242":
                this.sendToAll(message);
                break;

            case "#logout4242":
                this.logoutClient(message);
                this.clients.remove(socket);
                break;
        
            default:
                break;
        }
    }

    public void sendToAll(Message message){
        for(Socket i : clients) {
            try {
                OutputStreamWriter streamWriter = new OutputStreamWriter(i.getOutputStream());
                PrintWriter writer = new PrintWriter(streamWriter);
                String nickname = message.nickname;
                String messageToSend = new String("[" + message.nickname + "]: " + message.message);
                writer.println(messageToSend);
                writer.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void logoutClient(Message message) {

    }
}

class ConnectionInfoMenager implements Runnable {
    private ArrayList<Socket> clients;

    public ConnectionInfoMenager(ArrayList<Socket> clients) {
        this.clients = clients;
    }

    @Override
    public void run() {
        
    }

    public void showClients() {
        System.out.println("Connected clients:");
        for(Socket i : clients) {
            System.out.println(i);
        }
    }

    
}

class ComunicatorServerGui implements Runnable {
    
    private ComunicatorServer server;
    JFrame frame;
    JPanel mainPanel;
    BoxLayout boxlayout;

    JScrollPane scrollPanelConnections;
    JTextArea clientsTextArea;

    JScrollPane scrollPaneMessages;
    JTextArea messagesTextArea;


    JPanel commandPanel;
    JLabel commandSign;
    JTextField commandTextField;
    JButton executeCommandButton;

    MessagesDispalyMenager messagesDispalyMenager;


    public ComunicatorServerGui(ComunicatorServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        this.menageGui();
        ConnectionDisplayMenager connectionDisplayMenager = new ConnectionDisplayMenager();
        connectionDisplayMenager.startThread();
        this.messagesDispalyMenager = new MessagesDispalyMenager();
        messagesDispalyMenager.startThread();
    }

    public void startThread() {
        try {
            Thread thread = new Thread(this);
            thread.start();  
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void menageGui() {
        this.frame = new JFrame();
        this.frame.setSize(420, 420);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        this.mainPanel = new JPanel();
        this.mainPanel.setLayout(new BoxLayout(this.mainPanel, BoxLayout.Y_AXIS));
        this.frame.getContentPane().add(BorderLayout.CENTER, this.mainPanel);
        this.frame.setTitle("Communicates Server");

        this.mainPanel.add(Box.createVerticalStrut(10));

        this.clientsTextArea = new JTextArea(30,5);
        this.clientsTextArea.setEditable(false);
        this.scrollPanelConnections = new JScrollPane(this.clientsTextArea);
        this.scrollPanelConnections.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.scrollPanelConnections.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        Dimension connectionScrollPaneDimension = new Dimension(400,200);
        this.scrollPanelConnections.setPreferredSize(connectionScrollPaneDimension);
        this.scrollPanelConnections.setMaximumSize(connectionScrollPaneDimension);
        this.mainPanel.add(this.scrollPanelConnections);

        this.mainPanel.add(Box.createVerticalStrut(10));

        this.messagesTextArea = new JTextArea(30,5);
        this.clientsTextArea.setEditable(false);
        this.scrollPaneMessages = new JScrollPane(this.messagesTextArea);
        this.scrollPaneMessages.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.scrollPaneMessages.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        Dimension messagesScrollPaneDimension = new Dimension(400,200);
        this.scrollPaneMessages.setPreferredSize(messagesScrollPaneDimension);
        this.scrollPaneMessages.setMaximumSize(messagesScrollPaneDimension);
        this.mainPanel.add(this.scrollPaneMessages);

        this.mainPanel.add(Box.createVerticalStrut(10));

        this.commandPanel = new JPanel();
        this.commandPanel.setLayout(new BoxLayout(this.commandPanel, BoxLayout.X_AXIS));
        this.mainPanel.add(this.commandPanel);

        this.commandSign = new JLabel(">>");
        this.commandPanel.add(commandSign);
        this.commandPanel.add(Box.createHorizontalStrut(15));


        this.commandTextField = new JTextField();
        Dimension commandTextFieldSize = new Dimension(200, 30);
        this.commandTextField.setPreferredSize(commandTextFieldSize);
        this.commandTextField.setMaximumSize(commandTextFieldSize);
        this.executeCommandButton = new JButton("Execute");
        this.commandPanel.add(this.commandTextField);
        this.commandPanel.add(Box.createHorizontalStrut(15));
        this.commandPanel.add(this.executeCommandButton);

        this.mainPanel.add(Box.createVerticalStrut(10));

        this.frame.setVisible(true);
    }

    class ConnectionDisplayMenager implements Runnable {
        @Override
        public void run() {
            while(true) {
                
                clientsTextArea.setText(null);

                for(Socket i : server.getClientsList()) {
                    clientsTextArea.append(new String("Client: " + i + "\n"));
                }

                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
           
        }

        public void startThread() {
            try {
                Thread thread = new Thread(this);
                thread.start();    
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }
    }

    class MessagesDispalyMenager implements Runnable {

        @Override
        public void run() {
            this.displayMessage();
        }

        public void startThread() {
            Thread thread = new Thread(this);
            thread.start();
        }

        public void displayMessage() {
            while (true) {
                synchronized(this) {
                    ArrayList<Message> messageTmpList = new  ArrayList<Message>(server.messagesList);
                    if(server.messagesList.isEmpty() != true) {        
                        for(Message i : messageTmpList) {
                            messagesTextArea.append(new String("[" + i.nickname + "]: " + i.message + "\n"));
                        }
                        server.messagesList.clear();
                    }
                }
                
                try {
                    Thread.sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }


}