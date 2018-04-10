//NNfpOddsReader.groovy

public class NnfpOddsReader
{
   Map _oddsMap
   Map _oppsMap
   boolean _isWinner;
   Random _r;
   
   public NnfpOddsReader()
   {
      _r = new Random()
      
	   _oddsMap = NflOdds.getOdds()
      _oppsMap = NflOdds.getOpps()  
	  
      /**
	  println("Using hardcoded odds")
      _oddsMap = [CLE:-3, KC:3,
	              GB:-12.5, BUF:12.5,
				  BAL:-2.5, CIN:2.5,
				  TEN:-6, PIT:6,
				  PHI:-6.5, DET:6.5,
				  DAL:-7, CHI:7,
				  CAR:-4, TB:4,
				  ATL:-6.5, ARI:6.5,
				  MIN:-5.5, MIA:5.5,
				  OAK:-3.5, STL:3.5,
				  DEN:-3.5, SEA:3.5,
				  HOU:-2.5, WAS:2.5,
				  SD:-7, JAC:7,
				  NE:-3, NYJ:3,
				  IND:-4, NYG:4,
				  NO:-5.5, SF:5.5]
      /**/          
      
	   //println _oddsMap.toString()
	   
   }

   public boolean isLoser(String team)
   {
      return !_isWinner;
   }

   public boolean isWinner(String team)
   {
      double gaussian = _r.nextGaussian() * 13.86   // From the Hal Stern document
   
      double odds = _oddsMap.get(team)   // negative means team is the favorite
      
      if (odds == null)
         odds = _oddsMap.get(NnfpSlurper.convertBack(team))  //abbreviation change? 2012
      
      if (odds == null)
         throw new IllegalStateException("Unknown team = $team")
      
      _isWinner = odds + gaussian <= 0.0
       
      return _isWinner;
   }

   public boolean isTier(String team)
   {
      assert false;  // should never happen
   }
	
	public void incrementGameCounter()
	{
	}
   
   public Map getOpps()
   {
      return _oppsMap;
   }
}
