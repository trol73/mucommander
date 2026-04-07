package com.mucommander.launcher

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class LauncherExecutor(
    private val cores: Int
) : ThreadPoolExecutor(
    cores,  cores, 0L, TimeUnit.MILLISECONDS, LinkedBlockingQueue()
) {
    private val runningTasks: MutableSet<LauncherTask> = HashSet()

    fun isFull(): Boolean {
        if (runningTasks.size < cores) {
            return false
        }
        runningTasks.removeIf { obj: LauncherTask? -> obj!!.isDone() }
        return runningTasks.size >= cores
    }

    fun execute(task: LauncherTask, force: Boolean): Boolean {
        if (force || (runningTasks.size < cores && task.isReadyForExecution())) {
            super.execute(task.task)
            runningTasks.add(task)
            return true
        }
        return false
    }

    fun executeFirst(tasks: MutableCollection<LauncherTask>): Boolean {
        val it = tasks.iterator()
        while (it.hasNext()) {
            val task = it.next()
            if (execute(task, false)) {
                it.remove()
                return true
            }
        }
        return false
    }
}
