var reloading;

function checkReloading() 
{
    if (window.location.hash=="#autorefresh") {
        reloading=setTimeout("window.location.reload();", 180000);
        document.getElementById("reloadCB").checked=true;
    }
}

function toggleAutoRefresh(cb) 
{
    if (cb.checked) {
        window.location.replace("#autorefresh");
        reloading=setTimeout("window.location.reload();", 180000);
    } else {
        window.location.replace("#");
        clearTimeout(reloading);
    }
}

function onDocumentLoad()
{
   var oddsObj = JSON.parse(document.getElementById("odds").innerHTML);
   var oppsObj = JSON.parse(document.getElementById("opps").innerHTML);
   var gridObj = JSON.parse(document.getElementById("grid").innerHTML);
   var mflObj = JSON.parse(document.getElementById("mfl").innerHTML);  

   var resultsMap = getCurrentNflResults(mflObj);

   colorTheDocumentGrid(resultsMap, "winners", "Green");
   colorTheDocumentGrid(resultsMap, "losers", "Red");
   colorTheDocumentGrid(resultsMap, "ties", "Blue");
   
   $(".matchup").html(function(index, currentContent) {
      return currentContent + "&nbsp;";  //add breaking space to end, see if that helps mobile margin
   })

   
   var projectedLosers = [];
   
   var playerScoreArray = 
      getNnfpWinner(gridObj, resultsMap.winners, resultsMap.losers, resultsMap.ties, projectedLosers);  

   displayCurrentScoresHorizontal(playerScoreArray, resultsMap.unknown);
   
   // Now do projections, if games are in progress
   
   var projections = getProjections(resultsMap, playerScoreArray, gridObj, oddsObj, mflObj, resultsMap.unknown);
   if (resultsMap.unknown.length > 0)
   {
      displayProjections(projections);
      displayKeyMatchups(projections, mflObj);
   }     
   displayNflProjections(projections, mflObj, resultsMap);   // always display now.

   replaceHyphens();
 
   checkReloading(); 
   
   // Adds yellow highlighting
   $( ".projection" ).hover(
       function(){
           $(this).css("background-color", "yellow");
       }, 
       function(){
           $(this).css("background-color", "white");
       }
    );
    
   var matchupGridWidth = $(".matchup").width();

   // Make sure result table is not too small
   // (Seems to only happen for some mobile browsers?)
   if (matchupGridWidth > $( "table.result").width()) {
      $( "table.result").width(matchupGridWidth);
   } 

   // Make sure NFL projection table is not too small
   // It should be at least as large as matchup (should be larger)
   if (matchupGridWidth > $( "table.thin").width()) {
      $( "table.thin").width(matchupGridWidth);
   }
    
}

function replaceHyphens() {

   var line = jQuery(".matchup")[1];
   var orig = line.innerHTML;
   line.innerHTML = '<nobr>' + replaceAll(orig, "-", "&ndash;") + '</nobr>';  //non-breaking hyphens
   
}


function hasLowercase(str) {
    return str != str.toUpperCase();
}


function displayKeyMatchups(projections, mfl)
{
   var keyMatchupsArray = calculateKeyMatchups(projections, mfl);
   
         /* keyMatchup:
      
      {player: CHAD
       playerWins:  200
       order: ["GB", "DET", ...]
       GB : {teamWins=150, percent= 75, aboveAverage= 25, mustWin = false; clinches = false},
       DET : {teamWins=150, percent= 75, aboveAverage= 25, mustWin = false; clinches = false},
       }}}
               
      */
   
   var MAX_MATCHUP_DISPLAY = 7;   // was 10, then 9, now 7
   
   
   for (var i = 0; i < keyMatchupsArray.length; ++i)
   {
     
      var keyMatchup = keyMatchupsArray[i];
      var matchesLeft = keyMatchup.order.length;
      
      var html = "<FONT class='tinyfont'>";
      
      for (var j = 0; j < Math.min(MAX_MATCHUP_DISPLAY, matchesLeft); ++j)   //display no more than 10
      {
         var teamName = keyMatchup.order[j];
         var teamObj = keyMatchup[teamName];

         if (teamObj.clinches)
         {
            html += "<FONT class='tinyfont' COLOR=Green><B>" + make6(teamName + ":$$") + "&nbsp; </B></FONT>";
         }
         if (teamObj.mustWin)
         {
            html += "<FONT class='tinyfont' COLOR=Red><B>" + make6(teamName + ":MW") + "&nbsp; </B></FONT>";
         }
         else if (!teamObj.clinches && teamObj.aboveAverage >= 10)
         {
            html += make6(teamName + "+" + teamObj.aboveAverage) + "&nbsp; ";
         }
      }
      
      html += '</FONT></nobr>';

      // now replace HTML
      var projHtml = document.getElementById(keyMatchup.player + "proj").innerHTML;
      document.getElementById(keyMatchup.player + "proj").innerHTML = projHtml + html;
   }
}


