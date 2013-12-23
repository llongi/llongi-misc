#!/bin/bash

c=0;
d=0;
e=0;

n=500;

for i in $(seq 1 $n); do
	echo "Running simulation $i..."

	g=$(python gomoku-console.py $1 $2 | tail -n1)
	if echo "$g" | grep black; then
		c=$[c+1]
	elif echo "$g" | grep white; then
		d=$[d+1]
	else
		e=$[e+1]
	fi >/dev/null

	g=$(python gomoku-console.py $2 $1 | tail -n1)
	if echo "$g" | grep white; then
		c=$[c+1]
	elif echo "$g" | grep black; then
		d=$[d+1]
	else
		e=$[e+1]
	fi >/dev/null
done

echo "Win count for $1: $c/$[n+n]"
echo "Win count for $2: $d/$[n+n]"
echo "Number of draws: $e/$[n+n]"
