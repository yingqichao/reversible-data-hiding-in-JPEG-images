import java.util.ArrayList;
import java.util.Arrays;

//????????????????????0xFF????ü???????????????
//?????0xD0~0xD7,?????RSTn???????????????RSTn????????????0xFF??????0xDn??????????????????RST?????????????????
//?????0xFF?????????0xFF????????0xFF?????ж?
//?????????????????????0xFF?????????????????????????
public class HideScheme2 {
	private char[] im;
	private int[][] zigZag;
	private char[] water;
	private int[][] d0;
	private int[][] d1;
	private int[][] a0;
	private int[][] a1;
	private int[] treeSelect;
	private int row;
	private int col;
	private int block;
	private calctool T;
	private int[] temp;
	private int color;
	private int Bpp;
	private String ret_str = new String();
	private int DRI;
	private int Q;
	
	public HideScheme2(byte[] f,int[][]D0,int[][] D1,int[][] A0,int[][] A1,int[] tr,int[] s,int c,int dri,int Qt){
		T = new calctool();DRI=dri;
		Q=Qt;
		im=T.byte2char(f);
		temp = new int[f.length];
		for(int j=0;j<f.length;j++){
    		temp[j]=T.unsignDecoder(f[j]);
    	}
		d0=D0;d1=D1;a0=A0;a1=A1;treeSelect=tr;color=c;
		row=(int)Math.ceil((double)s[0]/8/Q);col=(int)Math.ceil((double)s[1]/8/Q);//row,col??????к??????ж???MCU
		zigZag = T.iZigZag();
	}
	
	
	public byte[] hideinfo(byte[] w){
		byte[] ret;int[][] DCT;int[] DC0 = new int[3];char[] DCcode;char[] tt;char[] ts;
		int encode=0;int nz = 0;ArrayList res;String temp = null;
		int flag = 0;int payload = 0;boolean ex=false;int ret_len;int RSTlocation;int[] RST = new int[100];int loc_ind = 0;
		//?16λ???water?????
		tt=T.byte2char(w);ts=T.int2bit((w.length),16);
		water = new char[tt.length+ts.length];
		System.arraycopy(ts, 0, water, 0, 16);
		System.arraycopy(tt, 0, water, 16,tt.length);

//		String buff;
		block=(row*Q)*(col*Q);
		//??????????????15??DCT,?????49λ??????DCT(????????0)
//		nz = nonZeros(im);//????????DCT????????
		Bpp = (int)Math.ceil((double)(water.length)/(double)(49*block));
		for (int j = 0; j < row; j += 1) {
			for (int k = 0; k < col; k += 1) {
				//Y???
				for(int zy=1;zy<=Math.pow(Q,2);zy++){
					res = DCTread(im,flag,treeSelect[0],DC0[0],j+1,k+1,zy);
					DCT = (int[][]) res.get(0);
					flag += (int) res.get(1);
					DC0[0] = DCT[0][0];
					DCcode = (char[]) res.get(2);
					if (ex==false){
//					//这里修改DCT系数，下面都是重新编码
						DCT = DCTembed(DCT,water,payload,j+1,k+1,treeSelect[0],zy);
					}
					res = embeddedData(water,payload,DCT,treeSelect[0],DCcode,ex,j+1,k+1,ret_str,zy);
					if(j==0 && k==0 && zy==1) 
						{ret_len=((String)res.get(0)).length();
						ret_str=(String)res.get(0);}
					else 
						{temp=(String)res.get(0);ret_len = temp.length();
						ret_str = ret_str.concat(temp);}
					payload = (int)res.get(1);
					RSTlocation = (int)res.get(2);
					if(RSTlocation !=0) {RST[loc_ind] = RSTlocation;loc_ind++;}
					if(payload>water.length)
					{ex=true;}
				}
				if (color==3){
				//Cr Cb???
				for (int zz=1;zz<=2;zz++){
					res = DCTread(im,flag,treeSelect[zz],DC0[zz],j+1,k+1,0);
					DCT = (int[][]) res.get(0);
					flag += (int) res.get(1);
					DC0[zz] = DCT[0][0];
					DCcode = (char[]) res.get(2);
					if (ex==false){
//					//这里修改DCT系数，下面都是重新编码
						DCT = DCTembed(DCT,water,payload,j+1,k+1,treeSelect[0],zz);
					}
					res = embeddedData(water,payload,DCT,treeSelect[zz],DCcode,ex,j+1,k+1,ret_str,0);
					ret_len = ((String)res.get(0)).length();
					ret_str = ret_str.concat((String)res.get(0));
					payload = (int)res.get(1);
					if(payload>water.length){ex=true;}
				}
				}
			}
		}
		
		ret = T.str2byte_control(ret_str,RST);
		return ret;
	}
	
