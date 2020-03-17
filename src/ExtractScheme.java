import javax.print.DocFlavor;
import java.io.*;
import java.util.*;

public class ExtractScheme {
	private boolean firstMode = true;
	private byte[] file;
	private int[][] D0;
	private int[][] D1;
	private int[][] A0;
	private int[][] A1;
	private byte[] dubious = null;
	private int[][] d2;
	private byte[] im;
	private byte[] head;
	private calctool T;
	private int[] size;
	private int[] treeSelect;
	private int[][] Q;
	private int DRI;
	private int color;
	private String path;
	private int row;
	private int col;
	private int Bpp = 0;
	private int[][] zigZag;
	private int key;
	private StringBuilder temp = new StringBuilder();
	private StringBuilder image_temp = new StringBuilder();
	private String DCcode;
	private int[][] coeff;
	private int[] DC0 = new int[3];
	private Map<String,Integer> d0 = new HashMap<>();
	private Map<String,Integer> d1 = new HashMap<>();
	private Map<String,Integer> a0 = new HashMap<>();
	private Map<String,Integer> a1 = new HashMap<>();
	private Map<Integer,String> a0rev = new HashMap<>();
	private Map<Integer,String> a1rev = new HashMap<>();
	private int totalLen = 0;
	private boolean block1Extracted = false;
	private boolean block2Extracted = false;
	private boolean ex = false;
	private int mode;
	private int startCoeff = 18;
	private byte[] finale;

	ExtractScheme(String p,int c){
		T = new calctool();

		d2 = new int[3][30];
		size = new int[2];
		treeSelect = new int[3];
		Q = new int[3][2];
		color=c;path=p;
		operation();
		zigZag = T.iZigZag();
	}

	private void operation(){
		try{
			file = readStream(path);}
		catch (Exception e){
			System.out.println("File Does Not Exist!");
		}
		cutX();
		cutdata();
		sof();
		hfm();
		if (color==3) {//Selection of Huffman Tree for three color channels.
			dri();
			sos();
		}
		mapConstruction(d0,D0);
		mapConstruction(d1,D1);
		mapConstruction(a0,A0);
		mapConstruction(a1,A1);
		mapRevConstruction(a0rev,A0);
		mapRevConstruction(a1rev,A1);
	}

