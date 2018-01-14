package tomasulo;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

/**
 * @author yanqing.qyq 2012-2015@USTC
 * 模板说明：该模板主要提供依赖Swing组件提供的JPanle，JFrame，JButton等提供的GUI。使用“监听器”模式监听各个Button的事件，从而根据具体事件执行不同方法。
 * Tomasulo算法核心需同学们自行完成，见说明（4）
 * 对于界面必须修改部分，见说明(1),(2),(3)
 *
 *  (1)说明：根据你的设计完善指令设置中的下拉框内容
 *	(2)说明：请根据你的设计指定各个面板（指令状态，保留站，Load部件，寄存器部件）的大小
 *	(3)说明：设置界面默认指令
 *	(4)说明： Tomasulo算法实现
 */

public class Tomasulo extends JFrame implements ActionListener{
	/*
	 * 界面上有六个面板：
	 * panel1 : 指令设置
	 * panel2 : 执行时间设置
	 * panel3 : 指令状态
	 * panel4 : 保留站状态
	 * panel5 : Load部件
	 * panel6 : 寄存器状态
	 */
	private JPanel panel1,panel2,panel2_1,panel3,panel4,panel5,panel6;

	/*
	 * 四个操作按钮：步进，进5步，重置，执行
	 */
	private JButton stepbut,step5but,resetbut,startbut;

	/*
	 * 指令选择框
	 */
	private JComboBox instbox[]=new JComboBox[24];

	/*
	 * 每个面板的名称
	 */
	private JLabel instl, timel, numl, tl1,tl2,tl3,tl4,resl,regl,ldl,insl,stepsl;
	private int time[]=new int[4]; //部件运算时间

	/*
	 * 部件执行时间的输入框
	 */
	private JTextField tt1,tt2,tt3,tt4;

	private int intv[][]=new int[6][4];//记录每条指令的四个“部件”,对应下拉框的index
	private int cnow,instnow=0;//cnow 当前执行周期 clock_now; instnow 当前执行指令编号,instruction now
	private int cal[][]={{-1,0,0},{-1,0,0},{-1,0,0},{-1,0,0},{-1,0,0}};
	//运算部件,3 bits,first bit -- -1 nop,others 执行指令编号,second bit -- execute time,third -- 操作数 busy tag
	private int ld[][]={{0,0},{0,0},{0,0}};//加载部件，2 bits,first bit--busy tag,second--remaining  execute time
	private int ff[]={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};//浮点数busy tag
	
	/*
	 * 部件数量下拉框
	 */
	private JComboBox[] unitN = new JComboBox[3];
	private int[] unitIndex = {2,2,1};
	/*
	 * 自定义变量
	 */
	private int inUnitPos[] = {-1, -1, -1, -1, -1, -1};//指令使用部件内部编号
	private int stage[] = {-1, -1, -1, -1, -1, -1};//当前指令执行阶段
	private int rr = 0;//操作数（temp）
	private boolean wbFlag = false;//wbFlag 写回操作标记
	private int wbRecord = 0;//wbRecord 进入写操作指令编号
	private int finished[] = {-1,-1,-1,-1,-1,-1};//当前指令执行阶段结束标记
	private int finRecord;//当前完成指令编号
	private int finInsNum=0;//已完成指令数
	private boolean endTag = false; //指令流执结标记
	
