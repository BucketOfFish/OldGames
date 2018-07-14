#include <iostream>
using namespace std;

class Piece
{
public:
	string name, rules, description;
	int type, x, y;

	Piece(string myName, int myType, string myRules, string myDescription, int myX, int myY);
};

Piece::Piece(string myName, int myType, string myRules, string myDescription, int myX, int myY)
{
	name = myName;
	type = myType;
	rules = myRules;
	description = myDescription;
	x = myX;
	y = myY;
}