function calculateKeyMatchups(projections, mfl)
{
   var keyMatchupsArray = []
   
   for (var player in projections)
   {
      if (hasLowercase(player))
         continue;
         
      var playerWinsObj = fetchKeyInArray(projections.playerWinsArray, player);
      var playerWins = getOnlyVal(playerWinsObj);
      var teamWinsMap = projections[player];
      var reps = getReps();
      
      /*want to generate an object, keyMatchupObj:
      
      {player: CHAD
       playerWins:  200
       order: ["GB", "DET", ...]
       GB : {teamWins=150, percent= 75, aboveAverage= 25, mustWin = false; clinches = false},
       DET : {teamWins=150, percent= 75, aboveAverage= 25, mustWin = false; clinches = false},
       }}}
               
      */
      
      var keyMatchupObj = {"player": player,
                           "playerWins": playerWins,
                           "order": []};
                           
      keyMatchupsArray[keyMatchupsArray.length] = keyMatchupObj;
      
      // {"GB": 210, "DET": 110...}
      
      for (var team in teamWinsMap)
      {
         var teamObj = {"teamWins": teamWinsMap[team],
                        "mustWin": teamWinsMap[team] == playerWins,
                        "clinches" : false};
         teamObj.percent = Math.round(teamWinsMap[team] * 100 / playerWins);
         teamObj.aboveAverage = teamObj.percent - Math.round(projections.overallNflWinners[team] * 100 / reps);
      
         //TODO should 3 be larger?  Prevents a team from being "mustwin" for everyone when it hits 98%
         if (teamObj.aboveAverage < 3)  
            teamObj.mustWin = false;
      
         keyMatchupObj[team] = teamObj;
         keyMatchupObj.order[keyMatchupObj.order.length] = team;
      }
      
   }
   
   if (keyMatchupsArray.length > 1)   //i.e. there is more than one player left
   {   
      // calculate clinches
     
      for (var team in projections.overallNflWinners)
      {
         //check to see if all but one player has the "mustWin" flag turned on for it.
         //If so, then that player should have the clinches flag turned on for it.
         
         var counter = 0;
         for (var i = 0; i < keyMatchupsArray.length; ++i)
         {
            //keyMatchupsArray[i] is undefined when the opp is a mustwin.
            if (keyMatchupsArray[i][team] == undefined)
            {
               keyMatchupsArray[i][team] = {"mustWin":false, "clinches":false};
            }
            
            if (keyMatchupsArray[i][team].mustWin)
               ++counter;
         }

         // turn on clinches flag         
         if (counter + 1 == keyMatchupsArray.length)
         {
            for (var i = 0; i < keyMatchupsArray.length; ++i)
            {
               if (keyMatchupsArray[i][team] != undefined &&   //undefined should never happen now.
                   keyMatchupsArray[i][team].mustWin == false)
                  keyMatchupsArray[i][mfl[team].opp].clinches = true;
            }
         }
      }
      
      // sort each players teams in order of "clinches", "mustWin", "aboveAverage".
      
      for (var i = 0; i < keyMatchupsArray.length; ++i)
      {
         keyMatchupsArray[i].order.sort( function(teamA, teamB) {

            var heuristicA = 0;
            var heuristicB = 0;
         
            if (keyMatchupsArray[i][teamA].clinches)
            {
               heuristicA = 1000;
            }
            if (keyMatchupsArray[i][teamB].clinches)
            {
               heuristicB = 1000;
            }
            if (keyMatchupsArray[i][teamA].mustWin)
            {
               heuristicA += 2000;
            }
            if (keyMatchupsArray[i][teamB].mustWin)
            {
               heuristicB += 2000;
            }
             
            heuristicA += keyMatchupsArray[i][teamA].aboveAverage;
            heuristicB += keyMatchupsArray[i][teamB].aboveAverage;
            
            return heuristicB - heuristicA;
         });
      }
   }
   
   return keyMatchupsArray;
}



