import java.util.*;

//????????????????????0xFF????ü???????????????
//?????0xD0~0xD7,?????RSTn???????????????RSTn????????????0xFF??????0xDn??????????????????RST?????????????????
//?????0xFF?????????0xFF????????0xFF?????ж?
//?????????????????????0xFF?????????????????????????
public class HideScheme2 {
	private byte[] im;
	private int[][] zigZag;
	private byte[] water;
	//	private int[][] d0;
//	private int[][] d1;
//	private int[][] a0;
//	private int[][] a1;
	private int[] treeSelect;
	private int row;
	private byte[] finale;
	private int col;
	private int block;
	private calctool T;
	private StringBuilder temp;
	private int color;
	private int Bpp;
	private int DRI;
	private int Q;
	private int key;
	private String DCcode;
	private int[][] coeff;
	private int[][] newDCT;
	private int[] DC0 = new int[3];
	private Map<Integer,Integer> d0 = new HashMap<>();
	private Map<Integer,Integer> d1 = new HashMap<>();
	private Map<Integer,Integer> a0 = new HashMap<>();
	private Map<Integer,Integer> a1 = new HashMap<>();
	private Map<Integer,Integer> a0rev = new HashMap<>();
	private Map<Integer,Integer> a1rev = new HashMap<>();
	private byte[] block1;
	private byte[] block2;
	private boolean block1Embedded = false;
	private boolean block2Embedded = false;
	//time complexity observation
	long writingData = 0;
	long embeddingData = 0;
	long loadingData = 0;
	private int OriBpp;
	private int startCoeff = 18;

	public HideScheme2(byte[] f,int[][] D0,int[][] D1,int[][] A0,int[][] A1,int[][] newA0,int[][] newA1,int[] tr,int[] s,int c,int dri,int Qt,int password){
		long startTime=System.currentTimeMillis();
		T = new calctool();DRI=dri;
		Q=Qt;key = password;
		im=f;//too space-consuming!!revised with bit manipulation
		finale = new byte[im.length*4];
//		temp = new int[f.length];
//		for(int j=0;j<f.length;j++){
//			temp[j]=T.unsignDecoder(f[j]);
//		}

		treeSelect=tr;color=c;
		row=(int)Math.ceil((double)s[0]/8/Q);col=(int)Math.ceil((double)s[1]/8/Q);//row,col分别是行和列上有多少MCU
		zigZag = T.iZigZag();
		//construct Map for huffman tree
		mapConstruction(d0,D0);
		mapConstruction(d1,D1);
		mapConstruction(a0,A0);
		mapConstruction(a1,A1);
		mapRevConstruction(a0rev,newA0);
		mapRevConstruction(a1rev,newA1);
		long endTime=System.currentTimeMillis();
		System.out.println("Generate Map： "+(endTime-startTime)+"ms");
	}


