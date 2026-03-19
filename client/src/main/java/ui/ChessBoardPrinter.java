package ui;

public class ChessBoardPrinter
{
    public void printBoard(boolean blackPerspective)
    {
        String[] cols = {"a","b","c","d","e","f","g","h"};

        if (blackPerspective)
        {
            reverse(cols);
        }

        for (int row = 8; row >= 1; row--)
        {
            int displayRow = blackPerspective ? 9 - row : row;
            System.out.print(displayRow + " ");

            for (int col = 0; col < 8; col++)
            {
                boolean light = (row + col) % 2 == 0;
                System.out.print(light ? "[ ]" : "[#]");
            }

            System.out.println(" " + displayRow);
        }

        System.out.print("  ");
        for (String c : cols)
        {
            System.out.print(" " + c + " ");
        }
        System.out.println();
    }

    private void reverse(String[] arr)
    {
        for (int i = 0; i < arr.length / 2; i++)
        {
            String temp = arr[i];
            arr[i] = arr[arr.length - 1 - i];
            arr[arr.length - 1 - i] = temp;
        }
    }
}