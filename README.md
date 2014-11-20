Anoted
======

Another note editor for android

##Status 
  - [X] Basis database CRUD routines
  - [X] Base Drawer, Adapters, and Fragment resources defined and implemented
  - [X] Syncing of edited documents to the database
  - [ ] intergrating a rich text editor
  - [ ] Async routines for database access
  - [ ] Settings activity implmeneted, and allows the switch of themes.
  - [ ] Intergrate time stamps into database, and UI
  - [ ] Drawer can be sorted variously, to benifit the users
  - [ ] Documents permissions and lock settings
  - [ ] Sync documents to cloud servics
  - [ ] Share notes through Bluetooth and NFC
  - [ ] Fullfill important unit tests
  - [ ] Clarify UI design satifastory

##Biography

My most used note editor is Simple Notepad on the Google Play store. I like it as allows customization of the interface, is fairly straight forward, and has great support for older devices.

Anoted (pronouced An-note-ed) will follow in the steps of Simple Notepad, by providing several styles of UI - Light, TransLight, Dark, TransDark, following the android styles, Holo light, holo / material light etc... 

Anoted uses the Android navigation drawer to provide a flat way to open notes (termed "Documents" inside the source code)
The wanted design is for the drawer to appear full screen - action bars hidden, giving the user non-obtrusive, priority access to the navigation drawer (termed Document Drawer in source code).

Notes are stored inside SQLite, in the form of Text fields. Various additional meta tags will be added to the note rows over time, like timestamps and access control, however too keep it simple, notes at the moments are just named entities with content, each assigned a custom id. 

Anoted project is meant for me to practice good, well formed code, and to really play with what works, and what is capable with Android. The knowledge and codebase of Anoted will be used as a basis for Awed: an aweful editor for Android.

