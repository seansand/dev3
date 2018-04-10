
$(document).ready(function() { 

   initialize();
   newGame(6);       // default level 6, hardest
   
}) 

var SQUARE_SIZE = 50;
var IMG_SIZE = SQUARE_SIZE - 1;
var LIZZIE_ID;
var COMPUTER_ID;

var COMPUTER_WIN = 1000;
var LIZZIE_WIN = -1000;
var TIE = 0.5;
var POS_INF = 999999;
var NEG_INF = -999999;
var INIT_PLY;
var LEVEL;

var MINIMUM_PAUSE_MILLIS = 500;

var THINKING = "I'm thinking...";
var LIZZIES_TURN = "Lizzie's turn!";

var currentMessage;

var winList = [];
var globalBoard;
var gameFinished;

var randomFirstMove; 

function alphabeta(player, board, alpha, beta, ply) {

   var retVal = {};
   /*
      {
         heur: -23
         move: 3
      }
   */

   var result = checkForResult(board, false);
 
   if (heuristicShowsTie(result.heur) ||
       heuristicShowsLizzieWin(result.heur) ||
       heuristicShowsComputerWin(result.heur) ||
       ply === 0)
   {
      retVal.heur = result.heur;
      return retVal;
   }

   var score;
   var children = getLegalMoves(board);  
            
   if (player === 'C') {
         
      $.each(children, $.proxy(function(index, legalMove) {

         makeMove(board, legalMove, 'C');
         score = alphabeta('L', board, alpha, beta, ply-1).heur;
         takeBackMove(board, legalMove);
         if (score > alpha) {
            alpha = score;
            retVal.move = legalMove;
         }
         if (alpha >= beta) {
            retVal.heur = alpha;
            retVal.move = legalMove;
            return false;  //break;
         }
      }, this));
      
      retVal.heur = alpha;
      return retVal;
   }
   else {  // player = 'L' 
   
      $.each(children, $.proxy(function(index, legalMove) {
      
         makeMove(board, legalMove, 'L')
         score = alphabeta('C', board, alpha, beta, ply-1).heur;
         takeBackMove(board, legalMove);
         if (score < beta) {
            beta = score;
            retVal.move = legalMove;
         }
         if (alpha >= beta) {
            retVal = beta;
            retVal.move = legalMove;
            return false;  //break
         }
      }, this));
      
      retVal.heur = beta;
      return retVal;
   }
}

/*  alphabeta algorithim: 
 
    if(max's turn)
        for each child   
            score = alpha-beta(other player,child,alpha,beta)
            if score > alpha then alpha = score (we have found a better best move)
            if alpha >= beta then return alpha (cut off)
        return alpha (this is our best move) //
    else (min's turn)
        for each child
            score = alpha-beta(other player,child,alpha,beta)
            if score < beta then beta = score (opponent has found a better worse move)
            if alpha >= beta then return beta (cut off)
        return beta (this is the opponent's best move)
*/

function computeBestMove(board) {    

  
   // Check for super-obvious moves first (wins, blocks)

   var wins = [];
   var blocks = [];
   var legalMoves = getLegalMoves(board);
   
   $.each(legalMoves, $.proxy(function(index, spot) { 
   
      makeMove(board, spot, 'C');
      var result = checkForResult(board, true);
      if (heuristicShowsComputerWin(result.heur)) {
         wins.push(spot);
      }
      takeBackMove(board, spot);
      
      makeMove(board, spot, 'L');
      result = checkForResult(board, true); 
      if (heuristicShowsLizzieWin(result.heur)) {
         blocks.push(spot);
      }
      takeBackMove(board, spot);
      
      if (wins.length > 0) {
         return false;  //break
      }
   }));
   
   if (wins.length > 0) {
      if (!($('body').attr('style')))
         $('#stats').html(COMPUTER_WIN);
      return wins[0];
   }
   else if (blocks.length > 0) {
      if (!($('body').attr('style')))
         $('#stats').html("Forced");
      return blocks[0];
   }

   // No obvious moves; run alpha beta functions
   
   var alphaBetaResult = alphabeta('C', board, NEG_INF, POS_INF, INIT_PLY);
   
   if (INIT_PLY === LEVEL) {
      INIT_PLY = LEVEL + 1;  //ramp ply up after first moves.
   } 
   else if (INIT_PLY === LEVEL + 1) {
      INIT_PLY = LEVEL + 2;
   }
   
   if ($('body').attr('style')) {    // only do this if there is no style
      $('#stats').html("");
   }
   else {
      $('#stats').html(alphaBetaResult.heur === TIE ? "Tie" : alphaBetaResult.heur);
   }
   
   if (heuristicShowsComputerWin(alphaBetaResult.heur)) {
      LIZZIES_TURN = 'You are DOOMED!';
   }
   else if (heuristicShowsLizzieWin(alphaBetaResult.heur)) {
      LIZZIES_TURN = "I'm worried!";
   }
   
   return alphaBetaResult.move;
}


