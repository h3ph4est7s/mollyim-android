#! /usr/bin/env python3

import sys
import fnmatch
from zipfile import ZipFile


class ApkDiff:
    IGNORE_FILES = [
        # Related to app signing. Not expected to be present in unsigned builds. Doesn't affect app code.
        "META-INF/MANIFEST.MF",
        "META-INF/*.RSA",
        "META-INF/*.SF",
    ]

    # MOLLY: Allow to exclude files with glob patterns
    def isIncluded(self, filepath):
        for ignoreFile in self.IGNORE_FILES:
            if fnmatch.fnmatchcase(filepath, ignoreFile):
                return False
        return True

    def compare(self, firstApk, secondApk):
        firstZip = ZipFile(firstApk, 'r')
        secondZip = ZipFile(secondApk, 'r')

        if self.compareEntryNames(firstZip, secondZip) and self.compareEntryContents(firstZip, secondZip) == True:
            print("APKs match!")
            return True
        else:
            print("APKs don't match!")
            return False

    def compareEntryNames(self, firstZip, secondZip):
        firstNameListSorted = sorted(n for n in firstZip.namelist() if self.isIncluded(n))
        secondNameListSorted = sorted(n for n in secondZip.namelist() if self.isIncluded(n))

        if len(firstNameListSorted) != len(secondNameListSorted):
            print("Manifest lengths differ!")

        for (firstEntryName, secondEntryName) in zip(firstNameListSorted, secondNameListSorted):
            if firstEntryName != secondEntryName:
                print("Sorted manifests don't match, %s vs %s" % (firstEntryName, secondEntryName))
                return False

        return True

    def compareEntryContents(self, firstZip, secondZip):
        firstInfoList = list(filter(lambda info: self.isIncluded(info.filename), firstZip.infolist()))
        secondInfoList = list(filter(lambda info: self.isIncluded(info.filename), secondZip.infolist()))

        if len(firstInfoList) != len(secondInfoList):
            print("APK info lists of different length!")
            return False

        success = True
        for firstEntryInfo in firstInfoList:
            for secondEntryInfo in list(secondInfoList):
                if firstEntryInfo.filename == secondEntryInfo.filename:
                    firstEntryBytes = firstZip.read(firstEntryInfo.filename)
                    secondEntryBytes = secondZip.read(secondEntryInfo.filename)

                    if firstEntryBytes != secondEntryBytes:
                        firstZip.extract(firstEntryInfo, "mismatches/first")
                        secondZip.extract(secondEntryInfo, "mismatches/second")
                        print("APKs differ on file %s! Files extracted to the mismatches/ directory." % (firstEntryInfo.filename))
                        success = False

                    secondInfoList.remove(secondEntryInfo)
                    break

        return success


if __name__ == '__main__':
    if len(sys.argv) != 3:
        print("Usage: apkdiff <pathToFirstApk> <pathToSecondApk>")
        sys.exit(1)

    match = ApkDiff().compare(sys.argv[1], sys.argv[2])
    if not match:
        sys.exit(2)
