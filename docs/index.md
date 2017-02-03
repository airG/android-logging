 [![Build Status](https://travis-ci.org/airG/android-logging.svg?branch=master)](https://travis-ci.org/airG/android-logging)
 [![Download](https://api.bintray.com/packages/airgoss/airGOss/logger/images/download.svg) ](https://bintray.com/airgoss/airGOss/logger/_latestVersion)

# Android Logging
The airG android logging library is a group of utilities for easier logging. airG android logger uses `android.util.log` methods to log so there's no special sauce and the same log granularity (`e`, `w`, `d`, `i`, `v`, etc.) is provided.

## Formatted Logging
Formatted messages (see [String Formatter](https://developer.android.com/reference/java/util/Formatter.html) documentation for reference) are easier to work with than concatenating bits and pieces of info.
### Example
`Log.d("LOGTAG", "There are %d items in category '%s'", categoryList.size(), categoryName)`

## Tagged Logger
Aside from the static methods, there is also a tagged logger which allows you to focus only on the log message itself and not the tag (the little things matter).

### Example
    public class ContactsActivity extends Activity {
        private final TaggedLogger LOG = Logger.tag("CONTACTS");
        ...
        private void doSomething () {
           LOG.d("Look, Ma! No Hands!");
           // This translates to: Log.d ("CONTACTS", "Look, Ma! No Hands!")
        }
        ...
    }

## Usage
To use the _android-logging_ library in your builds, add the following line to your Gradle build script:

`compile 'com.airg.android:logger:+@aar'`

Or download the library: [![Download](https://api.bintray.com/packages/airgoss/airGOss/logger/images/download.svg) ](https://bintray.com/airgoss/airGOss/logger/_latestVersion)

## Contributions
Please refer to the [contribution instructions](https://airg.github.io/#contribute).