	public List<byte[]> extract(int password){
		//这里只返回状态代码给上级，用ex结束嵌入即可
		long startTime=System.currentTimeMillis();
		temp = new StringBuilder(400);int finale_pointer = 0;int finale_pointer_image = 0;int moveForward;
		int Qt = Q[0][0];key = password;
		int pointer = 0;DC0 = new int[3];
		boolean preprocess;
		int ref = (((color==3)?2:0)+ (int)Math.pow(Qt,2));
		row=(int)Math.ceil((double)size[0]/8/Qt);col=(int)Math.ceil((double)size[1]/8/Qt);

		for (int j = 0; j < row; j += 1) {
			if(Bpp!=0)  Bpp = (int)Math.ceil((double)(totalLen+8-finale_pointer)/(5*(row-j)*col*ref));
//			if(ex) break;
			for (int k = 0; k < col; k += 1) {
				//Y
//				if(ex) break;
				for(int zy=1;zy<=Math.pow(Qt,2);zy++){
//					if(ex) break;
					preprocess = ( DRI!=0 && (j*col+k)!=0 && (j*col+k)%DRI==0 && zy == 1);
					pointer = DCTread(pointer,0,preprocess);//great modification

					if(!(block1Extracted && block2Extracted && finale_pointer>=totalLen)) {
//						ex=true;break;
						//说明嵌入未完毕
						dataExtract();
						finale_pointer += dataWriter(finale_pointer);
					}
					//重编码以恢复原始图像
					Recode(image_temp,treeSelect[0],j+1,k+1,zy,preprocess);
					moveForward = dataWriterForOriginalImage(finale_pointer_image,preprocess);
					finale_pointer_image += moveForward;

				}
				//CrCb
				if(ex) break;
				if (color==3){
					//Cr Cb通道
					for (int zz=1;zz<=2;zz++){
						if(ex) break;
						pointer = DCTread(pointer,zz,false);//great modification

						if(!(block1Extracted && block2Extracted && finale_pointer>=totalLen)) {
//						ex=true;break;
							//说明嵌入未完毕
							dataExtract();

							finale_pointer += dataWriter(finale_pointer);
						}
						//重编码以恢复原始图像
						Recode(image_temp,treeSelect[0],j+1,k+1,zz,false);
						moveForward = dataWriterForOriginalImage(finale_pointer_image,false);
						finale_pointer_image += moveForward;

					}
				}
			}
		}

		//编码剩余的比特
		if(temp.length()!=0){
			int len = temp.length();
			for(int i=0;i<8-len;i++){
				temp.append('1');
			}
			finale_pointer += dataWriter(finale_pointer);
		}
		if(image_temp.length()!=0){
			int len = image_temp.length();
			for(int i=0;i<8-len;i++){
				image_temp.append('1');
			}
			finale_pointer_image += dataWriterForOriginalImage(finale_pointer_image,false);
		}

		finale[finale_pointer_image]=(byte)255;finale_pointer_image++;
		finale[finale_pointer_image]=(byte)217;finale_pointer_image++;
		byte[] ori_data = new byte[head.length+finale.length];
		System.arraycopy(head, 0, ori_data, 0, head.length);
		System.arraycopy(finale, 0, ori_data, head.length, finale.length);
//		byte[] ori_image_data = Arrays.copyOfRange(finale, 0, finale_pointer_image);

		List<byte[]> list = new LinkedList<>();
		//这里的dubious是隐藏的信息，list：隐藏的信息+原图的byte[]
		list.add(dubious);list.add(ori_data);

		long endTime=System.currentTimeMillis();
		System.out.println("提取信息总运行时间： "+(endTime-startTime)+"ms");
		return list;
	}
	
//	public List<Object> extract(){
//		int Qt = Q[0][0];char[] DCcode;
//		ArrayList res;int[][] DCT;int flag = 0;int[] DC0 = new int[3];String a = new String();
//		String temp;int length = Integer.MAX_VALUE;int Bpp = 0;char[] cmp;//boolean ex = false;
//		row=(int)Math.ceil((double)size[0]/8/Qt);col=(int)Math.ceil((double)size[1]/8/Qt);
//		int RSTlocation;int[] RST = new int[100];int loc_ind = 0;
//		for (int j = 0; j < row; j += 1) {
//			for (int k = 0; k < col; k += 1) {
//				//Y
//				for(int zy=1;zy<=Math.pow(Qt,2);zy++){
//					res = DCTread(im,flag,treeSelect[0],DC0[0],j+1,k+1,zy);
//					DCT = (int[][]) res.get(0);DC0[0] = DCT[0][0];
//					flag += (int) res.get(1);
//					DCcode = (char[]) res.get(2);
//					//Modified
//					if(a.length()<length) {
//						res = dataExtract(DCT, Bpp);
//						if (j == 0 && k == 0 && zy == 1) {
//							Bpp = (int) res.get(1);
//							length = ((int) res.get(2)) * 8;
//						}//??????????????????
//						temp = (String) res.get(0);
//						a = a.concat(temp);
//					}
//					//if(a.length()>=length) {ex=true;break;}
//					//Recoding to recover the original image
//					res = Recode(0,DCT,treeSelect[0],DCcode,j+1,k+1,ret_str,0);
////					ret_len = ((String)res.get(0)).length();
//					if(j==0 && k==0 && zy==1)
//						ret_str=(String)res.get(0);
//					else
//						ret_str = ret_str.concat((String)res.get(0));
//					//						payload = (int)res.get(1);
//					RSTlocation = (int)res.get(2);
//					if(RSTlocation !=0) {RST[loc_ind] = RSTlocation;loc_ind++;}
//
//				}
//				//CrCb
//				if (color==3){
//					//Cr Cb???
//					for (int zz=1;zz<=2;zz++){
//						res = DCTread(im,flag,treeSelect[zz],DC0[zz],j+1,k+1,0);
//						DCT = (int[][]) res.get(0);DC0[zz] = DCT[0][0];
//						flag += (int) res.get(1);
//						DCcode = (char[]) res.get(2);
//						//Modified
//						if(a.length()<length) {
//							res = dataExtract(DCT, Bpp);
//							temp = (String) res.get(0);
//							a = a.concat(temp);
//						}
//						//if(a.length()>=length) {ex=true;break;}
//						//Recoding to recover the original image
//						res = Recode(0,DCT,treeSelect[zz],DCcode,j+1,k+1,ret_str,0);
//						ret_str = ret_str.concat((String)res.get(0));
////						payload = (int)res.get(1);
//						RSTlocation = (int)res.get(2);
//						if(RSTlocation !=0) {RST[loc_ind] = RSTlocation;loc_ind++;}
//
//					}
//				}
//				//if(ex==true) break;
//			}
//			//if(ex==true) break;
//		}
//		//????16λ
//
////		cmp = Tool.charcmp(Tool.str2char(a),proofreading);
////		int first = Tool.indexOfChar_1(cmp,49);
////
////		a = a.substring(16,a.length());
////		dubious = Tool.str2byte(a);
//
//		byte[] ret = T.str2byte_control(ret_str,RST);
//
////		recoverPath = savePic(dubious);
//		List<String> secret_data = stream2String(dubious);
//
//		List<Object> list = new ArrayList<>();
//		list.add(ret);list.add(secret_data);
//
//		return list;
//
//	}

