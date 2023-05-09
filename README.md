# [AndroidKtExtension](https://github.com/GodTreeV/AndroidKtExtension)  

> Android extension function collection, easy to use quickly .
>
> 收集了`Android`常用的扩展函数，方便快速调用。

# How to use

# DataBinding & ViewBinding
```
	buildFeatures {
        	viewBinding = true
    	}
	
    	dataBinding {
        	enabled = true
    	}
```

[![](https://jitpack.io/v/GodTreeV/AndroidKtExtension.svg)](https://jitpack.io/#GodTreeV/AndroidKtExtension)

To get a Git project into your build:

**Step 1.** Add the JitPack repository to your build file

- [gradle](https://jitpack.io/#gradle)
- [maven](https://jitpack.io/#maven)
- [sbt](https://jitpack.io/#sbt)
- [leiningen](https://jitpack.io/#lein)

Add it in your root build.gradle at the end of repositories:

```kotlin
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

**Step 2.** Add the dependency

```kotlin
	dependencies {
	        implementation 'com.github.GodTreeV:AndroidKtExtension:1.0.1'
	}
```
