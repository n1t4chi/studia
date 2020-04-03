/* parameters */
set baseResources;
set wasteResources;

set mainProducts;
set sideProducts;


param baseResourcesPrice {baseResources} >= 0 ;

param mainProductsPrice {mainProducts} >= 0 ;
param sideProductsPrice {sideProducts} >= 0 ;

param baseResourceLowerLimit {baseResources} >= 0 ;
param baseResourceUpperLimit {baseResources} >= 0 ;
check {resource in baseResources}:
    baseResourceLowerLimit[resource] <= baseResourceUpperLimit[resource]
;

param productionWaste{baseResources, mainProducts, wasteResources} >= 0;

param mainProductResourceLowerLimit {mainProducts,baseResources} >=0;
param mainProductResourceUpperLimit {mainProducts,baseResources} >=0;
check {product in mainProducts, resource in baseResources} :
    mainProductResourceLowerLimit[product,resource] <= mainProductResourceUpperLimit[product,resource]
;
param sideProductBaseResourceRequirement {sideProducts,baseResources} >=0;

param sideProductWasteCanBeUsedFromMainProduction {sideProducts,mainProducts} >= 0;

param wasteProductionFromBaseResourcesByProducts{mainProducts, baseResources, wasteResources} >= 0;

param utilisationCostsOfWasteResourceByProduct{mainProducts, wasteResources} >= 0;


/* variables */
var boughtResources{baseResources} >= 0;
var producedMainProducts{mainProducts} >= 0;
var producedSideProducts{sideProducts} >= 0;

var usedBaseResourcesOnMainProducts{mainProducts, baseResources} >= 0;

var actuallyUsedBaseResourcesOnMainProducts{mainProducts, baseResources} >= 0;
var wasteResourcesByMainProductAndBaseResource{mainProducts, baseResources, wasteResources} >= 0;
var wasteResourcesByMainProduct{mainProducts, wasteResources} >= 0;

var usedBaseResourcesOnSideProducts{sideProducts, baseResources} >= 0;
var usedWasteResourcesOnSideProductsFromMainProducts{sideProducts, mainProducts, wasteResources} >= 0;
var usedWasteResourcesOnSideProducts{sideProducts, wasteResources} >= 0;
var utilizedResources{mainProducts, wasteResources} >= 0;

var resourcesLeft { resource in baseResources } >=0;

/* solution */
maximize profit:
    0
    /*money gained on main products*/
    +(sum{product in mainProducts}
        producedMainProducts[product]*mainProductsPrice[product]
    )
    /*money gained on side products*/
    +(sum{product in sideProducts}
        producedSideProducts[product]*sideProductsPrice[product]
    )
    /*money spend on resources*/
    -(sum{resource in baseResources}
        boughtResources[resource] *
        baseResourcesPrice[resource]
    )

    /*money spend on utilisation*/
    -(sum{product in mainProducts, resource in wasteResources}
        utilizedResources[product, resource] *
        utilisationCostsOfWasteResourceByProduct[product, resource]
    )
;

/* constraints */
s.t. constraint_resourcesBoughtLimit {resource in baseResources} :
    baseResourceLowerLimit[resource] <= boughtResources[resource] <= baseResourceUpperLimit[resource]
;
s.t. constraint_usageOfMainResources {resource in baseResources} :
    /*switch on/off if the base resources have to be used all up*/
    resourcesLeft[resource] = 0
    /*
    resourcesLeft[resource] >= 0
    */
;

s.t. constraint_setUpResourcesLeft {resource in baseResources} :
    resourcesLeft[resource] =
    boughtResources[resource]
     -(sum{product in mainProducts} usedBaseResourcesOnMainProducts[product,resource])
     -(sum{product in sideProducts} usedBaseResourcesOnSideProducts[product,resource])
;


s.t. constraint_sideProductsRecipeConstraints {product in sideProducts, resource in baseResources} :
    sideProductBaseResourceRequirement[product,resource] * producedSideProducts[product]  = usedBaseResourcesOnSideProducts[product,resource]
;

s.t. constraint_mainProductsRecipeConstraints_upper {product in mainProducts, resource in baseResources} :
    actuallyUsedBaseResourcesOnMainProducts[product,resource] <=
    mainProductResourceUpperLimit[product,resource] * producedMainProducts[product]
