#include<reg51.h>
#include<intrins.h>
#define uchar unsigned char
#define uint unsigned int
#define Auto 2;
#define Single 3;
#define no 0;
#define yes 1;
#define pause 1;
#define start 2;
#define setInterval 3;
#define changeMod 4;
#define reSet 5;


/*指令格式：str[0]:singleMeasuring;
							 [1]:newCom?;
							 [2]:isWorked;
							 [3]:workMod;
							 [4~10]:interval;范围3s~166min*/
	
sbit CLK=P1^3;
sbit ST=P1^2;
sbit EOC=P1^1;
sbit OE=P1^0;

uchar str[50] = "";
uchar isReceived = no;
uchar isWorked = no;
uchar workMod = Single;
unsigned long interval = 3000;
uchar j = 0;
uchar newCom = no;
uchar singleMeas = no;
uchar result = 0;

void init()  
{  
     TMOD=0x22;    //定时器1用于蓝牙波特率，定时器2用于adc0809时钟          
		 TH0=0x14;
     TL0=0x00;     //8自动重装定时器
		 TR0=1;
		 TH1=0xfd;     //定时器1初值  ,设置波特率为9600 晶振11.0529MHZ?  
     TL1=0xfd;  
     TR1=1;        //开启定时器1  
   
     SM0=0;  
     SM1=1;        //10位异步接收，（8位数据）波特率可变  
     REN=1;        //允许串行口接收位  
     EA=1;         //允许中断（总闸）  
     ES=1;         //允许串口中断  
		 ET0=1;
}  

void setWorkStatu(){
	isWorked = str[2];
}

void setWorkMod(){
	workMod = str[3];
}



uint charToNum(uchar uc){
	switch uc{
		case 0:return 0;
		case 1:return 1;
		case 2:return 2;
		case 3:return 3;
		case 4:return 4;
		case 5:return 5;
		case 6:return 6;
		case 7:return 7;
		case 8:return 8;
		case 9:return 9;
	}
}

void setInterval(){
	interval = charToNum(str[4])*1000000+charToNum(str[5])*100000+
							charToNum(str[6])*10000+charToNum(str[7])*1000+
							charToNum(str[8])*100+charToNum(str[9])*10+charToNum(str[10]);
}


void delay(unsigned long z)
{
        uint x,y;
        for(x=z;x>0;x--)
                for(y=110;y>0;y--);       
}

void main(){
	
	while(1){
		
		if(newCom){
			switch newCom{
				case pause:
					isWorked = no;
				break;
				case start:
					isWorked = yes;
				break;
				case setInterval:
					setInterval();
				break;
				case changeMod:
					setWorkMod();
				break;
				case reSet:
					setWorkStatu();
				setWorkMod();
				setInterval();
				break;
			}
		}
		if(isWorked){
			switch(workMod){
				case Auto:
					delay(interval);
					ST=0;
          ST=1;
          ST=0;//start一个高脉冲启动AD0809;
          while(!EOC);
          OE=1;
          result = P2; //P2连接adc0809输出
          OE=0;
					break;
				case Single:
					break;
			}
		}
	
	}



}

void ser() interrupt 4  
{  
         if(RI)       //接收数据，手动将RI清0  
     {         
         RI=0;             
         if(isReceived)
         {                          
             for(j=0;str[j]!='#'&&j<50;j++){  
                   str[j]='\0'; 
						 } 
             j=0;  
						 isReceived = yes;
         }  
         str[j]=SBUF;            
         if(str[j]=='#'||j==49){     //以'#'作为传送字符串的结尾符，我定义的字符数组最长为50所以49也应该结束。  
           singleMeas = str[0];
					 newCom = str[1];  
					 isReceived = no;
				 }else{  
             j++; 
				 } 
     }  
   
     if(TI)     //发送数据  
     {  
     }      
}  

void timer0() interrupt 1
{
        CLK=~CLK;
}
 