package chess.moves;

import chess.ChessPosition;

public class KingMoves
{
    ChessPosition start;

    //build a 2D array matrix of possible moves.

    int[][] kingMatrix = {
            {-1,1}, {0,1}, {1,1},
            {-1,0}, {0,0}, {1,0},
            {-1,-1}, {0,-1}, {1,-1}
    };




}
