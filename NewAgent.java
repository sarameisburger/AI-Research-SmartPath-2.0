
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Collections;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.FileOutputStream;


/**
 * class NewAgent
 *
 * This is a "trial" agent that is being used to test a new algorithm for
 * finding the shortest path to the goal. This algorithm looks at
 * sequences of length 8 in episodic memory and combines the scores from
 * the positional weight matrix, a constituency/substring match algorithm, and the
 * number of steps to the goal to find the best possible
 * next move.
 *
 * @author: Sara Meisburger and Christine Chen
 *
 */
public class NewAgent extends StateMachineAgent
{

  public static void main (String[] args)
  {
    System.out.print("Hello World!");
  }
  
  //VARIABLES////////////////////////////

  //episodic memory generated
  //for this little NewAgent to use
  protected ArrayList<Episode> generateEpisodicMemory;

  //chance that a duplicate cmd is allowed if a random action is necessary
  double DUPLICATE_FORGIVENESS = .25; //25% chance a duplicate is permitted
  int COMPARE_SIZE = 8;
  public static final String OUTPUT_FILE2 = "sequences.csv";
  Episode tempEpisode;

  /**
  * MADE CHANGES TO NEW AGENT CONSTRUCTOR SINCE LAST COMMIT!!!
  */
  public NewAgent()
  {

    //set up environment in order to
    //initialize alphabet array (inherited from StateMachineAgent)
    env = new StateMachineEnvironment();
    alphabet = env.getAlphabet();

    //make sure the agent's memory is all spic and span
    generateEpisodicMemory.clear();

    //create a StateMachineAgent object, have it
    //roam around for a while, and then stash its episodic memory
    //away in genEpisodicMemory
    //StateMachineAgent gilligan = new StateMachineAgent();
    //gilligan.exploreEnvironment();
    //generateEpisodicMemory = gilligan.episodicMemory;
    //call generateRandomEpisodes//////////////////////
    generateRandomEpisodes(100);

  }

  protected int generateQualityScore(){

    Episode[] originalSequence = new Episode[COMPARE_SIZE];
    Episode[] foundSequence = new Episode[COMPARE_SIZE];
    int lastGoalIndex = findLastGoal(episodicMemory.size());
    int qualityScore = 0;//var to be returned

    if (lastGoalIndex == -1) {
        //since qualityScore has been init to 0, the ending score will be poor
        return qualityScore;
    }


    //If we've just reached the goal in the last 8 characters, then generate random steps
    //until we have a long enough original sequence
    for(int i=0; i< COMPARE_SIZE; i++){
      if (lastGoalIndex == episodicMemory.size() - i){
        generateRandomAction();
        generateQualityScore();
    }


    //fill the two arrays we will be comparing with 8 episodes
    for (int k=1; k<=COMPARE_SIZE; k++){
      originalSequence[i] = (generateEpisodicMemory.get(generateEpisodicMemory.size()-k));

    for (int j=1; j<=(COMPARE_SIZE); j++){
      foundSequence[j] = (generateEpisodicMemory.get(lastGoalIndex-j));
    }


    try {
        FileWriter csv = new FileWriter(OUTPUT_FILE2);
        for(int q=0; q<8; q++){
          csv.append(originalSequence[q].command);
        }
        for(int p=0; p<8; p++){
          csv.append(foundSequence[p].command);
        }

        csv.close();
      }
      catch (IOException e) {
          System.out.println("tryAllCombos: Could not create file, what a noob...");
          System.exit(-1);
      }
    //test to see if works


  }
}
return qualityScore;
}

  protected char generateRandomAction(){
        //decide if a dup command is acceptable
        double chanceForDup = Math.random();
        boolean dupPermitted = false;
        if (chanceForDup < DUPLICATE_FORGIVENESS) {
            dupPermitted = true;
        }

        //keep generating random moves till it is different from last or dups are allowed
        char possibleCmd;
        Episode lastEpisode = generateEpisodicMemory.get(generateEpisodicMemory.size() - 1);
        char lastCommand = lastEpisode.command;

        do {
            possibleCmd = alphabet[random.nextInt(alphabet.length)];
            if (dupPermitted)//if they are allowed we don't care to check for dup
                break;
        } while (possibleCmd == lastCommand); //same cmd, redo loop

		return possibleCmd;
	}

