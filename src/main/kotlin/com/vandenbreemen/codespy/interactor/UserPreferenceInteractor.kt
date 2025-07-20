package com.vandenbreemen.com.vandenbreemen.codespy.interactor

import java.io.File
import java.util.prefs.Preferences

class UserPreferenceInteractor {

    private val prefs = Preferences.userNodeForPackage(UserPreferenceInteractor::class.java)

    companion object {
        private const val KEY_LAST_PARENT_DIRECTORY = "last_parent_directory"
    }

    /**
     * Store the parent directory of the last directory the user selected
     */
    fun storeLastParentDirectory(directory: File) {
        val parentDir = directory.parentFile
        if (parentDir != null && parentDir.exists()) {
            prefs.put(KEY_LAST_PARENT_DIRECTORY, parentDir.absolutePath)
        }
    }

    /**
     * Get the last parent directory the user worked with
     * @return File object of the last parent directory, or null if none stored
     */
    fun getLastParentDirectory(): File? {
        val path = prefs.get(KEY_LAST_PARENT_DIRECTORY, null)
        return if (path != null) {
            val file = File(path)
            if (file.exists() && file.isDirectory) file else null
        } else null
    }

    /**
     * Clear all stored preferences
     */
    fun clearPreferences() {
        prefs.clear()
    }
}