# Table of Contents Tool

## Setup in Eclipse IDE
I've never actually got compilation to work. I just use it for syntax validation and run from the command line via groovysh or groovyConsole.


## Python Venv
Create venv:
```
python -m venv venv
```

Start venv:
```
nenv\Scripts\activate
```

Load requirements:
```
pip install -r requirements.txt
```

Stop venv:
```
deactivate
```

Update `requirements.txt` from output of following command:
```
pip freeze
```