#=
zad:
- Julia version: 1.4.1
- Author: Piotr Olejarz 229803
- Date: 2020-05-26
=#
using LinearAlgebra

using Dates
using JuMP
using GLPK


#input
#  M - machines
#  J - jobs
#  p:MxJ -> R - processing time
#  c:MxJ -> R - cost
#  T:M -> R - machine time availability
function alg(
    M,
    J,
    c,
    p,
    T;
)
    G = graph(M, J);
    f,i = alg( M,J,c,p,T,G );
    return f,i;
end

struct Graph
    V
    E
end


function partOf( edge, vertex)
    return edge[1] == vertex || edge[2] == vertex
end

function indexOf( array, element)
    for i=1:length(array)
        if( array[i] == element )
            return i;
        end
    end
    error("element not found");
end

function graph( M, J )
    V = []
    for j in J
        push!(V,j);
    end
    for m in M
        push!(V,m);
    end

    E = []
    for j in J
        for m in M
            push!(E,(m,j));
        end
    end
    return Graph(V,E)
end


function delta( G, v )
    ret=[]
    for e in G.E
        if partOf(e, v)
            push!(ret,e)
        end
    end
    return ret
end

function d( G, v )
    ret=0
    for e in G.E
        if partOf(e, v)
            ret = ret + 1;
        end
    end
    return ret
end

#input
#  M - machines
#  J - jobs
#  p:MxJ -> R - processing time
#  c:MxJ -> R - cost
#  T:M -> R - machine time capacity
#  G = (V,E)
#    V = M+J - vertices
#    E = MxJ - edges
#    delta:V -> P(V) - set of edges that have one point in given vertex
#    d:V -> R - vertex degree
#output
#  x:MxJ -> {0,1} - whether the Job is assigned (1) to Machine or not (0)
function alg(
    M,
    J,
    c,
    p,
    T,
    G;
)
    #init
    F = [];
    M2 = copy(M);
    J = copy(J);
    T = copy(T);
    iterations = 0
    while !isempty(J)
        iterations += 1
        # solve LP and remove every x[i,j] = 0
        #println( "\n\nloop #",iterations )
        #println( "J:", J );
        #println( "G.E:", G.E );
        #println( "M2:", M2 );
        status,x = solveLP( M,J,M2,c,p,T,G );
        if status != MOI.OPTIMAL
            error("No optimal solution!");
        end

        #println( "solution:" );
        i=1
        while( i <= length(G.E) )
            e = G.E[i];
            check = x[e] <= eps()
            #println( "x[",e,"]=", x[e], " (zero? ", check,")" );
            if( check )
                #println( "deleting ",G.E[i],"@index=",i," from G.E" );
                deleteat!( G.E, i );
                i-=1
            end
            i+=1
        end

        # if there is variable with x_ij=1 then update F=F+{ij}, J=J-{j}, T_i=T_i-p_ij
        for e in G.E
            x_e_minus_1 = x[e] - 1
            check = x_e_minus_1 >= -eps()
            #println( "x[",e,"]=", x[e], " (one? ", check ,")" );
            if check
                push!(F,e);
                index=indexOf(J,e[2])
                #println( "deleting ",e[2],"@index=",index," from J=", J );
                deleteat!(J,index);

                e1=e[1]
                old = T[e1]
                T[e1]=old-p[e];
                #println("new T[",e1,"]=",T[e1]," (", old,"-",p[e],")")
            end
        end
        #if there is machine i with d(i)=1 or
        #    a machine i with d(i)=2 and Sum x_ij over j in J
        #then update M2=M2-{i}

        for i in M2
            degree = d(G,i)
            if degree == 1 || ( degree == 2 && sum( getFromX(x,i,j) for j in J) - 1 >= eps() )
                index=indexOf(M2,i)
                #println( "deleting ",i,"@index=",index," from M2=", M2 );
                deleteat!(M2,index);
            end
        end
    end
    return F,iterations;
end

function getFromX( x, i, j )
    try
        return x[(i,j)]
    catch error
        return 0
    end
end

#input
#  J - jobs
#  M - machines
#  p:MxJ -> R - processing time
#  c:MxJ -> R - cost
#  T:M -> R - machine time capacity

#  E = MxJ - edges
#  delta(?) - ?
#output
#  x:MxJ -> {0,1} - whether the Job is assigned (1) to Machine or not (0)

#minimize:
#  sum c(e)*x(e) over e in E
#constaints:
#  for all j in J
#    assert 1 = sum x(e) over e in delta(j)
#  for all i in M2
#    assert T(i) >= sum p(e)x(e) over e in delta(j)
#  for all e in E
#    assert x(e) >= 0
function solveLP(
    M,
    J,
    M2,
    c,
    p,
    T,
    G;
)
    model = Model(GLPK.Optimizer)

    @variable(model, x[G.E] )

    #println( "\n\nsolveLP" )
    #println( "M:",M )
    #println( "J:",J )
    #println( "M2:",M2 )
    #println( "T:",T )
    #println( "c:",c )
    #println( "x:",x )
    #println( "G.E:",G.E )

    #for e in G.E
    #    print( "c[",e,"]="); println( c[e] );
    #    print( "x[",e,"]="); println( x[e] );
    #end
    @objective(model, Min, sum( c[e]*x[e] for e in G.E ) )



    for j in J
        @constraint(model, sum( x[e] for e in delta(G,j) ) == 1  )
    end

    for i in M2
        #println( "Constrait for " , i )
        #println( "sum p[e]*x[e] for e in " , delta(G,i) )
        #for e in delta(G,i)
        #    println( "  ",e,"->",p[e],"*",x[e] )
        #end
        #println( "<= ",T[i] )
        @constraint(model, sum( p[e]*x[e] for e in delta(G,i) ) <= T[i]  )
    end
    for e in G.E
        @constraint(model, x[e] >= 0  )
    end

    optimize!(model)
    status = termination_status(model)

    if status == MOI.OPTIMAL
        return status, value.(x)
    else
        return status, nothing
    end
