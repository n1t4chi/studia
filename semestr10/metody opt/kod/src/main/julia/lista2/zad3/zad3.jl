#Piotr Olejarz 229803
using LinearAlgebra

using JuMP
using GLPK

struct Result
    #parameters
    tasks::Vector{Int64}
    processors::Vector{Int64}
    taskDurationPerProcessor::Matrix{Float64}
    #results
    maxTime::Float64
    schedule::Vector{Int64}
end

#taskDurationPerProcessor: processors x tasks -> duration
#returns: schedule : [1..|tasks|] -> tasks - determines order in which tasks should be executed
function solve(
    taskDurationPerProcessor::Matrix{Float64};
    verbose = true
)
    (processorCount,taskCount) = size(taskDurationPerProcessor) #done
    processors = 1:processorCount #done
    tasks = 1:taskCount #done

    model = Model(GLPK.Optimizer)

    #= scheduledTasks are indexes for tasks after applying schedule =#
    scheduledTasks = 1:taskCount #done
    #main variables
    @variable(model, 1 <= schedule[scheduledTasks] <= taskCount, Int) #done
    @variable(model, 0 <= maxTime ) #done
    #minimise r
    @objective(model,Min, maxTime ) #done


    #mapping variables between tasks and scheduledTasks
    #swapping the tasks to scheduled tasks
    @variable(model, swaps[tasks,scheduledTasks], Bin ) #done
    for task=tasks
        @constraint(model, sum(swaps[task,targetTask] for targetTask in scheduledTasks ) == 1  ) #done
    end
    for targetTask=scheduledTasks
        @constraint(model, sum(swaps[task,targetTask] for task in tasks ) == 1  ) #done
    end

    #swapping the tasks values to scheduled tasks
    @variable(model, swapsValues[tasks,scheduledTasks], Int ) #done
    for task=tasks
        for targetTask=scheduledTasks
            @constraint(model, swapsValues[task,targetTask] == task * swaps[task,targetTask] ) #done
        end
    end
    for targetTask=scheduledTasks
        @constraint(model, schedule[targetTask] == sum(swapsValues[task,targetTask] for task in tasks ) ) #done
    end
    #swapping the task durations to scheduled tasks per processor
    @variable(model, swapsDurations[processors,tasks,scheduledTasks], Int ) #done
    for processor=processors
        for task=tasks
            for targetTask=scheduledTasks
                @constraint(model, swapsDurations[processor,task,targetTask] == taskDurationPerProcessor[processor,task] * swaps[task,targetTask] ) #done
            end
        end
    end


    #helper variables and constrinats
    @variable(model, taskLength[processors,scheduledTasks] >= 0 ) #done
    @variable(model, taskStart[processors,scheduledTasks] >= 0 ) #done
    @variable(model, taskEnd[processors,scheduledTasks] >= 0 ) #done

    for processor=processors
        for task=scheduledTasks
            if processor == 1
                if task == 1
                    @constraint(model, taskStart[processor,task] == 0  ) #done
                end
            else
                @constraint(model, taskStart[processor,task] >= taskEnd[processor-1,task] ) #done
            end
            if task > 1
                @constraint(model, taskStart[processor,task] >= taskEnd[processor,task-1] ) #done
            end

            @constraint(model, taskEnd[processor,task] == taskStart[processor,task] + taskLength[processor,task] ) #done

            @constraint(model, taskLength[processor,task] == sum( swapsDurations[processor,initialTask,task] for initialTask in tasks ) ) #done
        end
    end
    @constraint(model, maxTime == taskEnd[processorCount,taskCount] ) #done


    if verbose
        optimize!(model)
    else
      set_silent(model)
      optimize!(model)
      unset_silent(model)
    end
    status = termination_status(model)

    if status == MOI.OPTIMAL

        _schedule=value.(schedule)
        _maxTime=value.(maxTime)
        _taskStart=value.(taskStart)
        _taskEnd=value.(taskEnd)
        _taskLength=value.(taskLength)
        _swaps=value.(swaps)
        _swapsValues=value.(swapsValues)
        _swapsDurations=value.(swapsDurations)


        #=
        for task=tasks
            for targetTask=scheduledTasks
                println( "_swaps[",task,",",targetTask,"]=",_swaps[task,targetTask]," (val:",_swapsValues[task,targetTask],"), delays per processor:" );
                for processor=processors
                    println( "\t[p", processor ,"] -> ", _swapsDurations[processor, task, targetTask], "" );
                end
            end
        end
        println( "Task order:" );
        for targetTask=scheduledTasks
            for task=tasks
                if( _swaps[task, targetTask] > 0 )
                    println( "[", targetTask ,"] -> ", _swapsValues[task, targetTask], "(schedule:",_schedule[targetTask],")" );
                    for processor=processors
                        println( "\t[p", processor ,"] -> ", _swapsDurations[processor,task, targetTask] );
                    end
                end
            end
        end
        =#
        graph=""
        for processor=processors
            #println( "processor ", processor );

            graph=string(graph,"p",lpad(processor,2,"0"),"[");
            endLine=0
            for task=scheduledTasks
                #=
                println( "\ttask ", Int64(_schedule[task]),
                    ", start: ",rd(_taskStart[processor,task]),
                        "(possible values: [1]=",rd(_possibleTaskStart[processor,task,1])," [2]=",rd(_possibleTaskStart[processor,task,2]),")",
                    ", end: ",rd(_taskEnd[processor,task]),
                    ", length:",rd(_taskLength[processor,task])
                );
                =#
                newStart = Int64(round(_taskStart[processor,task]));
                if( endLine < newStart )
                    graph=string(graph,lpad("",newStart-endLine," "))
                    endLine = newStart
                end
                length = Int64(round(_taskLength[processor,task]));
                fill = string(Int64(_schedule[task]));
                graph=string(graph,lpad("",length,fill))
                endLine += length
            end
            graph=string(graph,lpad("",Int64(round(_maxTime))-endLine," "))
            graph=string(graph,"]\n");
        end
        #println( "maxTime = ",_maxTime );
        println( "graph\n",graph );

        return status, Result(
            #params
            collect(processors),
            collect(tasks),
            taskDurationPerProcessor,
            #results
            objective_value(model),
            value.(schedule)
         );
    else
        return status, nothing
    end
end #solve

function log( status, result )
    if status== MOI.OPTIMAL
        println("Solution: " );
        for task=result.schedule
            print(task," ");
        end
        println("");
        println("Max time: ", rd(result.maxTime) );
    else
        println("Status: ", status);
    end
end;

function rd( value )
    return round( value; digits = 2 )
end;

function solveAndLog( taskDurationPerProcessor::Matrix{Float64} )
    println("===========start===========");

    (status,result) = solve( taskDurationPerProcessor );
    log( status, result );
    println("============end============\n\n");
end

#=
Solution: 2 1 3
Max time: 8.0
p01[213333  ]
p02[ 2221113]
=#
solveAndLog(
    [
        1. 1. 4.;
        3. 3. 1.;
    ]
);
#=
Solution: 1 3 2
Max time: 9.0
p01[132222   ]
p02[  1113222]
=#
solveAndLog(
    [
        1. 4. 1.;
        3. 3. 1.;
    ]
);

#=
Solution: 1 4 2 3
Max time: 13.0
p01[1444 223333  ]
p02[  11444 22 3 ]
p03[    111444223]
=#
solveAndLog(
    [  #A  B  C  D
        1. 2. 4. 3.;
        2. 2. 1. 3.;
        3. 2. 1. 3.
    ]
);