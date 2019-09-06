// NflOdds.groovy 
// Date: Nov 1, 2008 
// Does NNFP Picks

public class NflOdds
{

   private static Map _oppsMap = new HashMap()

   public static void main(String[] args)
   {
      def oddsMap = NflOdds.getOdds()
	   println()
	   println("oddsMap = $oddsMap")
      
      def oppsMap = NflOdds.getOpps()
	   println()
	   println("oppsMap = $oppsMap")
   }

   public static Map<String, Double> getOdds()
   {
      def retVal = getOdds(new ArrayList<String>())
      return retVal
   }
   
   public static Map getOpps()
   {
      def retVal = _oppsMap;
      return retVal;
   }
   
   public static Map<String, Double> getOdds(List<String> output)
   {
      /*
      Properties props = System.getProperties(); 
      props.put("http.proxyHost", "webproxy.int.westgroup.com"); 
      props.put("http.proxyPort", "80"); 
      */
        
      //////////////def url = new URL("http://www.gamblerspalace.com/LiveLines.xml") 
      def url = new URL("http://lines.gamblerspalace.com/")
      //def url = new URL("http://home.comcast.net/~seansand/nflodds.xml")
	   //def url = new URL("http://dl.dropbox.com/u/14876946/nflodds.xml")
      
     
      def xmlString =  url.getText(); 
      // Get rid of opening junk 
	  
	  if (xmlString.contains("<?xml"))
         xmlString = xmlString.substring(xmlString.indexOf("<?xml")) 
      
	  def matchupList = [] 

      println()

      def slurper 
      try
      {
	      slurper = new XmlSlurper().parseText(xmlString) 
         def leagues = slurper.Leagues

         Set forceUnique = new HashSet()  //just in case a team is erroneously listed twice
         
         leagues.league.each() 
         { 
            league ->

            if (league.@IdSport.toString().trim() == "NFL" && league.@Description.toString().trim() == "NFL") 
            { 
               league.game.each() 
               { 
                   game ->

                   //println(game.@vtm.toString() + "vs" + game.@htm.toString());
                   
                   if (gameIsWithinAWeek(game) &&
                      (!forceUnique.contains(Matchup.teamName(game.@htm.toString()))))
                   {
                      forceUnique.add(Matchup.teamName(game.@htm.toString()))
                  
                      Matchup m = new Matchup(game.@htm.toString(), 
                                              game.@vtm.toString(),
                                              game.line.@hsprdt.toString()) 
                      matchupList.add(m)          
                                                  
                      _oppsMap.put(m.getHomeTeam().trim(), m.getAwayTeam().trim())
                      _oppsMap.put(m.getAwayTeam().trim(), m.getHomeTeam().trim())
                   }
               } 
            } 
         } 
        
         def retVal = new LinkedHashMap<String, Double>();
         getValues(matchupList)
		 
         matchupList.each() 
         {
            println it.toString()
            output.add(it.toString())
			
            retVal.put(it.getAwayTeam().trim(), (it.getHomeFavorite() ? 1 : -1) * new Double(it.getHomeSpread()))            
			   retVal.put(it.getHomeTeam().trim(), (it.getHomeFavorite() ? -1 : 1) * new Double(it.getHomeSpread()))
            
		   } 

         return retVal
      }
      catch (Exception e)
      {
         println xmlString
         println()
         e.printStackTrace()
      }

		println()
     
      // Done. 
   
   }