	public int Recode(StringBuilder emb,int select,int r,int c,int Qnum,boolean pre){
		//这个函数是从HideScheme那边的embedData搬过来的，原理类似，仅保留编码部分的逻辑
		long startTime=System.currentTimeMillis();
		Map<Integer,String> acrev;
		int runLength = 0;int codeLength;int res = 0;
		int runCode;int huffCode;String code ;String temp ;
		int[][] newDCT = coeff;
		//行首预处理
		if(pre){
			if(emb.length()%8!=0){
				int len = emb.length();
				for(int i=0;i<8-len%8;i++){
					emb.append('1');
				}
			}
			temp = int2String(255);temp = temp.concat(int2String(208+(r-2)%8));
			emb.append(temp);
		}

		switch(select&0x01){
			case(0):acrev = a0rev;break;
			default:acrev = a1rev;break;
		}
		//复制直流码字

		emb.append(DCcode);

		//交流编码
		for(int j=1;j<64;j++){
			//计算游程长度，会自动判断是否要添加16*15
			if (newDCT[zigZag[j][0]][zigZag[j][1]]==0){
				runLength += 1;
			}
			else{
				while(runLength>=16){
					huffCode = T.str2int(acrev.get(16*15));
					emb.append(int2String(huffCode&0xFFFF,huffCode>>16));
//                    emb.append(huffCode);
					runLength -= 16;
				}
				codeLength = int2String(Math.abs(newDCT[zigZag[j][0]][zigZag[j][1]])).length();runCode = runLength*16 + codeLength;
				huffCode = T.str2int(acrev.get(runCode));

				code = int2String(toPositiveRange(newDCT[zigZag[j][0]][zigZag[j][1]]),codeLength);
				emb.append(int2String(huffCode&0xFFFF,huffCode>>16));
				emb.append(code);
				runLength = 0;
			}
			//判断到最后一个DCT时是否还存在游程长度
			if(j==63 && runLength!=0){
				huffCode = T.str2int(acrev.get(0));
				emb.append(int2String(huffCode&0xFFFF,huffCode>>16));
			}
		}
		long endTime=System.currentTimeMillis();
//		embeddingData += (endTime-startTime);
		return res;
	}

	public static List<String> stream2String(byte[] buff){
		StringBuilder str = new StringBuilder();
		int[] bs = new int[buff.length/2];
		for (int i = 0; i < buff.length; i+=2) {
			int high = buff[i];int low = buff[i+1];
			if (high == -1 || low == -1) {
				str.append('?');
				bs[i / 2] = 0;
			}
			else {
				int in = high * ((int) Math.pow(2, 4)) + low;
				if(in!=0)
					str.append((char) in);
				bs[i / 2] = in;
			}
		}

		String chinese = new String();
		if(bs.length%2==0) {
			for (int i = 0; i < bs.length; i += 2) {
				int in = bs[i] * 256 + bs[i + 1];
				String strHex = Integer.toHexString(in);
				char letter = (char) Integer.parseInt(strHex, 16);
//            chinese.append(new Character(letter).toString());
				chinese += (letter);
			}
		}

		List<String> list = new LinkedList<>();
		//先英文结果，后中文结果
		list.add(str.toString());
		list.add(chinese);
		return list;

	}