	/*
	 * (1)说明：根据你的设计完善指令设置中的下拉框内容
	 * inst： 指令下拉框内容:"NOP","L.D","ADD.D","SUB.D","MULT.D","DIV.D"…………
	 * fx：       目的寄存器下拉框内容:"F0","F2","F4","F6","F8" …………
	 * rx：       源操作数寄存器内容:"R0","R1","R2","R3","R4","R5","R6","R7","R8","R9" …………
	 * ix：       立即数下拉框内容:"0","1","2","3","4","5","6","7","8","9" …………
	 */
	private String  inst[]={"NOP","L.D","ADD.D","SUB.D","MULT.D","DIV.D"},
					fx[]={"F0","F2","F4","F6","F8","F10","F12","F14","F16","F18","F20","F22","F24","F26","F28","F30"},
					rx[]={"R0","R1","R2","R3","R4","R5","R6","R7","R8","R9","R10","R11"
						,"R12","R13","R14","R15","R16","R17","R18","R19","R20","R21"
						,"R23","R24","R25","R26","R27","R28","R29","R30","R31"},
					ix[]={"0","1","2","3","4","5","6","7","8","9"
						,"10","11","12","13","14","15","16","17","18"
						,"19","20","21","22"},
					result[] = {"M1","M2","M3","M4","M5","M6"},
					unitNum[] = {"1","2","3","4","5"};
	/*
	 * (2)说明：请根据你的设计指定各个面板（指令状态，保留站，Load部件，寄存器部件）的大小
	 * 		指令状态 面板
	 * 		保留站 面板
	 * 		Load部件 面板
	 * 		寄存器 面板
	 * 					的大小
	 */
	private	String  instst[][]=new String[7][4], resst[][]=new String[6][8],
					ldst[][]=new String[4][4], regst[][]=new String[3][17];
	private	JLabel  instjl[][]=new JLabel[7][4], resjl[][]=new JLabel[6][8],
					ldjl[][]=new JLabel[4][4], regjl[][]=new JLabel[3][17];

