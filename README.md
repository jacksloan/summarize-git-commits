# summarize-git-commits
Summarize git commits by author and number of regex matches in commit messages

In this example, the program will recursively walk the `C:\dev\git` folder and search for git directories with a maximum depth of 6 nested directories.

##### Example Input:
```
Path to directory containing repos: C:\dev\git
Regex string to summarize: d'{0,1}oh
```

##### Example Output (stdout):
```
CommitSummary(author=abc@xyz.com, regexString=d'{0,1}oh, count=57)
CommitSummary(author=efg@xyz.com, regexString=d'{0,1}oh, count=42)
```

### Build:
```
mvn clean package
```

### Run:
```
# for help
java -jar target/git-log-1.0-SNAPSHOT-jar-with-dependencies.jar --help

# with args
java -jar target/git-log-1.0-SNAPSHOT-jar-with-dependencies.jar --parent-path="C:\path\to\dir" --regex-pattern="d'{0,1}oh"

# using prompts
java -jar target/git-log-1.0-SNAPSHOT-jar-with-dependencies.jar 
