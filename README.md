# Attention App Hack-a-Bull 2018

Inspiration

Our original inspiration was to develop an app to help students - for example those with ADHD - focus more easily when studying. However, we soon realized that our app has limitless possibilities, from preventing car accidents caused by distracted drivers, to teaching people how to be better communicators, presenters, and listeners.

What it does

The fix8 mobile app uses Muse to interpret brainwaves and quantify focus and drowsiness. Based on data received and user preferences, fix8 sends notifications with sound or vibration that tell the user if they have dipped below focus or drowsiness thresholds. Helpful tips accompany these notifications, ensuring users have a constructive way to get their minds back on track. The focus and drowsiness data for each session is stored on MongoDB Atlas and can be visualized through users' personal accounts through the fix8 website.

How we built it

We built the app using Android Studio primarily with some little portion of native C++ code for the hardware integration. Using MongoDB Stitch for user authentication(email/password) and storing as well retrieving the user specific data. Our mobile app connects to the brain sensing headband, Muse, and we extrapolate the data coming from it to detect several types of cognitive activities.

Challenges we ran into

Our biggest challenges included managing time and getting components to work together smoothly, even though they were worked on separately.

Accomplishments that we're proud of

Finishing a pretty app.

What we learned

Some members, who were new to app development, used Android Studio for the first time, while other members learned how to implement MongoDB Stitch as a backend service.

What's next for fix8
We would like to add more resources teaching people how to focus better, as well as implement different user interfaces depending on the activity users are engaged in and their demographic. For example, fix8 for drivers would include minimal text/visual distractions, while a different version of fix8 might turn the amount of time a user can stay focused into a game that they can compete against their friends with. Allowing live visualization of data collected by Muse on our website is also a priority.
