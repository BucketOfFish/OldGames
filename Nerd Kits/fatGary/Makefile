GCCFLAGS=-g -Os -Wall -mmcu=atmega168 
LINKFLAGS=-Wl,-u,vfprintf -lprintf_flt -Wl,-u,vfscanf -lscanf_flt -lm
AVRDUDEFLAGS=-c avr109 -p m168 -b 115200 -P COM3
LINKOBJECTS=../libnerdkits/delay.o ../libnerdkits/lcd.o ../libnerdkits/uart.o

all:	fatGary-upload

fatGary.hex:	fatGary.c
	make -C ../libnerdkits
	avr-gcc ${GCCFLAGS} ${LINKFLAGS} -o fatGary.o fatGary.c ${LINKOBJECTS}
	avr-objcopy -j .text -O ihex fatGary.o fatGary.hex
	
fatGary.ass:	fatGary.hex
	avr-objdump -S -d fatGary.o > fatGary.ass
	
fatGary-upload:	fatGary.hex
	avrdude ${AVRDUDEFLAGS} -U flash:w:fatGary.hex:a
