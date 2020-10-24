package src.games.Poker;

import java.util.ArrayList;

public class Pots {
        private ArrayList<Pot> pots;
        private Pot activePot;

        Pots(){
            pots = new ArrayList<Pot>();
            pots.add(new Pot());
            activePot = pots.get(0);
        }

        Pots(Pots copy){
            pots = new ArrayList<Pot>();
            for(Pot p:copy.pots)
                this.pots.add(new Pot(p));
        }

        public double add(double o_chips, int player,boolean allin) {
            double chips = o_chips;
            int numPot = pots.size();
            for(int i = 0;i<numPot;i++){
                Pot pot = pots.get(i);
                double toCall = pot.getOpenPlayer(player);

                if(chips >= toCall) {
                    pot.add(toCall, player);
                    chips -= toCall;
                }else{
                    pots.add(i+1,pot.split(chips, player));
                    chips = 0;
                    break;
                }
            }
            if(chips>0) {
                pots.get(pots.size() - 1).add(chips, player);
                if (allin)
                    pots.add(pots.size(), pots.get(pots.size() - 1).split(0, player));
            }
            return 0;
        }

        public double add(double o_chips, int player){
           return add(o_chips,player,false);
        }

        public int getOpenPlayer(int player){
            int open = 0;
            for (Pot pot: pots) {
                open += pot.getOpenPlayer(player);
            }
            return open;
        }

        public int getSize(){
            int size = 0;
            for (Pot pot: pots) {
                size += pot.getSize();
            }
            return size;
        }
        public Pots copy(){
            return new Pots(this);
        }

        public String toString(){
            String sout = "";
            for (Pot pot: pots){
                sout += pot.toString();
            }
            return sout;
        }

        public boolean[][] getClaims(){
            boolean[][] claims = new boolean[pots.size()][];
            for(int i = 0;i< pots.size();i++){
                claims[i] = pots.get(i).getClaims();
            }
            return claims;
        }

        public double getPotSize(int p){
            return this.pots.get(p).getSize();
        }

        public void allIn(int player){

        }
/*
        public int[][] getClaims(){
            int[][] claims = new int[pots.size()][];
            for(int i = 0;i< pots.size();i++){
                claims[i] = pots.get(i).getClaims();
            }
            return claims;
        }
 */

}
