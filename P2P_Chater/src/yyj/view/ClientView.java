package yyj.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;


public class ClientView {
	//UI  
	protected JFrame frame;
	protected JPanel settingPanel,         //配置面板
				     messagePanel;  	   //消息面板
	protected JSplitPane centerSplitPanel; //分隔面板
	protected JScrollPane userPanel,	   //左边用户面板
						  messageBoxPanel; //右边消息框
	protected JTextArea messageTextArea;   //消息编辑框
	protected JTextField nameTextField,    //用户名输入框
						 ipTextField;	   //服务器IP地址输入框
	protected JTextField portTextField;	   //端口输入框
	protected JTextField messageTextField; //消息编辑框
	protected JLabel messageToLabel;       //To..标签
	protected JButton connectButton, 	   //连接按钮
					  disconnectButton,    //断开按钮
	                  sendButton;  		   //发送按钮
	protected JList userList;  			   //动态变化的用户列表
    
    //Model  
	protected DefaultListModel<String> listModel;  
    
    //构造函数
    public ClientView() {  
        initUI();  
    } 
    
    //UI初始化函数
    private void initUI() {  
    	
    	//设置客户端窗口标题、大小以及布局
        frame = new JFrame("客户端");  
        frame.setSize(600, 400);  
        frame.setResizable(false);  
        frame.setLayout(new BorderLayout());  
          
        //设置面板初始参数  
        ipTextField = new JTextField("192.168.1.154");  
        portTextField = new JTextField("6666");  
        nameTextField = new JTextField("杨宇杰");  
        connectButton = new JButton("连接");  
        disconnectButton = new JButton("断开");  
  
        //配置面板
        settingPanel = new JPanel();  
        settingPanel.setLayout(new GridLayout(1, 8));  //设置布局为一行八列
        settingPanel.add(new JLabel("         名字:")); //为配置面板添加组件
        settingPanel.add(nameTextField);  
        settingPanel.add(new JLabel("  服务器IP:"));  
        settingPanel.add(ipTextField);  
        settingPanel.add(new JLabel("  端口号:"));  
        settingPanel.add(portTextField);  
        settingPanel.add(connectButton);  
        settingPanel.add(disconnectButton);  
        settingPanel.setBorder(new TitledBorder("客户端配置")); //设置配置面板标题
  
        //在线用户面板  
        listModel = new DefaultListModel<String>();  
        userList = new JList(listModel);  
        userPanel = new JScrollPane(userList);  
        userPanel.setBorder(new TitledBorder("在线用户"));  //设置在线用户面板标题
  
        //接收消息面板  
        messageTextArea = new JTextArea();  
        messageTextArea.setEditable(false);        //设置该区域不可编辑
        messageTextArea.setForeground(Color.blue); //设置字体默认颜色为蓝色
  
        messageBoxPanel = new JScrollPane(messageTextArea);   //设置为带滑动条的文本框
        messageBoxPanel.setBorder(new TitledBorder("接收消息")); //设置标题 
  
        //发送消息组件  
        messageToLabel = new JLabel("To:所有人  ");   //默认为发送给所有人
        messageTextField = new JTextField();  
        sendButton = new JButton("发送");  
  
        messagePanel = new JPanel(new BorderLayout());  //将组件放置在面板上
        messagePanel.add(messageToLabel, "West");  
        messagePanel.add(messageTextField, "Center");  
        messagePanel.add(sendButton, "East");  
        messagePanel.setBorder(new TitledBorder("发送消息"));  
  
        //将中间在线用户面板与接收消息面板组合起来  
        centerSplitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, userPanel, messageBoxPanel);  
        centerSplitPanel.setDividerLocation(100);  //设置分隔线离左边100px
  
        frame.add(settingPanel, "North");  
        frame.add(centerSplitPanel, "Center");  
        frame.add(messagePanel, "South");  
        frame.setVisible(true);  
   
        serviceUISetting(false); //设置按钮以及文本框的默认状态
    }
    
    public void serviceUISetting(boolean connected) {  
        nameTextField.setEnabled(!connected);  
        ipTextField.setEnabled(!connected);  
        portTextField.setEnabled(!connected);  
        connectButton.setEnabled(!connected);  
        disconnectButton.setEnabled(connected);  
        messageTextField.setEnabled(connected);  
        sendButton.setEnabled(connected);  
    }
    
}
