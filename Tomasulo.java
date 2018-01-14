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
 * ģ��˵������ģ����Ҫ�ṩ����Swing����ṩ��JPanle��JFrame��JButton���ṩ��GUI��ʹ�á���������ģʽ��������Button���¼����Ӷ����ݾ����¼�ִ�в�ͬ������
 * Tomasulo�㷨������ͬѧ��������ɣ���˵����4��
 * ���ڽ�������޸Ĳ��֣���˵��(1),(2),(3)
 *
 *  (1)˵������������������ָ�������е�����������
 *	(2)˵���������������ָ��������壨ָ��״̬������վ��Load�������Ĵ����������Ĵ�С
 *	(3)˵�������ý���Ĭ��ָ��
 *	(4)˵���� Tomasulo�㷨ʵ��
 */

public class Tomasulo extends JFrame implements ActionListener{
	/*
	 * ��������������壺
	 * panel1 : ָ������
	 * panel2 : ִ��ʱ������
	 * panel3 : ָ��״̬
	 * panel4 : ����վ״̬
	 * panel5 : Load����
	 * panel6 : �Ĵ���״̬
	 */
	private JPanel panel1,panel2,panel2_1,panel3,panel4,panel5,panel6;

	/*
	 * �ĸ�������ť����������5�������ã�ִ��
	 */
	private JButton stepbut,step5but,resetbut,startbut;

	/*
	 * ָ��ѡ���
	 */
	private JComboBox instbox[]=new JComboBox[24];

	/*
	 * ÿ����������
	 */
	private JLabel instl, timel, numl, tl1,tl2,tl3,tl4,resl,regl,ldl,insl,stepsl;
	private int time[]=new int[4]; //��������ʱ��

	/*
	 * ����ִ��ʱ��������
	 */
	private JTextField tt1,tt2,tt3,tt4;

	private int intv[][]=new int[6][4];//��¼ÿ��ָ����ĸ���������,��Ӧ�������index
	private int cnow,instnow=0;//cnow ��ǰִ������ clock_now; instnow ��ǰִ��ָ����,instruction now
	private int cal[][]={{-1,0,0},{-1,0,0},{-1,0,0},{-1,0,0},{-1,0,0}};
	//���㲿��,3 bits,first bit -- -1 nop,others ִ��ָ����,second bit -- execute time,third -- ������ busy tag
	private int ld[][]={{0,0},{0,0},{0,0}};//���ز�����2 bits,first bit--busy tag,second--remaining  execute time
	private int ff[]={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};//������busy tag
	
	/*
	 * ��������������
	 */
	private JComboBox[] unitN = new JComboBox[3];
	private int[] unitIndex = {2,2,1};
	/*
	 * �Զ������
	 */
	private int inUnitPos[] = {-1, -1, -1, -1, -1, -1};//ָ��ʹ�ò����ڲ����
	private int stage[] = {-1, -1, -1, -1, -1, -1};//��ǰָ��ִ�н׶�
	private int rr = 0;//��������temp��
	private boolean wbFlag = false;//wbFlag д�ز������
	private int wbRecord = 0;//wbRecord ����д����ָ����
	private int finished[] = {-1,-1,-1,-1,-1,-1};//��ǰָ��ִ�н׶ν������
	private int finRecord;//��ǰ���ָ����
	private int finInsNum=0;//�����ָ����
	private boolean endTag = false; //ָ����ִ����
	
	/*
	 * (1)˵������������������ָ�������е�����������
	 * inst�� ָ������������:"NOP","L.D","ADD.D","SUB.D","MULT.D","DIV.D"��������
	 * fx��       Ŀ�ļĴ�������������:"F0","F2","F4","F6","F8" ��������
	 * rx��       Դ�������Ĵ�������:"R0","R1","R2","R3","R4","R5","R6","R7","R8","R9" ��������
	 * ix��       ����������������:"0","1","2","3","4","5","6","7","8","9" ��������
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
	 * (2)˵���������������ָ��������壨ָ��״̬������վ��Load�������Ĵ����������Ĵ�С
	 * 		ָ��״̬ ���
	 * 		����վ ���
	 * 		Load���� ���
	 * 		�Ĵ��� ���
	 * 					�Ĵ�С
	 */
	private	String  instst[][]=new String[7][4], resst[][]=new String[6][8],
					ldst[][]=new String[4][4], regst[][]=new String[3][17];
	private	JLabel  instjl[][]=new JLabel[7][4], resjl[][]=new JLabel[6][8],
					ldjl[][]=new JLabel[4][4], regjl[][]=new JLabel[3][17];

