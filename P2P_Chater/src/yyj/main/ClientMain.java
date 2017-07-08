package yyj.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import yyj.model.User;
import yyj.view.*;

public class ClientMain extends ClientView{
	
	//model
	private User me; 
	// 所有在线用户
    private ConcurrentHashMap<String, User> onlineUsers = new ConcurrentHashMap<String, User>();  
    private String sendTarget = "ALL";  //默认发送对象
  
    //Socket  
    private Socket socket;  
    private PrintWriter writer;    //输出流
    private BufferedReader reader; //输入流 
  
    // 负责接收消息的线程  
    private MessageThread messageThread;  
  
    //Status  
    private boolean isConnected;   //判断是否连接到服务端
    
    //构造函数
    public ClientMain() {
    	
    // 写消息的文本框中按回车键时事件
    messageTextField.addActionListener(new ActionListener() {  
        public void actionPerformed(ActionEvent e) {  
            send();  
        }  
    });
    
    // 单击发送按钮时事件
    sendButton.addActionListener(new ActionListener() {  
        public void actionPerformed(ActionEvent e) {  
            send();  
        }  
    });  

    // 单击连接按钮时事件
    connectButton.addActionListener(new ActionListener() {  
        public void actionPerformed(ActionEvent e) {  
            if (!isConnected) {  
                connect();  
            }  
        }  
    });  

    // 单击断开按钮时事件
    disconnectButton.addActionListener(new ActionListener() {  
        public void actionPerformed(ActionEvent e) {  
            if (isConnected) {  
                disconnect();  
            }  
        }  
    });  

    // 关闭窗口时事件
    frame.addWindowListener(new WindowAdapter() {  
        public void windowClosing(WindowEvent e) {  
            if (isConnected) {  
                disconnect();  
            }  
            System.exit(0);  
        }  
    });  

    // 为在线用户添加点击事件
    userList.addListSelectionListener(new ListSelectionListener() { 
    	
        public void valueChanged(ListSelectionEvent e) {  
            int index = userList.getSelectedIndex();  //获取被点击的用户的序号
            if (index < 0) return;  
 
            if (index == 0) {  //默认为所有人
                sendTarget = "ALL";  
                messageToLabel.setText("To: 所有人");  
            } else {  
                String name = (String)listModel.getElementAt(index);  //获取被点击用户的名字
                if (onlineUsers.containsKey(name)) {  
                    sendTarget = onlineUsers.get(name).description();  
                    messageToLabel.setText("To: " + name);  //将To..标签改为To 用户名
                } else {  
                    sendTarget = "ALL";  
                    messageToLabel.setText("To: 所有人");  
                }  
            }  
        }  
    });
}
    
    //连接
    private void connect() {  
        int port;  
          
        try {  
            port = Integer.parseInt(portTextField.getText().trim());  //获取端口号
        } catch(NumberFormatException e) {  
            showErrorMessage("端口号必须为整数！");  
            return;  
        }  
  
        if (port < 1024 || port > 65535) {  //判断端口号是否符合
            showErrorMessage("端口号必须在1024～65535之间");  
            return;  
        }  
  
        String name = nameTextField.getText().trim();  //获取用户名
  
        if (name == null || name.equals("")) {  //判断用户名是否为空
            showErrorMessage("名字不能为空！");  
            return;  
        }  
  
        String ip = ipTextField.getText().trim();  //获取IP地址
  
        if (ip == null || ip.equals("")) {  //判断IP地址是否为空
            showErrorMessage("IP地址不能为空！");  
            return;  
        }  
  
        try {  
            listModel.addElement("所有人");  
  
            me = new User(name, ip);  
            socket = new Socket(ip, port);  //根据指定IP地址以及端口号建立线程
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));  //输入流
            writer = new PrintWriter(socket.getOutputStream());  //输出流
  
            String myIP = socket.getLocalAddress().toString().substring(1);  //获取客户端所在的IP地址
            sendMessage("LOGIN@" + name + "%" + myIP);  //发送用户登录信息
  
