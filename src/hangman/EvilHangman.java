package hangman;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.SortedSet;

import static java.lang.Integer.parseInt;

public class EvilHangman {
    public static void main(String[] args) throws EmptyDictionaryException, IOException {
        EvilHangmanGame game = new EvilHangmanGame();
        SortedSet<Character> guessedLetters = game.getGuessedLetters();

        File dictionary = new File(args[0]);
        int wordLength = parseInt(args[1]);
        int guesses = parseInt(args[2]);

        game.startGame(dictionary, wordLength);
        Scanner sc = new Scanner(System.in);

        while (guesses > 0) {
            // Display: # of remaining guesses, list of guesses, partially constructed word
            System.out.println("You have " + guesses + " guesses left");
            System.out.println("Used letters: " + guessedLetters);
            System.out.println("Word: " + game.getPartiallyConstructedWord());

            // Prompt the user for his/her next letter guess.
            System.out.print("Enter guess: ");
            String input = sc.nextLine();

            // If the guess is not a letter (U or L) then print “Invalid input” on the next line and re-prompt.
            if (input.length() != 1 || !Character.isLetter(input.charAt(0))) {
                System.out.println("Invalid input");
                continue;
            }

            char guess = Character.toLowerCase(input.charAt(0));

            try {
                game.makeGuess(guess);
            } catch (GuessAlreadyMadeException e) {
                System.out.println("Guess Already Made!");
                continue;
            }

            int count = game.getPartiallyConstructedWordCount(guess);

            if (count == 0) {
                System.out.println("Sorry, there are no " + guess + "\n");
                guesses--;
            } else {
                System.out.println("Yes, there is " + count + " " + guess);
            }

            if (game.gameOver()) {
                System.out.println("You win! The word was " + game.getPartiallyConstructedWord());
                break;
            }

            if (guesses == 0) {
                System.out.println("You lost. The word was " + game.getSecretWord());
                break;
            }
        }

        sc.close();
    }
}
