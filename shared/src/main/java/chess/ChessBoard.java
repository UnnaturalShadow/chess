package chess;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard
{

    // private int maxSize = 8;
    private int maxPieces = 32;
    private int numPieces = 0;
    private ChessPiece[] pieces = new ChessPiece[maxPieces];
    private ChessPosition[] positions = new ChessPosition[maxPieces];

    public ChessBoard()
    {

    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece)
    {
        pieces[numPieces] = piece;
        positions[numPieces] = position;
        numPieces++;
        //throw new RuntimeException("Not implemented");
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
        for(int i = 0; i < numPieces; i++)
        {
            if (positions[i] == position)
            {
                return pieces[i];
            }
        }
        return null;
        //throw new RuntimeException("Not implemented");
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard()
    {
        ChessPiece.PieceType[] backRowOrder =
                {
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
            int row = 0;
            int pawnRow = 1;
            if(team == ChessGame.TeamColor.BLACK)
            {
                row = 7;
                pawnRow = 6;
            }

            for (int col = 0; col < 8; col++)
            {
                addPiece(new ChessPosition(pawnRow, col), new ChessPiece(team, ChessPiece.PieceType.PAWN));
            }

            for (int col = 0; col < backRowOrder.length; col++)
            {
                addPiece(new ChessPosition(row, col), new ChessPiece(team, backRowOrder[col]));
            }
            team = ChessGame.TeamColor.BLACK;
        }
        //throw new RuntimeException("Not implemented");
    }
}