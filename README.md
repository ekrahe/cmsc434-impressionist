# cmsc434-impressionist
## Overview
This is the second Android project I made for CMSC 434: Introduction to Human-Computer Interaction. In this one, you can load an image that is displayed on the left side of the screen, and then draw on the right. Color samples from the LHS are used to create an Impressionist version of it on the right. You can save that creation to an app-specific folder. I included velocity-based brushes along with one that spins as you move your finger, undo/redo, and a preview of your brush that mirrors your actions as you draw on the RHS.

The undo/redo and preview features may not have been too technically difficult, but I feel that they were the two most useful/valuable additions to the app.

## Usage
This application runs on Android 4.03+, which is APIs 15-23. It forces landscape orientation, which is key for ample drawing space, but loading a photo still takes place in your system default orientation. 

When the user opens the app, they cannot draw immediately. Fist they must load an image. Some example images provided by the professor can be downloaded using the 'Download Images' button in the bottom left. After an image is loaded, the user can draw, choose their brush among 6 options, undo/redo, clear, and save their drawing.

## References
A lot more intuition was used this time around, and I did not use any references to learn how to do something.
The one source I used was to just confirm that I had correctly set up my toolbar that holds the undo and redo buttons. 

* http://www.sitepoint.com/material-design-android-design-support-library/
