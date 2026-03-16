package com.prey.json.actions

/**
 * Abstract base class for action handlers.
 *
 * This class serves as the foundation for action implementations, providing
 * common command strings and execution status constants used to communicate
 * the state and intent of various operations.
 */
abstract class BaseAction {

    companion object {
        const val CMD_GET = "get"
        const val CMD_START = "start"
        const val CMD_STOP = "stop"
        const val STATUS_STARTED = "started"
        const val STATUS_STOPPED = "stopped"
        const val STATUS_FAILED = "failed"
    }

}