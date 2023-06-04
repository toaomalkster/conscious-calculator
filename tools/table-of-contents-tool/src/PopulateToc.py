# References:
# https://www.programiz.com/python-programming/regex
import re
import argparse


# Gets the bounds of the TOC listing, excluding the TOC header.
# return [start (inclusive), end (exclusive)]
def get_toc_line_bounds(lines):
    bounds = [-1, -1]
    # find 'contents' marker
    idx = -1
    found = False
    for i, line in enumerate(lines):
        if re.match(r'^contents[^0-9a-zA-Z]*$', line, re.IGNORECASE):
            idx = i
            found = True
            break
    if not found:
        raise Exception('No marker found indicating location for contents section')

    # find start of existing toc entries
    found = False
    idx += 1
    for i, line in enumerate(lines[idx:], start=idx):
        if line.strip():
            idx = i
            found = True
            break
    if not found:
        raise Exception('No start of existing TOC entries found following contents section marker')
    bounds[0] = idx

    # find end of existing toc entries
    found = False
    idx += 1
    for i, line in enumerate(lines[idx:], start=idx):
        if not line.strip() or line.startswith('#'):
            idx = i
            found = True
            break
    if not found:
        raise Exception('No end of existing TOC entries found following contents section marker')
    bounds[1] = idx

    return bounds


# Searches lines given capturing any headings and turning them into TOC entries.
# Searches the entire contents given, so any non-indexed header section needs to be stripped beforehand.
# return [lines-with-newline-terminators]
def get_toc_entries(lines):
    entries = []
    for line in lines:
        match = re.match(r'^(#+) .*$', line)
        if match and len(match.group(1)) <= 3:
            entries.append(convert_heading_to_toc_entry(line))
    return entries


# returns newline-terminated string
def convert_heading_to_toc_entry(line):
    match = re.match(r'^(#+) (.*)$', line)
    level = len(match.group(1))
    heading = match.group(2)
    href = convert_heading_to_href(heading)
    indent = '  ' * (level-1)
    return f'{indent}* [{heading}]({href})\n'


# eg: "II.1 Interlude: Environment, Body, and Control Processes (first part)"
# becomes: `#ii1-interlude-environment-body-and-control-processes-first-part`
def convert_heading_to_href(heading):
    # remove leading header indication
    heading = re.sub(r'^#+ ', '', heading)
    # remove all but alphanumeric and spaces
    heading = re.sub(r'[^0-9a-zA-Z ]', '', heading)
    # convert to lowercase
    heading = heading.lower()
    # turn all spaces into hyphens
    heading = re.sub(r' ', '-', heading)
    # format as href
    return f'#{heading}'


def transform_file_inplace(path):
    # read all lines and identify whether file should be processed
    try:
        with open(path, 'r') as f:
            lines = f.readlines()
        toc_bounds = get_toc_line_bounds(lines)
    except Exception as e:
        print(f'{path} - Skipping: {e}')
        return

    # process
    print(f'{path} - Processing')
    entries = get_toc_entries(lines[toc_bounds[1]:])

    # save result, overwriting original file
    with open(path, "w") as f:
        # original lines _before_ TOC entries (including TOC header)
        f.writelines(lines[0:toc_bounds[0]])

        # TOC entries
        f.writelines(entries)

        # original lines _after_ TOC entries
        f.writelines(lines[toc_bounds[1]:])


if __name__ == '__main__':
    # Initialize CLI parser
    cli = argparse.ArgumentParser(description='Table of contents populator for markdown files.')
    cli.add_argument('file', nargs='+', help='Markdown file(s) to process')

    # Parse and process CLI arguments
    args = cli.parse_args()

    # Run
    for filename in args.file:
        transform_file_inplace(filename)
