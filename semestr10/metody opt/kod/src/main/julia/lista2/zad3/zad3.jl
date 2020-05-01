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
    (processorCount,taskCount) = size(taskDurationPerProcessor)
    processors = 1:processorCount
    tasks = 1:taskCount

    model = Model(GLPK.Optimizer)

    #= scheduledTasks are indexes for tasks after applying schedule =#
    scheduledTasks = 1:taskCount
    #main variables
    @variable(model, 1 <= schedule[scheduledTasks] <= taskCount, Int)
    @variable(model, 0 <= maxTime )
    #minimise r
    @objective(model,Min, maxTime )


    #mapping variables between tasks and scheduledTasks
    #swapping the tasks to scheduled tasks
    @variable(model, swaps[tasks,scheduledTasks], Bin )
    for task=tasks
        @constraint(model, sum(swaps[task,targetTask] for targetTask in scheduledTasks ) == 1  )
    end
    for targetTask=scheduledTasks
        @constraint(model, sum(swaps[task,targetTask] for task in tasks ) == 1  )
    end
    #swapping the tasks values to scheduled tasks
    @variable(model, swapsValues[tasks,scheduledTasks], Int )
    for task=tasks
        for targetTask=scheduledTasks
            @constraint(model, swapsValues[task,targetTask] == task * swaps[task,targetTask] )
        end
    end
    for targetTask=scheduledTasks
        @constraint(model, schedule[targetTask] == sum(swapsValues[task,targetTask] for task in tasks ) )
    end
    #swapping the task durations to scheduled tasks per processor
    @variable(model, swapsDurations[processors,tasks,scheduledTasks], Int )
    for processor=processors
        for task=tasks
            for targetTask=scheduledTasks
                @constraint(model, swapsDurations[processor,task,targetTask] == taskDurationPerProcessor[processor,task] * swaps[task,targetTask] )
            end
        end
    end


    #helper variables and constrinats
    @variable(model, taskLength[processors,scheduledTasks] >= 0 )
    @variable(model, taskStart[processors,scheduledTasks] >= 0 )
    @variable(model, taskEnd[processors,scheduledTasks] >= 0 )
    @variable(model, possibleTaskStart[processors,scheduledTasks,1:2] >= 0 )

    for processor=processors
        for task=scheduledTasks
            if processor == 1
                if task == 1
                    @constraint(model, possibleTaskStart[processor,task,1] == 0  )
                    @constraint(model, possibleTaskStart[processor,task,2] == 0  )
                else
                    @constraint(model, possibleTaskStart[processor,task,1] == taskEnd[processor,task-1] )
                    @constraint(model, possibleTaskStart[processor,task,2] == taskEnd[processor,task-1] )
                end
            else
                if task == 1
                    @constraint(model, possibleTaskStart[processor,task,1] == taskEnd[processor-1,task] )
                    @constraint(model, possibleTaskStart[processor,task,2] == taskEnd[processor-1,task] )
                else
                    @constraint(model, possibleTaskStart[processor,task,1] == taskEnd[processor-1,task] )
                    @constraint(model, possibleTaskStart[processor,task,2] == taskEnd[processor,task-1] )
                end
            end
            @constraint(model, taskEnd[processor,task] == taskStart[processor,task] + taskLength[processor,task] )

            @constraint(model, taskLength[processor,task] == sum( swapsDurations[processor,initialTask,task] for initialTask in tasks ) )
        end
    end
    for processor=processors
        for task=scheduledTasks
            @constraint(model, taskStart[processor,task] >= possibleTaskStart[processor,task,1] )
            @constraint(model, taskStart[processor,task] >= possibleTaskStart[processor,task,2] )
        end
    end
    @constraint(model, maxTime == taskEnd[processorCount,taskCount] )


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
        _possibleTaskStart=value.(possibleTaskStart)


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

        for processor=processors
            println( "processor ", processor );
            for task=scheduledTasks
                println( "\ttask ", _schedule[task],
                    ", start: ",rd(_taskStart[processor,task]),
                        "(possible values: [1]=",rd(_possibleTaskStart[processor,task,1])," [2]=",rd(_possibleTaskStart[processor,task,2]),")",
                    ", end: ",rd(_taskEnd[processor,task]),
                    ", length:",rd(_taskLength[processor,task])
                 );
            end
        end
        println( "maxTime = ",_maxTime );

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
A -> B -> D -> C ?
ABBDDDCCCC
 AABB DDD C
   AAABB DDDC
t=13
=#
println("Expected: 1->2->3->4 t=13");
solveAndLog(
    [  #A  B  C  D
        1. 2. 4. 3.;
        2. 2. 1. 3.;
        3. 2. 1. 3.
    ]
);