#ifndef ATTACKER_H
#define ATTACKER_H 1

/* Constants: debug info, size of board, number of stones needed to win */
//#define DEBUG 1
#define SIZE 19
#define LENGTH 5

/* Prototypes */
void read_opponent_move ();
bool perform_winning_move ();
bool analyze_opponent_move ();
void perform_aggressive_move ();
void perform_first_move ();
void print_move (int, int);

/* Global variable holding field */
enum Square { PLAYER = 1, OPPONENT = -1, EMPTY = 0 };
enum Square field[SIZE][SIZE];

/* Global fields with move possibilities */
int field_player[SIZE][SIZE], field_opponent[SIZE][SIZE];

#endif
