package yyj.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JOptionPane;
import yyj.model.User;
import yyj.view.*;

/* 
	message type 
	1.alias: 
    	USER <= USER_NAME%USER_IPADDR 

	2.format: 
		server:  
        MSG     @   to      @   from    @   content  
                    ALL         SERVER      xxx 
                    ALL         USER        xxx 
                    USER        USER        xxx 

        LOGIN   @   status  @   content 
                    SUCCESS     xxx 
                    FAIL        xxx 

        USER    @   type    @   other 
                    ADD         USER 
                    DELETE      USER 
                    LIST        number  {@  USER}+ 

        ERROR   @   TYPE 

        CLOSE 
 
	client: 
        MSG     @   to      @   from    @   content 
                    ALL         USER        xxx 
                    USER        USER        xxx 
        LOGOUT 

        LOGIN   @   USER     

*/ 

public class ServerMain extends ServerView{
	
	//Socket  
    private ServerSocket serverSocket;  
  
    //Status  
    private boolean isStart = false;  //判断服务器是否已经启动
    private int maxClientNum;  //最大连接人数
  
    //Threads  
    //ArrayList<ClientServiceThread> clientServiceThreads;  
    ConcurrentHashMap<String, ClientServiceThread> clientServiceThreads;  
    ServerThread serverThread;
	
