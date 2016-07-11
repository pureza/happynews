# HappyNews

HappyNews is a simple [NNTP](https://en.wikipedia.org/wiki/Network_News_Transfer_Protocol)
server (Usenet newsgroups) with some extra features. It is compatible with
[Mozilla Thunderbird](https://www.mozilla.org/en-GB/thunderbird/).


## Remarks

This project was originally written in 2004 as a learning exercise in developing
networking software. As such, it is not supposed to be used in a production
environment. In fact, the server supports just enough of the NNTP protocol in
order to be usable with Thunderbird as a client. Moreover, it was not built with
performance in mind, as it follows the one thread per connection model, which is
not exactly the recommended way to build high-throughput applications these days.

Nonetheless, the code was tested and revised in 2016. It works reasonably well
and is simple to understand.


## Building from source

### Requirements

In order to build and run this project, you will need:

* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or greater
* [Apache Maven](https://maven.apache.org/)


### Compiling

```bash
 $ mvn package
```

This should generate the `target/happynews-1.0.jar` file.


## How to run it

To run the server, just do:

```bash
 $ java -jar target/happynews-1.0.jar
12:17:20 INFO  main                 UserRepository                           2 users loaded
12:17:20 INFO  main                 GroupRepository                          4 groups loaded
12:17:20 INFO  main                 ArticleRepository                        3 articles found
12:17:20 INFO  Thread-2             NNTPServer                               Server up & listening on 1119
...
```

As to the client, you can either configure an NNTP client or just use *telnet*:

```bash
$ telnet localhost 1119
Trying ::1...
Connected to localhost.
Escape character is '^]'.
480 server ready - authentication required
AUTHINFO USER admin
381 password please...
AUTHINFO PASSWORD admin
281 Authentication accepted
quit
Connection closed by foreign host.
```

`admin`/`admin` is the username/password for the default account.


### Example session

#### Authentication

```
480 server ready - authentication required
AUTHINFO USER admin
381 password please...
AUTHINFO PASSWORD admin
281 Authentication accepted
```

#### Listing groups

```
LIST
215 list of newsgroups follows
happynews.users 2 1 y
happynews.dev 0 1 y
.
```

#### Creating a new group

```
NEWGROUP news
250 group created

LIST
215 list of newsgroups follows
news 0 1 y
happynews.users 2 1 y
happynews.dev 0 1 y
.
```


#### Posting a new message

```
POST
340 send article to be posted. End with <CR-LF>.<CR-LF>
Newsgroups: news
Subject: Latest news!
From: <admin@happynews>

Nothing's happened recently! Stay tuned for more news!
.
240 article posted ok
```

#### Selecting a Newsgroup

```
GROUP news
211 1 1 1 news group selected
```

#### Reading an article

```
ARTICLE 1
220 1 <3@happynews> article retrieved - head and body follows
newsgroups: news
subject: Latest news!
from: <admin@happynews>
date: Mon, 11 Jul 16 13:50:56
message-id: <3@happynews>

Nothing's happened recently! Stay tuned for more news!
.
```


#### Exiting

```
quit
```


#### And more...

Use the `HELP` command to list all the available commands and their
description:

```
HELP
 - Generic commands:

   --------------------- Navigation --------------------
   GROUP      Switch to a new newsgroup
   LIST       List all newsgroups
   NEWGROUPS  List all newsgroups created after the given date
   NEWNEWS    List all articles published after the given date
   ------------------ Reading articles -----------------
   ARTICLE    Display an article
   HEAD       Display the header of an article
   BODY       Display the body of an article
   STAT       Update the pointer to the current article
   NEXT       Go to the next article
   LAST       Go to the previous article
   XOVER      Display a summary of a set of articles
   FIND       Look for a string or regular expression within the body of all articles
   FINDHEADER Look for a string or regular expression within the header of all articles
   -------------------- Not supported ------------------
   IHAVE
   MODE
   SLAVE
   ---------------- Account management  ----------------
   PASSWD     Change password

 - Editor commands:

   ----------------- Article management ----------------
   POST       Publish an article
   ------------ Working directory management -----------
   CD         Change the current working directory
   PWD        Print the current working directory
   LS         Display the content of a directory
   MKDIR      Create a new directory
   RMDIR      Remove a directory
   MV         Move an article
   RM         Remove an article

 - Administrator commands:

   ------------------ Group management -----------------
   NEWGROUP   Create a new newsgroup
   ------------------ User management ------------------
   USERLIST   List all users in the system
   USERADD    Add a user
   USERCH     Update the user role
   USERRM     Remove a user
.
```

## License

This project is released under the MIT license. See the LICENSE file for details.