function displayTimeRemaining(remaining, awayScore, homeScore)
{
   if (remaining == 3600 ||(remaining == 0 && awayScore == 0 && homeScore == 0))
   {
      return " ";
   }
   var retVal = "";
   if (remaining > 2700)
      retVal += "1st";
   else if (remaining > 1800)
      retVal += "2nd";
   else if (remaining == 1800)
      retVal += "Half";
   else if (remaining > 900)
      retVal += "3rd";
   else
   {
      var quarter = ((remaining - 1) % 900) + 1;
      
      if (remaining == 0) 
         quarter = 0;
      
      var minutes = Math.floor(quarter / 60);
      var seconds = quarter % 60;
      
      retVal += minutes + ":";
      retVal += seconds < 10 ? "0" + seconds : seconds;
   }
   return retVal + " &nbsp;";
}

function displayNflScores(awayTeam, awayScore, homeTeam, homeScore, remaining)
{
   if (remaining == 3600 || (remaining == 0 && awayScore == 0 && homeScore == 0))
   {
      return awayTeam + " @ " + homeTeam;
   }
   return awayTeam + " " + awayScore + ", " + homeTeam + " " + homeScore;
}

function displayNflProjections(projections, mfl, resultsMap)
{
   //resultsMap only used for displaying scores of completed games	
	
   var nflString = "<HR color='white'>"
      // "<P>NFL game projections:<span class='bigfont'>&nbsp;</span><BR>";
   var reps = getReps();
   
   var winners = projections.overallNflWinners;
   
   var nflStringArray = [];

   // display completed games - winners
   
   
   for (var i = 0, len = resultsMap.winners.length; i < len; i++)
   {
	 var team = resultsMap.winners[i];  
	   
	 var opp = mfl[team].opp;
	 var home = (1 == mfl[team].home);
	 var homeTeam = home ? team : mfl[team].opp;
	 var awayTeam = home ? mfl[team].opp : team;
	 var homeScore = home ? mfl[team].score : mfl[team].oppScore;
	 var awayScore = home ? mfl[team].oppScore : mfl[team].score;
	 
	 var tdString = 
		"<TD ateamn=" + awayTeam + " hteamn=" + homeTeam + " align=center bgcolor='#A0A0A0'><SPAN class='normalfont'><nobr>&nbsp;"   //done dkgray

 	 nflStringArray[nflStringArray.length] = tdString +
         displayNflScores(awayTeam, awayScore, homeTeam, homeScore, mfl[team].remaining) + "&nbsp;</nobr><BR><nobr>&nbsp;Final&nbsp;</nobr></SPAN></TD>"; 
	
   }
   
   // display completed games - ties
   
   for (var i = 0, len = resultsMap.ties.length; i < len; i+=2)
   {
	 var team = resultsMap.ties[i];  
	   
	 var opp = mfl[team].opp;
	 var home = (1 == mfl[team].home);
	 var homeTeam = home ? team : mfl[team].opp;
	 var awayTeam = home ? mfl[team].opp : team;
	 var homeScore = home ? mfl[team].score : mfl[team].oppScore;
	 var awayScore = home ? mfl[team].oppScore : mfl[team].score;
	 
	 var tdString = 
		"<TD ateamn=" + awayTeam + " hteamn=" + homeTeam + " align=center bgcolor='#A0A0D0'><SPAN class='normalfont'><nobr>&nbsp;"   //done grayblue
	 
	 nflStringArray[nflStringArray.length] = tdString +
         displayNflScores(awayTeam, awayScore, homeTeam, homeScore, mfl[team].remaining) + "&nbsp;</nobr><BR><nobr>&nbsp;(Tie)&nbsp;</nobr></SPAN></TD>"; 
		 
   }
   
   
   // display uncompleted games
   
   for (team in winners)
   {
      if (winners[team] / reps > 0.5 || (winners[team] / reps == 0.5 && mfl[team].home == 1))
      {
         var opp = mfl[team].opp;
         var percentage = Math.round(100 * winners[team] / reps);
         var home = (1 == mfl[team].home);
         var homeTeam = home ? team : mfl[team].opp;
         var awayTeam = home ? mfl[team].opp : team;
         var homeScore = home ? mfl[team].score : mfl[team].oppScore;
         var awayScore = home ? mfl[team].oppScore : mfl[team].score;
         
         var tdString = mfl[team].remaining >= 3600 || mfl[team].remaining <= 0 ?
            "<TD ateamn=" + awayTeam + " hteamn=" + homeTeam + " align=center bgcolor='#E0E0E0'><SPAN class='normalfont'><nobr>&nbsp;" :          //not in progress gray
            "<TD ateamn=" + awayTeam + " hteamn=" + homeTeam + " align=center bgcolor='#FFFFEE'><SPAN class='normalfont'><nobr>&nbsp;";           //in progress yellow
         
         nflStringArray[nflStringArray.length] = tdString +
         displayNflScores(awayTeam, awayScore, homeTeam, homeScore, mfl[team].remaining) + "&nbsp;</nobr><BR><nobr>&nbsp;" +
         displayTimeRemaining(mfl[team].remaining, awayScore, homeScore) + 
         team + " " + percentage + "%&nbsp;</nobr></SPAN></TD>"; 
		 
		 
      }    
   }
   
   //nflStringArray.sort();
   nflStringArray = sortInGridOrder(nflStringArray);
   
   nflString += "<P><TABLE class='thin'><TR>";
   var column = 0;
   var first = true;
   
   for (var i = 0; i < nflStringArray.length; ++i)
   {
      first = false;
      nflString += nflStringArray[i];
      if (column++ % 4 == 3)     // was % 5 == 4
      {
         nflString += "</TR><TR>";
      }
      
   }
   nflString += "</TR></TABLE>";
   
   //Add spaces to the bottom so that the page has a small right margin for mobile
   for (var s = 0; s < 71; s++) {
      nflString += '&nbsp;';
   }
     
   document.getElementById('jsNflProjectionsGoHere').innerHTML = nflString;
}


