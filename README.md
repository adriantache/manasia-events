# Manasia Events
*A mockup functional app to pitch to Manasia Hub for their events*

An opportunity has arisen to create and pitch an app for Manasia Hub, a bar in Bucharest. After discussing with their manager, 
the main requirements for the app are to allow people to get details and notifications for events that take place at Manasia.

In order to minimize the overhead for Manasia and create a semi-autonomous app, event data is fetched via OKHTTP inside a loader 
from their FB Events page and parsed as a JSON, which is downloaded by the app and decoded into a custom class, which is also 
stored locally in an SQLite database, accessed via a Provider. Event photos are stored as an URL which is decoded from the FB 
info into actual accessible URLs, and they are loaded in using Picasso. Upon clicking any event in the list, an event details
page is opened using a transition (*for API >= LOLLIPOP*) based on the event image, which contains further details as well as 
intents for setting up individual event notifications and calling or navigating to the bar.

There is extra functionality in notifications, which use the latest practices, including [notification channels](https://medium.com/p/ae36006a921b), and they are 
[scheduled using WorkManager](https://medium.com/@eydryan/scheduling-notifications-on-android-with-workmanager-6d84b7f64613) to trigger on the day of the event at or after 12am. Notifications also have work as a general setting,
which is set using a SnackBar and stored in SharedPreferences. On click they open the details page for the event and auto-dismiss. 
They also create an intent towards MainActivity in order to maintain expected behaviour. I did not use Android's implementation 
for this task as the effects were not as intended.

The app also supports a widget showing the next event, with a PendingIntent to open the relevant event details page. It also uses
some workarounds, including accessing the Bitmap fetched by Picasso, in order to ensure functionality around the strange caching
mechanism that Android uses.

Finally, the app includes a menu for the bar which is more of a design exercise than anything else, but also uses custom 
behaviours for the back key and button. 

The app is still in development, with many ideas still pending and will be modified depending on feedback or interest from the 
people at Manasia.

**Functionality demo** (*as of 18/10/18, does not include notifications demo*):

![Manasia App Demo](https://thumbs.gfycat.com/QualifiedFreeHypacrosaurus-size_restricted.gif)
