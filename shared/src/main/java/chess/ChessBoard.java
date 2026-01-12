package chess;
import javax.swing.*;
import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard
{
    ChessPiece[][] board;

    public ChessBoard()
    {
        board = new ChessPiece[8][8];
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece)
    {
        board[position.getRow()-1][position.getColumn()-1] = piece;
    }

    public boolean inBounds(ChessPosition position)
    {
        int row = position.getRow();
        int col = position.getColumn();
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position)
    {
        return board[position.getRow()-1][position.getColumn()-1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard()
    {
        ChessPiece.PieceType[] backRowOrder = {
                ChessPiece.PieceType.ROOK,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.KING,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.ROOK
        };

        ChessGame.TeamColor team = ChessGame.TeamColor.WHITE;

        for (int i = 0; i < 2; i++)
        {
            int row = (team == ChessGame.TeamColor.WHITE) ? 1 : 8;
            int pawnRow = (team == ChessGame.TeamColor.WHITE) ? 2 : 7;

            for (int col = 1; col <= 8; col++)
            {
                ChessPosition pos = new ChessPosition(pawnRow, col);
                addPiece(pos, new ChessPiece(team, ChessPiece.PieceType.PAWN));
            }

            for (int col = 1; col <= 8; col++)
            {
                ChessPosition pos = new ChessPosition(row, col);
                addPiece(pos, new ChessPiece(team, backRowOrder[col - 1]));
            }

            team = ChessGame.TeamColor.BLACK;
        }
    }

    public String toString()
    {
        String str = "";
        for(int i = 0; i < board.length; i++)
        {
            for(int j = 0; j < board[i].length; j++)
            {
                if(board[i][j] == null)
                {
                    str += ".";
                }
                else {
                    str += board[i][j].getLetter();
                    str += " ";
                }
            }
            str += "\n";
        }
        return str;
    }

    @Override
    public int hashCode()
    {
        return Arrays.deepHashCode(board);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(board, that.board);
    }
}
