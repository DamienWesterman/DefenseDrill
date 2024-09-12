# DefenseDrill
Defense Drill Exercise Generation App

# Introduction
This app was created to help train and improve my self-defense martial arts skills. It takes all the exercises you've created - referred to as Drills - and create a workout for you. This workout is meant to help keep self-defense skills fresh by choosing a workout of Drills that you are less confident on or haven't practiced recently. While this was designed with martial arts in mind, it is flexible enough that it can be used for any kind of practice! For example, if you are practicing an instrument it can help to choose what exercise you should do for your scales, long tones, or articulation. For gym workouts it will give you a random variety of different muscle groups so you're always getting a right proper varied workout!

# Features
The app features a drill generation algorithm that prioritizes new drills, those marked as less confident by users, and drills that haven’t been practiced recently. Drills can be put into categories (such as by martial arts school) and sub-categories (such as strikes or kicks). During drill generation you are able to specify the kind of workout you want by selecting Category/Sub-Category combinations. This app features a very simple card based UI system that is easy and intuitive to use with a robust error feedback system. Much of the user feedback is communicated via snackbar messages and popups. Users can view, create, edit, and delete Drills, Categories, and Sub-Categories. All data is stored locally in device storage.

# Technologies Used
- Android Studio and SDK
- Java
- Room DB
- MVVM Architecture
- JUnit/Mockito for testing
- Git

# Setup and Instillation
To get started, clone the repository using Android Studio. Go to File > New > Project from Version Control. From the popup, select 'Git' for Version Control, and paste the URL `https://github.com/DamienWesterman/DefenseDrill.git`. Choose your desired directory, click 'Clone', and then build and install the project on a virtual or physical device. Please also be sure to see docs/diagrams for general application information.

# Usage
The app’s user interface is intuitive, featuring a card-based system for easy navigation. The home screen is as follows:

<img src="docs/screenshots/Home%20Screen.jpg" alt="Home Screen" width="200"/>

Then if you go on to select Generate Drill, you are offered two screens that look like the following, allowing you to pick a specific Category or Sub-Category of workout:

<img src="docs/screenshots/Workout%20Generation.jpg" alt="Workout Generation" width="200"/>

Once a drill is randomly selected, you are brought to the drill info screen. From here it shows some of the relevant information saved about the drill, along with several options to customize your workout. If you do not like the Drill that is selected for whatever reason, you can simply skip it - though if you continue to do this you will run out of drills eventually and will have to reset any you skipped in order to continue generating drills.

<img src="docs/screenshots/Drill%20Info%20Screen.jpg" alt="Drill Info Screen" width="200"/>

Then once you have exercised the drill, you can indicate that you have finished, save a new confidence level, and either go back home or continue generating more drills! From the home page, you also have the option to view and edit the Drills, Categories, and Sub-Categories. Each of these screens will look like the below, giving you the option to create, edit, delete, and view what you have saved.

<img src="docs/screenshots/List%20of%20Drills.jpg" alt="List of Drills" width="200"/>

# Future Improvements
I am actively working on expanding the project. I am currently learning and preparing to make a backend that allows users to have a central place to save all of their drills and even create lists of instructions for each drill. This local server would give an easier way to create Drills and link them to their instructions and potentially even include how to videos. Once this is done, I will be sure to include the link for that project's repository. Once this backend is done, there will be updates to the application to allow it to connect to the backend and download new Drills, as well as have additional fields to access the instructions and how to's from within the Drill Infor screens.

Aside from the above, I also have ideas and plans for some of the following features:
- Implementing a Help / first time use screen that guides users through the app explaining how to use the system as well as some of the terminology and use cases
- Potentially have a way to sync notes and other data between different devices
- Possibly figure out a way to have a mentor/mentee mode where the mentor will push recently learned exercises or workouts to the mentee's app and track practice habits

# Credits/Contributors
Special thanks to the staff and students at [Premier Martial Arts](https://premiermartialarts.com/) for their inspiration and ideas for this app! PMA is not affiliated with this project in any way, they are just awesome, and I recommend them for learning self-defense. 

# License
This project is licensed under the Apache License 2.0. For details, see the LICENSE file.