	public byte[] hideinfo(byte[] w,byte[] word,byte[] aux){
		int mode = 255;int pointer = 0;int payload = 0;int moveForward;
		if(aux!=null)	mode = 252;//Local Mode
		else if(word!= null && w!= null) mode =253;
		else if (word!= null) mode = 254;
		temp = new StringBuilder(400);int newEmbedded;
		boolean ex=false;boolean preprocess;
		int finale_pointer = 0;
		// No Secret Image will be embedded in the Information Blocks.
		// ---Hidden in the first block---
		// check:mode check(can be 255-Image Only/254-Message Only/253-Both)        ----------------------1Byte
		// key:6 digits(max:999999)
		// Also, LSB is hidden in the 15th coeff.												----------------------3Byte
		// ---Hidden in the second block---
		// w/ts:image bytes and its length(3Byte);word/ws:message bytes and its length(1Byte);     -------4Byte
//		char[] tt = null;char[] ts = null;char[] wt = null;char[] ws = null;
		block1 = new byte[4];
//		block1[0] = (byte)mode;//check
//		System.arraycopy(int2byte(key,3), 0, block1, 1, 3);//keystr
		//原始文件长度
		System.arraycopy(int2byte(im.length,4), 0, block1, 0, 4);
		block2 = new byte[5];
		System.arraycopy(int2byte((w!=null)?w.length:0,3),0,block2,0,3);
		block2[3] = (byte)((word!=null)?word.length:0);
		block2[4] = (byte)((aux!=null)?aux.length:0);

		//to-be-embedded data
		water = new byte[((w!=null)?w.length:0)+((word!=null)?word.length:0)+((aux!=null)?aux.length:0)];
		int pt = 0;
		if(w!= null) {
			System.arraycopy(w, 0, water, pt, w.length);
			pt += w.length;
		}
		if(word!= null) {
			System.arraycopy(word, 0, water, pt, word.length);
			pt += word.length;
		}
		if(aux!=null){
			System.arraycopy(aux, 0, water, pt, aux.length);
			pt += aux.length;
		}

		int ref = (((color==3)?2:0)+ (int)Math.pow(Q,2));
		block = row*col*ref;//(row*Q)*(col*Q);
		Bpp = (int)Math.ceil((double)(water.length+8)/(5*(block-2)));OriBpp = Bpp;
		for (int j = 0; j < row; j += 1) {
			Bpp = (int)Math.ceil((double)(water.length+8-payload/8)/(5*(row-j)*col*ref));
			for (int k = 0; k < col; k += 1) {
				//Y通道
				for(int zy=1;zy<=Math.pow(Q,2);zy++){
					preprocess = ( DRI!=0 && (j*col+k)!=0 && (j*col+k)%DRI==0 && zy == 1);
					pointer = DCTread(pointer,0,preprocess);//great modification

					newEmbedded = embeddedData(payload,temp,treeSelect[0],ex,j+1,k+1,zy,preprocess);//固定值6
					payload = newEmbedded;
					moveForward = dataWriter(finale_pointer,preprocess);
					finale_pointer += moveForward;
					ex = payload>(water.length)*8;
				}
				if (color==3){
					//Cr Cb通道
					for (int zz=1;zz<=2;zz++){
						pointer = DCTread(pointer,zz,false);

						newEmbedded = embeddedData(payload,temp,treeSelect[zz],ex,j+1,k+1,0,false);
						payload = newEmbedded;
						moveForward = dataWriter(finale_pointer,false);
						finale_pointer += moveForward;
						ex = payload>(water.length)*8;
					}
				}
			}
		}
		if(temp.length()!=0){
			int len = temp.length();
			for(int i=0;i<8-len;i++){
				temp.append('1');
			}
			finale_pointer += dataWriter(finale_pointer,false);
		}

		finale[finale_pointer]=(byte)255;finale_pointer++;
		finale[finale_pointer]=(byte)217;finale_pointer++;

		System.out.println("Bpp： "+ Bpp);
		System.out.println("Read DCT运行时间： "+ loadingData +"ms");
		System.out.println("Embedding运行时间： "+ embeddingData +"ms");
		System.out.println("Writing Data运行时间： "+ writingData +"ms");
		return Arrays.copyOfRange(finale, 0, finale_pointer);
	}

	private int dataWriter(int finale_pointer,boolean preprocess){
		//converts the global StringBuilder temp into bytes and store them into finale
		long startTime=System.currentTimeMillis();
		int moveForward = 0;int res;
		while(temp.length()>=8){
			res = String2int(temp.substring(0,8));
			finale[finale_pointer+moveForward] = (byte)res;
			temp.delete(0, 8);
			moveForward++;
			if(finale[finale_pointer+moveForward-1]==-1) //也就是byte里的255
			{
				if(preprocess && String2int(temp.substring(0,8))>=208 && String2int(temp.substring(0,8))<=215){
					preprocess=false;
				}else{
					finale[finale_pointer+moveForward]=0;moveForward++;
				}
			}
		}
		long endTime=System.currentTimeMillis();
		writingData += (endTime-startTime);
		return moveForward;
	}

	private int embeddedData(Integer payload,StringBuilder emb,int select,boolean ex,int r,int c,int Qnum,boolean pre){
		//modify DCT，and return new code of the new DCT.
		long startTime=System.currentTimeMillis();
		Map<Integer,Integer> acrev;int runLength = 0;int codeLength;int res;
		int runCode;int huffCode;String code ;String temp ;

		//嵌入完毕后，按之前的DCT来嵌入数据
		if(!ex){
			if(!block1Embedded){
				DCTembed(block1,payload,r,c,select,Qnum);res = 0;
				block1Embedded = true;
			}else if(!block2Embedded){
				DCTembed(block2,payload,r,c,select,Qnum);res = 0;
				block2Embedded = true;
			}else{
				res = DCTembed(water,payload,r,c,select,Qnum);//res = 40*Bpp;
			}
		}else{
			newDCT = coeff;res = 0;
		}

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
					huffCode = acrev.get(16*15);
					emb.append(int2String(huffCode&0xFFFF,huffCode>>16));
//                    emb.append(huffCode);
					runLength -= 16;
				}
				codeLength = int2String(Math.abs(newDCT[zigZag[j][0]][zigZag[j][1]])).length();runCode = runLength*16 + codeLength;
				huffCode = acrev.get(runCode);

