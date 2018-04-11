//NNfpSlurper.groovy

public class NnfpSlurper
{
   String mYear;  //default

   List mWinners = [];
   List mLosers = [];
   List mTiers = [];

   public static void main(String[] args)
   {
      def slurper = new NnfpSlurper(2, 2008);

      assert slurper.isLoser("KC");
      assert slurper.isLoser("JAX");
      assert slurper.isWinner("WAS");

      println("Success, all three are TRUE");
   }


   public NnfpSlurper(int week, int year)
   {
      mYear = "$year";
      println("NnfpSlurper reading results for week $week of $mYear.")

      try
      {
         String mills = "" + System.currentTimeMillis() 
      
         def url = new URL(
            "http://www.myfantasyleague.com/$mYear/export?TYPE=nflSchedule&L=&W=$week&whatever=$mills")

         def nflResultsString =  url.getText();
    
         def nflSchedule = new XmlSlurper().parseText(nflResultsString)

         nflSchedule.matchup.each()
         {
            if (it.@gameSecondsRemaining.text() == "0")
            {
               String score0Str = it.team[0].@score.text()
               String score1Str = it.team[1].@score.text()
   
               if (score0Str != "" && score1Str != "")
               {
                  int score0 = new Integer(score0Str)
                  int score1 = new Integer(score1Str)

                  if (score0 > score1)
                  {
                     mWinners.add(it.team[0].@id.text())
                     mLosers.add(it.team[1].@id.text())
                  }
                  else if (score0 < score1)
                  {
                     mWinners.add(it.team[1].@id.text())
                     mLosers.add(it.team[0].@id.text())
                  }
                  else if (score0 == score1)
                  {
                     mTiers.add(it.team[0].@id.text())
                     mTiers.add(it.team[1].@id.text())
                  }
               } 
            }
         } 
      }
      catch (Exception e)
      {
         println("Failure in reading results from web site:");
         println(e.getMessage);
      }
      println();
   }

   public boolean isLoser(String team)
   {
      team = convert(team);
      return team in mLosers;
   }

   public boolean isWinner(String team)
   {
      team = convert(team);
      return team in mWinners;
   }

   public boolean isTier(String team)
   {
      team = convert(team);
      return team in mTiers;
   }
   
   /* team = NNFP name (left)
      converts to MFL name (right) */
   public static String convert(String team)
   {
      team = team.toUpperCase().trim();
      switch (team)
      {
         case "JAX": team = "JAC"; break;
         case "KC":  team = "KCC"; break;
         case "NO":  team = "NOS"; break;
         case "GB":  team = "GBP"; break;
         case "SF":  team = "SFO"; break;
         case "TB":  team = "TBB"; break;
         case "NE":  team = "NEP"; break;
         case "SD":  team = "LAC"; break;
         case "AZ":  team = "ARI"; break;
         case "ARZ": team = "ARI"; break;
		 case "LA":  team = "LAR"; break;
      }
      return team;
   }
   
   /* team = MFL name (left)
      converts to NNFP name (right) */
   public static String convertBack(String team)
   {
      team = team.toUpperCase().trim();
      switch (team)
      {
         case "JAC": team = "JAX"; break;
         case "KCC": team = "KC"; break;
         case "NOS": team = "NO"; break;
         case "GBP": team = "GB"; break;
         case "SFO": team = "SF"; break;
         case "TBB": team = "TB"; break;
         case "NEP": team = "NE"; break;
         case "SDC": team = "LAC"; break;
         case "ARI": team = "ARZ"; break;
		 case "RAM": team = "LAR"; break;
      }
      return team;
   }
   
   public void incrementGameCounter()
	{
	}

}
