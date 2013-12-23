#ifndef ATTACKER_SUPPORT_H
#define ATTACKER_SUPPORT_H 1

/* Types */
typedef struct PointStruct *Point;
typedef struct LineStruct *Line;

/* Useful struct: Info about a point */
struct PointStruct {
	int x;
	int y;
};

/* Useful struct: Info about a line */
struct LineStruct {
	Point start;
	Point end;
	int len;
};

/* Prototypes */
void* emalloc (size_t);
void init_fields ();
bool in_field (int, int);
bool valid_move (int, int);
int search_possible_line (int, int, int, int, int);
Line longest_line (int, int, int, int, int);
int get_value_in_field (int, int, int);
void set_value_in_field (int, int, int, int);
void update_field_values (int, int, int);
void update_field_values_internal (int, int, int);
void print_field (int);

#endif
