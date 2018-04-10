var fetchKeyInArray = function(array, key)
{
   for (var i = 0; i < array.length; ++i)
   {
      if (getOnlyKey(array[i]) == key)
         return array[i];
   }
   return null;
}


var mapCounter = function(map, item)
{
   if (map[item] == undefined)
   {
      map[item] = 1;
   }   
   else
   {
      map[item] = 1 + map[item];
   }

}

var make3 = function(str)
{
   if (str.length == 2)
      return str + "&nbsp;";
   else 
      return str;
}

var make6 = function(str)
{
   if (str.length == 5)
      return str + "&nbsp;";
   else 
      return str;
}

var make4Front = function(str)
{ 
   while (str.length < 4)
   {
      str = "_" + str;
   }
   str = replaceAll(str, "_", "&nbsp");
   
   return str;
}

var make2 = function(obj)
{
   var str = "" + obj;
   if (str.length == 1)
      return "&nbsp;" + str;
   else 
      return str;
}

var getOnlyKey = function(object)
{
   for (var prop in object)
   {
       return prop;         
   }
}

var getOnlyVal = function(object)
{
   for (var prop in object)
   {
       return object[prop];         
   }
}

var sortByValFunction = function(a, b)
{
   return (getOnlyVal(a) - getOnlyVal(b));
}

var sortByValFunctionRandom = function(a, b)
{
   if (getOnlyVal(a) == getOnlyVal(b))
   {
      return (Math.random() * 2) - 1;
   }
   return (getOnlyVal(a) - getOnlyVal(b));
}

var sortByKeyFunction = function(a, b)
{
   return (getOnlyKey(a) - getOnlyKey(b));
}


function replaceAll(txt, replace, with_this) {
  return txt.replace(new RegExp(replace, 'g'),with_this);
}

function arrayContains(str, array)
{
   return -1 != $.inArray(str, array);
}

function alertOn(obj, label)
{
   if (label == undefined)
      alert(JSON.stringify(obj));
   else
      alert(label + ": " + JSON.stringify(obj));
}

Math.nextGaussian = function()
{
   var x1, x2, rad;
 
   do 
   {
      x1 = 2 * this.random() - 1;
      x2 = 2 * this.random() - 1;
      rad = x1 * x1 + x2 * x2;
   } while(rad >= 1 || rad == 0);
 
   var c = this.sqrt(-2 * Math.log(rad) / rad);
   return x1 * c;
}

/*
//scoreArray.sort(sortFunction)

document.write("<BR>Sorted:")
for (var i = 0; i < scoreArray.length; ++i)
{
   document.write('<BR>')
   document.write(getOnlyKey(scoreArray[i]) + " : " + getOnlyVal(scoreArray[i]))
}
 */
  