				code = int2String(toPositiveRange(newDCT[zigZag[j][0]][zigZag[j][1]]),codeLength);
				emb.append(int2String(huffCode&0xFFFF,huffCode>>16));
				emb.append(code);
				runLength = 0;
			}
			//判断到最后一个DCT时是否还存在游程长度
			if(j==63 && runLength!=0){
				huffCode = acrev.get(0);
				emb.append(int2String(huffCode&0xFFFF,huffCode>>16));
			}
		}
		long endTime=System.currentTimeMillis();
		embeddingData += (endTime-startTime);

		return res;

	}

	private int DCTembed(byte[] embed,Integer payload,int r,int c,int select,int Qnum){
		//系数小于0不动，等于0的话嵌入信息，大于0的右移1
		int[][] res = new int[8][8];
		int temp;int LSB;
		for(int i=0;i<64;i++){
			temp = coeff[zigZag[i][0]][zigZag[i][1]];
			if(i>=startCoeff) {
//				res[zigZag[i][0]][zigZag[i][1]] = temp - temp % ((int) Math.pow(2, Bpp));
//				LSB = 0;
//				for (int j = 0; j < Bpp; j++) {
//					if (payload + Bpp * (i - startCoeff) + j >= embed.length * 8){
//						break;
//					}
//					int tmp = (embed[(payload + Bpp * (i - startCoeff) + j) / 8] >> (7 - (payload + Bpp * (i - startCoeff) + j) % 8)) & 0x01;
//					LSB += tmp * Math.pow(2, j);//低位在前
//				}
//				res[zigZag[i][0]][zigZag[i][1]] += (temp>=0)?LSB:-LSB;
				if(temp<0){
					res[zigZag[i][0]][zigZag[i][1]] = coeff[zigZag[i][0]][zigZag[i][1]];
				}
				else if(temp==0) {//temp==1||temp==-1
					//信息隐藏
					LSB = 0;
					int dig1 = payload / 8;
					int dig2 = payload % 8;
					for (int j = 0; j < Bpp; j++) {
						if (payload >= embed.length * 8)
							break;
						LSB += (embed[dig1] >> (7 - dig2)) & 0x01;
						payload++;
					}
					res[zigZag[i][0]][zigZag[i][1]] += LSB;
					if (payload >= embed.length * 8)
						break;
				}
//				}else if(temp<0){
//					//左移
//					res[zigZag[i][0]][zigZag[i][1]] = coeff[zigZag[i][0]][zigZag[i][1]]-1;
//				}
				else{
					//右移
					res[zigZag[i][0]][zigZag[i][1]] = coeff[zigZag[i][0]][zigZag[i][1]]+1;
				}
			}
//			else{
//				res[zigZag[i][0]][zigZag[i][1]] = temp;
//			}

		}
		if(r==1 && c==1 && (select==0 || select==1) && Qnum==1){res[zigZag[startCoeff-1][0]][zigZag[startCoeff-1][1]] = Bpp;}
		newDCT = res;
		return payload;
	}


	private int DCTread(int flag,int select,boolean pre){
		//返回系数，指针，直流code
		long startTime=System.currentTimeMillis();
		Map<Integer,Integer> ac; Map<Integer,Integer> dc;coeff = new int[8][8];int pointer = 0;
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
		long endTime=System.currentTimeMillis();
		loadingData += (endTime-startTime);

		return pointer+flag;

	}

//----------Tool Function Repository--------------
	//Latest revision:2017/10/5
	//Several functions from calctool.java has been deprecated and removed.

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
			res[len-1-n] = (byte) ((in>>> (n * 8))&0xFF);
		return res;
	}

	private int[] findInHuffmanTree(int pointer,Map<Integer,Integer> map){
		// Used in DCTread, where we have to do a bit-by-bit search
		// returns:how much the pointer moves,value in the map,code

//        StringBuilder res = new StringBuilder(16);
		int res = 0;
		for(int i=0;i<16;i++){
			res *=2;res += (im[(pointer+i)/8]>>(7-(pointer+i)%8))&0x01;
//            res.append((im[(pointer+i)/8]>>(7-(pointer+i)%8))&0x01);
			if(map.containsKey(res+((i+1)<<16))){
				return new int[]{i+1,map.get(res+((i+1)<<16)),res};
			}
//            if(map.containsKey(res.toString())){
//                return new int[]{res.length(),map.get(res.toString()),String2int(res.toString())};
//            }
		}

		return new int[]{-1,-1,-1};//won't happen
	}

	private void mapConstruction(Map<Integer,Integer> map,int[][] tree){
		for(int i=0;i<tree[0].length;i++){
			map.put(tree[1][i]+(tree[0][i]<<16),tree[2][i]);
		}
	}

	private void mapRevConstruction(Map<Integer,Integer> map,int[][] tree){
		for(int i=0;i<tree[0].length;i++){
			map.put(tree[2][i],tree[1][i]+(tree[0][i]<<16));
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