end

struct Problem
    fileName
    index
    M
    J
    c
    p
    T
end

function readFile( fileName )
    problemArray = []
    open( fileName ) do file
        lines = readlines(file)
        problems = parse( Int, lines[1]);
        iterator = 1;

        for problemNo=1:problems
            #println( "problemNo: ", problemNo );
            v = split( lines[ iterator+=1 ], c -> c == ' ' );
            machines = parse( Int, v[2] )
            jobs = parse( Int, v[3] )
            M = []
            J = []
            for machine=1:machines
                push!(M,string("m",machine))
            end
            for job=1:jobs
                push!(J,string("j",job))
            end

            c = Dict()
            p = Dict()
            T = Dict()
            #println( "  machines(M) ", machines, ", jobs(J): ", jobs );
            for machine=1:machines
                costs = split( lines[ iterator+=1 ], c -> c == ' ' );
                #println( "  machine[",machine,"] costs (c): " )
                for job=1:jobs
                    c[M[machine],J[job]] = parse( Int, costs[job+1] )
                    #println( "    c[",machine,job,"]=", c[machine,job] )
                end
            end
            for machine=1:machines
                times = split( lines[ iterator+=1 ], c -> c == ' ' );
                #println( "  machine[",machine,"] time usage (p): " )
                for job=1:jobs
                    p[M[machine],J[job]] = parse( Int, times[job+1] )
                    #println( "    p[",machine,job,"]=", p[machine,job] )
                end
            end
            capacities = split( lines[ iterator+=1 ], c -> c == ' ' );
            #println( "  time capacities (T): " )
            for machine=1:machines
                T[M[machine]] = parse( Int, capacities[machine+1] )
                #println( "  T[",machine,"]=", T[machine] )
            end
            #println("\n\nread file")
            #println("M ",M)
            #println("J ",J)
            prob = Problem(fileName,problemNo,M,J,c,p,T)
            #println("prob.M ",prob.M)
            #println("prob.J ",prob.J)
            push!( problemArray, prob )
        end
    end
    return problemArray
end

function runAndPrint( problem )
    println( "################################################\n" );
    println( "Running problem #", problem.index, " from file: ", problem.fileName );
    startTime=Dates.now();
    assignments,iterations = alg( problem.M, problem.J, problem.c, problem.p, problem.T )
    endTime=Dates.now();
    totalRunTime = (Dates.value(endTime)-Dates.value(startTime))/1000.0;
    println( "Algoritm finished in ", iterations, " iterations and ", totalRunTime , " seconds" );
    println( "Result:" );

    totalCost = 0;
    totalTime = Dict( machine => 0 for machine in problem.M );
    println("######### M: ",problem.M);
    println("######### totalTime: ",totalTime);

    for assignment in assignments
        job = assignment[2]
        machine = assignment[1]
        time = problem.p[machine,job];
        cost = problem.c[machine,job];
        totalCost += cost;
        totalTime[machine] += time;
        println( "  exetue job " , job , " on machine ", machine, " in time " , time , " and cost ", cost );
    end
    println( "  Total cost: ", totalCost )
    println( "  Total machine times:" )
    for machine in problem.M
        totalMachineTime = totalTime[machine]
        machineLimit = problem.T[machine]
        isOverMax = totalMachineTime > machineLimit
        isUnder2TimesMAx = totalMachineTime > 2*machineLimit
        println( "    machine ", machine, " required for ", totalTime[machine], " time, (max:", machineLimit,", >max? ",isOverMax,", >2*max? ",isUnder2TimesMAx," )" );
    end
    println( "################################################\n\n" );
end
function parseAndRun( fileName )
    for problem in readFile( fileName );
        runAndPrint( problem );
    end
end


parseAndRun( "./data/gap1.txt" )

# runAndPrint( Problem(
#     "internal",
#     1,
#     ["m1","m2"],
#     ["j1","j2","j3","j4"],
#     Dict(
#         ("m1","j1") => 1, ("m2","j1") => 5,
#         ("m1","j2") => 4, ("m2","j2") => 1,
#         ("m1","j3") => 4, ("m2","j3") => 1,
#         ("m1","j4") => 1, ("m2","j4") => 5
#     ),
#     Dict(
#         ("m1","j1") => 2, ("m2","j1") => 2,
#         ("m1","j2") => 2, ("m2","j2") => 3,
#         ("m1","j3") => 1, ("m2","j3") => 3,
#         ("m1","j4") => 1, ("m2","j4") => 2
#     ),
#     Dict("m1"=>3,"m2"=>4)
# ) )



