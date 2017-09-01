package params;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Choice;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controllers.TD.TDAgent;
import controllers.TD.ntuple.TDNTupleAgt;

/**
 * This class realizes the parameter settings (GUI tab) for TD players.
 * These parameters and their [defaults] are: <ul>
 * <li> <b>alpha</b>: 		[0.001] initial strength of learning parameter 
 * <li> <b>alphaFinal</b>: 	[0.001] final strength of learning parameter  
 * <li> <b>epsilon</b>: 	[0.1] initial probability of random move 
 * <li> <b>epsilonFinal</b>:[0.0] final probability of random move  
 * <li> <b>lambda</b>: 		[0.0] eligibility trace parameter 
 * 	(only relevant for {@link controllers.TD.TDAgent})
 * <li> <b>gamma</b>: 		[1.0] discount parameter 
 * </ul> 
 * 
 * @see ParTD
 * @see TDAgent
 */
public class TDParams extends Frame implements Serializable
{
	private static final String TIPGAMMAL = "Discount factor in range [0,1] ";
	private static final String TIPEPOCHL = "Accumulate gradient for Epochs iterations, then update weights";
	private static final String TIPNORMALIZEL = "Normalize StateObservation's game score to the range of the agent's sigmoid function";
	private static final String TIPALPHA1L = "Initial learn step size";
	private static final String TIPALPHA2L = "Final learn step size";
	private static final String TIPEPSIL1L = "Initial random move rate in [0,1]";
	private static final String TIPEPSIL2L = "Final random move rate in [0,1]";
	private static final String TIPLAMBDAL = "Eligibility trace parameter in [0,1]";
	
	private static String[] lrnTypeString = { "backprop","RPROP" };
	
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 1L;

	JLabel alphaL;
	JLabel alfinL;
	JLabel epsilL;
	JLabel epfinL;
	JLabel lambdaL;
	JLabel gammaL;
	JLabel epochL;
	JPanel tdPanel;
	public JTextField alphaT;
	public JTextField alfinT;
	public JTextField epsilT;
	public JTextField epfinT;
	public JTextField lambdaT;
	public JTextField gammaT;
	public JTextField epochT;
	
	JLabel SigTypeL;
	JLabel NormalizeL;
	JLabel NetTypeL;
	JLabel LrnTypeL;
	CheckboxGroup cbgNetType;
	Checkbox LinNetType;
	Checkbox BprNetType;
	public JCheckBox withSigType;
	public JCheckBox normalize;
//	CheckboxGroup cbgLrnType;
//	public Checkbox bpropType;
//	public Checkbox rpropType;
	public Choice choiceLrnType;
	JLabel FeatTDS_L;
	public Choice choiceFeatTDS;
		String FeatTDS;
	
// -- obsolete, they are now stored in AgentBase
//
//	// These two members are here only to save these settings (from other param tabs) 
//	// together with the agent, so that we can restore them later on load (at least 
//	// maxGameNum is relevant for training): 
//	private int maxGameNum;
//	private int numEval;
	
