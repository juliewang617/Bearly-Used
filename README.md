# Project Details

**Bearly Used**

Bearly Used is a platform designed to facilitate sustainable buying, selling, and trading within the Brown University and RISD communities. The application includes features for users to list items, search through available listings, and communicate with other users. Authentication is managed through Clerk, ensuring only authorized users from Brown and RISD can access the platform.

# Github link:

https://github.com/cs0320-f24/term-project-ezheng37-jwang702-kli181-sliao13

**Team members**: Eric Zheng (ezheng37), Julie Wang (jwang702), Kathy Li (kli181), Sarah Liao (sliao13) 

# Design Choices + Project Overview

**Listing management:**
Users can create new listings by uploading images, adding descriptions, and specifying details like price, category, and condition. These listings are stored in Supabase, allowing them to persist across sessions and remain visible to other users. The ListItemPopup.tsx file provides a straightforward interface for creating and editing listings, while UserProfile.tsx lets users manage their active listings, including editing, deleting, or marking them as sold. On the backend, files like AddListingHandler.java, DeleteListingHandler.java, and UpdateListingHandler.java handle creating, removing, and updating listings, while RealStorage.java connects to the database and Sorter.java handles filtering and organizing listings.

**User profiles:**
Each user has a profile displaying personal details and a list of their active listings, which can be updated at any time. The UserProfile.tsx component shows this information, while EditProfilePopup.tsx provides a form for users to make changes to their name, school, or phone number. On the backend, AddUserHandler.java creates profiles for new users, GetUserHandler.java retrieves existing ones, and UpdateUserHandler.java saves any updates. These backend files work with RealStorage.java to handle data storage and retrieval efficiently.

**Filters and search:**
Users can narrow down listings using filters for categories, price ranges, and availability or search for specific keywords to quickly find relevant items. These features are part of the HomePage.tsx component, which offers dropdown menus and a search bar for customizing the display. On the backend, GetListingsHandler.java processes these filters and search requests, and Utils.java provides helper methods to ensure accurate results. The Sorter.java file organizes listings based on the selected criteria, making it easier for users to find what they’re looking for.

**Authentication:**
The app uses Clerk to handle authentication, ensuring that only users with Brown or RISD email addresses can sign in. New users are greeted with a clean sign-in page (SignUp.tsx) and are guided through setting up their profile with SetupProfile.tsx. The main App.tsx file ensures that only logged-in users can access the app. On the backend, AddUserHandler.java initializes profiles for new users, while GetUserHandler.java retrieves their data once they are signed in.

**Responsive design:**
The app is designed to work well on any device, from desktops to smartphones. Custom CSS files like HomePage.css and UserProfile.css provide a responsive layout that adapts to different screen sizes, and Bootstrap’s grid system helps maintain a clean and consistent design. Even the setup flow, managed by SetupProfile.css, is optimized for mobile users, ensuring the app remains easy to use no matter the device.

# Errors/Bugs
There are currently no known errors. There may be a few small bugs - one we know of currently is that when you edit a listing and then upload a new image, it renders twice. There are also a lot of future implementations we would like to make that are out of scope.

# How to

First, clone the repo, and start the server in `/server` by running `mvn package` and then run the file with `./run` or with the green play button in IntelliJ. Then, in `/client`, run `npm install` followed by `npm start` to access the app.

# Collaboration

OpenAI. (2024). ChatGPT (May 24 version) [Large language model]. https://chat.openai.com/chat/
We used ChatGPT to debug errors and to help fix styling for certain elements. We also used it to help with testing, syntax, and docs.

W3Schools (https://www.w3schools.com/) was used for CSS help.

Bootstrap (https://getbootstrap.com/) was used for elements like the modals and dropdowns.

Our backend uses Supabase (https://supabase.com/docs) to store data.