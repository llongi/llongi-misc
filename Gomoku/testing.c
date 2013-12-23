#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include "attacker.h"
#include "attacker_support.c"

/* Randomly add stones to empty field, print result */

int main (void) {
	int x, y;
	bool player = false;

	/* Seed PRNG */
	srand (time (NULL));

	/* Init fields */
	init_fields ();

	for (int i = 0; i < SIZE*SIZE; i++) {
		player = !player;

		do { /* Find position */
			x = rand () % SIZE;
			y = rand () % SIZE;
		} while (! valid_move (x, y));

		if (player) {
			update_field_values (x, y, PLAYER);
			fprintf (stderr, "Field for player:\n");
			fprintf (stderr, "-------------------------------\n");
			print_field (PLAYER);
			fprintf (stderr, "-------------------------------\n");
		} else {
			update_field_values (x, y, OPPONENT);
			fprintf (stderr, "Field for opponent:\n");
			fprintf (stderr, "-------------------------------\n");
			print_field (OPPONENT);
			fprintf (stderr, "-------------------------------\n");
		}
	}

	return (0);
}
