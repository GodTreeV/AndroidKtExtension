package com.ysw.android.extensions.viewbinding

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.AnyThread
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.decodeBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.ysw.android.extensions.atLeastT
import com.ysw.android.extensions.currentTimeMillis
import com.ysw.android.extensions.logE
import java.io.File

open class ActivityLaunchHelper(
    private val context: Context,
    private val activityResultCaller: ActivityResultCaller,
    private val lifecycleOwner: LifecycleOwner
) : LifecycleEventObserver {

    companion object {
        private const val TAG = "ActivityLaunchHelper"

        @JvmStatic
        fun isPhotoPickerAvailable(context: Context): Boolean {
            return ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)
        }
    }

    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
        private set

    private var activityResultCb: ((ActivityResult) -> Unit)? = null

    lateinit var mediaPickLauncher: ActivityResultLauncher<PickVisualMediaRequest>
        private set

    lateinit var multipleMediaPickLauncher: ActivityResultLauncher<PickVisualMediaRequest>

    private var mediaPickResultCb: ((Uri?) -> Unit)? = null
    private var multipleMediaPickResultCb: ((List<Uri>?) -> Unit)? = null

    lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
        private set

    private var takePictureResultCb: ((File) -> Unit)? = null

    lateinit var takePitureForPriviewLauncher: ActivityResultLauncher<Void?>
        private set

    private var takePictureForPreviewResultCb: ((Bitmap?) -> Unit)? = null

    var maxMultipleMediaItems = if (atLeastT) MediaStore.getPickImagesMaxLimit() else Int.MAX_VALUE
        private set

    init {
        runCatching {
            lifecycleOwner.lifecycle.addObserver(this)
        }.onFailure {
            logE(TAG) { "add lifecycle observer failed..." }
            it.printStackTrace()
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                registerActivityResultLaunchers(activityResultCaller)
            }

            Lifecycle.Event.ON_DESTROY -> {
                lifecycleOwner.lifecycle.removeObserver(this)
            }

            else -> {}
        }
    }

    open fun registerActivityResultLaunchers(caller: ActivityResultCaller) {
        with(caller) {
            activityResultLauncher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    activityResultCb?.invoke(it)
                }
            mediaPickLauncher =
                registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
                    mediaPickResultCb?.invoke(it)
                }
            multipleMediaPickLauncher = registerForActivityResult(
                ActivityResultContracts.PickMultipleVisualMedia(maxMultipleMediaItems)
            ) {
                multipleMediaPickResultCb?.invoke(it)
            }
            takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) {

            }
            takePitureForPriviewLauncher =
                registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
                    takePictureForPreviewResultCb?.invoke(it)
                }
        }
    }

    open fun launchActivityForResult(
        intent: Intent,
        activityOptions: ActivityOptionsCompat? = null,
        cb: (ActivityResult) -> Unit
    ) {
        activityResultCb = cb
        activityResultLauncher.launch(intent, activityOptions)
    }

    open fun takePicture(
        file: File = File("${context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath}/${TAG}_take_pic_${currentTimeMillis}.png"),
        cb: ((File) -> Unit)? = null
    ) {
        takePictureResultCb = cb
        val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
        takePictureLauncher.launch(
            uri
        )
    }

    open fun takePictureForPreview(
        cb: ((Bitmap?) -> Unit)? = null
    ) {
        takePictureForPreviewResultCb = cb
        takePitureForPriviewLauncher.launch(null)
    }

    open fun pickPhoto(
        cb: ((Uri?) -> Unit)? = null
    ) {
        mediaPickResultCb = cb
        mediaPickLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    open fun pickVideo(
        cb: ((Uri?) -> Unit)? = null
    ) {
        mediaPickResultCb = cb
        mediaPickLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
        )
    }

    open fun pickMultipleMedia(
        maxItems: Int = 1,
        mediaType: ActivityResultContracts.PickVisualMedia.VisualMediaType,
        cb: ((List<Uri>?) -> Unit)? = null
    ) {
        multipleMediaPickResultCb = cb
        maxMultipleMediaItems = maxItems
        multipleMediaPickLauncher.launch(
            PickVisualMediaRequest(mediaType)
        )
    }

    @AnyThread
    open fun getBimapFromUri(
        uri: Uri,
        action: ((info: ImageDecoder.ImageInfo, source: ImageDecoder.Source) -> Unit)? = null
    ): Bitmap {
        return ImageDecoder.createSource(context.contentResolver, uri)
            .decodeBitmap { info, source ->
                action?.invoke(info, source)
            }
    }
}