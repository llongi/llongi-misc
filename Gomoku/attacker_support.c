#include "attacker_support.h"

/* Error-checked malloc */
void* emalloc (size_t n) {
	void* temp = malloc (n);

	if (temp == NULL) {
		/* Whoops, this is bad */
		fprintf (stderr, "Fatal error: Unable to allocate memory!\n");
		exit (1);
	}

	return (temp);
}

/* Init fields */
void init_fields () {
	for (int x = 0; x < SIZE; x++) {
		for (int y = 0; y < SIZE; y++) {
			field[x][y] = EMPTY;
			field_player[x][y] = 1;
			field_opponent[x][y] = 1;
		}
	}
}

/* Check if coordinates are still in the field */
bool in_field (int x, int y) {
	return (x >= 0 && y >= 0 && x < SIZE && y < SIZE);
}

/* Check if move is valid */
bool valid_move (int x, int y) {
	return (in_field (x, y) && field[x][y] == EMPTY);
}

/* Return number of unicolored stones in given direction */
int search_possible_line (int x, int y, int dx, int dy, int player) {
	int cx = x + dx;
	int cy = y + dy;
	int stones = 0;

	/* Check colors of stones in given direction */
	while (in_field (cx, cy) && field[cx][cy] == player) {
			cx += dx;
			cy += dy;
			stones++;
	}

	return (stones);
}

/* Find longest line stone is part of */
Line longest_line (int x, int y, int dx, int dy, int player) {
	/* Array of possible directions etc. */

	Point ps = (struct PointStruct *) emalloc (sizeof (*ps));
	ps->x = x;
	ps->y = y;
	Point pe = (struct PointStruct *) emalloc (sizeof (*pe));
	pe->x = x;
	pe->y = y;

	Line l = (struct LineStruct *) emalloc (sizeof (*l));
	l->start = ps;
	l->end = pe;
	l->len = 1;

	/* Always check in two directions: Up/down, left/right, etc. */
	int u = search_possible_line (ps->x, ps->y, dx, dy, player);
	int d = search_possible_line (pe->x, pe->y, -dx, -dy, player);

	/* If longer, find new start/end point */
	if ((u + d + 1) > l->len) {
		l->len = u + d + 1;
		l->start->x = x + u*dx;
		l->start->y = y + u*dy;
		l->end->x = x + d*(-dx);
		l->end->y = y + d*(-dy);
	}

	#ifdef DEBUG
	fprintf (stderr, "Longest line for (%d, %d): (%d, %d) -> (%d, %d)\n",
		 x, y, l->start->x, l->start->y, l->end->x, l->end->y);
	#endif

	return (l);
}

int get_value_in_field (int x, int y, int player) {
	if (player == PLAYER) {
		return (field_player[x][y]);
	} else {
		return (field_opponent[x][y]);
	}
}

void set_value_in_field (int x, int y, int player, int v) {
	if (player == PLAYER) {
		field_player[x][y] = v;
	} else {
		field_opponent[x][y] = v;
	}
}

/* Update values in field. Player false is opponent, true is us */
void update_field_values (int x, int y, int player) {
	if (player == PLAYER) {
		field[x][y] = PLAYER;
		field_player[x][y] = 0;
		field_opponent[x][y] = -1;
		update_field_values_internal(x, y, player);
	} else {
		field[x][y] = OPPONENT;
		field_player[x][y] = -1;
		field_opponent[x][y] = 0;
		update_field_values_internal(x, y, player);
	}
}

void update_field_values_internal (int x, int y, int player) {
	int cx, cy;
	int dirs[4][2] = {{0,1},{1,0},{1,1},{1,-1}};

	#ifdef DEBUG
	fprintf (stderr, "Updating field values for field (%d, %d).\n", x, y);
	#endif

	for (int i = 0; i < 4; i++) {
		/* First direction */
		cx = x + dirs[i][0];
		cy = y + dirs[i][1];
		while (in_field (cx, cy) && get_value_in_field (cx, cy, player) == 0) {
			cx += dirs[i][0];
			cy += dirs[i][1];
		}

		#ifdef DEBUG
		fprintf (stderr, "Field needs update: (%d, %d), current value is %d.\n", cx, cy, get_value_in_field (cx, cy, player));
		#endif

		if (in_field (cx, cy) && get_value_in_field (cx, cy, player) != -1 && get_value_in_field (cx, cy, player) != 0) {
			Line l = longest_line (cx, cy, dirs[i][0], dirs[i][1], player);
			if (get_value_in_field (cx, cy, player) < l->len)
				set_value_in_field (cx, cy, player, l->len);
			free (l->start);
			free (l->end);
			free (l);
		}

		/* Second direction */
		cx = x - dirs[i][0];
		cy = y - dirs[i][1];
		while (in_field (cx, cy) && get_value_in_field (cx, cy, player) == 0) {
			cx -= dirs[i][0];
			cy -= dirs[i][1];
		}

		#ifdef DEBUG
		fprintf (stderr, "Field needs update: (%d, %d), current value is %d.\n", cx, cy, get_value_in_field (cx, cy, player));
		#endif

		if (in_field (cx, cy) && get_value_in_field (cx, cy, player) != -1 && get_value_in_field (cx, cy, player) != 0) {
			Line l = longest_line (cx, cy, -dirs[i][0], -dirs[i][1], player);
			if (get_value_in_field (cx, cy, player) < l->len)
				set_value_in_field (cx, cy, player, l->len);
			free (l->start);
			free (l->end);
			free (l);
		}
	}
}

/* Print table of field */
void print_field (int player) {
	fprintf (stderr, "   ");

	for (int j = 0; j < SIZE; j++) {
			fprintf (stderr, "%2d ", j);
	}

	fprintf (stderr, "\n");

	for (int i = 0; i < SIZE; i++) {
		fprintf (stderr, "%2d ", i);

		for (int j = 0; j < SIZE; j++) {
			fprintf (stderr, "%2d ", get_value_in_field (i, j, player));
		}

		fprintf (stderr, "\n");
	}
}
