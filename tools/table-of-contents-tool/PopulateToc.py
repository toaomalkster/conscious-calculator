# References:
# https://www.programiz.com/python-programming/regex
import re
import argparse
import sys


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


# Tests whether the existing toc lines use the 'top' style.
# Under the 'top' style, top-level chapters or parts are listed without bullet points. Their sub-sections
# are listed as bullet points each under top-level entry, creating a visual group. And there are line-spaces between
# each visual group.
def is_toc_style_top(toc_lines):
    return toc_lines[0].startswith('[')


# Searches lines given capturing any headings and turning them into TOC entries.
# Searches the entire contents given, so any non-indexed header section needs to be stripped beforehand.
# return [lines-with-newline-terminators]
def get_toc_entries(lines, use_top):
    entries = []
    first = True
    for line in lines:
        match = re.match(r'^(#+) .*$', line)
        if match and len(match.group(1)) <= 3:
            entries.append(convert_heading_to_toc_entry(line, first, use_top))
            first = False
    return entries


# returns newline-terminated string
def convert_heading_to_toc_entry(line, first, use_top):
    match = re.match(r'^(#+) (.*)$', line)
    level = len(match.group(1))
    heading = match.group(2)
    href = convert_heading_to_href(heading)
    if use_top:
        level -= 1
    if level <= 0:
        prefix_line = '' if first else '\n'
        return f'{prefix_line}[{heading}]({href})\n'
    else:
        indent = '  ' * (level - 1)
        return f'{indent}* [{heading}]({href})\n'


# eg: "II.1 Interlude: Environment, Body, and Control Processes (first part)"
# becomes: `#ii1-interlude-environment-body-and-control-processes-first-part`
def convert_heading_to_href(heading):
    # remove leading header indication
    heading = re.sub(r'^#+ ', '', heading)
    # remove all but alphanumeric, hyphens, and spaces
    heading = re.sub(r'[^0-9a-zA-Z -]', '', heading)
    # convert to lowercase
    heading = heading.lower()
    # turn all spaces into hyphens
    heading = re.sub(r' ', '-', heading)
    # format as href
    return f'#{heading}'


def transform_file(path, output, use_top):
    # read all lines and identify whether file should be processed
    try:
        with open(path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        toc_bounds = get_toc_line_bounds(lines)
    except Exception as e:
        print(f'{path} - Skipping: {e}')
        return

    # process
    print(f'{path} - Processing')
    existing_toc = lines[toc_bounds[0]:toc_bounds[1]]
    use_top = use_top if use_top is not None else is_toc_style_top(existing_toc)
    entries = get_toc_entries(lines[toc_bounds[1]:], use_top)

    # save result, either overwriting original file or saving to a new file
    with open(output, 'w', encoding='utf-8') as f:
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
    cli.add_argument('-o', '--output',
                     help='Output file. Error if used with multiple source files. Default: replaces source files')
    cli.add_argument('-t', '--top', choices=['true', 'false'], const='true', default=None,
                     help='Top level chapters or parts should appear without bullet points.'
                          ' Default: infers from existing text')

    # Parse and validate CLI arguments
    args = cli.parse_args()
    if args.output and len(args.file) > 1:
        print('Error: output option can only be used in single file mode.', sys.stderr)
        exit(1)

    # Run
    for filename in args.file:
        if args.output:
            transform_file(filename, args.output, args.top)
        else:
            transform_file(filename, filename, args.top)
