import PopulateToc
import unittest
import os
import glob
import difflib


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

    def test_generates_new_simple_bulleted_toc(self):
        PopulateToc.transform_file(
            'resources/text-with-toc-marker-only.md',
            'target/text-with-toc-marker-only.output')
        self.assert_has_expected_content(
            'target/text-with-toc-marker-only.output',
            'resources/expected-with-simple-bulleted-toc-with-blank-line.md')

    def test_replaces_simple_bulleted_toc(self):
        PopulateToc.transform_file(
            'resources/text-with-simple-bulleted-toc.md',
            'target/text-with-simple-bulleted-toc.output')
        self.assert_has_expected_content(
            'target/text-with-simple-bulleted-toc.output',
            'resources/expected-with-simple-bulleted-toc-without-blank-line.md')

    def assert_has_expected_content(self, expected_file, actual_file):
        expected = text_of(expected_file)
        actual = text_of(actual_file)
        if actual != expected:
            diff = ''.join(difflib.ndiff(expected, actual))
            self.fail(f'{actual_file} has wrong content:\n{diff}')


if __name__ == '__main__':
    unittest.main()
