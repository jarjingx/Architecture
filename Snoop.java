package snoop;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

class Shower extends Thread {
	private JLabel label;
	private final int fresh_time = 100;
	private final int fresh_num = 5;
	private final int gap_time = 0;
	
	private Color cb,ce;

	public Shower(JLabel label, Color cb, Color ce) {
		super();
		this.label = label;
		this.cb = cb;
		this.ce = ce;
	}
	public Shower(JLabel label, Color ce) {
		super();
		this.label = label;
		this.cb = Color.white;
		this.ce = ce;
	}
	

	@Override
	public void run() {
		try {
			Thread.sleep(gap_time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < fresh_num; i ++) {
			label.setBackground(this.cb);
			label.repaint();
			try {
				Thread.sleep(fresh_time);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			label.setBackground(this.ce);
			label.repaint();
			try {
				Thread.sleep(fresh_time);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}

public class Snoop extends JFrame implements ActionListener{
	//缓存参数
	final int CPUnum = 4;
	int cachePerCPU = 4;
	int blockPerGroup = 8;
	
	//界面参数
	final int width = 1200;
	final int height = 700;
	final int x = 50, y = 20;
	
	//操作界面参数
	final int opPanel_w = width, opPanel_h = 70;
	final int opSubPanel_gap = 30;
	final int opSubPanel_w = (width - CPUnum * opSubPanel_gap)/ CPUnum, opSubPanel_h = opPanel_h - 20;
	final int opLabel_w = opSubPanel_w / 3, opLabel_h = opSubPanel_h;
	final int opTf_w = opSubPanel_w / 6, opTf_h = opSubPanel_h;
	final int op_csBox_w = opSubPanel_w / 6, op_csBox_h = opSubPanel_h;
	final int btn_w = opSubPanel_w / 3, btn_h = opSubPanel_h;
	
	//主界面参数
	final int showPanel_w = width, showPanel_h = 400;
	final int showPanel_gap = 50;
	final int cache_w = 100, cache_h = 20;
	final int block_w = 150, block_h = 20;
	final int tips_w = 30, tips_h = block_h;
	
	//结果界面参数
	final int msgNum = 10;
	final int orderPanel_w = 300, orderPanel_h = 140;
	final int orderElem_w = orderPanel_w - 50, orderElem_h = (orderPanel_h - 20) / 3;
	final int resultPanel_w = width - orderPanel_w - 100, resultPanel_h = 140;
	
	//cache 界面
	final int showCachePanel_w = width;
	int showCachePanel_h = (cachePerCPU + 3) * cache_h;
	final int subCachePanel_gap = showCachePanel_w / CPUnum - (cache_w + tips_w) ;
	
	//存储器界面
	final int showBlockPanel_w = width - 300;
	int showBlockPanel_h = (blockPerGroup + 3) * block_h;
	final int subBlockPanel_gap = showBlockPanel_w / CPUnum - (block_w + tips_w);
	
	//操作界面变量
	private JPanel opPanel;
	private JPanel[] opSubPanel;
	private JLabel[] opLabel = new JLabel[CPUnum];
	private JTextField[] opTf; //Tf: textField
	private final String[] op_model = {"读", "写"};
	private JComboBox[] op_csBox;
	private JButton[] opBtn;
	
	//主界面变量
	private JPanel showCachePanel;
	private JPanel[] subCachePanel;
	private JLabel[][] showCacheLabel;
	private JLabel[][] showCacheTips;
	
	private JPanel showBlockPanel;
	private JPanel[] subBlockPanel;
	private JLabel[][] showBlockLabel;
	private JLabel[][] showBlockTips;
	
	//操作结果界面变量
	private JPanel resultPanel;
	private JLabel msg[] = new JLabel[msgNum];
	private int msg_idx = 0;
	
	//命令界面变量
	private JPanel orderPanel;
	private JButton resetBtn;
	private final String cache_num[] = {"2", "3", "4", "5"};
	private final String block_num[] = {"6", "7", "8", "9", "10"};
	private final String connect_num[] = {"1", "2", "3", "4", "5"};
	private JComboBox cache_csBox = new JComboBox(cache_num);
	private JComboBox block_csBox = new JComboBox(block_num);
	private JComboBox connect_csBox = new JComboBox(connect_num);
	
	//程序变量
	public enum State {
		Invalid,Shared,Modified
	}
	
	private int[][] cache;
	private State[][] cache_state;
	
	public Snoop() {
		super("监听法");
		cache_csBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				cachePerCPU = cache_csBox.getSelectedIndex() + 2;
				reset();
			}
		});
		cache_csBox.setSelectedIndex(2);
		cache_csBox.setPreferredSize(new Dimension(orderElem_w / 3, orderElem_h));
		block_csBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				blockPerGroup = block_csBox.getSelectedIndex() + 6;
				reset();
			}
		});
		block_csBox.setPreferredSize(new Dimension(orderElem_w / 3, orderElem_h));
		block_csBox.setSelectedIndex(2);
		reset();
	}
	public void reset() {
		/*
		 * 变量初始化
		 */
		msg_clear();
		cache = new int[CPUnum][cachePerCPU];
		cache_state = new State[CPUnum][cachePerCPU];
		
		orderPanel = new JPanel();
		resetBtn = new JButton("重置");
		
		resultPanel = new JPanel(new GridLayout(msgNum, 1, 0, 0));
		
		
		showCachePanel = new JPanel(new GridLayout(1, CPUnum, subCachePanel_gap, 0));
		subCachePanel = new JPanel[CPUnum];
		showCacheLabel = new JLabel[CPUnum][cachePerCPU+1];
		showCacheTips = new JLabel[CPUnum][cachePerCPU+1];
		
		showBlockPanel = new JPanel(new GridLayout(1, CPUnum, subBlockPanel_gap, 0));
		subBlockPanel = new JPanel[CPUnum];
		showBlockLabel = new JLabel[CPUnum][blockPerGroup];
		showBlockTips = new JLabel[CPUnum][blockPerGroup];
		
		opPanel = new JPanel(new GridLayout(1,CPUnum,0,opSubPanel_gap));
		opSubPanel = new JPanel[CPUnum];
		opTf = new JTextField[CPUnum]; //Tf: textField
		op_csBox = new JComboBox[CPUnum];
		opBtn = new JButton[CPUnum];
		
		showCachePanel_h = (cachePerCPU + 3) * cache_h;
		showBlockPanel_h = (blockPerGroup + 3) * block_h;
		
		
		/*
		 * 窗体设置
		 */
		this.getContentPane().removeAll();
		Container cp=getContentPane();
		FlowLayout layout=new FlowLayout(FlowLayout.CENTER,8,6);
		cp.setLayout(layout);
		
		opPanel.setPreferredSize(new Dimension(opPanel_w, opPanel_h));
		for(int i=0; i < CPUnum; i++)
		{
			opSubPanel[i] = new JPanel();
			opSubPanel[i].setPreferredSize(new Dimension(opSubPanel_w, opSubPanel_h));
			
			opLabel[i] = new JLabel("访问地址");
			opLabel[i].setPreferredSize(new Dimension(opLabel_w, opLabel_h));
			opLabel[i].setHorizontalAlignment(JLabel.RIGHT);
					
			opTf[i] = new JTextField();
			opTf[i].setPreferredSize(new Dimension(opTf_w, opTf_h));
			
			op_csBox[i] = new JComboBox(op_model);
			op_csBox[i].setPreferredSize(new Dimension(op_csBox_w, op_csBox_h));
			
			opBtn[i] = new JButton("执行");
			opBtn[i].setPreferredSize(new Dimension(btn_w, btn_h));
			opBtn[i].addActionListener(this);
			
			opSubPanel[i].add(opLabel[i]);
			opSubPanel[i].add(opTf[i]);
			opSubPanel[i].add(op_csBox[i]);
			opSubPanel[i].add(opBtn[i]);
			opPanel.add(opSubPanel[i]);
		}
		
		Dimension blcokDim = new Dimension(block_w, block_h);
		Dimension tipsDim = new Dimension(tips_w, tips_h);
		Dimension cacheDim = new Dimension(cache_w, cache_h);
		for(int i = 0; i < CPUnum; i++) {
			//cache
			subCachePanel[i] = new JPanel();
			
			showCacheLabel[i][0] = new JLabel("CPU "+ i);
			showCacheLabel[i][0].setHorizontalAlignment(JLabel.CENTER);
			showCacheLabel[i][0].setPreferredSize(cacheDim);
			showCacheTips[i][0] = new JLabel();
			showCacheTips[i][0].setPreferredSize(tipsDim);
			subCachePanel[i].add(showCacheTips[i][0]);
			subCachePanel[i].add(showCacheLabel[i][0]);
			
			for(int j = 1; j <= cachePerCPU; j++) {
				showCacheTips[i][j] = new JLabel("" + (j - 1));
				showCacheTips[i][j].setHorizontalAlignment(JLabel.RIGHT);
				showCacheTips[i][j].setPreferredSize(tipsDim);
				
				showCacheLabel[i][j] = new JLabel();
				showCacheLabel[i][j].setBackground(Color.gray);
				showCacheLabel[i][j].setOpaque(true);
				showCacheLabel[i][j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
				showCacheLabel[i][j].setPreferredSize(cacheDim);
				
				subCachePanel[i].add(showCacheTips[i][j]);
				subCachePanel[i].add(showCacheLabel[i][j]);
			}
			showCachePanel.add(subCachePanel[i]);
			
			//存储器
			subBlockPanel[i] = new JPanel();
			
			for(int j = 0; j < blockPerGroup; j++) {
				showBlockTips[i][j] = new JLabel("" + (i * blockPerGroup + j));
				showBlockTips[i][j].setHorizontalAlignment(JLabel.RIGHT);
				showBlockTips[i][j].setPreferredSize(tipsDim);
				
				showBlockLabel[i][j] = new JLabel();
				showBlockLabel[i][j].setBackground(Color.white);
				showBlockLabel[i][j].setOpaque(true);
				showBlockLabel[i][j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
				showBlockLabel[i][j].setPreferredSize(blcokDim);
				
				subBlockPanel[i].add(showBlockTips[i][j]);
				subBlockPanel[i].add(showBlockLabel[i][j]);
			}
			showBlockPanel.add(subBlockPanel[i]);
		}
		showCachePanel.setPreferredSize(new Dimension(showCachePanel_w, showCachePanel_h));
		showBlockPanel.setPreferredSize(new Dimension(showBlockPanel_w, showBlockPanel_h));	
		
		cp.add(opPanel);
		cp.add(showCachePanel);
		cp.add(showBlockPanel);
		
		for (int i=0; i < msgNum; i++) {
			msg[i] = new JLabel();
			resultPanel.add(msg[i]);
		}
		resetBtn.setPreferredSize(new Dimension(orderElem_w, orderElem_h));
		resetBtn.addActionListener(this);
		JLabel tips1 = new JLabel("cpu内cache块数"),tips2 = new JLabel("组存储器块数"),tips3 = new JLabel("相联度");
		tips1.setPreferredSize(new Dimension(orderElem_w/3 * 2, orderElem_h));
		tips2.setPreferredSize(new Dimension(orderElem_w/3 * 2, orderElem_h));
		tips3.setPreferredSize(new Dimension(orderElem_w/3 * 2, orderElem_h));
		orderPanel.add(tips1);
		orderPanel.add(cache_csBox);
		orderPanel.add(tips2);
		orderPanel.add(block_csBox);
		orderPanel.add(tips3);
		connect_csBox.setPreferredSize(new Dimension(orderElem_w / 3, orderElem_h));
		orderPanel.add(connect_csBox);
		orderPanel.add(resetBtn);
		
		orderPanel.setPreferredSize(new Dimension(orderPanel_w, orderPanel_h));
		orderPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		cp.add(orderPanel);
		
		
		resultPanel.setPreferredSize(new Dimension(resultPanel_w, resultPanel_h));
		resultPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		cp.add(resultPanel);
		
		this.setBounds(x, y, width, height);
		this.setResizable(false);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		init();
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		for(int i = 0; i < CPUnum; i++) {
			if(e.getSource() == opBtn[i]) {
				int op = op_csBox[i].getSelectedIndex();
				String address_str = opTf[i].getText();
				if (address_str.equals("")) {
					JOptionPane.showConfirmDialog(null,"输入地址不对","提示",JOptionPane.DEFAULT_OPTION);
					return;
				}
				int address = Integer.parseInt(opTf[i].getText());
				if (address < 0 || address >= CPUnum * blockPerGroup) {
					JOptionPane.showConfirmDialog(null,"输入地址不对","提示",JOptionPane.DEFAULT_OPTION);
					return;
				}
				else {
					for(int j = 0; j < CPUnum; j++) {
						opBtn[i].setEnabled(false);
						op_csBox[i].setEnabled(false);
						opTf[i].setEditable(false);
					}
					
					execute(i, address, op);
					
					for(int j = 0; j < CPUnum; j++) {
						opBtn[i].setEnabled(true);
						op_csBox[i].setEnabled(true);
						opTf[i].setEditable(true);
					}
				}
			}
		}
		if(e.getSource() == resetBtn) {
			reset();
		}
	}
	
	public void init() {
		for (int i = 0; i < CPUnum; i++) {
			for(int j = 0; j < cachePerCPU; j++) {
				cache_state[i][j] = State.Invalid;
			}
		}
	}
	public void execute(int cpuNo, int address, int op) {
		msg_clear();
		if(op == 0)
			write_msg("cpu" + cpuNo +"读操作" + " 主存块号" + address);
		else
			write_msg("cpu" + cpuNo + "写操作" + " 主存块号" + address);

		//判断是否在本地cache中
		int ind = address % cachePerCPU;
		
		if(cache_state[cpuNo][ind] != State.Invalid) {
			if(cache[cpuNo][ind] == address) {
				write_msg("命中");
				cacheHit(cpuNo, ind);
				if(op==0) return;
				showCacheLabel[cpuNo][ind+1].setText(String.valueOf(address));
				cacheValid(cpuNo, ind, State.Modified);
				blockInvalid(cpuNo, address);
				return;
			}
			else {
				write_msg("cache " + ind + "缓存替换");
				cacheInvalid(cpuNo, ind);
			}
		}
		write_msg("不命中");
		String message = "向总线发送消息: ";
		if(op == 0) {
			message += "读不命中";
			write_msg(message);
			readBlock(cpuNo, address);
		}
		else {
			message += "写不命中";
			write_msg(message);
			cache[cpuNo][ind] = address;
			showCacheLabel[cpuNo][ind+1].setText(String.valueOf(address));
			blockInvalid(cpuNo, address);
			cacheValid(cpuNo, ind, State.Modified);
		}
	}
	public void blockInvalid(int cpuNo, int address) {
		int ind = address % cachePerCPU;
		for(int i=0; i < CPUnum; i++) {
			if(cache_state[i][ind] != State.Invalid && cache[i][ind] == address && i != cpuNo) {
				write_msg("CPU " + i + "监听到块" + address + "被修改," + "缓存" + ind + "作废");
				if(cache_state[i][ind] == State.Modified) {
					writeBack(address);
				}
				cacheInvalid(i, ind);
			}
		}
	}
	public void readBlock(int cpuNo, int address) {
		cache[cpuNo][address%cachePerCPU] = address;
		writeBack(address);
		showCacheLabel[cpuNo][address%cachePerCPU+1].setText(String.valueOf(address));
		cacheValid(cpuNo, address%cachePerCPU, State.Shared);
		flash(1, address/blockPerGroup, address%blockPerGroup);
		write_msg("发送 数据块"+address+" 到 cpu"+cpuNo);
	}
	public void writeBack(int address) {
		int ind = address % cachePerCPU;
		for(int i = 0; i < CPUnum; i++) {
			if(cache_state[i][ind] == State.Modified && cache[i][ind] == address) {
				cacheValid(i, ind, State.Shared);
				write_msg("cpu" + i + "写回块" + address);
				break;
			}
		}
		flash(1, address/blockPerGroup, address%blockPerGroup);
	}
	//cache 命中，使有效，使失效
	public void cacheHit(int cpuNo, int offset) {
		flash(0, cpuNo, offset+1);
	}
	public void cacheValid(int cpuNo, int offset, State newState) {
		cacheStateChange(cpuNo, offset, newState);
	}
	public void cacheInvalid(int cpuNo, int offset) {
		if(cache_state[cpuNo][offset] == State.Modified) {
			writeBack(cache[cpuNo][offset]);
		}
		cacheStateChange(cpuNo, offset, State.Invalid);
	}

	// 块状态转换 及 动画,消息输出
	public void cacheStateChange(int cpuNo, int ind, State newState) {
		cache_state[cpuNo][ind] = newState;
		switch(newState) {
			case Invalid:
				flash(0, cpuNo, ind + 1, Color.gray);
				showCacheLabel[cpuNo][ind+1].setText("");
				break; 
			case Shared:
				flash(0, cpuNo, ind + 1, Color.cyan);
				break;
			case Modified:
				flash(0, cpuNo, ind + 1, Color.red);
				break;
 		}
	}

		
	//消息操作
	public void write_msg(String message) {
		msg[msg_idx++].setText(message);
	}
	public void msg_clear() {
		System.out.println();
		for(int i = 0; i < msg_idx;i++) {
			System.out.println(msg[i].getText());
		}
		for(; msg_idx>0; msg_idx--) {
			msg[msg_idx-1].setText("");
		}
	}
	
	//动画显示
	public void flash(int kind, int group, int ind) { //不改变颜色
		// 0 cache, 1 memory
		Color curColor;
		if (kind == 0)
			curColor = showCacheLabel[group][ind].getBackground();
		else
			curColor = showBlockLabel[group][ind].getBackground();

		if (kind == 0) {
			new Shower(showCacheLabel[group][ind], curColor).start();
		}
		else {
			new Shower(showBlockLabel[group][ind], Color.red, curColor).start();
		}
	}
	public void flash(int kind, int group, int ind, Color changeColor) { // 改变颜色
		// 0 cache, 1 memory
		Color curColor;
		if (kind == 0)
			curColor = showCacheLabel[group][ind].getBackground();
		else
			curColor = showBlockLabel[group][ind].getBackground();
			
		if(kind == 0) {
			new Shower(showCacheLabel[group][ind], curColor, changeColor).start();
		}
		else {
			new Shower(showBlockLabel[group][ind], curColor, changeColor).start();
		}
	}
	
	public static void main (String[] args) {
		Snoop DBEntry = new Snoop();
	}
}
