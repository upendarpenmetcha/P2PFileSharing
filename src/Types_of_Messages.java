import java.io.*;
import java.util.*;
import java.text.*;
import java.net.*;
import java.nio.*;
import java.util.concurrent.*;
import java.util.Map.*;
/**
 * Class of Message Type
 */
public class Types_of_Messages {

    /**
     * creating a hashMap for store Massage Types
     **/
      private HashMap<String, Character> map;
      
      public Types_of_Messages() {
          map = new HashMap<>();
            map.put("UNCHOKE" , '1');
            map.put("Bit_Field" , '5');
            map.put("INTERESTED" , '2');
            map.put("NOT_INTERESTED" , '3');
            map.put("REQUEST" , '6');
            map.put("PIECE" , '7');
            map.put("HAVE" , '4');
            map.put("CHOKE" , '0');
      }
  
      /**
       * return value of CHOKE
       */
      public char Choke_Char() {
          char ln = map.get("CHOKE");
          return ln;
      }
    
      /**
     * return value of UNCHOKE
     */
      public char UNChoke_Char() {
          char ln = map.get("UNCHOKE");
          return ln;
      }
  
      /**
     * return value of Interested peers
     */
      public char Interested_Char() {
          char ln = map.get("INTERESTED");
          System.out.println(ln + " ...................................................");
          return ln;
      }
  
      /**
     * return value of Not Interested peers
     */
      public char Not_Interested_Char() {
          char ln = map.get("NOT_INTERESTED");
          System.out.println(ln + " ...................................................");
          return ln;
      }
  
      /**
     * return value of Interested peers
     */
      public char Bit_Field_Char() {
          char ln = map.get("Bit_Field");
          System.out.println(ln + " ...................................................");
          return ln;
      }
  
      /**
     * return value of Requested character
     */
      public char Requested_Char() {
          char ln = map.get("REQUEST");
          System.out.println(ln + " ...................................................");
          return ln;
      }
  
      /**
     * return value of Piece
     */
      public char Piece_Char() {
          char ln = map.get("PIECE");
          System.out.println(ln + " ...................................................");
          return ln;
      }
  
      /**
     * return value of Have
     */
      public char Have_Char() {
          char ln = map.get("HAVE");
          System.out.println(ln + " ...................................................");
          return ln;
      }
  }