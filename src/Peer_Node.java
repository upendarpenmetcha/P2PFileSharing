import java.io.*;
import java.util.*;
import java.text.*;
import java.net.*;
import java.nio.*;
import java.util.concurrent.*;
import java.util.Map.*;


/*
 * Class for connect peer nodes
 * */
public class Peer_Node {
    /*
     * private boolean Have_File;
     */
    private int Peer_ID;
    private String Name_Of_Host;
    private int Port_Number;
    private int Has_File;
    private int[] Marker_Chunks ;
    private int Num_Of_Pieces;
    public Peer_Node() {
      this.Num_Of_Pieces = 0;
    }
  
    public int Peer_Object_Initializing(String Peer_Data) {
      String[] Parse = Peer_Data.split(" ");
      this.Peer_ID = Integer.parseInt(Parse[0]);
      this.Name_Of_Host = Parse[1];
      this.Port_Number = Integer.parseInt(Parse[2]);
      this.Has_File = Integer.parseInt(Parse[3]);
      return this.Peer_ID;
    }
  
    /*
    * Define the Has File
    * */
    public boolean Has_File() {
      if (this.Has_File == 1) {
        return true;
      }
      return false;
    }
  
    /*
     * Set the Value of Have File
     * */
    public void Have_File_Set(int Have_File) {
      this.Has_File = Have_File;
    }
    
    public void Put_File_One() {
      this.Has_File = 1;
    }
  
    public void Put_File_Zero() {
      this.Has_File = 0;
    }
  
    /*
     * Get the Value Peer ID
     * */
    public int Get_Peer_ID() {
      return this.Peer_ID;
    }
    public void Download_Complete_One() {
        this.Has_File = 1;
    }
    
    public void Download_Complete_Zero() {
      this.Has_File = 0;
    }
    
    public void Download_File() {
        this.Has_File = 1;
    }
    /*
     * Get the Value of Has File
     * */
    public int Get_Has_File() {
      return this.Has_File;
    }
    /*
     * Update the Value of Has File
     * */
    public boolean Update_Has_File(int value) {
      this.Has_File = value;
      return this.Has_File == 1;
    }
    /*
     * Get the of Marker chunker
     * */
    public void Get_Marker_Chunks (int[] Marker_Chunks ) {
      this.Marker_Chunks  = Marker_Chunks ;
    }
    /*
     * Update the value of Marker chunker
     * */
    public void Update_Marker_Chunks (int[] Marker_Chunks ) {
      System.out.println("updating bit field for " + this.Peer_ID);
      this.Marker_Chunks = Marker_Chunks ;
    }
    /*
     * getting stored chunk
     * */
    public int Get_Stored_Chucks() {
      int cnt = 0;
      int a;
      for (a = 0; a < Marker_Chunks .length; a++) {
        if (Marker_Chunks [a] == 1)
          cnt++;
      }
      return cnt;
    }
    /*
     * getting Length of chunk
     * */
    public int Get_Chunks_Length() {
      return this.Marker_Chunks .length;
    }
    /*
     * getting Update the Number OF Pieces
     * */
    public void Update_Num_Of_Pieces() {
      this.Num_Of_Pieces++;
      int Current_P = this.Num_Of_Pieces;
      int BitF_Ln = this.Marker_Chunks .length;
      
      /*
       * check if all the chunks have been recived. 
       */
      if (Current_P == BitF_Ln) {
        this.Has_File = 1;
      }
    }
    /*
     * Get the Number of Pieces.
     */
    public int Get_Num_Of_Pieces() {
      return this.Num_Of_Pieces;
    }
    /*
     * Get the Port Number
     */
    public int Get_Port_Number() {
      return this.Port_Number;
    }
    /*
     * Get the name of host
     * */
    public String Get_Host_Name() {
      return this.Name_Of_Host;
    }
    /*
     * Get the Bit Field
     */
    public int[] Get_Bit_Field() {
      return this.Marker_Chunks ;
    }
    /*
     * Get the Update value of Bit Field
     */
    public void Update_Bit_Field(int Key_Index) {
      this.Marker_Chunks [Key_Index] = 1;
    }
    
    @Override
    public String toString() {
      return (" Peer_ID " + this.Peer_ID + " hostName: " + this.Name_Of_Host + " Port_Number: " + this.Port_Number
          + " Has_File: " + this.Has_File + "\n");
    }
  }
  