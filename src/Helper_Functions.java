import java.io.*;
import java.util.*;
import java.text.*;
import java.net.*;
import java.nio.*;
import java.util.concurrent.*;
import java.util.Map.*;

/*
* class of Helper Functions
* */

public class Helper_Functions {

	/*
	* generating Handshake packet
	* */
  public static byte[] Handshake_Packet_Generate(int Cur_Node_Id) {

    byte[] Packet_Of_HandShake = new byte[32];

    int Key_Index = 0;
    int a;

    /*
    * calculating Header in bytes
    * */
    String Header_Hand_Shake = Constants.Get_Header_Hand_Shake();
    byte[] Header = new String(Header_Hand_Shake).getBytes();
    for (a = 0; a < Header.length; a++) {
      Packet_Of_HandShake[Key_Index] = Header[a];
      Key_Index++;
    }

    /*
     * calculating Zeros in bytes
     * */
    String zeros = Constants.Get_Zeros();
    byte[] In_Bytes_Zeroes = new String(zeros).getBytes();
    for (a = 0; a < In_Bytes_Zeroes.length; a++) {
      Packet_Of_HandShake[Key_Index] = In_Bytes_Zeroes[a];
      Key_Index++;
    }
   
    byte[] In_Bytes_Peer_ID= ByteBuffer.allocate(4).putInt(Cur_Node_Id).array();

    for (a= 0; a < In_Bytes_Peer_ID.length; a++) {
      Packet_Of_HandShake[Key_Index] = In_Bytes_Peer_ID[a];
      Key_Index++;
    }

    return Packet_Of_HandShake;
  }

  public static byte[] Array_Byte_Copy(byte[] Org, int start, int end) {
    int Length_New = end - start;
    if (Length_New < 0)
      throw new IllegalArgumentException(start + " > " + end);
    byte[] copied = new byte[Length_New];
    System.arraycopy(Org, start, copied, 0, Math.min(Org.length - start, Length_New));
    return copied;
  }

  /*
   * check for missing Chunks
   * */
  public static boolean Check_Missing_Chunks(int[] Peer_Bit_Field, int[] Peer_Bit_Field_Connection, int ln) {
    int a;
    for (a = 0; a < ln; a++) {
      if (Peer_Bit_Field_Connection[a] == 1 && Peer_Bit_Field[a] == 0) {
        return true;
      }
    }
    return false;
  }

  /*
   * getting Random Chunk file
   * */
  public static int Random_Chunk_File(int[] Peer_Bit_Field, int[] Peer_Bit_Field_Connection, int ln) {
    ArrayList<Integer> Chunk_Needed = new ArrayList<>();
    int a;
    for (a= 0; a < ln; a++) {
      if (Peer_Bit_Field_Connection[a] == 1 && Peer_Bit_Field[a] == 0 ) {
    	  Chunk_Needed.add(a);
      }
    }
    int Chunk_Need_Ln = Chunk_Needed.size();
    
    if (Chunk_Need_Ln <= 0) {
        return -1;
    } else {
        Random Random_Object = new Random();
      int randKey_Index = Math.abs(Random_Object.nextInt() % Chunk_Need_Ln); 
      int result = Chunk_Needed.get(randKey_Index);
      return result;
    }
  }

/*
 *  logging functions 
 * */
 
  public static void Log(BufferedWriter writer, int ID_1, int ID_2, String Type_Of_Message) {
    Date Time;
    Time = new Date();

    StringBuffer Log_Write;
    Log_Write = new StringBuffer();
    DateFormat Time_Format = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
    if (Type_Of_Message == "INTERESTED") {
      Log_Write.append(Time_Format.format(Time)+": Peer [" + ID_1 + "] received the 'interested' message from [" + ID_2 + "]." );
    }
    else if (Type_Of_Message == "NOTINTERESTED") {
      Log_Write.append(Time_Format.format(Time)+": Peer [" +ID_1 + "] received the 'not interested' message from [" + ID_2 + "]." );
    }
    else if (Type_Of_Message == "CHOKE") {
      Log_Write.append(Time_Format.format(Time)+": Peer [" + ID_1  +"] is choked by ["+ ID_2 +"].") ;
    }
    else if (Type_Of_Message == "UNCHOKE") {
      Log_Write.append(Time_Format.format(Time)+": Peer [" + ID_1 + "] is unchoked by [" + ID_2 + "].");
    }
    else if (Type_Of_Message == "connectionFrom") {
      Log_Write.append(Time_Format.format(Time)+": Peer [" + ID_1 + "] is connected from Peer [" + ID_2 + "].");
    }
    else if (Type_Of_Message == "connectionTo") {
      Log_Write.append(Time_Format.format(Time)+": Peer [" + ID_1 + "] makes a connection to Peer [" + ID_2 + "].");

    }  else if (Type_Of_Message == "changeUnchoke_OptimisticallydNeighbor") {
      Log_Write.append(Time_Format.format(Time)+": Peer [" + "] has the optimistically unchoked neighbor [" + ID_2 + "].");
    }
    try {
      String Last_Value;
      Last_Value = Log_Write.toString() ;
      writer.write(Last_Value);
      writer.newLine();
      writer.flush();
    }catch(FileNotFoundException e){
     
    }catch(IOException e){

    }
  }

