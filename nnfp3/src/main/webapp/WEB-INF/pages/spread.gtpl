<HTML>
<HEAD>
<TITLE>NNFP Spreads</TITLE>
<SCRIPT>

   var doReplaceNames = false;  
   var allLines;
   var ryanLines = [];
   var seanLines = [];
   var newSeanLines = [];
   var teamArray1 = [];
   var teamArray2 = [];
   var allText;
  
   String.prototype.contains = function(it) { return this.indexOf(it) != -1; };
  
   String.prototype.containsADigit = function(it) { return this.contains('0') ||
                                                           this.contains('1') ||
                                                           this.contains('2') ||
                                                           this.contains('3') ||
                                                           this.contains('4') ||
                                                           this.contains('5') ||
                                                           this.contains('6') ||
                                                           this.contains('7') ||
                                                           this.contains('8') ||
                                                           this.contains('9') }
  
   function truncate()
   {
      allText = document.getElementById("text").value;
      allText = allText.toUpperCase();
      allLines = allText.split("\\n");   
   
      allText = "";
   
      for (var i = 0; i < allLines.length; ++i) {
         if (allLines[i].containsADigit()) {
            allLines[i] = allLines[i].substring(0, 6);   //can you go over?
         }
         
         var trimmed = allLines[i].trim();
         
         if (i < 20 || trimmed !== '') {
            allText += trimmed + "\\n";
         }
      }

      document.getElementById("text").value = allText;
   }
  
   function rearrange()
   {
      ryanLines = [];
      seanLines = [];
      newSeanLines = [];
      teamArray1 = [];
      teamArray2 = [];
   
      try
      {
         readLines();
         getRyanOrder();
         sortSeanOrder();
         redrawTextArea();
         validationOk("Rearranged.");
         
      }
      catch (e)
      {
         document.getElementById("validationText").innerHTML = 
            "<FONT COLOR='#FF0000'>Error: " + e + "</FONT>";
      }
   }

   function verify()
   {
      ryanLines = [];
      seanLines = [];
      var truncatedSeanLines = [];
   
      try
      {
         readLines();
         
         for (var i = 0; i < seanLines.length; ++i) {
            truncatedSeanLines.push(parseInt(seanLines[i].substring(3,7).trim()));
         }

         truncatedSeanLines.sort(function compareNumbers(a, b) {
            return a - b;
         });
         
         var fail = false;
         
         for (var i = 0; i < seanLines.length; ++i) {
         
            if (i + 1 !== truncatedSeanLines[i]) {
               fail = true;
            }
         }
         
         if (fail)
            document.getElementById("validationText").innerHTML = 
               "<FONT COLOR='#FF0000'>Number values incorrect.</FONT>";
         else
            validationOk("Values OK.");
         
      }
      catch (e)
      {
         document.getElementById("validationText").innerHTML = 
            "<FONT COLOR='#FF0000'>Error: " + e + "</FONT>";
      }
   }

   
   function redrawTextArea()
   {
       allText = "";
       
       for (var j = 0; j < ryanLines.length; ++j) {
          allText += ryanLines[j] + "\\n";
       }
       allText += "\\n";

       for (var i = 0; i < newSeanLines.length; ++i) {
          allText += newSeanLines[i] + "\\n";
       }

       document.getElementById("text").value = allText;
   
   }
   
   function replaceNames(line)
   {
      if (doReplaceNames) {
      
         line = line.substring(0, 6);
      
         line = line.replace("NE ", "NEP");
         line = line.replace("GB ", "GBP");
         line = line.replace("KC ", "KCC");
         line = line.replace("NO ", "NOS");
         line = line.replace("TB ", "TBB");
         line = line.replace("SF ", "SFO");
      }
      return line;
   }
   
   
   
   function sortSeanOrder()
   {
      var found = false;
   
      for (var i = 0; i < seanLines.length; ++i) {
         
         for (var j = 0; j < ryanLines.length; ++j) {
             
            if (seanLines[i].indexOf(teamArray1[j]) !== -1 ||
                seanLines[i].indexOf(teamArray2[j]) !== -1)
            {
               newSeanLines[j] = replaceNames(seanLines[i]);
               found = true;
               break;
            }
         }
         
         if (!found) {
            throw ("Did not find Ryan's: " + seanLines[i]);         
         }
      }
   }
   
   
   function getRyanOrder()
   {
      for (var i = 0; i < ryanLines.length; ++i) {

         var tokens = ryanLines[i].split(" ");
		 
         tokens = tokens.filter(function(a) {
            return a != ""
         });
		 
		 if (tokens.length < 3)
            throw("Line has less than three tokens: " + ryanLines[i]);
         if (tokens[1] !== "@")
            throw("Did not find @ symbol: " + ryanLines[i]);
         if (tokens[0].length < 2)
            throw("Do not understand: " + tokens[0]);         
         if (tokens[2].length < 2)  
            throw("Do not understand: " + tokens[2]);   

         var useLength = tokens[0].substring(0, 2) === "DE" ||
                         tokens[0].substring(0, 2) === "MI" ||
                         tokens[0].substring(0, 2) === "CA" ||
                         tokens[0].substring(0, 2) === "AR" ||
                         tokens[0].substring(0, 2) === "IN" ||
                         tokens[0].substring(0, 2) === "LA" ||
                         tokens[0].substring(0, 2) === "NY" ? 3 : 2;
                         
         teamArray1[i] = tokens[0].substring(0, useLength);
         useLength = tokens[2].substring(0, 2) === "DE" ||
                     tokens[2].substring(0, 2) === "MI" ||
                     tokens[2].substring(0, 2) === "CA" ||
                     tokens[2].substring(0, 2) === "AR" ||
                     tokens[2].substring(0, 2) === "IN" ||
                     tokens[2].substring(0, 2) === "LA" ||
                     tokens[2].substring(0, 2) === "NY" ? 3 : 2;
         teamArray2[i] = tokens[2].substring(0, useLength);
      }
   }
   
   function readLines()
   {
      allText = document.getElementById("text").value;
      allText = allText.toUpperCase();
      allLines = allText.split("\\n");
      
      var foundSpace = false;
      
      for (var i = 0; i < allLines.length; ++i) {
         allLines[i] = allLines[i].trim();
         
         if (allLines[i] === '') {    
            foundSpace = true;
         }
         else if (foundSpace) {
            seanLines.push(allLines[i]);
         }
         else {
            ryanLines.push(allLines[i]);
         }
      }
      
      
   }
   
   function validationOk(text)
   {       
      document.getElementById("validationText").innerHTML = 
         "<FONT COLOR='#008000'>" + text + "</FONT>";
   }
  
  
  
</SCRIPT>
</HEAD>
<BODY>
   <H4>NNFP Spreads (Week 
   <% print request.week%>)<BR>
   E-mail order first, then a blank line, then the auto-generated spread lines.<P>
   <button onclick="rearrange()" type="button">Rearrange</button> <button onclick="truncate()" type="button">Truncate</button> 
   <button onclick="verify()" type="button">Verify</h4>
   <textarea id="text" autofocus="autofocus" cols=50 rows=34>
NYJ @ NE  (replace
WAS @ GB   these
CLE @ BAL  with
LAR @ ATL  Ryan's
LAC @ PHI  e-mail
MIN @ CHI  order)
CAR @ BUF
TEN @ HOU
MIA @ IND
DAL @ KC
NO  @ TB
DET @ ARI
DEN @ NYG
JAC @ OAK
SF  @ SEA
PIT @ CIN

<%request.lines.each(){println it}%>

</textarea><br>
   <p id="validationText"></P>
</BODY>
</HTML>