function drawGlobalBoard() {

   for (i = 0; i < 7; ++i) {
      for (j = 0; j < 6; ++j)  {
         $('#' + i + j).html(htmlForSpot(globalBoard[i][j]));
      }
   }
   $('.square').removeAttr('bgcolor');
  
}


function drawWin(result) {

   $('#' + result.coord0[0] + result.coord0[1]).attr('bgcolor', '#008000');
   $('#' + result.coord1[0] + result.coord1[1]).attr('bgcolor', '#008000');
   $('#' + result.coord2[0] + result.coord2[1]).attr('bgcolor', '#008000');
   $('#' + result.coord3[0] + result.coord3[1]).attr('bgcolor', '#008000');

}


function computerMoves() {   //TODO, currently random

   if (randomFirstMove != null) {
      computerMove = randomFirstMove;
      randomFirstMove = null;
   }
   else {
      computerMove = computeBestMove(globalBoard);
   }
   
   executeMoveProcess(computerMove, 'C');

   if (!gameFinished) {
      updateMessage(LIZZIES_TURN);
   }
}


function clickHandler() {

   if (currentMessage === THINKING || gameFinished) {
      return;  //don't allow user to click while thinking, or if the game is over
   }
   
   var columnClicked = $(this).attr('id').charAt(0);

   if (!isLegal(columnClicked, globalBoard)) {
      updateMessage("Can't move there, column is full.");
   }
   else {
      executeMoveProcess(columnClicked, 'L');
      
      if (!gameFinished) {
      
         updateMessage(THINKING, "pink");

         setTimeout(computerMoves, MINIMUM_PAUSE_MILLIS);
      }
      
   }
   
}

function executeMoveProcess(columnClicked, player) {

   var moveResult = makeMove(globalBoard, columnClicked, player);
   drawGlobalBoard();

   var result = checkForResult(globalBoard, true) 
   if (heuristicShowsTie(result.heur))
   {
      updateMessage('We have a tie!', 'yellow');
      gameFinished = true;
   }
   else if (heuristicShowsComputerWin(result.heur))
   {
      updateMessage('Computer wins!', 'yellow');
      gameFinished = true;
      drawWin(result);
   }
   else if (heuristicShowsLizzieWin(result.heur))
   {
      updateMessage('Lizzie wins!', 'yellow');
      gameFinished = true;
      drawWin(result);
   }
   
   // Last computer move blue   
   if (player === 'C') {
      $('#' + moveResult[0] + moveResult[1]).attr('bgcolor', '#004040');
   }
}

function makeMove(board, spot, playerChar) {
   for (i = 0; i < 6; ++i) {
      if (board[spot][i] === '-') {
         board[spot][i] = playerChar;
         break;
      }
   }
   return [spot, i];
}


function takeBackMove(board, spot) {
   
   for (i = 5; i >= 0; --i) {
      if (board[spot][i] !== '-') {
         board[spot][i] = '-';
         break;
      }
   }
}


