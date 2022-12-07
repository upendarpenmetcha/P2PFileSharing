import java.io.*;
import java.util.*;
import java.text.*;
import java.net.*;
import java.nio.*;
import java.util.concurrent.*;
import java.util.Map.*;
/**
 * class For Reading Common Config file 
 */
public class Read_Common_config{

    String Path_Of_Root;
  
    public Read_Common_config() {
      Path_Of_Root = System.getProperty("user.dir");
      Path_Of_Root = Path_Of_Root.concat("/");
    }
  
    public ArrayList<String> Parse_Com_Config_File(String File_Name) throws IOException {
      String Path_of_File = Path_Of_Root.concat(File_Name);
      System.out.println(Path_of_File);
      FileReader A = new FileReader(Path_of_File);
      BufferedReader B = new BufferedReader(A);
      ArrayList<String> Row = new ArrayList<>();
      for (Object row : B.lines().toArray()) {
        Row.add((String) row);
      }
  
      B.close();
      return Row;
    }
  
    /**
     * return the path of root
     */
    public String Get_Path_Of_Root() {
      return this.Path_Of_Root;
    }
  }
  