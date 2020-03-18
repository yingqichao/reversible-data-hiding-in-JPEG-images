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
		String waterPath = "anymark Co.Ltd";
		String foldname = "C:\\Users\\admin\\Desktop\\111";//将含密图像保存到这个目录下
		String saveAs = "111_water.jpg";//含密名称

		int[][] newA0 = null;int[][] newA1 = null;int password = 0;
		try {
			InputStream in = new FileInputStream("src\\16.txt");
			ObjectInputStream oin = new ObjectInputStream(in);
			newA0 = (int[][]) oin.readObject();
			in = new FileInputStream("src\\17.txt");
			oin = new ObjectInputStream(in);
			newA1 = (int[][]) oin.readObject();
		}catch(Exception e){
			System.out.println("[Error] Loading Self-defined Huffman Tree Failed!");
			e.printStackTrace();
		}


		jpeglibrary a = new jpeglibrary(imagePath,null,null,null,waterPath,null,3,password,newA0,newA1);
		//信息隐藏，得到含密图像
		String hidePath = a.hide(foldname,saveAs);
		//信息提取与图像恢复，恢复出的原始图像保存到foldname下
		List<String> result = a.extract(hidePath,foldname);

		String water = result.get(0);String image_path = result.get(1);
		System.out.println("------------------------");
		System.out.println("水印提取成功，水印信息： "+water);
		System.out.println("恢复图像保存路径为： "+image_path);

//		System.out.println("英文参考： "+result.get(0));
//		System.out.println("中文参考： "+result.get(1));
		
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
