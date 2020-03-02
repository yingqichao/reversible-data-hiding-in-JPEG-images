import java.util.List;

public class img {
	private static calctool Kit = new calctool();
	
	public static void main(String[] args) {
//		byte[] file;byte[] water;
//		// TODO Auto-generated method stub
//		
//			file = readStream("Lena");
//			water = readStream("Lena");}

		
		jpeglibrary a = new jpeglibrary("Ladybug300","water.txt",3);
		String foldname = "E:/";
		String hidePath = a.hide(foldname);
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