	public TDParams() {
		super("TD Parameter");
		
		// These are the initial defaults 
		// (Other game- and agent-specific defaults are in setParamDefaults, which is called
		// whenever one of the agent choice boxes changes to an agent requiring TDParams)
		//
		alphaT = new JTextField("0.001");  //("0.1");				// the defaults
		alfinT = new JTextField("0.001");			//
		epsilT = new JTextField("0.3");				// 
		epfinT = new JTextField("0.0");				//
		lambdaT = new JTextField("0.0"); //("0.9");			//
		gammaT = new JTextField("1.0");				//
		epochT = new JTextField("1");				//
		alphaL = new JLabel("Alpha init");
		alfinL = new JLabel("Alpha final");
		epsilL = new JLabel("Epsilon init");
		epfinL = new JLabel("Epsilon final");
		lambdaL = new JLabel("Lambda");
		gammaL = new JLabel("Gamma");
		epochL = new JLabel("Epochs");
		epochL.setToolTipText(TIPEPOCHL);
		alphaL.setToolTipText(TIPALPHA1L);
		alfinL.setToolTipText(TIPALPHA2L);
		lambdaL.setToolTipText(TIPLAMBDAL);
		epsilL.setToolTipText(TIPEPSIL1L);
		epfinL.setToolTipText(TIPEPSIL2L);
		gammaL.setToolTipText(TIPGAMMAL);
		
		withSigType = new JCheckBox();
		normalize = new JCheckBox();
//		cbgSigType = new CheckboxGroup();
//		wo_SigType = new Checkbox("without",cbgSigType,true);
//		withSigType = new Checkbox("with",cbgSigType,false);

		NetTypeL = new JLabel("Network Type: ");
		SigTypeL = new JLabel("Output Sigmoid: ");
		NormalizeL = new JLabel("Normalize: ");
		LrnTypeL = new JLabel("Learning rule: ");
		NormalizeL.setToolTipText(TIPNORMALIZEL);

		cbgNetType = new CheckboxGroup();
		LinNetType = new Checkbox("linear",cbgNetType,true);
		BprNetType = new Checkbox("neural net",cbgNetType,false);

//		cbgLrnType = new CheckboxGroup();
//		bpropType = new Checkbox("backprop",cbgLrnType,true);
//		rpropType = new Checkbox("RPROP",cbgLrnType,false);
		choiceLrnType = new Choice();
		for (String s : lrnTypeString) choiceLrnType.addItem(s);
		
		FeatTDS_L = new JLabel("Feature set");
		this.choiceFeatTDS = new Choice();

//		ok = new JButton("OK");
//		m_par = this;
		tdPanel = new JPanel();		// put the inner buttons into panel oPanel. This panel
									// can be handed over to a tab of a JTabbedPane 
									// (see class XArenaTabs)
		
//		ok.addActionListener(
//				new ActionListener()
//				{
//					public void actionPerformed(ActionEvent e)
//					{
//						m_par.setVisible(false);
//					}
//				}					
//		);

		setLayout(new BorderLayout(10,0));				// rows,columns,hgap,vgap
		tdPanel.setLayout(new GridLayout(0, 4, 10, 10)); // rows,columns,hgap,vgap
		
		tdPanel.add(alphaL);
		tdPanel.add(alphaT);
		tdPanel.add(epsilL);
		tdPanel.add(epsilT);
		
		tdPanel.add(alfinL);
		tdPanel.add(alfinT);
		tdPanel.add(epfinL);
		tdPanel.add(epfinT);
		
		tdPanel.add(lambdaL);
		tdPanel.add(lambdaT);
		tdPanel.add(gammaL);
		tdPanel.add(gammaT);
		
		tdPanel.add(SigTypeL);
		tdPanel.add(withSigType);
		tdPanel.add(NormalizeL);
		tdPanel.add(normalize);

		tdPanel.add(NetTypeL);
		tdPanel.add(LinNetType);
		tdPanel.add(BprNetType);
		tdPanel.add(new Canvas());					// fill one grid place with empty canvas
		
		tdPanel.add(LrnTypeL);
		tdPanel.add(choiceLrnType);
//		tdPanel.add(bpropType);
//		tdPanel.add(rpropType);
		tdPanel.add(new Canvas());				
		tdPanel.add(new Canvas());				

		tdPanel.add(FeatTDS_L);
		tdPanel.add(choiceFeatTDS);
		tdPanel.add(epochL);
		tdPanel.add(epochT);
		
		add(tdPanel,BorderLayout.CENTER);
		//add(ok,BorderLayout.SOUTH);
		
		pack();
		setVisible(false);
	} // constructor TDParams()	
	
	public TDParams(TDParams tdPar) {
		this();
		this.setFrom(tdPar);
	}
	
