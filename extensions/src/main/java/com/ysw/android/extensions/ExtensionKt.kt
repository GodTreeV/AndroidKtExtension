package com.ysw.android.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.res.Configuration
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.KeyEvent.FLAG_CANCELED
import android.view.inputmethod.InputMethodManager
import android.view.inspector.WindowInspector
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.BoolRes
import androidx.annotation.DimenRes
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.ActionBarContextView
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.Insets
import androidx.core.graphics.scale
import androidx.core.view.*
import androidx.lifecycle.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.internal.ViewUtils
import kotlinx.coroutines.*
import java.io.*
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.math.roundToInt

/**
 * # [typealias]
 */
typealias Block = () -> Unit

/**
 * # [Logger] extension functions
 */
/**
 * Inline function for the standard [Log.DEBUG] message log.
 *
 * @param tag Used to identify the source of the log message
 * @param desc The message to be logged
 */
inline fun logD(tag: String = Logger.getTag(), desc: () -> Any?) {
    //if (Logger.IS_USER_DEBUG) {
    Log.d(tag, desc()?.toString() ?: "null")
    //}
}

/**
 * Inline function for the standard [Log.VERBOSE] message log.
 *
 * @param tag Used to identify the source of the log message
 * @param desc The message to be logged
 */
inline fun logV(tag: String = Logger.getTag(), desc: () -> Any?) {
    //if (Logger.IS_USER_DEBUG) {
    Log.v(tag, desc()?.toString() ?: "null")
    //}
}

/**
 * Inline function for the standard [Log.ERROR] message log.
 *
 * @param tag Used to identify the source of the log message
 * @param desc The message to be logged
 */
inline fun logE(tag: String = Logger.getTag(), throwable: Throwable? = null, desc: () -> Any? = { null }) {
    Log.e(tag, desc()?.toString(), throwable)
}

/**
 * Inline function for the standard [Log.WARN] message log.
 *
 * @param tag Used to identify the source of the log message
 * @param desc The message to be logged
 */
inline fun logW(tag: String = Logger.getTag(), desc: () -> Any?) {
    Log.w(tag, desc()?.toString() ?: "null")
}

/**
 * Inline function for the standard [Log.INFO] message log.
 *
 * @param tag Used to identify the source of the log message
 * @param desc The message to be logged
 */
@SuppressWarnings("LogConditional")
inline fun logI(tag: String = Logger.getTag(), desc: () -> Any?) {
    Log.i(tag, desc()?.toString() ?: "null")
}

object Logger {
    /**
     * Tag Prefix to be used across the application.
     */
    private const val TAG_PREFIX = "MN."

    /**
     * Default tag.
     */
    private const val DEFAULT_TAG = "${TAG_PREFIX}ysw"

    /**
     * Within a stacktrace, our class appears in this position.
     */
    private const val CLASS_STACK_INDEX = 3

    /**
     * Regex to match anonymous class.
     */
    private val ANONYMOUS_CLASS_PATTERN = Pattern.compile("(\\$\\d+)+$")

    /**
     * Returns a TAG for the caller class. Tag format is : MM.classname
     *
     * @return The Tag to be used for logging.
     */
    fun getTag(): String = DEFAULT_TAG
}

/**
 * # [Any] extension functions
 */
inline fun <T, R> T.mapTo(block: (T) -> R): R = block(this)

inline val eventTimeOffset: Long
    get() = System.currentTimeMillis() - SystemClock.uptimeMillis()

inline val Any?.classHash: String
    get() = if (this == null) "null" else "${javaClass.simpleName}@${
        hashCode().toString(16)
    }, @${hashCode()}"

inline fun <reified T> Any.cast(): T {
    return this as T
}

inline fun <reified T> Any.castSafely(): T? {
    return this as? T
}

inline fun <reified T> Any?.castNullable(): T? {
    return this as T?
}

inline val currentTimeMillis: Long
    get() = System.currentTimeMillis()

