//pebblecards.groovy

//See: https://keanulee.com/pebblecards/#api

/* example

{
  "content" : "Hello John,\nWelcome to Cards for Pebble!",
  "refresh_frequency" : 30,   // Optional, in minutes
  "vibrate" : 1               // Optional
}

*/

class Request { public def vibrate; public def frequency; public def content }
Request request = new Request();

int DONT_VIBRATE = 0;
int SHORT_VIBRATE = 1;
int DOUBLE_VIBRATE = 2;
int LONG_VIBRATE = 3;

// St. Paul code = 2487129
URL source = new URL("http://weather.yahooapis.com/forecastrss?w=2487129&random=${System.currentTimeMillis()}");
String urlText = source.getText()

urlText = urlText.replaceAll('yweather:forecast', 'yweatherForecast');

def rssNode = new XmlSlurper().parseText(urlText);

request.content = "";

(1..3).each()
{
   def node = rssNode.channel.item.yweatherForecast[it];
   def day = node.@day
   def text = node.@text
   def high = node.@high
   def low = node.@low
   
   request.content += day.toString().substring(0, 1) + ":";
   request.content += text.toString().substring(0, Math.min(9, text.toString().size())) + ":";
   request.content += "${high.toString()}/${low.toString()}";
   request.content += "\\n";   
}


request.frequency = 120;
request.vibrate = DONT_VIBRATE;

//////////////

println request.content
println request.frequency
println request.vibrate

/*forward '/WEB-INF/pages/pebblecards.gtpl'*/

//////////////

