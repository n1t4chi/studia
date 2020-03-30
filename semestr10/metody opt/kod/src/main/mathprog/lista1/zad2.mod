/* parameters */
set camperType;
set cities;

param availability{cities, camperType} /*integer*/;
param distance{cities, cities} >=0;
param transportCostPerDistance{camperType} > 0;

/*camperCanBeReplacedBy(baseCamper,replacement) := whether replacement can be used instead of baseCamper*/
param camperCanBeReplacedBy{camperType, camperType} /*binary*/;


/* variables */
/*camperMovement( from in cities, to in cities, replacement in camperType, missing in camperType )
'replacement' camper is moved 'from' city 'to' city in order to fill 'missing' camper
*/
var camperMovement{
    cities,
    cities,
    camperType,
    camperType
} >=0 /*integer*/
;

/* solution */
minimize solution: sum{
    from in cities,
    to in cities,
    replacement in camperType,
    missing in camperType
}
camperMovement[from, to, replacement, missing] * distance[from, to] * transportCostPerDistance[replacement];


/* constraints */

/*there are enough campers in city when adding/subtracting incoming/outgoing traffic to base availability */
s.t. enoughCampers{city in cities, camper in camperType} :
/*base amount*/ availability[city, camper]
/*incoming campers*/ + ( sum{from in cities, replacement in camperType} camperMovement[from,city,replacement,camper] * camperCanBeReplacedBy[camper, replacement] )
/*outgoing campers*/ - ( sum{to in cities, requiredCamper in camperType} camperMovement[city,to,camper,requiredCamper] * camperCanBeReplacedBy[requiredCamper, camper] )
>= 0
;



solve;

/* display */

/*
display camperType;
display cities;
display{city in cities, camper in camperType}: availability[city, camper];
display{from in cities, to in cities}: distance[from,to];
display{base in camperType,replacement in camperType}: camperCanBeReplacedBy[base, replacement];
display{camper in camperType}: transportCostPerDistance[camper];
*/

printf "Movements:\n";
for {from in cities, to in cities, replacement in camperType, missing in camperType}
{
    printf if camperMovement[from, to, replacement, missing] > 0
        then "camperMovement[%s,%s,%s,%s]=%d\n"
        else ""
        ,from,to,replacement,missing,camperMovement[from, to, replacement, missing]
    ;
}


/*integer/binary are not needed in this case*/