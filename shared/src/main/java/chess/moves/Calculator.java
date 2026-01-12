package chess.moves;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import chess.ChessBoard;

public class Calculator
{
    ChessPosition pos;
    ChessBoard table;
    ChessPiece toMove;
    ArrayList<ChessMove> validMoves = new ArrayList<>();

    int[][] kingMatrix = {
            {-1,1}, {0,1}, {1,1},
            {-1,0}, {1,0},
            {-1,-1}, {0,-1}, {1,-1}
    };

    int[][] queenMatrix = {
            {-1,1}, {0,1}, {1,1},
            {-1,0}, {1,0},
            {-1,-1}, {0,-1}, {1,-1}
    };

    int[][] rookMatrix = {
            {0,1},
            {-1,0}, {1,0},
            {0,-1}
    };

    int[][] bishopMatrix = {
            {-1, 1}, {1, 1},
            {-1, -1}, {1, -1}
    };

    int[][] knightMatrix = {
            {-1, 2}, {1, 2},
            {-2, 1}, {2, 1},
            {-2, -1}, {2, -1},
            {-1, -2}, {1, -2}
    };

    public Calculator(ChessBoard board, ChessPosition position)
    {
        pos = position;
        table = board;
        toMove = table.getPiece(pos);

        switch (toMove.getPieceType())
        {
            case ChessPiece.PieceType.KING:
                moveFromMods(kingMatrix);
                break;
            case ChessPiece.PieceType.QUEEN:
                unboundedMoveFromMods(queenMatrix);
                break;
            case ChessPiece.PieceType.ROOK:
                unboundedMoveFromMods(rookMatrix);
                break;
            case ChessPiece.PieceType.BISHOP:
                unboundedMoveFromMods(bishopMatrix);
                break;
            case ChessPiece.PieceType.PAWN:
                pawnMoves();
                break;
            case ChessPiece.PieceType.KNIGHT:
                moveFromMods(knightMatrix);
                break;
            default:
                break;
        }
    }

    public void pawnMoves()
    {
        ChessGame.TeamColor team = toMove.getTeamColor();
        int rowMod;
        int toPromote;
        int startedAt;

        if (team == ChessGame.TeamColor.BLACK)
        {
            rowMod = -1;
            toPromote = 1;
            startedAt = 7;
        }
        else
        {
            rowMod = 1;
            toPromote = 8;
            startedAt = 2;
        }

        ChessPosition oneStep = new ChessPosition(pos.getRow() + rowMod, pos.getColumn());
        if (table.inBounds(oneStep) && table.getPiece(oneStep) == null)
        {
            if (oneStep.getRow() == toPromote)
            {
                for (ChessPiece.PieceType promo : List.of(
                        ChessPiece.PieceType.QUEEN,
                        ChessPiece.PieceType.ROOK,
                        ChessPiece.PieceType.BISHOP,
                        ChessPiece.PieceType.KNIGHT))
                {
                    validMoves.add(new ChessMove(pos, oneStep, promo));
                }
            }
            else
            {
                validMoves.add(new ChessMove(pos, oneStep, null));
            }

            if (pos.getRow() == startedAt)
            {
                ChessPosition twoStep = new ChessPosition(pos.getRow() + 2 * rowMod, pos.getColumn());
                if (table.inBounds(twoStep) && table.getPiece(twoStep) == null)
                {
                    validMoves.add(new ChessMove(pos, twoStep, null));
                }
            }
        }

        // Diagonal captures
        for (int dc = -1; dc <= 1; dc += 2)
        {
            ChessPosition attackPos = new ChessPosition(pos.getRow() + rowMod, pos.getColumn() + dc);
            if (!table.inBounds(attackPos))
            {
                continue;
            }

            ChessPiece target = table.getPiece(attackPos);
            if (target != null && target.getTeamColor() != toMove.getTeamColor())
            {
                if (attackPos.getRow() == toPromote)
                {
                    for (ChessPiece.PieceType promo : List.of(
                            ChessPiece.PieceType.QUEEN,
                            ChessPiece.PieceType.ROOK,
                            ChessPiece.PieceType.BISHOP,
                            ChessPiece.PieceType.KNIGHT))
                    {
                        validMoves.add(new ChessMove(pos, attackPos, promo));
                    }
                }
                else
                {
                    validMoves.add(new ChessMove(pos, attackPos, null));
                }
            }
        }
    }



    public void moveFromMods(int[][] mods)
    {
        int row = pos.getRow();
        int col = pos.getColumn();

        for (int[] m : mods)
        {
            int newRow = row + m[0];
            int newCol = col + m[1];

            ChessPosition moveTo = new ChessPosition(newRow, newCol);
            if (table.inBounds(moveTo))
            {
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
        for (int[] dir : mods)
        {
            int newRow = pos.getRow() + dir[0];
            int newCol = pos.getColumn() + dir[1];
            ChessPosition moveTo = new ChessPosition(newRow, newCol);

            while (table.inBounds(moveTo))
            {

                if (!isBlocked(moveTo))
                {
                    validMoves.add(new ChessMove(pos, moveTo, null));
                } else {
                    if (table.getPiece(moveTo).getTeamColor() != toMove.getTeamColor())
                    {
                        validMoves.add(new ChessMove(pos, moveTo, null));
                    }
                    break;
                }

                newRow += dir[0];
                newCol += dir[1];
                moveTo = new ChessPosition(newRow, newCol);
            }
        }
    }

    public boolean isBlocked(ChessPosition toCheck)
    {
        if(table.getPiece(toCheck) != null)
        {
            return true;
        }
        return false;
    }

    public Collection<ChessMove> getMoves()
    {
        return validMoves;
    }

}
