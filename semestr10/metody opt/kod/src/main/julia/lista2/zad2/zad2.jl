#Piotr Olejarz 229803
using LinearAlgebra

using JuMP
using GLPK

struct Result
    #parameters
    libraries::Vector{Int64}
    functions::Vector{Int64}
    memory::Matrix{Float64}
    time::Matrix{Float64}
    functionsToDo::Vector{Int64}
    memoryLimit::Float64
    #results
    timeCost::Float64
    memoryCost::Float64
    useLibPerFunc::Vector{Int64}
    timeUsage::Vector{Float64}
    memUsage::Vector{Float64}
end

#libraries: [1:m]
#functions: [1:n]
#memory - libraries x functions -> memory usage
#time - libraries x functions -> time usage
#functionsToDo - subset of functions to be executed
#memoryLimit - limit of memory usage
function solve(
    memory::Matrix{Float64},
    time::Matrix{Float64},
    functionsToDo::Vector{Int64},
    memoryLimit::Float64;
    verbose = true
)
    if size(memory) != size(time)
        error( "Incompatible sizes, parameter sizes:matrix[m,n],matrix[m,n]" );
    end
    (libCount, funcCount)=size(memory);
    libraries = 1:libCount
    functions = 1:funcCount

    toDoSize = length(functionsToDo)

    if length(Set(functionsToDo)) != length(functionsToDo) &&max(functionsToDo) <= funcCount &&min(functionsToDo) >= 1
        error( "Functions to execute is not non-empty subset of available functions{1..",funcCount,"}" );
    end
    if memoryLimit <= 0
        error( "Non positive memory limit!" );
    end

    model = Model(GLPK.Optimizer)

    #main variables

    #actualMemoryUsage within constraints
    @variable(model, 0 <= actualMemoryUsage <= memoryLimit) #done

    #use: functions -> library ∪ {none(0)} - determines what library to use for each function that needs to be done, none if does not to be done
    @variable(model, 0 <= use[functions] <= libCount, Int) #done

    #time usage per used functionfunctions
    @variable(model, timeUsage[functions] >=0 ) #done

    #memory usage per used function
    @variable(model, memUsage[functions] >=0 ) #done
    @constraint(model, actualMemoryUsage == sum(memUsage) ) #done

    #minimise r
    @objective(model,Min, sum(timeUsage) ) #done

    #helper variables and constrinats
    @variable(model, 0 <= mapUsage[functions,libraries] <= 1, Int ) #done
    @variable(model, 0 <= mapUsageVal[functions,libraries] <= libCount, Int ) #done
    @variable(model, mapTimeUsageVal[functions,libraries] >= 0 ) #done
    @variable(model, mapMemUsageVal[functions,libraries] >= 0 ) #done
    for func=functions
        for lib=libraries
            @constraint(model, mapUsageVal[func,lib] == mapUsage[func,lib] * lib ) #done
            @constraint(model, mapTimeUsageVal[func,lib] == mapUsage[func,lib] * time[lib,func] ) #done
            @constraint(model, mapMemUsageVal[func,lib] == mapUsage[func,lib] * memory[lib,func] ) #done
        end

        @constraint(model, use[func] == sum( mapUsageVal[func,lib] for lib in libraries ) )
        @constraint(model, timeUsage[func] == sum( mapTimeUsageVal[func,lib] for lib in libraries ) )
        @constraint(model, memUsage[func] == sum( mapMemUsageVal[func,lib] for lib in libraries ) )

        if func in functionsToDo
            @constraint(model, use[func] >= 1 ) #done
        else
            @constraint(model, use[func] == 0 ) #done
        end
    end

    if verbose
        optimize!(model)
    else
      set_silent(model)
      optimize!(model)
      unset_silent(model)
    end
    status = termination_status(model)

    #print(model)

    if status == MOI.OPTIMAL
        #=
        _mapUsage=value.(mapUsage)
        _mapUsageVal=value.(mapUsageVal)
        _mapTimeUsageVal=value.(mapTimeUsageVal)
        _mapMemUsageVal=value.(mapMemUsageVal)
        _use=value.(use)
        _timeUsage=value.(timeUsage)
        _memUsage=value.(memUsage)
        for func=functions
            println( "function ", func );
            for lib=libraries
                println( "should use lib ",lib,"? ",_mapUsage[func,lib],", _mapUsageVal:",_mapUsageVal[func,lib],", ",
                "time:",_mapTimeUsageVal[func,lib],"(max:",time[lib,func],"), ",
                "mem:",_mapMemUsageVal[func,lib],"(max:",memory[lib,func],")",
             );
            end
            println( "use[",func,"]=",_use[func] );
            println( "timeUsage[",func,"]=",_timeUsage[func] );
            println( "memUsage[",func,"]=",_memUsage[func] );
        end
        =#
        return status, Result(
            #params
            collect(libraries),
            collect(functions),
            memory,
            time,
            functionsToDo,
            memoryLimit,
            #results
            objective_value(model),
            value.(actualMemoryUsage),
            value.(use),
            value.(timeUsage),
            value.(memUsage)
         );
    else
        return status, nothing
    end
end #solve

function log( status, result )
    if status== MOI.OPTIMAL
        println("Solution: " );
        for i=result.functions
            if i in result.functionsToDo
                print("\trequired");
            else
                print("\tnot required");
            end
            if result.useLibPerFunc[i] == 0
                if result.timeUsage[i] == 0 && result.memUsage[i] == 0
                    println(" function ", result.functions[i], " should not be solved" );
                else
                    println(" function ", result.functions[i], " should not be solved but for some reason uses time: ", result.timeUsage[i], " or memory: ", result.memUsage[i] );
                end
            else
                println(" function ", result.functions[i], " should be solved by library ", result.useLibPerFunc[i], " in time: ",result.timeUsage[i]," and memory: ",result.memUsage[i] );
            end
        end
        println("Time cost: ", result.timeCost );
        println("Memory usage: ", result.memoryCost, "(max:",result.memoryLimit,")" );
    else
        println("Status: ", status);
    end
end;

function solveAndLog(
    memory::Matrix{Float64},
    time::Matrix{Float64},
    functionsToDo::Vector{Int64},
    memoryLimit::Float64;
)
    println("===========start===========");

    (status,result) = solve( memory, time, functionsToDo, memoryLimit );
    log( status, result );
    println("============end============\n\n");
end




println("Expected: (f1,l1) (f2,l2), m=5 t=10");
solveAndLog(
    [
        1. 3.;
        2. 4.
     ],
    [
        4. 8.
        3. 6.
    ],
    [ 1, 2 ],
    5.
);

println("Expected: (f1,l2)(f2,l2) m=6 t=9");
solveAndLog(
    [
        1. 3.;
        2. 4.
     ],
    [
        4. 8.
        3. 6.
    ],
    [ 1, 2 ],
    6.
);

println("Expected: (f1,l1)(f2,l1) m=4 t=12");
solveAndLog(
    [
        1. 3.;
        2. 4.
     ],
    [
        4. 8.
        3. 6.
    ],
    [ 1, 2 ],
    4.
);

println("Expected: (f1,l3)(f3,l1)(f4,l1) m=8 t=10");
solveAndLog(
    [
        1. 12. 2. 3.;
        2. 11. 3. 4.;
        3. 1. 4. 5.;
    ],
    [
        5. 1. 4. 3.;
        4. 11. 3. 2.;
        3. 12. 2. 1.
    ],
    [ 1, 3, 4 ],
    8.
);