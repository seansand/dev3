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
   
   function renumber()
   {
      ryanLines = [];
      seanLines = [];
      newSeanLines = [];
   
      try
      {
         readLines();

         for (var i = 0; i < seanLines.length; ++i) {
            const numberStr = (i < 9) ? (' ' + (i+1)) : ('' + (i+1));
            newSeanLines.push(seanLines[i].substring(0, 4) + numberStr + seanLines[i].substring(6));
         }
         
         redrawTextArea();
         validationOk("Renumbered.");
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
         
         if (truncatedSeanLines.length != ryanLines.length) {
            fail = true;
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
   <H4>NNFP Spreads (Week <% print request.week%>)</H4>
   <P>E-mail order, then spread lines, separated by a blank line.</P>
   <P>
   <button onclick="renumber()" type="button">Renumber</button>   
   <button onclick="rearrange()" type="button">Rearrange</button> 
   <button onclick="verify()" type="button">Verify</button>
   <button onclick="truncate()" type="button">Truncate</button> 
   </P>
   <TABLE BORDER=0>
     <TR><TD>
       <textarea id="text" autofocus="autofocus" cols=50 rows=34><%request.lines.each(){println it}%></textarea><br>
       <p id="validationText"></P>
       <TD WIDTH = 20></TD>
       <TD>
       <TABLE BORDER=0>
         <TR><TD ALIGN=CENTER COLSPAN=7>
         <I>Point spread to actual win/loss percentage</I>
         </TD></TR><TR HEIGHT=10></TR><TR>

         <TD ALIGN=CENTER><B>0.0</B><BR>&nbsp;50.0&nbsp;<BR s=split>&nbsp;50.0&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>0.5</B><BR>&nbsp;51.5&nbsp;<BR s=split>&nbsp;48.5&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>1.0</B><BR>&nbsp;52.7&nbsp;<BR s=split>&nbsp;47.3&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>1.5</B><BR>&nbsp;54.2&nbsp;<BR s=split>&nbsp;45.8&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>2.0</B><BR>&nbsp;55.7&nbsp;<BR s=split>&nbsp;44.3&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>2.5</B><BR>&nbsp;57.2&nbsp;<BR s=split>&nbsp;42.8&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>3.0</B><BR>&nbsp;58.6&nbsp;<BR s=split>&nbsp;41.4</TD end=end>
         </TR><TR></TR><TR></TR><TR>
         <TD ALIGN=CENTER><B>3.5</B><BR>&nbsp;59.9&nbsp;<BR s=split>&nbsp;40.1&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>4.0</B><BR>&nbsp;61.3&nbsp;<BR s=split>&nbsp;38.7&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>4.5</B><BR>&nbsp;62.7&nbsp;<BR s=split>&nbsp;37.3&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>5.0</B><BR>&nbsp;64.0&nbsp;<BR s=split>&nbsp;36.0&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>5.5</B><BR>&nbsp;65.5&nbsp;<BR s=split>&nbsp;34.5&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>6.0</B><BR>&nbsp;66.7&nbsp;<BR s=split>&nbsp;33.3&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>6.5</B><BR>&nbsp;68.1&nbsp;<BR s=split>&nbsp;31.9</TD end=end>
         </TR><TR></TR><TR></TR><TR>
         <TD ALIGN=CENTER><B>7.0</B><BR>&nbsp;69.3&nbsp;<BR s=split>&nbsp;30.7&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>7.5</B><BR>&nbsp;70.7&nbsp;<BR s=split>&nbsp;29.3&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>8.0</B><BR>&nbsp;71.9&nbsp;<BR s=split>&nbsp;28.1&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>8.5</B><BR>&nbsp;73.0&nbsp;<BR s=split>&nbsp;27.0&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>9.0</B><BR>&nbsp;74.1&nbsp;<BR s=split>&nbsp;25.9&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>9.5</B><BR>&nbsp;75.2&nbsp;<BR s=split>&nbsp;24.8&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>10.0</B><BR>&nbsp;76.5&nbsp;<BR s=split>&nbsp;23.5</TD end=end>
         </TR><TR></TR><TR></TR><TR>
         <TD ALIGN=CENTER><B>10.5</B><BR>&nbsp;77.7&nbsp;<BR s=split>&nbsp;22.3&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>11.0</B><BR>&nbsp;78.7&nbsp;<BR s=split>&nbsp;21.3&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>11.5</B><BR>&nbsp;79.7&nbsp;<BR s=split>&nbsp;20.3&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>12.0</B><BR>&nbsp;80.6&nbsp;<BR s=split>&nbsp;19.4&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>12.5</B><BR>&nbsp;81.7&nbsp;<BR s=split>&nbsp;18.3&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>13.0</B><BR>&nbsp;82.6&nbsp;<BR s=split>&nbsp;17.4&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>13.5</B><BR>&nbsp;83.5&nbsp;<BR s=split>&nbsp;16.5</TD end=end>
         </TR><TR></TR><TR></TR><TR>
         <TD ALIGN=CENTER><B>14.0</B><BR>&nbsp;84.4&nbsp;<BR s=split>&nbsp;15.6&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>14.5</B><BR>&nbsp;85.2&nbsp;<BR s=split>&nbsp;14.8&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>15.0</B><BR>&nbsp;86.0&nbsp;<BR s=split>&nbsp;14.0&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>15.5</B><BR>&nbsp;86.8&nbsp;<BR s=split>&nbsp;13.2&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>16.0</B><BR>&nbsp;87.5&nbsp;<BR s=split>&nbsp;12.5&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>16.5</B><BR>&nbsp;88.3&nbsp;<BR s=split>&nbsp;11.7&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>17.0</B><BR>&nbsp;89.0&nbsp;<BR s=split>&nbsp;11.0</TD end=end>
         </TR><TR></TR><TR></TR><TR>
         <TD ALIGN=CENTER><B>17.5</B><BR>&nbsp;89.6&nbsp;<BR s=split>&nbsp;10.4&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>18.0</B><BR>&nbsp;90.3&nbsp;<BR s=split>&nbsp;9.7&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>18.5</B><BR>&nbsp;90.9&nbsp;<BR s=split>&nbsp;9.1&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>19.0</B><BR>&nbsp;91.5&nbsp;<BR s=split>&nbsp;8.5&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>19.5</B><BR>&nbsp;92.1&nbsp;<BR s=split>&nbsp;7.9&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>20.0</B><BR>&nbsp;92.5&nbsp;<BR s=split>&nbsp;7.5&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>20.5</B><BR>&nbsp;93.0&nbsp;<BR s=split>&nbsp;7.0</TD end=end>
         </TR><TR></TR><TR></TR><TR>
         <TD ALIGN=CENTER><B>21.0</B><BR>&nbsp;93.5&nbsp;<BR s=split>&nbsp;6.5&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>21.5</B><BR>&nbsp;93.9&nbsp;<BR s=split>&nbsp;6.1&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>22.0</B><BR>&nbsp;94.4&nbsp;<BR s=split>&nbsp;5.6&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>22.5</B><BR>&nbsp;94.8&nbsp;<BR s=split>&nbsp;5.2&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>23.0</B><BR>&nbsp;95.1&nbsp;<BR s=split>&nbsp;4.9&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>23.5</B><BR>&nbsp;95.5&nbsp;<BR s=split>&nbsp;4.5&nbsp;</TD end=end>
         <TD ALIGN=CENTER><B>24.0</B><BR>&nbsp;95.9&nbsp;<BR s=split>&nbsp;4.1</TD end=end>
         </TR>
         </TABLE>
       
       </TD>
     </TD></TR>
   </TABLE>
</BODY>
</HTML>