package indi.cyh.jdbctool.tool;

import java.io.*;

/**
 * @ClassName FileTool
 * @Description TODO
 * @Author cyh
 * @Date 2020/12/8 0008 10:34
 */
public class FileTool {
   static  final   String encoding = "UTF-8";
    public static String readToString(String fileName) {
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }

    public static String readToString(InputStream inStream) {
        try {
            ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
            byte[] buffer = new byte[inStream.available()];
            int len = -1;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
            }
            outSteam.close();
            inStream.close();
            return  new String(buffer, encoding);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
