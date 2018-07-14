using namespace std;

class Piece
{
public:
	string name, rules, description;
	int type, x, y;

	Piece(string myName, int myType, string myRules, string myDescription, int myX, int myY);
};