  /*
   * logger for receive Have
   */
  public static void Have_Log_Receive(BufferedWriter writer, int ID_1, int ID_2, int Key_Index) {

    Date Time;
    Time = new Date();

    StringBuffer Log_Write; 
    DateFormat Time_Format = new SimpleDateFormat("yy/MM/dd HH:mm:ss");

    Log_Write = new StringBuffer();
    Log_Write.append(Time_Format.format(Time) + ": Peer [" + ID_1 +"] received 'have' message from [" + ID_2+ "] for the piece: " + Key_Index + '.' );
    try {
      String Last_Value;
      Last_Value = Log_Write.toString() ;
      writer.write(Last_Value);
      writer.newLine();
      writer.flush();
    } catch (Exception e) {
      /*
       * e.printStackTrace();
       */
    }
  }
  
  public static void Preferred_Neighbors_Log(BufferedWriter writer, int ID_1, int[] List_ID) {

    Date Time;
    Time = new Date();
    DateFormat Time_Format = new SimpleDateFormat("yy/MM/dd HH:mm:ss");

    StringBuffer Log_Write;
    Log_Write = new StringBuffer();
    Log_Write.append(Time_Format.format(Time) +": Peer [" + ID_1 + "] has the preferred neighbors [" );
    int a;
    for ( a= 0;a <List_ID.length ; a++) {
      Log_Write.append(List_ID[a]);
      if (a< (List_ID.length - 1) )
          Log_Write.append(',');
        }

    /*
     *writer_log.deleteCharAt(writer_log.length() - 1);
     * */
    Log_Write.append("].");
    try {
      String Last_Value;
      Last_Value = Log_Write.toString() ;
      writer.write(Last_Value);
      writer.newLine();
      writer.flush();
    } catch(FileNotFoundException e){
     
    }catch(IOException e){

    }
  }

  /*
   *logger for Download Pieces is complete
   */
  public static void Download_Pieces_Log(BufferedWriter writer, int ID_1, int ID_2, int Key_Index, int number_of_pieces) {
    Date Time;
    Time = new Date();

    StringBuffer Log_Write;
    Log_Write = new StringBuffer();
    DateFormat Time_Format = new SimpleDateFormat("yy/MM/dd HH:mm:ss");

    Log_Write.append(Time_Format.format(Time) + ": Peer [" + ID_1 + "] has downloaded the piece " + Key_Index + " from [" + ID_2 + "]. " +"Now the number of pieces it has is : "+ number_of_pieces + '.');
    
    try {
      String Last_Value;
      Last_Value = Log_Write.toString() ;
      writer.write(Last_Value);
      writer.newLine();
      writer.flush();
    } catch(FileNotFoundException e){
    }catch(IOException e){
    }
  }

  /*
   *logger for Complete Downloading
   */
  public static void Completed_Downloading_Log(BufferedWriter writer, int ID_1) {
    Date Time;
    Time = new Date();
    DateFormat Time_Format = new SimpleDateFormat("yy/MM/dd HH:mm:ss");

    StringBuffer Log_Write;
    Log_Write = new StringBuffer();
    Log_Write.append(Time_Format.format(Time)+": Peer [" + ID_1 + "] has downloaded complete file ");
    
    try {
      String Last_Value;
      Last_Value = Log_Write.toString() ;
      writer.write(Last_Value);
      writer.newLine();
      writer.flush();
    } catch(FileNotFoundException e){
     
    }catch(IOException e){

    }
  }
}