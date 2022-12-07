import java.io.*;
import java.util.*;
import java.text.*;
import java.net.*;
import java.nio.*;
import java.util.concurrent.*;
import java.util.Map.*;



/*
* Define the main Class PeerProcess
* */
public class peerProcess {


  static Types_of_Messages Types_of_Messages;
  static Read_Common_config Config_Reader;
  static Data_of_Common_config Data_of_Common_config;
  static LinkedHashMap<Integer, Peer_Node> Map_Peer;
  static int Cur_Node_Id;
  static Peer_Node Current_Node;
  static byte[][] Current_Chunk_File;
  static int Complete_Peer_Files = 0;
  static File Current_Node_Dir;
  private static ConcurrentHashMap<Integer, Adjacent_Connection_Node> Map_Connections;
  private static File File_Log;
  private static String File_Format;
  private static String Main_File_Name;

  private static class Background_Main_Thread extends Thread {
    private Adjacent_Connection_Node peer;

    public Background_Main_Thread(Adjacent_Connection_Node peer)
    {
      this.peer = peer;
    }
    
    public void Show_Downloaded_Data() {
    	int Till_Now_Chunks = Current_Node.Get_Num_Of_Pieces();
    	int Total_Chunks = Data_of_Common_config.Total_Num_Of_Chunks();
        double Percentage_Of_Downloaded_Data = ((Till_Now_Chunks * 100.0)/Total_Chunks);
        String output = Till_Now_Chunks + "/" + Total_Chunks + " downloaded: " + Percentage_Of_Downloaded_Data +"% ";
        System.out.println(output);
    }

    @Override
    public void run() {
      synchronized (this) {

        try {

          DataInputStream dataInputStream = new DataInputStream(peer.Get_Connection().getInputStream());
          System.out.println("Sending bit field message ... ");
          peer.Bit_Field_Message_Send();
          int ccc = 0;
          BufferedWriter writer = new BufferedWriter(new FileWriter(File_Log.getAbsolutePath(), true));

          while (Complete_Peer_Files < Map_Peer.size()) {
        	  
            int Message_Length = dataInputStream.readInt();
            
            byte[] Message = null, Input_Message = null;
            boolean vvv = true;
            if (vvv == true) {
                Message = new byte[Message_Length - 1];
                Input_Message = new byte[Message_Length];
            }
            double Begin_Time = (System.nanoTime() / 100000000.0);
            dataInputStream.readFully(Input_Message);
            double finish_Time = (System.nanoTime() / 100000000.0);
            
            char Message_Type = (char) (Input_Message[0]);
            {
              int Key_Index = 0;
              int a;
              for (a = 1; a < Message_Length; a++) {
                Message[Key_Index++] = Input_Message[a];
              }
            }
            writer.flush();

            if (Message_Type == Types_of_Messages.Bit_Field_Char()) {
               int key = 0;
               int a;
              int[] Bit_Field = new int[Message.length / 4];
              for (a = 0; a < Message.length; a += 4) { // 
                byte[] Temp_Byte_Array = Helper_Functions.Array_Byte_Copy(Message, a, a + 4);
                Bit_Field[key++] = ByteBuffer.wrap(Temp_Byte_Array).getInt();
              }

              /*
               * update the Bit_Field and chunks
               */
              Peer_Node Peer_Object_Connected = Map_Peer.get(peer.Get_Peer_ID());
              Peer_Object_Connected.Update_Marker_Chunks (Bit_Field);
              int Cur_Peer_Chunks = Peer_Object_Connected.Get_Stored_Chucks();

              if (Cur_Peer_Chunks == Current_Node.Get_Chunks_Length()) {
                boolean result = Peer_Object_Connected.Update_Has_File(1);
                Complete_Peer_Files++;
              } else {
                boolean result = Peer_Object_Connected.Update_Has_File(0);
              }

              boolean Check_Missing_Chunks = Helper_Functions.Check_Missing_Chunks(Current_Node.Get_Bit_Field(),
                  Peer_Object_Connected.Get_Bit_Field(), Peer_Object_Connected.Get_Chunks_Length());
              
              if (Check_Missing_Chunks) {
                /*
                 * Tells I want message from this connection
                 * */
                peer.Interested_Message_Send(); 
              } else {
//              /*
//              * Tells I don't want anything from this connection
//              * */
                peer.Not_Interested_Message_Send() ;
              }

            } else if (Message_Type == Types_of_Messages.Interested_Char()) {
              /*
               * peer is interested to taking 
               */
              peer.Interested_Fetch();
              Helper_Functions.Log(writer, Current_Node.Get_Peer_ID(), peer.Peer_ID, "INTERESTED");

            } else if (Message_Type == Types_of_Messages.Not_Interested_Char()) {
              /*
               * peer is not interested to taking
               */
              peer.Not_Interested_Put();
              Helper_Functions.Log(writer, Current_Node.Get_Peer_ID(), peer.Peer_ID, "NOTINTERESTED");

              /*
               * CHOKED status
               */
              if (!peer._Is_Choked()) {
                /*
                 *  peer is not choked 
                 */
                peer.Connection_Choked();
                /*
                 * Choked It
                 */
                peer.Choke_Message_Send();
              }
            } else if (Message_Type == Types_of_Messages.UNChoke_Char()) {
              peer.Connection_UNChoked();
              Helper_Functions.Log(writer, Current_Node.Get_Peer_ID(), peer.Peer_ID, "UNCHOKE");

              System.out.println(peer.Get_Peer_ID() + " is unchoked on Receiver side");
              /*
                Request for piece has been send from sender now.
               */
              Peer_Node Peer_Object_Connected = Map_Peer.get(peer.Get_Peer_ID());

              int Chunk_Random_Index = Helper_Functions.Random_Chunk_File(Current_Node.Get_Bit_Field(),
                  Peer_Object_Connected.Get_Bit_Field(), Peer_Object_Connected.Get_Chunks_Length());

              if (Chunk_Random_Index == -1) {
               /*
                * Maybe there is no file, Nothing found
                */
                System.out.println(peer._Is_Choked() + " <-----> " + peer.Is_Interested());
              } else {
                peer.Request_Message_Send(Chunk_Random_Index);
              }

            } else if (Message_Type == Types_of_Messages.Requested_Char()) {
              int Chunk_File_Index = ByteBuffer.wrap(Message).getInt();
              peer.Piece_Message_Send(Chunk_File_Index);
            } else if (Message_Type == Types_of_Messages.Piece_Char()) {
              System.out.println("PIECE Receive  from " + peer.Get_Peer_ID());

              {
            	int Receiver_Chunk_File_Index = ByteBuffer.wrap(Helper_Functions.Array_Byte_Copy(Message, 0, 4)).getInt();
                Peer_Node Peer_Object_Connected = Map_Peer.get(peer.Get_Peer_ID());
                Current_Chunk_File[Receiver_Chunk_File_Index] = new byte[Message.length - 4];

                int b = 0;
                int a;
                for (a = 4; a < Message.length; a++) {
	                byte[] Current_File_Row = Current_Chunk_File[Receiver_Chunk_File_Index];
	                Current_File_Row[b++] = Message[a];
                }
                
                Current_Node.Update_Bit_Field(Receiver_Chunk_File_Index);
                
                Current_Node.Update_Num_Of_Pieces();
                
                if (peer._Is_Choked() == false) {

                  int Chunk_Random_Index = Helper_Functions.Random_Chunk_File(Current_Node.Get_Bit_Field(),
                      Peer_Object_Connected.Get_Bit_Field(), Peer_Object_Connected.Get_Chunks_Length());

                  if (Chunk_Random_Index == -1) {
                    /*
                     * Nothing found,maybe there is no file to Fetch
                     */

                  } else {
                    /*
                      RECEIVER request for the another chunk after Unchoking
                     */
                    peer.Request_Message_Send(Chunk_Random_Index);
                  }
                }

                double Val = ((double) (Message.length + 5) / (finish_Time - Begin_Time));
                int Has_File = Peer_Object_Connected.Get_Has_File();
                if (Has_File == 1) {
                	/*
                	 * The file is completed
                	 */
                  peer.Download_Speed_Put(-1);
                } else {
                  peer.Download_Speed_Put(Val);
                }
                Helper_Functions.Download_Pieces_Log(writer, Current_Node.Get_Peer_ID(), peer.Get_Peer_ID(),
                Receiver_Chunk_File_Index, Current_Node.Get_Num_Of_Pieces());
                Show_Downloaded_Data();
                peer.Completed_Check(Receiver_Chunk_File_Index);
                /*
                 * Now check for have Message 
                 */
                for (int connect: Map_Connections.keySet()) {
                  Adjacent_Connection_Node pac = Map_Connections.get(connect);
                  pac.Have_Message_Send(Receiver_Chunk_File_Index);
                }
              }
            } else if (Message_Type == Types_of_Messages.Have_Char()) {
              /*
               * When server get HAVE message
               */
              int Chunk_Have_Index = ByteBuffer.wrap(Message).getInt();
              Peer_Node Peer_Object_Connected = Map_Peer.get(peer.Get_Peer_ID());
              Peer_Object_Connected.Update_Bit_Field(Chunk_Have_Index);
              int Bits_In_total = Peer_Object_Connected.Get_Stored_Chucks();
              if (Bits_In_total == Current_Node.Get_Chunks_Length()) {
                Peer_Object_Connected.Have_File_Set(1);
                Complete_Peer_Files++;
              }

              {
                boolean Check_Missing_Chunks = Helper_Functions.Check_Missing_Chunks(Current_Node.Get_Bit_Field(),
                    Peer_Object_Connected.Get_Bit_Field(), Peer_Object_Connected.Get_Chunks_Length());
                /*
                 * Check for Missing Chunks
                 */
                if (Check_Missing_Chunks) {
                  /*
                   * Missing Chuck found 
                   * Sending the Interested Message
                   */
                  peer.Interested_Message_Send();
                } else {
                  /*
                   * Sending the Not Interested Message
                   */
                  peer.Not_Interested_Message_Send() ;
                }
              }
              Helper_Functions.Have_Log_Receive(writer, Current_Node.Get_Peer_ID(), peer.Get_Peer_ID(), Chunk_Have_Index);
            } else if (Message_Type == Types_of_Messages.Choke_Char()) {
              Helper_Functions.Log(writer, Current_Node.Get_Peer_ID(), peer.Peer_ID, "CHOKE");
              peer.Connection_Choked();
              System.out.println("PEER is choked");
            } else {
              /*
               * Their is a problem
               */
              ccc++;
            }
          }
          /*
           * Peer get all the data
           */
         
          writer.close();
          Thread.sleep(5000);
          /*
           * System.exit(0);
           */
        } catch (Exception e) {
          
        }
      }
    }
  }

