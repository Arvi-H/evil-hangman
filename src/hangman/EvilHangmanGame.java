package hangman;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EvilHangmanGame implements IEvilHangmanGame {
    private Set<String> dictionaryWords = new TreeSet<>();
    private final SortedSet<Character> guessedLetters = new TreeSet<>();
    private String bestKey = null;

    @Override
    public void startGame(File dictionary, int wordLength) throws IOException, EmptyDictionaryException {
        try {
            Scanner sc = new Scanner(dictionary);
            dictionaryWords.clear();

            while (sc.hasNext()) {
                String word = sc.next();

                if (word.length() == wordLength) {
                    dictionaryWords.add(word);
                }
            }

            if (dictionaryWords.isEmpty()) {
                throw new EmptyDictionaryException("The dictionary file is empty.");
            }

            sc.close();
        } catch (IOException e) {
            throw new IOException("An error occurred while reading the dictionary file: " + e.getMessage());
        }
    }

    @Override
    public Set<String> makeGuess(char guess) throws GuessAlreadyMadeException {
        // STEP 0: Check to see if the guess was already made and update guess list
        if (guessedLetters.contains(Character.toLowerCase(guess))) {
            throw new GuessAlreadyMadeException();
        }
        guessedLetters.add(Character.toLowerCase(guess));

        /* STEP 1: Creates unique keys for all the words in the dictionary with the guessed letter
         *  and groups all the words with the letter at that index in a set that the key will point to */
        HashMap<String, Set<String>> partitions = new HashMap<>();
        createPartitions(partitions, guess);

        /* STEP 2: Keys that point to the same size sets in the map are stored in
         * the new set keysOfLargestSets. Example: a--, -a-, --a, aa-, -aa, | -a, a- were eliminated */
        Set<String> keysOfLargestSets = new HashSet<>();
        findKeysOfLargestSizedSets(partitions, keysOfLargestSets);

        /* STEP 3: Keys that point to the sets with the least number of guesses are stored in
         *  the new set: keysOfFewestGuessesSets. Example: a--, -a-, --a | -aa, aa- were eliminated */
        Set<String> keysOfFewestGuessesSets = new HashSet<>();
        findKeysOfFewestGuesses(keysOfLargestSets, keysOfFewestGuessesSets, guess);

        /* STEP 4: Find the key with the most amount of guesses on the right-hand side
         * --a // a--, -a- were eliminated */
        findRightMostKeyPattern(keysOfFewestGuessesSets, guess);

        /* STEP 5: Update our dictionary by setting it to the biggest subset we found */
        dictionaryWords = partitions.get(bestKey);
        return dictionaryWords;
    }

    private void createPartitions(HashMap<String, Set<String>> partitions, char guess) {
        for (String word : dictionaryWords) {
            String subsetKey = createKey(word, guess);

            if (!partitions.containsKey(subsetKey)) {
                // Add the key/value pair to our partitions map
                partitions.put(subsetKey, new HashSet<>());
            }

            // If there's already a subset for that key, add the word
            partitions.get(subsetKey).add(word);
        }
    }

    private void findKeysOfLargestSizedSets(HashMap<String, Set<String>> partitions, Set<String> keysOfLargestSets) {
        int largestKeySize = 0;

        for (String key : partitions.keySet()) {
            if (partitions.get(key).size() > largestKeySize) {
                largestKeySize = partitions.get(key).size();
                bestKey = key;
                keysOfLargestSets.clear();
                keysOfLargestSets.add(key);
            } else if (partitions.get(key).size() == largestKeySize) {
                keysOfLargestSets.add(key);
            }
        }
    }

    private void findKeysOfFewestGuesses(Set<String> keysOfLargestSets, Set<String> keysOfFewestGuessesSets, char guess) {
        int min = Integer.MAX_VALUE;

        // If there are multiple sets with the same size, use tiebreakers
        if (keysOfLargestSets.size() > 1) {
            for (String key : keysOfLargestSets) {
                // Step 2. Choose the set with the fewest letters a--, -a-, --a, // aa-, -aa
                int count = 0;
                for (int i = 0; i < key.length(); i++) {
                    if (key.charAt(i) == guess) {
                        count++;
                    }
                }

                if (count < min) {
                    keysOfFewestGuessesSets.clear();
                    keysOfFewestGuessesSets.add(key);
                    min = count;
                } else if (count == min) {
                    keysOfFewestGuessesSets.add(key);
                }
            }
        }
    }

    private void findRightMostKeyPattern(Set<String> keysOfFewestGuessesSets, char guess) {
        if (keysOfFewestGuessesSets.size() > 1) {
            bestKey = null;
            for (String key : keysOfFewestGuessesSets) {
                if (bestKey == null) {
                    bestKey = key;
                } else { // if we already assigned it to something
                    for (int i = key.length()-1; i >= 0; i--) {
                        if (bestKey.charAt(i) != guess && key.charAt(i) == guess) {
                            bestKey = key;
                            break;
                        } else if (bestKey.charAt(i) == guess && key.charAt(i) != guess) {
                            break;
                        }
                    }
                }
            }
        } else if (keysOfFewestGuessesSets.size() == 1) {
            for (String value : keysOfFewestGuessesSets) {
                bestKey = value;
            }
        }
    }

    private String createKey(String word, char guess) {
        StringBuilder subsetKey = new StringBuilder();

        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) != guess) {
                subsetKey.append('-'); // add '-' to our string builder
            } else {
                subsetKey.append(guess); // add the guess to our string builder
            }
        }

        return subsetKey.toString();
    }

    @Override
    public SortedSet<Character> getGuessedLetters() {
        return guessedLetters;
    }

    public String getPartiallyConstructedWord() {
        StringBuilder bestWord = new StringBuilder();
        String word = dictionaryWords.iterator().next();

        for (int i = 0; i < word.length(); i++) {
            if (!guessedLetters.contains((word.charAt(i)))) {
                bestWord.append('-');
            } else {
                bestWord.append(word.charAt(i));
            }
        }

        return bestWord.toString();
    }

    public String getSecretWord() {
        return dictionaryWords.iterator().next();
    }

    public int getPartiallyConstructedWordCount(char guess) {
        int partiallyConstructedWordCount = 0;

        for (int i = 0; i < bestKey.length(); i++) {
            if (bestKey.charAt(i) == guess) {
                partiallyConstructedWordCount++;
            }
        }

        return partiallyConstructedWordCount;
    }

    public boolean gameOver() {
        String wordTest = getPartiallyConstructedWord();
        for (int i = 0; i < wordTest.length(); i++) {
            if (wordTest.charAt(i) == '-') {
                return false;
            }
        }
        return true;
    }
}