fun defaultLaunch(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job {
    return CoroutineScope(Dispatchers.Default).launch(start = start, block = block)
}

fun ioLaunch(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job {
    return CoroutineScope(Dispatchers.IO).launch(start = start, block = block)
}

fun mainLaunch(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job {
    return MainScope().launch(start = start, block = block)
}

suspend fun <T> withMainContext(
    block: suspend CoroutineScope.() -> T
): T {
    return withContext(Dispatchers.Main, block)
}

suspend fun <T> withDefaultContext(
    block: suspend CoroutineScope.() -> T
): T {
    return withContext(Dispatchers.Default, block)
}

suspend fun <T> withIoContext(
    block: suspend CoroutineScope.() -> T
): T {
    return withContext(Dispatchers.IO, block)
}

inline fun <T> trace(tag: String, msg: String, debug: Boolean = true, block: () -> T): T {
    val start = currentTimeMillis
    if (debug) logD(tag) { msg }
    return block().also {
        if (debug) logD(tag) { "$msg - end, ${currentTimeMillis - start}" }
    }
}

/**
 * # [Activity] extension functions
 */
fun Activity.safeFinish() {
    if (!isDestroyed && !isFinishing) finish()
}

fun Activity.setDecorNotFitSystemWindowsInMultiWindowMode() {
    if (isInMultiWindowMode) {
        window.unableDecorFitsSystemWindows()
    }
}

/**
 * # [Window] extension functions
 */
inline fun Window.updateAttributes(block: WindowManager.LayoutParams.() -> Unit) {
    val attrs = attributes
    block(attrs)
    attributes = attrs
}

fun Window.setLayoutNoLimitFlags() {
    addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    // No more outdated flag View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    // use Window.setDecorFitsSystemWindows(boolean) instead
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        setDecorFitsSystemWindows(false)
    } else {
        decorView.systemUiVisibility = decorView.systemUiVisibility or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }
}

/**
 * When we make window no layout limit,if we start app from recent in split screen mode(horizontal screen),
 * The app will be obscured by the navigation bar, so we need to make DecorView not to fit the system window
 * inserts.
 *
 * @receiver Window
 */
fun Window.unableDecorFitsSystemWindows() {
    if (atLeastR) {
        setDecorFitsSystemWindows(true)
    } else {
        runCatching {
            decorView.systemUiVisibility.apply {
                minus(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
                minus(View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            }
        }
    }
}

/**
 * # [Context] extension functions
 */
inline val Context.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(this)

inline val Context.localBroadcastManager: LocalBroadcastManager
    get() = LocalBroadcastManager.getInstance(this)

fun Context.inflate(
    @LayoutRes resource: Int, root: ViewGroup? = null,
    attachToRoot: Boolean = false
): View = layoutInflater.inflate(resource, root, attachToRoot)

fun Context.getBoolean(@BoolRes id: Int): Boolean = resources.getBoolean(id)

fun Context.getDimensionPixelSize(@DimenRes id: Int): Int = resources.getDimensionPixelSize(id)

fun Context.getDimensionPixelOffset(@DimenRes id: Int): Int = resources.getDimensionPixelOffset(id)

fun Context.getDimension(@DimenRes id: Int): Float = resources.getDimension(id)

inline val Context.isRtl: Boolean
    get() = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

inline val Context.isDarkTheme: Boolean
    get() = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

inline val Context.isLightTheme: Boolean
    get() = !isDarkTheme

fun Context.toast(
    resId: Int,
    isLongToast: Boolean = false,
    block: (Toast.() -> Unit)? = null
): Toast =
    Toast.makeText(this, resId, if (isLongToast) Toast.LENGTH_LONG else Toast.LENGTH_SHORT)
        .apply {
            block?.invoke(this)
            show()
        }

fun Context.toast(
    text: CharSequence,
    isLongToast: Boolean = false,
    block: (Toast.() -> Unit)? = null
): Toast =
    Toast.makeText(this, text, if (isLongToast) Toast.LENGTH_LONG else Toast.LENGTH_SHORT)
        .apply {
            block?.invoke(this)
            show()
        }

fun Context.openInputStream(uri: Uri) = contentResolver.openInputStream(uri)
fun Context.openOutputStream(uri: Uri) = contentResolver.openOutputStream(uri)

fun Context.copyToClipboard(label: String, data: String) {
    getSystemService<ClipboardManager>()?.setPrimaryClip(ClipData.newPlainText(label, data))
}

inline val Context.screenSize: Rect
    get() = windowManager.currentWindowMetrics.bounds

inline val Context.windowManager: WindowManager
    get() = getSystemService()!!

inline val Context.inputMethodManager: InputMethodManager
    get() = getSystemService()!!

inline val Context.isScreenOn: Boolean
    get() = getSystemService<PowerManager>()!!.isInteractive


/**
 * # [dp] extension functions
 */
fun Int.dp(context: Context): Int = toFloat().dp(context)

fun Float.dp(context: Context): Int = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this, context.resources.displayMetrics).roundToInt()

/**
 * # [TextView] extension functions
 */
fun TextView.textChangedListener(init: DslTextWatcher.() -> Unit) {
    addTextChangedListener(DslTextWatcher().apply(init))
}

class DslTextWatcher : TextWatcher {

    private var beforeTextChangedL: ((s: CharSequence, start: Int, count: Int, after: Int) -> Unit)? = null
    private var onTextChangedL: ((s: CharSequence, start: Int, before: Int, count: Int) -> Unit)? = null
    private var afterTextChangedL: ((s: CharSequence) -> Unit)? = null

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        beforeTextChangedL?.invoke(s, start, count, after)
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        onTextChangedL?.invoke(s, start, before, count)
    }

    override fun afterTextChanged(s: Editable) {
        afterTextChangedL?.invoke(s)
    }

    fun beforeTextChanged(listener: (s: CharSequence, start: Int, count: Int, after: Int) -> Unit) {
        beforeTextChangedL = listener
    }

    fun onTextChanged(listener: (s: CharSequence, start: Int, before: Int, count: Int) -> Unit) {
        onTextChangedL = listener
    }

    fun afterTextChanged(listener: (s: CharSequence) -> Unit) {
        afterTextChangedL = listener
    }
}

/**
 * # [Build] extension functions
 */
inline val beforeT: Boolean get() = !atLeastT
inline val atLeastT: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
inline val beforeS: Boolean get() = !atLeastS
inline val atLeastS: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
inline val beforeR: Boolean get() = !atLeastR
inline val atLeastR: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R


/**
 * # [View] extension functions
 */
fun View.requestCancelTouchEvent() {
    with(
        MotionEvent.obtain(
            SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
        MotionEvent.ACTION_DOWN, -500f, -500f, 0)) {
        parent.cast<ViewGroup>().dispatchTouchEvent(this)
        MotionEvent.obtain(this).apply {
            action = MotionEvent.ACTION_CANCEL
            parent.cast<ViewGroup>().dispatchTouchEvent(this)
            recycle()
        }
        recycle()
    }
}

fun View.performLongPressFeedback() {
    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, 0)
}

fun View.performDragCrossingFeedback() {
    performHapticFeedback(/*HapticFeedbackConstants.DRAG_CROSSING*/11, 0)
}

fun View.performGestureStartFeedback() {
    performHapticFeedback(
        if (atLeastR)
            HapticFeedbackConstants.GESTURE_START
        else HapticFeedbackConstants.TEXT_HANDLE_MOVE,
        0)
}

fun View.performGestureEndFeedback() {
    performHapticFeedback(
        if (atLeastR)
            HapticFeedbackConstants.GESTURE_END
        else HapticFeedbackConstants.TEXT_HANDLE_MOVE,
        0)
}

fun View.addRipple() = with(TypedValue()) {
    context.theme.resolveAttribute(android.R.attr.selectableItemBackground, this, true)
    setBackgroundResource(resourceId)
}

fun View.addCircleRipple() = with(TypedValue()) {
    context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, this, true)
    setBackgroundResource(resourceId)
}

