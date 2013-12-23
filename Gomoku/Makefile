all: clean attacker

clean:
	rm -rf attacker
	rm -rf testing

attacker:
	g++ -Wall -Wextra -pedantic -O3 -o attacker attacker.c
	g++ -Wall -Wextra -pedantic -O3 -o testing testing.c

debug:
	g++ --debug -Wall -Wextra -pedantic -O2 -o attacker attacker.c
	g++ --debug -Wall -Wextra -pedantic -O2 -o testing testing.c
