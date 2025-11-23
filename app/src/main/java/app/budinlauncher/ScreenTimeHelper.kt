package app.budinlauncher

import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build

object ScreenTimeHelper {

    fun isUsageAccessPermissionGranted(context: Context): Boolean {
        return context.appUsagePermissionGranted()
    }

    fun getTodaysScreenTime(context: Context, prefs: Prefs): String {
        if (!isUsageAccessPermissionGranted(context)) {
            return "0m"
        }

        // Check if we need to update (cache for 1 minute like Olauncher)
        if (prefs.screenTimeLastUpdated.hasBeenMinutes(1).not()) {
            // Return cached value if available, otherwise calculate
            return try {
                // For now, we'll recalculate each time since we don't have a cached value
                // In a full implementation, we'd store the calculated value in prefs
                calculateScreenTime(context)
            } catch (e: Exception) {
                e.printStackTrace()
                "0m"
            }
        }

        return try {
            val result = calculateScreenTime(context)
            prefs.screenTimeLastUpdated = System.currentTimeMillis()
            result
        } catch (e: Exception) {
            e.printStackTrace()
            "0m"
        }
    }

    private fun calculateScreenTime(context: Context): String {
        return try {
            val usageStatsManager = context.getSystemService(Service.USAGE_STATS_SERVICE) as UsageStatsManager
            val appUsageStatsHashMap: MutableMap<String, AppUsageStats> = HashMap()
            val beginTime = System.currentTimeMillis().convertEpochToMidnight()
            val endTime = System.currentTimeMillis()
            val events = usageStatsManager.queryEvents(beginTime, endTime)
            val eventsMap: MutableMap<String, MutableList<UsageEvents.Event>> = HashMap()
            var currentEvent: UsageEvents.Event

            while (events.hasNextEvent()) {
                currentEvent = UsageEvents.Event()
                if (events.getNextEvent(currentEvent)) {
                    when (currentEvent.eventType) {
                        UsageEvents.Event.ACTIVITY_RESUMED, UsageEvents.Event.ACTIVITY_PAUSED, UsageEvents.Event.ACTIVITY_STOPPED, UsageEvents.Event.FOREGROUND_SERVICE_START, UsageEvents.Event.FOREGROUND_SERVICE_STOP -> {
                            var packageEvents = eventsMap[currentEvent.packageName]
                            if (packageEvents == null)
                                packageEvents = ArrayList(listOf(currentEvent))
                            else
                                packageEvents.add(currentEvent)
                            eventsMap[currentEvent.packageName] = packageEvents
                        }
                    }
                }
            }

            for ((key, value) in eventsMap) {
                val foregroundBucket = AppUsageStatsBucket()
                val backgroundBucketMap: MutableMap<String, AppUsageStatsBucket?> = HashMap()
                var pos = 0
                while (pos < value.size) {
                    val event = value[pos]
                    if (event.className != null) {
                        var backgroundBucket: AppUsageStatsBucket? = backgroundBucketMap[event.className]
                        if (backgroundBucket == null) {
                            backgroundBucket = AppUsageStatsBucket()
                            backgroundBucketMap[event.className] = backgroundBucket
                        }
                        when (event.eventType) {
                            UsageEvents.Event.ACTIVITY_RESUMED -> foregroundBucket.startMillis = event.timeStamp

                            UsageEvents.Event.ACTIVITY_PAUSED, UsageEvents.Event.ACTIVITY_STOPPED -> if (foregroundBucket.startMillis >= foregroundBucket.endMillis) {
                                if (foregroundBucket.startMillis == 0L) {
                                    foregroundBucket.startMillis = beginTime
                                }
                                foregroundBucket.endMillis = event.timeStamp
                                foregroundBucket.addTotalTime()
                            }

                            UsageEvents.Event.FOREGROUND_SERVICE_START -> backgroundBucket.startMillis = event.timeStamp
                            UsageEvents.Event.FOREGROUND_SERVICE_STOP -> if (backgroundBucket.startMillis >= backgroundBucket.endMillis) {
                                if (backgroundBucket.startMillis == 0L) {
                                    backgroundBucket.startMillis = beginTime
                                }
                                backgroundBucket.endMillis = event.timeStamp
                                backgroundBucket.addTotalTime()
                            }
                        }
                        if (pos == value.size - 1) {
                            if (foregroundBucket.startMillis > foregroundBucket.endMillis) {
                                foregroundBucket.endMillis = endTime
                                foregroundBucket.addTotalTime()
                            }
                            if (backgroundBucket.startMillis > backgroundBucket.endMillis) {
                                backgroundBucket.endMillis = endTime
                                backgroundBucket.addTotalTime()
                            }
                        }
                    }
                    pos++
                }

                val foregroundEnd: Long = foregroundBucket.endMillis
                val totalTimeForeground: Long = foregroundBucket.totalTime
                val backgroundEnd: Long = backgroundBucketMap.values
                    .mapNotNull { it?.endMillis }
                    .maxOrNull() ?: 0L

                val totalTimeBackground: Long = backgroundBucketMap.values
                    .mapNotNull { it?.totalTime }
                    .sum()

                appUsageStatsHashMap[key] = AppUsageStats(
                    maxOf(foregroundEnd, backgroundEnd),
                    totalTimeForeground,
                    backgroundEnd,
                    totalTimeBackground
                )
            }

            val totalTimeInMillis = appUsageStatsHashMap.values.sumOf { it.totalTimeInForegroundMillis }
            val viewTimeSpent = context.formattedTimeSpent((totalTimeInMillis * 1.1).toLong())

            viewTimeSpent
        } catch (e: Exception) {
            e.printStackTrace()
            "0m"
        }
    }
}