package src.games.Poker;

import game.rules.play.Play;
import games.Arena;
import tools.Types;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class pokerForm {
    public JPanel gameBoardPanel;
    private JButton checkButton;
    private JButton betButton;
    private JButton callButton;
    private JButton raiseButton;
    private JButton foldButton;
    private JPanel topInfo;
    private JLabel chipsLabel;
    private JLabel pot;
    private JPanel rightInfo;
    private JPanel actionPanel;
    private JPanel sharedPanel;
    private JLabel holeCard1;
    private JLabel holeCard2;
    private JPanel turnPanel;
    private JPanel riverPanel;
    private JPanel flopPanel;
    private JLabel flopCard1;
    private JLabel flopCard2;
    private JLabel flopCard3;
    private JLabel turnCard;
    private JLabel riverCard;
    private JPanel leftInfo;
    private JButton allInButton;
    private JTable playerTable;

    private GameBoardPoker m_gb;

    public pokerForm(GameBoardPoker gb, int player) {
        this.m_gb = gb;

        checkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Arena.Task aTaskState = m_gb.m_Arena.taskState;
                if (aTaskState == Arena.Task.PLAY)
                {
                    m_gb.HGameMove(1);
                }
                int dummy=1;
            }
        });
        betButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Arena.Task aTaskState = m_gb.m_Arena.taskState;
                if (aTaskState == Arena.Task.PLAY)
                {
                    m_gb.HGameMove(2);
                }
                int dummy=1;
            }
        });
        callButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Arena.Task aTaskState = m_gb.m_Arena.taskState;
                if (aTaskState == Arena.Task.PLAY)
                {
                    m_gb.HGameMove(3);
                }
                int dummy=1;
            }
        });
        raiseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Arena.Task aTaskState = m_gb.m_Arena.taskState;
                if (aTaskState == Arena.Task.PLAY)
                {
                    m_gb.HGameMove(4);
                }
                int dummy=1;
            }
        });
        foldButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Arena.Task aTaskState = m_gb.m_Arena.taskState;
                if (aTaskState == Arena.Task.PLAY)
                {
                    m_gb.HGameMove(0);
                }
                int dummy=1;
            }
        });

        leftInfo.setLayout(new GridLayout(player,1));
        allInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Arena.Task aTaskState = m_gb.m_Arena.taskState;
                if (aTaskState == Arena.Task.PLAY)
                {
                    m_gb.HGameMove(5);
                }
                int dummy=1;
            }
        });

        Font font=new Font("Arial",0,(int)(2* Types.GUI_HELPFONTSIZE));
        holeCard1.setFont(font);
        holeCard2.setFont(font);
        flopCard1.setFont(font);
        flopCard2.setFont(font);
        flopCard3.setFont(font);
        turnCard.setFont(font);
        riverCard.setFont(font);

    }

    public void setTableData(TableModel dataModel){
        this.playerTable.setModel(dataModel);
    }

    public void disableButtons(){
        checkButton.setEnabled(false);
        betButton.setEnabled(false);
        callButton.setEnabled(false);
        raiseButton.setEnabled(false);
        foldButton.setEnabled(false);
        allInButton.setEnabled(false);
    }

    public void enableFold(){
        foldButton.setEnabled(true);
    }
    public void enableCheck(){
        checkButton.setEnabled(true);
    }
    public void enableBet(){
        betButton.setEnabled(true);
    }
    public void enableCall(){
        callButton.setEnabled(true);
    }
    public void enableRaise(){
        raiseButton.setEnabled(true);
    }
    public void enableAllIn(){ allInButton.setEnabled(true);}

    public void addPlayerData(JPanel toAdd){
        leftInfo.add(toAdd);
    }

    public void updatePot(int x){
        pot.setText(Integer.toString(x));
    }

    public void updateHoleCards(PlayingCard[] cards){
        if(cards[0]!=null) {
            holeCard1.setText(cards[0].toString());
            holeCard2.setText(cards[1].toString());
        }else{
            holeCard1.setText("");
            holeCard2.setText("");
        }
    }

    public void updateCommunityCards(PlayingCard[] cards){
        if(cards != null){
            if(cards[0] != null){
                flopCard1.setText(cards[0].toString());
                flopCard2.setText(cards[1].toString());
                flopCard3.setText(cards[2].toString());
            }else{
                flopCard1.setText("");
                flopCard2.setText("");
                flopCard3.setText("");
            }
            if(cards[3] != null){
                turnCard.setText(cards[3].toString());
            }else{
                turnCard.setText("");
            }
            if(cards[4] != null){
                riverCard.setText(cards[4].toString());
            }else{
                riverCard.setText("");
            }
        }
    }

}
