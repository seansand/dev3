//nnfpbeta.groovy

import java.util.regex.*;

Random r = new Random()
final Integer YEAR = Constants.YEAR

try
{
   // First get picks and return them as request.picksTxt

   URL url = new URL('https://www.dropbox.com/s/i9tomzuiummmpwt/picks.txt?dl=1&' + r.nextInt() )

   def response = url.get()
   
   assert response.responseCode == 200
   assert response.text.contains('NNFP Week')

   Pattern p = Pattern.compile("(([\\d]+))")
   Matcher m = p.matcher(response.text)
   assert m.find()
   request.week = m.group(1)
   
   def picksTxt = response.text
   request.picksTxt = response.text
   
   // Then gets odds and return them as request.odds and request.opps
	
   request.grid = convertToMapOfMaps(response.text)
   
   // Then get MFL results and return them as request.results   
  
   // NEW 
   String newestText = getNewestMflUrlResponse()
   request.mflXml = newestText
   
   // ORIG
   //String uString = "https://api.myfantasyleague.com/$YEAR/export?TYPE=nflSchedule&L=&W=${request.week}&whatever=${r.nextInt()}";
   //def mflUrl = new URL(uString);  
   //response = mflUrl.get()
   //assert response.responseCode == 200
   //request.mflXml = response.text
   
   request.oddsMap = [:];
   request.oppsMap = [:];
   request.mfl = convertXmlToJson(newestText, request.oddsMap, request.oppsMap)
}

finally
{
   
}

//////////////
forward '/WEB-INF/pages/nnfp.gtpl'
//////////////


String getNewestMflUrlResponse() {

   final Integer YEAR = Constants.YEAR
   List responseTexts = [];
   Random r = new Random()
   String uString
   def thisResponse

   Integer i;
   for (i = 0; i < 10; ++i)
   {
      uString = "https://api.myfantasyleague.com/$YEAR/export?TYPE=nflSchedule&L=&W=${request.week}&whatever=${r.nextInt()}";
      def mflUrl = new URL(uString);  

      //TEMP, uncomment when testing with fake MFL data
      //mflUrl = new URL("http://seansand.appspot.com/dx/fakedata");  // DO NOT CHECK IN

      thisResponse = mflUrl.get()
      
      // break if you have three HTTP 200 successes
      if (thisResponse.responseCode == 200)
      {
         responseTexts << thisResponse.text
         if (responseTexts.size() >= 3) 
            break;
      }
   }   

   if (i == 10)
   {
      throw new RuntimeException("Could not get good response from $uString");
   }
   
   // Check the three goodResponses.text... go through each matchup and sum the gameSecondsRemaining
   // Take the one who has the least gameSecondsRemaining.

   Integer tr0 = timeRemaining(responseTexts[0])
   Integer tr1 = timeRemaining(responseTexts[1])
   Integer tr2 = timeRemaining(responseTexts[2])

   if (tr0 <= tr1 && tr0 <= tr2)
      return responseTexts[0];
   else if (tr1 <= tr0 && tr1 <= tr2)
      return responseTexts[1];
   else
      return responseTexts[2];
}

Integer timeRemaining(String text)
{
   def nflSchedule = new XmlSlurper().parseText(text)
   Integer totalTimeRemaining = 0;
   nflSchedule.matchup.each { totalTimeRemaining += normalizeScore(it.@gameSecondsRemaining) }
   return totalTimeRemaining;
}

boolean stringContainsDigit(String str)
{
   return ( str.contains("0") ||
            str.contains("1") ||
            str.contains("2") ||
            str.contains("3") ||
            str.contains("4") ||
            str.contains("5") ||
            str.contains("6") ||
            str.contains("7") ||
            str.contains("8") ||
            str.contains("9"))
}


Integer normalizeScore(Object text)
{
   text = text.toString()
   if (text.trim() == "")
      return 0;
   else return Integer.parseInt(text)
}

