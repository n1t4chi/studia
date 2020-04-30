#Piotr Olejarz 229803
using LinearAlgebra

using JuMP
using GLPK


#isPropAvailableByServer - Matrix representing availibility of resource per server (prop x server)
#accessTimes - arrays of access times for each server
function solve(
    isPropAvailableByServer::Matrix{Int64},
    accessTimes::Vector{Float64};
    verbose = true
)
    (serverCount, propCount)=size(isPropAvailableByServer);
    if serverCount != length(accessTimes)
        error( "Incompatible sizes, parameter sizes:matrix[propCount,serverCount], vector[serverCount]" );
    end

    model = Model(GLPK.Optimizer)

    #acc:Server -> {notUsed(0),used(1)} - is server used to access any resource
    @variable(model, acc[1:serverCount], Bin)

    #time:Server -> time - time used on server (if used[i] then accessTimes[i] else 0)
    @variable(model, time[1:serverCount] >= 0 )

    #minimise r
    @objective(model,Min, sum(time))

    #match time with acc. if acc[i] = used(1) then time[i] = accessTimes[i] else time[i]=0
    for server=1:serverCount
        @constraint(model, time[server] == acc[server] * accessTimes[server] )
    end

    #is there more than one server to access property
    for prop=1:propCount
        @constraint(model, sum( isPropAvailableByServer[server,prop]*acc[server] for server in 1:serverCount ) >= 1 )
    end

    #print(model)

    if verbose
        optimize!(model)
    else
      set_silent(model)
      optimize!(model)
      unset_silent(model)
    end
    status = termination_status(model)

    if status == MOI.OPTIMAL
        return status, objective_value(model), value.(acc), value.(time)
    else
        return status, nothing,nothing
    end
end #solve

function log( status, cost, acc, time, isPropAvailableByServer )
    if status== MOI.OPTIMAL
        (serverCount, propCount)=size(isPropAvailableByServer);
        propsLeftToAccess=collect(1:propCount);
        for server=1:serverCount
            if acc[server] > 0
                accessedProps=[]
                accessedPropsString=""
                for prop in propsLeftToAccess
                    if isPropAvailableByServer[server,prop] > 0
                        if length(accessedProps) != 0
                            accessedPropsString= "$(accessedPropsString), "
                        end
                        accessedPropsString= "$(accessedPropsString)$(prop)"


                        push!(accessedProps,prop);
                    end
                end
                println("Access server ", server , " at cost ", time[server], ", accessed resources: ", accessedPropsString );
                deleteat!(propsLeftToAccess,findall(x -> x in accessedProps, propsLeftToAccess));
            end
        end
        for prop in propsLeftToAccess
            println("Unable not access ", prop);
        end
        println("Total cost: ", cost);
    else
        println("Status: ", status);
    end
end;

function solveAndLog( isPropAvailableByServer, accessTimes )
    println("===========start===========");

    (status, cost, acc, time) = solve( isPropAvailableByServer, accessTimes );
    log( status, cost, acc, time, isPropAvailableByServer );
    println("============end============\n\n");
end


println("expected: access only server 1 at time 1");
solveAndLog(
    [
        1 1 1 1
        0 1 1 0
        1 0 0 1
    ],
    [ 10.0, 6.0, 5.0 ]
);
println("expected: access server 2(prop 2&3) & 3(prop 1&4) with time accessTimes=9");
solveAndLog(
    [
        1 1 1 1
        0 1 1 0
        1 0 0 1
    ],
    [ 10.0, 4.0, 5.0 ]
);
println("expected: either I or II and III, both at time 10");
solveAndLog(
    [
        1 1 1 1
        0 1 1 0
        1 0 0 1
    ],
    [ 10.0, 5.0, 5.0 ]
);

println("expected either:");
println("\t1(@1-8), 4(@9-11), 5(13,13) at accessTimes = 15");
println("\t2(@1-5), 3(@6-8),4(@9-11), 5(13,13) at accessTimes = 15");
solveAndLog(
    [
        1 1 1 1 1 1 1 1 0 0 0 0 0
        1 1 1 1 1 0 0 0 0 0 0 0 0;
        0 0 0 0 0 1 1 1 0 0 0 0 0;
        0 0 0 0 0 0 0 0 1 1 1 0 0;
        0 0 0 0 0 0 0 0 0 0 0 1 1;
    ],
    [ 12.0, 8.0, 5.0, 2.0, 1.0 ]
);