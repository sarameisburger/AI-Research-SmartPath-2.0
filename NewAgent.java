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
  //episodic memory generated by a "helper" StateMachineAgent
  //for this little NewAgent to use
  //protected static ArrayList<Episode> episodicMemory;
  //chance that a duplicate cmd is allowed if a random action is necessary
  double DUPLICATE_FORGIVENESS = .25; //25% chance a duplicate is permitted
  protected static int COMPARE_SIZE = 8;
  public static final String OUTPUT_FILE2 = "sequences.csv";
  Episode tempEpisode;
  //constants for scores
  private static int COUNTING_CONSTANT = 10;
  private static int ALIGNED_CONSTANT= 10;

  /**
  *
  */
  //public static void main(String[] args)
  public NewAgent()
  {
    //create a StateMachineAgent object, have it
    //roam around for a while, and then stash its episodic memory
    //away in genEpisodicMemory
    //StateMachineAgent gilligan = new StateMachineAgent();
    //gilligan.exploreEnvironment();
    //episodicMemory = gilligan.episodicMemory;
    //generateRandomEpisodes(100);
    //env = new StateMachineEnvironment();
    //alphabet = env.getAlphabet();
    //System.out.print("score");
    super();
  }

  public void exploreEnvironment(){

    Episode[] originalSequence = new Episode[COMPARE_SIZE];
    Episode[] foundSequence = new Episode[COMPARE_SIZE];
    int lastGoalIndex;
    int qualityScore = 0;//var to be returned
    int maxQualityScore = 0;
    char recommendedCharacter = generateSemiRandomAction();
    boolean atGoal = false;

    while (episodicMemory.size() < MAX_EPISODES) {
      lastGoalIndex = findLastGoal(episodicMemory.size());
      checkConditions(lastGoalIndex);
      originalSequence = getOriginalSequence();


      for(int w = lastGoalIndex; w >= COMPARE_SIZE-1; w--){
        int meetsFoundConditions = checkFoundConditions(w);
        if(meetsFoundConditions == -1){
        foundSequence = getFoundSequence(w);
      }
        else {
          w = meetsFoundConditions;
        }

        //call our quality methods to get scores
        double countingScore = getCountingScore(originalSequence, foundSequence);

        double alignedMatches = getAlignedMatchesScore(originalSequence, foundSequence);

        int tempQualityScore = (int)((COUNTING_CONSTANT)*countingScore + (ALIGNED_CONSTANT)*alignedMatches);
        if(tempQualityScore > maxQualityScore){
          maxQualityScore = tempQualityScore;
          recommendedCharacter = episodicMemory.get(w+1).command;
        }

      }

      atGoal = tryPath(stringToPath(Character.toString(recommendedCharacter)));


  }

}



// protected char generateSemiRandomAction(){
//   //decide if a dup command is acceptable
//   double chanceForDup = Math.random();
//   boolean dupPermitted = false;
//   if (chanceForDup < DUPLICATE_FORGIVENESS) {
//     dupPermitted = true;
//   }
//
//   //keep generating random moves till it is different from last or dups are allowed
//   char possibleCmd;
//   Episode lastEpisode = episodicMemory.get(episodicMemory.size() - 1);
//   char lastCommand = lastEpisode.command;
//
//   do {
//     possibleCmd = alphabet[random.nextInt(alphabet.length)];
//     if (dupPermitted)//if they are allowed we don't care to check for dup
//     break;
//   } while (possibleCmd == lastCommand); //same cmd, redo loop
//
//   return possibleCmd;
// }

