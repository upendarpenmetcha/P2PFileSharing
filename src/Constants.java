import java.io.*;
import java.util.*;
import java.text.*;
import java.net.*;
import java.nio.*;
import java.util.concurrent.*;
import java.util.Map.*;

/**
 * Class of  Constants
 */
public class Constants {

    /**
    * storing the torrent file in the variable
    */
    private final static String Transfer_File_Name = "TheFile.dat";
  
    /**
     * storing the config.cfg file in the variable
     */
    private final static String Common_Cfg_File = "Common.cfg";
  
    /**
     * storing the peerInfo.cfg file in the variable
     */
    private final static String PeerInfo_Cfg_File = "PeerInfo.cfg";
    private final static String Header_Hand_Shake = "P2PFILESHARINGPROJ";
    private final static String Zeros = "0000000000";
    public static String Get_Header_Hand_Shake() {
      return Header_Hand_Shake;
    }
    public static String Get_Zeros() {
      return Zeros;
    }
    public static String Get_Transfer_File() {
      return Transfer_File_Name;
    }
    public static String Get_Common_Cfg_File() {
      return Common_Cfg_File;
    }
    public static String Get_PeerInfo_cfg_File() {
      return PeerInfo_Cfg_File;
    }
  }