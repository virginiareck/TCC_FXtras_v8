package app;

public class HypothesisState
{
    private String nameHypState = "" ;
    
    private Double prob_Hyp = 0.0 ;
    
    public HypothesisState()
    {
        
    }
    
    public HypothesisState(String name)
    {
        this.nameHypState = name ;
    }
    
    public HypothesisState(double prob)
    {
        this.prob_Hyp = prob ;
    }
    
    public HypothesisState(String name, double prob)
    {
        this.nameHypState = name ;
        this.prob_Hyp = prob ;
    }

    public String getNameHypState ()
    {
        return nameHypState;
    }

    public void setNameHypState (String name)
    {
        this.nameHypState = name;
    }

    public Double getProb_Hyp ()
    {
        return prob_Hyp;
    }

    public void setProb_Hyp (Double prob)
    {
        this.prob_Hyp = prob;
    }    
}
