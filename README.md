# RichEditor
[![Apache License 2.0][1]][2]
[![Release Version][5]][6]
[![API][3]][4]
[![PRs Welcome][7]][8]
[![996.icu](https://img.shields.io/badge/link-996.icu-red.svg)](https://996.icu)
***
 * ### 介绍
    富文本编辑器,可添加视频和图片,带图片拖动功能
    ![](https://github.com/qq2519157/RichEditor/blob/master/app/src/main/assets/fly.gif)
***
 * ### 依赖方式
   #### Gradle:
     在你的module的build.gradle文件
     ```
        implementation 'com.log1992:richeditorlibary:1.1.0'
     ```
   ### Maven:
     ```
        <dependency>
        <groupId>com.log1992</groupId>
        <artifactId>richeditorlibary</artifactId>
        <version>1.1.0</version>
        <type>pom</type>
        </dependency>
     ```
   ### Lvy
     ```
        <dependency org='com.log1992' name='richeditorlibary' rev='1.1.0'>
         <artifact name='richeditorlibary' ext='pom' ></artifact>
        </dependency>
     ```
   ###### 如果Gradle出现compile失败的情况，可以在Project的build.gradle里面添加如下仓库地址：
     ```gradle
        allprojects {
            repositories {
                maven {url 'https://dl.bintray.com/qq2519157/maven'}
            }
        }
     ```
     ***
 * ### 引入的库：
    ```
        implementation 'androidx.appcompat:appcompat:1.1.0'
        testImplementation 'junit:junit:4.12'
        implementation 'cn.jzvd:jiaozivideoplayer:7.0.5'
        implementation 'com.github.bumptech.glide:glide:4.11.0'
        annotationProcessor 'com.github.bumptech.glide:compiler:4.11.0'
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
 * ### 注意
   1. 使用相机权限时请单独处理魅族手机(魅族手机的通病)
   
   ~~2. 测试demo中接入的4.0.3版本的[TakePhoto](https://github.com/crazycodeboy/TakePhoto)框架
        ```
         compile ('com.jph.takephoto:takephoto_library:4.0.3',{
                exclude group: 'com.android.support',module: 'support-v4'
            })
        ```
      可能会将其中的support-v4包忽略,4.10版本的takephoto作者换了包名,请自己替换~~
      
   3. 新Demo依赖如下
      ```
        implementation 'com.github.LuckSiege.PictureSelector:picture_library:v2.5.8'
        implementation 'com.yanzhenjie:permission:2.0.3'
        implementation 'io.reactivex.rxjava3:rxandroid:3.0.0'
        implementation 'io.reactivex.rxjava3:rxjava:3.0.4'
      ```   
    ***
 * ### 感谢
    [lipangit](https://github.com/lipangit)的[JiaoZiVideoPlayer](https://github.com/lipangit/JiaoZiVideoPlayer)
    ***

[1]:https://img.shields.io/:license-apache-blue.svg
[2]:https://www.apache.org/licenses/LICENSE-2.0.html
[3]:https://img.shields.io/badge/API-17%2B-red.svg?style=flat
[4]:https://android-arsenal.com/api?level=17
[5]:https://img.shields.io/badge/release-1.1.0-red.svg
[6]:https://github.com/qq2519157/RichEditor/releases
[7]:https://img.shields.io/badge/PRs-welcome-brightgreen.svg
[8]:https://github.com/qq2519157/RichEditor/pulls
