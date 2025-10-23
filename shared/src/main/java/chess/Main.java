package chess;

import java.util.Collection;

public class Main
{
    public static void main(String[] args)
    {
        ChessBoard board = new ChessBoard();
        ChessPiece king = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);
        ChessPosition position = new ChessPosition(3, 6);
        board.addPiece(position, king);

        System.out.println(board.toString());
        Collection<ChessMove> moves = king.pieceMoves(board, position);
        ChessMove test = new ChessMove(new ChessPosition(3, 6),  new ChessPosition(4, 5), null);
        System.out.println(test.hashCode());
    }
}
