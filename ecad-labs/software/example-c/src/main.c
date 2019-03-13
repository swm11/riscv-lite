int myfunction(int x, int y)
{
	return x+y;
}

int main(void)
{
	// declare some variables
	int x=12, y=34, z;

	z = myfunction(x,y);
	debug_print(z);

	return z;
}
