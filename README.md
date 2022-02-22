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
   #### 需要添加mavenCentral 
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
            }
      }
     ```
   
   #### Gradle:
     在你的module的build.gradle文件
     ```
        implementation 'com.log1992:RichEditor:2.0.0'
     ```
   ### Maven:
     ```
       <dependency>
         <groupId>com.log1992</groupId>
         <artifactId>RichEditor</artifactId>
         <version>2.0.0</version>
         <type>aar</type>
      </dependency>
     ```
   ### Lvy
     ```
       <dependency org="com.log1992" name="RichEditor" rev="2.0.0" />
     ```
  
     ***
 * ### 引入的库：
    ```
         implementation 'androidx.appcompat:appcompat:1.4.0'
         testImplementation 'junit:junit:4.13.2'
         implementation 'cn.jzvd:jiaozivideoplayer:7.4.1'
         api 'com.github.bumptech.glide:glide:4.12.0'
         annotationProcessor 'com.github.bumptech.glide:compiler:4.12.0'
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
   1. 权限处理
   
   2. FileProvider以及`android:requestLegacyExternalStorage="true"`
   
    ***
 * ### 感谢
    [Jzvd](https://github.com/Jzvd)的[JZVideo](https://github.com/Jzvd/JZVideo)
    ***

[1]:https://img.shields.io/:license-apache-blue.svg
[2]:https://www.apache.org/licenses/LICENSE-2.0.html
[3]:https://img.shields.io/badge/API-24%2B-red.svg?style=flat
[4]:https://android-arsenal.com/api?level=24
[5]:https://img.shields.io/badge/release-2.0.0-red.svg
[6]:https://github.com/qq2519157/RichEditor/releases
[7]:https://img.shields.io/badge/PRs-welcome-brightgreen.svg
[8]:https://github.com/qq2519157/RichEditor/pulls