            messageThread = new MessageThread();  //创建接收消息的线程
            messageThread.start();  
            isConnected = true;  
  
        } catch(Exception e) {  
            isConnected = false;  
            logMessage("客户端连接失败");  
            listModel.removeAllElements();  //移除在线面板上所有用户
            e.printStackTrace();  
            return;  
        }  
  
        logMessage("客户端连接成功");       //将连接成功的消息显示到消息面板上
        serviceUISetting(isConnected); //设置按钮的状态
    }  
  
    //消息发送
    private void send() {  
        if (!isConnected) {  
            showErrorMessage("未连接到服务器！");  
            return;  
        }  
        String message = messageTextField.getText().trim();  //获取发送框内容 
        if (message == null || message.equals("")) {  
            showErrorMessage("消息不能为空！");  
            return;  
        }  
  
        String to = sendTarget;  
        try {  
        	//向服务器发送消息
        	//MSG@+“接收消息用户名 %IP地址”+“发送者用户名 %IP地址”+@+message
            sendMessage("MSG@" + to + "@" + me.description() + "@" + message);  
            logMessage("我->" + to + ": " + message);  
        } catch(Exception e) {  
            e.printStackTrace();  
            logMessage("（发送失败）我->" + to + ": " + message);  
        }  
  
        messageTextField.setText(null);  //发送完毕把输入框置空
    }  
  
    //断开连接
    private synchronized void disconnect() {  
        try {  
        	//向服务器发送断开连接的消息
            sendMessage("LOGOUT");  
  
            messageThread.close();  
            listModel.removeAllElements();  
            onlineUsers.clear();  
  
            reader.close();  
            writer.close();  
            socket.close();  
            isConnected = false;  
            serviceUISetting(false);  
  
            sendTarget = "ALL";  
            messageToLabel.setText("To: 所有人");  
  
            logMessage("已断开连接...");  
        } catch(Exception e) {  
            e.printStackTrace();  
            isConnected = true;  
            serviceUISetting(true);  
            showErrorMessage("服务器断开连接失败！");  
        }  
    }  
  
    private void sendMessage(String message) {  
        writer.println(message);  
        writer.flush();  
    }  
  
    private void logMessage(String msg) {  
        messageTextArea.append(msg + "\r\n");  
    }  
  
    private void showErrorMessage(String msg) {  
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);  
    }   
              
    //接收消息的线程
    private class MessageThread extends Thread {  
        private boolean isRunning = false;  
  
        public MessageThread() {  
            isRunning = true;  
        }  
  
        public void run() {  
            while (isRunning) {  //不断接收消息
                try {  
                    String message = reader.readLine();  
                    StringTokenizer tokenizer = new StringTokenizer(message, "@");  
                    String command = tokenizer.nextToken();  
  
                    if (command.equals("CLOSE")) {  
                        logMessage("服务器已关闭，正在断开连接...");  
                        disconnect();  
                        isRunning = false;  
                        return;  
                    } else if (command.equals("ERROR")) {  
                        String error = tokenizer.nextToken();  
                        logMessage("服务器返回错误，错误类型：" + error);  
                    } else if (command.equals("LOGIN")) {  
                        String status = tokenizer.nextToken();  
                        if (status.equals("SUCCESS")) {  
                            logMessage("登录成功！" + tokenizer.nextToken());  
                        } else if (status.equals("FAIL")) {  
                            logMessage("登录失败，断开连接！原因：" + tokenizer.nextToken());  
                            disconnect();  
                            isRunning = false;  
                            return;  
                        }  
                    } else if (command.equals("USER")) {  
                        String type = tokenizer.nextToken();  
                        if (type.equals("ADD")) {  
                            String userDescription = tokenizer.nextToken();  
                            User newUser = new User(userDescription);  
                            onlineUsers.put(newUser.getName(), newUser);  
                            listModel.addElement(newUser.getName());  
  
                            logMessage("新用户（" + newUser.description() + "）上线！");  
  
                        } else if (type.equals("DELETE")) {  
                            String userDescription = tokenizer.nextToken();  
                            User deleteUser = new User(userDescription);  
                            onlineUsers.remove(deleteUser.getName());  
                            listModel.removeElement(deleteUser.getName());  
  
                            logMessage("用户（" + deleteUser.description() + "）下线！");  
  
                            if (sendTarget.equals(deleteUser.description())) {  
                                sendTarget = "ALL";  
                                messageToLabel.setText("To: 所有人");  
                            }  
  
                        } else if (type.equals("LIST")) {  
                            int num = Integer.parseInt(tokenizer.nextToken());  
                            for (int i = 0; i < num; i++) {  
                                String userDescription = tokenizer.nextToken();  
                                User newUser = new User(userDescription);  
                                onlineUsers.put(newUser.getName(), newUser);  
                                listModel.addElement(newUser.getName());  
  
                                logMessage("获取到用户（" + newUser.description() + "）在线！");  
                            }  
                        }  
                    } else if (command.equals("MSG")) {  
                        StringBuffer buffer = new StringBuffer();  
                        String to = tokenizer.nextToken();  
                        String from = tokenizer.nextToken();  
                        String content = tokenizer.nextToken();  
  
                        buffer.append(from);  
                        if (to.equals("ALL")) {  
                            buffer.append("（群发）");  
                        }  
                        buffer.append(": " + content);  
                        logMessage(buffer.toString());  
                    }  
  
                } catch(Exception e) {  
                    e.printStackTrace();  
                    logMessage("接收消息异常！");  
                }  
            }  
        }  
  
        public void close() {  
            isRunning = false;  
        }  
    }
    
    
    // 主函数
    public static void main(String args[]){     	
       new ClientMain();            
    }
    
}
