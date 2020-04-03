/* parameters */
set camperType;
set cities;

param availability{cities, camperType} integer;
param distance{cities, cities} >=0;
param transportCostPerDistance{camperType} > 0;

/*camperCanBeReplacedBy(baseCamper,replacement) := whether replacement can be used instead of baseCamper*/
param camperCanBeReplacedBy{camperType, camperType} binary
;


/* variables */
/*camperMovement( from in cities, to in cities, replacement in camperType, missing in camperType )
'replacement' camper is moved 'from' city 'to' city in order to fill 'missing' camper
*/
var camperMovement{
    cities,
    cities,
    camperType,
    camperType
} >=0 integer
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

/*there are enough campers in city to send out */
s.t. enoughCampers2{city in cities, camper in camperType} :
/*base amount*/ max(0, availability[city, camper])
/*outgoing campers*/ - ( sum{to in cities, requiredCamper in camperType} camperMovement[city,to,camper,requiredCamper] * camperCanBeReplacedBy[requiredCamper, camper] )
/*incoming campers*/ + ( sum{from in cities} camperMovement[from,city,camper,camper] * camperCanBeReplacedBy[camper, camper] )
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



printf "City stats:\n";
for {city in cities}
{
    printf "city: %s\n", city;
    for{ camper in camperType }
    {
        printf "\tcamper %s\n", camper;
        printf "\t\tbaseAvailability=%d\n", availability[city,camper];
        printf "\t\tincoming(same type)=%d\n", sum{from in cities} camperMovement[from,city,camper,camper];
        printf "\t\tincoming(replacements)=%d\n", sum{from in cities, replacement in camperType} if replacement = camper then 0 else camperMovement[from,city,replacement,camper];
        printf "\t\toutgoing=%d\n", sum{to in cities, requiredCamper in camperType} camperMovement[city,to,camper,requiredCamper];
        printf "\t\tresult=%d\n",
            availability[city, camper]
            + ( sum{from in cities, replacement in camperType} camperMovement[from,city,replacement,camper] )
            - ( sum{to in cities, requiredCamper in camperType} camperMovement[city,to,camper,requiredCamper] )
        ;
    }
}

printf "Movements:\n";
for {from in cities, to in cities, replacement in camperType, missing in camperType}
{
    printf if camperMovement[from, to, replacement, missing] > 0
        then "camperMovement[%s,%s,%s,%s]=%d (dist: %.2fkm, cost: $%.2f, $%.2f/km )\n"
        else "",
        from,
        to,
        replacement,
        missing,
        camperMovement[from, to, replacement, missing],
        distance[from, to],
        camperMovement[from, to, replacement, missing] * distance[from, to] * transportCostPerDistance[replacement],
        transportCostPerDistance[replacement]
    ;
}
printf "Total cost: $%.4f\n",solution;



/*integer/binary are not needed in this case*/


for {to in cities, missing in camperType, from in cities, replacement in camperType}
{
    printf if camperMovement[from, to, replacement, missing] > 0
        then "%s\t%s\t%s\t%s\t%d\n"
        else "",
        to,
        missing,
        from,
        replacement,
        camperMovement[from, to, replacement, missing]
    ;
}