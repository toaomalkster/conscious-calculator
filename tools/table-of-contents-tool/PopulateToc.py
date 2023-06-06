# See readme.md for description.

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
    # - find last line that looks like an existing toc entry
    # - stop looking when encountering a major new section
    found = False
    idx += 1
    for i, line in enumerate(lines[idx:], start=idx):
        if line.strip().startswith('[') or line.strip().startswith('* '):
            idx = i+1
        if line.startswith('#') or line.startswith('---'):
            found = True
            break
    if not found:
        raise Exception('No end of existing TOC entries found following contents section marker')
    bounds[1] = idx

    return bounds


# Tests whether the existing toc entries use the 'top' style.
def is_toc_style_top(toc_entries):
    return toc_entries[0].startswith('[')


# Tests whether the existing toc entries use the 'parts' style.
# This is assumed if we can find an early example where the top-level entry uses top style, is prefixed with "Part ",
# and is followed by a sub-entry.
def is_toc_style_parts(toc_entries):
    for i, entry in enumerate(toc_entries):
        if entry.startswith('[Part ') and (i+1) < len(toc_entries) and toc_entries[i+1].startswith('* '):
            # early stopping case: found definite positive example
            return True
        if entry.startswith('[') and not entry.startswith('[Part '):
            # early stopping case: found definite negative before finding a positive example
            return False
    # default assumption
    return False


# Searches lines given capturing any headings and turning them into TOC entries.
# Searches the entire contents given, so any non-indexed header section needs to be stripped beforehand.
# return [lines-with-newline-terminators]
def get_toc_entries(lines, use_top, use_parts):
    entries = []
    first = True
    for line in lines:
        match = re.match(r'^(#+) .*$', line)
        if match and len(match.group(1)) <= 3:
            entries.append(convert_heading_to_toc_entry(line, first, use_top, use_parts))
            first = False
    return entries


# returns newline-terminated string
def convert_heading_to_toc_entry(line, first, use_top, use_parts):
    match = re.match(r'^(#+) (.*)$', line)
    level = len(match.group(1))
    heading = match.group(2)
    href = convert_heading_to_href(heading)

    # adjust for 'top' and 'parts'
    if use_parts and level == 1 and heading.startswith('Part '):
        level = 0
    if use_parts and not use_top:
        # While not using 'top', levels should start at 1
        level += 1
    elif use_top and not use_parts:
        # While using 'top' without 'parts' then level 1 headings should be shifted to level 0
        level -= 1

    # render according to level
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


def transform_file(path, output, use_top, use_parts):
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
    use_parts = use_parts if use_parts is not None else is_toc_style_parts(existing_toc)
    if use_parts:
        use_top = use_top if use_top is not None else True
    else:
        use_top = use_top if use_top is not None else is_toc_style_top(existing_toc)
    entries = get_toc_entries(lines[toc_bounds[1]:], use_top, use_parts)

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
                          ' Default: infers from existing TOC entries')
    cli.add_argument('-p', '--parts', choices=['true', 'false'], const='true', default=None,
                     help='Parts use the same h1 header as chapters. Implies top, but can be overridden.'
                          ' Default: infers from existing TOC entries')

    # Parse and validate CLI arguments
    args = cli.parse_args()
    if args.output and len(args.file) > 1:
        print('Error: output option can only be used in single file mode.', sys.stderr)
        exit(1)

    # Run
    for filename in args.file:
        dest_file = args.output if args.output else filename
        transform_file(filename, dest_file, args.top, args.parts)
