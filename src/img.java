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

		String imagePath = "C:\\Users\\admin\\Desktop\\111\\123.jpg";//����ͼ��
		String waterPath = "anymark Co.Ltd";
		String foldname = "C:\\Users\\admin\\Desktop\\111";//������ͼ�񱣴浽���Ŀ¼��
		String saveAs = "111_water.jpg";//��������

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
		//��Ϣ���أ��õ�����ͼ��
		String hidePath = a.hide(foldname,saveAs);
		//��Ϣ��ȡ��ͼ��ָ����ָ�����ԭʼͼ�񱣴浽foldname��
		List<String> result = a.extract(hidePath,foldname);

		String water = result.get(0);String image_path = result.get(1);
		System.out.println("------------------------");
		System.out.println("ˮӡ��ȡ�ɹ���ˮӡ��Ϣ�� "+water);
		System.out.println("�ָ�ͼ�񱣴�·��Ϊ�� "+image_path);

//		System.out.println("Ӣ�Ĳο��� "+result.get(0));
//		System.out.println("���Ĳο��� "+result.get(1));
		
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
