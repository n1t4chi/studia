set product;
set delay;

param profit{product} >=0;       # one dimensional array of profit
param consumption{delay,product} >=0;  # two dimensional array
param capacity{delay} >=0;      # one dimensional array of amount of delay
param demand{product} >=0;  # one dimensional array of the distribution center requirements

var production{j in product} >=demand[j]; # decision variables plus trivial bounds

maximize Profit: sum{j in product} profit[j] * production[j];

s.t. time{i in delay}: sum{j in product} consumption[i,j]*production[j] <=capacity[i];

solve;

display production;
display 'testing';
display 'profit =', sum{j in product} profit[j]*production[j];


