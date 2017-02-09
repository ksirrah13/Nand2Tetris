// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input. 
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel. When no key is pressed, the
// program clears the screen, i.e. writes "white" in every pixel.

// Put your code here.

// if (key)
// black
// then
// white

// begin inf loop
@FILL
M=-1

@8192
D=A
@SIZE
M=D

(LOOP)

@KBD
D=M

// if key draw black
@BLACK
D;JNE

@SCREEN
D=M

@WHITE
D;JNE

@LOOP
0;JMP

// draw white
(WHITE)

// set num for loop
@SIZE
D=M

@num
M=D

@SCREEN
D=A

@pixel
M=D

// start loop
(LOOP_WHITE)
@num
D=M

@LOOP
D;JLE

@pixel
A=M
M=0

@pixel
M=M+1

@num
M=M-1

@LOOP_WHITE
0;JMP

// draw black
(BLACK)

// set num for loop
@SIZE
D=M

@num
M=D

@SCREEN
D=A

@pixel
M=D

// start loop
(LOOP_BLACK)
@num
D=M

@LOOP
D;JLE

@FILL
D=M

@pixel
A=M
M=D

@num
M=M-1

@pixel
M=M+1

@LOOP_BLACK
0;JMP