  private static class Adjacent_Connection_Node {
	  
    private Socket conn;
    private int Peer_ID;
    private boolean Is_Interested = false;
    private boolean _Is_Choked = true;
    private double Download_Rate = 0;
    private boolean Unchoked_Optimistically = false;

    public Adjacent_Connection_Node(Socket conn, int Peer_ID) {
      this.conn = conn;
      this.Peer_ID = Peer_ID;
      (new Background_Main_Thread(this)).start();

    }

    public double Download_Speed_Fetch() {
      return Download_Rate;
    }

    public void Download_Speed_Put(double Download_Rate) {
      this.Download_Rate = Download_Rate;
    }

    public boolean _Is_Choked() {
      return this._Is_Choked;
    }

    public void Connection_Choked() {
      this._Is_Choked = true;
    }

    public boolean Unchoked_Optimistically() {
      return Unchoked_Optimistically;
    }

    public void Connection_UNChoked() {
      this._Is_Choked = false;
    }

    public void Unchoke_Optimistically() {
    	Unchoked_Optimistically = true;
    }

    public void Choke_Optimistically() {
    	Unchoked_Optimistically = false;
    }

    public boolean Is_Interested() {
      return this.Is_Interested;
    }

    public void Not_Interested_Put() {
      this.Is_Interested = false;
    }

    public void Interested_Fetch() {
      this.Is_Interested = true;
    }

    public Socket Get_Connection() {
      return this.conn;
    }

    public int Get_Peer_ID() {
      return this.Peer_ID;
    }


    public byte[] Get_Chunk_File(int Piece_Index, byte[] piece) {
      int Key_Index = 0;
      int a;
      byte[] Payload_Value = new byte[4 + piece.length];

      byte[] Key_Bytes = ByteBuffer.allocate(4).putInt(Piece_Index).array();
      
      for (a=0;a < Key_Bytes.length;a++) {
		 Payload_Value[Key_Index++] = Key_Bytes[a];
      }
      for (a=0;a< piece.length;a++) {
	        Payload_Value[Key_Index++] = piece[a];
      }
      byte[] pac = null;
      try {
          pac =  Generate_Packet((5 + piece.length), Types_of_Messages.Piece_Char(), Payload_Value);
      } catch(Wrong_Packet_Exception ex) {
      	System.out.println(ex.getMessage());
      	System.exit(0);
      }
      return pac;
    }