	private int dataWriterForOriginalImage(int finale_pointer,boolean preprocess){
		//converts the global StringBuilder temp into bytes and store them into finale
		long startTime=System.currentTimeMillis();
		int moveForward = 0;int res;
		while(image_temp.length()>=8){
			res = String2int(image_temp.substring(0,8));
			finale[finale_pointer+moveForward] = (byte)res;
			image_temp.delete(0, 8);
			moveForward++;
			if(finale[finale_pointer+moveForward-1]==-1) //也就是byte里的255
			{
				if(preprocess && String2int(image_temp.substring(0,8))>=208 && String2int(image_temp.substring(0,8))<=215){
					preprocess=false;
				}else{
					finale[finale_pointer+moveForward]=0;moveForward++;
				}
			}
		}
		long endTime=System.currentTimeMillis();
//		writingData += (endTime-startTime);
		return moveForward;
	}

	private int dataWriter(int finale_pointer){
		if(dubious==null) {
			return 0;
		}
		int moveForward = 0;int res;
		while(temp.length()>=8){
			res = String2int(temp.substring(0,8));
			dubious[finale_pointer+moveForward] = (byte)res;
			if(finale_pointer+moveForward>=totalLen){
				break;
			}
			temp.delete(0, 8);
			moveForward++;
		}
		return moveForward;
	}

	private void dataExtract(){
		char a;int b;
        //系数小于0不动，等于0的话嵌入信息，大于0的右移1
		if(Bpp==0)
			Bpp=coeff[zigZag[startCoeff-1][0]][zigZag[startCoeff-1][1]];
		for(int i=startCoeff;i<64;i++){
			b = coeff[zigZag[i][0]][zigZag[i][1]];
			if(b>=2 || b<0){
				if(b>=2)
				    coeff[zigZag[i][0]][zigZag[i][1]] = coeff[zigZag[i][0]][zigZag[i][1]]-1;
//				else if(b<-2)	coeff[zigZag[i][0]][zigZag[i][1]] = coeff[zigZag[i][0]][zigZag[i][1]]+1;
			}
			else{
				//提取隐藏并恢复
				temp.append((b==1)?1:0);
				if(b==1) coeff[zigZag[i][0]][zigZag[i][1]] = 0;
//				if(b==-2)	coeff[zigZag[i][0]][zigZag[i][1]] = -1;
			}
//			b = Math.abs(coeff[zigZag[i][0]][zigZag[i][1]]);
//			for(int j=0;j<Bpp;j++){
//				a = (char) (((b&(0x01<<j))>>j)+48);
//				temp.append(a);
//			}
		}
		if(!block1Extracted){
			//get Information and clear temp
			block1Extracted = true;
			if(Bpp<=0){//then temp is null, which means no data was embedded.
				ex = true;dubious = new byte[1];
				temp.setLength(0);temp.append("11111110");//indicates 254-No Hidden Image.
				return;
			}

//			mode = String2int(temp.substring(0,8));
//			int keyExtracted = String2int(temp.substring(8,32));
//			if(keyExtracted!=key){//Wrong Keyword
//				ex = true;dubious = new byte[1];
//				temp.setLength(0);temp.append("11111101");//indicates 253-Wrong Keyword.
//				return;
//			}
			mode = 254;//仅文字模式

			int keyExtracted = String2int(temp.substring(0,32));

			//开finale空间，存原始文件
			finale = new byte[keyExtracted];

			temp.setLength(0);temp.append("11111111");//indicates 255-Complete.
			image_temp.setLength(0);

		}else if(!block2Extracted){
			//get Information and clear temp
			block2Extracted = true;
			totalLen = 6 + String2int(temp.substring(8,32));
			totalLen += String2int(temp.substring(32,40));
			totalLen += String2int(temp.substring(40,48));
			//这里初始化长度
			dubious = new byte[6+totalLen];

			temp.setLength(48);
		}

	}