String normalizeId(Object team)
{
   team = team.toString()
   team = team.toUpperCase().trim();
   switch (team)
   {
      /*   MFL id   --   NNFP id    */
      case "JAX": team = "JAC"; break;
      case "KCC": team = "KC"; break;
      case "NOS": team = "NO"; break;
      case "GBP": team = "GB"; break;
      case "SFO": team = "SF"; break;
      case "TBB": team = "TB"; break;
      case "NEP": team = "NE"; break;
      case "SDC": team = "LAC"; break;
      case "ARZ": team = "ARI"; break;
      case "AZ":  team = "ARI"; break;
	   case "RAM": team = "LAR"; break;
      case "LVR": team = "LV"; break;
      case "OAK": team = "LV"; break;
	  
      default:  break;  //do nothing
   }
   return team;

}


//NEW WAY...this method also returns oddsMap, oppsMap

/*
   example oddsMap to return
   [BAL:3.0, PIT:-3.0, ARI:6.5, CAR:-6.5, DET:6.5, DAL:-6.5, CIN:3.0, IND:-3.0]
   example oppsMap to return
   [DET:DAL, IND:CIN, BAL:PIT, PIT:BAL, DAL:DET, ARI:CAR, CAR:ARI, CIN:IND]
*/

Map convertXmlToJson(String text,
                     Map<String, Double> oddsMap,
                     Map<String, String> oppsMap)
{
   def mapOfMaps = new LinkedHashMap<String, LinkedHashMap<String, Object>>();
   
   def nflSchedule = new XmlSlurper().parseText(text)
   
   nflSchedule.matchup.each()
   {
      m ->
      
      Map prevMap = null;
      String prevOpp = ""
      String prevScore =""
      
      m.team.each()
      {
          String teamId = normalizeId(it.@id)
          Integer teamScore = normalizeScore(it.@score)
          Integer secsRemaining = normalizeScore(m.@gameSecondsRemaining)
          String isHome = it.@isHome	

		    String spreadString = it.@spread.toString();
		  
          Double spread = new Double(spreadString ? spreadString : "0.0");
          
          //For testing
          //if (teamId == "GB")  secsRemaining = 20  //srs delete this
          //if (teamId == "DET")  secsRemaining = 20;
          
          Map attrs = [:]
          mapOfMaps.put(teamId, attrs)
          attrs.put("score", teamScore)
          attrs.put("remaining", secsRemaining)
		    attrs.put("home", isHome);
          
          if (prevMap == null)
          {
             prevMap = attrs;
             prevScore = teamScore
             prevOpp = teamId
          }
          else
          {
             oddsMap.put(teamId, spread);
             oddsMap.put(prevOpp, -spread);
             oppsMap.put(teamId, prevOpp);
             oppsMap.put(prevOpp, teamId);
          
             attrs.put("oppScore", prevScore)
             attrs.put("opp", prevOpp)
             prevMap.put("oppScore", teamScore)
             prevMap.put("opp", teamId)       
             prevMap = null;
          }
      }
   }
   
   return mapOfMaps
}


Map<String, Map<String, Integer>> convertToMapOfMaps(String text)
{
   def mapOfMaps = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();
   
   def lines = text.split('\n')
   for (int i in (0..<lines.size()))
   {
      if (lines[i].contains("NNFP"))
         continue;
         
      if (lines[i].contains("RAJIV") || lines[i].contains("SEAN"))
      {
         def players = lines[i].split()
         players = players.findAll{it.trim().size() > 0}
         
         players.each()
         {
            mapOfMaps.put(it, [:]);         
         }
         continue;
      }
      
      if (stringContainsDigit(lines[i]))
      {
         def teamsPoints = lines[i].split();
      
         int j = 0; 
         
         mapOfMaps.keySet().each()
         {
            Map guessMap = mapOfMaps.get(it)
            guessMap.put(teamsPoints[j], teamsPoints[j+1])
            j += 2
         }
         
      }
      
   }
  
   return mapOfMaps
}