/*
 * 
 nflStringArray: Array[16]
"<TD ateamn=DAL hteamn=WAS align=center bgcolor='#FFFFEE'><SPAN class='normalfont'><nobr>&nbsp;DAL 44, WAS 17 100%&nbs...
"<TD ateamn=NO hteamn=TB align=center bgcolor='#FFFFEE'><SPAN class='normalfont'><nobr>&nbsp;NO 23, TB 20&nbsp;</nobr>...
"<TD ateamn=STL hteamn=SEA align=center bgcolor='#E0E0E0'><SPAN class='normalfont'><nobr>&nbsp;STL @ SEA&nbsp;</nobr><BR><nobr>&nbsp; SEA 98%&nbsp;</nobr></SPAN></TD>"
*/

function sortInGridOrder(stringArray)
{
   var correctOrderString = getCorrectOrderFromBody();
  
   var newArray = [];
   
   var homeTeamRegex = /hteamn=([A-Z]+) /;
   var awayTeamRegex = /ateamn=([A-Z]+) /;
   
   var arrLen = stringArray.length;
   for (var i = 0; i < arrLen; ++i)
   {
      var str = stringArray[i];
      var hTeam = homeTeamRegex.exec(str)[1];  // group 1
      var aTeam = awayTeamRegex.exec(str)[1];  // group 1
      
      var mainIndex = correctOrderString.indexOf(hTeam);
      if (mainIndex === -1)
         mainIndex = correctOrderString.indexOf(aTeam);
      
      newArray.push(
         { 
           "hTeam" : hTeam,
           "aTeam" : aTeam,
           "str" : stringArray[i],
           "mainIndex" : mainIndex
         }
      )
   }
   
   // The correct order is the mainIndex order; sort by mainIndex
   
   newArray.sort(function(a, b){return a.mainIndex - b.mainIndex});  
   
   var retVal = [];
   for (var j = 0; j < arrLen; ++j)
   {
      retVal.push(newArray[j].str);
   }
   
   return retVal;
}

