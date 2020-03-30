/* parameters */
param n>0 integer;

param a{i in 1..n,j in 1..n} := 1/(i+j-1);
param b{i in 1..n} := sum{j in 1..n} 1/(i+j-1);
param c{i in 1..n} := b[i];

param expectedX{1..n} := 1;

/* variables */
var x{1..n} >= 0;

/* solution */
minimize solution: sum{i in 1..n} c[i] * x[i];


/* constraints */
s.t. constraint{row in 1..n}: sum{col in 1..n} a[row,col] * x[col] = b[row];


solve;

/* display */

display n;
/* debug:
display{i in 1..n, j in 1..n}: a[i,j];
display{i in 1..n}: b[i];
display{i in 1..n}: c[i];
display{i in 1..n}: expectedX[i];
*/


display{i in 1..n}: x[i];

/*display relative error ( norm2(expectedX-x)/norm2(expectedX) ), norm2(x)=sqrt( sum i=1..n x_i^2 ) */
display: 'relative error:',sqrt( sum{i in 1..n} (expectedX[i]-x[i])^2 )/sqrt( sum{i in 1..n} expectedX[i]^2 );

display: 'absolute error:',sqrt( sum{i in 1..n} ((expectedX[i]-x[i])^2) );
