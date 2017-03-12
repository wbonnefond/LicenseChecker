# LicenseChecker
LicenseChecker is a gradle plugin for Android that ensures all Open Source Libraries are properly attributed and will even generate a formatted HTML file that can be used to display the licenses in your app.

LicenseChecker was created as a sanity check to prevent me from releasing an app with unattributed Open Source Libraries.  It is especially powerful for big teams where it is hard as an individual to manage all the dependencies.

# Usage
#### Input
Provide a `license.json` file in your app's directory.  This file is compared against all libraries (with the `compile` prefix) in your app's `build.gradle` file. The final HTML output is generated using the data in this file. Any library in the `licenses` section will be added to the final HTML output. The `excludedPackages` section is for libraries that don't need attribution (your own library, closed source, etc.).

```json
{
  "licenses": [
    {
      "name": "Fake Library",
      "author": "John Doe",
      "license": "License",
      "licenseText": "Text to display about the license",
      "gradlePackage": "com.fake.fakest:myFakeLibrary"
    },
    {
      "name": "Jane's Awesome Library",
      "author": "Jane Doe",
      "license": "License",
      "licenseText": "Text to display about the license",
      "gradlePackage": "com.jdoe:janes-awesome-library"
    }
  ],
  "excludedPackages": [
    {
      "gradlePackage": "com.android.support:appcompat-v7"
    },
    {
      "gradlePackage": "com.android.support:support-v4"
    }
  ]
}
```

`licenses.json` should be kept in-sync with your app's libraries.  You'll still need to provide the attributions, LicenseChecker won't do this, it will just verify they exist and then provide a formatted output file.

By default LicenseChecker won't fail builds for missing attributions. In order to take advantage of this functionality you can specify the following property on your variants.

```gradle
android {
    buildTypes {
        release {
            ext.failOnMissingAttributions = true
        }
    }
}
```


#### Output
By default the final HTML file will be output to `/{app}/src/main/assets/open_source_licenses.html`


# Known Limitations and Future Work

* Currently won't work if the app has `productFlavors`, only `buildTypes`



# License
```
MIT License

Copyright (c) 2017 Ward Bonnefond

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```