fun View.showIme() = context.getSystemService<InputMethodManager>()!!.showSoftInput(this, 0)

fun View.hideIme() = context.getSystemService<InputMethodManager>()!!.hideSoftInputFromWindow(windowToken, 0)

/**
 * View.isAttachedToWindow may return false if view is added but not invalidated
 */
inline val View.isRealAttachedToWindow: Boolean
    get() = WindowInspector.getGlobalWindowViews().contains(this)

fun View.setSelectedWithParent(selected: Boolean) {
    parent?.takeIf { it is View }?.cast<View>()?.apply {
        isSelected = selected
    }
    isSelected = selected
}

fun View.removeFromParent() {
    (parent as? ViewGroup)?.removeView(this)
}

fun View.dispatchEnableState(enabled: Boolean) {
    selfAndDescendants {
        this.isEnabled = enabled
    }
}

fun View.dispatchSelectState(selected: Boolean) {
    selfAndDescendants {
        this.isSelected = selected
    }
}

inline fun View.selfAndDescendants(block: View.() -> Unit) {
    block(this)
    (this as? ViewGroup)?.descendants?.forEach { block(it) }
}

/**
 * Represents the view padding.
 *
 * @param left the left padding
 * @param top the top padding
 * @param right the right padding
 * @param bottom the bottom padding
 * @param start the start padding
 * @param end the end padding
 */
