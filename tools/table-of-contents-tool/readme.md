# Table of Contents Tool
Python 3 CLI for generating and updating tables of contents within markdown files.

## Marking TOC location
The text file requires two things to indicate (a) that a TOC should be
populated, and (b) where it should be placed.

Example initial file:
```
...some text before....

Contents:

toc

...some text after...
```

The file must contain a line that only contains the text `Contents` (with optional suffix symbols).
The very next non-blank line will be treated as the start of the TOC entries.

When a file with existing TOC entries is being processed, the existing entries are assumed to continue
until the next heading or horizontal line.

## Styles
Supports a few variations on rendering style:

### Default
The default style is as a single bullet-pointed list.

For example:
```
* [Chapter 1](#..)
  * [Section 1.1](#..)
  * [Section 1.1](#..)
* [Chapter 2](#..)
  * [Section 2.1](#..)
  * [Section 2.2](#..)
```

Spacing is dependent on where the original text placed an initial marker.

### --top
Top-level chapters (or parts) are listed without bullet points.

For example as

```
[Chapter 1](#..)
* [Section 1.1](#..)
* [Section 1.1](#..)

[Chapter 2](#..)
* [Section 2.1](#..)
* [Section 2.2](#..)
```

### --parts
Parts can use the same top-level heading style as chapters, but get rendered
automatically as 'top' style. 
 
For example:

```
[Part 1](#..)
* [Chapter 1](#..)
* [Chapter 2](#..)

[Part 2](#..)
* [Chapter 1](#..)
* [Chapter 2](#..)
```

## Style detection
Existing used style is automatically detected from the existing TOC entries.

The purpose here is to ensure that CLI args to setup the style don't have to be
repeated when re-running the command. Consequently, I intentionally don't try to
guess any style if there are no existing TOC entries.

## Setup in PyCharm
Open the existing folder as a new project.

Create run configurations to supply specific CLI args as required.

Right-click `PopulateTocTests.py` and run to run the unit tests.

## Running from command line
1. Activate the venv (optional)
2. `python PopulateToc.py` to run CLI
3. `python PopulateTocTests.py` to run the unit tests

## Python Venv
Latest update: I currently have no libraries required, so this script works in any standard Python 3 installation.
You can still choose to follow the below to create a virtualenv and load the empty requirements if you wish. 

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

#