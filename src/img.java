import java.util.*;
import java.io.*;

public class img {
	private static calctool Kit = new calctool();
	
	public static void main(String[] args) {
//		byte[] file;byte[] water;
//		// TODO Auto-generated method stub
//		
//			file = readStream("Lena");
//			water = readStream("Lena");}

		String imagePath = "C:\\Users\\admin\\Desktop\\111\\123.jpg";//载体图像
		String waterPath = "C:\\Users\\admin\\Desktop\\111\\water.txt";
		String foldname = "C:\\Users\\admin\\Desktop\\111";//将含密图像保存到这个目录下
		String saveAs = "111_water.jpg";//含密名称

		int[][] newA0 = null;int[][] newA1 = null;
		try {
			InputStream in = new FileInputStream("16.txt");
			ObjectInputStream oin = new ObjectInputStream(in);
			newA0 = (int[][]) oin.readObject();
			in = new FileInputStream("17.txt");
			oin = new ObjectInputStream(in);
			newA1 = (int[][]) oin.readObject();
		}catch(Exception e){
			System.out.println("[Error] Loading Self-defined Huffman Tree Failed!");
		}


		jpeglibrary a = new jpeglibrary(imagePath,waterPath,null,null,null,null,3,0,newA0,newA1);
		//信息隐藏，得到含密图像
		String hidePath = a.hide(foldname,saveAs);
		//信息提取与图像恢复，恢复出的原始图像保存到foldname下
		List<String> result = a.extract(hidePath,foldname);

		System.out.println("------------------------");
		System.out.println("英文参考： "+result.get(0));
		System.out.println("中文参考： "+result.get(1));
		
//		StringBuilder a = new StringBuilder();
//		add(a);
//		System.out.println(a.toString());
//		byte[] compressedBytes= new byte[1024];
//		
//		JPEGLosslessDecoder decoder = new JPEGLosslessDecoder(compressedBytes);


//		char[] a = new char[2];int b=1;
//		char a=(char)1;
//		System.out.println(a);
//		ArrayList a = new ArrayList();
//		a.add(2);
//		System.out.println(a.size());
//		byte b = (byte) 247;
//        String s=  Kit.byteToBit(b);
//        
//        System.out.println((int)s.charAt(2));
	}
	
	public static void add(StringBuilder b){b.append('1');}
	

	

}
