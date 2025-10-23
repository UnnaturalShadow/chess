package chess;

import chess.movecalculators.MoveCalculator;

import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece
{

    private final ChessGame.TeamColor color;
    private final ChessPiece.PieceType type;

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

    @Override
    public int hashCode()
    {
        return 31 * type.hashCode() + color.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return color == that.color && type == that.type;
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
