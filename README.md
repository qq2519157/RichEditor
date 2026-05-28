# RichEditor
[![Apache License 2.0][1]][2]
[![Release Version][5]][6]
[![API][3]][4]
[![PRs Welcome][7]][8]
***
 * ### 介绍
    富文本编辑器,可添加视频和图片,带图片和视频拖动功能
    ![](https://github.com/qq2519157/RichEditor/blob/master/app/src/main/assets/fly.gif)
***
 * ### 依赖方式
   #### 需要添加mavenCentral 和 jitpack
      在setting.gradle文件中
     ```
      pluginManagement {
      repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
         }
      }
      dependencyResolutionManagement {
         repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
         repositories {
            google()
            mavenCentral()
            maven { url 'https://jitpack.io' }
            }
      }
     ```

   #### Gradle:
     在你的module的build.gradle文件
     ```
        implementation 'com.log1992:RichEditor:3.0.0'
     ```
   ### Maven:
     ```
       <dependency>
         <groupId>com.log1992</groupId>
         <artifactId>RichEditor</artifactId>
         <version>3.0.0</version>
         <type>aar</type>
      </dependency>
     ```
   ### Lvy
     ```
       <dependency org="com.log1992" name="RichEditor" rev="3.0.0" />
     ```

     ***
 * ### 引入的库：
    ```
         implementation 'androidx.appcompat:appcompat:1.7.0'
         implementation 'cn.jzvd:jiaozivideoplayer:7.7.2.3300'
         api 'com.github.bumptech.glide:glide:4.16.0'
         annotationProcessor 'com.github.bumptech.glide:compiler:4.16.0'
         implementation 'com.blankj:utilcodex:1.31.1'
    ```
    ***
 * ### 使用方法
    直接使用XML文件引入即可
    ```
    <com.pppcar.richeditorlibary.view.RichEditor
            android:id="@+id/rich_et"
            android:layout_above="@id/tool_container"
            android:layout_width="match_parent"
            android:layout_centerInParent="true"
            android:layout_height="match_parent"/>
    ```
    ***
 * ### v3.0.0 更新内容
   1. AGP 7.1.2 → 8.7.3，Gradle → 8.9，compileSdk/targetSdk → 35
   2. Java 11 → 17
   3. 权限处理：permissions-dispatcher 替换为 XXPermissions
   4. 适配 Android 13+ 细粒度媒体权限 (READ_MEDIA_IMAGES/READ_MEDIA_VIDEO)
   5. Glide 4.12.0 → 4.16.0
   6. PictureSelector v3.0.4 → v3.11.2
   7. JiaoZiVideoPlayer 7.7.0 → 7.7.2.3300
   8. RxJava 3.1.3 → 3.1.10，RxAndroid 3.0.0 → 3.0.2
   9. 新增 AndroidUtilCode 工具库
   10. 修复视频拖拽功能，统一图片/视频拖拽逻辑
   11. 使用 ActivityResultContracts 替代已废弃的 onActivityResult
   12. 使用 AlertDialog 替代已废弃的 ProgressDialog
   13. ScreenUtils 适配 Android R+ WindowMetrics API
   14. 修复 Glide SimpleTarget 废弃 API
   ***
 * ### 注意
   1. 权限处理：推荐使用 XXPermissions

   2. targetSdk 35 已适配分区存储

    ***
 * ### 感谢
    [Jzvd](https://github.com/Jzvd)的[JZVideo](https://github.com/Jzvd/JZVideo)
    [XXPermissions](https://github.com/getActivity/XXPermissions)
    [AndroidUtilCode](https://github.com/Blankj/AndroidUtilCode)
    ***

[1]:https://img.shields.io/:license-apache-blue.svg
[2]:https://www.apache.org/licenses/LICENSE-2.0.html
[3]:https://img.shields.io/badge/API-24%2B-red.svg?style=flat
[4]:https://android-arsenal.com/api?level=24
[5]:https://img.shields.io/badge/release-3.0.0-red.svg
[6]:https://github.com/qq2519157/RichEditor/releases
[7]:https://img.shields.io/badge/PRs-welcome-brightgreen.svg
[8]:https://github.com/qq2519157/RichEditor/pulls
