#include<reg51.h>
#include<intrins.h>
#define uchar unsigned char
#define uint unsigned int
#define Auto '2'
#define Single '3'
#define no '0'
#define yes '1'
#define pause '1'
#define start '2'
#define setInterval '3'
#define changeMod '4'
#define reSet '5'


/*指令格式：str[0]:singleMeasuring;
							 [1]:isWorked;
							 [2]:workMod;*/
	
sbit CLK=P1^0;
sbit OE=P1^1;
sbit EOC=P1^3;
sbit ST=P1^4;
sbit Eadc = P3^7;

uchar str[3] = "";
uchar isReceived = no;
uchar isWorked = no;
uchar workMod = Single;
uchar j = 0;
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



void delay(unsigned long z)
{
        uint x,y;
        for(x=z;x>0;x--)
                for(y=110;y>0;y--);       
}

void blueToothSend(uchar u){
	if(isReceived == no){	
		SBUF = u;
		while(!TI);
		TI = 0;
	}
}

void main(){
	init();
	while(1){
		
		
		if(isWorked == yes){
			
			if(workMod == Auto){
					Eadc = 1;
					delay(3000);
					ST=0;
          ST=1;
          ST=0;//start一个高脉冲启动AD0809;
          while(0==EOC);
          OE=1;
          result = P2; //P2连接adc0809输出
          OE=0;
					blueToothSend(result);
					
					P2 = 0xff;
					Eadc = 0;
			}
			if(workMod == Single){
					if(singleMeas == yes){
						Eadc = 1;
						delay(20);
						ST=0;
						ST=1;
						ST=0;//start一个高脉冲启动AD0809;
						while(!EOC);
						OE=1;
						result = P2; //P2连接adc0809输出
						OE=0;
						blueToothSend(result);
						
						singleMeas = no;
						P2 = 0xff;
						Eadc = 0;
					}
			}	
			
		}
	
	}



}

void ser() interrupt 4  
{  
         if(RI)       //接收数据，手动将RI清0  
     {         
         RI=0;             
         if(isReceived == no)
         {   
					 isReceived = yes;                       
           j=0;  						 
         }  
         str[j]=SBUF;            
         if(j==2){     
           singleMeas = str[0];
					 isWorked = str[1]; 
					 workMod = str[2];
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
 