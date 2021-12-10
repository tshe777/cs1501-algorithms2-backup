/** CS 1501 Spring 2021 Assignment 2: AutoComplete
* @author Tao Sheng
**/


import java.io.*;
import java.util.*;

public class AutoComplete{
  public static DLBNode root; 

  public AutoComplete(String dictFile) throws java.io.IOException {
 
    Scanner fileScan = new Scanner(new FileInputStream(dictFile));
    while(fileScan.hasNextLine()){
      StringBuilder word = new StringBuilder(fileScan.nextLine());
      add(word);
    }
    fileScan.close();
  }

  // Takes a StringBuilder as input and adds a word into DLB Trie
  public void add(StringBuilder word){
    if (word == null) throw new IllegalArgumentException("Cannot call add() with null word");
    root = add(root, word, 0);
  }
  
  // Helper method for main add method; takes a DLBNode pointer, word, and integer position and returns the DLBNodes into the other add method
  private DLBNode add(DLBNode x, StringBuilder word, int pos) {
    DLBNode result = x;
    // Match each character of the key as we traverse the list, 
    // calling recursively to traverse further either on children nodes or siblings (if x != null and x != word.charAt(pos))
    if (x == null) {
      result = new DLBNode(word.charAt(pos), 0);
      if (pos < word.length() - 1) {
        result.child = add(x, word, pos+1);
      } else {
        result.data = word.charAt(pos);
      }
    } 
    else if (x.data == word.charAt(pos)) {
      if (pos < word.length() -1) {
        result.child = add(x.child, word, pos+1);
      } else {
        result.data = word.charAt(pos);  
      }
    } 
    else {
      result.sibling = add(x.sibling, word, pos);
    }

    // Instead of adding sentinels, will change the boolean flag .isWord at the last character of the word that was added. Only does this under both conditions that 
    // we are at the correct DLBNode in the Trie and that this was after going through the entire insert key's characters. 
    if (result.data == word.charAt(word.length()-1) && pos == word.length()-1) {
      result.isWord = true;
    }
    return result;
  } 

  //Given the StringBuilder word, increments the score of word; called when a word is selected
  public void notifyWordSelected(StringBuilder word){
    //Calls getNode to get score (pointer points to the end, then just get that node's score)
    DLBNode result = getNode(root, word.toString(), 0);
    //In some cases, this is needed to ensure the node the pointer is indeed at a word 
    if (result.isWord) {
      result.score++;
    }
  }
  
  //Simply return the score of the input StringBuilder word
  public int getScore(StringBuilder word){
    //Calls getNode to get score (pointer points to the end, then just get that node's score)
    DLBNode result = getNode(root, word.toString(), 0);
        //In some cases, this is needed to ensure the node the pointer is indeed at a word 
    if (result.isWord) {
      return result.score;
    }
    return 0;
  }
 
  //Retrieve a sorted list of autocomplete words given a StringBuilder input
  //The list will be sorted in descending order based on score and if scores tie, by the letters as outlined in my custom compareTo()
  public ArrayList<Suggestion> retrieveWords(StringBuilder word){ 
    ArrayList<Suggestion> sug = new ArrayList<Suggestion>();
    DLBNode y = getNode(root, word.toString(), 0);
    //If getNode returns null we shouldn't do anything 
    if (y == null) return sug;

    //Check if the input is a word itself before using collect helper method because then it will append before adding 
    if (y.isWord) sug.add(new Suggestion (word, y.score));

    //Calling helper method
    retrieveWords(y.child, sug, word);

    //Sorting in desired order with new compareTo method
    Collections.sort(sug);
    return sug;
  } //end retrieve words 

  //Private helper method that alters the ArrayList of type Suggestion called "sug"
  //Takes a DLBNode, the arraylist reference, and a StringBuilder word
  private void retrieveWords(DLBNode x, ArrayList<Suggestion> sug, StringBuilder word) {
    // Base Case if given a null DLBNode
    if (x == null) return;
    DLBNode curr = x;

    // Traversing the DLB Trie to look for words and if one is found. Appending as we go down the Trie. Adding to the ArrayList if a word is found.
    // Backtracking by removing the last character (after recursive call) 
    while (curr != null) {
      word.append(curr.data);
      if (curr.isWord) {
        StringBuilder current = new StringBuilder (word);
        sug.add(new Suggestion (current, curr.score));
      }      
      retrieveWords(curr.child, sug, word);
      word.deleteCharAt(word.length()-1);
      // Move to siblings without appending to the StringBuilder
      curr = curr.sibling;
    }
  }

  // OTHER METHODS

  //Print the subtree after the start string
  public void printTree(String start){
    System.out.println("==================== START: DLB Tree Starting from "+ start + " ====================");
    DLBNode startNode = getNode(root, start, 0);
    if(startNode != null){
      printTree(startNode.child, 0);
    }
    System.out.println("==================== END: DLB Tree Starting from "+ start + " ====================");
  }

  //A helper method for printing the tree
  private void printTree(DLBNode node, int depth){
    if(node != null){
      for(int i=0; i<depth; i++){
        System.out.print(" ");
      }
      System.out.print(node.data);
      if(node.isWord){
        System.out.print(" *");
      }
        System.out.println(" (" + node.score + ")");
      printTree(node.child, depth+1);
      printTree(node.sibling, depth);
    }
  }

  //return a pointer to the node at the end of the start string. Called from printTree.
  private DLBNode getNode(DLBNode node, String start, int index){
    DLBNode result = node;
    if(node != null){
      if((index < start.length()-1) && (node.data.equals(start.charAt(index)))) {
          result = getNode(node.child, start, index+1);
      } else if((index == start.length()-1) && (node.data.equals(start.charAt(index)))) {
          result = node;
      } else {
          result = getNode(node.sibling, start, index);
      }
    }
    return result;
  }


  //A helper class to hold suggestions. Each suggestion is a (word, score) pair. This class implements Comparable
  public class Suggestion implements Comparable<Suggestion> {
    public StringBuilder word;
    public int score;

    //2 arguments constructor: StringBuilder and int. 
    public Suggestion (StringBuilder word, int score) {
      this.word = word;
      this.score = score;
    }

    // Custom compareTo method to do a backwards score sort (highest score goes first)
    // returns 1 if this is < than other (alphabetically and / or score wise)
    // returns -1 if other is > than this (alphabetically and / orscore wise)

    public int compareTo(Suggestion other) 
    {
      int scoreDiff = this.score - other.score;
      if (scoreDiff > 0) {
        return -1;
      }
      else if (scoreDiff < 0) {
        return 1;
      }
      else {
        //Lexigraphical comparison if the scores are tied
        int comp = this.word.toString().compareTo(other.word.toString());
        if (comp > 0) 
          return 1;
        else if (comp < 0)
          return -1;
        else
          return 0;
      }

    } //end compareTo method



  } //end suggestion class

  //The node class.
  private class DLBNode{
    private Character data;
    private int score;
    private boolean isWord;
    private DLBNode sibling;
    private DLBNode child;

    private DLBNode(Character data, int score){
        this.data = data;
        this.score = score;
        isWord = false;
        sibling = child = null;
    }
  }
}