function getCorrectOrderFromBody()
{
   var startIndex = document.body.innerHTML.indexOf("---&nbsp;");
   var endIndex = document.body.innerHTML.indexOf("jsResultsGoHere");

   var correctOrderString = document.body.innerHTML.substring(startIndex, endIndex);
   return correctOrderString;
}


function alertInfo()
{
   alert("Winning percentage projections:\n\nThe teams and values to the right of each percentage projection are an indicator of the teams that the " +
         'player needs to win in order to win this week of NNFP. The number that ' +
         "appears is the difference in percentage, above the team's actual win percentage, " +
         "in a player's total win set. The higher the number, the better " +
         "the win would be for the player.\n\n" +
         'For example, if GB is playing MIN and if "GB+30" appears, ' +
         "and GB's current win percentage is 50%, that means GB wins in 80% of that " +
         "player's possible NNFP victories. Thus it would be preferable to that player " +
         "that GB win and not MIN.\n\n" +
         'In place of a number value:\n' +
         '  "$$" indicates a win will clinch the week.\n' +
         '  "MW" indicates a must-win game to avoid elimination.');
         
}

function displayProjections(projections)
{
   var projectionString = '<P>';
   /*
   var projectionString = 
      "<P><NOBR>Projections are based on point spreads and current game scores. " +
         //"<input type='button' value='Info' onClick='alertInfo();'>" + 
         '<a href="javascript:alertInfo();">' +
         '<img class="projection" height=12 width=12 alt="info" src="img/info.png" />' +
         "</a></NOBR><BR>";
   */      
   var reps = getReps();
   
   $.map(projections.playerWinsArray, function(item) {
      var player = getOnlyKey(item);
      var wins = getOnlyVal(item);
      var percentage = Math.round(100 * wins / reps);
      
      var proj = player + " wins <B>" + percentage + "%</B> ";

      while (proj.length < 24)
      {
         proj += "#";
      }
      proj = replaceAll(proj, "#", "&nbsp;");
      proj += "<span class='bigfont'>&nbsp;</span>";
      
      projectionString += "<BR><NOBR>" + "<SPAN class='projection' id = '" + player + "proj'>" 
                          + proj + "</SPAN>";
      
   });

   projectionString += "</P>";
   
   document.getElementById('jsProjectionsGoHere').innerHTML = projectionString;
}

function getProjections(resultsMap, playerScoreArrayIn, grid, odds, mfl, unknownArray)
{
   var overallWinners = {};
   var overallLosers = {};
   var playerProjectedWinsArray = [];    //[{"SEAN": 0}, {"RAJIV" : 0}]
      
   var playerScoreArray = {};
   jQuery.extend(playerScoreArray, playerScoreArrayIn);   //defensive copy of playerScoreArray
   
   var gridKeys = $.map(grid, function(v,k) {return k;});   
     
   $.map(gridKeys, function(player) {  
      var pair = {};
      pair[player] = 0;
      playerProjectedWinsArray[playerProjectedWinsArray.length] = pair;
   });   
      
   var reps = getReps();

   var retVal = {};
   
   for (var i = 0; i < reps; ++i)
   { 
      var projection =
         getProjection(odds, mfl, unknownArray, overallWinners, overallLosers);
         
      var array = 
         getNnfpWinner(grid, resultsMap.winners, resultsMap.losers, resultsMap.ties, projection.projectedLosers);  
      
      var projectedNnfpWinner = getOnlyKey(array[0]);      
      incrementMapInArray(projectedNnfpWinner, playerProjectedWinsArray);

      /*  retval = {  playerWinsArray
                      overallNflWinners
                      overallNflLosers
                      CHAD = {GB: 10, DET: 12}...
                      SEAN = {GB: 21: DET: 15}...
                        }  */
      
      if (retVal[projectedNnfpWinner] == undefined)
      {
         retVal[projectedNnfpWinner] = {};
      }

      $.map(projection.projectedWinners, function(winningTeam) {
         mapCounter(retVal[projectedNnfpWinner], winningTeam);
         });
   }
     
   shuffle(playerProjectedWinsArray);  // must do this before calling sortByValFunctionRandom
   playerProjectedWinsArray.sort(sortByValFunctionRandom);
   playerProjectedWinsArray.reverse();
   
   retVal.playerWinsArray = playerProjectedWinsArray;
   retVal.overallNflWinners = overallWinners;
   retVal.overallNflLosers = overallLosers;

   return retVal;
   
}