    public byte[] Generate_Packet(int ln, char type, byte[] Payload_Value) throws Wrong_Packet_Exception {
      
      if (type == Types_of_Messages.Interested_Char() || type == Types_of_Messages.Not_Interested_Char() || type == Types_of_Messages.UNChoke_Char() || type == Types_of_Messages.Choke_Char()) {
          
          byte Message_Type = (byte) type;
          int Key_Index = 0;
          byte[] Packet_result = new byte[ln + 4];
          byte[] ln_Header = ByteBuffer.allocate(4).putInt(ln).array();
          for (int i=0;i < ln_Header.length;i++) {
        	byte x = ln_Header[i];
        	Packet_result[Key_Index++] = x;
          }
          Packet_result[Key_Index] = Message_Type;
          return Packet_result;
      } else if (type == Types_of_Messages.Bit_Field_Char() || type == Types_of_Messages.Requested_Char() || type == Types_of_Messages.Piece_Char() || type == Types_of_Messages.Have_Char()) {
          
          
          byte Message_Type = (byte) type;
          int count = 0;
          int a;
          byte[] Packet_result = new byte[ln + 4];
          byte[] ln_Header = ByteBuffer.allocate(4).putInt(ln).array();
          //for (byte x : length) {
          for (a=0; a < ln_Header.length;a++) {
        	byte y = ln_Header[a];
        	Packet_result[count++] = y;
          }
          Packet_result[count++] = Message_Type;
          for ( a=0;a < Payload_Value.length;a++) {
        	byte x = Payload_Value[a];
        	Packet_result[count++] = x;
          }
          return Packet_result;
      } else {
    	  System.out.println("Find Different Type of packet.....");
    	  throw new Wrong_Packet_Exception("Wrong packet request " + type);
      }
    }

    
    public void Request_Message_Send(int chunkKey_Index) {
    	try {
            DataOutputStream connOutStream = new DataOutputStream(conn.getOutputStream());
            connOutStream.flush();
            
            byte[] Payload_Value = ByteBuffer.allocate(4).putInt(chunkKey_Index).array();
            byte[] Message_Request = null;
            try {
                Message_Request = Generate_Packet(5, Types_of_Messages.Requested_Char(), Payload_Value);
            } catch(Wrong_Packet_Exception ex) {
            	System.out.println(ex.getMessage());
            	System.exit(0);
            }
            connOutStream.write(Message_Request);
            connOutStream.flush();
    	}  catch (IOException e) {
    		System.out.println("error in sending request packets....");
    	} finally {
    		System.out.println("Request is completed....");
    	}
    }
    
    public void Piece_Message_Send(int Key_Index) {
    	
    	try {
    		
            DataOutputStream connOutStream = new DataOutputStream(conn.getOutputStream());
            //flush the stream..
            boolean NOT_Working = true;
            if (NOT_Working) {
                connOutStream.flush();
            }
            connOutStream.write(Get_Chunk_File(Key_Index, Current_Chunk_File[Key_Index]));
            connOutStream.flush();
            connOutStream.flush();
            
    	}  catch (IOException exception) {
    		System.out.println("error in sending piece packets");
    		
    	} finally {
    		System.out.println("Piece packets Send is completed....");
    	}
    	
    }
    
    public boolean Dummy_Fn() {
    	System.out.println("Thread reached here");
    	return true;
    }
    public void Have_Message_Send(int Piece_Index) {
    	
    	try {
            DataOutputStream connOutStream = null;
            /*
              scope checking here...
             */
            boolean vvv = Dummy_Fn();
            if (vvv == true)
            {
            	connOutStream = new DataOutputStream(conn.getOutputStream());
            }
            connOutStream.flush();
            byte[] Payload_Value = ByteBuffer.allocate(4).putInt(Piece_Index).array();
            byte[] Message_Have = null;
            try {
                Message_Have = Generate_Packet(5, Types_of_Messages.Have_Char(), Payload_Value);
            } catch(Wrong_Packet_Exception ex) {
            	System.out.println(ex.getMessage());
            	System.exit(0);
            }
            connOutStream.write(Message_Have);
            connOutStream.flush();
            
    	}  catch (IOException exception) {
    		System.out.println("error in message sending ");
    	} finally {
    		System.out.println(" have Message Send successfully...");
    	}
    }
    
    
    private boolean D_All(int a, int b, String c) {
    	if (c.equals("")) return true;
        int i = c.length();
        if (a < 0 || b >= i) return false;
        if (a > b) return false;
        while(a <= b) {
            if (! isD(c.charAt(a))) return false;
            a++;
        }
        return true;
    }
    
    private boolean isD(char c) {
        return c >= '0' && c <= '9';
    }
    
    public void Interested_Message_Send() {
    	/*
    	 * interested Message Send
    	 */
    	
    	try {
            DataOutputStream connOutStream = null;
            boolean asss = D_All(0, 0, "");
            if (asss == true){
                connOutStream = new DataOutputStream(conn.getOutputStream());
                connOutStream.flush();
            }

            byte[] Message_Interested = null;
            try {
                Message_Interested = Generate_Packet(1, Types_of_Messages.Interested_Char(), null);
            } catch(Wrong_Packet_Exception ex) {
            	System.out.println(ex.getMessage());
            	System.exit(0);
            }
            connOutStream.write(Message_Interested);
            
            connOutStream.flush();
            
    	} catch (IOException exception) {
    		System.out.println("typo in sending interested Msg");
    	} finally {
    		System.out.println("Interested Message Send successfully...");
    	}
    }

    
    public void Bit_Field_Message_Send() {
    	/*
    	 * Entered into Bit_Field to send Message
    	 */
    	 try{
    		// fetch conn stream
            DataOutputStream connOutStream = new DataOutputStream(conn.getOutputStream());
            // flush..
            connOutStream.flush();
            int[] Bit_Field = Current_Node.Get_Bit_Field();
            int New_Message_Length = 1 + (4 * Current_Node.Get_Bit_Field().length);
            byte[] Payload_Value = new byte[New_Message_Length - 1];
            int Key_Index = 0;
            int a,b;
            for ( a=0; a < Bit_Field.length; a++) {
            int num = Bit_Field[a];
              byte[] eachNumberByteArray = ByteBuffer.allocate(4).putInt(num).array();
	        	for ( b=0; b < eachNumberByteArray.length;b++) {
	        	byte Each_Byte = eachNumberByteArray[b];
	            Payload_Value[Key_Index++] = Each_Byte;
	          }
            }
            byte[] Bit_Field_Message = null;
            
            try {
                Bit_Field_Message = Generate_Packet(New_Message_Length, Types_of_Messages.Bit_Field_Char(), Payload_Value);
            } catch(Wrong_Packet_Exception ex) {
            	System.out.println(ex.getMessage());
            	System.exit(0);
            }
            connOutStream.write(Bit_Field_Message);
            connOutStream.flush();
    	} catch(IOException exception){
    		System.out.println("error in interested message sending");
    	} finally {
           System.out.println("sending interested Message...");
         }
    }
    
