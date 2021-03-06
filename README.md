# LicenseChecker
LicenseChecker is a gradle plugin for Android that ensures all Open Source Libraries are properly attributed and will even generate a formatted HTML file that can be used to display the licenses in your app.

LicenseChecker was created as a sanity check to prevent me from releasing an app with unattributed Open Source Libraries. It is especially powerful for big teams where it is hard as an individual to manage all the dependencies.

# Installation

Add the following to your `build.gradle`:

```gradle
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.wardbonnefond:license-checker:0.1.6'
    }
}

apply plugin: 'com.android.application'

// Apply after the Android plugin
apply plugin: 'com.wardbonnefond.licensechecker'
```

# Usage
## Input
Provide an `attributions.json` file in your app module's directory. This file is compared against all libraries (with the `compile` prefix) in your app's `build.gradle` file. The final HTML output is generated using the data in this file. Any library in the `licenses` section will be added to the final HTML output. The `excludedLibraries` section is for libraries that don't need attribution (your own library, closed source, etc.). 

I use this tool for escaping strings in JSON: http://bernhardhaeussner.de/odd/json-escape/ 

```json
{
  "libraries": [
    {
      "name": "Fake Library",
      "legalText": "Text to display about the license",
      "gradlePackage": "com.fake.fakest:myFakeLibrary"
    },
    {
      "name": "Jane's Awesome Library",
      "legalText": "Text to display about the license",
      "gradlePackage": "com.jdoe:janes-awesome-library"
    }
  ],
  "excludedLibraries": [
    {
      "gradlePackage": "com.android.support:appcompat-v7"
    },
    {
      "gradlePackage": "com.android.support:support-v4"
    }
  ]
}
```

LicenseChecker supports `.jar` and `.aar` libraries added from you `/libs` folder. For instance, to include a library called `my-awesome-library.aar` in your `attributions.json` just remove the `.aar` extension:

```json
{
  "libraries": [
    {
      "name": "My Awesome Library",
      "legalText": "Text to display about the license",
      "gradlePackage": "my-awesome-library"
    }
  ]
}
```

`attributions.json` should be kept in-sync with your app's libraries. You'll still need to provide the attributions, LicenseChecker won't do this, it will just verify they exist and then provide a formatted output file.


By default LicenseChecker won't fail builds for missing attributions.In order to take advantage of this functionality you can specify the following property on your variants.


```gradle
android {
    buildTypes {
        release {
            ext.failOnMissingAttributions = true
        }
    }
}
```

LicenseChecker supports assemble tasks that build multiple variants.  If any variant has set `ext.failOnMissingAttributions = true` and there are missing attributions, then the entire build will fail.



## Output
By default the final HTML file will be output to `/{app}/build/intermediates/assets/{flavor}/{buildType}/`

Here's a [sample] output HTML file. And here's a screenshot of what it looks like in the app.

[sample]: <http://htmlpreview.github.io/?https://github.com/wbonnefond/LicenseChecker/blob/master/sample_output.html>

<img src="https://raw.githubusercontent.com/wbonnefond/LicenseChecker/master/sample_output.png" width="250">

# Contributing

Feel free to open an issue or pull request if you feel something is missing or can be improved upon!

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
