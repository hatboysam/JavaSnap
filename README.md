# JavaSnap - Unofficial Java API Client for Snapchat

## Overview
JavaSnap provides a simple Java interface to the Snapchat API, which has been unofficially documented.   It could be used to do the following, and more:

* Download Snaps to your computer or Android device.
* Send a local File as a Snap to your friends.

## Usage
### Build

Build a `jar` with Maven using:

    mvn clean compile assembly:assembly
	
### Command Line Execution
Run the `jar` in `target/` with:

    java -jar target/JavaSnap-1.1-SNAPSHOT-withDependency-ShadedForAndroid.jar
	
It should look something like this:

    samuelsternsmbp:JavaSnap samstern$ java -jar target/JavaSnap-1.0-SNAPSHOT-jar-with-dependencies.jar
    Snapchat username:
    YOUR_USERNAME
    Snapchat password:
    YOUR_PASSWORD
    Logging in...
    <options, etc>
	
Running the java library via the command line will allow you to send a local 'jpg' to a friend as a Snap or a Story, download all of your received 'jpg' snaps as well as downloading all of your friends stories.

### Using Library Functions
#### Logging In

    JSONObject loginObj = Snapchat.login(username, password);
    String authToken = loginObj.getString(Snapchat.AUTH_TOKEN_KEY);
	
**Note:** Keep track of the `loginObj` and `authToken`, you will need them as arguments for other methods.

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
    boolean video = false; //whether or not 'file' is a video or not.
    String mediaId = Snapchat.upload(file, username, authToken, video);
	
If that succeeds, use the following code to send it:

    List<String> recipiends = new ArrayList<String>();
    recipients.add("somebody");
    recipients.add("somebodyElse123");
    
    int viewTime = 10; //seconds
    boolean myStory = false; // add to story, or not
    boolean result = Snapchat.send(mediaId, recipients, myStory, viewTime, username, authToken);
	
#### Setting a Story	
Setting a Story consists of two steps: uploading and setting.  When you upload a Story, you provide a unique identifier called `media_id` which you will use when sentting the story.

The following code demonstrates uploading a `File` as a Story:

    File file = new File(...);
    boolean video = false; //whether or not 'file' is a video or not.
    String mediaId = Snapchat.upload(file, username, authToken, video);
	
If that succeeds, use the following code to set it:

    List<String> recipiends = new ArrayList<String>();
    recipients.add("somebody");
    recipients.add("somebodyElse123");
    
    int viewTime = 10; //seconds
    boolean video = false; // whether or not this is video data.
    String caption = "My Story"; //This is only shown in the story list, not on the actual story photo/video.
    boolean result = Snapchat.sendStory(mediaId, viewTime, video, caption, username, authToken);
	
#### Get Stories
This method will make an API call to snapchat with your username and the token you received from logging in. It will then return your friends stories in the same way as fetching snaps does.

    Story[] storyObjs = Snapchat.getStories(username, token);
    Story[] downloadable = Story.filterDownloadable(storyObjs); //All stories are downloadable but this makes the Story object in the same format as the Snap one.
	
A separate API call will be needed to download each `Story`, you will need to pass the Story[] you want to download as the first argument along with your username and token from logging in as the second and third arguments respectively.

    byte[] storyBytes = Snapchat.getStory(s, username, token);

#### Update Snap information
This method allows you to change the status of a specific snap/story. For example, marking the snap as viewed/screenshot/replayed.
You need to pass in the snapId for the snap you want to update, a boolean for seen/screenshot/replayed, along with your username, authtoken from logging in and your login object.

    updateSnap(snapId, seen, screenshot, replayed, username, authToken, loginObject)



## Other Information

* This code is based on the Gibson Security guide to the Snapchat API [here](http://gibsonsec.org/snapchat/fulldisclosure/).
