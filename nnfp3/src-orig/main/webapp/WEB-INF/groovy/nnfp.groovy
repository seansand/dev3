//nnfpbeta.groovy

import java.util.regex.*;

final Integer YEAR = Constants.YEAR

Random r = new Random()

try
{
   log.info("nnfpbeta.groovy begin")

   // First get picks and return them as request.picksTxt

   URL url = new URL('https://dl.dropbox.com/u/14876946/picks.txt?' + r.nextInt() )

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
	
   request.oddsMap = NflOdds.getOdds()
   request.oppsMap = NflOdds.getOpps()  
   
   request.grid = convertToMapOfMaps(response.text)
   
   // Then get MFL results and return them as request.results   
  
   def mflUrl = new URL(
      "http://football8.myfantasyleague.com/$YEAR/export?TYPE=nflSchedule&L=&W=${request.week}&whatever=${r.nextInt()}")
  
   response = mflUrl.get()
   assert response.responseCode == 200
   
   request.mflXml = response.text
   
   request.mfl = convertXmlToJson(response.text)

   
}

finally
{
   log.info("nnfpbeta.groovy end")
}

//////////////
forward '/WEB-INF/pages/nnfp.gtpl'
//////////////

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
      case "JAX": team = "JAC"; break;
      case "KCC": team = "KC"; break;
      case "NOS": team = "NO"; break;
      case "GBP": team = "GB"; break;
      case "SFO": team = "SF"; break;
      case "TBB": team = "TB"; break;
      case "NEP": team = "NE"; break;
      case "SDC": team = "SD"; break;
      case "ARZ": team = "ARI"; break;
      case "AZ":  team = "ARI"; break;
      default:  break;  //do nothing
   }
   return team;

}


Map convertXmlToJson(String text)
{
   def mapOfMaps = new LinkedHashMap<String, LinkedHashMap<String, Object>>();
   
   def nflSchedule = new XmlSlurper().parseText(text)
   
   println nflSchedule.matchup.size()
   
   nflSchedule.matchup.each()
   {
      m ->
      
      println m.@gameSecondsRemaining
      
      Map prevMap = null;
      String prevOpp = ""
      String prevScore =""
      
      m.team.each()
      {
          String teamId = normalizeId(it.@id)
          Integer teamScore = normalizeScore(it.@score)
          Integer secsRemaining = normalizeScore(m.@gameSecondsRemaining)
          String isHome = it.@isHome		  
          
          println teamScore
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
      
      log.info("lines: " + lines[i])
      
      if (stringContainsDigit(lines[i]))
      {
         def teamsPoints = lines[i].split();
         log.info("here: " + teamsPoints.toString())
      
         int j = 0; 
         
         mapOfMaps.keySet().each()
         {
            Map guessMap = mapOfMaps.get(it)
            guessMap.put(teamsPoints[j], teamsPoints[j+1])
            log.info("putting ${teamsPoints[j]}, ${teamsPoints[j+1]} ")
            j += 2
         }
         
      }
      
   }
  
   return mapOfMaps
}

