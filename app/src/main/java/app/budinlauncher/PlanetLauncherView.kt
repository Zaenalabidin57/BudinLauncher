package app.budinlauncher

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Vibrator
import android.os.VibrationEffect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import kotlin.math.*

class PlanetLauncherView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class Planet(
        val appModel: AppModel,
        var angle: Float = 0f,
        var radius: Float = 0f,
        var size: Float = 0f,
        var x: Float = 0f,
        var y: Float = 0f,
        var appIcon: Drawable? = null,
        var targetX: Float = 0f,
        var targetY: Float = 0f,
        var currentScale: Float = 0f,
        var targetScale: Float = 1f,
        var isSelected: Boolean = false
    )

    data class Sun(
        var x: Float = 0f,
        var y: Float = 0f,
        var radius: Float = 0f,
        var currentScale: Float = 0f,
        var targetScale: Float = 1f
    )

    private var planets = mutableListOf<Planet>()
    private var sun = Sun()
    private var centerX = 0f
    private var centerY = 0f
    private var isShowing = false
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var isLongPress = false
    private var selectedPlanet: Planet? = null
    private var longPressAnimation: ValueAnimator? = null
    private var hasMovedOutsideNeutralZone = false

    private lateinit var launcherApps: LauncherApps
    private var primaryColor = Color.WHITE
    private var primaryTextColor = Color.WHITE
    private var primaryShadeColor = Color.parseColor("#40FFFFFF")

    private val sunPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val planetPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val selectedPlanetPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 8f
        color = Color.WHITE
    }

    private val orbitPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private var onAppClickListener: ((AppModel) -> Unit)? = null
    private var vibrator: Vibrator? = null

    init {
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        loadThemeColors(context)
    }

    private fun loadThemeColors(context: Context) {
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
        primaryTextColor = typedValue.data

        theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
        val backgroundColor = typedValue.data

        // Create contrasting colors
        primaryColor = if (isColorLight(backgroundColor)) Color.BLACK else Color.WHITE
        primaryShadeColor = Color.argb(64, Color.red(primaryColor), Color.green(primaryColor), Color.blue(primaryColor))

        // Update paints with theme colors
        sunPaint.color = primaryColor
        selectedPlanetPaint.color = primaryColor
        orbitPaint.color = primaryShadeColor
        textPaint.color = primaryTextColor

        // Set text size based on theme
        textPaint.textSize = 48f * context.resources.displayMetrics.scaledDensity
    }

    private fun isColorLight(color: Int): Boolean {
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness > 0.5
    }

    fun setOnAppClickListener(listener: (AppModel) -> Unit) {
        onAppClickListener = listener
    }

    fun setApps(apps: List<AppModel>) {
        planets.clear()
        apps.forEachIndexed { index, app ->
            val appIcon = try {
                val activityList = launcherApps.getActivityList(app.appPackage, app.userHandle)
                if (activityList.isNotEmpty()) {
                    activityList[0].getIcon(0)
                } else null
            } catch (e: Exception) {
                null
            }
            planets.add(Planet(app, appIcon = appIcon))
        }
        invalidate()
    }

    fun showPlanets(touchX: Float, touchY: Float) {
        if (planets.isEmpty()) return
        
        centerX = touchX
        centerY = touchY
        sun.x = centerX
        sun.y = centerY
        sun.radius = 80f
        sun.currentScale = 0f
        sun.targetScale = 1f

        val orbitRadius = 250f
        val angleStep = (2 * PI / planets.size).toFloat()

        planets.forEachIndexed { index, planet ->
            planet.angle = index * angleStep
            planet.radius = 50f
            planet.targetScale = 1f
            planet.currentScale = 0f

            val targetX = centerX + cos(planet.angle) * orbitRadius
            val targetY = centerY + sin(planet.angle) * orbitRadius
            planet.targetX = targetX.toFloat()
            planet.targetY = targetY.toFloat()
            planet.x = centerX
            planet.y = centerY
        }

        isShowing = true
        animateAppearance()
        invalidate()
    }

    fun hidePlanets() {
        isShowing = false
        animateDisappearance()
    }

    private fun animateAppearance() {
        val animatorSet = AnimatorSet()
        
        // Sun animation
        val sunScaleAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 300
            interpolator = OvershootInterpolator(1.2f)
            addUpdateListener { animation ->
                sun.currentScale = animation.animatedValue as Float
                invalidate()
            }
        }

        // Planets animation
        val planetAnimators = planets.map { planet ->
            val scaleAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 400
                interpolator = OvershootInterpolator(1.1f)
                addUpdateListener { animation ->
                    planet.currentScale = animation.animatedValue as Float
                    val progress = animation.animatedValue as Float
                    planet.x = centerX + (planet.targetX - centerX) * progress
                    planet.y = centerY + (planet.targetY - centerY) * progress
                    invalidate()
                }
            }
            scaleAnimator
        }

        animatorSet.playTogether(sunScaleAnimator)
        animatorSet.playTogether(planetAnimators)
        animatorSet.startDelay = 50
        animatorSet.start()
    }

    private fun animateDisappearance() {
        val animatorSet = AnimatorSet()
        
        val sunScaleAnimator = ValueAnimator.ofFloat(1f, 0f).apply {
            duration = 200
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                sun.currentScale = animation.animatedValue as Float
                invalidate()
            }
        }

        val planetAnimators = planets.map { planet ->
            ValueAnimator.ofFloat(1f, 0f).apply {
                duration = 200
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { animation ->
                    planet.currentScale = animation.animatedValue as Float
                    val progress = animation.animatedValue as Float
                    planet.x = centerX + (planet.targetX - centerX) * progress
                    planet.y = centerY + (planet.targetY - centerY) * progress
                    invalidate()
                }
            }
        }

        animatorSet.playTogether(sunScaleAnimator)
        animatorSet.playTogether(planetAnimators)
        animatorSet.start()
    }

    private fun animatePlanetSelection(planet: Planet) {
        selectedPlanet = planet
        planet.isSelected = true
        
        val scaleAnimator = ValueAnimator.ofFloat(1f, 1.3f).apply {
            duration = 150
            interpolator = OvershootInterpolator()
            addUpdateListener { animation ->
                planet.targetScale = animation.animatedValue as Float
                invalidate()
            }
        }

        val reverseAnimator = ValueAnimator.ofFloat(1.3f, 1f).apply {
            duration = 150
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                planet.targetScale = animation.animatedValue as Float
                if (animation.animatedValue as Float == 1f) {
                    planet.isSelected = false
                    selectedPlanet = null
                }
                invalidate()
            }
        }

        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(scaleAnimator, reverseAnimator)
        animatorSet.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (!isShowing) return

        // Draw orbits
        planets.forEach { planet ->
            val orbitRadius = 250f
            canvas.drawCircle(centerX, centerY, orbitRadius, orbitPaint)
        }

        // Draw sun
        if (sun.currentScale > 0) {
            val sunRadius = sun.radius * sun.currentScale
            sunPaint.color = primaryColor
            canvas.drawCircle(sun.x, sun.y, sunRadius, sunPaint)

            // Sun glow effect
            val glowPaint = Paint().apply {
                color = Color.argb(40, Color.red(primaryColor), Color.green(primaryColor), Color.blue(primaryColor))
                isAntiAlias = true
                style = Paint.Style.FILL
            }
            canvas.drawCircle(sun.x, sun.y, sunRadius * 1.5f, glowPaint)
        }

        // Draw planets
        planets.forEach { planet ->
            if (planet.currentScale > 0) {
                val planetRadius = planet.radius * planet.currentScale * planet.targetScale

                // Draw app icon if available, otherwise draw colored circle
                planet.appIcon?.let { icon ->
                    val iconSize = (planetRadius * 2).toInt()
                    icon.setBounds(
                        (planet.x - planetRadius).toInt(),
                        (planet.y - planetRadius).toInt(),
                        (planet.x + planetRadius).toInt(),
                        (planet.y + planetRadius).toInt()
                    )
                    icon.draw(canvas)
                } ?: run {
                    // Fallback to colored circle if no icon
                    planetPaint.color = primaryColor
                    canvas.drawCircle(planet.x, planet.y, planetRadius, planetPaint)
                }

                // Draw selection ring if selected
                if (planet.isSelected) {
                    selectedPlanetPaint.color = primaryColor
                    canvas.drawCircle(planet.x, planet.y, planetRadius + 12f, selectedPlanetPaint)
                }

                // Draw app label
                textPaint.color = primaryTextColor
                canvas.drawText(planet.appModel.appLabel, planet.x, planet.y + planetRadius + 30f, textPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // If planets aren't showing, don't consume touch events
        if (!isShowing) {
            return false
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = event.x
                touchStartY = event.y
                isLongPress = false
                hasMovedOutsideNeutralZone = false

                // Start long press detection
                longPressAnimation = ValueAnimator.ofFloat(0f, 1f).apply {
                    duration = 500
                    addUpdateListener { animation ->
                        if (animation.animatedValue as Float >= 1f && !isLongPress) {
                            isLongPress = true
                            performHapticFeedback()
                            showPlanets(touchStartX, touchStartY)
                        }
                    }
                }
                longPressAnimation?.start()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isLongPress) {
                    // Calculate distance from touch start (center)
                    val distanceFromCenter = sqrt((event.x - touchStartX).pow(2) + (event.y - touchStartY).pow(2))
                    val neutralZoneRadius = 80f // Neutral zone radius around touch start point

                    // Check if user moved significantly (potential swipe gesture)
                    val totalMovement = sqrt((event.x - touchStartX).pow(2) + (event.y - touchStartY).pow(2))
                    if (totalMovement > 150f) {
                        // User is trying to swipe, cancel planet interaction
                        longPressAnimation?.cancel()
                        isLongPress = false
                        selectedPlanet = null
                        hasMovedOutsideNeutralZone = false
                        hidePlanets()
                        return false // Let parent handle swipe
                    }

                    // Check if user moved outside neutral zone
                    if (distanceFromCenter > neutralZoneRadius) {
                        if (!hasMovedOutsideNeutralZone) {
                            hasMovedOutsideNeutralZone = true
                        }

                        val nearestPlanet = findNearestPlanet(event.x, event.y)
                        if (nearestPlanet != null && nearestPlanet != selectedPlanet) {
                            selectedPlanet?.let {
                                it.isSelected = false
                            }
                            selectedPlanet = nearestPlanet
                            nearestPlanet.isSelected = true
                            performHapticFeedback()
                            invalidate()
                        }
                    } else {
                        // User is back in neutral zone, deselect any selected planet
                        if (hasMovedOutsideNeutralZone) {
                            selectedPlanet?.let {
                                it.isSelected = false
                            }
                            selectedPlanet = null
                            invalidate()
                        }
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                longPressAnimation?.cancel()

                if (isLongPress) {
                    // Only launch app if user moved outside neutral zone and has a selected planet
                    if (hasMovedOutsideNeutralZone && selectedPlanet != null) {
                        selectedPlanet?.let { planet ->
                            animatePlanetSelection(planet)
                            onAppClickListener?.invoke(planet.appModel)
                        }
                    }
                    hidePlanets()
                }

                isLongPress = false
                selectedPlanet = null
                hasMovedOutsideNeutralZone = false
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                longPressAnimation?.cancel()
                isLongPress = false
                selectedPlanet = null
                hasMovedOutsideNeutralZone = false
                hidePlanets()
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    private fun findTappedPlanet(x: Float, y: Float): Planet? {
        if (!isShowing) return null
        
        planets.forEach { planet ->
            val distance = sqrt((x - planet.x).pow(2) + (y - planet.y).pow(2))
            if (distance <= planet.radius * planet.currentScale * planet.targetScale) {
                return planet
            }
        }
        return null
    }

    private fun findNearestPlanet(x: Float, y: Float): Planet? {
        if (!isShowing) return null
        
        var nearestPlanet: Planet? = null
        var minDistance = Float.MAX_VALUE
        
        planets.forEach { planet ->
            val distance = sqrt((x - planet.x).pow(2) + (y - planet.y).pow(2))
            if (distance < minDistance) {
                minDistance = distance
                nearestPlanet = planet
            }
        }
        
        return nearestPlanet
    }

    private fun performHapticFeedback() {
        vibrator?.let { vibrator ->
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        }
    }
}