	private int DCTread(int flag,int select,boolean pre){
		//返回系数，指针，直流code

		Map<String,Integer> ac; Map<String,Integer> dc;coeff = new int[8][8];int pointer = 0;
		int[] ans;int wordLen;int Code;int[] signed;int zeroLen;int diff;int ACnum = 1;int dct;int t1;int t2;
		switch(treeSelect[select]){
			case(0):ac = a0;dc = d0;break;
			case(1):ac = a1;dc = d0;break;
			case(16):ac = a0;dc = d1;break;//低位ac，高位dc
			default:ac = a1;dc = d1;break;
		}
		//DC
		if(pre){
			//必须满足：1.DRI不是0.  2.当前MCU块（由r、c算出）是DRI的正整数倍。  3.当前是直流。  4.如果Y的采样比CrCb密集，则必须是第一个Y的采样块
			if(flag%8!=0){
				pointer += 8-flag%8;
			}
			t1 = im[(pointer+flag)/8]&(0xff);
			t2 = im[(pointer+flag)/8+1]&(0xff);
			DC0[select]=0;//bias是将flag与pointer在这里对齐
			if(t1==255 && t2>=208 && t2<=215){
				pointer +=16;
			}
		}
		//find DC in Map
		ans = findInHuffmanTree(pointer+flag,dc);
		if(ans[0]==-1){
			//如果进入到这个if里，说明码字读取错误，需要作进一步检查
			//一般地，不会进入到这个if，建议在这个if里设置断点，以防程序挂掉
			t1 = im[(pointer+flag)/8]&(0xff);
			t2 = im[(pointer+flag)/8+1]&(0xff);
			int here = 1;
		}

		//DC,length of diff equals wordLen
		pointer += ans[0];wordLen = ans[1];Code = ans[2];
		signed = toSignedNum(pointer+flag,wordLen);
		diff = signed[1];

		coeff[0][0]= DC0[select] + diff;
		DC0[select] = coeff[0][0];
		pointer += wordLen;
		DCcode = int2String(Code,ans[0]);//这里不再需要bias这个变量来控制对齐
		DCcode += int2String(signed[0],wordLen);
		//AC
		while(ACnum<=63){
			ans = findInHuffmanTree(pointer+flag,ac);
			pointer += ans[0];
			if(ans[1]==0){
				break;//the following coefficients are all zeros
			}
			zeroLen = (ans[1]&(0xF0))/16;wordLen = ans[1]&(0x0F);
			ACnum += zeroLen;
//            for(int j=0;j<zeroLen;j++){
//                coeff[zigZag[ACnum][0]][zigZag[ACnum][1]] = 0;
//                ACnum ++;
//            }
			//wordLen here might also be 0,for 0xF0 indicates 16 zeros
			dct = toSignedNum(pointer+flag,wordLen)[1];
			pointer += wordLen;

			coeff[zigZag[ACnum][0]][zigZag[ACnum][1]] = dct;
			ACnum ++;
		}

		return pointer+flag;

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
					case 0:im2[j]=imTmp[i];j++;i+=2;break;//????255???????????0
					case 255:i++;//????棬??????255
					case 208:im2[j]=imTmp[i];j++;i++;break;//???????RST???
					case 209:im2[j]=imTmp[i];j++;i++;break;
					case 210:im2[j]=imTmp[i];j++;i++;break;
					case 211:im2[j]=imTmp[i];j++;i++;break;
					case 212:im2[j]=imTmp[i];j++;i++;break;
					case 213:im2[j]=imTmp[i];j++;i++;break;
					case 214:im2[j]=imTmp[i];j++;i++;break;
					case 215:im2[j]=imTmp[i];j++;i++;break;
					default:im2[j]=imTmp[i+1];j++;i+=2;break;//????255???????????0/255
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
		int[] temp = new int[19];
		for(int j=0;j<19;j++){
			temp[j]=byte2int(head[j+i]);
		}
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

	private void hfm(){
		//可能有多个DHT字段,或者一个字段内有超过1张表
		List<Integer> res = findAll(d2[0],196);int thisLength;int pointer;int pointerOrigin;
		int a;int huffLength;
		for(int z=0;z<res.size();z++){
			a=(int) res.get(z);huffLength = 0;
			pointer = d2[1][a];pointerOrigin = d2[1][a]+2;//please follow the straight-forward moving of this pointer
			thisLength = byte2int(head[pointer+2])*256+byte2int(head[pointer+3]);
			int[] temp = new int[thisLength+4];
			for(int i=0;i<thisLength;i++){
				temp[i]=byte2int(head[pointer+i]);
			}
			pointer += 4;
			while(huffLength<thisLength){
				int mode = byte2int(head[pointer]);pointer += 1;
				int[] huff_num = new int[16];int total=0;
				for(int i=0;i<16;i++){//码字总个数
					huff_num[i] = head[pointer+i];total+=huff_num[i];
				}
				pointer +=16;int codePointer=0;int code=0;
				int[][] huffmanTree = new int[3][total];
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
					case(0):D0 = huffmanTree;break;
					case(1):D1 = huffmanTree;break;
					case(16):A0 = huffmanTree;break;
					case(17):A1 = huffmanTree;break;
				}
			}
		}
	}

