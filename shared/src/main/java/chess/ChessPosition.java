package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition
{
    private int row;
    private int col;


    public ChessPosition(int row, int col)
    {
        this.row = row;
        this.col = col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow()
    {
        return row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn()
    {
        return col;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(row, col);
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
        ChessPosition that = (ChessPosition) o;
        return row == that.row && col == that.col;
    }


    public String toString()
    {
        return "Postion: (" + row + ", " + col + ")";
    }

    public static ChessPosition fromAlgebraic(String algebraic) {
        if (algebraic == null || algebraic.length() != 2) {
            throw new IllegalArgumentException("Invalid algebraic position: " + algebraic);
        }

        char fileChar = algebraic.charAt(0);
        char rankChar = algebraic.charAt(1);

        int col = fileChar - 'a' + 1;  // 'a' -> 1, 'b' -> 2, ..., 'h' -> 8
        int row = rankChar - '1' + 1;  // '1' -> 1, '2' -> 2, ..., '8' -> 8

        if (col < 1 || col > 8 || row < 1 || row > 8) {
            throw new IllegalArgumentException("Invalid algebraic position: " + algebraic);
        }

        return new ChessPosition(row, col);
    }
}

