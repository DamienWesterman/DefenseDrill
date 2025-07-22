#!/bin/python3

# /****************************\
# *      ________________      *
# *     /  _             \     *
# *     \   \ |\   _  \  /     *
# *      \  / | \ / \  \/      *
# *      /  \ | / | /  /\      *
# *     /  _/ |/  \__ /  \     *
# *     \________________/     *
# *                            *
# \****************************/
#
# Copyright 2025 Damien Westerman
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from argparse import ArgumentParser
import os
import re
import json

def run_build(args):
    APK_SIGNED_PATH = 'app/build/outputs/apk/release/app-release.apk'
    OUTPUT_DIR = os.path.expanduser(args.output)
    APK_OUTPUT_PATH = OUTPUT_DIR + 'DefenseDrill.apk'
    VERSION_OUTPUT_PATH = OUTPUT_DIR + 'DefenseDrill.version'

    print('Building and signing the release apk')
    print('###########################################################')
    if 0 != os.system('./gradlew assembleRelease'):
        print('Failed to build the apk!')
        return

    print('\n\nMoving the apk to the output directory')
    print('###########################################################')
    if 0 != os.system(f"cp {APK_SIGNED_PATH} {APK_OUTPUT_PATH}"):
        print('Failed to copy the apk to the output directory!')
        return

    print(f"\n\nDone! Signed release apk is located in: {APK_OUTPUT_PATH}")

    print('\n\nCreating the version file')
    print('###########################################################')
    with open('app/build.gradle', 'r') as file:
        content = file.read()

    match_version_code = re.search(r'versionCode\s+(\d+)', content)
    match_version_name = re.search(r'versionName\s+"([^"]+)"', content)

    version_code = match_version_code.group(1) if match_version_code else None
    version_name = match_version_name.group(1) if match_version_name else None

    if not version_code or not version_name:
        print('Failed to extract version code and version name, json file not created')
        return

    print(f"Creating version file using versionCode <{version_code}> and versionName <{version_name}>")
    with open(VERSION_OUTPUT_PATH, 'w+') as output_file:
        json.dump({
            "versionCode": int(version_code),
            "versionName": version_name
        }, output_file)

    print(f"\n\nDone! Version file created at: {VERSION_OUTPUT_PATH}")

if __name__ == '__main__':
    parser = ArgumentParser(
        prog='BuildApk.py',
        description='Builds a signed APK for DefenseDrill and copies it to the desired output directory. Also writes the version information in a json in the output directory to be served to the app.')

    parser.add_argument('-o', '--output', required=True,
                        help='Output directory to put the resulting files. Should be in the DefenseDrillMVC/src/main/resources/static/ directory.')

    run_build(parser.parse_args())
