# JavaSnap - Unofficial Java API Client for Snapchat

## Usage
### Build

Build a `jar` with Maven using:

	mvn clean compile assembly:single
	
### Execute
Run the `jar` in `target/` with:

	java -jar target/JavaSnap-1.0-SNAPSHOT-jar-with-dependencies.jar
	
It should look something like this:

	samuelsternsmbp:JavaSnap samstern$ java -jar target/JavaSnap-1.0-SNAPSHOT-jar-with-dependencies.jar
	Snapchat username:
	YOUR_USERNAME
	Snapchat password:
	YOUR_PASSWORD
	Logging in...
	Fetching snaps...
	Downloading snap from somefriend123
	Downloading snap from anotherfriend234
	
All unviewed, non-image Snaps will be downloaded to the JavaSnap directory as `.jpg` files.

### Other Information

* Based on the Gibson Security guide to the Snapchat API [here](http://gibsonsec.org/snapchat/fulldisclosure/).
