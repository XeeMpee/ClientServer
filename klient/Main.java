import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.plaf.DimensionUIResource;

// Logout code:             #logout4242
// Normal message code:     #message4242 

public class Main {
    public static void main(String[] args) {
        System.out.println("HelloWorld!");
        Client client = new Client("127.0.0.1", 4242);
        client.connectToTheServer();
        ClientGui clientGui = new ClientGui(client);
        clientGui.runThread();
        ClientServerMessagesMenager clientServerMessagesMenager = new ClientServerMessagesMenager(client);
        clientServerMessagesMenager.startThread();
    }
}

class Client {
    Socket socket;
    String ip;
    int tcp;
    InputStreamReader inputStream;
    OutputStreamWriter outputStream;
    BufferedReader bufferedReader;
    PrintWriter writer;
    String nickname;
    ArrayList<String> messageQueue;


    public Client(String ip, int tcp) {
        this.ip = ip;
        this.tcp = tcp;
        this.nickname = "Anonimous";
        this.messageQueue = new ArrayList<String>();
    }

    public void connectToTheServer(){

        boolean connected = false;

        while (connected == false) {
            try {
                this.socket = new Socket(this.ip, this.tcp);
                connected = true;
            } catch (ConnectException e) {

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



        try {
            this.inputStream = new InputStreamReader(this.socket.getInputStream());
            this.outputStream = new OutputStreamWriter(this.socket.getOutputStream());
            this.bufferedReader = new BufferedReader(this.inputStream);
            this.writer = new PrintWriter(this.outputStream);
        } catch (Exception e) {
            System.out.println("Stream inizialisation error!");
            e.printStackTrace();
        }
    }


    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void sendMessage(String message) {
        String code = "#message4242";
        String nickname = this.nickname;
        this.writer.println(code + " " + nickname + " " + message);
        System.out.println("Send message: " + code + " " + nickname + " " + message);
        this.writer.flush();
    }

   class messagesListner implements Runnable {
        @Override
        public void run() {
            
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
}

class ClientGui implements Runnable {
    private Client client;
    private JFrame frame;

    private JPanel mainPanel;
    private JPanel textPanel;
    private JPanel messagePanel;

    private JTextArea messagesArea;
    private JScrollPane messageScrollPane;

    private JTextField nickanmeField; 

    private JTextField toSendTextField;
    private JButton sendButton;

    private boolean sendFieldFocus;

    public ClientGui(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        this.startGui();    
    }

    public void runThread() {
        try {
            MessagesDisplayMenager messagesDisplayMenager = new MessagesDisplayMenager();
            messagesDisplayMenager.startThread();
            Thread thread = new Thread(this);
            thread.start();    
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    private void startGui() {
        this.frame = new JFrame();
        this.frame.setSize(400,300);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setTitle("Comunicataor client");

        this.mainPanel = new JPanel();
        this.mainPanel.setLayout(new BoxLayout(this.mainPanel, BoxLayout.Y_AXIS));
        this.frame.getContentPane().add(this.mainPanel);

        this.messagePanel = new JPanel();
        this.mainPanel.add(this.messagePanel);

        this.messagePanel.add(Box.createVerticalStrut(10));

        this.messagePanel.setLayout(new BoxLayout(this.messagePanel, BoxLayout.Y_AXIS));
        this.messagesArea = new JTextArea(15,10);
        this.messagesArea.setEditable(false);
        
        Dimension messagesAreaDimension = new Dimension(360,200);
        this.messageScrollPane = new JScrollPane(this.messagesArea);
        this.messageScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.messageScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.messageScrollPane.setPreferredSize(messagesAreaDimension);
        this.messageScrollPane.setMaximumSize(messagesAreaDimension);
        this.messagePanel.add(this.messageScrollPane);

        this.messagePanel.add(Box.createVerticalStrut(10));

        this.textPanel = new JPanel();
        this.textPanel.setLayout(new BoxLayout(this.textPanel, BoxLayout.X_AXIS));
        this.mainPanel.add(this.textPanel);

        this.nickanmeField = new JTextField("Anonimous");
        Dimension nicknameFieldDimension = new DimensionUIResource(100, 20);
        this.nickanmeField.setPreferredSize(nicknameFieldDimension);
        this.nickanmeField.setMaximumSize(nicknameFieldDimension);
        this.textPanel.add(this.nickanmeField);

        this.textPanel.add(Box.createHorizontalStrut(5));
        
        this.toSendTextField = new JTextField();
        Dimension toSendFiDimension = new DimensionUIResource(200, 20);
        this.toSendTextField.setPreferredSize(toSendFiDimension);
        this.toSendTextField.setMaximumSize(toSendFiDimension);
        this.toSendTextField.addKeyListener(new KeysListner());
        this.textPanel.add(this.toSendTextField);

        this.sendButton = new JButton("Send");
        this.sendButton.addActionListener(new sendButtonListner());
        this.textPanel.add(Box.createHorizontalStrut(10));
        this.textPanel.add(this.sendButton);

        this.frame.setVisible(true);
        this.toSendTextField.requestFocusInWindow();
        this.setSendFieldFocus(true);

    }

    private void setSendFieldFocus(boolean value) {
        this.sendFieldFocus = value;
    }

    public boolean checkSendFieldFocus(){
        if(this.sendFieldFocus == true) {
            return true;
        } else {
            return false;
        }
    }

    class sendButtonListner implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String message = toSendTextField.getText();
            toSendTextField.setText("");
            client.setNickname(nickanmeField.getText());
            System.out.println("Send button clicked! Message: " + message);
            client.sendMessage(message) ;
            
            frame.setVisible(true);
        }
    }

    class KeysListner implements KeyListener {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if(key == KeyEvent.VK_ENTER) {
                if(toSendTextField.getText() != "") {
                    String message = toSendTextField.getText();
                    toSendTextField.setText("");
                    client.setNickname(nickanmeField.getText());
                    System.out.println("Send button clicked! Message: " + message);
                    client.sendMessage(message) ;
                    
                    frame.setVisible(true);
                }
            }
        }
        @Override
        public void keyTyped(KeyEvent e) {
            
        }
        @Override
        public void keyReleased(KeyEvent e) {
            
        }
    }

    class MessagesDisplayMenager implements Runnable {
        @Override
        public void run() {
            this.fillMessageArea();
        }

        public void startThread() {
            try {
                Thread thread = new Thread(this);
                thread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            

        }

        private void fillMessageArea() {
            while(true) {
                synchronized(this) {
                    if(client.messageQueue.isEmpty() == false) {
                        for(String i  : client.messageQueue) {
                            messagesArea.append(i + "\n");
                        }
                        client.messageQueue.clear();
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

class ClientServerMessagesMenager implements Runnable {

    private Client client;

    public ClientServerMessagesMenager(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        this.listningForMessages();
    }

    public void startThread() {
        try {
            Thread thread = new Thread(this);
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void listningForMessages() {
        while(true) {
            synchronized(this) {
                try {
                    if(this.client.bufferedReader.ready()) {
                        String message = this.client.bufferedReader.readLine();
                        System.out.println("Message from server: " + message);
                        this.client.messageQueue.add(message);   
                    }   
                
                } catch (NullPointerException e) {

                } 
                
                catch (Exception e) {
                    e.printStackTrace();
                }
                

            }
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    


}