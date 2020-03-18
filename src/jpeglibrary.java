import sun.awt.image.ImageWatched;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class jpeglibrary {
	private byte[] file;
	private byte[] water = null;
	private byte[] word = null;
	private byte[] kk = null;
	private byte[] dubious;
	private int[][] d2;
	private byte[] im;
	private byte[] head;
	private calctool T;
	private int[] size;
	private int block;
	private int[][] d0;
	private int[][] d1;
	private int[][] a0;
	private int[][] a1;
	private int[][] newa0 = null;
	private int[][] newa1 = null;
	private int[] treeSelect;
	private int[][] Q;
	private int DRI;
	private int color;
	private String imagePath;
	private String waterPath;
	private String hidePath;
	private String recoverPath = null;
	private String Description;
	private int key;
	private boolean changed = false;
	private int startOfHuff;
	private byte[] newHuff;
	private byte[] newHead;
	private static HideScheme2 hide;
	private byte[] doodle;
	private byte[] selfie;
	private byte[] aux;


	jpeglibrary(String imagePath,String waterPath,byte[] fromByteData,byte[] fromPhoto,String description,byte[] auxInfo,int c,int password,int[][] newA0,int[][] newA1) {
		long startTime=System.currentTimeMillis();
		newa0 = newA0;newa1 = newA1;
		T = new calctool();
		d2 = new int[3][30];
		size = new int[2];
		treeSelect = new int[3];
		Q = new int[3][2];
		Description = description;
		color=c;this.imagePath=imagePath;this.waterPath=waterPath;
		key = password;selfie = fromPhoto;doodle = fromByteData;aux = auxInfo;
		operation();
		long endTime=System.currentTimeMillis();
//		System.out.println("Preparation运行时间： "+(endTime-startTime)+"ms");
	}

	void init(String imagePath){
//		newa0 = newA0;newa1 = newA1;
//		T = new calctool();
		d2 = new int[3][30];
		size = new int[2];
		treeSelect = new int[3];
		Q = new int[3][2];
//		Description = description;
//		color=c;
		this.imagePath=imagePath;
//		key = password;selfie = fromPhoto;doodle = fromByteData;aux = auxInfo;
		operation();
	}

	private void operation() {
		int a;int b;
		if(doodle!=null){
			file = doodle;
		}
		else {
			try {
				file = readStream(imagePath);
			} catch (Exception e) {
				System.out.println("Original Does Not Exist!");
			}
		}
		cutX();
		cutdata();
		sof();
		a=(int)Math.ceil((double)size[0]/8);b=(int)Math.ceil((double)size[1]/8);
		block=a*b;
		hfm();
		if (color==3) {
			dri();
			sos();
		}//Selection of Huffman Tree for three color channels.
	}

	public String hide(String foldname,String saveAs){
		long startTime=System.currentTimeMillis();
		byte[] emb_im;int Qt = Q[0][0];
		hide = new HideScheme2(false,im,d0,d1,a0,a1,(newa0==null)?a0:newa0,(newa1==null)?a1:newa1,treeSelect,size,color,DRI,Qt,key);
		if(selfie!=null){
			water = selfie;
		}
		else if(waterPath != null){
			try{
				water = readStream(waterPath);}
			catch (Exception e){
				System.out.println("To-be-Embedded Image Does Not Exist!");
			}
		}
		if(Description != null){
			try{
				word  = Description.getBytes("gbk");
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		emb_im = hide.hideinfo(water,word,aux);

		if(changed){head=newHead;}
		dubious = new byte[head.length+emb_im.length+2];
		System.arraycopy(head, 0, dubious, 0, head.length);
		System.arraycopy(emb_im, 0, dubious, head.length, emb_im.length);
		long endTime=System.currentTimeMillis();
		System.out.println("嵌入信息总运行时间： "+(endTime-startTime)+"ms");

		hidePath = savePic(dubious,foldname,saveAs);

//		ExtractScheme exd = new ExtractScheme(hidePath,3,water);//water is just for proofreading!
//		List<String> res = exd.extract();
//		return res;
		return hidePath;
	}

	public List<String> extract(String hidePath,String foldname){
		int password = 0;
		init(hidePath);
		ExtractScheme exd = new ExtractScheme(hidePath,3);
		List<byte[]> list = exd.extract(password);
		byte[] emb_im = list.get(1);
		dubious = new byte[head.length+emb_im.length+2];
		System.arraycopy(head, 0, dubious, 0, head.length);
		System.arraycopy(emb_im, 0, dubious, head.length, emb_im.length);
		//恢复图像
		String path = savePic(dubious,foldname,"recover");
		byte[] extract = (byte[])list.get(0);
		int imgLen = toLength(Arrays.copyOfRange(extract,1,4));
		int wordLen = extract[4]&(0xff);int auxLen = extract[5]&(0xff);

		int Qt = Q[0][0];
		hide = new HideScheme2(true,im,d0,d1,a0,a1,(newa0==null)?a0:newa0,(newa1==null)?a1:newa1,treeSelect,size,color,DRI,Qt,key);
		emb_im = hide.hideinfo(water,word,aux);

		if(changed){head=newHead;}
		dubious = new byte[head.length+emb_im.length+2];
		System.arraycopy(head, 0, dubious, 0, head.length);
		System.arraycopy(emb_im, 0, dubious, head.length, emb_im.length);
		long endTime=System.currentTimeMillis();

		hidePath = savePic(dubious,foldname,"Extracted_new");

//		dubious = Arrays.copyOfRange(extract,6,6+imgLen);
//                hidePath[numIm] = SavePicIntoSystemAlbum(Arrays.copyOfRange(dubious, 0, dubious.length), "recover");
		byte[] wordExtract = Arrays.copyOfRange(extract,6+imgLen,6+imgLen+wordLen);
		String wordex = new String(wordExtract);

		List<String> ans = new LinkedList<>();
		//返回值：水印信息+恢复出来的原始图像保存路径
		ans.add(wordex);ans.add(path);
//		List<String> res = (List<String>)list.get(1);
		return ans;
	}

	public static byte[] readStream(String a) throws Exception{
//			String path = "C:\\Users\\yqc_s\\Desktop\\myjpeg0105final\\jpeg\\".concat(a).concat(".jpg");
		String path = a;
		FileInputStream fs = new FileInputStream(path);
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while (-1 != (len = fs.read(buffer))) {
			outStream.write(buffer, 0, len);
		}
		outStream.close();
		fs.close();
		return outStream.toByteArray();
	}

	private String savePic(byte[] b,String foldname,String filename) {
		String path = foldname.concat("\\").concat(filename).concat(".jpg");
		OutputStream out;
		try {
			out = new FileOutputStream(new File(path));
			out.write(b);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return path;
    }

	private void cutX(){
		int s = file.length;int k = 1;int i = 2;int j;
		d2[0][0]=byte2int(file[1]);d2[1][0]=0;d2[2][0]=1;
		while(d2[0][k-1]!=218){
			d2[1][k]=i;
			d2[0][k]=byte2int(file[i+1]);
			i=i+2+byte2int(file[i+2])*256+byte2int(file[i+3]);
			d2[2][k]=i-1;
			k=k+1;
		}
		for (j=s-1;j<i;j--){
			if((byte2int(file[j-1])==255)&&(byte2int(file[j])==217)) break;
		}
		d2[0][k]=217;d2[1][k]=i;d2[2][k]=j+1;
	}

	private void cutdata(){
		int a =getIndex(d2[0],218);
		int b =getIndex(d2[0],217);
		int a2;int b1=0;
		head = Arrays.copyOfRange(file, 0, d2[2][a]+1);
		byte[] imTmp = Arrays.copyOfRange(file, d2[1][b], d2[2][b]-2);//-2:delete the last EOI message.
		byte[] im2 = new byte[imTmp.length];
		int j=0;int i=0;//dynamically record the length of the revised sequence
		while(i<imTmp.length){
			if(i!=imTmp.length-1) {a2=imTmp[i+1];b1=byte2int(a2);}
			if((i!=imTmp.length-1)&&(byte2int(imTmp[i])==255)){
				switch(b1){
					case 0:im2[j]=imTmp[i];j++;i+=2;break;//保存255，跳过后面的0
					case 255:i++;//无保存，仅跳过255
					case 208:im2[j]=imTmp[i];j++;i++;break;//保存这个RST字段
					case 209:im2[j]=imTmp[i];j++;i++;break;
					case 210:im2[j]=imTmp[i];j++;i++;break;
					case 211:im2[j]=imTmp[i];j++;i++;break;
					case 212:im2[j]=imTmp[i];j++;i++;break;
					case 213:im2[j]=imTmp[i];j++;i++;break;
					case 214:im2[j]=imTmp[i];j++;i++;break;
					case 215:im2[j]=imTmp[i];j++;i++;break;
					default:im2[j]=imTmp[i+1];j++;i+=2;break;//跳过255，保存后面的非0/255
				}
			}
			else{
				im2[j]=imTmp[i];j++;i++;
			}
		}
		im=Arrays.copyOfRange(im2, 0, j);//delete zeros in the end of the sequence

	}

	private void sof(){
		int z = getIndex(d2[0],192);
		int i = d2[1][z];

		int ph=i+5;int pw=i+7;
		size[0] =  byte2int(head[ph])*256+byte2int(head[ph+1]);
		size[1] =  byte2int(head[pw])*256+byte2int(head[pw+1]);
		i += 11;//skip some unused letters
		for(int j=0;j<3;j++){
			int k = byte2int(head[i]);
			Q[j][0] = (k & 0xF0)/16;
			Q[j][1] = k & 0x0F;
			i += 3;
		}
	}

	private void hfm() {
		//可能有多个DHT字段,或者一个字段内有超过1张表
		List<Integer> res = findAll(d2[0],196);int thisLength=0;int totalOriLen = 0;int pointer;int pointerOrigin;boolean thisTreeNormal = true;
		int huffLength;int[][] huffmanTree = null;
		byte[] d0buff=null;byte[] d1buff=null;byte[] a0buff=null;byte[] a1buff=null;
		startOfHuff= res.get(0);
		for(int z=0;z<res.size();z++){
			int beginHuff= res.get(z);huffLength = 0;
			pointer = d2[1][beginHuff];pointerOrigin = d2[1][beginHuff]+2;//please follow the straight-forward moving of this pointer
			thisLength = byte2int(head[pointer+2])*256+byte2int(head[pointer+3]);
			totalOriLen += thisLength+2;//算上了树前的2byte标识位

			pointer += 4;
			while(huffLength<thisLength){
				int mode = byte2int(head[pointer]);pointer += 1;thisTreeNormal = true;
				int[] huff_num = new int[16];int[] newhuff_num = new int[16];int total=0;
				for(int i=0;i<16;i++){//码字总个数
					huff_num[i] = head[pointer+i];total+=huff_num[i];
				}
				//先判断交流树是否完整，不完整则直接使用保存下来的树
				if((mode==16 || mode==17) && (total<160 || changed)){
					changed = true;thisTreeNormal = false;

					if(mode==16)    huffmanTree = newa0;
					else    huffmanTree = newa1;
//					try{
//						huffmanTree = read("/data/data/"+String.valueOf(mode)+".txt");}
//					catch(Exception e) {
//						System.out.println("Error in loading huffman tree " + String.valueOf(mode));
//					}
					byte[] buff = new byte[huffmanTree[2].length+17];buff[0] = (byte)mode;
					for(int i=0;i<huffmanTree[0].length;i++){
						newhuff_num[huffmanTree[0][i]-1]++;
					}
					for(int i=0;i<16;i++){
						buff[i+1] = (byte)newhuff_num[i];
					}
					for(int i=0;i<huffmanTree[2].length;i++){
						buff[i+17] = (byte)huffmanTree[2][i];
					}
					if(mode==16){
//						newa0 = huffmanTree;
						a0buff=buff;
					}
					else{
//						newa1 = huffmanTree;
						a1buff=buff;
					}

//	    			pointer += (16+total);huffLength += (pointer-pointerOrigin);pointerOrigin = pointer;
				}
				pointer +=16;int codePointer=0;int code=0;
				//直接拷贝正常树的二进制内容
				if(thisTreeNormal){
					switch(mode){
						case(0):d0buff = Arrays.copyOfRange(head, pointer-17, pointer+total);break;
						case(1):d1buff = Arrays.copyOfRange(head, pointer-17, pointer+total);break;
						case(16):a0buff = Arrays.copyOfRange(head, pointer-17, pointer+total);;break;
						case(17):a1buff = Arrays.copyOfRange(head, pointer-17, pointer+total);break;}
				}
				huffmanTree = new int[3][total];
				for(int i=0;i<16;i++){
					if(i!=0){
						code *= 2;
					}
					for(int j=0;j<huff_num[i];j++){
						huffmanTree[0][codePointer]=i+1;
						huffmanTree[1][codePointer]=code;
						huffmanTree[2][codePointer]=byte2int(head[pointer+codePointer]);
						code++;codePointer++;
					}
				}
				huffLength += pointer + codePointer - pointerOrigin;pointer += codePointer;
				pointerOrigin = pointer;

				switch(mode){
					case(0):d0 = huffmanTree;break;
					case(1):d1 = huffmanTree;break;
					case(16):a0 = huffmanTree;break;
					case(17):a1 = huffmanTree;break;}
			}
		}
		if(changed){
			int totalLen = d0buff.length+d1buff.length+a0buff.length+a1buff.length+2;//没有算上标识位的2byte
			newHead = new byte[head.length+totalLen+2-totalOriLen];newHuff = new byte[totalLen+2];
			newHuff[0] = (byte)255;newHuff[1] = (byte)196;newHuff[2] = (byte) (totalLen/256);newHuff[3] = (byte)(totalLen-256*newHuff[2]);
			System.arraycopy(d0buff, 0, newHuff, 4, d0buff.length);
			System.arraycopy(d1buff, 0, newHuff, 4+d0buff.length, d1buff.length);
			System.arraycopy(a0buff, 0, newHuff, 4+d0buff.length+d1buff.length, a0buff.length);
			System.arraycopy(a1buff, 0, newHuff, 4+d0buff.length+d1buff.length+a0buff.length, a1buff.length);
			System.arraycopy(head, 0, newHead, 0, d2[1][startOfHuff]);
			System.arraycopy(newHuff, 0, newHead, d2[1][startOfHuff], newHuff.length);
			System.arraycopy(head, d2[1][startOfHuff+res.size()], newHead, d2[1][startOfHuff]+newHuff.length, head.length-d2[1][startOfHuff+res.size()]);
		}
	}

	private void dri(){
		int z = getIndex(d2[0],221);
		if(z!=-1){
			int pointer = d2[1][z];
			int len = byte2int(head[pointer+2])*256+byte2int(head[pointer+3]);

			DRI = byte2int(head[d2[1][z]+4])*256+byte2int(head[d2[1][z]+5]);}
	}

	private void sos(){
		int z = getIndex(d2[0],218);int a = d2[1][z];

		int pointer = d2[1][z]+6;
		for(int j=0;j<3;j++){
			treeSelect[j] = byte2int(head[pointer]);
			pointer += 2;
		}
	}

	//----------Tool Function Repository--------------
	//Latest revision:2018/10/5
	//Several functions from calctool.java has been deprecated and removed.


	private int toLength(byte[] in){
		int res = 0;
		for(int i=0;i<3;i++){
			res <<= 8;
			int tmp = in[i]&(0xff);
			res += tmp;
		}
		return res;
	}

	private int byte2int(int in){
		if(in<0) return 256+in;
		return in;
	}

	private int getIndex(int[] list,int in){
		for(int i=0;i<list.length;i++){
			if(list[i]==in)     return i;
		}
		return -1;
	}

	private List<Integer> findAll(int[] list,int in){
		List<Integer> res = new ArrayList<>();
		for(int i=0;i<list.length;i++){
			if(list[i]==in)	res.add(i);
		}
		return res;
	}
    
}
