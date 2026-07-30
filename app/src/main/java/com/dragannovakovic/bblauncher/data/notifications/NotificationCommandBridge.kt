package com.dragannovakovic.bblauncher.data.notifications

interface NotificationCommands {
    fun dismissNotification(key: String)

    fun dismissAllNotifications()
}

object NotificationCommandBridge {
    private var commands: NotificationCommands? = null

    @Synchronized
    fun attach(commands: NotificationCommands) {
        this.commands = commands
    }

    @Synchronized
    fun detach(commands: NotificationCommands) {
        if (this.commands === commands) {
            this.commands = null
        }
    }

    @Synchronized
    fun dismiss(key: String): Boolean {
        val activeCommands = commands ?: return false
        activeCommands.dismissNotification(key)
        return true
    }

    @Synchronized
    fun dismissAll(): Boolean {
        val activeCommands = commands ?: return false
        activeCommands.dismissAllNotifications()
        return true
    }
}