data class ViewPaddingState(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val start: Int,
    val end: Int
)

/**
 * Add Padding for the specified View, and a height Padding for the status bar if top is true.
 * In addition, the View is specifically divided into [ActionBarContextView] and other views.
 * When using [AppBarLayout] and [ActionMode], you need to set the padding for [AppBarLayout].
 * We also need to add Padding for the view that is displayed after the response to the [ActionMode],
 * so the view that the V displays after the response is the [ActionBarContextView]
 *
 * @receiver View
 * @param left Boolean
 * @param right Boolean
 * @param top Boolean
 * @param bottom Boolean
 */
@SuppressLint("WrongConstant")
fun View.applyViewPaddingInset(
    left: Boolean = false,
    right: Boolean = false,
    top: Boolean = false,
    bottom: Boolean = false,
) {
    doOnApplyWindowInsets { _, windowInsetsCompat, _ ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val insets = windowInsetsCompat.getInsets(WindowInsetsCompat.Type.systemBars())
            val statusBarHeight = windowInsetsCompat.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val navBarHeight = windowInsetsCompat.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            // Consider using ActionMode,when ActionBarCallBack.onPrepareActionMode is executed,
            // there will inflate a new view(ActionBarContextView) attached to root view,
            // so this new attached view also needs a padding
            when (this) {
                is ActionBarContextView -> {
                    updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        topMargin = insets.top.coerceAtLeast(statusBarHeight)
                    }
                }
                else -> updatePadding(
                    if (left) insets.left else paddingLeft,
                    if (top) insets.top.coerceAtLeast(statusBarHeight) else paddingTop,
                    if (right) insets.right else paddingRight,
                    if (bottom) insets.bottom.coerceAtLeast(navBarHeight) else paddingBottom
                )
            }
        }
    }
}

/**
 * Call func with [WindowInsets] when view is attached to window and/or when they are changed by
 * SystemUI.
 *
 * @param function function called when insets are ready, this function receives as parameter the
 * view and the new [WindowInsetsCompat] values
 */
@SuppressLint("RestrictedApi")
fun View.doOnApplyWindowInsets(
    function: (View, WindowInsetsCompat, ViewPaddingState) -> Unit
) {
    // Create a snapshot of the view's padding state
    val paddingState = createStateForView()
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        function(v, insets, paddingState)
        insets
    }
    ViewUtils.requestApplyInsetsWhenAttached(this)
}