	public JPanel getPanel() {
		return tdPanel;
	}
	public double getAlpha() {
		return Double.valueOf(alphaT.getText()).doubleValue();
	}
	public double getAlphaFinal() {
		return Double.valueOf(alfinT.getText()).doubleValue();
	}
	public double getEpsilon() {
		return Double.valueOf(epsilT.getText()).doubleValue();
	}
	public double getEpsilonFinal() {
		return Double.valueOf(epfinT.getText()).doubleValue();
	}
	public double getLambda() {
		return Double.valueOf(lambdaT.getText()).doubleValue();
	}
	public double getGamma() {
		return Double.valueOf(gammaT.getText()).doubleValue();
	}
	public int getEpochs() {
		return Integer.valueOf(epochT.getText()).intValue();
	}
	public int getFeatmode() {
		String s = (String) choiceFeatTDS.getSelectedItem();
		//int i = Integer.valueOf(s).intValue();
		return Integer.valueOf(s).intValue();
	}
	public boolean hasSigmoid() {
		return withSigType.isSelected();
	}
	public boolean getNormalize() {
		return normalize.isSelected();
	}
	public boolean hasLinearNet() {
		return LinNetType.getState();
	}
	public boolean hasRpropLrn() {
		Object Type = choiceLrnType.getSelectedItem();
		if (Type == "RPROP")
			return true;
		return false;
	}
//	public int getMaxGameNum() {
//		return maxGameNum;
//	}
//	public int getNumEval() {
//		return numEval;
//	}
	
	public void setAlpha(double value) {
		alphaT.setText(value+"");
	}
	public void setAlphaFinal(double value) {
		alfinT.setText(value+"");
	}
	public void setEpsilon(double value) {
		epsilT.setText(value+"");
	}
	public void setEpsilonFinal(double value) {
		epfinT.setText(value+"");
	}
	public void setLambda(double value) {
		lambdaT.setText(value+"");
	}
	public void setGamma(double value) {
		gammaT.setText(value+"");
	}
	public void setEpochs(int value) {
		epochT.setText(value+"");
	}
	public void setFeatmode(int featmode) {
		//If the feature list has not been initialized, add the selected featmode to the list
		if (choiceFeatTDS.getItemCount() == 0){
			choiceFeatTDS.add(Integer.toString(featmode));
		}
		choiceFeatTDS.select(featmode+"");
	}
	public void setFeatList(int[] featList){
		for (int i : featList) choiceFeatTDS.addItem(Integer.toString(i));
	}
	public void setSigmoid(boolean state) {
		withSigType.setSelected(state);
	}
	public void setNormalize(boolean state) {
		normalize.setSelected(state);
	}
	public void setLinearNet(boolean state) {
		LinNetType.setState(state);
		BprNetType.setState(!state);
	}
	public void setRpropLrn(boolean state) {
		choiceLrnType.select(state ? 1 : 0);
//		rpropType.setState(state);
//		bpropType.setState(!state);
	}
	
//	public void setMaxGameNum(int maxGameNum) {
//		this.maxGameNum = maxGameNum;
//	}
//	public void setNumEval(int numEval) {
//		this.numEval = numEval;
//	}
	
	/**
	 * Needed to restore the param tab with the parameters from a re-loaded agent
	 * @param tp  TDParams of the re-loaded agent
	 */
	public void setFrom(TDParams tp) {
		setAlpha(tp.getAlpha());
		setAlphaFinal(tp.getAlphaFinal());
		setEpsilon(tp.getEpsilon());
		setEpsilonFinal(tp.getEpsilonFinal());
		setGamma(tp.getGamma());
		setLambda(tp.getLambda());
		setLinearNet(tp.hasLinearNet());
		setRpropLrn(tp.hasRpropLrn());
		setSigmoid(tp.hasSigmoid());
		setNormalize(tp.getNormalize());
		setEpochs(tp.getEpochs());
		setFeatmode(tp.getFeatmode());
//		setMaxGameNum(tp.getMaxGameNum());	// this is now in AgentBase
//		setNumEval(tp.getNumEval());		// this is obsolete now (we have ParOther)
	}
	
	/**
	 * Needed to restore the param tab with the parameters from a re-loaded agent
	 * @param tp  TDParams of the re-loaded agent
	 */
	public void setFrom(ParTD tp) {
		setAlpha(tp.getAlpha());
		setAlphaFinal(tp.getAlphaFinal());
		setEpsilon(tp.getEpsilon());
		setEpsilonFinal(tp.getEpsilonFinal());
		setGamma(tp.getGamma());
		setLambda(tp.getLambda());
		setLinearNet(tp.hasLinearNet());
		setRpropLrn(tp.hasRpropLrn());
		setSigmoid(tp.hasSigmoid());
		setNormalize(tp.getNormalize());
		setEpochs(tp.getEpochs());
		setFeatmode(tp.getFeatmode());
	}
	