protected ArrayList<Episode> generateRandomEpisodes(int length){
  //generate random episodes based on chosen length
  ArrayList<Episode> episodicMemoryAL = new ArrayList<Episode>();

  for(int i=0; i<length; i++){
    //create a random episode
    Episode tempEpisode = new Episode('a',0);
    //tempEpisode.command = generateSemiRandomAction();
    tempEpisode.command = alphabet[random.nextInt(alphabet.length)];
    //System.out.print(tempEpisode.command);

    tempEpisode.sensorValue = randomAtGoal(50);
    //episodicMemory.add(tempEpisode);
    episodicMemoryAL.add(tempEpisode);
  }
  //PrintWriter writer = new PrintWriter ("outputfile2.txt");
  //PrintWriter out = new PrintWriter(new FileWriter("users\\sarameisburger\\Desktop\\outputfile2.txt"));
  //save(episodicMemory);
  //return episodicMemory;
  return episodicMemoryAL;

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
protected double getAlignedMatchesScore(Episode[] originalEpisodes, Episode[] foundEpisodes)
{
  //convert Episode arrays into char arrays
  char[] originalChars = new char[COMPARE_SIZE];
  char[] foundChars = new char[COMPARE_SIZE];

  for (int i = 0; i<COMPARE_SIZE; i++)
  {
    originalChars[i] = originalEpisodes[i].command;
    foundChars[i] = foundEpisodes[i].command;
  }

  int numAlignedChars = 0;

  for(int i = 0; i<COMPARE_SIZE; i++)
  {
    if(originalChars[i] == (foundChars[i]))
    {
      numAlignedChars++;
      System.out.println("direct matching char: "+i + " "+ originalChars[i]);
    }
  }

  double scoreToReturn = (double)numAlignedChars/COMPARE_SIZE;
  return scoreToReturn;


  // //create 2D array to house the positional weight matrix that will
  // //be created based on originalChars
  // //number of rows should be number of letters in alphabet (a->row 0, b->row 1, etc.)
  // //number of columns should be the length of the char arrays
  // double[][] pwm = new double [alphabet.length][COMPARE_SIZE];
  //
  // //might want to initialize each position in pwm to 0.0 in the future
  //
  // //arbitrary values subject to change
  // double high_score = .91;
  // double low_score = .09/(alphabet.length-1);
  //
  // //start putting values into pwm
  // //outer loop is keeping track of the column number, k, and k
  // //corresponds to the kth position in originalChars
  // for (int k = 0; k<COMPARE_SIZE; k++)
  // {
  //   //what row number does the character in originalChars[k]
  //   //correspond to?
  //   int charRowNumber = this.indexOfCharacter(originalChars[k]);
  //
  //   //orginalChars[k] is the correct character for this spot,
  //   //so it gets a high_score
  //   pwm[charRowNumber][k] = high_score;
  //
  //   //every other element in this column gets a low_score
  //   for(int row = 0; row<pwm.length; row++)
  //   {
  //     if(row != charRowNumber)
  //     {
  //       pwm[row][k] = low_score;
  //     }
  //
  //   }
  //
  // }
  //
  // double scoreToReturn = 1.0;
  //
  // //now calculate the pwm score of foundChars based on the matrix
  // //k corresponds to the kth position in foundChars
  // for (int k = 0; k<COMPARE_SIZE; k++)
  // {
  //   //what row number does the character in foundChars[k]
  //   //correspond to?
  //   int charRowNumber = this.indexOfCharacter(foundChars[k]);
  //   scoreToReturn = scoreToReturn * pwm[charRowNumber][k];
  // }



}



/**
*Step by step what this method does
* 1. turns the array of characters found and original, into one long string each
* 2. iterates through each the found string and original string and finds all subsequences
* 3. compares each subsequence to the other, if it finds one it moves on to avoid overcounting

*/

protected double getCountingScore(Episode[] original, Episode[] found){
  ArrayList<String> originalSubsequences = new ArrayList<String>();
  ArrayList<String> foundSubsequences = new ArrayList<String>();
  int count = 0; //counter for how many subsequences match
  int score = 0; //score for matching subsequences, longer sub = higher score
  double finalCountingScore;

  //convert Episode arrays into char arrays
  char[] originalChars = new char[COMPARE_SIZE];
  char[] foundChars = new char[COMPARE_SIZE];

  for (int i = 0; i<COMPARE_SIZE; i++)
  {
    originalChars[i] = original[i].command;
    foundChars[i] = found[i].command;
  }

  String originalString = new String (originalChars); //make arrays into string to get subsequences
  String foundString = new String (foundChars);

  //get arraylist of subsequences for original
  for(int i=1; i<=originalString.length(); i++){ // i determines length of string
    for(int j=0; j<=originalString.length()-i; j++){ // j determines where we start (indice) in string
      originalSubsequences.add(originalString.substring(j,j+i));
      System.out.println("original subsequences: "+ originalString.substring(j,j+i));
    }
  }
  //get arraylist of subsequences for found
  for(int g=1; g<=foundString.length(); g++){
    for(int f=0; f<=foundString.length()-g; f++){
      foundSubsequences.add(foundString.substring(f,f+g));
      System.out.println("found subsequences: "+ foundString.substring(f,f+g));
    }
  }
  //for each subsequence in original, compare to see if it is in found list of subsequences
  for(int p=0; p<originalSubsequences.size(); p++){
    for(int q=0; q<foundSubsequences.size(); q++){
      if(originalSubsequences.get(p).equals(foundSubsequences.get(q))){
        String temp = originalSubsequences.get(p);
        System.out.println(temp + " " + foundSubsequences.get(q));

        count++;
        score = temp.length() + score;
        System.out.println(score);
        foundSubsequences.remove(q); //avoid overcounting, once counted, remove
        //p++;
        break;
        //if found matching sequence, move on to next i so we don't repeat counting
        //use break instead of p++ to get out of inner for loop
        //generate number between 0 and 1 for score...divide by max score 1=best
        //fairly evenly
      }
    }
  }
  //get max number of subsequences possible with sequence length
  int maxSubSeq = ((COMPARE_SIZE)*(COMPARE_SIZE)+(COMPARE_SIZE))/2;
  //System.out.print("the max amount of subsequences is: " + maxSubSeq);
  System.out.println("the total number of matching sequences is: "+ count);
  finalCountingScore = (double)score/maxSubSeq;
  return finalCountingScore;
}
/**
* main
*
* helper methods (above) have been defined to do various things here.
* Modify this method to call the one(s) you want.
*/
public static void main(String [ ] args) {

  //System.out.println(generateRandomEpisodes(100));
  tryGenLearningCurves();
}

private void checkConditions(int lastGoalIndex){
  int atGoal = -1;

  while (lastGoalIndex == -1) {
    System.out.println("we are checking conditions");
  //since qualityScore has been init to 0, the ending score will be poor
  char randomChar  = generateSemiRandomAction();
  tryPath(stringToPath(Character.toString(randomChar)));
  atGoal = episodicMemory.get(episodicMemory.size()-1).sensorValue;
  if(atGoal == 1){
    break;
  }
  lastGoalIndex = findLastGoal(episodicMemory.size()-1);
  }


//If we've just reached the goal in the last 8 characters, then generate random steps until long enough
    while (lastGoalIndex > episodicMemory.size() - COMPARE_SIZE || episodicMemory.size() < COMPARE_SIZE || lastGoalIndex < COMPARE_SIZE){
      System.out.println("In the second while loop");
    char randomAction = generateSemiRandomAction();
    tryPath(stringToPath(Character.toString(randomAction)));
    lastGoalIndex = findLastGoal(episodicMemory.size()-1);
  }
}

private Episode[] getOriginalSequence(){
  //fill the array we will be comparing with 8 episodes
  Episode[] originalSequence = new Episode[COMPARE_SIZE];
  for (int k=1; k<=COMPARE_SIZE; k++){ //WE CHANGED THIS ON BOTH BRANCHES
    originalSequence[k-1] = (episodicMemory.get(episodicMemory.size()-k));
  }
  return originalSequence;
}

private Episode[] getFoundSequence(int indice){
  Episode[] foundSequence = new Episode[COMPARE_SIZE];
  for (int j=1; j<=COMPARE_SIZE; j++){
    foundSequence[j-1] = (episodicMemory.get(indice));
    indice--;
  }
  return foundSequence;
}

private int checkFoundConditions(int indice){
  for(int i=indice; i>indice - COMPARE_SIZE; i--){
    if(episodicMemory.get(i).sensorValue == 1){
      return i;
    }
  }
  return -1;
}

public static void tryGenLearningCurves()
{
    try {

        FileWriter csv = new FileWriter(OUTPUT_FILE);
        for(int i = 0; i < NUM_MACHINES; ++i) {
            System.out.println("making a new agent");
            NewAgent gilligan = new NewAgent();
            gilligan.exploreEnvironment();
            gilligan.recordLearningCurve(csv);
        }
        csv.close();
    }
    catch (IOException e) {
        System.out.println("tryAllCombos: Could not create file, what a noob...");
        System.exit(-1);
    }
}//tryGenLearningCurves

}

  // try {
  //   FileWriter csv = new FileWriter(OUTPUT_FILE2);
  //   for(int q=0; q<8; q++){
  //     csv.append(originalSequence[q].command);
  //   }
  //   for(int p=0; p<8; p++){
  //     csv.append(foundSequence[p].command);
  //   }
  //
  //   csv.close();
  // }
  // catch (IOException e) {
  //   System.out.println("tryAllCombos: Could not create file, what a noob...");
  //   System.exit(-1);
  // }
