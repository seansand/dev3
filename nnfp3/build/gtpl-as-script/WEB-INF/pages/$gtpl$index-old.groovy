package web_inf.pages;out.print("""""");
/* include#begin \WEB-INF\includes\header.gtpl */
out.print("""<html>
    <head>
        <title>  
         """);
            print(request?.title == null ? "NNFP latest results" : request.title)
         ;
out.print("""
        
        </title>
        
        <meta name=viewport content=\"width=device-width, initial-scale=1\">
        <META HTTP-EQUIV=\"Expires\" CONTENT=\"Tue, 01 Jan 1980 1:00:00 GMT\">
        <META HTTP-EQUIV=\"Pragma\" CONTENT=\"no-cache\"> 

        <link rel=\"shortcut icon\" href=\"/images/gaelyk-small-favicon.png\" type=\"image/png\">
        <link rel=\"icon\" href=\"/images/gaelyk-small-favicon.png\" type=\"image/png\">
        
        <link rel=\"stylesheet\" type=\"text/css\" href=\"/css/main.css\"/>
    

    
    </head>
    
    
    
    <body>

        <div>
""");
/* include#end   \WEB-INF\includes\header.gtpl */
out.print("""


<h1>NNFP latest results</h1>

<!--
<p>
    Congratulations, you've just created your first 
    <a href=\"http://gaelyk.appspot.com\">Gaelyk</a> application.
</p>

<p>
    Click <a href=\"datetime.groovy\">here</a> to view the current date/time.
</p>
-->

<!-- get picks and display them here -->

<p>
   """);
            URL url = new URL('http://home.comcast.net/~seansand/picks.txt')
            def response = url.get()
            assert response.responseCode == 200
            assert response.text.contains('NNFP Week')
				
				response.text.eachLine()
				{
				   String line = it.replaceAll(" ", "&nbsp;")
				   print("$line<BR>")
				}
             
				Integer weekNumber = Nnfp.readPicks(response.text)
				print("<P>Week Number: $weekNumber</P>")
				 
				 
	;
out.print("""
</p>





""");
/* include#begin \WEB-INF\includes\footer.gtpl */
out.print("""        <P></P>
		  <P></P>
        <h3> </h3>
        <TABLE BORDER=0 WIDTH=350><TR><TD WIDTH=155>
        <div>Powered by<BR>
            <A HREF=\"http://gaelyk.appspot.com\"><img src=\"/images/gaelyk.png\" HEIGHT=24 BORDER=0></A>
        </div></TD><TD WIDTH=245><A HREF=\"http://seansand.appspot.com/nnfp.groovy\">New NNFP with live scoring</A></TD></TABLE>
        
    </body>
</html>""");
/* include#end   \WEB-INF\includes\footer.gtpl */
out.print("""

""");
