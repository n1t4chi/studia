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

param wasteProductionFromBaseResourcesByProducts{mainProducts, baseResources, wasteResources} >= 0;

param utilisationCostsOfWasteResourceByProduct{mainProducts, wasteResources} >= 0;


/* variables */
var boughtResources{baseResources} >= 0;
var producedMainProducts{mainProducts} >= 0;
var producedSideProducts{sideProducts} >= 0;


var usedBaseResourcesOnMainProducts{mainProducts, baseResources} >= 0;

var wasteResourcesFromProduction{wasteResources} >= 0;

var usedBaseResourcesOnSideProducts{sideProducts, baseResources} >= 0;
var usedWasteResourcesOnSideProducts{sideProducts, wasteResources} >= 0;
var utilizedResources{mainProducts, wasteResources} >= 0;

var mainProductsRecipe{mainProducts, baseResources} >= 0;

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
s.t. resourcesBoughtLimit {resource in baseResources} :
    baseResourceLowerLimit[resource] <= boughtResources[resource] <= baseResourceUpperLimit[resource]
;

s.t. mainProductsRecipeConstraints_bounds {product in mainProducts, resource in baseResources} :
    mainProductResourceLowerLimit[product,resource]
    <= mainProductsRecipe[product,resource]
    <= mainProductResourceUpperLimit[product,resource]
;
s.t. mainProductsRecipeConstraints_sum  :
    sum{product in mainProducts, resource in baseResources} mainProductsRecipe[product,resource] = 1
;

s.t. sideProductsRecipeConstraints {product in sideProducts, resource in baseResources} :
    sideProductBaseResourceRequirement[product,resource] * producedSideProducts[product]  = usedBaseResourcesOnSideProducts[product,resource]
;

s.t. usageOfMainResources {resource in baseResources} :
    boughtResources[resource]
    >=
    (sum{product in mainProducts} usedBaseResourcesOnMainProducts[product,resource])
    +
    (sum{product in sideProducts} usedBaseResourcesOnSideProducts[product,resource])
;
s.t. wasteProduction {resource in wasteResources} :
    wasteResourcesFromProduction[resource] =
    sum{baseResource in baseResources, product in mainProducts}
        usedBaseResourcesOnMainProducts[product, baseResource] * productionWaste[baseResource,product,resource]
;
s.t. utilisationOfWasteResources {resource in wasteResources} :
    wasteResourcesFromProduction[resource]
    =
    ( sum{product in mainProducts} utilizedResources[product,resource] )
    +
    ( sum{product in sideProducts} usedWasteResourcesOnSideProducts[product,resource] )
;

s.t. mainProduction {product in mainProducts} :
    producedMainProducts[product]
    =
    sum{resouce in baseResources}
        usedBaseResourcesOnMainProducts[product,resouce]
;

s.t. sideProduction {product in sideProducts} :
    producedSideProducts[product]
    =
    (sum{resouce in baseResources}
        usedBaseResourcesOnSideProducts[product,resouce]
    )
    +
    (sum{resouce in wasteResources}
        usedWasteResourcesOnSideProducts[product,resouce])
;


solve;

/* display */

/*
display baseResources;
display wasteResources;
display mainProducts;
display sideProducts;

display baseResourcesPrice;
display baseResourceLowerLimit;
display baseResourceUpperLimit;

display productionWaste;

display mainProductsPrice;
display sideProductsPrice;
display mainProductResourceLowerLimit;
display mainProductResourceUpperLimit;
display sideProductBaseResourceRequirement;
*/