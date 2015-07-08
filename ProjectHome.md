# Android Drawable Extension Toolkit #

Do you want android support SVG natively? Have you ever thought define your custom XML drawables?
ADET(Android Drawable Extension Toolkit) could solve these problems, use ADET, you can use SVG as a **REAL** drawable for any scenarios, such as use SVG for an android **NATIVE ImageView** src attribute. or even define your custom XML drawables to enhance and replace the ordinary simple shape drawable in android.

adet use [svg-android-2](http://code.google.com/p/svg-android-2) as it's svg library.
Why didn't I use LibSvgAndroid? Because the lib has an annoying bug that crash apps in some circumstance.

All in all, ADET can extend and enhance android drawable system.

the screen shot demonstrates a very popular SVG file named lion<br />
![http://adet.googlecode.com/svn/trunk/AdetSample/screenshots/screen_shot.jpg](http://adet.googlecode.com/svn/trunk/AdetSample/screenshots/screen_shot.jpg)

# Simple to Use #
  1. Just reference ADET as an Android library <br />
  1. Create a class(such as MyApp) which extends Application <br />
  1. in MyApp write this code below:
```
public class MyApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		// call this init method at your application once, and we done!
		ADET.init(getApplicationContext());
	}
}
```
  1. Don't forget to modify [AndroidManifest.xml](https://code.google.com/p/adet/source/browse/trunk/AdetSample/AndroidManifest.xml)
```
    <application
        android:name="MyApp" 
        android:label="@string/app_name"
        android:icon="@drawable/ic_launcher"
        android:theme="@style/AppTheme">
    </application>
```
  1. Put some SVG files into res/drawable folder, and then you can use these SVG files as **native** drawables in anywhere! the fellowing xml contents are part of [adet\_demo\_layout.xml](https://code.google.com/p/adet/source/browse/trunk/AdetSample/res/layout/adet_demo_layout.xml):
```
    <ImageView
          android:layout_width="fill_parent"
          android:layout_height="fill_parent"
          android:layout_gravity="center"
          android:background="#FF806440"
          android:scaleType="fitXY"
          android:src="@drawable/lion" />
```

Simple! isn't it? Using SVG in android has never been so easy before ;-)