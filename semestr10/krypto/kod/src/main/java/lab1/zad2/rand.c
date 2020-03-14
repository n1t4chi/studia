#include <stdio.h>
#include <stdlib.h>
#include <time.h>

int main() {
	srand (time(NULL));

	printf( "random values:\n" );
	for( int i = 0; i<32 ; i++ )
	{
		printf( "%d,\n", rand() );
	}

	printf( "value to guess:\n" );
	printf( "%d\n", rand() );
}