	public ArrayList embeddedData(char[] w, Integer payload,int[][] DCT,int select,char[] DCcode,boolean ex,int r,int c,String emb,int Qnum){
		//?????DCT??????????DCT??????????
		int[][] ac = null;ArrayList res = new ArrayList(2);String a = new String();int runLength = 0;int codeLength = 0;
		int runCode = 0;char[] huffcode;int index;char[] code;String temp;int RSTlocation = 0;
		int[][] newDCT;
		//????????????DCT?????????
//		if (ex==false){
//			//这里修改DCT系数，下面都是重新编码
//			newDCT = DCTembed(DCT,w,payload,r,c,select,Qnum);}
//		else {newDCT=DCT;}

		newDCT = DCT;
		
		//重新编码
		if( DRI!=0 && ((r-1)*col+c-1)!=0 && ((r-1)*col+c-1)%DRI==0 && (select==0 || select==1) && Qnum == 1){
		 if(emb.length()%8!=0){
			for(int i=0;i<8-emb.length()%8;i++){
				a=a+(char)49;
			}
		 }
		 RSTlocation = (emb.length()+a.length())/8;
		 temp = T.int2str(255);temp = temp.concat(T.int2str(208+(r-2)%8));
		 
		 a=a.concat(temp);
		}
		
//		if(r==1 && c==1 && (select==0 || select==1) && Qnum == 1){payload += 48*Bpp;}
//		else{payload += 49*Bpp;}
		
		switch(select){
			case(0):ac = a0;break;
			case(1):ac = a1;break;
			case(16):ac = a0;break;
			case(17):ac = a1;break;
		}
		//???????????
		for (int i=0;i<DCcode.length;i++){
			a = a + DCcode[i];
		}
		//????????
		for(int j=1;j<64;j++){
			//?????γ????
			if (newDCT[zigZag[j][0]][zigZag[j][1]]==0){
				runLength += 1; 
				if(runLength==16){
					index = T.indexOf_1(ac[2],16*15);
					huffcode = T.int2bit(ac[1][index],ac[0][index]);
					for(int y=0;y<huffcode.length;y++){
						a = a + huffcode[y];
					}
					runLength = 0;
				}
			}
			else{
				codeLength = T.howManyBits(newDCT[zigZag[j][0]][zigZag[j][1]]);runCode = runLength*16 + codeLength;
				index = T.indexOf_1(ac[2],runCode);
				huffcode = T.int2bit(ac[1][index],ac[0][index]);
				code = T.int2bit(T.signDecoder(newDCT[zigZag[j][0]][zigZag[j][1]],codeLength),codeLength);
				for(int y=0;y<huffcode.length;y++){
					a = a + huffcode[y];
				}
				for(int z=0;z<code.length;z++){
					a = a + code[z];
				}
				runLength = 0;
			}
			//?ж????????DCT????????γ????
			if(j==63 && runLength!=0){
				index = T.indexOf_1(ac[2],0);
				huffcode = T.int2bit(ac[1][index],ac[0][index]);
				for(int y=0;y<huffcode.length;y++){
					a = a + huffcode[y];
				}
			}
		}
		res.add(a);//编码后的码字
		//???
//		check = DCTread(T.str2char(a),0,select, 0,0,0);
//		DCTcmp = (int[][]) check.get(0);
//		test = T.twoDimensionalEqual(DCTcmp,newDCT);
//		assert(test==true);
		res.add(payload);//嵌入了多少bit
		res.add(RSTlocation);//是否重置
		return res;
	}