    //构造函数
	public ServerMain() {
		
		//广播消息框绑定回车键
		serverMessageTextField.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent e) {  
                sendAll();  
            }  
        });  
  
		//发送按钮绑定点击事件
        sendButton.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent e) {  
                sendAll();  
            }  
        });  
  
        //启动按钮绑定点击事件
        startButton.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent e) {  
                if (!isStart) {  
                    startServer();  
                }  
            }  
        });  
  
        //停止按钮绑定点击事件
        stopButton.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent e) {  
                if (isStart) {  
                    stopServer();  
                }  
            }  
        });  
  
        //绑定窗口关闭事件
        frame.addWindowListener(new WindowAdapter() {  
            public void windowClosing(WindowEvent e) {  
                if (isStart) {  
                    stopServer();  
                }  
                System.exit(0);  
            }  
        });
	}
	
	//启动服务端
	private void startServer() {  
        int port;  
  
        //判断输入的端口号跟人数上限数是否符合规范
        try {  
            port = Integer.parseInt(portTextField.getText().trim());  
        } catch(NumberFormatException e) {  
            showErrorMessage("端口号必须为整数！");  
            return;  
        }  
  
        if (port < 1024 || port > 65535) {  
            showErrorMessage("端口号必须在1024～65535之间");  
            return;  
        }  
  
        try {  
            maxClientNum = Integer.parseInt(maxClientTextField.getText().trim());  
        } catch(NumberFormatException e) {  
            showErrorMessage("人数上限必须是正整数！");  
            maxClientNum = 0;  
            return;  
        }  
  
        if (maxClientNum <= 0) {  
            showErrorMessage("人数上限必须是正整数！");  
            maxClientNum = 0;  
            return;  
        }  
  
        try {  //运用获取到的端口号开启服务器线程
            clientServiceThreads = new ConcurrentHashMap<String, ClientServiceThread>();  
            serverSocket = new ServerSocket(port);  
            serverThread = new ServerThread();  
            serverThread.start();  
            isStart = true;  
        } catch (BindException e) {  
            isStart = false;  
            showErrorMessage("启动服务器失败：端口被占用！");  
            return;  
        } catch (Exception e) {  
            isStart = false;  
            showErrorMessage("启动服务器失败：启动异常！");  
            e.printStackTrace();  
            return;  
        }  
  
        logMessage("服务器启动：人数上限：" + maxClientNum + " 端口号：" + port);  
        serviceUISetting(true);  
    }  
  
    private synchronized void stopServer() {  
        try {  
            serverThread.closeThread();  
            //断开与所有客户端的连接
            for (Map.Entry<String, ClientServiceThread> entry : clientServiceThreads.entrySet()) {  
                ClientServiceThread clientThread = entry.getValue();  
                clientThread.sendMessage("CLOSE");  
                clientThread.close();  
            }  
  
            clientServiceThreads.clear();  
            listModel.removeAllElements();  
            isStart = false;  
            serviceUISetting(false);  
            logMessage("服务器已关闭！");  
        } catch(Exception e) {  
            e.printStackTrace();  
            showErrorMessage("关闭服务器异常！");  
            isStart = true;  
            serviceUISetting(true);  
        }  
    }  
  
    private void sendAll() {  
        if (!isStart) {  
            showErrorMessage("服务器还未启动，不能发送消息！");  
            return;  
        }  
  
        if (clientServiceThreads.size() == 0) {  
            showErrorMessage("没有用户在线，不能发送消息！");  
            return;  
        }  
  
        String message = serverMessageTextField.getText().trim();  
        if (message == null || message.equals("")) {  
            showErrorMessage("发送消息不能为空！");  
            return;  
        }  
  
        for (Map.Entry<String, ClientServiceThread> entry : clientServiceThreads.entrySet()) {  
            entry.getValue().sendMessage("MSG@ALL@SERVER@" + message);  
        }  
  
        logMessage("Server: " + message);  
        serverMessageTextField.setText(null);  
    }  
  
    private void logMessage(String msg) {  
        logTextArea.append(msg + "\r\n");  
    }  
      
    private void showErrorMessage(String msg) {  
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);  
    }
    
    //Server Thread class  
    private class ServerThread extends Thread {  
        private boolean isRunning;  
  
        public ServerThread() {  
            this.isRunning = true;  
        }  
  
        public void run() {  
            while (this.isRunning) {  
                try {  
                    if (!serverSocket.isClosed()) {  //接收客户端发来的连接请求
                        Socket socket = serverSocket.accept();  
  
                        if (clientServiceThreads.size() == maxClientNum) {  //判断人数是否已达上限
                            PrintWriter writer = new PrintWriter(socket.getOutputStream());  
                            writer.println("LOGIN@FAIL@对不起，服务器在线人数已达到上限，请稍候尝试！");  
                            writer.flush();  
                            writer.close();  
                            socket.close();  
                        } else {  
                            ClientServiceThread clientServiceThread = new ClientServiceThread(socket);  
                            User user = clientServiceThread.getUser();  
                            clientServiceThreads.put(user.description(), clientServiceThread);  
                            listModel.addElement(user.getName());  
                            logMessage(user.description() + "上线...");  
  
                            clientServiceThread.start();  
                        }  
                    }  
                } catch(Exception e) {  
                    e.printStackTrace();  
                }  
            }  
        }  
  
        public synchronized void closeThread() throws IOException {  
            this.isRunning = false;  
            serverSocket.close();  
            System.out.println("serverSocket close!!!");  
        }  
    }  
  
    //Client Thread class  
    private class ClientServiceThread extends Thread {  
        private Socket socket;  
        private User user;  
        private BufferedReader reader;  
        private PrintWriter writer;  
        private boolean isRunning;  
  
        private synchronized boolean init() {  
            try {  
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));  
                writer = new PrintWriter(socket.getOutputStream());  
  
                String info = reader.readLine();  
                StringTokenizer tokenizer = new StringTokenizer(info, "@");  
                String type = tokenizer.nextToken();  
                if (!type.equals("LOGIN")) {  
                    sendMessage("ERROR@MESSAGE_TYPE");  
                    return false;  
                }  
  
                user = new User(tokenizer.nextToken());  
                sendMessage("LOGIN@SUCCESS@" + user.description() + "与服务器连接成功！");  
  
                int clientNum = clientServiceThreads.size();  
                if (clientNum > 0) {  
                    //告诉该客户端还有谁在线  
                    StringBuffer buffer = new StringBuffer();  
                    buffer.append("@");  
                    for (Map.Entry<String, ClientServiceThread> entry : clientServiceThreads.entrySet()) {  
                        ClientServiceThread serviceThread = entry.getValue();  
                        buffer.append(serviceThread.getUser().description() + "@");  
                        //告诉其他用户此用户在线  
                        serviceThread.sendMessage("USER@ADD@" + user.description());  
                    }  
  
                    sendMessage("USER@LIST@" + clientNum + buffer.toString());  
                }  
  
                return true;  
  
            } catch(Exception e) {  
                e.printStackTrace();  
                return false;  
            }  
        }  
  
        public ClientServiceThread(Socket socket) {  
            this.socket = socket;  
            this.isRunning = init();  
            if (!this.isRunning) {  
                logMessage("服务线程开启失败！");  
            }  
        }  
  
        public void run() {  
            while (isRunning) {  
                try {  
                    String message = reader.readLine();  
                   // System.out.println("recieve message: " + message);  
                    if (message.equals("LOGOUT")) {  
                        logMessage(user.description() + "下线...");  
  
                        int clientNum = clientServiceThreads.size();  
                          
                        //告诉其他用户该用户已经下线  
                        for (Map.Entry<String, ClientServiceThread> entry : clientServiceThreads.entrySet()) {  
                            entry.getValue().sendMessage("USER@DELETE@" + user.description());  
                        }  
  
                        //移除该用户以及服务器线程  
                        listModel.removeElement(user.getName());  
                        clientServiceThreads.remove(user.description());  
  
                       // System.out.println(user.description() + " logout, now " + listModel.size() + " client(s) online...(" + clientServiceThreads.size() + " Thread(s))");  
  
                        close();  
                        return;  
                    } else {  //发送消息
                        dispatchMessage(message);  
                    }  
                } catch(Exception e) {  
                    e.printStackTrace();  
                }  
            }  
        }  
  
        public void dispatchMessage(String message) {  
            StringTokenizer tokenizer = new StringTokenizer(message, "@");  
            String type = tokenizer.nextToken();  
            if (!type.equals("MSG")) {  
                sendMessage("ERROR@MESSAGE_TYPE");  
                return;  
            }  
  
            String to = tokenizer.nextToken();  
            String from = tokenizer.nextToken();  
            String content = tokenizer.nextToken();  
  
            logMessage(from + "->" + to + ": " + content);  
            if (to.equals("ALL")) {  
                //send to everyone  
                for (Map.Entry<String, ClientServiceThread> entry : clientServiceThreads.entrySet()) {  
                    entry.getValue().sendMessage(message);  
                }  
            } else {  
                //发送给某一个人  
                if (clientServiceThreads.containsKey(to)) {  
                    clientServiceThreads.get(to).sendMessage(message);  
                } else {  
                    sendMessage("ERROR@INVALID_USER");  
                }  
            }  
        }  
  
        public void close() throws IOException {  
            this.isRunning = false;  
            this.reader.close();  
            this.writer.close();  
            this.socket.close();  
              
        }  
  
        public void sendMessage(String message) {  
            writer.println(message);  
            writer.flush();  
        }  
  
        public User getUser() {  
            return user;  
        }  
    }  
      
    //客户端主函数
    public static void main(String args[]) {  
        new ServerMain();  
    }
    
}
