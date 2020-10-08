package src.games.Poker;

public class Pot {
    private int size;
    private int[] open;
    private boolean[] claim;

    public Pot(){
        open = new int[StateObserverPoker.NUM_PLAYER];
        claim = new boolean[StateObserverPoker.NUM_PLAYER];
    }

    public Pot(Pot opot){
        this.size =  opot.size;
        this.open = new int[opot.open.length];
        this.claim = new boolean[opot.open.length];
        for(int i=0;i<open.length;i++) {
            this.open[i] = opot.open[i];
            this.claim[i] = opot.claim[i];
        }
    }

    public int getOpenPlayer(int player){
        return open[player];
    }


    public void add(int chips, int player){
        size += chips;

        int dif =  chips - open[player];

        if(dif > 0) {
            for (int i = 0; i < open.length; i++)
                open[i] += dif;
            claim = new boolean[StateObserverPoker.NUM_PLAYER];
        }
        claim[player] = true;
        open[player] -= chips;
    }

    public Pot split(int chips, int player){
        Pot splitPot = new Pot();

        int dif = open[player]-chips;
        splitPot.size = dif*getPaid();

        this.size -= splitPot.size;

        for (int i = 0; i < open.length; i++){
            if(open[i]>0){
               open[i] -= dif;
               splitPot.open[i] = dif;
            }
            splitPot.claim[i] = claim[i];
        }
        if(chips>0) {
            this.size += chips;
            this.claim[player] = true;
            this.open[player] = 0;
        }
        return splitPot;
    }

    private int getPaid(){
        int p = 0;
        for(int i:open)
            p+=i>0?0:1;
        return p;
    }

    public int getSize(){
        return size;
    }

    public Pot copy(){
        return new Pot(this);
    }

    public String toString(){
        String sout = "";
        sout += "Pot: " + size + " (";
        for(int i = 0 ; i < open.length ; i++)
            sout += Integer.toString(i) + ": "+Integer.toString(open[i])+", ";
        return sout + ")";
    }
    public boolean[] getClaims(){
        return claim;
    }

}
