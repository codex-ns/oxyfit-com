import java.util.Arrays;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;
public class CRC {	
    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
    public static void main(String args[]) {
      // Generate commands using input from terminal
      System.out.println("Command:");
      genCommand(Integer.parseInt(args[0]));


      // Generate time sync command
      System.out.println("Time - Command:");
      syncTime(Integer.parseInt(args[1]));

      System.out.println("Setting - Command:");
      String setting = args[2];
      String[] arrOfSetting = setting.split(":");
      updateSetting(arrOfSetting[0], Integer.parseInt(arrOfSetting[1]));
     
      System.out.println("FileStart - Command:");
      readFileStart(args[3]);

      System.exit(0);
    }
    public static void genCommand(int i) {
	byte [] byteArr;
	byte [] fourZ = {-86, (byte) i, (byte) (~i), 0, 0, 0, 0};
        byte [] twoZ = 	{-86, (byte) i, (byte) (~i), 0, 0, (byte) 1, (byte) 0, 0};
	byte[] rC = {-86, (byte) i, (byte) (~i), (byte) 0, (byte) (0 >> 8), 0, 0};
	if ( i == 4) {
		byteArr = Arrays.copyOf(rC, rC.length);
	} else if(i == 29 || i == 20 || i == 23 || i == 24 ){
		byteArr = Arrays.copyOf(fourZ, fourZ.length);
	} else {
		byteArr = Arrays.copyOf(twoZ, twoZ.length);
	}
        byte b = calCRC8(byteArr);
	byteArr = Arrays.copyOf(byteArr, byteArr.length + 1); // Increase size of byte array by 1
        byteArr[byteArr.length - 1] = b; // Add new byte to end of byte array
	
	byte te = calCRC8(byteArr);
	byteArr[byteArr.length - 1] = te;
	printMsg(byteArr);
    }
    public static final char[] Table_CRC8 =  {0, 7, 14, '\t', 28, 27, 18, 21, '8', '?', '6', '1', '$', '#', '*', '-', 'p', 'w', '~', 'y', 'l', 'k', 'b', 'e', 'H', 'O', 'F', 'A', 'T', 'S', 'Z', ']', 224, 231, 238, 233, 252, 251, 242, 245, 216, 223, 214, 209, 196, 195, 202, 205, 144, 151, 158, 153, 140, 139, 130, 133, 168, 175, 166, 161, 180, 179, 186, 189, 199, 192, 201, 206, 219, 220, 213, 210, 255, 248, 241, 246, 227, 228, 237, 234, 183, 176, 185, 190, 171, 172, 165, 162, 143, 136, 129, 134, 147, 148, 157, 154, '\'', ' ', ')', '.', ';', '<', '5', '2', 31, 24, 17, 22, 3, 4, '\r', '\n', 'W', 'P', 'Y', '^', 'K', 'L', 'E', 'B', 'o', 'h', 'a', 'f', 's', 't', '}', 'z', 137, 142, 135, 128, 149, 146, 155, 156, 177, 182, 191, 184, 173, 170, 163, 164, 249, 254, 247, 240, 229, 226, 235, 236, 193, 198, 207, 200, 221, 218, 211, 212, 'i', 'n', 'g', '`', 'u', 'r', '{', '|', 'Q', 'V', '_', 'X', 'M', 'J', 'C', 'D', 25, 30, 23, 16, 5, 2, 11, '\f', '!', '&', '/', '(', '=', ':', '3', '4', 'N', 'I', '@', 'G', 'R', 'U', '\\', '[', 'v', 'q', 'x', 127, 'j', 'm', 'd', 'c', '>', '9', '0', '7', '\"', '%', ',', '+', 6, 1, '\b', 15, 26, 29, 20, 19, 174, 169, 160, 167, 178, 181, 188, 187, 150, 145, 152, 159, 138, 141, 132, 131, 222, 217, 208, 215, 194, 197, 204, 203, 230, 225, 232, 239, 250, 253, 244, 243};
    public static byte calCRC8(byte[] bArr) {
        if (bArr == null || bArr.length == 0) {
            return (byte) 0;
        }
        byte b = 0;
        for (int i = 0; i < bArr.length - 1; i++) {
            b = (byte) Table_CRC8[(b ^ bArr[i]) & 255];
        }
        return b;
    }
    
     public static void syncTime(int time) {
	String format = new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss", Locale.getDefault()).format(new Date((long) time*1000));     
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("SetTIME", format);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sync(jSONObject);
    }
      public static void updateSetting(String str, int i) {
              JSONObject jSONObject = new JSONObject();
              try {
                  StringBuilder sb = new StringBuilder();
                  sb.append(i);
                  sb.append("");
                  jSONObject.put(str, sb.toString());
              } catch (JSONException e) {
                  e.printStackTrace();
              }
              sync(jSONObject);
          }

    public static void readFileStart(String str) {
        char[] charArray = str.toCharArray();
        int length = charArray.length + 1;
        int i = length + 8;
        byte[] bArr = new byte[i];
        bArr[0] = -86;
        int i2 = 3;
        bArr[1] = (byte) i2;
        bArr[2] = (byte) (~i2);
        bArr[5] = (byte) length;
        bArr[6] = (byte) (length >> 8);
        for (int i3 = 0; i3 < length - 1; i3++) {
            bArr[i3 + 7] = (byte) charArray[i3];
        }
        bArr[i - 1] = calCRC8(bArr);
        printMsg(bArr);
    }
    public static  void sync(JSONObject jSONObject) {
	System.out.println(jSONObject);
        char[] charArray = jSONObject.toString().toCharArray();
        int length = charArray.length;
        int i = length + 8;
        byte[] bArr = new byte[i];
        bArr[0] = -86;
        int i2 = 22;
        bArr[1] = (byte) i2;
        bArr[2] = (byte) (~i2);
        bArr[5] = (byte) length;
        bArr[6] = (byte) (length >> 8);
        for (int i3 = 0; i3 < length; i3++) {
            bArr[i3 + 7] = (byte) charArray[i3];
        }
        bArr[i - 1] = calCRC8(bArr);
        printMsg(bArr);
    }
   
    public static String bytesToHex(byte[] bytes){
    	char[] hexChars = new char[bytes.length * 2];
    	for (int j = 0; j < bytes.length; j++){
	    int v = bytes[j] & 0xff;
	    hexChars[j * 2] = HEX_ARRAY[v >>> 4];
	    hexChars[j * 2 +1] = HEX_ARRAY[v & 0x0f];
	}
	return new String(hexChars);
    }
   public static void printMsg(byte[] cmd){
      // Convert - use as input to device
      String hexCmd = bytesToHex(cmd);
      System.out.println(hexCmd);

   }
}