   // Return true if the game is within the next seven days
   static boolean gameIsWithinAWeek(def game)
   {
      long SIX_HOURS_MILLIS = 1000L * 60 * 60 * 6;
      long EIGHTEEN_HOURS_MILLIS = 1000L * 60 * 60 * 18;
      long ONE_DAY_MILLIS = 1000L * 60 * 60 * 24;
   
      def sdf = new java.text.SimpleDateFormat("yyyyMMdd"); 

      String gameDateString = game.@gmdt.toString();   // "20130926"
      
      Date gameDate = sdf.parse(gameDateString);
      long gameDateLong = gameDate.getTime() + EIGHTEEN_HOURS_MILLIS;
      long now = System.currentTimeMillis();
      Date nowDate = new Date(now - SIX_HOURS_MILLIS);
      String currWeekday = nowDate.toString().substring(0,3);

      //println("gameDate: $gameDate");
      //println("gameDateLong: $gameDateLong");
      //println("now: $now")
      //println("nowDate: $nowDate")
      //println("currWeekday: $currWeekday")
      
      switch(currWeekday)
      {
         case "Tue": return gameDateLong - now < ONE_DAY_MILLIS * 7;
         case "Wed": return gameDateLong - now < ONE_DAY_MILLIS * 6;
         case "Thu": return gameDateLong - now < ONE_DAY_MILLIS * 5;
         case "Fri": return gameDateLong - now < ONE_DAY_MILLIS * 4;
         case "Sat": return gameDateLong - now < ONE_DAY_MILLIS * 3;
         case "Sun": return gameDateLong - now < ONE_DAY_MILLIS * 2;
         case "Mon": return gameDateLong - now < ONE_DAY_MILLIS * 1;
      }
      
      return false; // this is not possible
   }

   static getValues(List matchupList) 
   { 
      TreeMap map = new TreeMap() 
      matchupList.each() 
      { 
         while (map.containsKey(it.getSpread())) 
            it.setSpread(it.getSpread() + 0.01) 
         map.put(it.getSpread(), it)
      } 
      int counter = 1 
      map.each() 
      { 
          it.getValue().setValue(counter++) 
      } 
   } 

}


class Matchup 
{ 
    String favorite = "" 
    String otherTeam = "" 
    String homeTeam = "" 
    String awayTeam = "" 
    String homeSpread = ""
    Boolean homeFavorite = true 
    Double spread = 0.0 
    Integer value = 0 
    Matchup (String home, String away, String homeSpreadIn) 
    { 
       homeTeam = teamName(home)
       awayTeam = teamName(away)
       homeSpread = homeSpreadIn

       homeFavorite = homeSpread.startsWith('-') 
       /*
       try 
       { 
          homeSpread = homeSpread.substring(0, homeSpread.lastIndexOf("-")) 
       } 
       catch (Exception e) 
       { 
          try
          {
             if (homeSpread.contains("+"))
                homeSpread = homeSpread.substring(0, homeSpread.lastIndexOf("+")) 
          }
          catch (Exception e2)
          {
             println ("OFF: $home $away")
             homeSpread = "0.0";
          }
       } 

       homeSpread = homeSpread.replaceAll("\\-", "").replaceAll("\\+", "") 
       */
       homeSpread = homeSpread.replaceAll("\\+", "")
       homeSpread = homeSpread.replaceAll("-", "")
       homeSpread = homeSpread.replaceAll("Â½", ".5") 
       homeSpread = homeSpread.replaceAll("n/a", "0") 

       if (homeSpread == "") 
          homeSpread = "0" 
       spread = new Double(homeSpread) 
       favorite = homeFavorite? teamName(home) : teamName(away) 
       otherTeam = homeFavorite? teamName(away) : teamName(home) 
    } 

 

    String toString() 
    { 
        String valStr = value.toString() 
        if (valStr.size() == 1) 
           valStr = " " + valStr 
        "$favorite $valStr   ($awayTeam @ $homeTeam) ${homeFavorite ? "-" : " "}$homeSpread" 
    } 

    // Only works when the "½" character is supported 
    String toString(Double f) 
    { 
        String origString = f.toString() 
        String fString = origString.substring(0, origString.indexOf(".")) 
        //origString.contains(".5") ? fString + "½" : fString 
        return origString.contains(".5") ? fString + ".5" : fString + ".0" 
    } 
       
