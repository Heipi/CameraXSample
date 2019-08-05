package com.camerax.sample

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Rational
import android.util.Size
import android.view.TextureView
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraX
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

private const val REQUEST_CODE_PERMISSION = 10
private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE)
class MainActivity : AppCompatActivity(),LifecycleOwner {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

       viewFinder = findViewById(R.id.view_finder)
       if (allPermissionsGranted()){
           viewFinder.post { startCamera() }
       }else{
           ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSION)
       }
       viewFinder.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
           updateTransform()
       }


    }
    private lateinit var viewFinder:TextureView

    private fun startCamera(){
        //为viewfinder用例创建配置对象
    val previewConfig =  PreviewConfig.Builder().apply {
         setTargetAspectRatio(Rational(16,9)) //设置宽高比
         setTargetResolution(Size(1920,1080))//设置分辨率
         setLensFacing(CameraX.LensFacing.BACK)
     }.build()
      //构建viewfinder用例
       val preview = Preview(previewConfig)
       //每次更新viewfinder时，重新计算布局
       preview.setOnPreviewOutputUpdateListener {
           //为了更新SurfaceTexture，我们必须删除它并重新添加它
        val parent =  viewFinder.parent as ViewGroup
           parent.removeView(viewFinder)
           parent.addView(viewFinder,0)

           viewFinder.surfaceTexture = it.surfaceTexture
           updateTransform()
       }
   //将用例绑定到生命周期
   //如果Android Studio提示“this”不是一个生命周期所有者
   //尝试重新构建项目或更新appcompat依赖项
   //版本1.1.0或更高。
        CameraX.bindToLifecycle(this,preview)
    }
    private fun updateTransform(){

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSION){
             if (allPermissionsGranted()){
                 viewFinder.post {
                     startCamera()
                 }
             }else{
                 Toast.makeText(this,"Permission not granted by the user",Toast.LENGTH_LONG).show()
                 finish()
             }
        }
    }
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext,it) == PackageManager.PERMISSION_GRANTED
    }

}
