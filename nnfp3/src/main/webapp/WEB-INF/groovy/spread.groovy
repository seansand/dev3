//Spread.groovy

package com.uwalumni.seansand.spread


final Integer YEAR = (new Date() - 55).getAt(Calendar.YEAR)  // From Constants.groovy

// The object that gets sent to the spread.gtpl file is 'request'
// Must value request.week and request.lines.

request.lines = []
request.week = params.size() == 0 ? 1 : new Integer(params.keySet()[0])



Random r = new Random()

String uString = "http://www.myfantasyleague.com/$YEAR/export?TYPE=nflSchedule&L=&W=${request.week}&whatever=${r.nextInt()}";
   
def mflUrl = new URL(uString)

//TEMP, use when faking MFL data
//def mflUrl = new URL("https://dl.dropboxusercontent.com/u/14876946/fakedata.xml");   // when faking MFL data
   
   
def urlResponse = mflUrl.get()
assert 200 == urlResponse.responseCode

def nflSchedule = new XmlSlurper().parseText(urlResponse.text)

List<Matchup> matchupList1 = processXml1(nflSchedule)

matchupList1.each() {
   request.lines << it.toFirstString()
}

request.lines << ""

List<Matchup> matchupList2 = processXml2(nflSchedule)

matchupList2.each() {
   request.lines << it.toSecondString()
}


//////////////
forward '/WEB-INF/pages/spread.gtpl'
//////////////


class Matchup implements Comparable {

   String pick
   String opponent
   Double spread
   Integer rank

   String rankToString() {
      return rank < 10 ? " " + rank : "" + rank
   }

   public String toFirstString() {
      return "${normalize(pick)} @ ${normalize(opponent)}"
   }

   public String toSecondString() {
      return "${normalize(pick)} ${rankToString()}  (vs. ${normalize(opponent)}) ${spread/10}"
   }
   
   @Override
   public int compareTo(Object other) {
       return other.spread - this.spread
   }
   
   public String normalize(String team) {
      
      switch(team) {
         case 'TBB': return 'TB ';
         case 'SFO': return 'SF ';
         case 'GBP': return 'GB ';
         case 'KCC': return 'KC ';
         case 'NEP': return 'NE ';
         case 'NOS': return 'NO ';
         default: return team
      }
      return team
   }
   
}

/**
 * Chronological list (replace with Ryan's)
 */
List<Matchup> processXml1(def nflSchedule)
{
   List<Matchup> allMatchups = []
   
   nflSchedule.matchup.each()
   {
      m ->
      
      List<Matchup> twoTeams = []

      // XML seems to always put the visitor first, then home...this code assumes this is true.
      // If this changes, then modify this 'each' to check, as there is an 'isHome' flag in the XML.

      m.team.each()
      {
          Matchup matchup = new Matchup(pick: it.@id)
          twoTeams << matchup
      }

      allMatchups << new Matchup(pick: twoTeams[0].pick, opponent: twoTeams[1].pick)
   }
   
   return allMatchups
}

/**
 * List with spreads and ranks
 */
List<Matchup> processXml2(def nflSchedule)
{
   List<Matchup> allMatchups = []
   
   nflSchedule.matchup.each()
   {
      m ->
      
      Map prevMap = null;
      
      List<Matchup> twoTeams = []
      
      m.team.each()
      {
          String teamId = it.@id
		    String spreadString = it.@spread.toString();
      
          Matchup matchup = new Matchup(pick: it.@id, 
                                        spread: 10 * new Double(spreadString ? spreadString : "0.0"))

          twoTeams << matchup
      }
      
      twoTeams[0].opponent = twoTeams[1].pick
      twoTeams[1].opponent = twoTeams[0].pick
      
      if (twoTeams[0].spread < twoTeams[1].spread) {
         allMatchups << twoTeams[0]
      } 
      else {
         allMatchups << twoTeams[1]
      }
   }
   
   allMatchups.sort() 

   int counter = 1
   allMatchups.each() {
      it.rank = counter++
   }
   
   return allMatchups
}
