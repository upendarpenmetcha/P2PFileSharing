import java.io.*;
import java.util.*;
import java.text.*;
import java.net.*;
import java.nio.*;
import java.util.concurrent.*;
import java.util.Map.*;

/*
 * Class for common.cfg (Common Config Data)
 */


public class Data_of_Common_config {

    /**
     * Define the preferred number of Neighbours
     *
     * @var int
     */
    private int Pref_Num_of_Neighbours;
  
    /**
     * Define the interval of Unchoking
     *
     * @var int
     */
    private int int_of_UnChoking;
  
    /**
     * Define the interval of optimistic Unchoking
     *
     * @var int
     */
    private int int_of_opti_UnChoking;
  
    /**
     * Define The size of a file
     *
     * @var string
     */
    private String my_File_Name;
  
    /**
     * Define The size of a file
     *
     * @var int
     */
    private int File_Size;
  
    /**
     * Define chuck size
     *
     * @var int
     */
    private int Size_of_Chunk;
  
    /**
     * Constructor
     */
    public Data_of_Common_config() {
      this.Pref_Num_of_Neighbours = 0;
      this.int_of_UnChoking = 0;
      this.int_of_opti_UnChoking = 0;
      this.File_Size = 0;
      this.Size_of_Chunk = 0;
    }
  
    /**
     * function getting the Value of UnChoking interval
     */
    public int Unchoking_Interval() {
      return int_of_UnChoking;
    }
  
    /**
     * Function for getting parse and updating the data
     */
    public void UpdateData_and_Parse(ArrayList<String> Rows_Data) {
      //throw error, create custom exception
      if (Rows_Data.size() < 6) {
        System.out.println("Number of the rows are Less");
        return;
      }
  
      /**
       * function for updating the value of common config
       */
      updatePref_Num_of_Neighbours(Rows_Data.get(0));
      updateint_of_UnChoking(Rows_Data.get(1));
      updateint_of_opti_UnChoking(Rows_Data.get(2));
      updatemy_File_Name(Rows_Data.get(3));
      updateFile_Size(Rows_Data.get(4));
      updateSize_of_Chunk(Rows_Data.get(5));
    }
  
    /**
     * function for getting the Value of optimistic Un-chocking Interval
     */
    public int Optimistic_UnChocking_Interval() {
      return int_of_opti_UnChoking;
    }
  
    /**
     * function for Updating the Value of preferred number of Neighbours
     */
    private void updatePref_Num_of_Neighbours(String key) {
      String[] Parse = key.split(" ");
      this.Pref_Num_of_Neighbours = Integer.parseInt(Parse[1]);
    }
  
    /**
     * function for interval of Unchoking
     */
    private void updateint_of_UnChoking(String key) {
      String[] Parse = key.split(" ");
      this.int_of_UnChoking = Integer.parseInt(Parse[1]);
    }
  
    /**
     * function interval of optimistic Unchoking
     */
    private void updateint_of_opti_UnChoking(String key) {
      String[] Parse = key.split(" ");
      this.int_of_opti_UnChoking = Integer.parseInt(Parse[1]);
    }
  
    /**
     * function for My File Name
     */
    private void updatemy_File_Name(String key) {
      String[] Parse = key.split(" ");
      this.my_File_Name = Parse[1];
    }
  
    /**
     * function for File Size
     */
    private void updateFile_Size(String key) {
      String[] Parse = key.split(" ");
      this.File_Size = Integer.parseInt(Parse[1]);
    }
  
    /**
     * function for getting the value of File size
     */
    public int Get_File_size() {
      return this.File_Size;
    }
  
    /**
     * function for updating the Chuck size
     */
    private void updateSize_of_Chunk(String key) {
      String[] Parse = key.split(" ");
      this.Size_of_Chunk = Integer.parseInt(Parse[1]);
    }
  
    /**
     * function for getting the value of chuck size 
     */
    public int Get_Chuck_Size() {
      return this.Size_of_Chunk;
    }
  
    /**
     * function for getting the value of preferred number of Neighbours 
     */
    public int Get_Pref_Num_of_Neighbours() {
      return Pref_Num_of_Neighbours;
    }
  
    /*
     * Calculating the The Total no of chunks
     */
    public int Total_Num_Of_Chunks() {
        int Total_Chunks = (int) Math.ceil((double) this.Get_File_size() / this.Get_Chuck_Size());
        return Total_Chunks;
    }
  
    /**
     * Printing the data of common config file
     */
    public void printIt() {
      System.out.println("Value of Preferred Neigbours " + this.Pref_Num_of_Neighbours);
      System.out.println("Valued of UnchokingInterval " + this.int_of_UnChoking);
      System.out.println("Valued of OptimisticUnchokingInterval " + this.int_of_opti_UnChoking);
      System.out.println("Valued of File_Name " + this.my_File_Name);
      System.out.println("Valued of File_Size " + this.File_Size);
      System.out.println("Valued of Piece_Size " + this.Size_of_Chunk);
    }
  }