package cache;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class CacheSim extends JFrame implements ActionListener{

	private JPanel panelTop, panelLeft, panelRight, panelBottom;
	private JButton execResetBtn,execResetAllBtn,execStepBtn, execAllBtn, fileBotton;
	private JComboBox csBox, data_csBox, in_csBox, bsBox, wayBox, replaceBox, prefetchBox, writeBox, allocBox;
	private JFileChooser fileChoose=new JFileChooser("./");
	
	private JLabel labelTop,labelLeft,rightLabel,bottomLabel,fileLabel,fileAddrBtn,
		    csLabel, data_csLabel,in_csLabel,bsLabel, wayLabel, replaceLabel, prefetchLabel, writeLabel, allocLabel;
	private JLabel results[];
	private ButtonGroup cacheBox;
	private JCheckBox cacheBox1,cacheBox2;

    //参数定义
	private String cachesize[] = { "2KB", "8KB", "32KB", "128KB", "512KB", "2MB" };
	private String blocksize[] = { "16B", "32B", "64B", "128B", "256B" };
	private String way[] = { "直接映象", "2路", "4路", "8路", "16路", "32路" };
	private String replace[] = { "LRU", "FIFO", "RAND" };
	private String pref[] = { "不预取", "不命中预取" };
	private String write[] = { "写回法", "写直达法" };
	private String alloc[] = { "按写分配", "不按写分配" };
	private String typename[] = { "读数据", "写数据", "读指令" };
	private String hitname[] = {"不命中", "命中" };
	
	//右侧结果显示
	private String rightLable[]={"访问总次数：", "读指令次数：", "读数据次数：", "写数据次数："};
	
	//打开文件
	private File file;
	
	//分别表示左侧几个下拉框所选择的第几项，索引从 0 开始
	private int csIndex, data_csIndex,in_csIndex, bsIndex, wayIndex, replaceIndex, prefetchIndex, writeIndex, allocIndex;
	
	//其它变量定义
	Scanner sca;
	
	private int cacheType = 0;
	private int[] Cache;
	private int[] dataCache, inCache;
	private int[][] cacheVisit, dataCacheVisit, inCacheVisit;
	private int[][] cacheDirty;
	private int cacheNum, dataCacheNum, inCacheNum;
	private int cacheIndexNum, dataCacheIndexNum, inCacheIndexNum;
	private int csize, data_csize, in_csize, bsize;
	private int wn;
	private int[] visit = new int[4];
	private int[] miss = new int[4];
	private int visitLabel, visitAddress, bAddress, blockOffset, cacheIndex;
	private boolean hitTag;
	private String[] visitType={"读数据", "写数据", "读指令"};

	/*
	 * 构造函数，绘制模拟器面板
	 */
	public CacheSim()
	{
		super("Cache Simulator");
		fileChoose.setFileFilter(new FileNameExtensionFilter("DIN文件(*.din)","din"));
		draw();
	}
	
	// 响应事件，共有五种事件：
	// 1.全部复位事件
	// 2.部分复位事件
	// 3.执行到底事件
	// 4.单步执行事件
	// 5.文件选择事件
	public void actionPerformed(ActionEvent e){
				
		if (e.getSource() == execResetAllBtn) {		// 全部复位
			if(JOptionPane.showConfirmDialog(null,"全部复位将重置文件及所有运行参数，是否需要继续？","提示",JOptionPane.YES_NO_OPTION)==0)
			{
				file=null;
				fileAddrBtn.setText("");
				reset_record();
				initCache();
			}
		}
		if (e.getSource() == execResetBtn) {		// 复位
			if(JOptionPane.showConfirmDialog(null,"复位操作将重置所有运行参数，是否需要继续？","提示",JOptionPane.YES_NO_OPTION)==0)
			{
				reset_record();
				if(file!=null) readFile();
				initCache();
			}
		}
		if (e.getSource() == execAllBtn) {			// 执行到底
			if(file==null){
				JOptionPane.showConfirmDialog(null,"未指定文件","提示",JOptionPane.DEFAULT_OPTION);
				return;
			}
			else if(!sca.hasNext()){
				JOptionPane.showConfirmDialog(null,"文件已执行完毕","提示",JOptionPane.DEFAULT_OPTION);
				return;
			}
			simExecAll();
		}
		if (e.getSource() == execStepBtn)			// 单步
		{
			if (file == null)
				JOptionPane.showConfirmDialog(null,"未指定文件","提示",JOptionPane.DEFAULT_OPTION);
			else
				if(simExecStep());
			else
				JOptionPane.showConfirmDialog(null,"文件已执行完毕","提示",JOptionPane.DEFAULT_OPTION);
		}
		if (e.getSource() == fileBotton){			// 选择地址流文件
			if(file!=null && sca.hasNext() && JOptionPane.showConfirmDialog(null,"文件尚未执行完毕，是否重新选择？","提示",JOptionPane.YES_NO_OPTION)==1)
				return;
			int fileOver = fileChoose.showOpenDialog(null);
			if (fileOver == 0) {
				   String path = fileChoose.getSelectedFile().getAbsolutePath();
				   fileAddrBtn.setText(path);
				   file = new File(path);
				   reset_record();
				   readFile();
				   initCache();
			}
		}
	}
	
	/*
	 * 初始化 Cache 模拟器
	 */
	public void initCache() {
		int KB=1024;
		int num;
		switch(bsIndex)			// 块大小
		{
			case 0:
				bsize=16;
				break;
			case 1:
				bsize=32;
				break;
			case 2:
				bsize=64;
				break;
			case 3:
				bsize=128;
				break;
			case 4:
				bsize=256;
				break;
			case 5:
				bsize=256;
				break;
		}	
		switch(wayIndex){		// 组相联的路数
			case 0:
				wn=1;
				break;
			case 1:
				wn=2;
				break;
			case 2:
				wn=4;
				break;
			case 3:
				wn=8;
				break;
			case 4:
				wn=16;
				break;
			case 5:
				wn=32;
				break;
		}
		if(cacheType==0){		// 选择统一的cache
			switch(csIndex){	// 统一cache的大小
				case 0:
					csize=2*KB;
					break;
				case 1:
					csize=8*KB;
					break;
				case 2:
					csize=32*KB;
					break;
				case 3:
					csize=128*KB;
					break;
				case 4:
					csize=512*KB;
					break;
				case 5:
					csize=2048*KB;
					break;
			}
			cacheNum = csize / bsize;		// cache的块数
			cacheIndexNum = cacheNum / wn;	// cache的组数
			cacheIndexNum = cacheIndexNum > 0? cacheIndexNum: 1;
			Cache = new int[cacheNum];
			for (int i = 0; i < cacheNum; i++) 		// 用-1初始化cache的tag表示该块未使用
				Cache[i] = -1;
			cacheVisit = new int[cacheIndexNum][wn];		// 用于维护替换顺序的多功能数组
			for(int i = 0;i < cacheIndexNum; i++)
				for(int j = 0; j < wn; j++) 
					cacheVisit[i][j] = j;
			cacheDirty = new int[cacheIndexNum][wn];
		}
		else{					// 选择独立的cache
			switch(data_csIndex){
				case 0:
					data_csize=2*KB;
					break;
				case 1:
					data_csize=8*KB;
					break;
				case 2:
					data_csize=32*KB;
					break;
				case 3:
					data_csize=128*KB;
					break;
				case 4:
					data_csize=512*KB;
					break;
				case 5:
					data_csize=2048*KB;
					break;
			}
			dataCacheNum = data_csize / bsize;
			dataCacheIndexNum = dataCacheNum / wn;
			dataCache = new int[dataCacheNum];
			for (int i = 0; i < dataCacheNum; i++)
				dataCache[i] = -1;
			dataCacheVisit = new int[dataCacheIndexNum][wn];
			for(int i = 0; i < dataCacheIndexNum; i++)
				for(int j = 0; j < wn; j++)
					dataCacheVisit[i][j] = j;
			cacheDirty = new int[dataCacheNum / wn][wn];
			switch(in_csIndex){
				case 0:
					in_csize=2*KB;
					break;
				case 1:
					in_csize=8*KB;
					break;
				case 2:
					in_csize=32*KB;
					break;
				case 3:
					in_csize=128*KB;
					break;
				case 4:
					in_csize=512*KB;
					break;
				case 5:
					in_csize=2048*KB;
					break;
			}
			inCacheNum = in_csize / bsize;
			inCacheIndexNum = inCacheNum / wn;
			inCache = new int[inCacheNum];
			for (int i = 0; i < inCacheNum; i++)
				inCache[i] = -1;
			inCacheVisit = new int[inCacheIndexNum][wn];
			for(int i = 0; i < inCacheIndexNum; i++)
				for(int j=0; j < wn; j++)
					inCacheVisit[i][j] = j;
		}
		print_record();		// 打印面板右侧信息
	}
	
	/*
	 * 将指令和数据流从文件中读入
	 */
	public void readFile() {
		try
		{
			sca = new Scanner(file);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	

	public boolean simExecStep()		// 单步执行
	{
		boolean tag = simExecStepBody();
		refresh_record();
		return tag;
	}
	
	private boolean simExecStepBody()		// 单步执行主体
	{
		boolean tag = false;
		if(sca.hasNext())
		{
			visitLabel = sca.nextInt();
			visitAddress = sca.nextInt(16);
			hitTag = cacheCheck(visitLabel, visitAddress);
			if(!hitTag) cachePrefetch(visitLabel, visitAddress);
			modify_record(visitLabel, hitTag);
			tag = true;
		}
		return tag;
	}
	
	public void simExecAll()		// 执行到底
	{
		while(simExecStepBody());
		print_record();
		for(int i = 20; i < 32; i++)
			results[i].setVisible(false);
	}
	
	private boolean cacheCheck(int label, int address)		// cache查找与替换
	{
		bAddress = address / bsize;		// 字节地址转为memory中的块地址和块偏移
		blockOffset = address % bsize;
		if(cacheType == 0)		// 当使用统一cache时
		{
			int offset = wn > cacheNum ? cacheNum : wn;		// 一组中所拥有的块数，可能小于路数！！！
			cacheIndex = bAddress % cacheIndexNum;		// memory块地址在cache中的组索引
			for(int i = wn * cacheIndex; i < wn * cacheIndex + offset; i++)	// 在该组中遍历查找
				if(bAddress == Cache[i])
				{
					modify_cacheVisit(cacheIndex, i - cacheIndex * wn, cacheVisit); // 可能需要更新访问时间
					if(label == 1)
						modify_cacheDirty(cacheIndex, i - cacheIndex * wn);
					return true;
				}
			if(label != 1 || allocIndex != 1)
				cacheReplace(label, bAddress, cacheIndex, Cache, cacheVisit);
			return false;
		}
		else
		{
			if(label == 2)
			{
				if(inCacheIndexNum == 0)
					cacheIndex = 0;
				else
					cacheIndex = bAddress % inCacheIndexNum;
				for(int i = wn * cacheIndex; i < wn * cacheIndex + wn; i++)
					if(bAddress == inCache[i])
					{
						modify_cacheVisit(cacheIndex,i - wn * cacheIndex, inCacheVisit);
						return true;
					}
				cacheReplace(label, bAddress, cacheIndex, inCache, inCacheVisit);
				return false;
			}
			else
			{
				if(dataCacheIndexNum == 0)
					cacheIndex = 0;
				else 
					cacheIndex = bAddress % dataCacheIndexNum;
				for(int i = wn * cacheIndex; i < wn * cacheIndex + wn; i++)
					if(bAddress==dataCache[i])
					{
						modify_cacheVisit(cacheIndex, i - wn * cacheIndex,dataCacheVisit);
						if(label == 1)
							modify_cacheDirty(cacheIndex, i - wn * cacheIndex);
						return true;
					}
				if(label != 1 || allocIndex != 1)
					cacheReplace(label,bAddress,cacheIndex,dataCache,dataCacheVisit);
				return false;
			}
		}
	}
	
	private void cacheReplace(int label, int bAddress, int group, int []cache, int [][]cache_visit)
	{
		int idx = 0;
		switch(replaceIndex)
		{
			case 0:		// LRU
				int offset = wn > cacheNum ? cacheNum : wn;
				for(int i = 0; i < offset; i++)
					if(cache_visit[group][i] == 0)
					{
						idx = i;
						break;
					}
				cache_visit[group][idx] = wn;
				for(int i = 0; i < wn; i++)
					cache_visit[group][i]--;
				cache[group * wn + idx]=bAddress;
				break;
			case 1:		// FIFO
				idx = cache_visit[group][0];
				cache_visit[group][0]++;
				cache_visit[group][0] %= wn;
				cache[group * wn + idx] = bAddress;
				break;
			case 2:		// RAND
				Random rand = new Random();
				idx = rand.nextInt(wn);
				cache[group * wn + idx] = bAddress;
				break;
		}
		if(cache != inCache) cacheDirty[group][idx] = 0;
	}
	
	private void modify_cacheVisit(int group, int bn, int [][]cache_visit)  // 更新访问时间
	{
		switch(replaceIndex)
		{
			case 0:		// LRU
				int t = cache_visit[group][bn];
				for(int i = 0; i < wn; i++)
					if(cache_visit[group][i] > t)
						cache_visit[group][i]--;
				cache_visit[group][bn] = wn - 1;
				break;
			case 1:		// FIFO
				break;
			case 2:		// RAND
				break;
		}
	}
	
	private void modify_cacheDirty(int group, int bn)		// 写回法时，标记该块未写入memory
	{
		if(writeIndex == 0) cacheDirty[group][bn] = 1;
	}
	
	private void cachePrefetch(int label, int address)		// 不命中预取
	{
		if(prefetchIndex == 1)
			cacheCheck(label, address + bsize);
	}
	
	private void modify_record(int label, boolean tag)		// 修改统计信息
	{
		visit[0]++;
		visit[label+1]++;
		if(!tag)
		{
			miss[0]++;
			miss[label+1]++;
		}
	}
	
	private void refresh_record()		// 打印所有模拟结果（统计信息 + 当前信息）
	{
		double missRate = 0.00;
		results[5].setText(visit[0] + "");
		results[6].setText(miss[0] + "");
		if(visit[0] != 0)
			missRate=((double)miss[0]) / visit[0];
		results[7].setText(missRate * 100 + "");
		results[4 * (visitLabel + 2) + 1].setText(visit[visitLabel + 1] + "");
		results[4 * (visitLabel + 2) + 2].setText(miss[visitLabel + 1] + "");
		missRate = 0.00;
		if(visit[visitLabel + 1] != 0)
			missRate = ((double)miss[visitLabel + 1]) / visit[visitLabel + 1];
		results[4 * (visitLabel + 2) + 3].setText(Math.round(missRate * 10000)/100.0 + "");
		results[21].setText(visitType[visitLabel]);
		results[23].setText(visitAddress + "");
		results[25].setText(bAddress + "");
		results[27].setText(blockOffset + "");
		results[29].setText(cacheIndex + "");
		if(hitTag)
			results[31].setText(hitname[1]);
		else	
			results[31].setText(hitname[0]);
	}
	
	private void print_record()		// 打印到目前为止的统计信息（右侧前四行）
	{
		for(int i = 1; i < 5; i++)
		{
			results[i * 4 + 1].setText(visit[i - 1] + "");
			results[i * 4 + 2].setText(miss[i - 1] + "");
			double missRate = 0.00;
			if(visit[i - 1] != 0)
				missRate = ((double)miss[i - 1]) / visit[i - 1];
			results[i * 4 + 3].setText(Math.round(missRate * 10000)/100.0 + "");
		}
	}
	
	private void reset_record()		// 重置统计信息与当前信息
	{
		for(int i = 1; i < 5; i++)
		{
			visit[i - 1] = 0;
			miss[i - 1] = 0;
		}
		for(int i = 20; i < 32; i++) {
			if(i % 2 == 1)
				results[i].setText("");
			results[i].setVisible(true);
		}
	}
	/*
	 * 主函数
	 */
	public static void main(String[] args)
	{
		new CacheSim();
	}
	
	/**
	 * 绘制 Cache 模拟器图形化界面
	 * 无需做修改
	 */
	public void draw()
	{
		//模拟器绘制面板
		setLayout(new BorderLayout(5,5));
		panelTop = new JPanel();
		panelLeft = new JPanel();
		panelRight = new JPanel();
		panelBottom = new JPanel();
		panelTop.setPreferredSize(new Dimension(800, 30));
		panelLeft.setPreferredSize(new Dimension(300, 500));
		panelRight.setPreferredSize(new Dimension(500, 500));
		panelBottom.setPreferredSize(new Dimension(800, 80));
		panelTop.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		panelLeft.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		panelRight.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		panelBottom.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		//*****************************顶部面板绘制*****************************************//
		labelTop = new JLabel("Cache Simulator");
		labelTop.setAlignmentX(CENTER_ALIGNMENT);
		panelTop.add(labelTop);

		//*****************************左侧面板绘制*****************************************//
		labelLeft = new JLabel("Cache 参数设置");
		labelLeft.setPreferredSize(new Dimension(300, 20));
		//cache类型选择
		cacheBox1 = new JCheckBox("统一的cache大小",true);
		cacheBox1.setPreferredSize(new Dimension(120,30));
		cacheBox2 = new JCheckBox("独立cache",false);
		cacheBox2.setPreferredSize(new Dimension(285,30));

		
		//cache 大小设置
		csLabel = new JLabel("总大小");
		csLabel.setPreferredSize(new Dimension(120, 30));
		csBox = new JComboBox(cachesize);
		csBox.setPreferredSize(new Dimension(160, 30));
		csBox.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				csIndex = csBox.getSelectedIndex();
			}
		});
		//data_cache,in_cache设置
		data_csLabel = new JLabel("数据cache大小");
		data_csLabel.setPreferredSize(new Dimension(120, 30));
		data_csBox = new JComboBox(cachesize);
		data_csBox.setPreferredSize(new Dimension(160, 30));
		data_csBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				data_csIndex = data_csBox.getSelectedIndex();
			}
		});
		in_csLabel = new JLabel("指令cache大小");
		in_csLabel.setPreferredSize(new Dimension(120, 30));
		in_csBox = new JComboBox(cachesize);
		in_csBox.setPreferredSize(new Dimension(160, 30));
		in_csBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				in_csIndex = in_csBox.getSelectedIndex();
			}
		});
		//cacheType 选择
		cacheBox1.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getItemSelectable()==cacheBox1){
					cacheType=0;
					csBox.enable();
					data_csBox.disable();
					in_csBox.disable();
					csBox.repaint();
					data_csBox.repaint();
					in_csBox.repaint();
				}
			}
		});
		cacheBox2.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getItemSelectable()==cacheBox2){
					cacheType=1;
					csBox.disable();
					data_csBox.enable();
					in_csBox.enable();
					csBox.repaint();
					data_csBox.repaint();
					in_csBox.repaint();
				}
			}
		});
		cacheBox = new ButtonGroup();
		cacheBox.add(cacheBox1);
		cacheBox.add(cacheBox2);
		csBox.enable();
		data_csBox.disable();
		in_csBox.disable();
		
		//cache 块大小设置
		bsLabel = new JLabel("块大小");
		bsLabel.setPreferredSize(new Dimension(120, 30));
		bsBox = new JComboBox(blocksize);
		bsBox.setPreferredSize(new Dimension(160, 30));
		bsBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				bsIndex = bsBox.getSelectedIndex();
			}
		});
		
		//相连度设置
		wayLabel = new JLabel("相联度");
		wayLabel.setPreferredSize(new Dimension(120, 30));
		wayBox = new JComboBox(way);
		wayBox.setPreferredSize(new Dimension(160, 30));
		wayBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				wayIndex = wayBox.getSelectedIndex();
			}
		});
		
		//替换策略设置
		replaceLabel = new JLabel("替换策略");
		replaceLabel.setPreferredSize(new Dimension(120, 30));
		replaceBox = new JComboBox(replace);
		replaceBox.setPreferredSize(new Dimension(160, 30));
		replaceBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				replaceIndex = replaceBox.getSelectedIndex();
			}
		});
		
		//预取策略设置
		prefetchLabel = new JLabel("预取策略");
		prefetchLabel.setPreferredSize(new Dimension(120, 30));
		prefetchBox = new JComboBox(pref);
		prefetchBox.setPreferredSize(new Dimension(160, 30));
		prefetchBox.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				prefetchIndex = prefetchBox.getSelectedIndex();
			}
		});
		
		//写策略设置
		writeLabel = new JLabel("写策略");
		writeLabel.setPreferredSize(new Dimension(120, 30));
		writeBox = new JComboBox(write);
		writeBox.setPreferredSize(new Dimension(160, 30));
		writeBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				writeIndex = writeBox.getSelectedIndex();
			}
		});
		
		//调块策略
		allocLabel = new JLabel("写不命中调块策略");
		allocLabel.setPreferredSize(new Dimension(120, 30));
		allocBox = new JComboBox(alloc);
		allocBox.setPreferredSize(new Dimension(160, 30));
		allocBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				allocIndex = allocBox.getSelectedIndex();
			}
		});
		
		//选择指令流文件
		fileLabel = new JLabel("选择指令流文件");
		fileLabel.setPreferredSize(new Dimension(120, 30));
		fileAddrBtn = new JLabel();
		fileAddrBtn.setPreferredSize(new Dimension(210,30));
		fileAddrBtn.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		fileBotton = new JButton("浏览");
		fileBotton.setPreferredSize(new Dimension(70,30));
		fileBotton.addActionListener(this);
		
		panelLeft.add(labelLeft);
		//panelLeft.add(csLabel);
		panelLeft.add(cacheBox1);
		panelLeft.add(csBox);
		panelLeft.add(cacheBox2);
		panelLeft.add(data_csLabel);
		panelLeft.add(data_csBox);
		panelLeft.add(in_csLabel);
		panelLeft.add(in_csBox);
		panelLeft.add(bsLabel);
		panelLeft.add(bsBox);
		panelLeft.add(wayLabel);
		panelLeft.add(wayBox);
		panelLeft.add(replaceLabel);
		panelLeft.add(replaceBox);
		panelLeft.add(prefetchLabel);
		panelLeft.add(prefetchBox);
		panelLeft.add(writeLabel);
		panelLeft.add(writeBox);
		panelLeft.add(allocLabel);
		panelLeft.add(allocBox);
		panelLeft.add(fileLabel);
		panelLeft.add(fileAddrBtn);
		panelLeft.add(fileBotton);
		
		//*****************************右侧面板绘制*****************************************//
		//模拟结果展示区域
		rightLabel = new JLabel("模拟结果");
		rightLabel.setPreferredSize(new Dimension(500, 20));
		results = new JLabel[32];
		String toplabel[]={"总次数","缺失次数","缺失率（%）"};
		
		results[0]=new JLabel();
		results[0].setPreferredSize(new Dimension(100, 30));
		for(int i=1;i<4;i++)
		{
			results[i]=new JLabel(toplabel[i-1]);
			results[i].setPreferredSize(new Dimension(100, 30));
		}
		String label[]={"访问","读数据","写数据","读指令"};
		for (int i = 1; i < 5; i++)
			for(int j = 0;j < 4; j++)
			{
				if(j == 0)
					results[i*4+j] = new JLabel(label[i-1]);
				else
				{
					results[i*4+j] = new JLabel();
					results[i*4+j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
				}
				results[i*4+j].setPreferredSize(new Dimension(100, 30));
			}
		String inf[]={"访问类型","地址","块号","块内地址","索引","命中情况"};
		for(int i = 20; i < 32; i++ )
		{
			if(i % 2 == 0)
				results[i] = new JLabel(inf[(i-20)/2]);
			else
			{
				results[i] = new JLabel();
				results[i].setBorder(new EtchedBorder(EtchedBorder.RAISED));
			}
			results[i].setPreferredSize(new Dimension(100, 30));
		}
		
		JLabel[] stepLabel = new JLabel[9];
		panelRight.add(rightLabel);
		
		stepLabel[0] = new JLabel();
		stepLabel[0].setPreferredSize(new Dimension(500, 1));
		panelRight.add(stepLabel[0]);
		
		for (int i = 0; i < 32; i++)
		{
			panelRight.add(results[i]);
			if((i + 1) % 4 == 0)
			{
				stepLabel[(i + 1) / 4] = new JLabel();
				stepLabel[(i + 1) / 4].setPreferredSize(new Dimension(1000, 1));
				panelRight.add(stepLabel[(i + 1) / 4]);
			}
		}
		
		//*****************************底部面板绘制*****************************************//
		
		bottomLabel = new JLabel("执行控制");
		bottomLabel.setPreferredSize(new Dimension(800, 30));
		execResetAllBtn = new JButton("全部复位");
		execResetAllBtn.setLocation(100, 30);
		execResetAllBtn.addActionListener(this);
		execResetBtn = new JButton("复位");
		execResetBtn.setLocation(100, 30);
		execResetBtn.addActionListener(this);
		execStepBtn = new JButton("步进");
		execStepBtn.setLocation(100, 30);
		execStepBtn.addActionListener(this);
		execAllBtn = new JButton("执行到底");
		execAllBtn.setLocation(300, 30);
		execAllBtn.addActionListener(this);
		
		panelBottom.add(bottomLabel);
		panelBottom.add(execResetAllBtn);
		panelBottom.add(execResetBtn);
		panelBottom.add(execStepBtn);
		panelBottom.add(execAllBtn);

		add("North", panelTop);
		add("West", panelLeft);
		add("Center", panelRight);
		add("South", panelBottom);
		setSize(820, 620);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}