    // srs note - no longer used.
    static String teamName(String name) 
    { 
        name = name.toUpperCase() 
        if (name.contains("NFC")) return "NFC" 
        if (name.contains("AFC")) return "AFC" 
        if (name.contains("SEAT")) return "SEA" 
        if (name.contains("FRAN")) return "SF " 
        if (name.contains("ARIZ")) return "ARI" 
        if (name.contains("ST.")) return "STL" 
        if (name.contains("GREE")) return "GB " 
        if (name.contains("CHIC")) return "CHI" 
        if (name.contains("MINN")) return "MIN" 
        if (name.contains("DETR")) return "DET" 
        if (name.contains("ORLE")) return "NO " 
        if (name.contains("TAMP")) return "TB " 
        if (name.contains("CARO")) return "CAR" 
        if (name.contains("ATLA")) return "ATL" 
        if (name.contains("GIAN")) return "NYG" 
        if (name.contains("DALL")) return "DAL" 
        if (name.contains("WASH")) return "WAS" 
        if (name.contains("PHIL")) return "PHI" 
        if (name.contains("DIEG")) return "SD " 
        if (name.contains("DENV")) return "DEN" 
        if (name.contains("OAKL")) return "OAK" 
        if (name.contains("KANS")) return "KC " 
        if (name.contains("BALT")) return "BAL" 
        if (name.contains("CLEV")) return "CLE" 
        if (name.contains("CINC")) return "CIN" 
        if (name.contains("PITT")) return "PIT" 
        if (name.contains("INDI")) return "IND" 
        if (name.contains("HOUS")) return "HOU" 
        if (name.contains("TENN")) return "TEN" 
        if (name.contains("JACK")) return "JAC" 
        if (name.contains("MIAM")) return "MIA" 
        if (name.contains("JETS")) return "NYJ" 
        if (name.contains("ENGL")) return "NE " 
        if (name.contains("BUFF")) return "BUF" 
        throw new IllegalStateException("Team name $name not found") 
    } 
    
    String teamNameOld(String name) 
    { 
        name = name.toUpperCase() 
        if (name.startsWith("NFC")) return "NFC" 
        if (name.startsWith("AFC")) return "AFC" 
        if (name.startsWith("SEA")) return "SEA" 
        if (name.contains("FRA")) return "SF " 
        if (name.startsWith("ARI")) return "ARI" 
        if (name.startsWith("ST.")) return "STL" 
        if (name.startsWith("GRE")) return "GB " 
        if (name.startsWith("CHI")) return "CHI" 
        if (name.startsWith("MIN")) return "MIN" 
        if (name.startsWith("DET")) return "DET" 
        if (name.contains("ORL")) return "NO " 
        if (name.startsWith("TAM")) return "TB " 
        if (name.startsWith("CAR")) return "CAR" 
        if (name.startsWith("ATL")) return "ATL" 
        if (name.contains("GIA")) return "NYG" 
        if (name.startsWith("DAL")) return "DAL" 
        if (name.startsWith("WAS")) return "WAS" 
        if (name.startsWith("PHI")) return "PHI" 
        if (name.contains("DIE")) return "SD " 
        if (name.startsWith("DEN")) return "DEN" 
        if (name.startsWith("OAK")) return "OAK" 
        if (name.startsWith("KAN")) return "KC " 
        if (name.startsWith("BAL")) return "BAL" 
        if (name.startsWith("CLE")) return "CLE" 
        if (name.startsWith("CIN")) return "CIN" 
        if (name.startsWith("PIT")) return "PIT" 
        if (name.startsWith("IND")) return "IND" 
        if (name.startsWith("HOU")) return "HOU" 
        if (name.startsWith("TEN")) return "TEN" 
        if (name.startsWith("JAC")) return "JAC" 
        if (name.startsWith("MIA")) return "MIA" 
        if (name.contains("JET")) return "NYJ" 
        if (name.contains("ENG")) return "NE " 
        if (name.startsWith("BUF")) return "BUF" 
        throw new IllegalStateException("Team name $name not found") 
    } 
} 

