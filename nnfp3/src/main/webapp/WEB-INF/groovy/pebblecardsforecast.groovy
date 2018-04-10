//pebblecards.groovy

//See: https://keanulee.com/pebblecards/#api

/* example

{
  "content" : "Hello John,\nWelcome to Cards for Pebble!",
  "refresh_frequency" : 30,   // Optional, in minutes
  "vibrate" : 1               // Optional
}

*/

int DONT_VIBRATE = 0;
int SHORT_VIBRATE = 1;
int DOUBLE_VIBRATE = 2;
int LONG_VIBRATE = 3;


String urlText = "http://www.rssweather.com/zipcode/55109/rss.php".toURL().getText();


def rssNode = new XmlSlurper().parseText(urlText);

request.content = "";

(0..5).each()
{
   def title = rssNode.channel.item[it].title
   def desc = rssNode.channel.item[it].description
   
   request.content += "* " 
   request.content += title 
   request.content += "\\n"
   request.content += desc
   request.content += "\\n"
}

log.info(request.content)

request.frequency = 120;
request.vibrate = DONT_VIBRATE;

//////////////

forward '/WEB-INF/pages/pebblecards.gtpl'

//////////////

