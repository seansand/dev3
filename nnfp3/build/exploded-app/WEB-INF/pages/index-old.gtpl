<% include '/WEB-INF/includes/header.gtpl' %>


<h1>NNFP latest results</h1>

<!--
<p>
    Congratulations, you've just created your first 
    <a href="http://gaelyk.appspot.com">Gaelyk</a> application.
</p>

<p>
    Click <a href="datetime.groovy">here</a> to view the current date/time.
</p>
-->

<!-- get picks and display them here -->

<p>
   <%
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
				 
				 
	%>
</p>





<% include '/WEB-INF/includes/footer.gtpl' %>