function getLegalMoves(board) {
   var retVal = [];
   
   for (i = 0; i < 7; ++i) {
      if (isLegal(i, board)) {
         retVal.push(i);
      }
   }
   return retVal;
}


function isLegal(spot, board) {
   return board[spot][5] === '-';
}


function isTie(board) {
   return !isLegal(0, board) && !isLegal(1, board) && !isLegal(2, board) && !isLegal(3, board) && 
          !isLegal(4, board) && !isLegal(5, board) && !isLegal(6, board);
}


function updateMessage(message, color) {
   currentMessage = message;
   
   if (color) {
      $('#message').html('<P style="color:' + color + '">' + currentMessage + "</P>");   
   }
   else {
      $('#message').html("<P>" + currentMessage + "</P>");
   }
   
}


function initialize() {

   $('TD').attr("align", "center");
   $('#stats').attr("align", "right");
   
   $('.square').attr("width", SQUARE_SIZE);
   $('.square').attr("height", SQUARE_SIZE);
   $('.square').click(clickHandler);  

   LEVEL = 6;
   
   generateWinList();
} 

function newGame(level) {

   gameFinished = false;
   
   $('h1').html("Connect 4 - Level " + level);
   
   $('#stats').html("");
   LIZZIES_TURN = "Lizzie's turn!";
   
   INIT_PLY = level;
   randomFirstMove = Math.floor(Math.random() * 7);
   
   LIZZIE_ID = Math.floor(Math.random() * 6);
   COMPUTER_ID = Math.floor(Math.random() * 6);
   while (COMPUTER_ID === LIZZIE_ID) {
      COMPUTER_ID = Math.floor(Math.random() * 6);
   }

   $('#lizziesquare').html(htmlForSpot('L'));
   $('#computersquare').html(htmlForSpot('C'));
   
   $('.square').removeAttr('bgcolor');

   var column0 = ['-','-','-','-','-','-'];
   var column1 = ['-','-','-','-','-','-'];
   var column2 = ['-','-','-','-','-','-'];
   var column3 = ['-','-','-','-','-','-'];
   var column4 = ['-','-','-','-','-','-'];
   var column5 = ['-','-','-','-','-','-'];
   var column6 = ['-','-','-','-','-','-'];
   
   globalBoard = [column0, column1, column2, column3, column4, column5, column6];
   
   drawGlobalBoard();
   updateMessage('Lizzie goes first. Click a column.');
}


function htmlForSpot(id) {

   if (id === '-') {
      return '<IMG HEIGHT=' + IMG_SIZE + ' WIDTH=' + IMG_SIZE + 
             ' SRC="connect4/images/empty.png"></IMG>';
   }
   else if (id === 'L') {
      return '<IMG HEIGHT=' + IMG_SIZE + ' WIDTH=' + IMG_SIZE + 
             ' SRC="connect4/images/' + LIZZIE_ID + '.png"></IMG>';
   }
   else if (id === 'C') {
      return '<IMG HEIGHT=' + IMG_SIZE + ' WIDTH=' + IMG_SIZE + 
             ' SRC="connect4/images/' + COMPUTER_ID + '.png"></IMG>';
   }
             
}
   