	private void dri(){
		int z = getIndex(d2[0],221);
		if(z!=-1){
			int pointer = d2[1][z];
			int len = byte2int(head[pointer+2])*256+byte2int(head[pointer+3]);
			int[] temp = new int[len+2];
			for(int i=0;i<(len+2);i++){
				temp[i]=byte2int(head[pointer+i]);
			}
			DRI = byte2int(head[d2[1][z]+4])*256+byte2int(head[d2[1][z]+5]);}
	}

	private void sos(){
		int z = getIndex(d2[0],218);int a = d2[1][z];
		int len = byte2int(head[a+2])*256+byte2int(head[a+3]);
		int[] temp = new int[len+2];
		for(int j=0;j<len+2;j++){
			temp[j]=byte2int(head[j+a]);
		}

		int pointer = d2[1][z]+6;
		for(int j=0;j<3;j++){
			treeSelect[j] = byte2int(head[pointer]);
			pointer += 2;
		}
	}

	//----------Tool Function Repository--------------
	//Latest revision:2017/10/5
	//Several functions from calctool.java has been deprecated and removed.

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

	private String int2String(int in){
		//positive only!
		if(in==0)   return "0";
		StringBuilder res = new StringBuilder(16);
		while(in!=0){
			res.append(in&0x01);
			in >>=1;
		}
		return res.reverse().toString();
	}

	private String int2String(int in,int bit){
		//positive only!
		StringBuilder res = new StringBuilder(16);
		if(in!=0) {
			while (in != 0) {
				res.append(in & 0x01);
				in >>= 1;
			}
		}
		while(res.length()<bit){
			res.append('0');
		}
		return res.reverse().toString();
	}

	private byte[] int2byte(int in,int len){
		byte[] res = new byte[len];
		for (int n = 0; n < len; n++)
			res[len-1-n] = (byte) (in>>> (n * 8));
		return res;
	}

	private int[] findInHuffmanTree(int pointer,Map<String,Integer> map){
		// Used in DCTread, where we have to do a bit-by-bit search
		// returns:how much the pointer moves,value in the map,code
		StringBuilder res = new StringBuilder(16);
		for(int i=0;i<16;i++){
			res.append((im[(pointer+i)/8]>>(7-(pointer+i)%8))&0x01);
			if(map.containsKey(res.toString())){
				return new int[]{res.length(),map.get(res.toString()),String2int(res.toString())};
			}
		}
		return new int[]{-1,-1,-1};//won't happen
	}

	private void mapConstruction(Map<String,Integer> map,int[][] tree){
		for(int i=0;i<tree[0].length;i++){
			map.put(int2String(tree[1][i],tree[0][i]),tree[2][i]);
		}
	}

	private void mapRevConstruction(Map<Integer,String> map,int[][] tree){
		for(int i=0;i<tree[0].length;i++){
			map.put(tree[2][i],int2String(tree[1][i],tree[0][i]));
		}
	}

	private int String2int(String in){
		int res = 0;
		for(int i=0;i<in.length();i++){
			res +=  (in.charAt(i)-'0')*Math.pow(2,in.length()-1-i);
		}
		return res;
	}

	private int[] toSignedNum(int pointer,int len){
		//convert an unsigned integer read from the array "im" to signed integer, which is required in reading DCT coeff.
		//the first index returns unsigned value. the second returns signed value.
		if(len==0)	return new int[]{0,0};//这是AC交流表的结束符，很重要
		int res = 0;
		for (int i=0;i<len;i++) {
			int tmp = (im[(pointer+i)/8]>>(7-(pointer+i)%8))&0x01;
			res += tmp * Math.pow(2, len - 1 - i);
		}
		int res1 = res;
		if(res<Math.pow(2,len-1)){
			res1= (int) -(Math.pow(2,len)-1-res);
		}
		return new int[]{res,res1};
	}

	private int toPositiveRange(int a){
		//calculate length of a and convert a into positive range.
		int num;
		for(num=0;num<=16;num++){
			if(Math.abs(a)<Math.pow(2,num))		break;
		}
		if(num==0){return 0;}
		if(a<0)	return a+(int)Math.pow(2,num)-1;
		return a;
	}
}
