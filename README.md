# LitePal for Android  
![Logo](https://github.com/LitePalFramework/LitePal/blob/master/sample/src/main/logo/mini_logo.png) 

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
 * **[litepal-1.3.2.jar](https://github.com/LitePalFramework/LitePal/raw/master/downloads/litepal-1.3.2.jar)** (library contains *.class files)
 * **[litepal-1.3.2-src.jar](https://github.com/LitePalFramework/LitePal/raw/master/downloads/litepal-1.3.2-src.jar)** (library contains *.class files and *.java files)
 
## Quick Setup
#### 1. Include library
##### Using Eclipse
 * Download the latest jar in the above section. Or browse all versions **[here](https://github.com/LitePalFramework/LitePal/tree/master/downloads)** to choose one to download.
 * Put the jar into **libs** folder of your Android project.
 
##### Using Android Studio
Edit your **build.gradle** file and add below dependency:
``` groovy
dependencies {
    compile 'org.litepal.android:core:1.3.2'
}
```
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
    
    <!--
        Define where the .db file should be. "internal" means the .db file
        will be stored in the database folder of internal storage which no
        one can access. "external" means the .db file will be stored in the
        path to the directory on the primary external storage device where
        the application can place persistent files it owns which everyone
        can access. "internal" will act as default.
        For example:
        <storage value="external"></storage>
    -->
    
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
That's OK. LitePal can still live with that. Just change the inheritance of **MyOwnApplication** from **Application** to **LitePalApplication**, like:
``` java
public class MyOwnApplication extends LitePalApplication {
	...
}
```
Finally, if it still didn't work for you because you have to inherit from another Application. In this case, you can call **LitePalApplication.initialize(context)** instead of inheritance:
```java
public class MyOwnApplication extends AnotherApplication {

	@Override
	public void onCreate() {
		super.onCreate();
		LitePalApplication.initialize(this);
	}
	...
}
```
Just make sure to call this method as early as you can. In the **onCreate()** method of Application will be fine. And always remember to use the application context as parameter. Do not use any intance of activity or service as parameter, or memory leaks might happen.
## Get Started
After setup, you can experience the powerful function now.

#### 1. Create tables
Define the models first. For example you have two models, **Album** and **Song**. The models can be defined as below:
``` java
public class Album extends DataSupport {
	
	@Column(unique = true, defaultValue = "unknown")
	private String name;
	
	private float price;
	
	private byte[] cover;
	
	private List<Song> songs = new ArrayList<Song>();

	// generated getters and setters.
	...
}
```
``` java
public class Song extends DataSupport {
	
	@Column(nullable = false)
	private String name;
	
	private int duration;
	
	@Column(ignore = true)
	private String uselessField;
	
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
	name text unique default 'unknown',
	price real,
	cover blob
);

CREATE TABLE song (
	id integer primary key autoincrement,
	name text not null,
	duration integer,
	album_id integer
);
```

#### 2. Upgrade tables
Upgrade tables in LitePal is extremely easy. Just modify your models everyway you want:
```java
public class Album extends DataSupport {
	
	@Column(unique = true, defaultValue = "unknown")
	private String name;
	
	@Column(ignore = true)
	private float price;
	
	private byte[] cover;
	
	private Date releaseDate;
	
	private List<Song> songs = new ArrayList<Song>();

	// generated getters and setters.
	...
}
```
A **releaseDate** field was added and **price** field was annotated to ignore.
Then increase the version number in **litepal.xml**:
```xml
<!--
    Define the version of your database. Each time you want 
    to upgrade your database, the version tag would helps.
    Modify the models you defined in the mapping tag, and just 
    make the version value plus one, the upgrade of database
    will be processed automaticly without concern.
    For example:    
    <version value="1" ></version>
-->
<version value="2" ></version>
```
The tables will be upgraded next time you operate database. A **releasedate** column will be added into **album** table and the original **price** column will be removed. All the data in **album** table except those removed columns will be retained.

But there are some upgrading conditions that LitePal can't handle and all data in the upgrading table will be cleaned:
 * Add a field which annotated as `unique = true`.
 * Change a field's annoation into `unique = true`.
 * Change a field's annoation into `nullable = false`.

Be careful of the above conditions which will cause losing data.

#### 3. Save data
The saving API is quite object oriented. Each model which inherits from **DataSupport** would have the **save()** method for free:
``` java
Album album = new Album();
album.setName("album");
album.setPrice(10.99f);
album.setCover(getCoverImageBytes());
album.save();
Song song1 = new Song();
song1.setName("song1");
song1.setDuration(320);
song1.setAlbum(album);
song1.save();
Song song2 = new Song();
song2.setName("song2");;
song2.setDuration(356);
song2.setAlbum(album);
song2.save();
```
This will insert album, song1 and song2 into database with associations.

#### 4. Update data
The simplest way, use **save()** method to update a record found by **find()**:
``` java
Album albumToUpdate = DataSupport.find(Album.class, 1);
albumToUpdate.setPrice(20.99f); // raise the price
albumToUpdate.save();
```
Each model which inherits from **DataSupport** would also have **update()** and **updateAll()** method. You can update a single record with a specified id:
``` java
Album albumToUpdate = new Album();
albumToUpdate.setPrice(20.99f); // raise the price
albumToUpdate.update(id);
```
Or you can update multiple records with a where condition:
``` java
Album albumToUpdate = new Album();
albumToUpdate.setPrice(20.99f); // raise the price
albumToUpdate.updateAll("name = ?", "album");
```

#### 5. Delete data
You can delete a single record using the static **delete()** method in **DataSupport**:
``` java
DataSupport.delete(Song.class, id);
```
Or delete multiple records using the static **deleteAll()** method in **DataSupport**:
``` java
DataSupport.deleteAll(Song.class, "duration > ?" , "350");
```

#### 6. Query data
Find a single record from song table with specified id:
``` java
Song song = DataSupport.find(Song.class, id);
```
Find all records from song table:
``` java
List<Song> allSongs = DataSupport.findAll(Song.class);
```
Constructing complex query with fluent query:
``` java
List<Song> songs = DataSupport.where("name like ?", "song%").order("duration").find(Song.class);
```

## Developed By
 * Tony Green - tonygreendev@gmail.com
 
## Sample App
The sample app has been published onto Google Play for easy access. 

Get it on:

[![Google Play](http://www.gstatic.com/android/market_images/web/play_logo.png)](https://play.google.com/store/apps/details?id=org.litepal.litepalsample)

## Bugs Report
If you find any bug when using LitePal, please report **[here](https://github.com/LitePalFramework/LitePal/issues/new)**. Thanks for helping us building a better one.

## Change logs
### 1.3.2
 * Improve an outstanding speed up of querying and saving.
 * Support to store the database in external storage.
 * Support to mapping fields which inherit from superclass.
 * Add **findFirst()** and **findLast()** in fluent query.
 * Add **isExist()** and **saveIfNotExist()** method in DataSupport.

### 1.3.1
 * Support storing binary data. Byte array field will be mapped into database as blob type.
 * Add **saveFast()** method in DataSupport. If your model has no associations to handle, use **saveFast()** method will be much more efficient.
 * Improve query speed with optimized algorithm.
 
### 1.3.0
 * Add annotation functions to decalre **unique**, **not null** and **default** constraints.
 * Remove the trick of ignore mapping fields with non-private modifier.
 * Support to use annotation to ignore mapping fields with `ignore = true`
 * Add some magical methods in DataSupport for those who understand LitePal deeper.
 * Fix known bugs.
 
## License
```
Copyright (C)  Tony Green, LitePal Framework Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
