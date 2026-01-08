package chess.moves;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public class Calculator
{
    ChessPosition pos;
    ChessBoard table;
    ChessPiece toMove;
    ArrayList<ChessMove> validMoves = new ArrayList<>();

    int[][] kingMatrix = {
            {-1,1}, {0,1}, {1,1},
            {-1,0}, {0,0}, {1,0},
            {-1,-1}, {0,-1}, {1,-1}
    };

    int[][] queenMatrix = {
            {-1,1}, {0,1}, {1,1},
            {-1,0}, {0,0}, {1,0},
            {-1,-1}, {0,-1}, {1,-1}
    };

    int[][] rookMatrix = {
            {0,1},
            {-1,0}, {1,0},
            {0,-1}
    };

    public Calculator(ChessBoard board, ChessPosition position)
    {
        pos = position;
        table = board;
        toMove = table.getPiece(pos);

        switch (toMove)
        {
            case ChessPiece.PieceType.KING:
                moveFromMods(kingMatrix);
                break;
            case ChessPiece.PieceType.QUEEN:
                unboundedMoveFromMods(queenMatrix);
                break;
            case ChessPiece.PieceType.ROOK:
                unboundedMoveFromMods(rookMatrix);
            default:
                break;
        }
    }

    public boolean inBounds(int row, int col)
    {
        if(row <= 8 && row >=1)
        {
            if(col <= 8 && col >=1)
            {
                return true;
            }
        }
        return false;
    }

    public void moveFromMods(int[][] mods)
    {
        int row = pos.getRow();
        int col = pos.getColumn();

        for (int[] m : mods)
        {
            int newRow = row + m[0];
            int newCol = col + m[1];

            if (inBounds(newRow, newCol))
            {
                ChessPosition moveTo = new ChessPosition(newRow, newCol);
                ChessPiece targetPiece = table.getPiece(moveTo);

                if (targetPiece == null || targetPiece.getTeamColor() != toMove.getTeamColor())
                {
                    validMoves.add(new ChessMove(pos, moveTo, null));
                }
            }
        }
    }

    public void unboundedMoveFromMods(int[][] mods)
    {
        for (int[] dir : mods) {
            int newRow = pos.getRow() + dir[0];
            int newCol = pos.getColumn() + dir[1];

            ChessPosition moveTo = new ChessPosition(newRow, newCol);
            while (inBounds(newRow, newCol)) {
                if (table.getPiece(moveTo) == null) {
                    validMoves.add(new ChessMove(pos, moveTo, null));
                } else {
                    if (table.getPiece(moveTo).getTeamColor() != toMove.getTeamColor()) {
                        validMoves.add(new ChessMove(pos, moveTo, null));
                    }
                    break;
                }

                newRow += dir[0];
                newCol += dir[1];
            }
        }
    }


    public Collection<ChessMove> getMoves()
    {
        return validMoves;
    }

}