	/**
	 * Set sensible parameters for a specific agent and specific game. By "sensible
	 * parameters" we mean parameter producing good results. Likewise, some parameter
	 * choices may be enabled or disabled.
	 * 
	 * @param agentName either "TD-Ntuple" (for {@link TDNTupleAgt}) or "TDS" (for {@link TDAgent})
	 * @param gameName the string from {@link games.StateObservation#getName()}
	 */
	public void setParamDefaults(String agentName, String gameName) {
		// Currently we have here only the sensible defaults for two games ("TicTacToe", "Hex")
		// and for three agents ("TD-Ntuple[-2]" = class TDNTuple[2]Agt and "TDS" = class TDAgent).
		//
		// If later good parameters for other games are found, they should be
		// added with suitable nested switch(gameName). 
		// Currently we have only one switch(gameName) on the initial featmode (=3 for 
		// TicTacToe, =2 for Hex, and =0 for all others)
		switch (agentName) {
		case "TD-Ntuple": 
		case "TD-Ntuple-2": 
			switch (agentName) {
			case "TD-Ntuple": 
				alphaT.setText("0.001");  		// the defaults
				alfinT.setText("0.001");		//
				break;
			case "TD-Ntuple-2": 
				alphaT.setText("0.2");  		// the defaults
				alfinT.setText("0.2");			//
			}
			epfinT.setText("0.0");				//
			lambdaT.setText("0.0"); 			//
			gammaT.setText("1.0");				//
			epochT.setText("1");				//
			withSigType.setSelected(true);		// tanh
			normalize.setSelected(false);		// 
//			withSigType.setEnabled(false);
//			SigTypeL.setEnabled(false);   
			withSigType.setEnabled(true); // NEW		
			SigTypeL.setEnabled(true);    // NEW
			NetTypeL.setEnabled(false);
			LinNetType.setEnabled(false);
			BprNetType.setEnabled(false);
			LrnTypeL.setEnabled(false);
			choiceLrnType.setEnabled(false);
//			bpropType.setEnabled(false);
//			rpropType.setEnabled(false);
			FeatTDS_L.setEnabled(false);
			choiceFeatTDS.setEnabled(false);
			epochL.setEnabled(false);
			epochT.setEnabled(false);
			switch (gameName) {
			case "2048": 
				epsilT.setText("0.0");				
				break;
			default:	//  all other
				epsilT.setText("0.3");				
				break;
			}
			break;
		case "TDS":
			alphaT.setText("0.1");				// the defaults
			alfinT.setText("0.001");			//
			epfinT.setText("0.0");				//
			lambdaT.setText("0.9");				//
			gammaT.setText("1.0");				//
			epochT.setText("1");				//
			withSigType.setSelected(false);		// Fermi fct
			normalize.setSelected(false);		// 
			withSigType.setEnabled(true);		
			SigTypeL.setEnabled(true);
			NetTypeL.setEnabled(true);
			LinNetType.setEnabled(true);
			BprNetType.setEnabled(true);
			LrnTypeL.setEnabled(true);
			choiceLrnType.setEnabled(true);
//			bpropType.setEnabled(true);
//			rpropType.setEnabled(true);
			FeatTDS_L.setEnabled(true);
			choiceFeatTDS.setEnabled(true);
			epochL.setEnabled(true);
			epochT.setEnabled(true);
			switch (gameName) {
			case "TicTacToe": 
				setFeatmode(3);
				break;
			case "Hex": 
				setFeatmode(2);
				lambdaT.setText("0.0");				
				break;
			default:	//  all other
				setFeatmode(0);
				lambdaT.setText("0.0");				
				break;
			}
			switch (gameName) {
			case "2048": 
				epsilT.setText("0.0");				
				break;
			default:	//  all other
				epsilT.setText("0.3");				
				break;
			}
			break;
		}
		
	}
	
} // class TDParams
