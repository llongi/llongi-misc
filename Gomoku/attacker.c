#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>
#include "attacker.h"
#include "attacker_support.c"

/* Read move from stdin */
void read_opponent_move () {
	int x, y;

	if (fscanf (stdin, "%2d %2d", &x, &y) == EOF) {
		#ifdef DEBUG
		fprintf (stderr, "Standard input closed.\n");
		#endif
		exit (1);
	}

	/* Input coordinates range from 1 to 19,
	 * so we decrement them to get 0 to 18 */
	x--;
	y--;

	/* Check if move was valid */
	if (! valid_move (x, y)) {
		#ifdef DEBUG
		fprintf (stderr, "Opponent's move was invalid, ignoring.\n");
		#endif
		return;
	}

	/* Update field values */
	update_field_values (x, y, OPPONENT);
}

/* Find out if we can win in one move */
bool perform_winning_move () {
	for (int i = 0; i < SIZE; i++) {
		for (int j = 0; j < SIZE; j++) {
			if (field_player[i][j] >= LENGTH) {
				#ifdef DEBUG
				fprintf (stderr, "* perform_winning_move settled for (%d, %d)\n", i, j);
				#endif

				print_move (i, j);
				return (true);
			}
		}
	}

	return (false);
}

/* Analyze opponent's move */
bool analyze_opponent_move () {
	int l, x, y;
	bool candidates[SIZE][SIZE];

	/* Check for fives, then fours, then threes */
	for (int length = (LENGTH * 2); length >= (LENGTH - 2); length--) {
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				candidates[i][j] = false;
				if (field_opponent[i][j] >= length) {
					/* This field is interesting */
					candidates[i][j] = true;
				}
			}
		}

		l = 0;
		x = -1;
		y = -1;

		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				if (candidates[i][j]) {
					/* Find number in our field */
					if (field_player[i][j] > l) {
						x = i;
						y = j;
					}
				}
			}
		}

		if (x >= 0 && y >= 0) {
			#ifdef DEBUG
			fprintf (stderr, "* analyze_opponent_move settled for (%d, %d)\n", x, y);
			#endif

			print_move (x, y);
			return (true);
		}
	}

	return (false);
}

/* Perform aggressive move: Try to form a line */
void perform_aggressive_move () {
	int l, x, y;
	bool candidates[SIZE][SIZE];

	/* Check for fives, then fours, then threes */
	for (int length = (LENGTH * 2); length >= 1; length--) {
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				candidates[i][j] = false;
				if (field_player[i][j] >= length) {
					/* This field is interesting */
					candidates[i][j] = true;
				}
			}
		}

		l = 0;
		x = -1;
		y = -1;

		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				if (candidates[i][j]) {
					/* Find number in our field */
					if (field_opponent[i][j] > l) {
						x = i;
						y = j;
					}
				}
			}
		}

		if (x >= 0 && y >= 0) {
			#ifdef DEBUG
			fprintf (stderr, "* perform_aggressive_move settled for (%d, %d)\n", x, y);
			#endif

			print_move (x, y);
			return;
		}
	}
}

/* Perform first time move: make it so we can block the opponent later if we need to */
void perform_first_move () {
	/* Find where opponent placed his first stone */
	for (int i = 0; i < SIZE; i++) {
		for (int j = 0; j < SIZE; j++) {
			if (field_opponent[i][j] == 0) {
				if (valid_move (i - 1, j)) {
					i--;
				} else if (valid_move (i + 1, j)) {
					i++;
				} else if (valid_move (i, j - 1)) {
					j--;
				} else {
					j++;
				}

				#ifdef DEBUG
				fprintf (stderr, "* perform_first_move settled for (%d, %d)\n", i, j);
				#endif

				print_move (i, j);
				return;
			}
		}
	}
}

/* Perform our move */
void print_move (int x, int y) {
	/* Print and flush stdout (flushing is needed b/c of python script) */
	fprintf (stdout, "%d %d\n", x + 1, y + 1);
	fflush (stdout);

	/* Update field values */
	update_field_values (x, y, PLAYER);
}

/* Main routine */
int main (void) {
	#ifdef DEBUG
	fprintf (stderr, "Initializing...\n");
	#endif

	/* Seed PRNG */
	srand (time (NULL));

	/* Init fields */
	init_fields ();

	/* What color are we? (black/white) */
	char *color = (char *) emalloc (6);

	if (fscanf (stdin, "%5s", color) == EOF) {
		#ifdef DEBUG
		fprintf (stderr, "Standard input closed.\n");
		#endif
		exit (1);
	}

	#ifdef DEBUG
	fprintf (stderr, "Our color is %s.\n", color);
	#endif

	/* If we're black, we can start */
	if (strncmp (color, "black", 5) == 0)
		print_move (rand () % SIZE, rand () % SIZE);

	free (color);

	bool first = true;

	/* Analyze opponent moves and perform our moves */
	while (1) {
		read_opponent_move ();

		if (first) {
			perform_first_move();
			first = false;
			continue;
		}

		/* Try to win */
		if (perform_winning_move ()) continue;

		/* Try to block */
		if (analyze_opponent_move ()) continue;

		/* Be aggressive */
		perform_aggressive_move ();
	}

	return (0);
}
