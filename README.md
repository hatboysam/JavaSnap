# JavaSnap - Unofficial Java API Client for Snapchat

## Overview
JavaSnap provides a simple Java interface to the Snapchat API, which has been unofficially documented.   It could be used to do the following, and more:

* Download Snaps to your computer or Android device.
* Send a local File as a Snap to your friends.

## Usage
### Build

Build a `jar` with Maven using:

	mvn clean compile assembly:single
	
### Command Line Execution
Run the `jar` in `target/` with:

	java -jar target/JavaSnap-1.0-SNAPSHOT-jar-with-dependencies.jar
	
It should look something like this:

	samuelsternsmbp:JavaSnap samstern$ java -jar target/JavaSnap-1.0-SNAPSHOT-jar-with-dependencies.jar
	Snapchat username:
	YOUR_USERNAME
	Snapchat password:
	YOUR_PASSWORD
	Logging in...
	<options, etc>
	
You can either send a local `jpg` file to a friend as a Snap, or download all of your unviewed image snaps to the local directory as `jpg` files using the presented options.

### Using Library Functions
#### Logging In

	JSONObject loginObj = Snapchat.login(username, password);
	String authToken = loginObj.getString(Snapchat.AUTH_TOKEN_KEY);
	
**Note:** Keep track of the `loginObj` and `authToken`, you will need them as arguments to other methods.

#### Get Friends
A list of your Snapchat friends is returned in the `loginObj` from the `login()` method.  You can use the following code to extract them as Java objects containing the friends' Snapchat usernames and real names (as you have assigned them):

	Friend[] friends = Snapchat.getFriends(loginObj);

#### Get Snaps
The `JSONObject` returned after login contains all of the metadata for your Snaps.  The following code will extract it into `Snap` objects.

	Snap[] snaps = Snapchat.getSnaps(loginObj);
	
A separate API call will be needed to download each `Snap`.  The `getSnaps()` method will return some Snaps that are not available to download, such as already-viewed Snaps or snaps that don't contain media (such as friend requests).  A `Snap` can be downloaded if `snap.isIncoming() == true`, `snap.isMedia() == true`, and `snap.isViewed() == false`.

To get a list of only such snaps, you can pass the `Snap[]` to method `Snapchat.filterDownloadable(Snap[] snaps)`.

#### Download a Snap
Once you have determined a Snap candidate for downloading using the methods above, the following code will fetch the actual media and save it to a file:

	byte[] snapBytes = Snapchat.getSnap(snap, username, authToken);
	File snapFile = new File(...);
	FileOutputStream fos = new FileOutputStream(snapFile);
	snapOs.write(snapBytes);
	snapOs.close();

#### Sending a Snap	
Sending a Snap consists of two steps: uploading and sending.  When you upload a Snap, you provide a unique identifier called `media_id` which you will use when sending the snap to its eventual recipients.  

The following code demonstrates uploading a `File` as a Snap:

	File file = new File(...);
	String mediaId = Snapchat.upload(file, username, authToken);
	
If that succeeds, use the following code to send it:

	List<String> recipiends = new ArrayList<String>();
	recipients.add("somebody");
	recipients.add("somebodyElse123");
	
	int viewTime = 10; //seconds
	boolean result = Snapchat.send(mediaId, recipients, viewTime, username, authToken);

## Other Information

* This code is based on the Gibson Security guide to the Snapchat API [here](http://gibsonsec.org/snapchat/fulldisclosure/).
