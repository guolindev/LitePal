# ![Logo](https://github.com/LitePalFramework/LitePal/blob/master/sample/res/drawable-ldpi/logo.png) LitePal for Android
LitePal is an Open Source Android library that allows developers to use SQLite database extremely easy. You can finish most of the database operations without writing even a SQL statement, including create or upgrade tables, crud operations, aggregate functions, etc. The setup of LitePal is quite simple as well, you can integrate it into your app in less than 5 minutes. 

Explore the magic right now. Have Fun!!

## Features
 * Using object-relational mapping (ORM) pattern.
 * Almost zero-configuration(only one configuration file with few properties).
 * Maintains all tables automatically(e.g. create, alter or drop tables).
 * Encapsulated APIs for avoiding writings SQL statements.
 * Alternative choice to use SQL still, but easier and better APIs than the originals.
 * More for you to explore.
 
## Latest Downloads
 * **[litepal-1.1.0.jar](https://github.com/LitePalFramework/LitePal/raw/master/downloads/litepal-1.1.0.jar)** (library contains *.class files)
 * **[litepal-1.1.0-src.jar](https://github.com/LitePalFramework/LitePal/raw/master/downloads/litepal-1.1.0-src.jar)** (library contains *.class files and *.java files)
 
## Quick Setup
#### 1. Include library
 * Download the latest jar in the above section or browse all the versions **[here](https://github.com/LitePalFramework/LitePal/tree/master/downloads)** to choose the one you want to download.
 * Put the jar in the **libs** folder of your Android project.
 
#### 2. Configure litepal.xml
Create a file in the assets folder of your project and name it as **litepal.xml**. Then copy the following codes into it.
``` xml
<?xml version="1.0" encoding="utf-8"?>
<litepal>
    <!--
		Define the database file name of your application. 
		By default each database file name should be end with .db. 
		If you didn't name your database end with .db, 
		LitePal would plus the suffix automaticly for you.
		For example:    
    	<dbname value="demo" ></dbname>
		
		Note that this tag is necessary.
    -->
    <dbname value="demo" ></dbname>

    <!--
    	Define the version of your codes currently is. Each time you 
    	want to upgrade your database, the version mark will help
    	you. Using LitePal to upgrade database is very simple. Modify
    	the models you defined in the mapping tag, and just make the
    	value in version mark plus one, the upgrade of database will
    	be processed automaticly without concern.
		For example:    
    	<version value="1" ></version>
		    	
    	Note that this tag is necessary.
    -->
    <version value="1" ></version>

    <!--
    	Define your models in the list with mapping tag, LitePal will create
    	tables for each mapping class with all the supported fields
    	defined in models into corresponding columns. Remeber to use
    	the full name of class, or LitePal won't be able to find it.
    	For example:    
    	<list>
    		<mapping class="com.test.model.Reader"></mapping>
    		<mapping class="com.test.model.Magazine"></mapping>
    	</list>
    	
    	Note that the list tag and mapping tag are necessary.
    -->
    <list>
    </list>

</litepal>
```
This is the only configuration file, and the properties are simple. 
 * **<dbname>** configure the database name of project.
 * **<version>** configure the version of database. Each time you want to upgrade database, plus the value here.
 * **<list>** configure the mapping classes.
 
#### 3. Configure LitePalApplication
You don't want to pass the Context param all the time. To makes the APIs simple, just configure the LitePalApplication in **AndroidManifest.xml** as below.
``` xml
<manifest>
	<application
		android:name="org.litepal.LitePalApplication"
		...
	>
    ...
	</application>
</manifest>
```
Of course you may have your own Application and has already configured here, like:
``` xml
<manifest>
	<application
		android:name="com.example.MyOwnApplication"
		...
	>
    ...
	</application>
</manifest>
```
That's not problem. Just change the inheritance of **MyOwnApplication** from **Application** to **LitePalApplication**, like:
``` java
public class MyOwnApplication extends LitePalApplication {
	...
}
```
This will make all things work without any side effects.
