package chess;

import chess.moveCalculators.MoveCalculator;

import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece
{

    private ChessGame.TeamColor color;
    private ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type)
    {
        this.color = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType
    {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor()
    {
        return this.color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType()
    {
        return this.type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition)
    {
        return MoveCalculator.getMoveCalculator(board, myPosition).returnMoves();
    }

//    public void printMoves(Collection<ChessMove> moves)
//    {
//
//        if (moves.isEmpty())
//        {
//            System.out.println("No available moves.");
//            return;
//        }
//
//        System.out.println("Available moves:");
//
//        for (ChessMove move : moves)
//        {
//            ChessPosition from = move.getStartPosition();
//            ChessPosition to = move.getEndPosition();
//
//
//            int fromCol = from.getColumn();
//            int fromRow = from.getRow();
//            int toCol = to.getColumn();
//            int toRow = to.getRow();
//
//            System.out.println(fromRow + "," + fromCol + " -> " + toRow + "," + toCol + move.hashCode());
//        }
//    }

    @Override
    public int hashCode()
    {
        return 31 * type.hashCode() + color.hashCode();
    }

    public String toString()
    {
        String str = "";
        str += "Color: " + this.color + "\n";
        str += "Type: " + this.type + "\n";
        return str;
    }
    public String getLetter()
    {
        if(type == ChessPiece.PieceType.KING)
        {
            return "K";
        }
        else if(type == ChessPiece.PieceType.QUEEN)
        {
            return "Q";
        }
        else if(type == ChessPiece.PieceType.BISHOP)
        {
            return "B";
        } else if (type == ChessPiece.PieceType.KNIGHT)
        {
            return "N";
        }
        else if(type == ChessPiece.PieceType.ROOK)
        {
            return "R";
        }
        else if(type == ChessPiece.PieceType.PAWN)
        {
            return "P";
        }
        return "";
    }
}
