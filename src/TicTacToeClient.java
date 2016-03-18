import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

/* 
 * 1. Enter name
 * 2. Enter number corresponding to the spot on board, or surrender, or exit
 * 3. ???
 * 4. profit
 */
 
public class TicTacToeClient {
	public static void main(String[] args) throws Exception {
		Socket socket = new Socket("45.50.5.238", 38007);
		ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		Scanner kb = new Scanner(System.in);

		System.out.println("Player 1 name: ");
		ConnectMessage c = new ConnectMessage(kb.nextLine());

		out.writeObject(c);
		out.writeObject(new CommandMessage(CommandMessage.Command.NEW_GAME));

		start(out, in);
	}

	private static void start(ObjectOutputStream out, ObjectInputStream in) throws Exception {
		Scanner kb = new Scanner(System.in);
		BoardMessage board = (BoardMessage) in.readObject();
		displayBoard(board);

		for (int i = 0; i < 5; i++) {
			System.out.println("Enter a number 0 - 8, -1 to exit, or -2 to surrender ");
			System.out.println("[0][1][2]");
			System.out.println("[3][4][5]");
			System.out.println("[6][7][8]");
			
			int num = kb.nextInt();
			int row = num / 3;
			int col = num % 3;

			if (num == -1) {
				out.writeObject(new CommandMessage(CommandMessage.Command.EXIT));
				System.out.println("Exited the game");
				return;
			}
			if (num == -2) {
				out.writeObject(new CommandMessage(CommandMessage.Command.SURRENDER));
				System.out.println("Player 1 surrendered.");
				return;
			}
			while (row < 0 || col < 0 || board.getBoard()[row][col] != 0) {
				System.out.println("Invalid Spot");
				System.out.println("Enter a number 0 - 8, -1 to exit, or -2 to surrender ");
				num = kb.nextInt();
				row = num / 3;
				col = num % 3;
				if (num == -1) {
					out.writeObject(new CommandMessage(CommandMessage.Command.EXIT));
					System.out.println("Exited the game");
					return;
				}
				if (num == -2) {
					out.writeObject(new CommandMessage(CommandMessage.Command.SURRENDER));
					System.out.println("Player 1 surrendered");
					return;
				}
			}

			MoveMessage m = new MoveMessage((byte) row, (byte) col);
			out.writeObject(m);

			board = (BoardMessage) in.readObject();
			displayBoard(board);

			if (board.getStatus() == BoardMessage.Status.PLAYER1_VICTORY 
				|| board.getStatus() == BoardMessage.Status.PLAYER2_SURRENDER) {
				System.out.println("Player 1 wins! ");
				return;
			}
			else if (board.getStatus() == BoardMessage.Status.PLAYER2_VICTORY
					|| board.getStatus() == BoardMessage.Status.PLAYER1_SURRENDER) {
				System.out.println("Player 2 wins! ");
				return;
			}
			else if (board.getStatus() == BoardMessage.Status.STALEMATE){
				System.out.println("Stalemate! ");
				return;
			}
		}
	}
	
	private static void displayBoard(BoardMessage board) {
		byte[][] b = board.getBoard();

		System.out.println("Current Board");
		for (int i = 0; i < b.length; i++) {
			for (int j = 0; j < b.length; j++) {
				char spot = ' ';
				if (b[i][j] == 1) {
					spot = 'X';
				} else if (b[i][j] == 2) {
					spot = 'O';
				}
				System.out.print("[" + spot + "]");
			}
			System.out.println();
		}
	}
}