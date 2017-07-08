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


public class ServerView {
	
	//UI  
	protected JFrame frame;  
	protected JPanel settingPanel,                //配置面板
					 messagePanel;  			  //消息面板
	protected JSplitPane centerSplitPanel;        //分隔面板
	protected JScrollPane userPanel,              //左边用户面板
						  logPanel;               //右边消息框
	protected JTextArea logTextArea;              //服务器日志
	protected JTextField maxClientTextField, 	  //人数上限
						 portTextField;           //端口号
	protected JTextField serverMessageTextField;  //广播消息输入框
	protected JButton startButton, 				  //启动按钮
					  stopButton, 				  //停止按钮
					  sendButton;  				  //发送按钮
	protected JList userList;                     //动态变化的用户列表
    
    //Model  
	protected DefaultListModel<String> listModel;
    
	//构造函数
    public ServerView() {  
        initUI();  
    }
    
    //UI初始化函数  
    @SuppressWarnings("unchecked")
	private void initUI() {  
    	
    	//设置服务端窗口标题、默认大小以及布局
        frame = new JFrame("服务器");  
        frame.setSize(600, 400);  
        frame.setResizable(false);  
        frame.setLayout(new BorderLayout());  
          
        //服务器配置面板（设置默认参数）  
        maxClientTextField = new JTextField("10");  
        portTextField = new JTextField("6666");  
        startButton = new JButton("启动");  
        stopButton = new JButton("停止");  
  
        settingPanel = new JPanel();  
        settingPanel.setLayout(new GridLayout(1, 6));  //设置布局为一行六列
        settingPanel.add(new JLabel("人数上限"));  
        settingPanel.add(maxClientTextField);  
        settingPanel.add(new JLabel("端口号"));  
        settingPanel.add(portTextField);  
        settingPanel.add(startButton);  
        settingPanel.add(stopButton);  
        settingPanel.setBorder(new TitledBorder("服务器配置"));  //设置标题
  
        //在线用户面板  
        listModel = new DefaultListModel<String>();  
  
        userList = new JList(listModel);  
        userPanel = new JScrollPane(userList);  
        userPanel.setBorder(new TitledBorder("在线用户"));  
  
        //服务器日志面板  
        logTextArea = new JTextArea();  
        logTextArea.setEditable(false);  
        logTextArea.setForeground(Color.blue);  //设置默认字体颜色为蓝色
  
        logPanel = new JScrollPane(logTextArea);  
        logPanel.setBorder(new TitledBorder("服务器日志"));  
  
        //发送消息组件  
        serverMessageTextField = new JTextField();  
        sendButton = new JButton("发送");  
  
        messagePanel = new JPanel(new BorderLayout());  
        messagePanel.add(serverMessageTextField, "Center");  
        messagePanel.add(sendButton, "East");  
        messagePanel.setBorder(new TitledBorder("广播消息"));  
  
  
        //将中间在线用户面板与接收消息面板组合起来    
        centerSplitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, userPanel, logPanel);  
        centerSplitPanel.setDividerLocation(100);  //设置分隔线离左边100px
  
        frame.add(settingPanel, "North");  
        frame.add(centerSplitPanel, "Center");  
        frame.add(messagePanel, "South");  
        frame.setVisible(true);    
  
        serviceUISetting(false);  //设置按钮以及文本框的默认状态
    }  
  
    protected void serviceUISetting(boolean started) {  
        maxClientTextField.setEnabled(!started);  
        portTextField.setEnabled(!started);  
        startButton.setEnabled(!started);  
        stopButton.setEnabled(started);  
        serverMessageTextField.setEnabled(started);  
        sendButton.setEnabled(started);  
    }
       
}