	//构造方法
	public Tomasulo(){
		super("Tomasulo Simulator");

		//设置布局
		Container cp=getContentPane();
		FlowLayout layout=new FlowLayout(FlowLayout.LEFT,8,6);
		cp.setLayout(layout);

		//指令设置。GridLayout(int 指令条数, int 操作码+操作数, int hgap, int vgap)
		instl = new JLabel("指令设置");
		panel1 = new JPanel(new GridLayout(6,4,0,0));
		panel1.setPreferredSize(new Dimension(350, 150));
		panel1.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		//执行时间
		timel = new JLabel("执行时间设置");
		panel2 = new JPanel(new GridLayout(2,4,0,0));
		panel2.setPreferredSize(new Dimension(280, 80));
		panel2.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		//执行部件数目
		numl = new JLabel("执行部件数目设置");
		panel2_1 = new JPanel(new GridLayout(3,2,0,0));
		panel2_1.setPreferredSize(new Dimension(300, 80));
		panel2_1.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		
		//指令状态
		insl = new JLabel("指令状态");
		panel3 = new JPanel(new GridLayout(7,4,0,0));
		panel3.setPreferredSize(new Dimension(420, 175));
		panel3.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		//保留站
		resl = new JLabel("保留站");
		panel4 = new JPanel(new GridLayout(6,7,0,0));
		panel4.setPreferredSize(new Dimension(420, 150));
		panel4.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		//Load部件
		ldl = new JLabel("Load部件");
		panel5 = new JPanel(new GridLayout(4,4,0,0));
		panel5.setPreferredSize(new Dimension(300, 100));
		panel5.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		//寄存器状态
		regl = new JLabel("寄存器");
		panel6 = new JPanel(new GridLayout(3,17,0,0));
		panel6.setPreferredSize(new Dimension(740, 75));
		panel6.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		tl1 = new JLabel("Load");
		tl2 = new JLabel("加/减");
		tl3 = new JLabel("乘法");
		tl4 = new JLabel("除法");

		//操作按钮:执行，重设，步进，步进5步
		stepsl = new JLabel();
		stepsl.setPreferredSize(new Dimension(200, 30));
		stepsl.setHorizontalAlignment(SwingConstants.CENTER);
		stepsl.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		stepbut = new JButton("步进");
		stepbut.addActionListener(this);
		step5but = new JButton("步进5步");
		step5but.addActionListener(this);
		startbut = new JButton("执行");
		startbut.addActionListener(this);
		resetbut= new JButton("重设");
		resetbut.addActionListener(this);
		tt1 = new JTextField("2");
		tt2 = new JTextField("2");
		tt3 = new JTextField("10");
		tt4 = new JTextField("40");

		//指令设置
		/*
		 * 设置指令选择框（操作码，操作数，立即数等）的default选择
		 */
		for (int i=0;i<2;i++)
			for (int j=0;j<4;j++){
				if (j==0){
					instbox[i*4+j]=new JComboBox(inst);
				}
				else if (j==1){
					instbox[i*4+j]=new JComboBox(fx);
				}
				else if (j==2){
					instbox[i*4+j]=new JComboBox(ix);
				}
				else {
					instbox[i*4+j]=new JComboBox(rx);
				}
				instbox[i*4+j].addActionListener(this);
				panel1.add(instbox[i*4+j]);
			}
		for (int i=2;i<6;i++)
			for (int j=0;j<4;j++){
				if (j==0){
					instbox[i*4+j]=new JComboBox(inst);
				}
				else {
					instbox[i*4+j]=new JComboBox(fx);
				}
				instbox[i*4+j].addActionListener(this);
				panel1.add(instbox[i*4+j]);
			}
		/*
		 * (3)说明：设置界面默认指令，根据你设计的指令，操作数等的选择范围进行设置。
		 * 默认6条指令。待修改
		 */
		instbox[0].setSelectedIndex(1);
		instbox[1].setSelectedIndex(4);
		instbox[2].setSelectedIndex(21);
		instbox[3].setSelectedIndex(3);

		instbox[4].setSelectedIndex(1);
		instbox[5].setSelectedIndex(2);
		instbox[6].setSelectedIndex(16);
		instbox[7].setSelectedIndex(4);

		instbox[8].setSelectedIndex(4);
		instbox[9].setSelectedIndex(1);
		instbox[10].setSelectedIndex(2);
		instbox[11].setSelectedIndex(3);

		instbox[12].setSelectedIndex(3);
		instbox[13].setSelectedIndex(5);
		instbox[14].setSelectedIndex(4);
		instbox[15].setSelectedIndex(2);

		instbox[16].setSelectedIndex(5);
		instbox[17].setSelectedIndex(6);
		instbox[18].setSelectedIndex(1);
		instbox[19].setSelectedIndex(4);

		instbox[20].setSelectedIndex(2);
		instbox[21].setSelectedIndex(4);
		instbox[22].setSelectedIndex(5);
		instbox[23].setSelectedIndex(2);

		//执行时间设置
		panel2.add(tl1);
		panel2.add(tt1);
		panel2.add(tl2);
		panel2.add(tt2);
		panel2.add(tl3);
		panel2.add(tt3);
		panel2.add(tl4);
		panel2.add(tt4);
		
		
		//执行部件数目设置
		
		unitN[0]=new JComboBox(unitNum);
		unitN[0].setSelectedIndex(unitIndex[0]);
		unitN[0].addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				unitIndex[0] = unitN[0].getSelectedIndex();
			}
		});
		
		unitN[1]=new JComboBox(unitNum);
		unitN[1].setSelectedIndex(unitIndex[1]);
		unitN[1].addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				unitIndex[1] = unitN[1].getSelectedIndex();
			}
		});
		
		unitN[2]=new JComboBox(unitNum);
		unitN[2].setSelectedIndex(unitIndex[2]);
		unitN[2].addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				unitIndex[2] = unitN[2].getSelectedIndex();
			}
		});
		
		
		
		panel2_1.add(new JLabel("load/store"));
		panel2_1.add(unitN[0]);
		panel2_1.add(new JLabel("加法"));
		panel2_1.add(unitN[1]);
		panel2_1.add(new JLabel("乘法/除法"));
		panel2_1.add(unitN[2]);
		

		//指令状态设置
		for (int i=0;i<7;i++)
		{
			for (int j=0;j<4;j++){
				instjl[i][j]=new JLabel(instst[i][j]);
				instjl[i][j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
				panel3.add(instjl[i][j]);
			}
		}
		//保留站设置
		for (int i=0;i<6;i++)
		{
			for (int j=0;j<8;j++){
				resjl[i][j]=new JLabel(resst[i][j]);
				resjl[i][j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
				panel4.add(resjl[i][j]);
			}
		}
		//Load部件设置
		for (int i=0;i<4;i++)
		{
			for (int j=0;j<4;j++){
				ldjl[i][j]=new JLabel(ldst[i][j]);
				ldjl[i][j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
				panel5.add(ldjl[i][j]);
			}
		}
		//寄存器设置
		for (int i=0;i<3;i++)
		{
			for (int j=0;j<17;j++){
				regjl[i][j]=new JLabel(regst[i][j]);
				regjl[i][j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
				panel6.add(regjl[i][j]);
			}
		}

		//向容器添加以上部件
		cp.add(instl);
		cp.add(panel1);
		cp.add(timel);
		cp.add(panel2);
		cp.add(numl);
		cp.add(panel2_1);

		cp.add(startbut);
		cp.add(resetbut);
		cp.add(stepbut);
		cp.add(step5but);

		cp.add(panel3);
		cp.add(insl);
		cp.add(panel5);
		cp.add(ldl);
		cp.add(panel4);
		cp.add(resl);
		cp.add(stepsl);
		cp.add(panel6);
		cp.add(regl);

		stepbut.setEnabled(false);
		step5but.setEnabled(false);
		panel3.setVisible(false);
		insl.setVisible(false);
		panel4.setVisible(false);
		ldl.setVisible(false);
		panel5.setVisible(false);
		resl.setVisible(false);
		stepsl.setVisible(false);
		panel6.setVisible(false);
		regl.setVisible(false);
		setSize(1250,700);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/*
	 * 点击”执行“按钮后，根据选择的指令，初始化其他几个面板
	 */
	public void init(){
		// get value
		for (int i=0;i<6;i++){
			intv[i][0]=instbox[i*4].getSelectedIndex();
			if (intv[i][0]!=0){
				intv[i][1]=instbox[i*4+1].getSelectedIndex();
				if (intv[i][0]==1){
					intv[i][2]=instbox[i*4+2].getSelectedIndex();
					intv[i][3]=instbox[i*4+3].getSelectedIndex();
				}
				else {
					intv[i][2]=instbox[i*4+2].getSelectedIndex();
					intv[i][3]=instbox[i*4+3].getSelectedIndex();
				}
			}
		}
		time[0]=Integer.parseInt(tt1.getText());
		time[1]=Integer.parseInt(tt2.getText());
		time[2]=Integer.parseInt(tt3.getText());
		time[3]=Integer.parseInt(tt4.getText());
		// set 0
		instst[0][0]="指令";
		instst[0][1]="发射";
		instst[0][2]="执行";
		instst[0][3]="写回";


		ldst[0][0]="名称";
		ldst[0][1]="Busy";
		ldst[0][2]="地址";
		ldst[0][3]="值";
		ldst[1][0]="Load1";
		ldst[2][0]="Load2";
		ldst[3][0]="Load3";
		ldst[1][1]="no";
		ldst[2][1]="no";
		ldst[3][1]="no";

		resst[0][0]="Time";
		resst[0][1]="名称";
		resst[0][2]="Busy";
		resst[0][3]="Op";
		resst[0][4]="Vj";
		resst[0][5]="Vk";
		resst[0][6]="Qj";
		resst[0][7]="Qk";
		resst[1][1]="Add1";
		resst[2][1]="Add2";
		resst[3][1]="Add3";
		resst[4][1]="Mult1";
		resst[5][1]="Mult2";
		resst[1][2]="no";
		resst[2][2]="no";
		resst[3][2]="no";
		resst[4][2]="no";
		resst[5][2]="no";

		regst[0][0]="字段";
		for (int i=1;i<17;i++){
			regst[0][i]=fx[i-1];

		}
		regst[1][0]="状态";
		regst[2][0]="值";

		for (int i=1;i<7;i++)
		for (int j=0;j<4;j++){
			if (j==0){
				int temp=i-1;
				String disp;
				disp = inst[instbox[temp*4].getSelectedIndex()]+" ";
				if (instbox[temp*4].getSelectedIndex()==0) disp=disp;
				else if (instbox[temp*4].getSelectedIndex()==1){
					disp=disp+fx[instbox[temp*4+1].getSelectedIndex()]+','+ix[instbox[temp*4+2].getSelectedIndex()]+'('+rx[instbox[temp*4+3].getSelectedIndex()]+')';
				}
				else {
					disp=disp+fx[instbox[temp*4+1].getSelectedIndex()]+','+fx[instbox[temp*4+2].getSelectedIndex()]+','+fx[instbox[temp*4+3].getSelectedIndex()];
				}
				instst[i][j]=disp;
			}
			else instst[i][j]="";
		}
		for (int i=1;i<6;i++)
		for (int j=0;j<8;j++)if (j!=1&&j!=2){
			resst[i][j]="";
		}
		for (int i=1;i<4;i++)
		for (int j=2;j<4;j++){
			ldst[i][j]="";
		}
		for (int i=1;i<3;i++)
		for (int j=1;j<17;j++){
			regst[i][j]="";
		}
		instnow=0;
		for (int i=0;i<5;i++){
			for (int j=1;j<3;j++) cal[i][j]=0;
			cal[i][0]=-1;
		}
		for (int i=0;i<3;i++){
			for (int j=1;j<2;j++) ld[i][j]=0;
			ld[i][0]=-1;
		}
		
		//自定义变量的初始化
		rr = 0;
		wbFlag = false;
		wbRecord = -1;
		finRecord = -1;
		finInsNum = 0;
		endTag = false;
		for (int i=0;i<17;i++) ff[i]=0;
		for(int i = 0; i < 6; i++){ 
			inUnitPos[i] = -1;
			stage[i] = -1;
			finished[i] = -1;
		}
	}

	/*
	 * 点击操作按钮后，用于显示结果
	 */
	public void display(){
		for (int i=0;i<7;i++)
			for (int j=0;j<4;j++){
				instjl[i][j].setText(instst[i][j]);
			}
		for (int i=0;i<6;i++)
			for (int j=0;j<8;j++){
				resjl[i][j].setText(resst[i][j]);
			}
		for (int i=0;i<4;i++)
			for (int j=0;j<4;j++){
				ldjl[i][j].setText(ldst[i][j]);
			}
		for (int i=0;i<3;i++)
			for (int j=0;j<17;j++){
				regjl[i][j].setText(regst[i][j]);
			}
		stepsl.setText("当前周期："+String.valueOf(cnow-1));
	}

	public void actionPerformed(ActionEvent e){
		//点击“执行”按钮的监听器
		if (e.getSource()==startbut) {
			for (int i=0;i<24;i++) instbox[i].setEnabled(false);
			for (int i=0;i<3;i++)	unitN[i].setEnabled(false);
			tt1.setEnabled(false);tt2.setEnabled(false);
			tt3.setEnabled(false);tt4.setEnabled(false);
			stepbut.setEnabled(true);
			step5but.setEnabled(true);
			startbut.setEnabled(false);
			//根据指令设置的指令初始化其他的面板
			init();
			cnow=1;
			//展示其他面板
			display();
			panel3.setVisible(true);
			panel4.setVisible(true);
			panel5.setVisible(true);
			panel6.setVisible(true);
			insl.setVisible(true);
			ldl.setVisible(true);
			resl.setVisible(true);
			stepsl.setVisible(true);
			regl.setVisible(true);
		}
		//点击“重置”按钮的监听器
		if (e.getSource()==resetbut) {
			for (int i=0;i<24;i++) instbox[i].setEnabled(true);
			for (int i=0;i<3;i++)	unitN[i].setEnabled(true);
			tt1.setEnabled(true);tt2.setEnabled(true);
			tt3.setEnabled(true);tt4.setEnabled(true);
			stepbut.setEnabled(false);
			step5but.setEnabled(false);
			startbut.setEnabled(true);

			init();
			cnow = 1;
			display();

			panel3.setVisible(false);
			insl.setVisible(false);
			panel4.setVisible(false);
			ldl.setVisible(false);
			panel5.setVisible(false);
			resl.setVisible(false);
			stepsl.setVisible(false);
			panel6.setVisible(false);
			regl.setVisible(false);
		}
		//点击“步进”按钮的监听器
		if (e.getSource()==stepbut) {
			if(endTag) {
				stepbut.setEnabled(false);
				step5but.setEnabled(false);
			}
			core();
			cnow++;
			display();
		}
		//点击“进5步”按钮的监听器
		if (e.getSource()==step5but) {
			for (int i=0;i<5;i++){
				if(endTag) {
					stepbut.setEnabled(false);
					step5but.setEnabled(false);
					break;
				}
				core();
				cnow++;
			}
			display();
		}

		for (int i=0;i<24;i=i+4)
		{
			if (e.getSource()==instbox[i]) {
				if (instbox[i].getSelectedIndex()==1){
					instbox[i+2].removeAllItems();
					for (int j=0;j<ix.length;j++) instbox[i+2].addItem(ix[j]);
					instbox[i+3].removeAllItems();
					for (int j=0;j<rx.length;j++) instbox[i+3].addItem(rx[j]);
				}
				else {
					instbox[i+2].removeAllItems();
					for (int j=0;j<fx.length;j++) instbox[i+2].addItem(fx[j]);
					instbox[i+3].removeAllItems();
					for (int j=0;j<fx.length;j++) instbox[i+3].addItem(fx[j]);
				}
			}
		}
	}
	/*
	 * (4)说明： Tomasulo算法实现
	 */
	public void core()
	{
		if(finInsNum >= 6)
			endTag = true;
		
		int i = 0, j = 0, timetemp;

		// 执行一周期
		for(i = 0; i < instnow; i++)
			if(inUnitPos[i] != -1)		// 若当前指令还占用着部件，即还未执行完毕
			{
				if(intv[i][0] == 1) 	// 当前指令是LOAD指令
					if(stage[i] == 0) 	// 若当前指令处于刚发射阶段
					{
						stage[i]++;
						ldst[inUnitPos[i]+1][2] = "R["+rx[intv[i][3]]+"]+"+ix[intv[i][2]];
						instst[i+1][2] = Integer.toString(cnow)+"~";
						ld[inUnitPos[i]][1]--;		// 更新剩余时间
					}
					else if(stage[i] == 1)	// 若当前指令已在执行阶段
						if(ld[inUnitPos[i]][1] == 1)	// 若只剩最后一个执行周期，即本轮可以执行完毕 
						{
							stage[i]++;									
							ldst[inUnitPos[i] + 1][3] = "M[R["+rx[intv[i][3]]+"]+"+ix[intv[i][2]]+"]";
							instst[i+1][2] = instst[i+1][2] + (Integer.toString(cnow));
						}
						else		// 若本轮不能执行完毕，则减去一个执行周期即可
							ld[inUnitPos[i]][1]--;
					else if(stage[i] == 2)	// 若当前指令已在写回阶段
					{
						finished[i] = 0;	// 标记当前指令已完成				
						if(!wbFlag)
						{
							wbFlag = true;
							wbRecord = i;
						}
					}
				if(intv[i][0] > 1)		// 当前指令是除了NOP、LOAD外的其他指令
					if(stage[i] == 0)		// 若当前指令处于刚发射阶段
						if(!(resst[inUnitPos[i]+1][4].matches("") || resst[inUnitPos[i]+1][5].matches("")))	// 源操作数已经准备好
						{
							stage[i]++;
							instst[i+1][2] = Integer.toString(cnow)+"~";
							switch(intv[i][0])
							{
								case 2:		// ADD
									resst[inUnitPos[i]+1][0] = Integer.toString(time[1] - 1); break;
								case 3:		// SUB
									resst[inUnitPos[i]+1][0] = Integer.toString(time[1] - 1); break;
								case 4:		// MUL
									resst[inUnitPos[i]+1][0] = Integer.toString(time[2] - 1); break;
								case 5:		// DIV
									resst[inUnitPos[i]+1][0] = Integer.toString(time[3] - 1);
								default:
									System.out.println("Error");
							}
						}
						else;
					else if(stage[i] == 1)		// 若当前指令处于执行阶段
					{
						timetemp = Integer.parseInt(resst[inUnitPos[i]+1][0]);
						if(timetemp == 1)
						{
							resst[inUnitPos[i]+1][0] = "";
							stage[i]++;
							instst[i+1][2] = instst[i+1][2].concat(Integer.toString(cnow));
						}
						else
						{
							timetemp--;
							resst[inUnitPos[i]+1][0] = Integer.toString(timetemp);
						}
					}
					else if(stage[i] == 2)
					{
						finished[i] = 0;
						if(!wbFlag)
						{
							wbFlag = true;
							wbRecord = i;
						}
					}
			}

		if(wbFlag)		// 有指令正在写结果阶段
		{
			i = wbRecord;		// 当前需要写回的指令是wbRecord
			finRecord = wbRecord;
			finInsNum++;
			if(intv[i][0] == 1)		// 当前指令是LOAD
			{
				stage[i]++;
				// 修改寄存器状态里的值
				if(inUnitPos[i]+1 == Integer.parseInt(regst[1][1+intv[i][1]].charAt(4)+""))
				{
					regst[2][1+intv[i][1]] = result[rr];
					rr++;
				}
				// 更新保留站中的状态
				for(j = 0; j < 5; j++)
				{
					if(resst[j+1][6].matches(regst[1][1+intv[i][1]]))
					{
						if (!ldst[inUnitPos[i]+1][0].matches(resst[j+1][6]))
							continue;
						resst[j+1][4] = regst[2][1+intv[i][1]];
						resst[j+1][6] = "";
					}
					if(resst[j+1][7].matches(regst[1][1+intv[i][1]]))
					{
						if (!ldst[inUnitPos[i]+1][0].matches(resst[j+1][7]))
							continue;
						resst[j+1][5] = regst[2][1+intv[i][1]];
						resst[j+1][7] = "";
					}
				}
			}
			if(intv[i][0] > 1)		// 当前指令是NOP、LOAD外的其他指令
			{
				stage[i]++;
				// 修改寄存器状态里的值
				regst[2][1+intv[i][1]] = result[rr];
				rr++;
				// 更新保留站中的状态
				for(j = 0; j < 5; j++)
				{
					if(resst[j+1][6].matches(regst[1][1+intv[i][1]]))
					{
						if (!resst[inUnitPos[i]+1][1].matches(resst[j+1][6]))
							continue;
						resst[j+1][4] = regst[2][1+intv[i][1]];
						resst[j+1][6] = "";
					}
					if(resst[j+1][7].matches(regst[1][1+intv[i][1]]))
					{
						if (!resst[inUnitPos[i]+1][1].matches(resst[j+1][7]))
							continue;
						resst[j+1][5] = regst[2][1+intv[i][1]];
						resst[j+1][7] = "";
					}
				}
			}
			wbFlag = false;
		}

		// 提取指令
		if(instnow < 6)
			if(intv[instnow][0] == 0)		// NOP指令
			{
				instnow++;
				inUnitPos[i] = -1;
				finished[i] = 0;
				finInsNum++;
			}
			else if(intv[instnow][0] == 1)		// LOAD指令
			{
				// 提取出LOAD指令后，遍历LOAD部件寻找一个空位
				for(i = 0; i < unitIndex[0]; i++)		
					if(ld[i][0] == -1) break;
				if(i != unitIndex[0])		// 在LOAD部件中找到了一个空位
				{
					inUnitPos[instnow] = i;		// 更新指令占用的部件编号
					ld[i][0] = instnow;			// 更新部件正在操作的指令编号
					ld[i][1] = time[0];			// 剩余时间设置为用户配置的时间
					stage[instnow] = 0;			// 更新当前指令状态
					instnow++;
					// 接下来刷新“指令状态”“LOAD部件”“寄存器状态”面板的信息
					instst[instnow][1] = Integer.toString(cnow);
					ldst[i+1][1] = "yes";
					ldst[i+1][2] = ix[intv[ ld[i][0] ][2]];
					regst[1][1+intv[ld[i][0]][1]] = "Load"+Integer.toString(i+1);		
				}
			}
			else if(intv[instnow][0] > 1)		// 其他指令
			{
				// 为不同指令在运算器中寻找空位
				if(intv[instnow][0] == 2 || intv[instnow][0] == 3)		// ADD SUB
					for(i = 0; i < 3; i++)
						if(cal[i][0] == -1) break;
				if(intv[instnow][0] == 4 || intv[instnow][0] == 5)		// MUL DIV
					for(i = 3; i < 5; i++)
						if(cal[i][0] == -1) break;
				if( (intv[instnow][0] == 2 || intv[instnow][0] == 3) && i != 3 ||
				(intv[instnow][0] == 4 || intv[instnow][0] == 5) && i != 5 )
				{
					inUnitPos[instnow] = i;		// 更新指令占用的部件编号
					cal[i][0] = instnow;		// 更新部件正在操作的指令编号
					switch(intv[cal[i][0]][0])	// 设置运算部件被占用的剩余时间
					{
						case 2:
							cal[i][1] = time[1];break;
						case 3:
							cal[i][1] = time[1];break;
						case 4:
							cal[i][1] = time[2];break;
						case 5:
							cal[i][1] = time[3];break;
					}
					stage[instnow] = 0;			// 更新指令状态为已发射
					instnow++;
					// 刷新“指令状态”“保留站”“寄存器状态”面板信息
					instst[instnow][1] = Integer.toString(cnow);
					resst[i+1][2] = "yes";
					resst[i+1][3] = inst[intv[cal[i][0]][0]];
					// 先考虑源操作数j
					// 源寄存器未在寄存器状态中，其值可以直接引用，故把引用值填入V处
					if(regst[1][1+intv[cal[i][0]][2]].matches(""))		
					{
						resst[i+1][4] = "R["+fx[intv[cal[i][0]][2]]+"]"; 
						cal[i][2] = 1;
					}
					// 源寄存器在寄存器状态中，但是其值还未算出来，故只能先把产生数据的操作名填入Q处
					else if(regst[2][1+intv[cal[i][0]][2]].matches(""))
					{
						resst[i+1][6] = regst[1][1+intv[cal[i][0]][2]];
						cal[i][2] = 0;
					}
					// 源寄存器在寄存器状态中，且其值已经计算出来，故可以直接把计算好的值填入V处
					else
					{
						resst[i+1][4] = regst[2][1+intv[cal[i][0]][2]];
						cal[i][2] = 1;
					}
					// 再考虑源操作数k，对其进行一样的分析
					if(regst[1][1+intv[cal[i][0]][3]].matches(""))
					{
						resst[i+1][5] = "R["+fx[intv[cal[i][0]][3]]+"]"; 
						cal[i][2] = 1;
					}
					else if(regst[2][1+intv[cal[i][0]][3]].matches(""))
					{
						resst[i+1][7] = regst[1][1+intv[cal[i][0]][3]];
						cal[i][2] = 0;
					}
					else
					{
						resst[i+1][5] = regst[2][1+intv[cal[i][0]][3]];
						cal[i][2] = 1;
					}
					// 最后考虑目的操作数
					switch(i)
					{
						case 0:
							regst[1][1+intv[cal[i][0]][1]] = "Add1";
							break;
						case 1:
							regst[1][1+intv[cal[i][0]][1]] = "Add2";
							break;
						case 2:
							regst[1][1+intv[cal[i][0]][1]] = "Add3";
							break;
						case 3:
							regst[1][1+intv[cal[i][0]][1]] = "Mult1";
							break;
						case 4:
							regst[1][1+intv[cal[i][0]][1]] = "Mult2";
							break;
						default:
							System.out.println("Error2: core--other!");
							break;
					}
				}
			}
		
		// 处理已经执行完的指令
		if(finRecord >=0 && finRecord < 6 && finished[finRecord] >= 0)
		{
			i = finRecord;
			if(intv[i][0] == 1)		// 指令是 LOAD
			{
				// 刷新“指令状态”界面信息
				instst[i+1][3] = Integer.toString(cnow);
				// 刷新“LOAD部件”界面信息
				ldst[inUnitPos[i]+1][1] = "no";
				ldst[inUnitPos[i]+1][2] = "";
				ldst[inUnitPos[i]+1][3] = "";
				// 更新load部件信息
				ld[inUnitPos[i]][0] = -1;
				// 更新指令使用的部件信息
				inUnitPos[i] = -1;
			}
			if(intv[i][0] > 1)		// 指令不是 LOAD
			{
				// 刷新“指令状态”界面信息
				instst[i+1][3] = Integer.toString(cnow);
				// 刷新“保留站”界面信息
				resst[inUnitPos[i]+1][2] = "no";
				resst[inUnitPos[i]+1][3] = "";
				resst[inUnitPos[i]+1][4] = "";
				resst[inUnitPos[i]+1][5] = "";
				resst[inUnitPos[i]+1][6] = "";
				resst[inUnitPos[i]+1][7] = "";
				// 更新运算器信息
				cal[inUnitPos[i]][0] = -1;
				// 更新指令使用的部件信息
				inUnitPos[i] = -1;
			}
			finRecord = -1; 
		}	
	}

	public static void main(String[] args) {
		new Tomasulo();
	}

}