	//嵌入信息过程，修改DCT系数
	public int[][] DCTembed(int[][] coeff, char[] w,Integer payload,int r,int c,int select,int Qnum){
		int temp;int LSB;boolean flag = false;
		for(int i=1;i<64;i++){
//			if (payload+Bpp*(i-15)>w.length){flag=true;break;}
			if (payload>=w.length){flag=true;break;}
			if(r==1 && c==1 && i==63 && (select==0 || select==1) && Qnum==1){coeff[zigZag[i][0]][zigZag[i][1]] = Bpp;}
			else{
			  temp = coeff[zigZag[i][0]][zigZag[i][1]];
			  if(temp==0)	continue;
			  else if(temp==1||temp==-1){
				//信息隐藏
				  coeff[zigZag[i][0]][zigZag[i][1]] = temp-temp%((int)Math.pow(2, Bpp));
				  LSB = 0;
				  for(int j=0;j<Bpp;j++){
//				  if (payload+Bpp*(i-15)+j+1>w.length){flag=true;break;}
					  if (payload>=w.length){flag=true;break;}
					  LSB += w[payload+Bpp*(i-1)+j];
					  payload++;
				  }
				  coeff[zigZag[i][0]][zigZag[i][1]] += (coeff[zigZag[i][0]][zigZag[i][1]]>0)?LSB:-LSB;
				  if (flag==true) break;
			  }else if(temp<0){
			  	//左移
				  coeff[zigZag[i][0]][zigZag[i][1]] = coeff[zigZag[i][0]][zigZag[i][1]]-1;
			  }else{
			  	//右移
				  coeff[zigZag[i][0]][zigZag[i][1]] = coeff[zigZag[i][0]][zigZag[i][1]]+1;
			  }

			}
		}
		return coeff;
	}
	
//	public int nonZeros(char[] im){
//	//??????????????????Host????????з???DCT???????????????????????DCT??????????
//	//????????????????DCT??????????????????д??????????????????????????????????
//		ArrayList res;int here;
//		int[][] DCT;int flag = 0;int sumOfNotZero = 0;int[] DC0 = new int[3];
//		for (int j = 0; j < row; j += 1) {
//			for (int k = 0; k < col; k += 1) {
//				//Y???
//				res = DCTread(im,flag,treeSelect[0],DC0[0],j+1,k+1);
//				DCT = (int[][]) res.get(0);
//				flag += (int) res.get(1);
//				DC0[0] = (int) res.get(2);
//				sumOfNotZero += sumNonZero(DCT,15);
//				if (color==3){
//				//Cr???
//				res = DCTread(im,flag,treeSelect[1],DC0[1],j+1,k+1);
//				DCT = (int[][]) res.get(0);
//				flag += (int) res.get(1);
//				DC0[1] = (int) res.get(2);
//				sumOfNotZero += sumNonZero(DCT,15);
//				//Cb???
//				res = DCTread(im,flag,treeSelect[2],DC0[2],j+1,k+1);
//				DCT = (int[][]) res.get(0);
//				flag += (int) res.get(1);
//				DC0[2] = (int) res.get(2);
//				sumOfNotZero += sumNonZero(DCT,15);
//				}
//			}
//		}
//		return sumOfNotZero;
//	}
//	
	public ArrayList DCTread(char[] im,int flag,int select, int DC0,int r,int c,int Qnum){
		//???????????????code
		String rev=new String();char[] DCcode;int[] redundant = new int[5];
		ArrayList res = new ArrayList(2);int[][] ac = null; int[][] dc = null;int[][] coeff = new int[8][8];int pointer = 0;
		int[] ans;int wordLen;int zeroLen;int diff;int ACnum = 1;int dct;int t1;int t2;int skipStep;int bias = 0;int wrong;
		switch(select){
		case(0):ac = a0;dc = d0;break;
		case(1):ac = a1;dc = d0;break;
		case(16):ac = a0;dc = d1;break;//??λac????λdc
		case(17):ac = a1;dc = d1;break;
		}
		//DC
		if(DRI!=0 &&  ((r-1)*col+c-1)!=0 && ((r-1)*col+c-1)%DRI==0 && (select==0 || select==1) && Qnum == 1){
			//????????1.DRI????0.  2.???MCU?飨??r??c???????DRI????????????  3.??????????  4.???Y???????CrCb????????????????Y???????
			if(flag%8!=0){
				skipStep = flag%8;
				pointer +=8-skipStep;bias +=8-skipStep;
			}
			t1 = T.bin2dec_str(im,pointer+flag,8);
			t2 = T.bin2dec_str(im,pointer+flag+8,8);
			DC0=0;//bias???flag??pointer?????????
			if(t1==255 && t2>=208 && t2<=215){
			pointer +=16;bias +=16;}
		}
		ans = T.huffmanDecoder(im,pointer+flag,dc,row,col);
		if(ans[0]==-1){
			//??????????if????????????????????????????
			//???????????????if???????????if?????????????????
			wrong = 1;
			t1 = T.bin2dec_str(im,pointer+flag,8);
			t2 = T.bin2dec_str(im,pointer+flag+8,8);
		}
		
		pointer += ans[0];wordLen = ans[1];
		diff = T.i_unsignDecoder(T.bin2dec_str(im,pointer+flag,wordLen),wordLen);	
		coeff[0][0]= DC0 + diff;
		pointer += wordLen;
		if(DRI!=0 && ((r-1)*col+c-1)!=0 && ((r-1)*col+c-1)%DRI==0 && (select==0 || select==1) && Qnum == 1)
		{DCcode=Arrays.copyOfRange(im, bias+flag, pointer+flag);}
		else{DCcode=Arrays.copyOfRange(im, flag, pointer+flag);}
		//AC
		while(ACnum<=63){
			ans = T.huffmanDecoder(im,pointer+flag,ac,row,col);
			pointer += ans[0];
			if(ans[1]==0){//
				break;}
			zeroLen = (ans[1]&(0xF0))/16;wordLen = ans[1]&(0x0F);
			for(int j=0;j<zeroLen;j++){
				coeff[zigZag[ACnum][0]][zigZag[ACnum][1]] = 0;
						ACnum ++;
			}
			dct = T.i_unsignDecoder(T.bin2dec_str(im,pointer+flag,wordLen),wordLen);
			pointer += wordLen;
			
			coeff[zigZag[ACnum][0]][zigZag[ACnum][1]] = dct;
			ACnum ++;
		}
		
		res.add(coeff);
		res.add(pointer);
		res.add(DCcode);
		res.add(redundant);
		return res;
		
	}
	
//	public int sumNonZero(int[][] coeff,int thresh){
//		int summary=0;int a;
//		for (int l = thresh; l < 8*8; ++l) {
//				a=coeff[zigZag[l][0]][zigZag[l][1]];
//				if (a!=0){
//					summary+=1;
//				}
//		}
//		return summary;
//	}
	
}
