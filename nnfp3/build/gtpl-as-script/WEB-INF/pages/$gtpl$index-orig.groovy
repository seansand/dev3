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
<div class=\"hero-unit center\">
    <a href=\"http://gaelyk.appspot.com\"><img alt=\"Gaelyk Logo\" src=\"/images/gaelyk.png\"/></a>
	<br/>
	<p>
	    Congratulations, you've just created your first 
	    <a href=\"http://gaelyk.appspot.com\">Gaelyk</a> application.
	</p>
	
	<p>
	    <a href=\"/datetime\" class=\"btn btn-primary btn-large\">Show current time &raquo;</a>
	</p>
</div>
<div class=\"row\">
  <div class=\"span4\">
    <h2>Start Experimenting</h2>
     <p>This template contains following sample files<ul><li><code>datetime.groovy</code></li><li><code>WEB-INF/pages/datetime.gtpl</code></li></ul>Try to edit them and watch the changes.</p>
  </div>
  <div class=\"span4\">
    <h2>Learn More</h2>
     <p>All <a href=\"http://gaelyk.appspot.com\">Gaelyk</a> features are well documented. If you are new to <a href=\"http://gaelyk.appspot.com\">Gaelyk</a> best place to learn more is the <a href=\"http://gaelyk.appspot.com/tutorial\">Tutorial</a>.</p>
    <p><a class=\"btn\" href=\"http://gaelyk.appspot.com/tutorial\">Read Tutorial &raquo;</a></p>
 </div>
  <div class=\"span4\">
    <h2>Work Less</h2>
    <p>Take advantage of existing plugins. You can for example unleash the power of <a href=\"http://developer.google.com/appengine/\">Google App Engine</a> using <a href=\"https://github.com/musketyr/gpars-appengine\">GPars App Engine</a> integration library</p>
    <p><a class=\"btn\" href=\"http://gaelyk.appspot.com/plugins\">More about plugins &raquo;</a></p>
  </div>
</div>
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