;

s.t. constraint_mainProductsRecipeConstraints_lower {product in mainProducts, resource in baseResources} :
    mainProductResourceLowerLimit[product,resource] * producedMainProducts[product]  <=
    actuallyUsedBaseResourcesOnMainProducts[product,resource]
;


s.t. constraint_wasteProduced_fullDetails { product in mainProducts, resource in baseResources, waste in wasteResources } :
    wasteResourcesByMainProductAndBaseResource[product, resource, waste]
    = usedBaseResourcesOnMainProducts[product, resource] * productionWaste[resource,product,waste]
;

s.t. constraint_wasteProduced_byProduct { product in mainProducts, waste in wasteResources } :
    wasteResourcesByMainProduct[product, waste] =
    sum{resource in baseResources}
        wasteResourcesByMainProductAndBaseResource[product,resource,waste]
;

s.t. constraint_actualBaseUsageOnMainProducts {product in mainProducts, resource in baseResources} :
    actuallyUsedBaseResourcesOnMainProducts[product,resource] =
        usedBaseResourcesOnMainProducts[product, resource] -
        sum {waste in wasteResources}
            wasteResourcesByMainProductAndBaseResource[product, resource, waste]
;

s.t. constaint_blockInvalidWasteFromMainProductionOnSideProduction{product in mainProducts, sideProduct in sideProducts, waste in wasteResources}:
    usedWasteResourcesOnSideProductsFromMainProducts[sideProduct, product, waste] =
    usedWasteResourcesOnSideProductsFromMainProducts[sideProduct, product, waste]
    *
    sideProductWasteCanBeUsedFromMainProduction[sideProduct, product]
;


s.t. constraint_sumWasteResourcesOnSideProduction {product in sideProducts, waste in wasteResources} :
    usedWasteResourcesOnSideProducts[product, waste] =
    sum {mainProduct in mainProducts}
    usedWasteResourcesOnSideProductsFromMainProducts[ product, mainProduct, waste]
;

s.t. constraint_utilisationOfWasteResources {product in mainProducts, resource in wasteResources} :
    wasteResourcesByMainProduct[product, resource]
    =
    utilizedResources[product,resource] +
    sum{ sideProduct in sideProducts }
        usedWasteResourcesOnSideProductsFromMainProducts[sideProduct,product,resource]
;

s.t. constraint_mainProduction {product in mainProducts} :
    producedMainProducts[product]
    =
    sum{resource in baseResources} ( actuallyUsedBaseResourcesOnMainProducts[product,resource] )
;

s.t. constraint_sideProduction {product in sideProducts} :
    producedSideProducts[product]
    =
    (sum{resource in baseResources}
        usedBaseResourcesOnSideProducts[product,resource]
    )
    +
    (sum{resource in wasteResources}
        usedWasteResourcesOnSideProducts[product, resource])
;

solve;

/* display */


printf "\nBought resources:\n";
for {resource in baseResources }
{
    printf "bought[%s]=%.2fkg (cost: -$%.2f, $%.2f/kg) (min: %d max:%d)\n",
        resource,
        boughtResources[resource],
        boughtResources[resource] * baseResourcesPrice[resource],
        baseResourcesPrice[resource],
        baseResourceLowerLimit[resource],
        baseResourceUpperLimit[resource]
    ;
}
printf "Total cost of buying resources: $%.4f\n",
    sum{resource in baseResources} boughtResources[resource] * baseResourcesPrice[resource]
;

