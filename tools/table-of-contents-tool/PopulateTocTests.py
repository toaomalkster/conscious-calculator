import PopulateToc
import unittest
import os
import glob
import difflib
import inspect


def text_of(filename):
    with open(filename, 'r', encoding='utf-8') as f:
        return f.readlines()


class MyTestCase(unittest.TestCase):
    target_dir = os.path.dirname(os.path.realpath(__file__)) + '/target'

    def setUp(self):
        if not os.path.exists(self.target_dir):
            os.makedirs(self.target_dir)
            print(f'Created temp folder: {self.target_dir}')
        files = glob.glob(f'{self.target_dir}/*.md')
        for file in files:
            try:
                os.remove(file)
            except Exception as e:
                print(f'Error while removing old file from target: {file} - {e}')

    def test_does_not_generate_given_no_toc_marker(self):
        name = inspect.stack()[0][3]
        PopulateToc.transform_file(
            'text-without-toc.md',
            f'target/{name}.output',
            None)
        if os.path.exists(f'target/{name}.output'):
            self.fail('Output file was generated when should not have been')

    def test_generates_new_simple_bulleted_toc(self):
        name = inspect.stack()[0][3]
        self.copy_text_with_new_toc(
            'resources/text-with-simple-bulleted-toc.md',
            f'target/{name}.src',
            toc_lines=[
                'toc\n'
            ])
        PopulateToc.transform_file(
            f'target/{name}.src',
            f'target/{name}.output',
            None)
        self.assert_has_expected_content(
            f'target/{name}.output',
            'resources/text-with-simple-bulleted-toc.md')

    def test_replaces_simple_bulleted_toc(self):
        name = inspect.stack()[0][3]
        PopulateToc.transform_file(
            'resources/text-with-simple-bulleted-toc.md',
            f'target/{name}.output',
            None)
        self.assert_has_expected_content(
            f'target/{name}.output',
            'resources/text-with-simple-bulleted-toc.md')

    def test_replaces_toc_with_top_level_chapters(self):
        name = inspect.stack()[0][3]
        PopulateToc.transform_file(
            'resources/text-with-top-level-chapters.md',
            f'target/{name}.output',
            None)
        self.assert_has_expected_content(
            f'target/{name}.output',
            'resources/text-with-top-level-chapters.md')

    def assert_has_expected_content(self, expected_file, actual_file):
        expected = text_of(expected_file)
        actual = text_of(actual_file)
        if actual != expected:
            diff = ''.join(difflib.ndiff(actual, expected))
            self.fail(f'{actual_file} has wrong content:\n{diff}')


    def copy_text_with_new_toc(self, source_file, dest_file, toc_lines):
        try:
            with open(source_file, 'r', encoding='utf-8') as f:
                lines = f.readlines()
            toc_bounds = PopulateToc.get_toc_line_bounds(lines)
        except Exception as e:
            print(f'Error while setting up text: {e}')
            self.fail(e)

        try:
            with open(dest_file, 'w', encoding='utf-8') as f:
                f.writelines(lines[0:toc_bounds[0]])
                f.writelines(toc_lines)
                f.writelines(lines[toc_bounds[1]:])
        except Exception as e:
            print(f'Error while setting up text: {e}')
            self.fail(e)


if __name__ == '__main__':
    unittest.main()
