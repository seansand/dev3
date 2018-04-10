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


int hour = 1000 * 60 * 60;
int day = 24 * hour;
def d = new Date();
int mills = d.getTime();
mills += hour * 80;  //was 66
mills = mills % (hour * 24 * 14);

if (mills > 0 && mills < hour * 72)
   request.content = "Green trash-only week";
else if (mills > (7 * day) && mills < (7 * day) + (hour * 72))
   request.content = "Gold recycling week";
else {
   def randomList = ["Don't forget to be awesome",
                     "Execute order 66",
                     "My life for Aiur"];
   Collections.shuffle(randomList);
   request.content = randomList[0];
}


request.frequency = 120;
request.vibrate = DONT_VIBRATE;

//////////////

forward '/WEB-INF/pages/pebblecards.gtpl'

//////////////