printf "\nUsed resources on main production:\n";
for {product in mainProducts, resource in baseResources}
{
    printf "baseUsage[%s, %s]=%.2fkg (total res. usage: %.2f%%)\n",
        product,
        resource,
        usedBaseResourcesOnMainProducts[product,resource],
        usedBaseResourcesOnMainProducts[product,resource] / boughtResources[resource] * 100
    ;
    printf {waste in wasteResources}
        "\twasted[%s] = %.2fkg (%.2f%%)\n",
        waste,
        wasteResourcesByMainProductAndBaseResource[product,resource,waste],
        productionWaste[resource,product,waste]
    ;
}
printf "\nActual resource usage on main production\n";
for {product in mainProducts, resource in baseResources}
{
    printf "actualBaseUsage[%s, %s]=%.2fkg (prod. composition: %.3f%%, min: %d%%, max:%d%%)\n",
        product,
        resource,
        actuallyUsedBaseResourcesOnMainProducts[product,resource],
        actuallyUsedBaseResourcesOnMainProducts[product,resource]
            / max(1, (sum {r in baseResources} actuallyUsedBaseResourcesOnMainProducts[product,r] ) )
             * 100
         ,
        mainProductResourceLowerLimit[product, resource] * 100,
        mainProductResourceUpperLimit[product, resource] * 100
    ;
}
printf "\nWaste resources after main production:\n";
for {product in mainProducts, resource in wasteResources}
{
    printf "waste[%s,%s]=%.2fkg\n",
        product,
        resource,
        wasteResourcesByMainProduct[product, resource]
    ;
}


printf "\nUsed base resources in side production:\n";
for {product in sideProducts, resource in baseResources}
{
    printf "baseUsage[%s, %s]=%.2fkg (prod. composition: %.2f%%, req: %.2f%%)\n",
        product,
        resource,
        usedBaseResourcesOnSideProducts[product, resource],
        usedBaseResourcesOnSideProducts[product, resource]
            / max(1, producedSideProducts[product])
            * 100,
        sideProductBaseResourceRequirement[product, resource] * 100
    ;
}


printf "\nUsed waste resources in side production:\n";
for {product in sideProducts, resource in wasteResources}
{
    printf "wasteUsage[%s, %s]=%.2fkg (prod. composition: %.2f%%)\n",
        product,
        resource,
        usedWasteResourcesOnSideProducts[product, resource],
        usedWasteResourcesOnSideProducts[product, resource]
            / max(1, producedSideProducts[product])
            * 100
    ;
    for {mainProduct in mainProducts}
    printf "\twasteUsage[%s, %s, %s]=%.2fkg (can be used? %s)\n",
        product,
        mainProduct,
        resource,
        usedWasteResourcesOnSideProductsFromMainProducts[product, mainProduct, resource],
        if sideProductWasteCanBeUsedFromMainProduction[product,mainProduct] == 1
            then "yes"
            else "no"
    ;
}

printf "\nActual Production:\n";
for {product in mainProducts}
{
    printf "produced[%s]=%.2fkg (income +$%.2f, $%.2f/kg)\n",
        product,
        producedMainProducts[product],
        producedMainProducts[product]*mainProductsPrice[product],
        mainProductsPrice[product]
    ;
}
printf "Total income from side production: $%.4f\n",
    sum{product in mainProducts} producedMainProducts[product]*mainProductsPrice[product]
;

printf "\nSide Production:\n";
for {product in sideProducts}
{
    printf "produced[%s]=%.2fkg (income +$%.2f, $%.2f/kg)\n",
        product,
        producedSideProducts[product],
        producedSideProducts[product]*sideProductsPrice[product],
        sideProductsPrice[product]
    ;
}
printf "Total income from side production: $%.4f\n",
    sum{product in sideProducts} producedSideProducts[product]*sideProductsPrice[product]
;

printf "\nBase resources left after production:\n";
for {resource in baseResources}
{
    printf "unused[%s]=%.2fkg\n",
        resource,
        resourcesLeft[resource]
    ;
}

printf "\nUtilised resources by production:\n";
for {product in mainProducts, resource in wasteResources}
{
    printf "utilised[%s, %s]=%.2fkg (cost -$%.2f, $%.2f/kg)\n",
        product,
        resource,
        utilizedResources[product, resource],
        utilizedResources[product, resource] * utilisationCostsOfWasteResourceByProduct[product, resource],
        utilisationCostsOfWasteResourceByProduct[product, resource]
    ;
}
printf "Total utilisation cost: $%.4f\n\n",
    sum{product in mainProducts, resource in wasteResources}
        utilizedResources[product, resource] * utilisationCostsOfWasteResourceByProduct[product, resource]
;
printf "Total profit: $%.4f\n\n", profit;