private fun View.createStateForView() =
    ViewPaddingState(
        paddingLeft,
        paddingTop,
        paddingRight,
        paddingBottom,
        paddingStart,
        paddingEnd
    )


inline fun View.setImeVisibilityListener(crossinline callback: (visible: Boolean, height: Int) -> Unit) {
    ViewCompat.setOnApplyWindowInsetsListener(this, object : OnApplyWindowInsetsListener {
        var lastVisible: Boolean? = null
        override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            if (lastVisible != imeVisible) {
                val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                lastVisible = imeVisible
                callback(imeVisible, imeHeight)
            }
            return insets
        }
    })

    this.doOnDetach {
        ViewCompat.setOnApplyWindowInsetsListener(this, null)
    }
}

/**
 * # [Point] & [PointF] extension functions
 */
fun Point.set(p: Point) {
    set(p.x, p.y)
}

fun Point.swap() {
    x.let {
        this.x = y
        this.y = it
    }
}

fun Point.reset() {
    x = 0
    y = 0
}

fun PointF.reset() {
    x = 0f
    y = 0f
}

/**
 * # [File] extension functions
 */
inline val File.isImage: Boolean
    get() = extension.isImageExtension

inline val File.mimeTypeOfExtension: String
    get() = extension.mimeTypeOfExtension

inline val String.isImageExtension: Boolean
    get() = equals("jpg", true) || equals("jpeg", true)
            || equals("png", true)

inline val String.mimeTypeOfExtension: String
    get() = MimeTypeMap.getSingleton().getMimeTypeFromExtension(this) ?: "*/*"

fun File.toFileProviderUri(context: Context): Uri {
    return FileProvider.getUriForFile(
        context, context.packageName + ".provider", this
    )
}

fun File.smartCreate(isDir: Boolean = false): File {
    if (!exists()) {
        parentFile?.mkdirs()
        if (isDir) mkdir() else createNewFile()
    }
    return this
}

fun File.decodeImageSize(withRotation: Boolean = true): Point {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeFile(this.absolutePath, options)
    return Point(options.outWidth, options.outHeight).apply {
        if (withRotation) {
            decodeImageRotation().takeIf { it % 180 != 0 }?.let {
                this.swap()
            }
        }
    }
}

fun File.decodeImageRotation(): Int {
    return try {
        val orientation = ExifInterface(this).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    } catch (e: IOException) {
        0
    }
}

fun File.unZipTo(dir: String) {
    ZipInputStream(inputStream()).use { it.unzipTo(dir) }
}

fun ZipInputStream.unzipTo(dir: String) {
    while (true) {
        val entry = nextEntry ?: break
        if (entry.isDirectory) File(dir, entry.name).smartCreate(true)
        else File(dir, entry.name).smartCreate().outputStream().use {
            copyTo(it)
        }
    }
}

fun File.zipTo(dest: File) {
    ZipOutputStream(dest.smartCreate().outputStream()).use { out ->
        if (isFile) zipTo(out, "")
        else listFiles()?.forEach { it.zipTo(out, "") }
    }
}

private fun File.zipTo(out: ZipOutputStream, curPath: String) {
    if (isDirectory) listFiles()?.forEach {
        it.zipTo(out, curPath + name + File.separator)
    }
    else inputStream().use {
        out.putNextEntry(ZipEntry(curPath + name))
        it.copyTo(out)
        out.closeEntry()
    }
}

object FileKt {
    @JvmStatic
    fun deleteRecursively(file: File) {
        file.deleteRecursively()
    }

    @JvmStatic
    fun nameWithoutExtension(file: File) = file.nameWithoutExtension
}

/**
 * # [Bitmap] extension functions
 */
fun Bitmap.rotate(rotationDegree: Int): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(rotationDegree.toFloat())
    return Bitmap.createBitmap(
        this, 0, 0, this.width,
        this.height, matrix, true
    )
}