function incrementMapInArray(key, array)  //[{"SEAN": 0}, {"RAJIV" : 0}]
{
   $.map(array, function(item) {
      if (getOnlyKey(item) == key)
      {
         item[key] = item[key] + 1;
      }
   });
}


function displayCurrentSums(playerScoreArray, unknownTeams)
{

   var nameLine = jQuery(".matchup").first().html();
   var lastLine = jQuery(".matchup").last().html();
   
   var nameArray = nameLine.split("&nbsp;")
   var nameOnly = [];
   $.map(nameArray, function(item) {
      if (item != "")
         nameOnly[nameOnly.length] = item;
   });

   var scoreLine = "<BR><B>";

   for (var j = 0; j < nameOnly.length; ++j)   
   {
      var arrayItem = fetchKeyInArray(playerScoreArray, nameOnly[j]);
      
      scoreLine += "&nbsp;&nbsp;" + make4Front("" + getOnlyVal(arrayItem)) + "&nbsp;&nbsp;";
   }
   
   scoreLine += "</B><span style='font-size:18'>&nbsp;</span>";
   
   jQuery(".matchup").last().html(lastLine + scoreLine);
}

/**
 * Old vertical way of doing scores, not used 
 */

function displayCurrentScores(playerScoreArray, unknownTeams)
{
   var resultsString = "<P><NOBR><SPAN class='normalfont'>";

   if (unknownTeams.length == 0)
      resultsString += "Results complete.";
   else if (unknownTeams.length == 2) {
      resultsString += 'One game not yet determined. ';
      resultsString += '&nbsp;Auto-refresh: <input type="checkbox" onclick="toggleAutoRefresh(this);" id="reloadCB">';
   }
   else {
      resultsString += (unknownTeams.length / 2) + ' games not yet determined. ';
      resultsString += '&nbsp;Auto-refresh: <input type="checkbox" onclick="toggleAutoRefresh(this);" id="reloadCB">';
   }
     
   if (true)                         //always display     
   //if (unknownTeams.length == 0)   //only display this when finished
   {
      resultsString += "</SPAN></NOBR><BR><BR>";   
      $.map(playerScoreArray, function(playerScorePair) {
         resultsString += "<NOBR><SPAN CLASS='normalfont'>" + getOnlyKey(playerScorePair) + ": <B>" + getOnlyVal(playerScorePair) + "</B></SPAN><span class='bigfont'>&nbsp;</span></NOBR><BR>";
      });
   }
   
   resultsString += "</P>",
   
   document.getElementById('jsResultsGoHere').innerHTML = resultsString;
}