function checkForResult(board, resultOnly) {

   // resultOnly means we are only interested in the W/L/T result and not the heuristic

   /*
      result = { heur: 0,
                 coord0: [3, 0],
                 coord1: [0, 0],
                 coord2: [1, 0],
                 coord3: [2, 0]
               }
   */

   var result = {};
   result.heur = 0;
   
   $.each(winList, $.proxy(function(index, arrayOfPairs) {

      var spot0 = board[arrayOfPairs[0][0]][arrayOfPairs[0][1]];
      var spot1 = board[arrayOfPairs[1][0]][arrayOfPairs[1][1]];
      var spot2 = board[arrayOfPairs[2][0]][arrayOfPairs[2][1]];
      var spot3 = board[arrayOfPairs[3][0]][arrayOfPairs[3][1]];
      
      if (spot0 !== '-' && 
          spot0 === spot1 &&
          spot0 === spot2 &&
          spot0 === spot3) {
          result.heur += (spot0 === 'L' ? LIZZIE_WIN : COMPUTER_WIN);
          result.coord0 = [arrayOfPairs[0][0], arrayOfPairs[0][1]];
          result.coord1 = [arrayOfPairs[1][0], arrayOfPairs[1][1]];
          result.coord2 = [arrayOfPairs[2][0], arrayOfPairs[2][1]];
          result.coord3 = [arrayOfPairs[3][0], arrayOfPairs[3][1]];
          return false;  // false breaks out of $.each
      }
      else if (!resultOnly &&
               (spot0 === '-' || spot0 === 'L') &&
               (spot1 === '-' || spot1 === 'L') &&
               (spot2 === '-' || spot2 === 'L') &&
               (spot3 === '-' || spot3 === 'L'))
      {
         --result.heur;    // Lizzie only row, negative one
      }      
      else if (!resultOnly &&
               (spot0 === '-' || spot0 === 'C') &&
               (spot1 === '-' || spot1 === 'C') &&
               (spot2 === '-' || spot2 === 'C') &&
               (spot3 === '-' || spot3 === 'C'))
      {
         ++result.heur;    // Computer only row, positive one
      }      
      
   }, this));

   if (!heuristicShowsLizzieWin(result.heur) &&
       !heuristicShowsComputerWin(result.heur) &&
       isTie(board)) {
      result.heur = TIE;
   }
   
   return result;
}


function heuristicShowsLizzieWin(heur) {
   return heur - 100 <= LIZZIE_WIN;
}

function heuristicShowsComputerWin(heur) {
   return heur + 100 >= COMPUTER_WIN;
}

function heuristicShowsTie(heur) {
   return heur === TIE;
}


function pushToWinList(arrayObj) {

   var okay = true;

   $.each(arrayObj, function(index, pair) {
      if (pair[0] < 0 || pair[1] < 0 ||
          pair[0] > 6 || pair[1] > 5) {
         okay = false;
      }
   });
      
   if (okay) {
      winList.push(arrayObj);
   }
}

function generateWinList() {

   for (i = 0; i < 7; ++i) {
      for (j = 0; j < 6; ++j)  {
         //vertical up
         pushToWinList([[i, j],
                       [i, j+1],
                       [i, j+2],
                       [i, j+3]]);
         // horiz right
         pushToWinList([[i, j],
                       [i+1, j],
                       [i+2, j],
                       [i+3, j]]);
         // diag nw
         pushToWinList([[i, j],
                       [i-1, j-1],
                       [i-2, j-2],
                       [i-3, j-3]]);
         // diag ne
         pushToWinList([[i, j],
                       [i+1, j-1],
                       [i+2, j-2],
                       [i+3, j-3]]);
      }
   }

   if (winList.length != 21 + 24 + 24) {
      alert("Error, winList is not of correct length " + winList.length);
   }
   
}

function changeLizzie() {
   var rememberId = LIZZIE_ID;
   LIZZIE_ID = Math.floor(Math.random() * 6);
   while (COMPUTER_ID === LIZZIE_ID ||
          rememberId === LIZZIE_ID) {
      LIZZIE_ID = Math.floor(Math.random() * 6);
   }

   $('#lizziesquare').html(htmlForSpot('L'));
   drawGlobalBoard();
}

function changeComputer() {
   var rememberId = COMPUTER_ID;
   COMPUTER_ID = Math.floor(Math.random() * 6);
   while (COMPUTER_ID === LIZZIE_ID ||
          rememberId === COMPUTER_ID) {
      COMPUTER_ID = Math.floor(Math.random() * 6);
   }
   
   $('#computersquare').html(htmlForSpot('C'));
   drawGlobalBoard();
}

function toggleBackground() {
   
   if ($('body').attr('style')) {
      $('body').removeAttr('style');
   }
   else {
      $('body').attr('style', 'background-image:url(connect4/images/wallpaper.png)');
   }
}
