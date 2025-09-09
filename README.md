# Parkinson GUI

<img width="1913" height="1046" alt="Screenshot from 2025-09-06 12-06-28" src="https://github.com/user-attachments/assets/b3f21808-65ea-4e90-88ab-aecd04d69289" />

While the actual project can be done with only python, the GUI application will provide a clean and modern interface to run the parkinson prediction model.
The python scripts for this project will be in the ``` python ``` folder while the rest of the directories are for the Application itself

## Technical details 
1. Programming Language : Java 21
2. GUI Framework : JavaFX 21
3. Build tool : Gradle
4. Developed and tested on : Linux Ubuntu 24

## How the Application works

(Orange - Java | White - User | Green - Python)

<img width="507" height="1022" alt="PD-GUI-FlowChart" src="https://github.com/user-attachments/assets/e28b9b4d-df9e-4abe-be83-e25feeb047c1" />

## To run this application on your machine 

**Downloading the source code**
1. Download the .zip from the ``` <Code> ``` dropdown at the top of this repo
2. Exctract the .zip folder
3. Navigate to project Directory :- ``` cd ParkinsonGUI ```

**First we setup the python virtual environment**
1. Navigate to python directory :- ``` cd python ```
2. Create the venv with command :- ```python -m venv venv```
3. Activate the venv :- ```venv\Scripts\activate```
4. Install libaries :- ``` pip install -r requirements.txt```

**Running it with IntelliJ IDEA**
1. Install IntelliJ IDEA (Community Edition)
2. Open the ``` ParkinsonGUI ``` folder with IntelliJ IDEA
4. Navigate to the main ``` App.java``` main file (src/main/java/com/parkinsongui)
5. If Java is not installed, you will get a yellow prompt "Project JDK not defined". Click on "Setup SDK" and install Java v21 (Any distribution works)
6. Click on the gradle icon on the right toolbar (an elephant icon)
7. ParkinsonGUI > Tasks > application > run

## Snapshots

<img width="1913" height="1046" alt="Screenshot from 2025-09-06 12-06-45" src="https://github.com/user-attachments/assets/957e6ab6-c98e-4956-b12b-49afe0ddfe14" />
<img width="1913" height="1046" alt="Screenshot from 2025-09-06 12-07-02" src="https://github.com/user-attachments/assets/b40dac6f-852b-4550-88e3-ad18fc856f21" />