function displayCurrentScoresHorizontal(playerScoreArray, unknownTeams)
{
   var resultsString = "<P><NOBR><SPAN class='normalfont'>";

   if (unknownTeams.length == 0)
      resultsString += "Results complete.";
   else if (unknownTeams.length == 2) {
      resultsString += 'One game not yet determined. ';
      resultsString += '<a href="javascript:alertInfo();"><img class="projection" height=12 width=12 alt="info" src="img/info.png" /></a>&nbsp;';
      resultsString += '&nbsp;Auto-refresh: <input type="checkbox" onclick="toggleAutoRefresh(this);" id="reloadCB">';
   }
   else {
      resultsString += (unknownTeams.length / 2) + ' games not yet determined. ';
      resultsString += '<a href="javascript:alertInfo();"><img class="projection" height=12 width=12 alt="info" src="img/info.png" /></a>&nbsp;';
      resultsString += '&nbsp;Auto-refresh: <input type="checkbox" onclick="toggleAutoRefresh(this);" id="reloadCB">';
   }
   
   resultsString += "<P><TABLE class='result'><TR>";
   
   var winnerScore = -1;
   if (unknownTeams.length === 0) {  // games are done, find lowest (best) score
      var winner = playerScoreArray.reduce(function (lowestPair, pair) {
         return (getOnlyVal(lowestPair) || 9999) < getOnlyVal(pair) ? lowestPair : pair;}, {});
      winnerScore = getOnlyVal(winner);   
   }
   
   $.map(playerScoreArray, function(playerScorePair) {
      if (getOnlyVal(playerScorePair) === winnerScore)
         resultsString += '<TD BGCOLOR="#AAFFAA" ';
      else 
         resultsString += '<TD BGCOLOR=WHITE ';
       
      resultsString += "ALIGN=CENTER><SPAN CLASS='normalfont'>" + getOnlyKey(playerScorePair) + "<BR><B>" + getOnlyVal(playerScorePair) + "</B></SPAN></TD>"    
   });
   
   resultsString += "</TR></TABLE>";
   
   resultsString += "</P>",
   
   document.getElementById('jsResultsGoHere').innerHTML = resultsString;

}


// function in: unknown, grid, returns array of random losers
function getProjection(odds, mfl, unknown, overallWinnerCountMap, overallLoserCountMap)
{
   var projectedLosers = [];
   var projectedWinners= [];

   var i = 0;
   while(unknown.length > projectedLosers.length + projectedWinners.length)
   {
      var team = unknown[i];
      if (!arrayContains(team, projectedLosers) &&
         (!arrayContains(team, projectedWinners)))
      {
         var opp = mfl[team].opp;
         var teamScore = mfl[team].score;
         var oppScore = mfl[team].oppScore;
         var remaining = mfl[team].remaining;
         var spread = odds[team]

         if (spread === undefined)  //this happens when the odds are missing...assume 50/50 then
         {
            spread = 0;
         }
         
         if (remaining == 0)     //fix a bug where remaining is erroneously 0.
         {                       //if remaining is really, we should not be in here.
            if (teamScore > 0)
               remaining = 900;  //OT
            else
               remaining = 3600;
         }

         
         // game = 3600 seconds -  The more negative spread is, the better your odds
         
         var randomSpread = Math.nextGaussian() * 13.86;
         var adjustedGametimeSpread = (spread - randomSpread) * (remaining / 3600);
         
         if (remaining < 3600 && remaining > 0)   // if game is in progress at all
         {
            // For games in progress, adjust Gametime spread by a random number between -11 and 11
            // This spread should start at 0 but end with the (-11..11) spread   (23 - 11)
            
            adjustedGametimeSpread += 
               ((3600 - remaining) / 3600) * Math.floor(Math.random() * 23 - 11);
               
            // Also adjust spread by a random number that starts at 0 and ends in 0,
            // But peaks at halftime (at 28, +/- four touchdowns).  This will help
            // prevent teams from being called 100% at halftime (unless really far ahead).
            // Adjust the 28 - 14 numbers (the random multiplier) to be higher to make everything more even.
            // The first number, 28, is the max score to increase at halftime.  Subtracting 14 makes it even for both teams.            
            
            adjustedGametimeSpread += (Math.random() * 28 - 14) *       
               (1 - Math.cos(2 * Math.PI * (3600 - remaining) / 3600));            
               
         }
         
         
         var teamIsWinner = teamScore > oppScore + adjustedGametimeSpread;
         
         if (teamIsWinner)
         {
            projectedWinners[projectedWinners.length] = team;
            projectedLosers[projectedLosers.length] = opp;
            mapCounter(overallWinnerCountMap, team);
            mapCounter(overallLoserCountMap, opp);
         }
         else
         {
            projectedWinners[projectedWinners.length] = opp;
            projectedLosers[projectedLosers.length] = team;
            mapCounter(overallWinnerCountMap, opp);
            mapCounter(overallLoserCountMap, team);
         }
      }
      ++i;
   } 
     
   var retVal = {};
   retVal.projectedWinners = projectedWinners;   
   retVal.projectedLosers = projectedLosers;
     
   return retVal;
}


