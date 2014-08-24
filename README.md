# LitePal for Android  
![Logo](https://github.com/LitePalFramework/LitePal/blob/master/sample/logo/mini_logo.png) 

LitePal is an Open Source Android library that allows developers to use SQLite database extremely easy. You can finish most of the database operations without writing even a SQL statement, including create or upgrade tables, crud operations, aggregate functions, etc. The setup of LitePal is quite simple as well, you can integrate it into your project in less than 5 minutes. 

Experience the magic right now and have fun!

## Features
 * Using object-relational mapping (ORM) pattern.
 * Almost zero-configuration(only one configuration file with few properties).
 * Maintains all tables automatically(e.g. create, alter or drop tables).
 * Encapsulated APIs for avoiding writing SQL statements.
 * Awesome cluster query function.
 * Alternative choice to use SQL still, but easier and better APIs than the originals.
 * More for you to explore.
 
## Latest Downloads
 * **[litepal-1.1.0.jar](https://github.com/LitePalFramework/LitePal/raw/master/downloads/litepal-1.1.0.jar)** (library contains *.class files)
 * **[litepal-1.1.0-src.jar](https://github.com/LitePalFramework/LitePal/raw/master/downloads/litepal-1.1.0-src.jar)** (library contains *.class files and *.java files)
 
## Quick Setup
#### 1. Include library
 * Download the latest jar in the above section.
 * Put the jar in the **libs** folder of your Android project.
 
#### 2. Configure litepal.xml
Create a file in the **assets** folder of your project and name it as **litepal.xml**. Then copy the following codes into it.
``` xml
<?xml version="1.0" encoding="utf-8"?>
<litepal>
    <!--
    	Define the database name of your application. 
    	By default each database name should be end with .db. 
    	If you didn't name your database end with .db, 
    	LitePal would plus the suffix automaticly for you.
    	For example:    
    	<dbname value="demo" ></dbname>
    -->
    <dbname value="demo" ></dbname>

    <!--
    	Define the version of your database. Each time you want 
    	to upgrade your database, the version tag would helps.
    	Modify the models you defined in the mapping tag, and just 
    	make the version value plus one, the upgrade of database
    	will be processed automaticly without concern.
			For example:    
    	<version value="1" ></version>
    -->
    <version value="1" ></version>

    <!--
    	Define your models in the list with mapping tag, LitePal will
    	create tables for each mapping class. The supported fields
    	defined in models will be mapped into columns.
    	For example:    
    	<list>
    		<mapping class="com.test.model.Reader"></mapping>
    		<mapping class="com.test.model.Magazine"></mapping>
    	</list>
    -->
    <list>
    </list>
</litepal>
```
This is the only configuration file, and the properties are simple. 
 * **dbname** configure the database name of project.
 * **version** configure the version of database. Each time you want to upgrade database, plus the value here.
 * **list** configure the mapping classes.
 
#### 3. Configure LitePalApplication
You don't want to pass the Context param all the time. To makes the APIs simple, just configure the LitePalApplication in **AndroidManifest.xml** as below:
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

## Get Started
After setup, you can experience the powerful function now.

#### 1. Create tables
Define the models first. For example you have two models, **Album** and **Song**. The models can be defined as below:
``` java
public class Album extends DataSupport {
	
	private String name;
	
	private float price;
	
	private List<Song> songs = new ArrayList<Song>();

	// generated getters and setters.
	...
}
```
``` java
public class Song extends DataSupport {
	
	private String name;
	
	private int duration;
	
	private Album album;

	// generated getters and setters.
	...
}
```
Then add these models into the mapping list in **litepal.xml**:
``` xml
<list>
    <mapping class="org.litepal.litepalsample.model.Album"></mapping>
    <mapping class="org.litepal.litepalsample.model.Song"></mapping>
</list>
```
OK! The tables will be generated next time you operate database. For example, gets the **SQLiteDatabase** with following codes:
``` java
SQLiteDatabase db = Connector.getDatabase();
```
Now the tables will be generated automatically with SQLs like this:
``` sql
CREATE TABLE album (
	id integer primary key autoincrement,
	price real, 
	name text
);

CREATE TABLE song (
	id integer primary key autoincrement,
	duration integer, 
	name text, 
	album_id integer
);
```

