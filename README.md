# JavaSnap - Unofficial Java API Client for Snapchat

## Overview
JavaSnap provides a simple Java interface to the Snapchat API, which has been unofficially documented.   It could be used to do the following, and more:

* Download Snaps to your computer or Android device.
* Send a local File as a Snap to your friends.
* Most features from the original Snapchat app.

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

    Snapchat snapchat = Snapchat.login(username, password);

#### Get Friends
You can use the following code to get all your friends. Snapchat usernames and real names (as you have assigned them):

    Friend[] friends = snapchat.getFriends();

#### Get Snaps
You can use the following code to get all your snaps :

    Snap[] snaps = snapchat.getSnaps();
	
A separate API call will be needed to download each `Snap`.  The `getSnaps()` method will return some Snaps that are not available to download, such as already-viewed Snaps or snaps that don't contain media (such as friend requests).  A `Snap` can be downloaded if `snap.isIncoming() == true`, `snap.isMedia() == true`, and `snap.isViewed() == false`.
To get a list of only such snaps, you can pass the `Snap[]` to method `Snapchat.filterDownloadable(Snap[] snaps)`. You can also use `snaps[#].isDownloadable()`.

#### Download a Snap
Once you have determined a Snap candidate for downloading using the methods above, the following code will fetch the actual media and save it to a file:

    byte[] snapBytes = snapchat.getSnap(snap);
    File snapFile = new File(...);
    FileOutputStream snapOs = new FileOutputStream(snapFile);
    snapOs.write(snapBytes);
    snapOs.close();

#### Sending a Snap	
Sending a Snap consists of two steps: uploading and sending.  When you upload a Snap, you provide a unique identifier called `media_id` which you will use when sending the snap to its eventual recipients.
Lucky you, the API will do everything for you in the background.

The following code demonstrates uploading a `File` as a Snap:

    File file = new File(...);
    boolean video = false; //whether or not 'file' is a video or not.
    boolean story = false; //whether or not add this to your story.
    int time = 10; //How long will the snap last. Max = 10.
    List<String> recipients = (...);
    String mediaId = Snapchat.upload(file, recipients, video, story, time);
	
#### Setting a Story	
Setting a Story consists of two steps: uploading and setting.  When you upload a Story, you provide a unique identifier called `media_id` which you will use when sentting the story.
Lucky you, the API will do everything for you in the background.

The following code demonstrates uploading a `File` as a Story:

    File file = new File(...);
    boolean video = false; //whether or not 'file' is a video or not.
    int time = 10; //How long will the snap last. Max = 10.
    String caption = "My Story"; //Can be anything. We couldn't find any effect.
    boolean result = snapchat.sendStory(file, video, time, caption);
	
#### Get Stories
You can use the following code to get all your stories :

    Story[] storyObjs = snapchat.getStories();
    Story[] downloadable = Story.filterDownloadable(storyObjs); //All stories are downloadable but this makes the Story object in the same format as the Snap one.
	
A separate API call will be needed to download each `Story`, you will need to pass the Story[] you want to download as argument.

    byte[] storyBytes = Snapchat.getStory(story);

#### Update Snap information
This method allows you to change the status of a specific snap/story. For example, marking the snap as viewed/screenshot/replayed.
You need to pass in the snap object for the snap you want to update, a boolean for seen/screenshot/replayed.

    snapchat.setSnapFlags(snap, seen, screenshot, replayed)



## Other Information

* This code is based on the Gibson Security guide to the Snapchat API [here](http://gibsonsec.org/snapchat/fulldisclosure/).