	//���췽��
	public Tomasulo(){
		super("Tomasulo Simulator");

		//���ò���
		Container cp=getContentPane();
		FlowLayout layout=new FlowLayout(FlowLayout.LEFT,8,6);
		cp.setLayout(layout);

		//ָ�����á�GridLayout(int ָ������, int ������+������, int hgap, int vgap)
		instl = new JLabel("ָ������");
		panel1 = new JPanel(new GridLayout(6,4,0,0));
		panel1.setPreferredSize(new Dimension(350, 150));
		panel1.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		//ִ��ʱ��
		timel = new JLabel("ִ��ʱ������");
		panel2 = new JPanel(new GridLayout(2,4,0,0));
		panel2.setPreferredSize(new Dimension(280, 80));
		panel2.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		//ִ�в�����Ŀ
		numl = new JLabel("ִ�в�����Ŀ����");
		panel2_1 = new JPanel(new GridLayout(3,2,0,0));
		panel2_1.setPreferredSize(new Dimension(300, 80));
		panel2_1.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		
		//ָ��״̬
		insl = new JLabel("ָ��״̬");
		panel3 = new JPanel(new GridLayout(7,4,0,0));
		panel3.setPreferredSize(new Dimension(420, 175));
		panel3.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		//����վ
		resl = new JLabel("����վ");
		panel4 = new JPanel(new GridLayout(6,7,0,0));
		panel4.setPreferredSize(new Dimension(420, 150));
		panel4.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		//Load����
		ldl = new JLabel("Load����");
		panel5 = new JPanel(new GridLayout(4,4,0,0));
		panel5.setPreferredSize(new Dimension(300, 100));
		panel5.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		//�Ĵ���״̬
		regl = new JLabel("�Ĵ���");
		panel6 = new JPanel(new GridLayout(3,17,0,0));
		panel6.setPreferredSize(new Dimension(740, 75));
		panel6.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		tl1 = new JLabel("Load");
		tl2 = new JLabel("��/��");
		tl3 = new JLabel("�˷�");
		tl4 = new JLabel("����");

		//������ť:ִ�У����裬����������5��
		stepsl = new JLabel();
		stepsl.setPreferredSize(new Dimension(200, 30));
		stepsl.setHorizontalAlignment(SwingConstants.CENTER);
		stepsl.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		stepbut = new JButton("����");
		stepbut.addActionListener(this);
		step5but = new JButton("����5��");
		step5but.addActionListener(this);
		startbut = new JButton("ִ��");
		startbut.addActionListener(this);
		resetbut= new JButton("����");
		resetbut.addActionListener(this);
		tt1 = new JTextField("2");
		tt2 = new JTextField("2");
		tt3 = new JTextField("10");
		tt4 = new JTextField("40");

		//ָ������
		/*
		 * ����ָ��ѡ��򣨲����룬���������������ȣ���defaultѡ��
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
		 * (3)˵�������ý���Ĭ��ָ���������Ƶ�ָ��������ȵ�ѡ��Χ�������á�
		 * Ĭ��6��ָ����޸�
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

		//ִ��ʱ������
		panel2.add(tl1);
		panel2.add(tt1);
		panel2.add(tl2);
		panel2.add(tt2);
		panel2.add(tl3);
		panel2.add(tt3);
		panel2.add(tl4);
		panel2.add(tt4);
		
		
		//ִ�в�����Ŀ����
		
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
		panel2_1.add(new JLabel("�ӷ�"));
		panel2_1.add(unitN[1]);
		panel2_1.add(new JLabel("�˷�/����"));
		panel2_1.add(unitN[2]);
		

		//ָ��״̬����
		for (int i=0;i<7;i++)
		{
			for (int j=0;j<4;j++){
				instjl[i][j]=new JLabel(instst[i][j]);
				instjl[i][j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
				panel3.add(instjl[i][j]);
			}
		}
		//����վ����
		for (int i=0;i<6;i++)
		{
			for (int j=0;j<8;j++){
				resjl[i][j]=new JLabel(resst[i][j]);
				resjl[i][j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
				panel4.add(resjl[i][j]);
			}
		}
		//Load��������
		for (int i=0;i<4;i++)
		{
			for (int j=0;j<4;j++){
				ldjl[i][j]=new JLabel(ldst[i][j]);
				ldjl[i][j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
				panel5.add(ldjl[i][j]);
			}
		}
		//�Ĵ�������
		for (int i=0;i<3;i++)
		{
			for (int j=0;j<17;j++){
				regjl[i][j]=new JLabel(regst[i][j]);
				regjl[i][j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
				panel6.add(regjl[i][j]);
			}
		}

		//������������ϲ���
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
	 * �����ִ�С���ť�󣬸���ѡ���ָ���ʼ�������������
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
		instst[0][0]="ָ��";
		instst[0][1]="����";
		instst[0][2]="ִ��";
		instst[0][3]="д��";


		ldst[0][0]="����";
		ldst[0][1]="Busy";
		ldst[0][2]="��ַ";
		ldst[0][3]="ֵ";
		ldst[1][0]="Load1";
		ldst[2][0]="Load2";
		ldst[3][0]="Load3";
		ldst[1][1]="no";
		ldst[2][1]="no";
		ldst[3][1]="no";

		resst[0][0]="Time";
		resst[0][1]="����";
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

		regst[0][0]="�ֶ�";
		for (int i=1;i<17;i++){
			regst[0][i]=fx[i-1];

		}
		regst[1][0]="״̬";
		regst[2][0]="ֵ";

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
		
		//�Զ�������ĳ�ʼ��
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
	 * ���������ť��������ʾ���
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
		stepsl.setText("��ǰ���ڣ�"+String.valueOf(cnow-1));
	}

	public void actionPerformed(ActionEvent e){
		//�����ִ�С���ť�ļ�����
		if (e.getSource()==startbut) {
			for (int i=0;i<24;i++) instbox[i].setEnabled(false);
			for (int i=0;i<3;i++)	unitN[i].setEnabled(false);
			tt1.setEnabled(false);tt2.setEnabled(false);
			tt3.setEnabled(false);tt4.setEnabled(false);
			stepbut.setEnabled(true);
			step5but.setEnabled(true);
			startbut.setEnabled(false);
			//����ָ�����õ�ָ���ʼ�����������
			init();
			cnow=1;
			//չʾ�������
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
		//��������á���ť�ļ�����
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
		//�������������ť�ļ�����
		if (e.getSource()==stepbut) {
			if(endTag) {
				stepbut.setEnabled(false);
				step5but.setEnabled(false);
			}
			core();
			cnow++;
			display();
		}
		//�������5������ť�ļ�����
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
	 * (4)˵���� Tomasulo�㷨ʵ��
	 */
	public void core()
	{
		if(finInsNum >= 6)
			endTag = true;
		
		int i = 0, j = 0, timetemp;

		// ִ��һ����
		for(i = 0; i < instnow; i++)
			if(inUnitPos[i] != -1)		// ����ǰָ�ռ���Ų���������δִ�����
			{
				if(intv[i][0] == 1) 	// ��ǰָ����LOADָ��
					if(stage[i] == 0) 	// ����ǰָ��ڸշ���׶�
					{
						stage[i]++;
						ldst[inUnitPos[i]+1][2] = "R["+rx[intv[i][3]]+"]+"+ix[intv[i][2]];
						instst[i+1][2] = Integer.toString(cnow)+"~";
						ld[inUnitPos[i]][1]--;		// ����ʣ��ʱ��
					}
					else if(stage[i] == 1)	// ����ǰָ������ִ�н׶�
						if(ld[inUnitPos[i]][1] == 1)	// ��ֻʣ���һ��ִ�����ڣ������ֿ���ִ����� 
						{
							stage[i]++;									
							ldst[inUnitPos[i] + 1][3] = "M[R["+rx[intv[i][3]]+"]+"+ix[intv[i][2]]+"]";
							instst[i+1][2] = instst[i+1][2] + (Integer.toString(cnow));
						}
						else		// �����ֲ���ִ����ϣ����ȥһ��ִ�����ڼ���
							ld[inUnitPos[i]][1]--;
					else if(stage[i] == 2)	// ����ǰָ������д�ؽ׶�
					{
						finished[i] = 0;	// ��ǵ�ǰָ�������				
						if(!wbFlag)
						{
							wbFlag = true;
							wbRecord = i;
						}
					}
				if(intv[i][0] > 1)		// ��ǰָ���ǳ���NOP��LOAD�������ָ��
					if(stage[i] == 0)		// ����ǰָ��ڸշ���׶�
						if(!(resst[inUnitPos[i]+1][4].matches("") || resst[inUnitPos[i]+1][5].matches("")))	// Դ�������Ѿ�׼����
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
					else if(stage[i] == 1)		// ����ǰָ���ִ�н׶�
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

		if(wbFlag)		// ��ָ������д����׶�
		{
			i = wbRecord;		// ��ǰ��Ҫд�ص�ָ����wbRecord
			finRecord = wbRecord;
			finInsNum++;
			if(intv[i][0] == 1)		// ��ǰָ����LOAD
			{
				stage[i]++;
				// �޸ļĴ���״̬���ֵ
				if(inUnitPos[i]+1 == Integer.parseInt(regst[1][1+intv[i][1]].charAt(4)+""))
				{
					regst[2][1+intv[i][1]] = result[rr];
					rr++;
				}
				// ���±���վ�е�״̬
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
			if(intv[i][0] > 1)		// ��ǰָ����NOP��LOAD�������ָ��
			{
				stage[i]++;
				// �޸ļĴ���״̬���ֵ
				regst[2][1+intv[i][1]] = result[rr];
				rr++;
				// ���±���վ�е�״̬
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

		// ��ȡָ��
		if(instnow < 6)
			if(intv[instnow][0] == 0)		// NOPָ��
			{
				instnow++;
				inUnitPos[i] = -1;
				finished[i] = 0;
				finInsNum++;
			}
			else if(intv[instnow][0] == 1)		// LOADָ��
			{
				// ��ȡ��LOADָ��󣬱���LOAD����Ѱ��һ����λ
				for(i = 0; i < unitIndex[0]; i++)		
					if(ld[i][0] == -1) break;
				if(i != unitIndex[0])		// ��LOAD�������ҵ���һ����λ
				{
					inUnitPos[instnow] = i;		// ����ָ��ռ�õĲ������
					ld[i][0] = instnow;			// ���²������ڲ�����ָ����
					ld[i][1] = time[0];			// ʣ��ʱ������Ϊ�û����õ�ʱ��
					stage[instnow] = 0;			// ���µ�ǰָ��״̬
					instnow++;
					// ������ˢ�¡�ָ��״̬����LOAD���������Ĵ���״̬��������Ϣ
					instst[instnow][1] = Integer.toString(cnow);
					ldst[i+1][1] = "yes";
					ldst[i+1][2] = ix[intv[ ld[i][0] ][2]];
					regst[1][1+intv[ld[i][0]][1]] = "Load"+Integer.toString(i+1);		
				}
			}
			else if(intv[instnow][0] > 1)		// ����ָ��
			{
				// Ϊ��ָͬ������������Ѱ�ҿ�λ
				if(intv[instnow][0] == 2 || intv[instnow][0] == 3)		// ADD SUB
					for(i = 0; i < 3; i++)
						if(cal[i][0] == -1) break;
				if(intv[instnow][0] == 4 || intv[instnow][0] == 5)		// MUL DIV
					for(i = 3; i < 5; i++)
						if(cal[i][0] == -1) break;
				if( (intv[instnow][0] == 2 || intv[instnow][0] == 3) && i != 3 ||
				(intv[instnow][0] == 4 || intv[instnow][0] == 5) && i != 5 )
				{
					inUnitPos[instnow] = i;		// ����ָ��ռ�õĲ������
					cal[i][0] = instnow;		// ���²������ڲ�����ָ����
					switch(intv[cal[i][0]][0])	// �������㲿����ռ�õ�ʣ��ʱ��
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
					stage[instnow] = 0;			// ����ָ��״̬Ϊ�ѷ���
					instnow++;
					// ˢ�¡�ָ��״̬��������վ�����Ĵ���״̬�������Ϣ
					instst[instnow][1] = Integer.toString(cnow);
					resst[i+1][2] = "yes";
					resst[i+1][3] = inst[intv[cal[i][0]][0]];
					// �ȿ���Դ������j
					// Դ�Ĵ���δ�ڼĴ���״̬�У���ֵ����ֱ�����ã��ʰ�����ֵ����V��
					if(regst[1][1+intv[cal[i][0]][2]].matches(""))		
					{
						resst[i+1][4] = "R["+fx[intv[cal[i][0]][2]]+"]"; 
						cal[i][2] = 1;
					}
					// Դ�Ĵ����ڼĴ���״̬�У�������ֵ��δ���������ֻ���ȰѲ������ݵĲ���������Q��
					else if(regst[2][1+intv[cal[i][0]][2]].matches(""))
					{
						resst[i+1][6] = regst[1][1+intv[cal[i][0]][2]];
						cal[i][2] = 0;
					}
					// Դ�Ĵ����ڼĴ���״̬�У�����ֵ�Ѿ�����������ʿ���ֱ�ӰѼ���õ�ֵ����V��
					else
					{
						resst[i+1][4] = regst[2][1+intv[cal[i][0]][2]];
						cal[i][2] = 1;
					}
					// �ٿ���Դ������k���������һ���ķ���
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
					// �����Ŀ�Ĳ�����
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
		
		// �����Ѿ�ִ�����ָ��
		if(finRecord >=0 && finRecord < 6 && finished[finRecord] >= 0)
		{
			i = finRecord;
			if(intv[i][0] == 1)		// ָ���� LOAD
			{
				// ˢ�¡�ָ��״̬��������Ϣ
				instst[i+1][3] = Integer.toString(cnow);
				// ˢ�¡�LOAD������������Ϣ
				ldst[inUnitPos[i]+1][1] = "no";
				ldst[inUnitPos[i]+1][2] = "";
				ldst[inUnitPos[i]+1][3] = "";
				// ����load������Ϣ
				ld[inUnitPos[i]][0] = -1;
				// ����ָ��ʹ�õĲ�����Ϣ
				inUnitPos[i] = -1;
			}
			if(intv[i][0] > 1)		// ָ��� LOAD
			{
				// ˢ�¡�ָ��״̬��������Ϣ
				instst[i+1][3] = Integer.toString(cnow);
				// ˢ�¡�����վ��������Ϣ
				resst[inUnitPos[i]+1][2] = "no";
				resst[inUnitPos[i]+1][3] = "";
				resst[inUnitPos[i]+1][4] = "";
				resst[inUnitPos[i]+1][5] = "";
				resst[inUnitPos[i]+1][6] = "";
				resst[inUnitPos[i]+1][7] = "";
				// ������������Ϣ
				cal[inUnitPos[i]][0] = -1;
				// ����ָ��ʹ�õĲ�����Ϣ
				inUnitPos[i] = -1;
			}
			finRecord = -1; 
		}	
	}

	public static void main(String[] args) {
		new Tomasulo();
	}

}
