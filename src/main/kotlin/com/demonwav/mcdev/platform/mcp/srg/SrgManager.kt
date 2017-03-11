/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2017 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.mcp.srg

import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.util.Getter
import org.jetbrains.concurrency.Promise
import org.jetbrains.concurrency.rejectedPromise
import org.jetbrains.concurrency.runAsync
import java.nio.file.Paths

class SrgManager(val files: Set<String>) {

    var srgMap: Promise<McpSrgMap> = rejectedPromise("SRG map not loaded")
        @Synchronized get
        private set

    val srgMapNow: McpSrgMap?
        @Synchronized get() = (srgMap as? Getter<*>)?.get() as McpSrgMap?

    @Synchronized
    fun parse() {
        if (srgMap.state == Promise.State.PENDING)  {
            return
        }

        srgMap = if (files.isNotEmpty()) {
            runAsync {
                // Load SRG map from files
                McpSrgMap.parse(Paths.get(files.find { it.endsWith("mcp-srg.srg") }))
            }.rejected {
                PluginManager.processException(it)
            }
        } else {
            // Path to SRG files is unknown
            rejectedPromise("No mapping data available")
        }
    }

    companion object {
        private val map = HashMap<Set<String>, SrgManager>()

        @JvmStatic fun getInstance(files: Set<String>) = map.computeIfAbsent(files, ::SrgManager)
    }
}