  protected ArrayList<Episode> generateRandomEpisodes(int length){
    //generate random episodes based on chosen length
    for(int i=0; i<length; i++){
      //create a random episode
      Episode tempEpisode = null;
      tempEpisode.command = generateRandomAction();
      tempEpisode.sensorValue = randomAtGoal(50);
      generateEpisodicMemory.add(tempEpisode);
    }
    //PrintWriter writer = new PrintWriter ("outputfile2.txt");
    //PrintWriter out = new PrintWriter(new FileWriter("users\\sarameisburger\\Desktop\\outputfile2.txt"));
    save(generateEpisodicMemory);
    return generateEpisodicMemory;

  }

  public void save(ArrayList<Episode> output) {
  try {
    PrintWriter pw = new PrintWriter(new FileOutputStream(OUTPUT_FILE2));
  for (Episode episode : output)
      pw.println(episode.command);
  pw.close();
}
  catch (IOException e) {
    System.out.println("tryAllCombos: Could not create file, what a noob...");
    System.exit(-1);
}
}

  //randomly gives an "at goal?" value of 0 or 1
  public int randomAtGoal(int probability){
    int atGoal = (int)(Math.random()*probability);
    if(atGoal == 0){
      return 1; //say it reeached the goal
    }
    else {
      return 0; //otherwise it did not reach the goal
    }
  }

  public char getChar(Episode epi){
    return epi.command;
  }


/*
* creates a positional weight matrix based on originalEpisodes
* and calculates a positional weight matrix score for foundEpisodes based
* on the matrix
*
*/
private double calcPWMScore(Episode[] originalEpisodes, Episode[] foundEpisodes)
{
  //convert Episode arrays into char arrays
  char[] originalChars = new char[COMPARE_SIZE];
  char[] foundChars = new char[COMPARE_SIZE];

  for (int i = 0; i<COMPARE_SIZE; i++)
  {
    originalChars[i] = originalEpisodes[i].command;
    foundChars[i] = foundEpisodes[i].command;
  }

  //create 2D array to house the positional weight matrix that will
  //be created based on originalChars
  //number of rows should be number of letters in alphabet (a->row 0, b->row 1, etc.)
  //number of columns should be the length of the char arrays
  double[][] pwm = new double [alphabet.length][COMPARE_SIZE];

  //might want to initialize each position in pwm to 0.0 in the future

  //arbitrary values subject to change
  double high_score = .91;
  double low_score = .09/(alphabet.length-1);

  //start putting values into pwm
  //outer loop is keeping track of the column number, k, and k
  //corresponds to the kth position in originalChars
  for (int k = 0; k<COMPARE_SIZE; k++)
  {
    //what row number does the character in originalChars[k]
    //correspond to?
    int charRowNumber = this.indexOfCharacter(originalChars[k]);

    //orginalChars[k] is the correct character for this spot,
    //so it gets a high_score
    pwm[charRowNumber][k] = high_score;

    //every other element in this column gets a low_score
    for(int row = 0; row<pwm.length; row++)
    {
      if(row != charRowNumber)
      {
        pwm[row][k] = low_score;
      }

    }

  }

  double scoreToReturn = 1.0;

  //now calculate the pwm score of foundChars based on the matrix
  //k corresponds to the kth position in foundChars
  for (int k = 0; k<COMPARE_SIZE; k++)
  {
    //what row number does the character in foundChars[k]
    //correspond to?
    int charRowNumber = this.indexOfCharacter(foundChars[k]);
    scoreToReturn = scoreToReturn * pwm[charRowNumber][k];
  }

  return scoreToReturn;

}


}
