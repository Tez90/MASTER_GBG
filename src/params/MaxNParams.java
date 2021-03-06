package params;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Choice;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controllers.ExpectimaxNAgent;
import controllers.MaxNAgent;

/**
 * This class realizes the parameter settings (GUI tab) for  
 * {@link MaxNAgent}, {@link ExpectimaxNAgent}.
 * These parameters and their [defaults] are: <ul>
 * <li> <b>tree depth</b>: 	[10] initial strength of learning parameter 
 * <li> <b>useHashMap</b>: 	[true] (only MaxN) whether to store calculated values in a hash map or not 
 * </ul> 
 * The defaults are defined in {@link ParMaxN}. 
 * 
 * @see ParMaxN
 * @see MaxNAgent
 */
public class MaxNParams extends Frame 
{
	private static final long serialVersionUID = 1L;

	JLabel maxnDepth_L;
	JLabel maxnUseHm_L;
	public JTextField maxnDepth_T;
	public JCheckBox maxnUseHmTrue;

	JPanel mPanel;
	
	public MaxNParams() {
		super("MaxN Parameter");
		
		maxnDepth_L = new JLabel("Tree Depth");
		maxnDepth_T = new JTextField(ParMaxN.DEFAULT_MAXN_TREE_DEPTH+"");				// 
		maxnUseHm_L = new JLabel("MaxN Hashmap ");
		maxnUseHmTrue = new JCheckBox("use hashmap",ParMaxN.DEFAULT_MAXN_USE_HASHMAP);
		mPanel = new JPanel();		// put the inner buttons into panel mPanel. This panel
									// can be handed over to a tab of a JTabbedPane object
									// (see class XArenaTabs)
		
		maxnDepth_L.setToolTipText("Tree depth (for MaxN or ExpectimaxN)");
		maxnUseHm_L.setToolTipText("MaxN: use hashmap to save values of visited states");
		
		setLayout(new BorderLayout(10,0));				// rows,columns,hgap,vgap
		mPanel.setLayout(new GridLayout(0,4,10,10));		
		
		mPanel.add(maxnDepth_L);
		mPanel.add(maxnDepth_T);
		mPanel.add(new Canvas());
		mPanel.add(new Canvas());

		mPanel.add(maxnUseHm_L);
		mPanel.add(maxnUseHmTrue);
		mPanel.add(new Canvas());
		mPanel.add(new Canvas());

		mPanel.add(new Canvas());
		mPanel.add(new Canvas());
		mPanel.add(new Canvas());
		mPanel.add(new Canvas());

		mPanel.add(new Canvas());
		mPanel.add(new Canvas());
		mPanel.add(new Canvas());
		mPanel.add(new Canvas());

		add(mPanel,BorderLayout.CENTER);
				
		pack();
		setVisible(false);
		
	} // constructor MaxNParams()	
	
	public JPanel getPanel() {
		return mPanel;
	}

	public void enableHashmapPart(boolean enable) {
		maxnUseHm_L.setEnabled(enable);
		maxnUseHmTrue.setEnabled(enable);
	}
	
	public int getMaxNDepth() {
		return Integer.valueOf(maxnDepth_T.getText()).intValue();
	}

	public boolean getMaxNUseHashmap() {
		return maxnUseHmTrue.isSelected();
	}
	
	public void setMaxNDepth(int value) {
		maxnDepth_T.setText(value+"");
	}
	
	public void setMaxNUseHashmap(boolean bval) {
		maxnUseHmTrue.setSelected(bval);
	}
	
	/**
	 * Needed to restore the param tab with the parameters from a re-loaded agent
	 * @param mp  ParMaxN of the re-loaded agent
	 */
	public void setFrom(ParMaxN mp) {
		this.setMaxNDepth(mp.getMaxNDepth());
		this.setMaxNUseHashmap(mp.getMaxNUseHashmap());
	}
	
	/**
	 * Set sensible parameters for a specific agent and specific game. By "sensible
	 * parameters" we mean parameter producing good results. Likewise, some parameter
	 * choices may be enabled or disabled.
	 * 
	 * @param agentName currently only "MaxN" 
	 * @param gameName the string from {@link games.StateObservation#getName()}
	 * @param numPlayers
	 */
	@Deprecated
	public void setParamDefaults(String agentName, String gameName, int numPlayers) {
		switch (agentName) {
		case "MaxN": 
		case "Max-N": 
			switch (gameName) {
			case "Sim": 
				maxnDepth_T.setText("15");		
				maxnUseHmTrue.setSelected(true);
				break;
			}
			break;
		}
	}	

	/**
	 * @return	the {@link ParMaxN} representation of {@code this}
	 */
	public ParMaxN getParMaxN() {
		return new ParMaxN(this);
	}

} // class MaxNParams
