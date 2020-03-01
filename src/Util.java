

import java.io.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {


    public static boolean isNumeric(String str){
        return str.matches("-?[0-9]+\\.?[0-9]*");
    }

    public static boolean isInteger(String str) {
//        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
//        return pattern.matcher(str).matches();
        return str.matches("^[-\\+]?[\\d]*$");
    }

    public static String toBinary(int num, int digits) {
        int value = 1 << digits | num;
        String bs = Integer.toBinaryString(value); //0x20 | 这个是为了保证这个string长度是6位数
        return  bs.substring(1);
    }


    public static String readWatermark(String filename){
        String str = null;
        try {
            FileReader fr = new FileReader(filename);
            BufferedReader bf = new BufferedReader(fr);

            str = bf.readLine();

            bf.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }


    public static byte[] StreamFromString(String str) {
        char[] strChar=str.toCharArray();
        int digits = (isContainChinese(str))?16:8;
        StringBuilder result=new StringBuilder();

        for(int i=0;i<strChar.length;i++){
            String tmp = Integer.toBinaryString(strChar[i]);
            for(int j=tmp.length();j<digits;j++)
                tmp = '0'+tmp;
            result.append(tmp);
        }
        if(!isContainChinese(str)&&result.length()/16!=0){
            while(result.length()%16!=0)
                result.append('0');
        }

        //现规定传过去的长度至少为1，也即2个中文，所以需要至少补到32位
        while(result.length()<32)
            result.append('0');

        byte[] res=new byte[result.length()];
        for(int i=0;i<result.length();i++)
            res[i] = (byte)(result.charAt(i)-'0');
        return res;
    }

    /**
     * 判断字符串中是否包含中文
     * @param str
     * 待校验字符串
     * @return 是否为中文
     * @warn 不能校验是否为中文标点符号
     */
    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    public static String dec2bin(int in,int digits){
        StringBuilder str = new StringBuilder();
        while(in!=0){
            str.append(in%2);
            in /= 2;
        }
        str.reverse();String res = str.toString();
        for(int i=str.length();i<digits;i++){
            res = "0" + res;
        }

        return res;


    }

    public static int bin2dec(String str){
        int i=0;
        for(char c:str.toCharArray()){
            i<<=1;
            i+=(c=='1')?1:0;

        }
        return i;
    }


}