    public void Not_Interested_Message_Send() {
    	try {
            DataOutputStream connOutStream = new DataOutputStream(conn.getOutputStream());
            connOutStream.flush();
            byte[] Message_Not_Interested = null;
            
            try {
                Message_Not_Interested = Generate_Packet(1, Types_of_Messages.Not_Interested_Char(), null);
            } catch(Wrong_Packet_Exception ex) {
            	System.out.println(ex.getMessage());
            	System.exit(0);
            }
            
            connOutStream.write(Message_Not_Interested);
            connOutStream.flush();
    	} catch (IOException exception) {
    		System.out.println("error in not-interested message sending");
    	} finally {
          System.out.println("sending not-interested Message");
        }
    }
    public void Choke_Message_Send() {
    	try {
            DataOutputStream connOutStream = new DataOutputStream(conn.getOutputStream());
            connOutStream.flush();
            byte[] Choke_Message = null;
            
            try {
                Choke_Message = Generate_Packet(1, Types_of_Messages.Choke_Char(), null);
           } catch(Wrong_Packet_Exception ex) {
            	System.out.println(ex.getMessage());
            	System.exit(0);
            }
            
            connOutStream.write(Choke_Message);
            connOutStream.flush();

    	} catch (IOException exception) {
    		System.out.println("error in choke message sending");
    		
    	} finally {
    		System.out.println("sending Choke Message");
    	}
    }
    
    
    public void UnChoke_Message_Send() {
    	
    	try {
            DataOutputStream dataOutputStream = new DataOutputStream(conn.getOutputStream());
            dataOutputStream.flush();
            byte[] unChoke_Message = null;
            try {
                unChoke_Message = Generate_Packet(1, Types_of_Messages.UNChoke_Char(), null);
           } catch(Wrong_Packet_Exception ex) {
            	System.out.println(ex.getMessage());
            	System.exit(0);
            }
            dataOutputStream.write(unChoke_Message);
            dataOutputStream.flush();

    	} catch (IOException exception) {
    		System.out.println("error in sending unchoke message");
    	} finally {
    		System.out.println("sending unchoke Message");
    	}
    }

    private  void File_To_Write(String File_Final_Path, byte[] In_Bytes_New_File) throws IOException{
        /*
         * start writing
         */
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(File_Final_Path));
        bufferedOutputStream.write(In_Bytes_New_File);
        bufferedOutputStream.close();
      }
    
