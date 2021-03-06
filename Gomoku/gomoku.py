#!/usr/bin/env python

from Tkinter import *
import tkMessageBox

import math
from math import floor

import subprocess # used to spawn the process implementing the opponent

class Application(Frame):

    def createWidgets(self):

        # Create canvas:
        self.canvas = Canvas(self, width="280m", height="280m", bg='white')
        self.canvas.pack(side=LEFT)

    def __init__(self, master=None):
        Frame.__init__(self, master)
        Pack.config(self)
        self.createWidgets()

        Widget.bind(self.canvas, "<1>", self.mouseDown)
        Widget.bind(self.canvas, "<Motion>", self.mouseMove)

        self.humans_turn = 1 # =1 denotes that the human will start.
        self.color = 'black'

        #print sys.argv
        if len(sys.argv) < 2:
            print "Not enough arguments. Usage: gomokuclient <name of opponent program> [youstart]"
            print "If argument youstart is given, the opponent program starts, otherwise the human starts."
            sys.exit()
        else:
            opponentName = sys.argv[1]

        # create the opponent process:
        if opponentName.endswith(".class"):
            opponentName = opponentName[:-6]
            args = "java "+opponentName
            self.opponent = subprocess.Popen(args,
                    stdin=subprocess.PIPE, shell=True,
                    stdout=subprocess.PIPE, bufsize=1, universal_newlines=True)
        else:
            self.opponent = subprocess.Popen("./"+opponentName,
                    stdin=subprocess.PIPE,
                    stdout=subprocess.PIPE, bufsize=1, universal_newlines=True)

        # draw the board:
        self.size = 40
        size = self.size
        for x in range(19):
            self.canvas.create_line(size+x*size,size,size+x*size,size+18*size)
        for y in range(19):
            self.canvas.create_line(size,size+y*size,size+18*size,size+size*y)

        # initialize the board (for checking the rules, wins etc.)
        # we use indices 0..18 here.
        self.board = [[-1 for col in range(19)] for row in range(19)]
        # -1 means empty, 0 is black, 1 is white.
        self.movecount = 0 # count how many moves were made..

        if len(sys.argv) <= 2:
            self.opponent.stdin.write("white\n") # opponent will not start
            self.opponent.stdin.flush()
        else:
            self.opponent.stdin.write("black\n") # opponent will start
            self.opponent.stdin.flush()
            # let opponent make first move:
            self.letOpponentMove()

    def mouseDown(self, event):

            size = self.size*1.0
            move_x = round ((event.x-size)/size)
            move_y = round ((event.y-size)/size)

            if move_x < 0: move_x = 0
            if move_y < 0: move_y = 0
            if move_x > 18: move_x = 18
            if move_y > 18: move_y = 18
            move_x = int(move_x)
            move_y = int(move_y)

            if self.checkMove(move_x,move_y): # if move is OK:
                self.updateBoard(move_x,move_y,self.color)
                self.flipColor()

                # now tell the move to the opponent and let him move:
                self.opponent.stdin.write( str(move_x+1)+" "+str(move_y+1)+"\n")
                self.opponent.stdin.flush()
                self.letOpponentMove()
            else:
                tkMessageBox.showwarning("Invalid move:",
                   "Human player tried invalid move:" +str(move_x+1)+", "+str(move_y+1))


    def flipColor(self):
        if self.color == 'black':
            self.color = 'white'
        else:
            self.color = 'black'

    def letOpponentMove(self):
        answer = self.opponent.stdout.readline()
        #print "got reply: ",answer

        # why -1: because the opponent returns index 1..19
        move_x = int(answer.split()[0]) -1
        move_y = int(answer.split()[1]) -1

        if not self.checkMove(move_x,move_y):
            tkMessageBox.showwarning("invalid move:","AI player tried invalid move:" +str(x+1)+", "+str(y+1))
            #self.opponent.kill() # would only work with python 2.6..
            sys.exit()

        self.updateBoard(move_x,move_y,self.color)
        self.flipColor()

    def mouseMove(self, event):
        pass ## maybe: show the stone that would be drawn

    def updateBoard(self, x,y, color):
        # first the drawing:
        size = self.size*1.0
        b = self.size*0.85*0.5
        pos_x = size + x*size
        pos_y = size + y*size
        self.canvas.create_oval(pos_x-b,pos_y-b,pos_x+b,pos_y+b, width = 2, fill=self.color, outline ='black')
        self.update()
        # logging:
        print x+1, y+1, color
        # now update the state:
        self.movecount = self.movecount + 1
        if color == 'black':
            self.board[x][y] = 0
        elif color == 'white':
            self.board[x][y] = 1
        # and see if anyone has just won (only consider wins created by the last move):
        if self.checkForWin(x,y):
            tkMessageBox.showwarning("Game over","Player "+self.color+" won!")
            #self.opponent.kill() # would only work with python 2.6..
            sys.exit()
        # or if it's a draw:
        if self.movecount == 19*19:
            tkMessageBox.showwarning("Game over","It's a draw!")
            sys.exit()

    def checkMove(self, x,y):
        if x < 0 or x > 18 or y < 0 or y > 18:
            # move is invalid (index out of range)
            return False
        elif self.board[x][y] != -1:
            # field is already occupied
            # tkMessageBox.showwarning("invalid move:","invalid move: field already occupied!")
            return False
        else:
            return True # Move is OK.

    def checkForWin(self,x,y):
        #Richtungen
        dirs = [(1,0), (0,1), (1,1), (1,-1)]
        for (dx,dy) in dirs:
            anz1 = self.countSame(x,y,dx,dy)
            anz2 = self.countSame(x,y,-dx,-dy)
            #print "length: ", anz1+anz2+1
            if anz1+anz2 >= 4: # a row or length at least 5 wins..
                return True
        return False

    def countSame(self, x,y,dx,dy):
        c = -1  # don't count x,y itself
        me = self.board[x][y]
        while x >= 0 and y >= 0 and x <= 18 and y <= 18 and self.board[x][y] == me:
            c = c + 1
            x = x + dx
            y = y + dy
        return c

myapp = Application()

myapp.mainloop()