fun Bitmap.saveToFile(file: File) {
    saveToFile(file.parent, file.name, file.name.lowercase().contains("png"))
}

fun Bitmap.saveToFile(path: String, name: String, isPng: Boolean = false): File? {
    return runCatching {
        File(path).apply {
            mkdirs()
        }
        File(path, name).smartCreate().takeIf { it.exists() }?.let { file ->
            FileOutputStream(file).let {
                compress(
                    if (isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG,
                    100, it
                )
                it.flush()
                it.close()
                logD { "file save success: $path" }
            }
            file
        }
    }.onFailure {
        logE { "file save failed..." }
        it.printStackTrace()
    }.getOrNull()
}

fun Bitmap.toBase64String(): String? {
    return runCatching {
        ByteArrayOutputStream().let {
            compress(Bitmap.CompressFormat.PNG, 100, it)
            Base64.encodeToString(it.toByteArray(), Base64.DEFAULT)
        }
    }.getOrNull()
}

fun calculateSizeWithRatio(
    targetRatio: Float, maxWidth: Int,
    maxHeight: Int,
    out: IntArray
) {
    out[0] = maxWidth
    out[1] = maxHeight
    val ratio = maxWidth * 1f / maxHeight
    if (ratio > targetRatio) {
        out[0] = (maxHeight * targetRatio).toInt()
    } else if (ratio < targetRatio) {
        out[1] = (maxWidth / targetRatio).toInt()
    }
}

/**
 * # [String] extension functions
 */
fun String.openInputStreamFromPath(context: Context): InputStream? {
    return runCatching {
        if (isAssetPath)
            context.assets.open(assetPathWithoutPrefix)
        else runCatching {
            context.contentResolver.openInputStream(Uri.parse(this))
        }.getOrElse { File(this).inputStream() }
    }.onFailure {
        logE { "openInputStreamFromPath error" }
        it.printStackTrace()
    }.getOrNull()
}

private fun decodeBitmap(
    maxWidth: Int, maxHeight: Int,
    isSupplier: () -> InputStream?
): Bitmap? {
    return try {
        if (maxWidth <= 0 || maxHeight <= 0) {
            return BitmapFactory.decodeStream(isSupplier())
        }
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(isSupplier(), null, options)
        if (options.outWidth <= maxWidth && options.outHeight <= maxHeight) {
            return BitmapFactory.decodeStream(isSupplier())
        }
        val wh = IntArray(2)
        val aspectRatio = options.outWidth * 1f / options.outHeight
        calculateSizeWithRatio(aspectRatio, maxWidth, maxHeight, wh)
        options.inJustDecodeBounds = false
        options.inSampleSize = options.outWidth / wh[0]
        var bitmap = BitmapFactory.decodeStream(isSupplier(), null, options)
        if (bitmap != null && (bitmap.width > maxWidth || bitmap.height > maxHeight)) {
            bitmap = bitmap.scale(maxWidth, maxHeight)
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun getRotationDegree(inputStream: InputStream): Int {
    var degree = 0
    try {
        val exif = ExifInterface(inputStream)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        degree =
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return degree
}

fun String.toBitmap(): Bitmap? {
    return runCatching {
        Base64.decode(this, Base64.DEFAULT).let {
            BitmapFactory.decodeByteArray(
                it,
                0,
                it.size
            )
        }
    }.getOrNull()
}

inline val String.assetFilePath: String
    get() = "file:///android_asset/$this"

inline val String.assetFilePathWithoutPrefix: String
    get() = replaceFirst("file:///android_asset/", "")

inline val String.toAssetPath: String
    get() = "file:///android_asset/$this"

inline val String.isAssetPath: Boolean
    get() = isNotBlank() && startsWith("file:///android_asset/", true)

inline val String.assetPathWithoutPrefix: String
    get() = replaceFirst("file:///android_asset/".toRegex(), "")

class StringKt {
    companion object {
        @JvmStatic
        fun isAssetPath(fileName: String) = fileName.isAssetPath

        @JvmStatic
        fun toAssetPath(fileName: String) = fileName.toAssetPath

        @JvmStatic
        fun assetFilePath(fileName: String) = fileName.assetFilePath

        @JvmStatic
        fun assetFilePathWithoutPrefix(fileName: String) = fileName.assetFilePathWithoutPrefix
    }
}

/**
 * # [Color] extension functions
 */
inline val Int.colorWithoutAlpha: Int
    get() = ColorUtils.setAlphaComponent(this, 255)

fun Int.toColorString(): String = String.format("#%06X", (0xFFFFFF and this))

/**
 * # [BroadcastReceiver] extension functions
 */
typealias BroadcastBlock = BroadcastReceiver.(Context, Intent) -> Unit

inline fun broadcastReceiver(
    crossinline block: BroadcastBlock
): BroadcastReceiver {
    return object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            block(this, context, intent)
        }
    }
}


/**
 * # [MotionEvent] extension functions
 */
inline val MotionEvent.actionAsString: String
    get() = MotionEvent.actionToString(action)

inline val MotionEvent.hasCancelFlag: Boolean
    // new added since
    // This flag associated with ACTION_POINTER_UP,
    // this indicates that the pointer has been canceled.
    // Typically this is used for palm event when the user has accidental touches.
    get() = if (atLeastT) flags.and(MotionEvent.FLAG_CANCELED) != 0 else false

inline val MotionEvent.pointerId: Int
    get() = getPointerId(actionIndex)

inline val MotionEvent.isActionDownOrPointerDown: Boolean
    get() = isActionDown || isPointerDown

inline val MotionEvent.isActionDown: Boolean
    get() = actionMasked == MotionEvent.ACTION_DOWN

inline val MotionEvent.isPointerDown: Boolean
    get() = actionMasked == MotionEvent.ACTION_POINTER_DOWN

inline val MotionEvent.isActionMove: Boolean
    get() = actionMasked == MotionEvent.ACTION_MOVE

inline val MotionEvent.isPointerUp: Boolean
    get() = actionMasked == MotionEvent.ACTION_POINTER_UP

inline val MotionEvent.isActionFinished: Boolean
    get() = isActionUp || isActionCancel

inline val MotionEvent.isActionUpOrPointerUp: Boolean
    get() = isActionUp || isPointerUp

inline val MotionEvent.isActionUp: Boolean
    get() = actionMasked == MotionEvent.ACTION_UP

inline val MotionEvent.isActionCancel: Boolean
    get() = actionMasked == MotionEvent.ACTION_CANCEL


/**
 * # [WindowInsetsCompat] extension functions
 */
inline val WindowInsetsCompat.statusBars: Insets
    get() = getInsets(WindowInsetsCompat.Type.statusBars())

inline val WindowInsetsCompat.systemBars: Insets
    get() = getInsets(WindowInsetsCompat.Type.systemBars())

inline val WindowInsetsCompat.navigationBars: Insets
    get() = getInsets(WindowInsetsCompat.Type.navigationBars())

inline val WindowInsetsCompat.navigationBarsAndIme: Insets
    get() = getInsets(WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.ime())

inline val WindowInsetsCompat.ime: Insets
    get() = getInsets(WindowInsetsCompat.Type.ime())

inline val WindowInsetsCompat.systemGestures: Insets
    get() = getInsets(WindowInsetsCompat.Type.systemGestures())

inline val WindowInsetsCompat.captionBar: Insets
    get() = getInsets(WindowInsetsCompat.Type.captionBar())

inline val WindowInsetsCompat.mandatorySystemGestures: Insets
    get() = getInsets(WindowInsetsCompat.Type.mandatorySystemGestures())

inline val WindowInsetsCompat.displayCutout: Insets
    get() = getInsets(WindowInsetsCompat.Type.displayCutout())

inline val WindowInsetsCompat.tappableElement: Insets
    get() = getInsets(WindowInsetsCompat.Type.tappableElement())