    public void Completed_Check(int Receiver_Chunk_File_Index) {
    	int Total_Pieces = 0;
	    int[] Bit_Fields_Curr_Node = Current_Node.Get_Bit_Field();
        int a;
    	{
    	      for (a=0;a < Bit_Fields_Curr_Node.length;a++) {
    	        if (Bit_Fields_Curr_Node[a] == 1) {
    	        	Total_Pieces += 1;
    	        }
    	      }
    	}
    	
      if (Total_Pieces == Bit_Fields_Curr_Node.length) {
        try { 
	        BufferedWriter writing = new BufferedWriter(new FileWriter(File_Log.getAbsolutePath(), true));
	        Helper_Functions.Completed_Downloading_Log(writing, Current_Node.Get_Peer_ID());
	        writing.close();
        } catch (IOException e1){
        	e1.printStackTrace();
        }
        int Temp_Index = 0;
        int tab = Data_of_Common_config.Get_File_size();
        byte[] In_Bytes_New_File = new byte[tab];
        int b,c;
        for ( b=0; b < Current_Chunk_File.length; b++) {
        	for ( c=0; c < Current_Chunk_File[b].length;c++) {
        		byte Current_Byte = Current_Chunk_File[b][c];
        		In_Bytes_New_File[Temp_Index++] = Current_Byte;
	          }
        }
        try {
          Random random = new Random();
          //int i = random.nextInt(5000000);
          String File_Final_Path = Config_Reader.Get_Path_Of_Root() + "/peer_" + Cur_Node_Id + "/" + (Main_File_Name + "." + File_Format);
          
          /*
           * Writing a file to Preferred folder with these bytes.
           */
          File_To_Write(File_Final_Path, In_Bytes_New_File);
          Current_Node.Download_File();
          Complete_Peer_Files += 1;

        } catch (Exception exception) {
          exception.printStackTrace();
        } finally {
        	System.out.println("writing file in folder is completed");
        }
      } else {
          int Temp_Index = 0;
          int Result_Size_of_Chunk = Data_of_Common_config.Get_Chuck_Size();
          byte[] In_Bytes_New_File = new byte[Result_Size_of_Chunk];
          int b;
          	for ( b=0; b < Current_Chunk_File[Receiver_Chunk_File_Index].length;b++) {
          		byte Current_Byte = Current_Chunk_File[Receiver_Chunk_File_Index][b];
          		In_Bytes_New_File[Temp_Index++] = Current_Byte;
  	      }
          
          System.out.println("Writing chunk to the folder.... " + Receiver_Chunk_File_Index);
          
          try {
              String File_Final_Path = Config_Reader.Get_Path_Of_Root() + "/peer_" + Cur_Node_Id + "/" + ("chunk_" + Main_File_Name + "_" + Receiver_Chunk_File_Index + "." + File_Format);
              File_To_Write(File_Final_Path, In_Bytes_New_File);

            } catch (Exception exception) {
              exception.printStackTrace();
            }
      }
    }
  }
  
  

  private static class Thread_Runnable_Server implements Runnable {
	
	  public boolean Is_Number(String c) {
	        if (c.isEmpty()) return false;
	        c = c.trim();
	        int k = c.length();
	        if (k == 0) return false;
	        if (k == 1) return isD(c.charAt(0));
	        int a = 0;
	        int b = k - 1;
	        int Dote = 0;
	        int Expe = 0;
	        int Sig = 0;
	        for(a = 0; a < k; a++) {
	            char s = c.charAt(a);
	            if (s == '.') {
	                Dote++;
	            }
	            else if (s == 'e') {
	                Expe++;
	            }
	            else if (Is_Sig(s)) {
	                Sig++;
	            } else {
	                if (! isD(s)) return false;
	            }
	            
	        }
	        if (Expe > 1 || Dote > 1 || Sig > 2) return false;
	        if (Expe == 1) {
	            a = c.indexOf('e');
	            return Is_Expe(a+1, k-1, c) && Is_Decimal(0, a-1, c);
	        }
	        if (Dote == 1) {
	            return Is_Decimal(0, k-1, c);
	        }
	        /*
	         * only one leading Sigh allowed
	         */
	        if (Is_Sig(c.charAt(0))) return D_All(1, k-1,c);
	        return D_All(0, k-1, c);
	    }

	    
	    private boolean Is_Decimal(int a, int b, String c) {
	        int k = c.length();
	        if (a < 0 || b >= k) return false;
	        if (a > b) return false;
	        
	        if (Is_Sig(c.charAt(a))) return Is_Decimal(a+1, b, c);
	        
	        int m = a;
	        for(;m <= b; m++) if (c.charAt(m) == '.') break;
	        
	        if (m > b) return D_All(a, b, c);
	        if (m == a) return D_All(a+1, b, c);
	        if (m == b) return D_All(a, b- 1, c);
	        
	        return D_All(a, m-1, c) && D_All(m+1, b, c);
	    }
	    
	    
	    private boolean Is_Expe(int i, int j, String s) {
	        int n = s.length();
	        if (i < 0 || j >= n) return false;
	        if (i > j) return false;
	        
	        if (Is_Sig(s.charAt(i))) return D_All(i+1,j, s);
	        
	        return D_All(i, j, s);
	    }
	    
	    private boolean D_All(int a, int b, String c) {
	        int k = c.length();
	        if (a < 0 || b >= k) return false;
	        if (a > b) return false;
	        while(a <= b) {
	            if (! isD(c.charAt(a))) return false;
	            a++;
	        }
	        return true;
	    }
	    
	    private boolean isD(char s) {
	        return s >= '0' && s <= '9';
	    }
	    
	    private boolean Is_Sig(char s) {
	        return s == '-' || s == '+';
	    }  
	    
    @Override
    public void run() {
    	
	int S_Checks = 32;
	byte[] Buffer_Read = new byte[S_Checks];
      try {
    	int Current_Port = Current_Node.Get_Port_Number();
        ServerSocket serverSocket = new ServerSocket(Current_Port);
        int Connection_Map_Len = Map_Connections.size();
        System.out.println("given size " + Connection_Map_Len);
        int Peer_Map_Len = Map_Peer.size();
        System.out.println("present connection " + Connection_Map_Len);
        int a;
        for ( a=0; Connection_Map_Len < Peer_Map_Len - 1;) {
          a++;
          System.out.println("Establishing the connection");
          Socket conn = serverSocket.accept();

          DataInputStream Input_Stream_Conn = new DataInputStream(conn.getInputStream());
          System.out.println("Read the conn");
          Input_Stream_Conn.readFully(Buffer_Read);
          StringBuilder Message_Hand_Shake = new StringBuilder();
          byte[] Buffer_HandShake_Temp = Helper_Functions.Array_Byte_Copy(Buffer_Read, 0, 28);
          Message_Hand_Shake.append(new String(Buffer_HandShake_Temp));
          byte[] Buffer_ID_Temp = Helper_Functions.Array_Byte_Copy(Buffer_Read, 28, 32);
          int Peer_ID = ByteBuffer.wrap(Buffer_ID_Temp).getInt();
          
          try {
              BufferedWriter writer = new BufferedWriter(new FileWriter(File_Log.getAbsolutePath(), true));
              Helper_Functions.Log(writer, Cur_Node_Id, Peer_ID, "From connection");
              writer.close();
          } catch(IOException e) {
        	  System.out.println("In Server Thread there is an error in write to logs ");
          }

          Message_Hand_Shake.append(Peer_ID);
          Is_Number("");
          System.out.println("on server side Handshake Message  : " + Message_Hand_Shake.toString());
          boolean value = true;
          if (value) {
            char ch = 'd';
            isD(ch);
          }
          DataOutputStream Output_Stream_Connection = new DataOutputStream(conn.getOutputStream());
          
          byte[] Packet_Of_HandShake_Generated = Helper_Functions.Handshake_Packet_Generate(Cur_Node_Id);
          Output_Stream_Connection.flush();
          Output_Stream_Connection.write(Packet_Of_HandShake_Generated);

          Map_Connections.put(Peer_ID, new Adjacent_Connection_Node(conn, Peer_ID));
          
          Connection_Map_Len = Map_Connections.size();
          Peer_Map_Len = Map_Peer.size();
          
        }

      } catch (IOException exception) {
    	  exception.printStackTrace();
      } finally {
    	  System.out.println("Complete successfully");
    	  
      }
      
    }
    
  }

  private static class Thread_Runnable_Client implements Runnable {
	   
	  
	    @Override
	    public void run() {
	      try {
	        byte[] Pipe_Response;
	        for (Entry<Integer, Peer_Node> entry : Map_Peer.entrySet()) {
	          int key_Val = entry.getKey();
	          if (key_Val == Cur_Node_Id) {
	            break;
	          }
	          System.out.println("Client:" + key_Val);
	          Peer_Node Peer_Connection = Map_Peer.get(key_Val);
	          
	          String Host_Name = Peer_Connection.Get_Host_Name();
	          int Port_Num = Peer_Connection.Get_Port_Number();
	          Socket clientSocket = new Socket(Host_Name, Port_Num);
	          System.out.println("Client: " + key_Val + " Socket created Now Connecting to Server: " + Peer_Connection.Get_Host_Name()
	              + " with " + Peer_Connection.Get_Port_Number());
	          DataOutputStream Pipe_Output = new DataOutputStream(clientSocket.getOutputStream());
	          Pipe_Output.flush();
	          {
	            Pipe_Output.flush();
	            byte[] HandShakePacketForServerGenerated = Helper_Functions.Handshake_Packet_Generate(Cur_Node_Id);
	            Pipe_Output.write(HandShakePacketForServerGenerated);
	            System.out.println("Client: " + key_Val + " For Connecting to Server Handshake packet sent: "
	                + Peer_Connection.Get_Host_Name() + " with " + Peer_Connection.Get_Port_Number());
	          }
	          Pipe_Output.flush();
	          DataInputStream Pipe_Input = new DataInputStream(clientSocket.getInputStream());
	          Pipe_Response = new byte[32];
	          Pipe_Input.readFully(Pipe_Response);
	          byte[] Buffer_Temp = Helper_Functions.Array_Byte_Copy(Pipe_Response, 28, 32);
	          int Peer_id = ByteBuffer.wrap(Buffer_Temp).getInt();
	          if (Peer_id == key_Val) {
	            
	          try {
	                BufferedWriter writer = new BufferedWriter(new FileWriter(File_Log.getAbsolutePath(), true));
	                Helper_Functions.Log(writer, Cur_Node_Id, key_Val, "connection_To");           
	                writer.close();
	          } catch (IOException exx) {
	            System.out.println("error to running Logs operation ");
	            
	          }
	              StringBuilder HandShake_generated = new StringBuilder("");
	              Buffer_Temp = Helper_Functions.Array_Byte_Copy(Pipe_Response, 0, 28);
	              HandShake_generated.append(new String(Buffer_Temp));
	              HandShake_generated.append(Peer_id);
	              Map_Connections.put(key_Val, new Adjacent_Connection_Node(clientSocket, key_Val));
	              System.out.println("Client: " + key_Val + " From Server: Handshake packet received " + Peer_Connection.Get_Host_Name()
	                  + " with " + Peer_Connection.Get_Port_Number() + " Packet = " + HandShake_generated.toString() + " Add to "
	                  + " connections Updated" + Map_Connections.size() + "/" + (Map_Peer.size() - 1)
	                  + " connections till now");
	            } else {
	              clientSocket.close();
	          }
	        }
	        
	      } catch (IOException exception) {
	        
	        exception.printStackTrace();
	        
	      } finally {
	      System.out.println("successfully execute now exit ");
	      }
	    }
	  }



  /*
   * Initialize functions for Common Config File
   */
  public static void Common_Config_File_Initializing() throws IOException {
    ArrayList<String> Common_Config_File_Parse = Config_Reader.Parse_Com_Config_File("Common.cfg");
    Data_of_Common_config.UpdateData_and_Parse(Common_Config_File_Parse);
  }
  /*
   * Initialize functions for PeerInfo Config File
   */
  public static void PeerInfo_Config_File_Initializing() throws IOException {
    ArrayList<String> PeerInfo_Config_File_Parse = Config_Reader.Parse_Com_Config_File("PeerInfo.cfg");
    for (String key : PeerInfo_Config_File_Parse) {
      Peer_Node peer_node = new Peer_Node();
      int Peer_ID = peer_node.Peer_Object_Initializing(key);
      Map_Peer.put(Peer_ID, peer_node);
    }
  }

  public static void initializeComplete_Peer_Files() {
    Complete_Peer_Files = 0;
  }
  /*
   * Initialize functions for Peer Directory
   */
  public static void Peer_Directory_Initialize() {
    Current_Node_Dir = new File("peer_" + Cur_Node_Id);
    if (!Current_Node_Dir.exists()) {
      Current_Node_Dir.mkdir();
    }
  }
  /*
   * Initialize functions for File Chunks
   */
  public static byte[][] File_Chunks_Initialize(int S_chunk) {
    Current_Chunk_File = new byte[S_chunk][];
    return Current_Chunk_File;
  }
  /*
   * Writing a chucks file 
   * */
  public static void File_Chunks_Write() throws IOException {
    int File_Size = Data_of_Common_config.Get_File_size(), Size_of_Chunk = Data_of_Common_config.Get_Chuck_Size();
    String To_Path = Config_Reader.Get_Path_Of_Root() + Constants.Get_Transfer_File();
    FileInputStream fr = new FileInputStream(To_Path);
    BufferedInputStream fl = new BufferedInputStream(fr);
    byte[] Bytes_File = new byte[File_Size];
    
    /*
     * Read the file 
     */
    fl.read(Bytes_File);

    /*
     * close the file
     */
    fl.close();

    {
      int cnt = 0, Key_Index = 0;
      for (; cnt < File_Size;) {
        if (cnt + Size_of_Chunk <= File_Size)
          Current_Chunk_File[Key_Index] = Helper_Functions.Array_Byte_Copy(Bytes_File, cnt, cnt + Size_of_Chunk);
        else
          Current_Chunk_File[Key_Index] = Helper_Functions.Array_Byte_Copy(Bytes_File, cnt, File_Size);
        Current_Node.Update_Num_Of_Pieces();
        cnt += Size_of_Chunk;
        Key_Index++;
      }
    }
  }
  /*
   *  For dividing the file in parts creating helper functions
   */
  public static void Chunks_Calculate() throws IOException {
    int File_Size = Data_of_Common_config.Get_File_size(), Size_of_Chunk = Data_of_Common_config.Get_Chuck_Size();

    double S_chunks = File_Size * 1.0 / Size_of_Chunk;
    int Chunks_Round_Off = (int) (Math.ceil(S_chunks));
    Current_Chunk_File = File_Chunks_Initialize(Chunks_Round_Off);
    /*
     * Checking number of received chunks in the file is signify or not .
     */
    int[] Marker_Chunks  = new int[Chunks_Round_Off];
    /*
     * checking for file already exists in the peer or not.
     */
    boolean Is_Complete = Current_Node.Has_File();
    if (!Is_Complete) {
      int a;
      for ( a = 0; a < Marker_Chunks .length; a++) {
        Marker_Chunks [a] = 0;
      }
      Current_Node.Get_Marker_Chunks (Marker_Chunks );
    } else {
      Complete_Peer_Files++;
      /*
       * When we get the entire file set all chunks to one.
       */
      int a;
      for (a = 0; a < Marker_Chunks .length; a++) {
        Marker_Chunks [a] = 1;
      }
      Current_Node.Get_Marker_Chunks (Marker_Chunks );
      /*
      * Start writing the chuck file
      * */
      File_Chunks_Write();

    }
  }
  

  public static void Set_FileName_And_Format() {
	  String File_Name = Constants.Get_Transfer_File();
	  String[] IP_s = File_Name.split("\\.");
	  File_Format = IP_s[1];
	  Main_File_Name = IP_s[0];
  }

  public static void main(String[] args) throws IOException {

    /*
     * For messages Type and Format
     */
	Types_of_Messages = new Types_of_Messages();
	Set_FileName_And_Format();

    /*
     * config file reader
     */
    Config_Reader = new Read_Common_config();

    /*
     * common config data
     */
    Data_of_Common_config = new Data_of_Common_config();

    /*
     * initialize common config File
     */
    Common_Config_File_Initializing();
    Data_of_Common_config.printIt();

    /*
     * peerInfo config hash map
     */
    Map_Peer = new LinkedHashMap<>();

    /*
     * initialize PeerInfo config File
     */
    PeerInfo_Config_File_Initializing();

    System.out.println(Map_Peer);

    /*
     * receiving Host
     */
    Cur_Node_Id = Integer.parseInt(args[0]);
    Current_Node = Map_Peer.get(Cur_Node_Id);

    /*
     * directory creating 
     */
    Peer_Directory_Initialize();

    /*
     * logs creating 
     */
    File_Log = new File(System.getProperty("user.dir") + "/" + "log_peer_" + Cur_Node_Id + ".log");
    if (File_Log.exists() == false)
      File_Log.createNewFile();
    
    /*
     * MAP Creating 
     */
    Map_Connections = new ConcurrentHashMap<>();


    /*
     * dividing the files_into_Chunks and if the file is completed then execute write
     */
    Chunks_Calculate();

    /*
    * Define Threads
    * */
    Thread Thread_Client = new Thread(new Thread_Runnable_Client());
    Thread Thread_Server = new Thread(new Thread_Runnable_Server());
    Thread Upper = new Thread(new Upper_Runnable());
    Thread OUpper = new Thread(new OUpper_Runnable());

    Thread_Client.start();
    Thread_Server.start();
    Upper.start();
    OUpper.start();

  }
  

  static class Comparator_Value implements Comparator<Integer> {

    Map<Integer, Double> Base_Value;
    public Comparator_Value(Map<Integer, Double> Base_Value) {
        this.Base_Value = Base_Value;
    }
  
    public int compare(Integer i, Integer j) {
        if (Base_Value.get(i) >= Base_Value.get(j)) {
            return -1;
        } else {
            return 1;
        }
    }
  }
  private static class Upper_Runnable implements Runnable {
	 
	public List<Integer> Connection_Ids_Fetch() {
        List<Integer> Connection_IDs = new ArrayList<>();
        for (int key: Map_Connections.keySet()) {
        	Connection_IDs.add(key);
        }
        return Connection_IDs;
	}
    public List<Integer> Interested_Connection_Fetch(List<Integer> Connect) {
        List<Integer> Interested_Msg = new ArrayList<>();
        int a;
        for ( a=0;a < Connect.size();a++) {
        int connects = Connect.get(a);
        Adjacent_Connection_Node Connection_Temp = Map_Connections.get(connects);
          if (Connection_Temp.Is_Interested()) {
            Interested_Msg.add(connects);
          }
        }
        return Interested_Msg;
	}
	/*
	* Fetch Interested msg according to download Rate
	* */

	public List<Integer> Interested_Acc_To_Dload_Rate_Fetch(List<Integer> Connect) {
		List<Integer> Peer_Interested = new ArrayList<>();
		
        for (int Peers : Connect) {
            Adjacent_Connection_Node Connection_Object = Map_Connections.get(Peers);
            if (Connection_Object.Is_Interested() && Connection_Object.Download_Speed_Fetch() >= 0)
              Peer_Interested.add(Peers);
          }
        return Peer_Interested;
	}
	 
    @Override
    public void run() {
    	
      while (Complete_Peer_Files < Map_Peer.size()) {
        List<Integer> Connects = Connection_Ids_Fetch();
        
        /*
         * check for file is it available or not
         */
        if (Current_Node.Get_Has_File() == 1) {

        	/*
        	* Find the peer which has the file.
        	* */
          List<Integer> Peer_Interested = Interested_Connection_Fetch(Connects);
          System.out.println(Peer_Interested.size() + " <> This Peer Has File");
          
          if (Peer_Interested.size() <= 0) {
              System.out.println("No Interested Peer");
          } else {
        	int Preferred_Neighbours = Data_of_Common_config.Get_Pref_Num_of_Neighbours();
        	int Interested_Msg_Length = Peer_Interested.size();
            if (Interested_Msg_Length <= Preferred_Neighbours) {
                int a;
                for ( a=0; a < Interested_Msg_Length;a++) {
                	int Peers = Peer_Interested.get(a);
                	Adjacent_Connection_Node Connection_Temp = Map_Connections.get(Peers);
                  if (Connection_Temp._Is_Choked()) {
                    System.out.println("Sender Sending UNCHOKE Message to preferred Neighbours");
                    Connection_Temp.Connection_UNChoked();
                    Connection_Temp.UnChoke_Message_Send();
                  }
                }
              } else {

                System.out.println("More preferred Neighbours Present than Config");
                int Preferred_Neighbour_Size = Data_of_Common_config.Get_Pref_Num_of_Neighbours();
                int[] Preferred_Neighbors = new int[Preferred_Neighbour_Size];

                Random Random_Object = new Random();
                int a;
                for ( a = 0; a < Preferred_Neighbour_Size; a++) {
                int Random_Index = Math.abs(Random_Object.nextInt() % Peer_Interested.size());
                  Preferred_Neighbors[a] = Peer_Interested.remove(Random_Index);
                }
                
                for ( a=0;a < Preferred_Neighbour_Size;a++) {
                int Peer_ID_Temp = Preferred_Neighbors[a];
                  Adjacent_Connection_Node Connection_Object = Map_Connections.get(Peer_ID_Temp);
                  if (Connection_Object._Is_Choked() == true) {
                    // 
                    Connection_Object.Connection_UNChoked();
                    Connection_Object.UnChoke_Message_Send();
                  }
                }

                for ( a=0; a< Peer_Interested.size();a++) {
                int Peers = Peer_Interested.get(a);
                  Adjacent_Connection_Node Connection_Object = Map_Connections.get(Peers);
                  boolean Is_Choked_Connection = Connection_Object._Is_Choked(),
                          Is_UNChoked_Connection = Connection_Object.Unchoked_Optimistically();
                  if (Is_Choked_Connection == false && Is_UNChoked_Connection == false) { // What the it is
                    System.out.println("CHOKED WITH OPTIMISTICALLY " + Peers);
                    Connection_Object.Connection_Choked();
                    Connection_Object.Choke_Message_Send();
                  }
                }
                System.out.println("Preferred neighbours of Node" + Cur_Node_Id + " is " + Preferred_Neighbors);

                try {
                   BufferedWriter writer = new BufferedWriter(new FileWriter(File_Log.getAbsolutePath(), true));
                   Helper_Functions.Preferred_Neighbors_Log(writer, Current_Node.Get_Peer_ID(), Preferred_Neighbors);
                   writer.close();
                } catch (IOException e) {
	            	System.out.println("error with writing logs in UNCHOCKING PEER");
	            	e.printStackTrace();
                }
              }
          }
        } else {
          List<Integer> Peer_Interested = Interested_Acc_To_Dload_Rate_Fetch(Connects);
          int Preferred_Neighbour_Size = Data_of_Common_config.Get_Pref_Num_of_Neighbours();
          if (Peer_Interested.size() <= Preferred_Neighbour_Size) {
            int a;
        	  for (a=0; a < Peer_Interested.size();a++) {
        	    int Peers = Peer_Interested.get(a);
              Adjacent_Connection_Node Connection_Object = Map_Connections.get(Peers);
              if (Connection_Object._Is_Choked() == false) {} 
              else {
                System.out.println("sending Available Pieces ");
                Connection_Object.Connection_UNChoked();
                Connection_Object.UnChoke_Message_Send();
              }
            }
          } else {
            int[] Preferred_Neighbors = new int[Preferred_Neighbour_Size];
            HashMap<Integer,Double> Maps = new HashMap<Integer,Double>();
            Comparator_Value bb =  new Comparator_Value(Maps);
            TreeMap<Integer,Double> Map_Sorted = new TreeMap<Integer,Double>(bb);
            int a,b,c;
            for( a=0; a<Peer_Interested.size();a++){
              Maps.put(Peer_Interested.get(a),Map_Connections.get(Peer_Interested.get(a)).Download_Speed_Fetch());
            }
            Map_Sorted.putAll(Maps);
            List<Integer> Peers_Sorted = new ArrayList<Integer>();
	        Peers_Sorted.addAll(Map_Sorted.keySet());
            for( b=0; b < Preferred_Neighbour_Size;b++){
              int Peers = Peers_Sorted.get(b);
              Preferred_Neighbors[b]= Peers;
              Adjacent_Connection_Node Connection_Temp = Map_Connections.get(Peers);
              if(Connection_Temp._Is_Choked()){
            	 Connection_Temp.Connection_UNChoked();
            	 Connection_Temp.UnChoke_Message_Send();
              }
              Peer_Interested.remove(Peers);
            }
            for( c=0; c<Peer_Interested.size();c++){
              int Peers = Peer_Interested.get(c);
              Adjacent_Connection_Node Connection_Temp = Map_Connections.get(Peers);
              
              if( Connection_Temp._Is_Choked() == false && Connection_Temp.Unchoked_Optimistically() == false){
            	  Connection_Temp.Connection_Choked();
            	  Connection_Temp.Choke_Message_Send();
              }
            }
            System.out.println("Preferred neighbours of node" + Cur_Node_Id + " is " + Preferred_Neighbors);
            try {
            	BufferedWriter writer = new BufferedWriter(new FileWriter(File_Log.getAbsolutePath(), true));
            	Helper_Functions.Preferred_Neighbors_Log(writer, Current_Node.Get_Peer_ID(), Preferred_Neighbors);
            	writer.close();
            } catch (IOException e) {
            System.out.println("error with writing logs in UNCHOCKING PEER");
              e.printStackTrace();
            }
          }
        }
        try {
          System.out.println("Thread sleeps");
          int Time_Unchoke = Data_of_Common_config.Unchoking_Interval();
          int Time_Unchoke_In_Seconds = Time_Unchoke * 1000;
          Thread.sleep(Time_Unchoke_In_Seconds);
        } catch (Exception exception) {
           exception.printStackTrace();
        } finally {
          System.out.println("Inner thread complete");
        } 
      }
      try {
      } catch (Exception exception) {
        exception.printStackTrace();
      } finally {
        System.out.println("Outer thread complete");
      }
      System.exit(0);
    }
  }

  private static class OUpper_Runnable implements Runnable {
	 
	public List<Integer> Connection_Ids_Fetch() {
        List<Integer> Connection_IDs = new ArrayList<>();
        for (int Keys: Map_Connections.keySet()) {
        	Connection_IDs.add(Keys);
        }
        return Connection_IDs;
  }
    /*start from here*/
	public List<Integer> Interested_Connection_Fetch(List<Integer> Connects) {
        List<Integer> Interested_Msg = new ArrayList<>();
        int a;
        for ( a=0; a < Connects.size();a++) {
        int connect = Connects.get(a);
        Adjacent_Connection_Node Connection_Temp = Map_Connections.get(connect);
          if (Connection_Temp.Is_Interested()) {
            Interested_Msg.add(connect);
          }
        }
        
        return Interested_Msg;
	}
	
    @Override
    public void run() {
      while (Complete_Peer_Files < Map_Peer.size()) {
    	
    	List<Integer> Connects = Connection_Ids_Fetch();
    	List<Integer> Interested_Msg = Interested_Connection_Fetch(Connects);
        
        
        if (Interested_Msg.size() > 0) {
          Random Random_Object = new Random();
          int Interested_Length = Interested_Msg.size();
          int Random_Index = Math.abs(Random_Object.nextInt() % Interested_Length);
          int Connection_Random_ID = Interested_Msg.get(Random_Index);
          
          Adjacent_Connection_Node Connection_Random = Map_Connections.get(Connection_Random_ID);
          Connection_Random.Connection_UNChoked();
          Connection_Random.UnChoke_Message_Send();
          Connection_Random.Unchoke_Optimistically();
          
          try {
        	BufferedWriter writer = new BufferedWriter(new FileWriter(File_Log.getAbsolutePath(), true));
            Helper_Functions.Log(writer, Current_Node.Get_Peer_ID(), Connection_Random.Get_Peer_ID(), "change Unchocked Optimistically Neighbor");
            writer.close();
          } catch (IOException e1) {
            System.out.println("Problem with adding Optimistically unchoked peer logs");
            e1.printStackTrace();
          }

          try {
            System.out.println("CHOKE THE CONNECTION When unchocking interval Sleeps");
            int Interval_Sleep = Data_of_Common_config.Optimistic_UnChocking_Interval();

            int Interval_Sleep_In_Secs = Interval_Sleep * 1000;
            /*
             * Thread Sleeps
             */
            Thread.sleep(Interval_Sleep_In_Secs);

            /*
             * Also Choke this connection.
             */
            Connection_Random.Choke_Optimistically();

          } catch (Exception exception) {
          } finally {
            System.out.println("Finish");
          }
        }
      }
      try {
        System.out.println("Optimistically Thread is sleeping Now");
        Thread.sleep(5000);
      } catch (Exception e) {

      }
      System.exit(0);
    }
  }
}