// function in: winners, losers, ties, projection:  returns Array containing Pairs of scores for everyone
// if projection is empty, then it returns the real nnfpwinner.

function getNnfpWinner(grid, winners, losers, ties, projectedLosers)
{
   var playerScoreArray = [];

   var gridKeys = $.map(grid, function(v,k) {return k;});   
     
   $.map(gridKeys, function(player) {  
   //$.map(Object.keys(grid), function(player) {
   //Object.keys(grid).map(function(player) {
      var pair = {};
      pair[player] = 0.0;
      playerScoreArray[playerScoreArray.length] = pair;
      
      for (team in grid[player])
      {
         if (arrayContains(team, losers))   ////this should be (team, losers)
         {
            pair[player] += parseInt(grid[player][team]);
         }
         else if (arrayContains(team, ties))
         {
            pair[player] += parseInt(grid[player][team]) / 2;
         }
         
         // include projection
         if (arrayContains(team, projectedLosers))
         {
            pair[player] += parseInt(grid[player][team]);
         }
      }
      
   });

   //to get nnfpWinner = getOnlyKey(playerScoreArray[0]);
   
   shuffle(playerScoreArray);  // must do this before calling sortByValFunctionRandom
   playerScoreArray.sort(sortByValFunctionRandom);
   return playerScoreArray;
}


function colorTheDocumentGrid(resultsMap, array, color)
{
   $.map(resultsMap[array], function(teamName) {
    
      $(".matchup").html(function(index, currentContent) {
            return replaceAll(currentContent, teamName + "&nbsp;", 
                              '<B><FONT COLOR=' + color + '>' + teamName + '&nbsp;</FONT></B>')
         })
      })
}

function getReps()
{
   var reps = getUrlParameter('reps');
   reps = parseInt(reps);
   if (reps == undefined || isNaN(reps))
   {
      reps = 10000;
   }
   if (reps < 10 || reps > 1000000)
   {
      reps = 10000;
   }
   return reps
}

// function in: mfl, returns map of four arrays: of winners, losers, ties, unknown1, unknown2
function getCurrentNflResults(mfl)
{
   var retMap = {"winners": [],
                 "losers": [],
                 "ties": [],
                 "unknown" : []}
   
   for (team in mfl)
   {
      if (mfl[team].remaining == 0)
      {
         if (mfl[team].score == 0 && mfl[team].oppScore == 0)
         {
            retMap.unknown[retMap.unknown.length] = team   
         }
         // this fixes the bug at beginning of games where score is 0,0 but remaining is also 0
         
         else if (mfl[team].score > mfl[team].oppScore)
         {
            retMap.winners[retMap.winners.length] = team;
         }        
         else if (mfl[team].score < mfl[team].oppScore)
         {
            retMap.losers[retMap.losers.length] = team;
         }   
         else 
         {
            retMap.ties[retMap.ties.length] = team;
         }          

      }
      else 
      {
         retMap.unknown[retMap.unknown.length] = team       
      }
   }
                
   return retMap;                
}


function getUrlParameter(sParam)
{
    var sPageURL = window.location.search.substring(1);
    var sURLVariables = sPageURL.split('&');
    for (var i = 0; i < sURLVariables.length; i++) 
    {
        var sParameterName = sURLVariables[i].split('=');
        if (sParameterName[0] == sParam) 
        {
            return sParameterName[1];
        }
    }
}


function homePercentage(homePointSpread)
{
   var TRIALS = 5000;   //1500000
   var winCount = 0; 

   for (var i = 0; i < TRIALS; ++i)
   {
      var a = Math.nextGaussian() * 13.86 + homePointSpread
      if (a < 0.0)
         ++winCount      
   }

   return Math.round(100 * winCount / TRIALS)

}

