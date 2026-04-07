package com.mucommander.launcher

import com.mucommander.profiler.Profiler
import java.util.concurrent.Callable
import java.util.concurrent.FutureTask

class LauncherTask (
    name: String,
    private val handler: Runnable,
) : Callable<Void?> {
    private val name = "launcher.$name"
    private var depends: List<LauncherTask>? = null
    internal val task: FutureTask<Void?> = FutureTask<Void?>(this)

    fun depends(vararg tasks: LauncherTask): LauncherTask {
        depends = tasks.toList()
        return this
    }

    //@Throws(Exception::class)
    override fun call(): Void? {
        if (!depends.isNullOrEmpty()) {
            Profiler.start("$name.depends")
            for (t in depends) {
                t.task.get()
            }
            Profiler.stop("$name.depends")
        }
        Profiler.start(name)
        try {
            handler.run()
        } catch (e: Throwable) {
            printError("Launcher getTask error for $name: ", e)
        }
        Profiler.stop(name)
        onFinish()
        return null
    }

    fun isReadyForExecution(): Boolean {
        if (depends.isNullOrEmpty()) {
            return true
        }
        for (dt in depends) {
            if (!dt.isDone()) {
                return false
            }
        }
        return true
    }

    fun isDone(): Boolean = task.isDone()

    fun onFinish() {
    }

    override fun toString() = name

    fun printError(msg: String, e: Throwable) {
        println(msg)
        e.printStackTrace()
    }
}
