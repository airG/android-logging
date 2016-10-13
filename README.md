 [ ![Download](https://api.bintray.com/packages/airgoss/airGOss/logger/images/download.svg) ](https://bintray.com/airgoss/airGOss/logger/_latestVersion)

#Android Logging
The airG android logging library is a group of utilities for easier logging. airG android logger uses `android.util.log` methods to log so there's no special sauce and the same log granularity (`e`, `w`, `d`, `i`, `v`, etc.) is provided.

##Formatted Logging
Formatted messages (see [String Formatter](https://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html) documentation for reference) are easier to work with than concatenating bits and pieces of info.
###Example
`Log.d("LOGTAG", "There are %d items in category '%s'", categoryList.size(), categoryName)`

##Tagged Logger
Aside from the static methods, there is also a tagged logger which allows you to focus only on the log message itself and not the tag (the little things matter).
### Example
    public class ContactsActivity extends Activity {
        private final TaggedLogger LOG = Log.tag("CONTACTS");
        ...
        private void doSomething () {
           LOG.d("Look, Ma! No Hands!");
           // This translates to: Log.d ("CONTACTS", "Look, Ma! No Hands!")
        }
        ...
    }

##Contributions
Contributions are appreciated and welcome. In order to contribute to this repo please follow these steps:

1. Fork the repo
1. Add this repo as the `upstream` repo in your fork (`git remote add upstream git@github.com:airG/android-logging.git`)
1. Contribute (Be sure to format your code according to th included code style settings)
1. IMPORTANT: Rebase with upstream (`git pull --rebase upstream`)
1. Submit a pull request
